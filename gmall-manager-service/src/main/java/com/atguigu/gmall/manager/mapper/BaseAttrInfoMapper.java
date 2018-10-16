package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.manager.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
     * 按照三级分类的id查出平台属性的名和值
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id);
}
