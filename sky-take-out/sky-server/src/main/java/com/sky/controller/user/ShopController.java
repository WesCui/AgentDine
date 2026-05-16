package com.sky.controller.user;
import com.sky.result.Result;
import com.sky.service.HotDataCacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "商铺管理相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private HotDataCacheService hotDataCacheService;


    /**
     * 获取商铺状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取商铺状态")
    public Result<Integer> getStatus() {
        Integer status = hotDataCacheService.getShopStatus(() -> 1);
        log.info("获取商铺状态: {}", status==1?"开启":"关闭");
        return Result.success(status);
    }



}
