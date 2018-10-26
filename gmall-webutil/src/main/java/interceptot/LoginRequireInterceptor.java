package interceptot;


import annotation.LoginRequired;
import constant.CookieConstant;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import utils.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class LoginRequireInterceptor implements HandlerInterceptor{

    //目标方法执行之前
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object o) throws Exception {
    //1.先判断这个方法是否需要登录后才能访问，拿到我们要执行的目标方法
        HandlerMethod handlerMethod = (HandlerMethod) o;
        LoginRequired annotion = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if(annotion!=null){
            //标了注解
            //1.验证是否是第一次过来，只是带了一个参数位置的token字符
            String token = request.getParameter("token");
            String cookieValue = CookieUtils.getCookieValue(request, CookieConstant.SSO_COOKIE_NAME);

            if(!StringUtils.isEmpty(token)){

                //只要参数有说明登录成功了，我们要设置这个cookie
                //我想先验证一下token对不对

                //验证令牌，远程验证的
                // 1.验证令牌对不对
                //发请求
                RestTemplate restTemplate = new RestTemplate();
                String confirmTokenUrl="http://www.gmallsso.com:8110/confirmToken?token="+token;
                //去远程验证
                String result = restTemplate.getForObject(confirmTokenUrl, String.class);
                System.out.println("远程验证结果是:"+result);
                if(result.equals("ok")){
                    //验证通过放行方法
                    Cookie cookie = new Cookie(CookieConstant.SSO_COOKIE_NAME, token);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    return  true;
                }else{
                    //验证失败，重新去登录
                    String redirectUrl="http://www.gmallsso.com:8110/login?originUrl="+request.getRequestURL();
                    response.sendRedirect(redirectUrl);
                    return  false;
                }

            }

            //2.验证是否存在的cookie
            if(!StringUtils.isEmpty(cookieValue)){
                //说明之前已经登录过，cookie放好了
                //验证令牌，远程验证的
                // 1.验证令牌对不对
                //发请求
                RestTemplate restTemplate = new RestTemplate();
                String confirmTokenUrl="http://www.gmallsso.com:8110/confirmToken?token="+cookieValue;
                //去远程验证
                String result = restTemplate.getForObject(confirmTokenUrl, String.class);
                System.out.println("远程验证结果是:"+result);
                if(result.equals("ok")){
                    //验证通过放行方法
                    Map<String, Object> map = CookieUtils.resolveTokenData(cookieValue);
                    request.setAttribute("userInfo",map);
                    return  true;
                }else{
                    //验证失败，重新去登录
                    String redirectUrl="http://www.gmallsso.com:8110/login?originUrl="+request.getRequestURL();
                    response.sendRedirect(redirectUrl);
                    return  false;
                }
            }
            //两个都没有
            if(StringUtils.isEmpty(token) &&StringUtils.isEmpty(cookieValue)){
                String redirectUrl="http://www.gmallsso.com:8110/login?originUrl="+request.getRequestURL();
                response.sendRedirect(redirectUrl);
                return  false;
            }
        }else {
            //没有标注注解直接放行
            return  true;
        }
        return false;
    }
    //目标方法执行之后
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }
    //页面渲染出来以后
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
