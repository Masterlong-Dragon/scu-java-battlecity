package com.masterlong.framework.components;

import com.masterlong.framework.essentials.IGameEvent;

import java.util.HashMap;

/**
 * 周期事件计时器
 */
public class PeriodicTimer {
    protected final HashMap<String, IGameEvent> events;//事件
    protected final HashMap<String, Long> currentClocks;//每个事件的时钟
    protected long minInterval, timerClock;//最小更新间隔与计时器时钟
    protected final Object parent;//父对象

    //更新最小更新间隔
    private void updateMinInterval(long period) {
        if (period < minInterval || minInterval == 0L)
            minInterval = period;
    }

    //初始化
    public PeriodicTimer(Object... args) {
        currentClocks = new HashMap<String, Long>();
        events = new HashMap<String, IGameEvent>();
        if (args.length > 0)
            parent = args[0];
        else
            parent = null;
        if (args.length > 1)
            minInterval = (long) args[1];
        else
            minInterval = 0L;
    }

    //重置事件时钟
    public void resetEventClock(String id, long clock) {
        if (currentClocks.containsKey(id))
            currentClocks.put(id, clock);
    }

    //更新
    public void update(long clock, Object... args) {
        if (clock - timerClock >= minInterval) {
            timerClock = clock;
            for (String key : events.keySet()) {
                IGameEvent event;
                long period;
                if (clock - currentClocks.get(key) >= (period = (long) (event = events.get(key)).getInfo())) {
                    currentClocks.put(key, clock);
                    updateMinInterval(period);
                    event.run(parent, args);
                }
            }
        }
    }

    //添加事件
    public PeriodicTimer addEvent(String id, IGameEvent event) {
        events.put(id, event);
        currentClocks.put(id, 0L);
        updateMinInterval((long) event.getInfo());
        return this;
    }

    //移除事件
    public PeriodicTimer removeEvent(String id) {
        events.remove(id);
        currentClocks.remove(id);
        minInterval = 0L;
        return this;
    }

}
