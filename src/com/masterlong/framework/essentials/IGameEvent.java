package com.masterlong.framework.essentials;

/**
 * 事件接口
 */
public interface IGameEvent {
    //返回值标记是否阅后即焚
    Object getInfo(Object... args);
    void run(Object... args);
}
