package com.zeeyeh.quyvera.plugin.controller;

import com.zeeyeh.common.plugin.annotations.PluginEvent;
import com.zeeyeh.common.plugin.annotations.PluginParam;
import com.zeeyeh.quyvera.plugin.dto.PluginInstallDto;
import com.zeeyeh.quyvera.plugin.sdk.event.plugin.*;
import com.zeeyeh.quyvera.plugin.service.PluginService;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 插件web接口
 * @author Qinloren
 * @date 2026/10/25 22:38
 */
@RestController
@RequestMapping("/plugin")
public class PluginController {

    private final PluginService pluginService;

    public PluginController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @PostMapping(value = "/install", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PluginEvent(
            value = PluginInstallPreEvent.class,
            params = {
                    @PluginParam(name = "dto", mode = PluginParam.Mode.BEAN),
                    @PluginParam(name = "file", mode = PluginParam.Mode.PROPERTY)
            }
    )
    @PluginEvent(value = PluginInstallPostEvent.class, phase = PluginEvent.Phase.POST)
    public PluginVo install(@ModelAttribute PluginInstallDto dto, @RequestPart("file") MultipartFile file) {
        return pluginService.installPlugin(dto, file);
    }

    @PostMapping("/enable")
    @PluginEvent(PluginEnabledPreEvent.class)
    @PluginEvent(value = PluginEnabledPostEvent.class, phase = PluginEvent.Phase.POST)
    public PluginVo enable(@RequestParam("identifier") String identifier) {
        if (StringUtils.isNumeric(identifier)) {
            return pluginService.enablePluginById(Long.parseLong(identifier));
        } else {
            return pluginService.enablePluginByCode(identifier);
        }
    }

    @PostMapping("/disable")
    @PluginEvent(PluginEnabledPreEvent.class)
    @PluginEvent(value = PluginDisabledPostEvent.class, phase = PluginEvent.Phase.POST)
    public PluginVo disable(@RequestParam String identifier) {
        if (StringUtils.isNumeric(identifier)) {
            return pluginService.disablePluginById(Long.parseLong(identifier));
        } else {
            return pluginService.disablePluginByCode(identifier);
        }
    }

    @PostMapping("/unload")
    @PluginEvent(PluginUnloadPreEvent.class)
    @PluginEvent(value = PluginUnloadPostEvent.class, phase = PluginEvent.Phase.POST)
    public void unload(@RequestParam("identifier") String identifier) {
        if (StringUtils.isNumeric(identifier)) {
            pluginService.unloadPluginById(Long.parseLong(identifier));
        } else {
            pluginService.unloadPluginByCode(identifier);
        }
    }
}
