package com.sky.test;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
public class SpringDataRedisTest {
    //@Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisTemplate() {
        System.out.println(redisTemplate);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        HashOperations hashOperations = redisTemplate.opsForHash();
        ListOperations listOperations = redisTemplate.opsForList();
        SetOperations setOperations = redisTemplate.opsForSet();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();



    }

    @Test
    public void testString() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("city", "BeiJing");
        String city = (String) redisTemplate.opsForValue().get("city");
        System.out.println(city);
        redisTemplate.opsForValue().set("code", "1234",3, TimeUnit.MINUTES);
        redisTemplate.opsForValue().setIfAbsent("lock", "001");

    }

    @Test
    public void testHash() {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put("001","name","zhangsan");
        hashOperations.put("001","age","23");

        String name = (String) hashOperations.get("001", "name");
        System.out.println(name);

        Set keys=hashOperations.keys("001");
        System.out.println(keys);

        hashOperations.delete("001","age");


    }

    @Test
    public void testList() {
        ListOperations listOperations = redisTemplate.opsForList();


        listOperations.leftPush("mylist","a");
        listOperations.leftPush("mylist","b");
        listOperations.leftPush("mylist","c");

        String element = (String) listOperations.rightPop("mylist");
        System.out.println(element);

        Long size = listOperations.size("mylist");
        System.out.println(size);
    }

    @Test
    public void testSet() {
        SetOperations setOperations = redisTemplate.opsForSet();
        setOperations.add("myset","a","b","c","a");

        Set members = setOperations.members("myset");
        System.out.println(members);

        setOperations.remove("myset","b");
    }




}
