package com.zeeyeh.quyvera.plugin.convert;

import com.zeeyeh.quyvera.plugin.domain.Plugin;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * 插件转换器接口
 * @author Qinloren
 * @date 2026/10/25 22:25
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PluginConvert {

    /**
     * 将插件对象转换为插件视图对象
     * @param plugin 插件对象
     * @return 插件视图对象
     */
    PluginVo toVo(Plugin plugin);
}
