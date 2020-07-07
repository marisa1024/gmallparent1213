package com.atguigu.gmall1213.activity.service;

import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author mqx
 * @date 2020/7/3 14:12
 */
public interface SeckillGoodsService {

    // 查询所有秒杀商品列表
    List<SeckillGoods> findAll();

    // 根据秒杀商品Id 查看秒杀商品详情
    SeckillGoods getSeckillGoodsBySkuId(Long skuId);

    void seckillOrder(Long skuId,String userId);

    /**
     * 根据商品ID查询订单信息
     * @param skuId
     * @param userId
     * @return
     */
    Result checkOrder(Long skuId,String userId);
}
