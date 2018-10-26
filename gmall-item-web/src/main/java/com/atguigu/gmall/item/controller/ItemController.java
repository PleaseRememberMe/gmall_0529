package com.atguigu.gmall.item.controller;

import annotation.LoginRequired;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.SkuEsService;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.sku.SkuAttrValueMappingTo;
import com.atguigu.gmall.manager.sku.SkuInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ItemController {

    @Reference
    SkuService skuService;


    /**
     * 我们可以写一个拦截器，在请求达到目标方法的时候，看看方法是否需要登陆才能访问，如果需要就进行登陆操作
     * @return
     */
    @LoginRequired
    @RequestMapping("/haha")
    public String haha(HttpServletRequest request){
        //常用的key需要抽取常量
        Map<String,Object> userInfo = (Map<String, Object>) request.getAttribute("userInfo");

        //要用户名
        //1、如果这个token不是没意义的随机数（只用来做redis中key标识的）
        //2、假设这个token是 一串有意义的  dsjakljdsalkjdals.djsaljdaskljdlasjdkslajdsadlkas
        //   这串数据以及包含了你最常用的信息，你要用这些信息不用查了，你的领牌里面就有
        //不可伪造;还携带了常用信息
        //JWT(JSON Web Token)（规范）稍微加密。能加也要能解
        //UserInfo = redis.get(token)
        log.info("我们可以解码到用户的信息是：{}",userInfo);
        return "haha";

    }





    @RequestMapping("/{skuId}.html")
    public String  itemPage(@PathVariable("skuId") Integer skuId, Model model){

        //1、查出sku的详细信息
        //2.service应该用缓存机制
        SkuInfo skuInfo=null;
        skuInfo=skuService.getSkuInfoBySkuId(skuId);
        if(skuInfo==null){
            //跳转到商品不存在页
            return "saleEmpty";
        }
        model.addAttribute("skuInfo",skuInfo);
        Integer spuId = skuInfo.getSpuId();
        //2、查出当前sku对应的spu下面所有sku销售属性值的组合
        List<SkuAttrValueMappingTo> skuAttrValueMappingTos=skuService.getSkuAttrValueMapping(spuId);
        model.addAttribute("skuAttrValueMappingTos",skuAttrValueMappingTos);


        //3.增加点击率，更新es的hotScore值
        //redis, 把redis中这个商品的热度保存起来增加即可
        skuService.incrSkuHotScore(skuId);



        return "item";
    }
}
