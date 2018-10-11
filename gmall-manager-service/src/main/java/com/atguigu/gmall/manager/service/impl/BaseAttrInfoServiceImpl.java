package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.BaseAttrInfoService;
import com.atguigu.gmall.manager.BaseAttrValue;
import com.atguigu.gmall.manager.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manager.mapper.BaseAttrValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Override
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id) {
        QueryWrapper<BaseAttrInfo> queryWrapper = new QueryWrapper<BaseAttrInfo>().eq("catalog3_id",catalog3Id);
        return baseAttrInfoMapper.selectList(queryWrapper);
    }

    @Override
    public List<BaseAttrValue> getBaseAttrValueByAttrId(Integer baseAttrInfoId) {
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<BaseAttrValue>().eq("attr_id", baseAttrInfoId);
        return baseAttrValueMapper.selectList(queryWrapper);
    }
}