package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品，同时保存对应的口味数据
     *
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //向菜品表插入1数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);
        //获取菜品id,insert语句执行完毕之后，dish对象的id属性会自动回填
        Long dishId = dish.getId();  //菜品id

        //向口味表插入n数据

        List<DishFlavor> flavors = dishDTO.getFlavors();
        //flavors :集合 遍历集合
        if (flavors !=null &&flavors.size()>0){

            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n数据
            dishFlavorMapper.insertBatch(flavors);
        }

        log.info("保存菜品及口味信息");
    }

    /**
     * 菜品分页查询
     *
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }



    @Transactional
    public void deleteBatch(List<Long> ids) {

        //判断是否有菜品关联了套餐或处于起售状态
        for (Long id : ids) {
            Dish dish= dishMapper.getById(id);
            if (dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        List<Long> setmealids= setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealids!=null && setmealids.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }


        //删除菜品表中的数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除口味表中的数据
            dishFlavorMapper.deleteByDishId(id);
        }
        log.info("批量删除菜品及口味信息");
    }

}
