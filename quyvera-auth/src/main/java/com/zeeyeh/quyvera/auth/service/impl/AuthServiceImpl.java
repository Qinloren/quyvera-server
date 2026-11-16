package com.zeeyeh.quyvera.auth.service.impl;

import com.zeeyeh.quyvera.auth.enums.AuthErrorCode;
import com.zeeyeh.quyvera.auth.service.AuthService;
import com.zeeyeh.quyvera.common.auth.AuthRequest;
import com.zeeyeh.quyvera.common.auth.AuthResult;
import com.zeeyeh.quyvera.common.auth.AuthStrategy;
import com.zeeyeh.quyvera.common.core.utils.BusinessAsserts;
import com.zeeyeh.quyvera.user.api.v1.UserInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Qinloren
 * @date 2026/10/24 16:01
 */
@Service
public class AuthServiceImpl implements AuthService {
}
