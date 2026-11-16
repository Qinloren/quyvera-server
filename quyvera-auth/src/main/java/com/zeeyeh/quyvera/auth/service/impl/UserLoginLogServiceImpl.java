package com.zeeyeh.quyvera.auth.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.zeeyeh.quyvera.auth.domain.UserLoginLog;
import com.zeeyeh.quyvera.auth.service.UserLoginLogService;
import com.zeeyeh.quyvera.auth.mapper.UserLoginLogMapper;
import org.springframework.stereotype.Service;

/**
 * 登录历史业务接口实现
 * @author Qinloren
 * @date  2026-10-23 23:40:26
 */
@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog>
    implements UserLoginLogService{

}




