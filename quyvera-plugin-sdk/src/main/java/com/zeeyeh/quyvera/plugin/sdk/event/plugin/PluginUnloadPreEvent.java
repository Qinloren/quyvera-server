package com.zeeyeh.quyvera.plugin.sdk.event.plugin;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * 插件卸载前事件
 * @author Qinloren
 * @date 2026/11/16 17:03
 */
public class PluginUnloadPreEvent extends Event implements Vetoable {
    private boolean vetoed;
    @Getter
    @Setter
    private String identifier;

    /**
     * 是否为插件Id
     */
    public boolean isId() {
        return StringUtils.isNumeric(this.identifier);
    }

    /**
     * 是否为插件编码
     */
    public boolean isCode() {
        return !isId();
    }

    @Override
    public boolean isVetoed() {
        return vetoed;
    }

    @Override
    public void setVetoed(boolean vetoed) {
        this.vetoed = vetoed;
    }
}
