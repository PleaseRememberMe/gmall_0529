package com.atguigu.gmall.manager.sku;

import lombok.Data;

import java.io.Serializable;
@Data
public class SkuAttrValueMappingTo implements Serializable {
//    sku_id  spu_id  sku_name  sale_attr_value_id_mapping  sale_attr_value_name_mapping

    private  Integer skuId;
    private  Integer spuId;
    private  String skuName;
    private  String  saleAttrValueIdMapping; //销售属性值id的映射
    private  String saleAttrValueNameMapping; //销售属性值名字的映射
}
