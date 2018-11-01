package com.atguigu.gmall.user;

import java.util.List;

public interface UserService {

    public User getUser(String id);

    public void buyMovie(String uid,String mid);

    /**
     * 获取用户的收货地址列表
     * @param id 用户的id
     * @return
     */
    List<UserAddress> getUserAddressByUserId(int id);
}
