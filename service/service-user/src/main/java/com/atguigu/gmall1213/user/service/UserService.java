package com.atguigu.gmall1213.user.service;

import com.atguigu.gmall1213.model.user.UserInfo;

/**
 * @author mqx
 * @date 2020/6/22 14:44
 */
public interface UserService {

    // 登录
    UserInfo login(UserInfo userInfo);

    // UserInfo login(String userName , String pwd);
}
