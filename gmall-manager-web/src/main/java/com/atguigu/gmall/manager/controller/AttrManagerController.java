package com.atguigu.gmall.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/attr")
@Controller
public class AttrManagerController {
    /**
     * 去平台属性页面
     * @return
     */
    @RequestMapping("/listPage.html")
    public String toAttrListPage(){
        return "attr/attrListPage";
    }
}
