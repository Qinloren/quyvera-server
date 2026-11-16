package com.zeeyeh.quyvera.plugin.service.plugin.manager;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Listener;
import com.zeeyeh.quyvera.plugin.sdk.event.Vetoable;
import com.zeeyeh.quyvera.plugin.service.plugin.event.RegisteredListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 事件管理器。AI生成
 * @author Qinloren
 * @date 2026/11/15 17:12
 */
@Slf4j
public final class EventManager {

    /**
     * 保护监听器集合。
     */
    private final Object lock = new Object();

    /**
     * 事件类型 -> 监听器列表。
     */
    private final Map<Class<? extends Event>, List<RegisteredListener<?>>> listeners =
            new HashMap<>();

    /**
     * 异步事件执行器。
     */
    private final Executor asyncExecutor;

    /**
     * 监听器注册序号。
     */
    private long sequence;

    /**
     * 当前线程的事件回调嵌套深度。
     *
     * 使用 ThreadLocal 支持事件回调中再次触发同步事件。
     */
    private final ThreadLocal<Integer> dispatchDepth = new ThreadLocal<>();

    public EventManager(Executor asyncExecutor) {
        this.asyncExecutor = Objects.requireNonNull(
                asyncExecutor, "asyncExecutor"
        );
    }

    /**
     * 注册监听器。
     */
    public <T extends Event> void register(
            String pluginName,
            Class<T> eventType,
            Listener<T> listener
    ) {
        Objects.requireNonNull(pluginName, "pluginName");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(listener, "listener");

        if (pluginName.isBlank()) {
            throw new IllegalArgumentException("插件名称不能为空");
        }

        int order = listener.order();

        synchronized (lock) {
            List<RegisteredListener<?>> registeredListeners =
                    listeners.computeIfAbsent(
                            eventType,
                            ignored -> new ArrayList<>()
                    );

            for (RegisteredListener<?> registered : registeredListeners) {
                if (registered.getPluginName().equals(pluginName)
                        && registered.getListener() == listener) {
                    throw new IllegalStateException(
                            "监听器重复注册: " + pluginName
                                    + " -> " + eventType.getName()
                    );
                }
            }

            registeredListeners.add(
                    new RegisteredListener<>(
                            pluginName,
                            eventType,
                            listener,
                            order,
                            sequence++
                    )
            );
        }
    }

    /**
     * 注销指定监听器。
     *
     * <p>
     * 不允许在事件监听器回调中同步注销，
     * 避免当前线程等待自身退出。
     * </p>
     */
    public <T extends Event> boolean unregister(
            String pluginName,
            Class<T> eventType,
            Listener<T> listener
    ) {
        Objects.requireNonNull(pluginName, "pluginName");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(listener, "listener");

        ensureNotDispatchThread();

        RegisteredListener<?> target = null;

        synchronized (lock) {
            List<RegisteredListener<?>> registeredListeners =
                    listeners.get(eventType);

            if (registeredListeners == null) {
                return false;
            }

            for (RegisteredListener<?> registered : registeredListeners) {
                if (registered.getPluginName().equals(pluginName)
                        && registered.getListener() == listener) {
                    target = registered;
                    break;
                }
            }

            if (target == null) {
                return false;
            }

            // 先停用，阻止新的事件进入。
            target.deactivate();

            // 再从监听器集合移除。
            registeredListeners.remove(target);

            if (registeredListeners.isEmpty()) {
                listeners.remove(eventType);
            }
        }

        // 不持有全局锁等待正在执行的监听器结束。
        target.awaitIdle();

        return true;
    }

    /**
     * 注销插件的全部监听器。
     *
     * <p>
     * 先统一停用并移除该插件的全部监听器，
     * 再等待所有已经进入执行状态的监听器结束。
     * </p>
     */
    public void unregisterPlugin(String pluginName) {
        Objects.requireNonNull(pluginName, "pluginName");

        if (pluginName.isBlank()) {
            throw new IllegalArgumentException("插件名称不能为空");
        }

        ensureNotDispatchThread();

        List<RegisteredListener<?>> removed = new ArrayList<>();

        synchronized (lock) {
            listeners.entrySet().removeIf(entry -> {
                List<RegisteredListener<?>> registeredListeners =
                        entry.getValue();

                registeredListeners.removeIf(registered -> {
                    if (!registered.getPluginName().equals(pluginName)) {
                        return false;
                    }

                    // 停用监听器，阻止新的事件进入。
                    registered.deactivate();

                    removed.add(registered);

                    return true;
                });

                return registeredListeners.isEmpty();
            });
        }

        // 不持有全局锁等待监听器执行结束。
        for (RegisteredListener<?> registered : removed) {
            registered.awaitIdle();
        }
    }

    /**
     * 触发事件。
     */
    public void callEvent(Event event) {
        Objects.requireNonNull(event, "event");

        if (event.isAsync()) {
            asyncExecutor.execute(() -> dispatch(event));
        } else {
            dispatch(event);
        }
    }

    /**
     * 同步触发事件
     */
    public void callEventSync(Event event) {
        Objects.requireNonNull(event, "event");
        dispatch(event);
    }

    /**
     * 事件分发。
     */
    private void dispatch(Event event) {
        Objects.requireNonNull(event, "event");

        List<RegisteredListener<?>> matchedListeners = new ArrayList<>();

        // 获取匹配的监听器快照。
        synchronized (lock) {
            Class<?> actualType = event.getClass();

            for (Map.Entry<Class<? extends Event>,
                    List<RegisteredListener<?>>> entry : listeners.entrySet()) {

                Class<? extends Event> registeredType = entry.getKey();

                // 支持父事件类型监听子事件。
                if (registeredType.isAssignableFrom(actualType)) {
                    matchedListeners.addAll(entry.getValue());
                }
            }
        }

        // 按监听器优先级排序，相同优先级按注册顺序执行。
        matchedListeners.sort(
                Comparator
                        .comparingInt(
                                (RegisteredListener<?> registered) -> registered.order()
                        )
                        .thenComparingLong(
                                RegisteredListener::sequence
                        )
        );

        // 依次执行监听器。
        for (RegisteredListener<?> registered : matchedListeners) {

            // 如果监听器已经注销，则不再进入执行。
            if (!registered.tryEnter()) {
                continue;
            }

            try {
                // 标记当前线程正在执行事件回调。
                enterDispatch();

                try {
                    // 事件已经被取消，并且监听器选择跳过取消事件。
                    if (event instanceof Vetoable vetoable
                            && vetoable.isVetoed()
                            && registered.getListener().skipOnVeto()) {
                        continue;
                    }

                    // 执行监听器。
                    invoke(registered, event);

                } finally {
                    // 无论监听器正常返回还是抛出异常，都退出回调状态。
                    exitDispatch();
                }

            } catch (RuntimeException exception) {
                log.error(
                        "插件事件监听器执行失败，插件: {}，事件: {}",
                        registered.getPluginName(),
                        event.getClass().getName(),
                        exception
                );

            } finally {
                // 通知注销操作：当前监听器已经执行完毕。
                registered.exit();
            }
        }
    }

    /**
     * 调用监听器。
     */
    @SuppressWarnings("unchecked")
    private <T extends Event> void invoke(
            RegisteredListener<?> registered,
            Event event
    ) {
        Listener<T> listener = (Listener<T>) registered.getListener();
        listener.on((T) event);
    }

    /**
     * 判断当前线程是否正在执行事件监听器回调。
     */
    public boolean isDispatchThread() {
        Integer depth = dispatchDepth.get();
        return depth != null && depth > 0;
    }

    /**
     * 进入事件回调。
     */
    private void enterDispatch() {
        Integer depth = dispatchDepth.get();

        dispatchDepth.set(depth == null ? 1 : depth + 1);
    }

    /**
     * 退出事件回调。
     */
    private void exitDispatch() {
        Integer depth = dispatchDepth.get();

        if (depth == null || depth <= 1) {
            dispatchDepth.remove();
        } else {
            dispatchDepth.set(depth - 1);
        }
    }

    /**
     * 禁止在事件监听器回调中同步注销监听器或插件。
     */
    private void ensureNotDispatchThread() {
        if (isDispatchThread()) {
            throw new IllegalStateException(
                    "不允许在事件监听器回调中同步注销监听器或插件，"
                            + "请将注销操作提交到独立线程或任务队列中执行"
            );
        }
    }
}