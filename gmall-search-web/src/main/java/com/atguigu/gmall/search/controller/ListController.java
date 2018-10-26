package com.atguigu.gmall.search.controller;

import annotation.LoginRequired;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.SkuEsService;
import com.atguigu.gmall.manager.es.SkuSearchParamEsVo;
import com.atguigu.gmall.manager.es.SkuSearchResultEsVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
public class ListController {

    @Reference
    SkuEsService skuEsService;



    @LoginRequired  //需要登陆
    @RequestMapping("/hehe")
    public String hehe(){
        //想要用户信息
        //eyJoZWFkSW1nIjoiMyIsIm5pY2tOYW1lIjoi5byg5LiJIiwibG9naW5OYW1lIjoiemhhbmdzYW4iLCJpZCI6MywiZW1haWwiOiJhdGd1aWd1LmNvbSJ9
        //以上解码就是用户信息


        return "hehe";
    }





    /**
     * 将所有页面可能提交的数据封装入参
     * @param paramEsVo
     * @return
     */
    @RequestMapping("/list.html")
    public String getList(SkuSearchParamEsVo paramEsVo, Model model){

        //搜索完成后返回这个对象，这个对象里面有所有的数据
        SkuSearchResultEsVo searchResult=skuEsService.searchSkuFromES(paramEsVo);

        model.addAttribute("searchResult",searchResult);
        return "list";
    }


}
