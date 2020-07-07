package com.atguigu.gmall1213.item.client.impl;

import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

/**
 * @author mqx
 * @date 2020/6/15 10:22
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Result getItem(Long skuId) {
        return null;
    }
}
