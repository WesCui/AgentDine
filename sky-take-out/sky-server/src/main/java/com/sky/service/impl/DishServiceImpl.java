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
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Autowired
    private SetmealMapper setmealMapper;
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
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            //删除口味表中的数据
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteByDishIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);


        log.info("批量删除菜品及口味信息");
    }

    /**
     * 根据id查询菜品及其口味信息
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品基本信息
        Dish dish= dishMapper.getById(id);
        //根据菜品查询口味信息
        List<DishFlavor> flavors= dishFlavorMapper.getByDishId(id);
        //封装vo对象并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品信息，同时更新对应的口味数据
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        //更新dish表基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //更新口味表信息
        //先删除原有口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //再插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
        log.info("更新菜品及口味信息");
    }

    /**
     * 根据分类id查询对应的菜品列表
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
