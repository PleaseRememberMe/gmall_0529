package com.atguigu.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.movice.MovieService;
import com.atguigu.gmall.user.Movie;
import com.atguigu.gmall.user.User;
import com.atguigu.gmall.user.UserService;

//将这个服务暴露出去
@Service
public class MovieServiceImp implements MovieService {



    @Override
    public Movie getMovie(String id) {



        return new Movie("1","abc");
    }
}
