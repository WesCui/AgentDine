package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {


    @Select("SELECT * FROM user WHERE openid = #{openid} limit 1")
    User getByOpenid(String openid);

    /**
     * 新增用户
     * @param user
     */

    void insert(User user);
}
