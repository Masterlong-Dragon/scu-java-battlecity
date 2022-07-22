package com.masterlong.framework.essentials;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制使能状态
 */
public class AliveMonitor extends GameComponentSystem {

    private final ConcurrentHashMap<Long, IUpdate> hangOnUpdates;
    private final LogicManager logicManager;

    public AliveMonitor() {
        hangOnUpdates = new ConcurrentHashMap<Long, IUpdate>();
        logicManager = SingletonManager.getLogicManager();
        componentID = "aliveMonitor";
    }

    @Override
    public void init() {
        super.init();
        // 仅通过findComponent被其它组件调用
        addItem(this);
    }

    public AliveMonitor hangOnAllItems() {
        for (IUpdate update : logicManager.updateItems.values())
            if (update instanceof GameItem)
                addItem(update);
        return this;
    }

    public AliveMonitor resumeAllItems() {
        for (IUpdate update : hangOnUpdates.values())
            if (update instanceof GameItem)
                removeItem(update);
        return this;
    }

    public AliveMonitor resumeAll() {
        for (IUpdate update : hangOnUpdates.values())
            removeItem(update);
        return this;
    }

    public void clear() {
        hangOnUpdates.clear();
    }

    @Override
    public GameComponentSystem addItem(Object o, Object... args) {
        if (o instanceof IUpdate) {
            hangOnUpdates.put(((IUpdate) o).getLogicID(), (IUpdate) o);
            logicManager.removeItem((IUpdate) o, false);
            return this;
        }
        return null;
    }

    @Override
    public GameComponentSystem removeItem(Object o) {
        if (o instanceof IUpdate) {
            hangOnUpdates.remove(((IUpdate) o).getLogicID());
            logicManager.addItem((IUpdate) o, false);
            return this;
        }
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }
}
