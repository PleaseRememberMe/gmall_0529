package com.atguigu.gmall.manager;

import com.atguigu.gmall.manager.es.SkuBaseAttrEsVo;
import com.atguigu.gmall.manager.sku.SkuAttrValueMappingTo;
import com.atguigu.gmall.manager.sku.SkuInfo;
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

    void saveBigSkuInfo(SkuInfo skuInfo);

    /**
     * 根据spuId查询sku的信息
     * @param spuId
     * @return
     */
    List<SkuInfo> getSkuInfoBySpuId(Integer spuId);

    /**
     * 根据skuId获取到sku的信息
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoBySkuId(Integer skuId);

    /**
     * 根据spuId查到spu下面所有sku销售属性值的组合
     * @param spuId
     * @return
     */
    List<SkuAttrValueMappingTo> getSkuAttrValueMapping(Integer spuId);

    /**
     * 获取sku所有平台属性值的id集合
     * @param skuId
     * @return
     */
    List<SkuBaseAttrEsVo> getSkuBaseAttrValueIds(Integer skuId);

    /**
     * 查询所有涉及到的平台属性以及值
     * @param valueIds 平台属性值的集合
     * @return
     */
    List<BaseAttrInfo> getSkuBaseAttrInfoGroupByValueId(List<Integer> valueIds);

    /**
     * 通过点击率增加热度并保存到redis中
     * @param skuId
     */
    void incrSkuHotScore(Integer skuId);
}
