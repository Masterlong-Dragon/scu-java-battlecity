package com.masterlong.framework.essentials;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;


/**
 * 主逻辑循环管理
 * 物体&组件管理
 */
public class LogicManager {
    private GameView gameView;
    private SceneManager sceneManager;
    protected ConcurrentHashMap<Long, IUpdate> updateItems;//更新物体
    //前后优先级
    protected ConcurrentHashMap<Long, IUpdate>[] updateComponents;//更新组件
    private Lock writeLock;//写锁

    protected LogicManager(GameView gameView, Lock writeLock, SceneManager sceneManager) {
        init(gameView, writeLock, sceneManager);
    }

    protected LogicManager() {

    }

    public void init(GameView gameView, Lock writeLock, SceneManager sceneManager) {
        this.gameView = gameView;
        this.sceneManager = sceneManager;
        this.writeLock = writeLock;
    }

    public void setActiveScene(String id, boolean clear, boolean init) {
        sceneManager.setActiveScene(id, clear, init);
    }

    public LogicManager addItem(IUpdate updateItem, Object... args) {
        if (updateItem instanceof GameComponentSystem)
            updateComponents[((GameComponentSystem) updateItem).priority()].put(updateItem.getLogicID(), updateItem);//添加
        else
            updateItems.put(updateItem.getLogicID(), updateItem);//添加
        updateItem.bind(this);//绑定
        if (args.length == 0)
            updateItem.init();
        else if (args[0] instanceof Boolean && !(Boolean) args[0])
            updateItem.resume();
        return this;
    }

    public LogicManager removeItem(IUpdate updateItem, Object... args) {
        ConcurrentHashMap<Long, IUpdate> table = (updateItem instanceof GameComponentSystem) ? updateComponents[((GameComponentSystem) updateItem).priority()] : updateItems;
        if (table.get(updateItem.getLogicID()) != null) {
            table.remove(updateItem.getLogicID());
            if (args.length == 0)
                updateItem.onDestroy();
            else if (args[0] instanceof Boolean && !(Boolean) args[0])
                updateItem.hang();
        }
        return this;
    }

    //全部清除
    //已废弃 此部分交由场景交换时处理
    public void clear() {
        /*updateItems.clear();
        updateComponents[0].clear();
        updateComponents[1].clear();*/
    }

    //获取
    public IUpdate findUpdateItem(long id) {
        IUpdate res = updateComponents[0].get(id);
        if (res == null)
            res = updateComponents[1].get(id);
        return res;
    }

    void update(long clock) {
        //分别执行
        for (IUpdate updateComponent : updateComponents[0].values()) {
            //if (!updateComponent.isDestroyed())
            writeLock.lock();
            updateComponent.update(clock);
            writeLock.unlock();
        }
        writeLock.lock();
        for (IUpdate updateItem : updateItems.values())
            //if (!updateItem.isDestroyed())
            updateItem.update(clock);
        for (IUpdate updateComponent : updateComponents[1].values()) {
            //if (!updateComponent.isDestroyed())
            updateComponent.update(clock);
        }
        writeLock.unlock();
        for (ConcurrentHashMap updates : updateComponents)
            for (Object updateComponent : updates.values())
                ((GameComponentSystem) updateComponent).doRemoveList();
        /*if (sceneSwitch) {
            sceneSwitch = false;
            sceneManager.setActiveScene((String) switchArgs[0], (Boolean) switchArgs[1], (Boolean) switchArgs[2]);
        }*/
    }

    public void setUpdateItems(ConcurrentHashMap<Long, IUpdate> updateItems) {
        this.updateItems = updateItems;
    }

    public void setUpdateComponents(ConcurrentHashMap<Long, IUpdate>[] updateComponents) {
        this.updateComponents = updateComponents;
    }
}
