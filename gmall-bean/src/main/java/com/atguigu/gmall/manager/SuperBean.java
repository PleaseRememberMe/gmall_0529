package com.atguigu.gmall.manager;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

public class SuperBean implements Serializable{
    @TableId(type = IdType.AUTO)
    private Integer id;
}
