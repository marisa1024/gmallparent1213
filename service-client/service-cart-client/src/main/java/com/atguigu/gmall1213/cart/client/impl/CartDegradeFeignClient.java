package com.atguigu.gmall1213.cart.client.impl;

import com.atguigu.gmall1213.cart.client.CartFeignClient;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author mqx
 * @date 2020/6/24 11:13
 */
@Component
public class CartDegradeFeignClient implements CartFeignClient {
    @Override
    public Result addToCart(Long skuId, Integer skuNum) {
        return null;
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        return null;
    }

    @Override
    public Result loadCartCache(String userId) {
        return null;
    }
}
