package com.zeeyeh.quyvera.plugin.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.zeeyeh.quyvera.plugin.dto.PluginInstallDto;
import com.zeeyeh.quyvera.plugin.domain.Plugin;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 插件表业务接口
 * @author Qinloren
 * @date  2026-10-25 22:12:13
 */
public interface PluginService extends IService<Plugin> {

    /**
     * 安装插件
     * @param dto 插件安装请求参数
     * @param file 插件文件
     * @return 安装后的插件信息
     */
    PluginVo installPlugin(PluginInstallDto dto, MultipartFile file);
}
