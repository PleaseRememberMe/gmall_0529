package com.atguigu.gmall.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/basecatalog")
@RestController
public class BaseCatalogRestController {

    @Reference
    CatalogService catalogService;

    @Reference
    BaseAttrInfoService baseAttrInfoService;

    /**
     * 查询三级分类下的平台属性
     * @param id 三级分类id
     * @return
     */
    @RequestMapping("/attr.json")
    public List<BaseAttrInfo> listBaseAttrInfo(Integer id){
        return baseAttrInfoService.getBaseAttrInfoByCatalog3Id(id);
    }


    /**
     * 查询一级分类
     * @return
     */
    @RequestMapping("/1/list.json")
    public List<BaseCatalog1> listBaseCatalog1(){
        return catalogService.getAllBaseCatalog1();
    }

    /**
     *查询二级分类
     * @param id 一级分类的id
     * @return
     */
    @RequestMapping("/2/list.json")
    public List<BaseCatalog2> listBaseCatalog2(Integer id){
        return catalogService.getAllBaseCatalog2ByC1Id(id);
    }

    /**
     *查询三级分类
     * @param id  二级分类的id
     * @return
     */
    @RequestMapping("/3/list.json")
    public List<BaseCatalog3> listBaseCatalog3(Integer id){
        return catalogService.getAllBaseCatalog3ByC2Id(id);
    }


}
