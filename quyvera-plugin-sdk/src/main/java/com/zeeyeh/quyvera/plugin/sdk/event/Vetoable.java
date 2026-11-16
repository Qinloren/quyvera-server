package com.zeeyeh.quyvera.plugin.sdk.event;

/**
 * 标记事件可被取消
 * @author Qinloren
 * @date 2026/11/8 13:48
 */
public interface Vetoable {

    /**
     * 是否可被取消
     * @return 状态
     */
    boolean isVetoed();

    /**
     * 设置是否可被取消
     * @param vetoed 状态
     */
    void setVetoed(boolean vetoed);

    /**
     * 被取消原因
     * @return 取消原因
     */
    default String getVetoReason() {
        return "";
    }
}
