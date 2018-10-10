package com.atguigu.gmall;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.movice.MovieService;
import com.atguigu.gmall.user.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {

    @Reference
    MovieService movieService;


    @ResponseBody
    @RequestMapping("/movie")
    public Movie buyMovie(String  id){
        Movie movie = movieService.getMovie(id);
        return movie;
    }
}
