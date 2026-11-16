package com.zeeyeh.quyvera.plugin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 插件信息
 * @author Qinloren
 * @date 2026/10/25 22:26
 */
@Data
@AllArgsConstructor
public class PluginVo {

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
