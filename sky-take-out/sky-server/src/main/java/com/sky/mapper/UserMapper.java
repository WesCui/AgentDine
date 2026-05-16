package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {


    @Select("SELECT * FROM user WHERE openid = #{openid} limit 1")
    User getByOpenid(String openid);

    /**
     * 新增用户
     * @param user
     */

    void insert(User user);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User getById(Long userId);

    /**
     * 根据动态条件统计用户数量
     *
     * @param map 动态查询参数
     * @return 用户数量
     */
    Integer countByMap(Map<String, Object> map);
}
