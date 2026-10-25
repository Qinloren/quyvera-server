package com.zeeyeh.quyvera.plugin.controller;

import com.zeeyeh.quyvera.plugin.dto.PluginInstallDto;
import com.zeeyeh.quyvera.plugin.vo.PluginVo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Qinloren
 * @date 2026/10/25 22:38
 */
@RestController
@RequestMapping("/plugin")
public class PluginController {

    @PostMapping(value = "/install", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PluginVo install(@ModelAttribute PluginInstallDto dto, @RequestPart("file") MultipartFile file) {

    }
}
