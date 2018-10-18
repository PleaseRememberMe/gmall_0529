package com.atguigu.gmall.manager.mapper.sku;

import com.atguigu.gmall.manager.sku.SkuAllSaveAttrAndValueTo;
import com.atguigu.gmall.manager.sku.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    /**
     * 给页面获取sku对应的所有能供选择的销售属性以及当前sku是哪个销售属性的大对象
     * @param skuId
     * @param spuId
     * @return
     */
    List<SkuAllSaveAttrAndValueTo> getSkuAllSaveAttrAndValue(@Param("id") Integer skuId,@Param("spuId") Integer spuId);
}
