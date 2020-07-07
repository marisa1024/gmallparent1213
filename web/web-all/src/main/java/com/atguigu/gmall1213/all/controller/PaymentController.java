package com.atguigu.gmall1213.all.controller;

import com.atguigu.gmall1213.model.order.OrderInfo;
import com.atguigu.gmall1213.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 * @date 2020/6/29 15:36
 */
@Controller
public class PaymentController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    // http://payment.gmall.com/pay.html?orderId=153
    @GetMapping("pay.html")
    public String pay(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));

        // 保存一个orderInfo 对象
        request.setAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

    //支付成功之后回调地址 http://payment.gmall.com/pay/success.html
    @GetMapping("pay/success.html")
    public String success(){
        return "payment/success";
    }
}
