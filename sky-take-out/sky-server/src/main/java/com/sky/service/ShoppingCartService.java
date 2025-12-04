package com.sky.service;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ShoppingCartService {


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车列表
     * @return List<ShoppingCart>
     */
    List<ShoppingCart> showShoppingCart();


    /**
     * 减少购物车中对应菜品或套餐的数量
     * @param shoppingCartDTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 清空购物车
     */
    void cleanShoppingCart();
}


