package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    void insertBatch(List<OrderDetail> orderDetails);


    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 统计商品销量 Top10
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @param status    订单状态
     * @return 商品销量列表
     */
    @Select("select od.name as name, sum(od.number) as number " +
            "from order_detail od " +
            "inner join orders o on od.order_id = o.id " +
            "where o.status = #{status} " +
            "and o.order_time >= #{beginTime} " +
            "and o.order_time <= #{endTime} " +
            "group by od.name " +
            "order by number desc " +
            "limit 10")
    List<GoodsSalesDTO> getSalesTop10(@Param("beginTime") LocalDateTime beginTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("status") Integer status);

}
