package com.zeeyeh.quyvera.plugin.enums;

import com.zeeyeh.quyvera.common.core.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Qinloren
 * @date 2026/10/25 22:52
 */
@Getter
@AllArgsConstructor
public enum PluginErrorCode implements ErrorCode {
    PLUGIN_FILE_NOT_EMPTY(33001, "error.plugin.file.not.empty"),
    PLUGIN_EXT_NOT_SUPPORTED(33002, "error.plugin.ext.not.supported"),
    PLUGIN_PATH_INVALID(33003, "error.plugin.path.invalid"),
    PLUGIN_UPLOAD_FAILED(33004, "error.plugin.upload.failed"),
    PLUGIN_META_LOAD_FAILED(33005, "error.plugin.meta.load.failed"),
    ;
    private final Integer code;
    private final String key;
}
