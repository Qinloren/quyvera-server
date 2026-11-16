package com.zeeyeh.quyvera.plugin.sdk.event.plugin;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 插件安装前事件
 * @author Qinloren
 * @date 2026/11/16 16:50
 */
public class PluginInstallPreEvent extends Event implements Vetoable {
    private boolean vetoed;

    /**
     * 插件编码
     */
    @Getter
    @Setter
    private String pluginCode;

    /**
     * 描述
     */
    @Getter
    @Setter
    private String description;

    /**
     * 上传的文件
     */
    @Getter
    @Setter
    private MultipartFile file;

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}
