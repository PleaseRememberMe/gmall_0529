package com.atguigu.gmall.manager;

import java.util.List;

import com.atguigu.gmall.manager.spu.BaseSaleAttr;
import com.atguigu.gmall.manager.spu.SpuImage;
import com.atguigu.gmall.manager.spu.SpuInfo;

public interface SpuInfoService {

    List<SpuInfo> getSpuInfoByC3Id(Integer catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttr();

    //spuInfo的大保存
    void saveBigSpuInfo(SpuInfo spuInfo);

    //通过spuId获取spu下的图片
    List<SpuImage> getSpuImages(Integer spuId);
}
