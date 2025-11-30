package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "商铺管理相关接口")
@Slf4j
public class ShopController {

    public final static String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 修改商铺状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("修改商铺状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("修改商铺状态: {}", status==1?"开启":"关闭");
        redisTemplate.opsForValue().set("KEY", status);
        return Result.success();
    }

    /**
     * 获取商铺状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取商铺状态")
    public Result<Integer> getStatus() {
        //log.info("获取商铺状态");
        Integer status = (Integer) redisTemplate.opsForValue().get("KEY");
        log.info("获取商铺状态: {}", status==1?"开启":"关闭");
        return Result.success(status);
    }



}
