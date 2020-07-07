package com.atguigu.gmall1213.user.service;

import com.atguigu.gmall1213.model.user.UserAddress;

import java.util.List;

/**
 * @author mqx
 * @date 2020/6/24 14:20
 */
public interface UserAddressService {

    // 业务接口  select * from user_address where user_id = ?
    List<UserAddress> findUserAddressListByUserId(String userId);


}
