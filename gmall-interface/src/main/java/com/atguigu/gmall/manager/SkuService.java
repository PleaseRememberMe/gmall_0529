package com.atguigu.gmall.manager;

import com.atguigu.gmall.manager.spu.SpuSaleAttr;

import java.util.List;

public interface SkuService {
    List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id);

    /**
     * 根据spuId查找销售属性的id和销售属性的值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrBySpuId(Integer spuId);
}
