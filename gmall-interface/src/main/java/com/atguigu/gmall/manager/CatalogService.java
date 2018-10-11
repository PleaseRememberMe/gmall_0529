package com.atguigu.gmall.manager;

import java.util.List;

/**
 * 操作分类的接口
 */
public interface CatalogService {

    /**
     * 获取所有的一级分类
     * @return
     */
    public List<BaseCatalog1> getAllBaseCatalog1();

    /**
     * 获取所有的二级分类
     * @param catalog1Id  一级分类id
     * @return
     */
    public List<BaseCatalog2> getAllBaseCatalog2ByC1Id(Integer catalog1Id);


    /**
     * 获取所有三级分类
     * @param catalog2Id  二级分类id
     * @return
     */
    public List<BaseCatalog3> getAllBaseCatalog3ByC2Id(Integer catalog2Id);

}
