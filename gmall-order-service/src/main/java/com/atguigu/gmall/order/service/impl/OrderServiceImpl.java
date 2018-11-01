package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.CartItem;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.CartVo;
import com.atguigu.gmall.cart.SkuItem;
import com.atguigu.gmall.constant.CartConstant;
import com.atguigu.gmall.order.*;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.mapper.UserAddressMapper;
import com.atguigu.gmall.user.UserAddress;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    JedisPool jedisPool;

    @Reference
    CartService cartService;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;


    @Override
    public String createTradeToken() {

        String token = UUID.randomUUID().toString().replaceAll("-", "");
        Jedis jedis = jedisPool.getResource();

        //服务端设置了令牌
        jedis.setex(token,60*3,"1111");//只要把令牌放进redis中就可以方便检查，值是乱写的

        return token;
    }

    /**
     * 验证令牌
     * @param token
     * @return
     */
    @Override
    public boolean verfyToken(String token) {
        Jedis jedis = jedisPool.getResource();
        Long del = jedis.del(token);
        return del==1?true:false;
    }

    @Override
    public List<String> verfyStock(Integer userId) {
        List<CartItem> cartItems = cartService.getCartInfoCheckedList(userId);
        List<String> result = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            try {
                //第一个库存
                boolean b = stockCheck(cartItem.getSkuItem().getId(), cartItem.getNum());
                if(!b){
                    //验证失败
                    result.add(cartItem.getSkuItem().getSkuName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public UserAddress getUserAddressById(Integer userAddressId) {
        return userAddressMapper.selectById(userAddressId);
    }

    @Transactional
    @Override
    public void createOrder(Integer userId, OrderInfoTo orderInfoTo) {
        //找到购物车中所有需要下单的商品
        List<CartItem> cartItems = cartService.getCartInfoCheckedList(userId);
        CartVo cartVo = new CartVo();
        cartVo.setCartItems(cartItems);
        BigDecimal totalPrice = cartVo.getTotalPrice();//计算总价

        //插入订单的信息
        OrderInfo orderInfo = new OrderInfo();//总订单
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setPaymentWay(PaymentWay.ONLINE);
        orderInfo.setCreateTime(new Date());
        //30分钟不用就过期
        long l=System.currentTimeMillis()+1000*60*30;
        orderInfo.setExpireTime(new Date(l));
        orderInfo.setUserId(userId);
        //设置收货人等信息
        orderInfo.setConsignee(orderInfoTo.getConsignee());
        orderInfo.setConsigneeTel(orderInfoTo.getConsigneeTel());
        orderInfo.setDeliveryAddress(orderInfoTo.getDeliveryAddress());
        orderInfo.setOrderComment(orderInfoTo.getOrderComment());
        //对外业务号
        orderInfo.setOutTradeNo("ATGUIGU_"+ System.currentTimeMillis()+"_"+userId);
        orderInfo.setTotalAmount(totalPrice);
        orderInfoMapper.insert(orderInfo);

        //2、这些商品对应的是OrderDetail；
        for (CartItem cartItem : cartItems) {
            SkuItem skuItem = cartItem.getSkuItem();
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImgUrl(skuItem.getSkuDefaultImg());
            orderDetail.setOrderId(orderInfo.getId());
            orderDetail.setOrderPrice(skuItem.getPrice());
            orderDetail.setSkuId(skuItem.getId());
            orderDetail.setSkuName(skuItem.getSkuName());
            orderDetail.setSkuNum(cartItem.getNum());
            orderDetailMapper.insert(orderDetail);

        }
        //3.所有的orderDetail才组成一个orderInfo
        //以上完成了，删除购物车中已经结算的东西
        Jedis jedis = jedisPool.getResource();
        String[] delStrIds = new String[cartItems.size()];
        for (int i=0;i<cartItems.size();i++){
            delStrIds[i]=cartItems.get(i).getSkuItem().getId()+"";
        }
        //删除购物车数据
        jedis.hdel(CartConstant.USER_CART_PREFIX+userId,delStrIds);

        //redis购物车原来列表顺序
        String fieldOrder = jedis.hget(CartConstant.USER_CART_PREFIX + userId, "fieldOrder");
        List list = JSON.parseObject(fieldOrder, List.class);

        //购物车中新列表顺序
        List<Integer> newfieldOrder = new ArrayList<>();
        //遍历原来列表的顺序，只要不是删除项都可以添加到新列表中
        for (Object o : list) {
            Integer id = Integer.parseInt(o.toString());
            boolean exist=false;
            for (CartItem cartItem : cartItems) {
                if(cartItem.getSkuItem().getId() ==id){
                    //原列表的id项在删除项中有
                    exist=true;
                }
            }
            if(!exist){
                //如果没有就添加到新列表顺序中
                newfieldOrder.add(id);
            }

        }
        //更新列表顺序
        jedis.hset(CartConstant.USER_CART_PREFIX+userId,"fieldOrder",JSON.toJSONString(newfieldOrder));
        jedis.close();

    }


    /**
     * 第三方的所有功能怎么掉？
     *  发请求
     *
     * @param skuId
     * @param num
     * @return
     * @throws IOException
     */
    private boolean stockCheck(Integer skuId,Integer num) throws IOException {
        //1、验证用户购物车里面勾选的商品的每一个库存是否足够
        //1）、HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        //2）、
        HttpGet httpGet = new HttpGet("http://www.gware.com:9001/hasStock?skuId="+skuId+"&num="+num);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            // 404 NOTFOUD
            //获取响应体
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent(); //0或者1
            String data = EntityUtils.toString(entity);
            return  "0".equals(data)?false:true;

        }  finally {
            //关响应
            response.close();
        }
    }


}
