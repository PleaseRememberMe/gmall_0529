package com.atguigu.gmall.user;

public interface UserService {

    public User getUser(String id);

    public void buyMovie(String uid,String mid);
}
