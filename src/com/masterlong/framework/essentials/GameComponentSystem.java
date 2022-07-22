package com.masterlong.framework.essentials;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 抽象的游戏组件
 * 需要由update托管更新
 */
public abstract class GameComponentSystem implements IUpdate {

    protected boolean isDestroyed;
    protected LogicManager logicManager;
    protected long ID;
    protected String componentID;
    protected boolean toRemove;
    protected Queue<Object> removeList;
    protected Scene scene;

    public GameComponentSystem() {
        ID = GameItem.generateID();
        isDestroyed = false;
        toRemove = false;
        removeList = new ConcurrentLinkedQueue<Object>();
    }

    public String getComponentID() {
        return componentID;
    }

    public GameComponentSystem addItem(Object o, Object... args) {
        return this;
    }

    public Object findItem(long id, Object... args) {
        return null;
    }

    //移除物体
    public GameComponentSystem removeItem(Object o) {
        toRemove = true;
        removeList.offer(o);
        return this;
    }

    public void clear(){
        toRemove = false;
        removeList.clear();
    }

    public void doRemoveList() {
    }

    public int priority(){
        return 1;
    }

    @Override
    public void update(long clock) {

    }

    @Override
    public void init() {

    }

    @Override
    public void bind(LogicManager l) {

    }

    @Override
    public long getLogicID() {
        return ID;
    }

    @Override
    public void onDestroy() {
        isDestroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void hang() {

    }

    @Override
    public void resume() {

    }
}
