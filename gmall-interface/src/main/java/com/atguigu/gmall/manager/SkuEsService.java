package com.atguigu.gmall.manager;

import com.atguigu.gmall.manager.es.SkuSearchParamEsVo;
import com.atguigu.gmall.manager.es.SkuSearchResultEsVo;

public interface SkuEsService {
    /**
     * 商品上架
     * @param skuId
     */
    void onSale(Integer skuId);

    SkuSearchResultEsVo searchSkuFromES(SkuSearchParamEsVo paramEsVo);

    /**
     * 更新热度信息
     * @param skuId
     * @param hincrBy
     */
    void updateHotScore(Integer skuId, Long hincrBy);
}
