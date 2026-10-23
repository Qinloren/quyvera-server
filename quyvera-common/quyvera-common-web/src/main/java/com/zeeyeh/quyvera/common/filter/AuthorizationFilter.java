package com.zeeyeh.quyvera.common.filter;

import cn.hutool.core.util.StrUtil;
import com.zeeyeh.quyvera.common.config.properties.AppSecurityProperties;
import com.zeeyeh.quyvera.common.constants.CommonErrorCode;
import com.zeeyeh.quyvera.common.core.exception.BusinessExceptions;
import com.zeeyeh.quyvera.common.core.utils.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Qinloren
 * @date 2026/10/20 18:13
 */
public class AuthorizationFilter extends OncePerRequestFilter {
    private final AppSecurityProperties properties;
    private final String secret;
    private final int expire;

    public AuthorizationFilter(AppSecurityProperties properties, String secret, int expire) {
        this.properties = properties;
        this.secret = secret;
        this.expire = expire;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = StrUtil.removePrefix(authorization, "Bearer ");
        if (!TokenUtil.builder(secret, expire).validateToken(token)) {
            throw new BusinessExceptions(CommonErrorCode.NOT_LOGIN);
        }
        String userId = request.getHeader(properties.getUserIdHeader());
        if (StringUtils.isNotBlank(userId) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String roles = request.getHeader(properties.getRolesHeader());
            List<GrantedAuthority> authorities = new ArrayList<>(Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(SimpleGrantedAuthority::new)
                    .toList());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
