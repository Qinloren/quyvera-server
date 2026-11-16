package com.zeeyeh.quyvera.plugin.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.zeeyeh.quyvera.common.core.entity.PageResult;
import com.zeeyeh.quyvera.plugin.dto.PluginInstallDto;
import com.zeeyeh.quyvera.plugin.domain.Plugin;
import com.zeeyeh.quyvera.plugin.dto.PluginPageQueryDto;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 插件表业务接口
 * @author Qinloren
 * @date  2026-10-25 22:12:13
 */
public interface PluginService extends IService<Plugin> {

    /**
     * 获取插件信息
     * @param id 插件Id
     * @return 插件信息
     */
    PluginVo getPluginById(Long id);

    /**
     * 获取插件信息
     * @param name 插件名称
     * @return 插件信息
     */
    PluginVo getPluginByName(String name);

    /**
     * 获取插件信息
     * @param code 插件编码
     * @return 插件信息
     */
    PluginVo getPluginByCode(String code);

    /**
     * 批量获取插件信息
     * @param pluginIds 插件Id列表
     * @return 插件信息列表
     */
    List<PluginVo> listPluginByIds(List<Long> pluginIds);

    /**
     * 分页查询插件信息
     * @param dto 分页查询请求参数
     * @return 分页查询结果
     */
    PageResult<PluginVo> pageQuery(PluginPageQueryDto dto);

    /**
     * 安装插件
     * @param dto 插件安装请求参数
     * @param file 插件文件
     * @return 安装后的插件信息
     */
    PluginVo installPlugin(PluginInstallDto dto, MultipartFile file);

    /**
     * 启用插件
     * @param id 插件Id
     * @return 启用后的插件信息
     */
    PluginVo enablePluginById(Long id);

    /**
     * 启用插件
     * @param code 插件编码
     * @return 启用后的插件信息
     */
    PluginVo enablePluginByCode(String code);

    /**
     * 禁用插件
     * @param id 插件Id
     * @return 禁用后的插件信息
     */
    PluginVo disablePluginById(Long id);

    /**
     * 禁用插件
     * @param code 插件编码
     * @return 禁用后的插件信息
     */
    PluginVo disablePluginByCode(String code);

    /**
     * 更新插件状态
     * @param id 插件Id
     * @param status 插件状态
     * @return 更新后的插件信息
     */
    PluginVo updatePluginStatus(Long id, Boolean status);

    /**
     * 更新插件状态
     * @param code 插件编码
     * @param status 插件状态
     * @return 更新后的插件信息
     */
    PluginVo updatePluginStatus(String code, Boolean status);

    /**
     * 卸载插件
     * @param id 插件Id
     */
    void unloadPluginById(Long id);

    /**
     * 卸载插件
     * @param code 插件编码
     */
    void unloadPluginByCode(String code);

    /**
     * 卸载插件文件
     * @param plugin 插件对象
     */
    void unloadPluginFile(Plugin plugin);
}
