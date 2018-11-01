package com.atguigu.gmall.order.controller;


import annotation.LoginRequired;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.cart.CartItem;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.CartVo;
import com.atguigu.gmall.order.OrderService;
import com.atguigu.gmall.order.TradePageVo;
import com.atguigu.gmall.user.UserAddress;
import com.atguigu.gmall.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class TradeController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;


    @LoginRequired
    @RequestMapping("toTrade")
    public  String toTrade(HttpServletRequest request){
        //1.判断用户选中的商品（验证商品），没有选中还是返回购物车页面
        //1.1获取到用户信息
        Map<String,Object> userInfo = (Map<String, Object>) request.getAttribute("userInfo");
        int id=Integer.parseInt(userInfo.get("id").toString());
        //1.2获取购物车被选中的商品
        List<CartItem> cartItemList=cartService.getCartInfoCheckedList(id);

        //2.查询和展示收货人信息
        List<UserAddress> userAddresses=userService.getUserAddressByUserId(id);

        //3.列举购物车选中的清单
        
        TradePageVo vo = new TradePageVo();
        CartVo cartVo = new CartVo();
        cartVo.setCartItems(cartItemList);
        BigDecimal totalPrice = cartVo.getTotalPrice();
        vo.setTotalPrice(totalPrice);
        vo.setCartItems(cartItemList);
        vo.setUserAddresses(userAddresses);



        //4.防止用户重复提交:生成一个令牌，服务器一份，页面一份
        String token=orderService.createTradeToken();//创建一个交易令牌

        log.info("要传的数据，{}",vo);
        //跳到结算页
        request.setAttribute("tradeInfo",vo);
        request.setAttribute("token",token);
        return "trade";
    }
}
