package com.atguigu.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.passport.utils.JwtUtils;
import com.atguigu.gmall.user.UserInfo;
import com.atguigu.gmall.user.UserInfoService;
import constant.CookieConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class LoginController {


    @Reference
    UserInfoService userInfoService;
    /**所有没有登录的都要去往www.gmallsso.com:8110/loginPage.html
     * 登录业务
     * @return
     */

    @RequestMapping("/login")
    public String login(UserInfo userInfo, String originUrl,
                        @CookieValue(name= CookieConstant.SSO_COOKIE_NAME, required = false)
                        String token, HttpServletResponse response){
       //1.如果说cookie也没东西，userInfo也没有东西，这个人直接访问登录页
        if(StringUtils.isEmpty(token) && userInfo.getLoginName() ==null){
            return  "index";
        }
        //1.登录过了
        if (!StringUtils.isEmpty(token)){


            //都已经登录过了就重新定向到那个人那里
              //在此之前我感觉应该检查一下token，在拦截器那边，因为token可能被修改了
            return  "redirect:"+originUrl+"?token="+token;
        }else {
            //2.没有登录过
            if (StringUtils.isEmpty(userInfo.getLoginName())){
                //去登录页
                return  "index";
            }else{
                //用户填写了用户信息
                UserInfo login = userInfoService.login(userInfo);

                    if(login!=null){

                        //登录成功，回到原始地方

                        Map<String,Object> body=new HashMap<>();
                        body.put("id",login.getId());
                        body.put("loginName",login.getLoginName());
                        body.put("nickName",login.getNickName());
                        body.put("headImg",login.getHeadImg());
                        body.put("email",login.getEmail());
                        String newToken = JwtUtils.createJwtToken(body);

                        //本sso域也要在cookie中保存令牌
                        Cookie cookie=new Cookie(CookieConstant.SSO_COOKIE_NAME,newToken);
                        cookie.setPath("/");//无论当前网站那一层都能用
                        response.addCookie(cookie);
                        //登录 成功后将你的信息放到redis中
                        //redis.set(newToken,loginJson)
                        if(!StringUtils.isEmpty(originUrl)){
                            return "redirect:"+originUrl+"?token="+newToken;
                        }else {
                            //登录成了到首页
                            return "redirect:http://www.gmall.com";
                        }
                    }else{
                    //登录失败继续登录
                    return  "index";
                    }
                }
            }
        }

    @ResponseBody
    @RequestMapping("/confirmToken")
    public String confirmToken(String token){
        boolean b = JwtUtils.confirmJwtToken(token);
        return b?"ok":"error";
    }




}
