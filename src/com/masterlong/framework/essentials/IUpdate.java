package com.masterlong.framework.essentials;

/**
 * 更新逻辑接口
 */
public interface IUpdate {
    void update(long clock);
    void init();
    void hang();
    void resume();
    void bind(LogicManager l);
    long getLogicID();
    void onDestroy();
    boolean isDestroyed();
}
