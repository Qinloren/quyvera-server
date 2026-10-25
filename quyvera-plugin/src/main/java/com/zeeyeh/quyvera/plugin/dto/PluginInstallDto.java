package com.zeeyeh.quyvera.plugin.dto;

import com.zeeyeh.quyvera.plugin.domain.Plugin;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 插件安装请求参数
 * @author Qinloren
 * @date 2026/10/25 22:29
 */
@Data
@AllArgsConstructor
@AutoMapper(target = Plugin.class)
public class PluginInstallDto {

    /**
     * 插件编码
     */
    private String pluginCode;

    /**
     * 描述
     */
    private String description;
}
