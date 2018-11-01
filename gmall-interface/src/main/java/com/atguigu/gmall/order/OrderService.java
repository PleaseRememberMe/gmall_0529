package com.atguigu.gmall.order;

import com.atguigu.gmall.user.UserAddress;

import java.util.List;

public interface OrderService {
    /**
     * 创建当次的一个交易令牌
     * @return
     */
    String createTradeToken();

    /**
     * 验证令牌防止重复提交
     * @param token
     * @return
     */
    boolean verfyToken(String token);

    /**
     *验证库存
     * @param userId 用户id
     * @return  所有商品库存不足的信息
     */
    List<String> verfyStock(Integer userId);

    /**
     * 根据用户id获取地址
     * @param userAddressId
     * @return
     */
    UserAddress getUserAddressById(Integer userAddressId);

    /**
     * 下单
     * @param userId
     * @param orderInfoTo
     */
    void createOrder(Integer userId, OrderInfoTo orderInfoTo);
}
