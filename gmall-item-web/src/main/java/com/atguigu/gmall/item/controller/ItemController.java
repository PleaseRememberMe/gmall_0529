package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.sku.SkuInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @RequestMapping("/{skuId}.html")
    public String  itemPage(@PathVariable("skuId") Integer skuId, Model model){

        //1、查出sku的详细信息
        SkuInfo skuInfo=skuService.getSkuInfoBySkuId(skuId);
        model.addAttribute("skuInfo",skuInfo);

        return "item";
    }
}
