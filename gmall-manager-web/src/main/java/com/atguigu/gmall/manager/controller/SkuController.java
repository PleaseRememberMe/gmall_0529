package com.atguigu.gmall.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.SkuEsService;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.SpuInfoService;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.spu.SpuImage;
import com.atguigu.gmall.manager.spu.SpuSaleAttr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/sku")
@RestController
public class SkuController {


    @Reference
    SkuService skuService;

    @Reference
    SpuInfoService spuInfoService;


    @Reference
    SkuEsService skuEsService;
    /**
     * 传入一个skuId将这个商品上架，缓存到es中
     * @param skuId
     * @return
     */
    @RequestMapping("/onSale")
    public String onSale(@RequestParam("skuId")Integer skuId){
        skuEsService.onSale(skuId);
        return  "ok";
    }





    @RequestMapping("/skuinfo")
    public List<SkuInfo> getSkuInfoBySpuId(@RequestParam("id") Integer spuId){
        return skuService.getSkuInfoBySpuId(spuId);
    }


    @RequestMapping("/bigsave")
    public String skuBigSave(@RequestBody SkuInfo skuInfo){
        skuService.saveBigSkuInfo(skuInfo);
        log.debug("页面提交过来的大skuInfo信息：{}",skuInfo);
        return  "Ok";
    }


    /**
     * 查询spu下的所有图片
     * @param spuId
     * @return
     */
    @RequestMapping("/spuImages")
    public List<SpuImage> getSpuImages(@RequestParam("id")Integer spuId){
        return  spuInfoService.getSpuImages(spuId);
    }


    /**
     * 按照三级分类查出下面的属性和值
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/base_attr_info.json")
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(@RequestParam("id") Integer catalog3Id){
        return skuService.getBaseAttrInfoByCatalog3Id(catalog3Id);
    }


    @RequestMapping("/spu_sale_attr.json")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(@RequestParam("id") Integer spuId){
        return  skuService.getSpuSaleAttrBySpuId(spuId);
    }





}
