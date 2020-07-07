package com.atguigu.gmall1213.item.controller;

import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.item.service.ItemService;
import com.atguigu.gmall1213.model.product.SkuInfo;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.sentinel.SentinelProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author mqx
 * 商品详情的数据接口【数据提供者】
 * @date 2020/6/13 11:35
 */
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    @GetMapping("{skuId}")
    public Result getItem(@PathVariable Long skuId){
        Map<String, Object> result = itemService.getBySkuId(skuId);
        //Logger logger = LoggerFactory.getLogger(SkuInfo.class);
        //logger.info(skuId.toString());
        // 放入Result
        return Result.ok(result);
    }
}
