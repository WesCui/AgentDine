package com.sky.service;

import com.sky.vo.DishVO;
import com.sky.entity.Setmeal;

import java.util.List;
import java.util.function.Supplier;

public interface HotDataCacheService {

    List<DishVO> getDishList(Long categoryId, Supplier<List<DishVO>> loader);

    List<Setmeal> getSetmealList(Long categoryId, Supplier<List<Setmeal>> loader);

    Integer getShopStatus(Supplier<Integer> loader);

    void updateShopStatus(Integer status);

    void evictDishCategory(Long categoryId);

    void evictAllDish();

    void evictAllSetmeal();
}
