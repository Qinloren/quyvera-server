package com.zeeyeh.quyvera.plugin.sdk.service;

import java.util.Map;

/**
 * @author Qinloren
 * @date 2026/11/16 20:07
 */
public interface PluginServiceInvoker {

    /**
     * 调用主程序开放的服务 API
     * @param api API 名称
     * @param params 参数
     * @return JSON 兼容的数据
     */
    Object invoke(String api, Map<String, Object> params);
}
