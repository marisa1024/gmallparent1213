package com.atguigu.gmall1213.activity.controller;

import com.atguigu.gmall1213.activity.service.SeckillGoodsService;
import com.atguigu.gmall1213.activity.util.CacheHelper;
import com.atguigu.gmall1213.activity.util.DateUtil;
import com.atguigu.gmall1213.common.constant.MqConst;
import com.atguigu.gmall1213.common.constant.RedisConst;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.result.ResultCodeEnum;
import com.atguigu.gmall1213.common.service.RabbitService;
import com.atguigu.gmall1213.common.util.AuthContextHolder;
import com.atguigu.gmall1213.common.util.MD5;
import com.atguigu.gmall1213.model.activity.OrderRecode;
import com.atguigu.gmall1213.model.activity.SeckillGoods;
import com.atguigu.gmall1213.model.activity.UserRecode;
import com.atguigu.gmall1213.model.order.OrderDetail;
import com.atguigu.gmall1213.model.order.OrderInfo;
import com.atguigu.gmall1213.model.product.SkuInfo;
import com.atguigu.gmall1213.model.user.UserAddress;
import com.atguigu.gmall1213.order.client.OrderFeignClient;
import com.atguigu.gmall1213.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author mqx
 * @date 2020/7/3 14:19
 */
@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderFeignClient orderFeignClient;

    //getSkuValueIdsMap
    // 查询所有秒杀商品数据
    @GetMapping("findAll")
    public Result findAll(){
        List<SeckillGoods> list = seckillGoodsService.findAll();
        return Result.ok(list);
    }

    // 查询秒杀对象
    @GetMapping("getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId){
        return Result.ok(seckillGoodsService.getSeckillGoodsBySkuId(skuId));
    }

    // 获取下单码
    // http://api.gmall.com/api/activity/seckill/auth/getSeckillSkuIdStr/31
    @GetMapping("auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request){
        // 怎么生成下单码 使用用户Id 来做MD5加密。加密之后的这个字符串就是下单码
        // 用户用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 通过当前商品Id查询到当前秒杀商品这个对象，看当前的这个商品是否正在秒杀，如果正在秒杀，则获取下单码，否则不能获取！
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoodsBySkuId(skuId);
        if (null!=seckillGoods){
            // 判断当前商品是否正在参与秒杀 ，可以通过时间判断
            Date curTime = new Date();
            // 判断当前系统时间是否在秒杀时间范围内
            if (DateUtil.dateCompare(seckillGoods.getStartTime(),curTime) &&
                    DateUtil.dateCompare(curTime,seckillGoods.getEndTime())){
                // 可以生成下单码
                if (StringUtils.isNotEmpty(userId)){
                    String encrypt = MD5.encrypt(userId);
                    return Result.ok(encrypt);
                }
            }
        }
        return Result.fail().message("获取下单码失败！");
    }

    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId,HttpServletRequest request){
        String skuIdStr = request.getParameter("skuIdStr");
        String userId = AuthContextHolder.getUserId(request);
        if(!skuIdStr.equals(MD5.encrypt(userId))){
            //验证失败
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        String state = CacheHelper.get(skuId.toString()).toString();
        if(null!=state){
            //验证失败
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        if ("1".equals(state)){
            //请求合法
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);

            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,MqConst.ROUTING_SECKILL_USER,userRecode);

        }else {
            //商品为0
            //验证失败
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        return Result.ok();
    }

    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        Result result = seckillGoodsService.checkOrder(skuId,userId);
        return result;
    }

    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        OrderRecode orderRecode = (OrderRecode)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        if(null==orderRecode){
            return Result.fail().message("下单失败");
        }
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        orderDetails.add(orderDetail);
        //订单总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetails);
        orderInfo.sumTotalAmount();

        Map<String, Object> map = new HashMap<>();
        map.put("detailArrayList",orderDetails);
        map.put("userAddressList",userAddressList);
        map.put("totlAmount",orderInfo.getTotalAmount());
        return Result.ok(map);
    }

    /**
     * 秒杀的提交订单
     * @return
     */
    @GetMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        OrderRecode orderRecode = (OrderRecode)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        if(null==orderRecode) {
            return Result.fail().message("非法操作");
        }
        Long orderId = orderFeignClient.submitOrder(orderInfo);
        if(null == orderId){
            return Result.fail().message("下订单失败");
        }
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);

        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId,orderId.toString());
        return Result.ok(orderId);
    }
}
