package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.CartItem;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.CartVo;
import com.atguigu.gmall.cart.SkuItem;
import com.atguigu.gmall.constant.CartConstant;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.sku.SkuInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    JedisPool jedisPool;

    @Reference
    SkuService skuService;

    @Override
    public String addToCartUnLogin(Integer skuId, String cartKey, Integer num) {
        Jedis jedis = jedisPool.getResource();

        if(!StringUtils.isEmpty(cartKey)){
            //之前创建过购物车
            Boolean exists = jedis.exists(cartKey); //判断是不是伪造的cartKey
            if(exists ==false){
                //传来的购物车这个键不存在；也要新建购物车；假设传来的是非法的 cart-key:ddddddd
                String newCartKey = creatCart(skuId, num, false, null);
                return  newCartKey;
            }else {
                //向已有的购物车中添加商品，有就商品就增加数量，没有就新建商品放进去，
                // 把代码抽取了处出来，没有登录的和登录用的购物车都能用
                addCartItem(jedis,cartKey,skuId,num);
            }
        }else {
            //无购物车就新建
            return creatCart(skuId,num,false,null);
        }
        jedis.close();
        //返回之前的cart-key
        return cartKey;
    }

    @Override
    public void addToCartLogin(Integer skuId, Integer userId, Integer num) {
                //登录后加入购物车
        Jedis jedis = jedisPool.getResource();
        Boolean exists = jedis.exists(CartConstant.USER_CART_PREFIX+userId);
        if(exists){
            //用户这个购物车有
            String cartKey = CartConstant.USER_CART_PREFIX + userId;
//            String hget = jedis.hget(cartKey, skuId + "");
//            if(!StringUtils.isEmpty(hget)){
//                //有这个商品叠加数量
//            }else {
//                //没有这个商品新增商品
//            }
            addCartItem(jedis,cartKey,skuId,num);

        }else {
            //用户还没有这个购物车
            //新建购物车加商品
            String newCartKey = creatCart(skuId, num, true, userId);
        }
    }

    @Override
    public CartVo getYourCart(String cartKey) {
        return null;
    }

    @Override
    public void mergeCart(String cartKey, Integer userId) {
            //合并购物车
        //1.查出临时购物车的所有数据
        List<CartItem> cartItems = getCartInfoList(cartKey, false);
        if(cartItems!=null&& cartItems.size()>0){
            for (CartItem tempCartItem : cartItems) {
                //依次将临时购物车中的数据添加到用户购物车中
                addToCartLogin(tempCartItem.getSkuItem().getId(),userId,tempCartItem.getNum());
            }
        }
        //合并完成，删除redis中的临时购物车
        Jedis jedis = jedisPool.getResource();
        jedis.del(cartKey);
    }

    /**
     * 根据购物车id和商品id，从购物车中查出商品的详情
     * @param cartKey
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItemInfo(String cartKey, Integer skuId) {
        Jedis jedis = jedisPool.getResource();
        String json = jedis.hget(cartKey, skuId + "");
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        return cartItem;
    }

    /**
     * 查询购物车数据
     * @param cartKey 登录了传的是用户id
     * @param login
     * @return
     */
    @Override
    public List<CartItem> getCartInfoList(String cartKey, boolean login) {
        String queryKey=cartKey;
        if(login){
            queryKey = CartConstant.USER_CART_PREFIX + cartKey;

        }
        //查redis中这个key对应的购物车数据
        Jedis jedis = jedisPool.getResource();
        List<CartItem> cartItemList= new ArrayList<>();
        String fieldOrder = jedis.hget(queryKey, "fieldOrder");
        List list = JSON.parseObject(fieldOrder, List.class);
        for (Object o : list) {
            int inSort = Integer.parseInt(o.toString());
            String hget = jedis.hget(queryKey, inSort + "");
            CartItem cartItem = JSON.parseObject(hget, CartItem.class);
            cartItemList.add(cartItem);
        }

//        Map<String, String> hgetAll = jedis.hgetAll(queryKey);
//        for(Map.Entry<String,String> entry:hgetAll.entrySet()){
//            String key = entry.getKey();
//            String value = entry.getValue();
//            //转化
//         //   String value1="{\"check\":false,\"num\":9,\"skuItem\":{\"catalog3Id\":61,\"id\":58,\"price\":1,\"skuDefaultImg\":\"http://file.gmall.com/group1/M00/00/00/wKiZiFvPIBSAbDRFAABSLTTn9nI9569697\",\"skuDesc\":\"谁买谁是锤子2\",\"skuName\":\"锤子2\",\"spuId\":61,\"weight\":0.10},\"totalPrice\":9}";
//           CartItem cartItem = JSON.parseObject(value, CartItem.class);
//           // System.out.println(value1);
//            cartItemList.add(cartItem);
//        }

        return cartItemList;
    }

    /**
     * 勾选某个商品
     * @param skuId 商品id
     * @param checkFlag 是否勾选
     * @param tempCartKey 临时购物车id
     * @param userId  用户id
     * @param loginFlag 是否登录
     */
    @Override
    public void checkItem(Integer skuId, Boolean checkFlag, String tempCartKey, int userId, boolean loginFlag) {
        //购物车勾选
        String cartKey=loginFlag?CartConstant.USER_CART_PREFIX+userId:tempCartKey;
        CartItem cartItem = getCartItemInfo(cartKey, skuId);
        //设置勾选状态
        cartItem.setCheck(checkFlag);
        //修改购物车数据
        String jsonString = JSON.toJSONString(cartItem);
        Jedis jedis = jedisPool.getResource();
        jedis.hset(cartKey,skuId+"",jsonString);
        jedis.close();
    }

    @Override
    public List<CartItem> getCartInfoCheckedList(int id) {
        Jedis jedis = jedisPool.getResource();
        String cartKey=CartConstant.USER_CART_PREFIX+id;
        //1、获取购物车所有商品
        Map<String, String> stringMap = jedis.hgetAll(cartKey);
        //2.保存所有被选中的项目
        List<CartItem> checkedItems=new ArrayList<>();
        List<CartItem> cartInfoList = getCartInfoList(id+"", true);//老师写的是id
        if (cartInfoList==null){
            return  null;
        }
        for (CartItem cartItem : cartInfoList) {
            if(cartItem.isCheck()){
                checkedItems.add(cartItem);
            }
        }
        //返回被候选的
        if (checkedItems.size()==0){
            return  null;
        }
        jedis.close();
        return checkedItems;
    }

    /**
        向已有的购物车中添加商品
     */
    private void  addCartItem( Jedis jedis,String cartKey,Integer skuId,Integer num){

        String skuInfoJson = jedis.hget(cartKey, skuId + "");
        if(!StringUtils.isEmpty(skuInfoJson)){
            //购物车中有此商品，叠加数量
            CartItem cartItem = JSON.parseObject(skuInfoJson, CartItem.class);
            cartItem.setNum(cartItem.getNum()+num);
            //重新计算价格
            cartItem.setTotalPrice(cartItem.getTotalPrice());
            String jsonString = JSON.toJSONString(cartItem);
            jedis.hset(cartKey,skuId+"",jsonString);
        }else {
            //购物车无此商品，新增商品
            CartItem cartItem = new CartItem();
            SkuInfo skuInfo = skuService.getSkuInfoBySkuId(skuId);
            SkuItem skuItem = new SkuItem();
            BeanUtils.copyProperties(skuInfo,skuItem);
            cartItem.setNum(num);
            cartItem.setSkuItem(skuItem);
            cartItem.setTotalPrice(cartItem.getTotalPrice());

            //添加商品
            String jsonString = JSON.toJSONString(cartItem);
            jedis.hset(cartKey,skuId+"",jsonString);

            //更新顺序字段
            //拿出之前的顺序，把顺序也更新一下
            String fieldOrder = jedis.hget(cartKey, "fieldOrder");
            List list = JSON.parseObject(fieldOrder, List.class);
            //把新的商品放进list
            list.add(skuId);
            String toJSONString = JSON.toJSONString(list);
            jedis.hset(cartKey,"fieldOrder",toJSONString);
        }

    }


    private String creatCart(Integer skuId,Integer num,boolean login,Integer userId){
        Jedis jedis = jedisPool.getResource();
        String newCartKey;
        if(login){
            //已登录用的key
            newCartKey= CartConstant.USER_CART_PREFIX+userId;
        }else {
            //未登录用的key
            newCartKey=CartConstant.TEMP_CART_PREFIX+ UUID.randomUUID().toString().substring(0,10).replaceAll("_","");
        }
        //保存购物车数据
        //1.查出商品的详细信息
        SkuInfo skuInfo = skuService.getSkuInfoBySkuId(skuId);
        CartItem cartItem = new CartItem();
        SkuItem skuItem = new SkuItem();
        //2.拷贝商品的详细信息进来，准备保存到redis
        BeanUtils.copyProperties(skuInfo,skuItem);
        cartItem.setSkuItem(skuItem);
        cartItem.setNum(num);
        cartItem.setTotalPrice(cartItem.getTotalPrice());

        String jsonString = JSON.toJSONString(cartItem);

        List<Integer> ids=new ArrayList<>() ;
        ids.add(cartItem.getSkuItem().getId());
        Long hset = jedis.hset(newCartKey, skuItem.getId() + "", jsonString);
        String fieldOrder = JSON.toJSONString(ids);
        jedis.hset(newCartKey,"fieldOrder",fieldOrder);

        jedis.close();
        return  newCartKey;
    }

}
