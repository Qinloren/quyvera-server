package com.zeeyeh.quyvera.plugin.sdk.event.plugin;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 插件启用后事件
 * @author Qinloren
 * @date 2026/11/16 16:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PluginEnabledPostEvent extends Event {

    /**
     * 插件Id
     */
    private Long id;

    /**
     * 插件编码
     */
    private String pluginCode;

    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 存储文件名称
     */
    private String filename;

    /**
     * 版本号
     */
    private String version;

    /**
     * 描述
     */
    private String description;

    /**
     * 作者
     */
    private String author;

    /**
     * 是否启用：0=禁用，1=启用
     */
    private Boolean isEnabled;

    /**
     * 安装时间
     */
    private LocalDateTime installTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
