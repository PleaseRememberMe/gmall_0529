package com.atguigu.gmall.manager;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

@Data
public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;

    @TableLogic
    private Integer delFlag;
}
