package com.zeeyeh.quyvera.plugin.service.plugin.event;

import com.zeeyeh.quyvera.plugin.sdk.event.Event;
import com.zeeyeh.quyvera.plugin.sdk.event.Listener;
import lombok.Getter;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 监听器注册记录
 * @author Qinloren
 * @date 2026/11/15 17:13
 */
public final class RegisteredListener<T extends Event> {

    @Getter
    private final String pluginName;

    @Getter
    private final Class<T> eventType;

    @Getter
    private final Listener<T> listener;

    private final int order;

    private final long sequence;

    /**
     * 保护监听器执行状态。
     */
    private final ReentrantLock executionLock = new ReentrantLock();

    /**
     * 等待所有正在执行的调用结束。
     */
    private final Condition idle = executionLock.newCondition();

    /**
     * 当前正在执行的调用数量。
     */
    private int inFlight;

    /**
     * 当前线程正在执行该监听器的次数。
     * <p>
     * 使用 IdentityHashMap，按照 Thread 对象身份进行匹配。
     */
    private final Map<Thread, Integer> executingThreads =
            new IdentityHashMap<>();

    /**
     * 是否允许新的调用进入。
     */
    private boolean active = true;

    public RegisteredListener(
            String pluginName,
            Class<T> eventType,
            Listener<T> listener,
            int order,
            long sequence
    ) {
        this.pluginName = Objects.requireNonNull(
                pluginName, "pluginName"
        );

        this.eventType = Objects.requireNonNull(
                eventType, "eventType"
        );

        this.listener = Objects.requireNonNull(
                listener, "listener"
        );

        this.order = order;
        this.sequence = sequence;
    }

    public int order() {
        return order;
    }

    public long sequence() {
        return sequence;
    }

    /**
     * 尝试进入监听器。
     *
     * @return 是否成功进入
     */
    public boolean tryEnter() {
        executionLock.lock();

        try {
            if (!active) {
                return false;
            }

            Thread current = Thread.currentThread();

            inFlight++;

            executingThreads.merge(current, 1, Integer::sum);

            return true;

        } finally {
            executionLock.unlock();
        }
    }

    /**
     * 结束一次监听器调用。
     */
    public void exit() {
        executionLock.lock();

        try {
            Thread current = Thread.currentThread();

            Integer count = executingThreads.get(current);

            // 检查调用状态，避免错误递减 inFlight。
            if (count == null || count <= 0 || inFlight <= 0) {
                throw new IllegalStateException(
                        "监听器执行状态异常: " + pluginName
                );
            }

            if (count == 1) {
                executingThreads.remove(current);
            } else {
                executingThreads.put(current, count - 1);
            }

            inFlight--;

            if (inFlight == 0) {
                idle.signalAll();
            }

        } finally {
            executionLock.unlock();
        }
    }

    /**
     * 禁止新的调用进入。
     * <p>
     * 已经进入执行状态的调用不受影响。
     */
    public void deactivate() {
        executionLock.lock();

        try {
            active = false;
        } finally {
            executionLock.unlock();
        }
    }

    /**
     * 判断当前线程是否正在执行该监听器。
     */
    public boolean isExecutingOnCurrentThread() {
        executionLock.lock();

        try {
            return executingThreads.containsKey(
                    Thread.currentThread()
            );
        } finally {
            executionLock.unlock();
        }
    }

    /**
     * 等待已经开始的监听器调用结束。
     *
     * <p>
     * 等待过程不因线程中断而提前结束。
     * 等待完成后恢复线程的中断标记。
     * </p>
     */
    public void awaitIdle() {
        Thread current = Thread.currentThread();
        boolean interrupted = false;

        executionLock.lock();

        try {
            if (executingThreads.containsKey(current)) {
                throw new IllegalStateException(
                        "不能在监听器自身执行期间同步注销该监听器: "
                                + pluginName
                );
            }

            while (inFlight > 0) {
                try {
                    idle.await();
                } catch (InterruptedException exception) {
                    interrupted = true;
                }
            }

        } finally {
            executionLock.unlock();

            if (interrupted) {
                current.interrupt();
            }
        }
    }
}