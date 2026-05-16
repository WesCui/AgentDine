package com.sky.agent.tools;

import com.sky.entity.Dish;
import com.sky.entity.OrderDetail;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.OrderService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.OrderVO;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能体工具类
 * 提供给 AI 智能体调用的具体业务方法
 * 每个方法都通过 @Tool 注解暴露给 LLM，用于获取动态数据或执行操作
 */
@Component
public class AgentTools {

    @Autowired
    private OrderService orderService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 查询指定用户的最近历史订单
     * 用于分析用户口味偏好
     *
     * @param userId 用户ID
     * @return 最近订单中的菜品名称列表 (去重，最多 10 个)
     */
    @Tool("查询指定用户的最近历史订单，返回菜品名称列表。userId是用户ID。")
    @SuppressWarnings("unchecked") //SuppressWarnings 用于抑制未经检查的转换警告
    public List<String> getUserLastOrders(Long userId) {
        if (userId == null) return new ArrayList<>();

        // 调用OrderService查询用户订单，不依赖ThreadLocal中的userId
        PageResult pageResult = orderService.pageQuery4User(userId, 1, 5, null);
        List<OrderVO> orders = (List<OrderVO>) pageResult.getRecords();

        List<String> dishNames = new ArrayList<>();
        if (orders != null) {
            for (OrderVO order : orders) {
                // OrderVO 中已经包含了 orderDetailList
                List<OrderDetail> details = order.getOrderDetailList();
                if (details != null) {
                    dishNames.addAll(details.stream().map(OrderDetail::getName).collect(Collectors.toList()));
                }
            }
        }
        return dishNames.stream().distinct().limit(10).collect(Collectors.toList());
    }

    /**
     * 查询所有起售的菜品
     * 用于告知用户当前店铺有哪些单品可供选择
     *
     * @return 菜品名称和价格的列表
     */
    @Tool("查询当前所有起售的菜品列表，包含价格")
    public List<String> getOnSaleDishes() {
        Dish dish = new Dish();
        dish.setStatus(1); // 起售状态
        List<DishVO> dishVOs = dishService.listWithFlavor(dish);

        if (dishVOs == null) return new ArrayList<>();
        return dishVOs.stream()
                .map(d -> d.getName() + " (价格: " + d.getPrice() + ")")
                .collect(Collectors.toList());
    }

    /**
     * 查询所有起售的套餐
     * 用于告知用户当前店铺有哪些优惠套餐可供选择
     *
     * @return 套餐名称和价格的列表
     */
    @Tool("查询当前所有起售的套餐列表，包含价格")
    public List<String> getOnSaleSetmeals() {
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(1); // 起售状态
        List<Setmeal> setmeals = setmealService.list(setmeal);

        if (setmeals == null) return new ArrayList<>();
        return setmeals.stream()
                .map(s -> s.getName() + " (价格: " + s.getPrice() + ")")
                .collect(Collectors.toList());
    }
}
