package com.zeeyeh.quyvera.plugin.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 插件分页查询请求参数
 * @author Qinloren
 * @date 2026/10/26 18:27
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PluginPageQueryDto {

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
     * 安装起始时间
     */
    private LocalDateTime installTimeStart;

    /**
     * 安装截止时间
     */
    private LocalDateTime installTimeEnd;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sort = "installTime";

    /**
     * 当前页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 10;
}
