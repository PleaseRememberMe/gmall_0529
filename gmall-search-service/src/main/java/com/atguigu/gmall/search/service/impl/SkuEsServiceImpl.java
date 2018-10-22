package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manager.SkuEsService;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.es.SkuBaseAttrEsVo;
import com.atguigu.gmall.manager.es.SkuInfoEsVo;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.search.constant.EsConstant;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class SkuEsServiceImpl implements SkuEsService {

   @Reference
    SkuService skuService;


   @Autowired
    JestClient jestClient;


    @Async //异步，表示这是一个异步调用
    @Override
    public void onSale(Integer skuId) {
        //查出这个sku对应的详细信息
        SkuInfo info = skuService.getSkuInfoBySkuId(skuId);
        System.out.println("mmmmmmmmmmmmmmm"+info);
        log.info("获取到的sku信息：{}",info);

        SkuInfoEsVo skuInfoEsVo = new SkuInfoEsVo();
        //将查询到的信息拷贝出来
        BeanUtils.copyProperties(info,skuInfoEsVo);
        //查出当前sku的所有平台属性值
        List<SkuBaseAttrEsVo> vos= skuService.getSkuBaseAttrValueIds(skuId);

        skuInfoEsVo.setBaseAttrEsVos(vos);

        //保存信息到es
        Index index = new Index.Builder(skuInfoEsVo).index(EsConstant.GMALL_INDEX).type(EsConstant.GMALL_SKU_TYPE)
                .id(skuInfoEsVo.getId() + "").build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            log.error("es数据保存出问题了，{}",e);
        }
    }
}
