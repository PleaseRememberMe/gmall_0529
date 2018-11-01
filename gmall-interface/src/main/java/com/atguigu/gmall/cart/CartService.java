package com.atguigu.gmall.cart;

import java.util.List;

/**
 * 购物车功能
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * @param skuId 商品id
     * @param cartKey 未登录情况下购物车的id
     * @param num 数量
     * @return
     */
    String addToCartUnLogin(Integer skuId,String cartKey, Integer num);


    /**
     * 登录添加到购物车
     * @param skuId 商品id
     * @param userId 用户id
     * @param num
     */
    void addToCartLogin(Integer skuId,Integer userId,Integer num);


    /**
     *获取购物车的数据
     * @param cartKey 购物车在redis中保存的key、
     * @return
     */
    CartVo getYourCart(String cartKey);


    /**
     * 合并购物车
     * @param cartKey
     * @param userId
     */
    void  mergeCart(String cartKey,Integer userId);

    /**
     * 根据购物车id和商品id，从购物车中查出商品的详情
     * @param cartKey
     * @param skuId
     * @return
     */
    CartItem getCartItemInfo(String cartKey, Integer skuId);

    /**
     * 查询购物车数据
     * @param cartKey
     * @param login
     * @return
     */
    List<CartItem> getCartInfoList(String cartKey, boolean login);

    /**
     * 勾选某个商品
     * @param skuId 商品id
     * @param checkFlag 是否勾选
     * @param tempCartKey 临时购物车id
     * @param userId  用户id
     * @param loginFlag 是否登录
     */
    void checkItem(Integer skuId, Boolean checkFlag, String tempCartKey, int userId, boolean loginFlag);

    /**
     * 返回购物车被选中的商品
     * @param id 用户id
     * @return
     */
    List<CartItem> getCartInfoCheckedList(int id);
}
