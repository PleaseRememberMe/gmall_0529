package com.atguigu.gmall.manager;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

@Data
public class BaseAttrInfo extends SuperBean{

    private String attrName;

    private Integer catalog3Id;

    @TableField(exist = false)//数据库并不存在此字段
    private List<BaseAttrValue> attrValues;
}
