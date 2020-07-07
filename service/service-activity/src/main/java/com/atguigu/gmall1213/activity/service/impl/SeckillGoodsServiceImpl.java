package com.atguigu.gmall1213.activity.service.impl;

import com.atguigu.gmall1213.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall1213.activity.service.SeckillGoodsService;
import com.atguigu.gmall1213.activity.util.CacheHelper;
import com.atguigu.gmall1213.common.constant.RedisConst;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.result.ResultCodeEnum;
import com.atguigu.gmall1213.common.util.MD5;
import com.atguigu.gmall1213.model.activity.OrderRecode;
import com.atguigu.gmall1213.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author mqx
 * @date 2020/7/3 14:14
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    // 因为秒杀商品在凌晨会将数据加载到缓存中，所以此处查询缓存即可
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public List<SeckillGoods> findAll() {
        // 商品保存到缓存redis-Hash
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return seckillGoodsList;
    }

    @Override
    public SeckillGoods getSeckillGoodsBySkuId(Long skuId) {
        // 根据skuId 查询秒杀对象信息 redis - hash 通过key找value。
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
        return seckillGoods;
    }

    @Override
    public void seckillOrder(Long skuId, String userId) {

        String state = (String)CacheHelper.get(skuId.toString());
        if("0".equals(state)) {
            return;
        }
        Boolean isExist = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId, RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);

        if (!isExist){
            return;
        }
        String goodsId = (String) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        if(null==goodsId){
            //销售空
            redisTemplate.convertAndSend("seckillpush",skuId+":0");
            return;
        }
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setNum(1);
        orderRecode.setUserId(userId);
        orderRecode.setOrderStr(MD5.encrypt(userId));
        orderRecode.setSeckillGoods(getSeckillGoodsBySkuId(skuId));

        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(),orderRecode);

        this.updateStockCount(skuId);
    }

    @Override
    public Result checkOrder(Long skuId, String userId) {
        //是否秒杀成功
        Boolean isExist = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        if(isExist){
            //存在
            Boolean flag = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            if(flag){
                OrderRecode orderRecode = (OrderRecode)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);

                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }

        }
        //是否下单
        Boolean res = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        if(res){
            String orderId = (String)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);

        }

        String state = (String)CacheHelper.get(skuId.toString());
        if("0".equals(state)){
            return Result.build(null,ResultCodeEnum.SECKILL_FAIL);
        }
        return Result.build(null,ResultCodeEnum.SECKILL_RUN);
    }

    /**\
     * 更新库存
     * @param skuId
     */
    private void updateStockCount(Long skuId) {
        Long count = redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();
        if(count%2==0){
            SeckillGoods seckillGoodsBySkuId = getSeckillGoodsBySkuId(skuId);
            seckillGoodsBySkuId.setStockCount(count.intValue());
            seckillGoodsMapper.updateById(seckillGoodsBySkuId);

            redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(seckillGoodsBySkuId.getSkuId().toString(),seckillGoodsBySkuId);
        }
    }
}
