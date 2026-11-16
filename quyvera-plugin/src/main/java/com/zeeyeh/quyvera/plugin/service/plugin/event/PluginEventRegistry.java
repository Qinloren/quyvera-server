package com.zeeyeh.quyvera.plugin.service.plugin.event;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.EventRegistry;
import com.zeeyeh.quyvera.plugin.sdk.event.Listener;
import com.zeeyeh.quyvera.plugin.service.plugin.manager.EventManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * 插件独立事件注册器。AI生成
 * @author Qinloren
 * @date 2026/11/15 17:31
 */
public final class PluginEventRegistry implements EventRegistry, AutoCloseable {

    private enum State {
        OPEN,
        ACTIVATING,
        ACTIVE,
        CLOSING,
        CLOSED
    }

    private final String pluginName;

    private final EventManager eventManager;

    /**
     * 只允许在当前对象 monitor 内访问。
     */
    private State state = State.OPEN;

    /**
     * 尚未激活时暂存的监听器。
     */
    private final List<Registration<?>> registrations = new ArrayList<>();

    public PluginEventRegistry(
            String pluginName,
            EventManager eventManager
    ) {
        this.pluginName = Objects.requireNonNull(
                pluginName,
                "pluginName"
        );

        this.eventManager = Objects.requireNonNull(
                eventManager,
                "eventManager"
        );
    }

    /**
     * 注册事件监听器。
     *
     * <p>OPEN 状态下暂存监听器。</p>
     * <p>ACTIVE 状态下直接注册到 EventManager。</p>
     */
    @Override
    public synchronized <T extends Event> void register(
            Class<T> eventType,
            Listener<T> listener
    ) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(listener, "listener");

        if (state == State.OPEN) {
            registrations.add(
                    new Registration<>(eventType, listener)
            );

            return;
        }

        if (state == State.ACTIVE) {
            eventManager.register(
                    pluginName,
                    eventType,
                    listener
            );

            return;
        }

        throw new IllegalStateException(
                "插件事件注册器当前不允许注册监听器: "
                        + pluginName
                        + ", state="
                        + state
        );
    }

    /**
     * 注销指定事件监听器。
     */
    @Override
    public <T extends Event> boolean unregister(
            Class<T> eventType,
            Listener<T> listener
    ) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(listener, "listener");

        synchronized (this) {
            if (state == State.OPEN) {
                for (int i = 0; i < registrations.size(); i++) {
                    Registration<?> registration = registrations.get(i);

                    if (registration.matches(eventType, listener)) {
                        registrations.remove(i);
                        return true;
                    }
                }

                return false;
            }

            if (state != State.ACTIVE) {
                throw new IllegalStateException(
                        "插件事件注册器当前不允许注销监听器: "
                                + pluginName
                                + ", state="
                                + state
                );
            }
        }

        /*
         * EventManager.unregister() 可能等待监听器执行结束，
         * 因此不能持有当前对象的 monitor。
         */
        return eventManager.unregister(
                pluginName,
                eventType,
                listener
        );
    }

    /**
     * 发布事件。
     *
     * <p>只有 ACTIVE 状态允许发布事件。</p>
     */
    @Override
    public void callEvent(Event event) {
        Objects.requireNonNull(event, "event");

        synchronized (this) {
            if (state != State.ACTIVE) {
                throw new IllegalStateException(
                        "插件事件注册器尚未激活或已经关闭: "
                                + pluginName
                );
            }
        }

        eventManager.callEvent(event);
    }

    /**
     * 激活事件注册器。
     *
     * <p>由 PluginManager 在插件 init() 和 load() 全部成功后调用。</p>
     *
     * <p>激活期间不允许并发注册、注销、发布事件或关闭注册器。</p>
     */
    public void activate() {
        List<Registration<?>> pendingRegistrations;

        synchronized (this) {
            if (state == State.ACTIVE) {
                return;
            }

            if (state != State.OPEN) {
                throw new IllegalStateException(
                        "无法激活插件事件注册器: "
                                + pluginName
                                + ", state="
                                + state
                );
            }

            state = State.ACTIVATING;

            pendingRegistrations = new ArrayList<>(registrations);
        }

        try {
            for (Registration<?> registration : pendingRegistrations) {
                registration.register(
                        eventManager,
                        pluginName
                );
            }

            synchronized (this) {
                registrations.clear();
                state = State.ACTIVE;
                notifyAll();
            }

        } catch (RuntimeException | Error cause) {
            /*
             * 激活过程中可能已经有部分监听器注册成功。
             *
             * 必须先撤销所有已经注册的监听器，
             * 再结束激活失败流程。
             */
            try {
                eventManager.unregisterPlugin(pluginName);

                synchronized (this) {
                    registrations.clear();
                    state = State.CLOSED;
                    notifyAll();
                }

            } catch (RuntimeException | Error cleanupException) {
                /*
                 * 清理失败时保留注册器，以便 PluginManager
                 * 后续再次调用 close() 进行清理。
                 */
                synchronized (this) {
                    state = State.ACTIVE;
                    notifyAll();
                }

                addSuppressedSafely(cause, cleanupException);
            }

            throw cause;
        }
    }

    /**
     * 关闭事件注册器。
     *
     * <p>关闭顺序：</p>
     * <ol>
     *     <li>禁止新的注册、注销和事件发布。</li>
     *     <li>注销该插件的全部监听器。</li>
     *     <li>等待正在执行的监听器结束。</li>
     *     <li>进入 CLOSED 状态。</li>
     * </ol>
     */
    @Override
    public void close() {
        ensureNotInEventCallback();

        State previousState;

        synchronized (this) {
            while (state == State.ACTIVATING
                    || state == State.CLOSING) {

                try {
                    wait();

                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();

                    throw new IllegalStateException(
                            "等待插件事件注册器关闭时被中断: "
                                    + pluginName,
                            exception
                    );
                }
            }

            if (state == State.CLOSED) {
                return;
            }

            previousState = state;
            state = State.CLOSING;
        }

        try {
            /*
             * 不能持有当前对象 monitor 等待监听器结束。
             */
            eventManager.unregisterPlugin(pluginName);

            synchronized (this) {
                registrations.clear();
                state = State.CLOSED;
                notifyAll();
            }

        } catch (RuntimeException | Error cause) {
            synchronized (this) {
                /*
                 * 关闭失败，恢复关闭前的状态，
                 * 允许后续重新尝试关闭。
                 */
                state = previousState;
                notifyAll();
            }

            throw cause;
        }
    }

    /**
     * 判断注册器是否已经激活。
     */
    public synchronized boolean isActive() {
        return state == State.ACTIVE;
    }

    /**
     * 判断注册器是否已经关闭。
     */
    public synchronized boolean isClosed() {
        return state == State.CLOSED;
    }

    /**
     * 禁止在事件回调线程中同步关闭注册器。
     */
    private void ensureNotInEventCallback() {
        if (eventManager.isDispatchThread()) {
            throw new IllegalStateException(
                    "不允许在事件监听器回调中同步关闭插件事件注册器: "
                            + pluginName
            );
        }
    }

    /**
     * 添加 suppressed exception，避免异常对象自我抑制。
     */
    private void addSuppressedSafely(
            Throwable original,
            Throwable additional
    ) {
        if (original != additional) {
            original.addSuppressed(additional);
        }
    }

    /**
     * 暂存的监听器注册信息。
     */
    private static final class Registration<T extends Event> {

        private final Class<T> eventType;

        private final Listener<T> listener;

        private Registration(
                Class<T> eventType,
                Listener<T> listener
        ) {
            this.eventType = eventType;
            this.listener = listener;
        }

        private void register(
                EventManager eventManager,
                String pluginName
        ) {
            eventManager.register(
                    pluginName,
                    eventType,
                    listener
            );
        }

        private boolean matches(
                Class<?> targetEventType,
                Listener<?> targetListener
        ) {
            return eventType == targetEventType
                    && listener == targetListener;
        }
    }
}