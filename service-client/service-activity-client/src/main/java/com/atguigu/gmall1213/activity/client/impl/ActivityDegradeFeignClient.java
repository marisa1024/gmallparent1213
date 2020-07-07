package com.atguigu.gmall1213.activity.client.impl;

import com.atguigu.gmall1213.activity.client.ActivityFeignClient;
import com.atguigu.gmall1213.common.result.Result;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author mqx
 * @date 2020/7/3 9:49
 */
@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {
    @Override
    public Result findAll() {
        return null;
    }

    @Override
    public Result getSeckillGoods(Long skuId) {
        return null;
    }

    @Override
    public Result trade() {
        return null;
    }
}
