package com.atguigu.gmall.cart.controller;


import annotation.LoginRequired;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.cart.CartItem;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.CartVo;
import constant.CookieConstant;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Reference
    CartService cartService;

   @LoginRequired(needLogin = false)
   @ResponseBody
   @RequestMapping("checkItem")
   public  String checkItem(Integer skuId,Boolean checkFlag,HttpServletRequest request){
       Map<String,Object> userInfo = (Map<String, Object>) request.getAttribute(CookieConstant.LOGIN_USER_INFO_KEY);
       String tempCartKey = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);
       boolean loginFlag=userInfo==null?false:true;
       int userId=0;

       userId=Integer.parseInt(userInfo.get("id").toString());
       cartService.checkItem(skuId,checkFlag,tempCartKey,userId,loginFlag);
       return  "ok";
   }





    /**
     * 查询购物车的数据
     * @param request
     * @param response
     * @return
     */
    @LoginRequired(needLogin = false)
    @RequestMapping("/cartList")
    public String  cartInfoPage(HttpServletRequest request,HttpServletResponse response){
        Map<String,Object> userInfo = (Map<String, Object>) request.getAttribute(CookieConstant.LOGIN_USER_INFO_KEY);
        //判断是否需要合并购物车
        String tempCart = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);
        if(!StringUtils.isEmpty(tempCart) &&userInfo!=null){
            //说明有临时购物车合并购物车
            cartService.mergeCart(tempCart,Integer.parseInt(userInfo.get("id").toString()));
            Cookie cookie = new Cookie(CookieConstant.COOKIE_CART_KEY, "aaaa");
            cookie.setMaxAge(0);
            //立即删除之前的购物车数据
            response.addCookie(cookie); //感觉和下面的删除cookie是一样的！！！
        }

        boolean login=false;
        String cartKey="";
        if(userInfo!=null){
            //1.登录了
            login=true;
            cartKey=userInfo.get("id").toString();
        }else {
            //没有登录
            login=false;
            cartKey=CookieUtils.getCookieValue(request,CookieConstant.COOKIE_CART_KEY);
        }
        //查询数据
       List<CartItem> cartItems= cartService.getCartInfoList(cartKey,login);

        //购物车
        CartVo  cartVo=new CartVo();
        cartVo.setCartItems(cartItems);
        cartVo.setTotalPrice(cartVo.getTotalPrice());

        request.setAttribute("cartVo",cartVo);

        return "cartList";
    }



    @LoginRequired(needLogin = false)
    @RequestMapping("/addToCart")
    public  String addToCart(Integer skuId, Integer num, HttpServletRequest request,
                             HttpServletResponse response){
        //判断是否登录，登录用user：cart ：12：info在redis中
        Map<String,Object> loginUser = (Map<String, Object>) request.getAttribute(CookieConstant.LOGIN_USER_INFO_KEY);
        //没登录，临时搞一个购物车id
        //这个id在redis中存数据
        String cartKey = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);
        if(loginUser==null){ //未登录下的处理
            if(StringUtils.isEmpty(cartKey)){ //还没有购物车
                //跟你造一个购物车在redis中存数据用的key
                 cartKey = cartService.addToCartUnLogin(skuId,null, num);
                Cookie cookie = new Cookie(CookieConstant.COOKIE_CART_KEY, cartKey);
                cookie.setMaxAge(CookieConstant.COOKIE_CART_KEY_MAX_AGE);
                response.addCookie(cookie);
            }else { //有一个购物车在redis中存数据用的key
                String cartId = cartService.addToCartUnLogin(skuId, cartKey, num);
            }
        }else { //登录了
            Integer userId = Integer.parseInt(loginUser.get("id").toString());
            //合并购物车
           if (StringUtils.isEmpty(cartKey)){
               //cookie没有临时购物车
               cartService.addToCartLogin(skuId,userId,num);
           }else {
               //有临时购物车，先合并购物车
               cartService.mergeCart(cartKey,userId);
               cartService.addToCartLogin(skuId,userId,num);
               //删掉cart-key这个cookie
               CookieUtils.removeCookie(response,CookieConstant.COOKIE_CART_KEY);
           }

        }

        //把购物车刚才的数据查出来
        CartItem cartItem=cartService.getCartItemInfo(cartKey,skuId);
        request.setAttribute("skuInfo",cartItem);
        return  "success";
    }
}
