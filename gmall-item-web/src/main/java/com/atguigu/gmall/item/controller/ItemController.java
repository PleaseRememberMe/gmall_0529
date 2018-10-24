package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.SkuEsService;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.sku.SkuAttrValueMappingTo;
import com.atguigu.gmall.manager.sku.SkuInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;





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
