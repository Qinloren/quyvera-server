package com.zeeyeh.quyvera.user.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.user.domain.UserProfile;
import com.zeeyeh.quyvera.user.service.UserProfileService;
import com.zeeyeh.quyvera.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;

/**
 * 用户资料表业务接口实现类
 * @author Qinloren
 */
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

}




