package com.zeeyeh.quyvera.plugin.sdk.enums;

/**
 * 插件状态
 * @author Qinloren
 * @date 2026/11/9 22:19
 */
public enum PluginState {
    /**
     * 已发现，尚未开始初始化
     */
    DISCOVERED,

    /**
     * 正在初始化
     */
    INITIALIZING,

    /**
     * 初始化完成
     */
    INITIALIZED,

    /**
     * 正在加载
     */
    LOADING,

    /**
     * 加载完成
     */
    LOADED,

    /**
     * 正在卸载
     */
    UNLOADING,

    /**
     * 卸载完成
     */
    UNLOADED,

    /**
     * 加载失败
     */
    FAILED
}
