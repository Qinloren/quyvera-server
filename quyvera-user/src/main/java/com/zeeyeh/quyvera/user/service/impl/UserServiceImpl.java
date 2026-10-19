package com.zeeyeh.quyvera.user.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.user.domain.User;
import com.zeeyeh.quyvera.user.service.UserService;
import com.zeeyeh.quyvera.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户表业务接口实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}




