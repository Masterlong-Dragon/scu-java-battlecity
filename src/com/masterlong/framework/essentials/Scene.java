package com.masterlong.framework.essentials;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏物体管理器
 * 现重用为游戏场景
 */
public class Scene {

    //逻辑宽高
    protected int width;
    protected int height;
    public DrawManager drawManager;
    public LogicManager logicManager;
    protected AliveMonitor aliveMonitor;
    //老生常谈
    //管理场景物体
    protected ConcurrentHashMap<Long, GameComponentSystem> components;
    protected ArrayList<HashMap<Long, IResDraw>> drawLayers;
    protected ConcurrentHashMap<Long, IUpdate> updateItems;//更新物体
    //前后优先级
    protected ConcurrentHashMap<Long, IUpdate>[] updateComponents;//更新组件

    protected GameView gameView;

    protected boolean paused;

    public Scene(GameView gameFrame) {
        this.gameView = gameFrame;
        aliveMonitor = new AliveMonitor();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void init(Object... args) {
        width = 800;
        height = 600;
        //获取两个重要管理器的单例
        logicManager = SingletonManager.getLogicManager();
        drawManager = SingletonManager.getDrawManager();
        components = new ConcurrentHashMap<Long, GameComponentSystem>();
        //初始化渲染层
        drawLayers = new ArrayList<HashMap<Long, IResDraw>>();
        for (int i = 0; i < DrawManager.maxLayerCount; i++)
            drawLayers.add(new HashMap<Long, IResDraw>());
        //逻辑
        updateItems = new ConcurrentHashMap<Long, IUpdate>();
        updateComponents = new ConcurrentHashMap[]{new ConcurrentHashMap<Long, IUpdate>(), new ConcurrentHashMap<Long, IUpdate>()};
        takeControl();
        registerEvents();
    }

    public void takeControl() {
        drawManager.setDrawLayers(drawLayers);
        logicManager.setUpdateItems(updateItems);
        logicManager.setUpdateComponents(updateComponents);
        if (aliveMonitor != null && findComponent("aliveMonitor") == null)
            addComponent(aliveMonitor);
    }

    public void clear() {
        components.clear();
        for (HashMap<Long, IResDraw> layer : drawLayers)
            layer.clear();
        drawLayers.clear();
        updateComponents[0].clear();
        updateComponents[1].clear();
        updateItems.clear();
        aliveMonitor.clear();
    }

    //增加物体
    public Scene addItem(GameItem item) {
        item.bind(this);
        logicManager.addItem(item);
        drawManager.addItem(item);
        return this;
    }

    //删除物体
    public Scene removeItem(GameItem item) {
        logicManager.removeItem(item);
        drawManager.removeItem(item);
        return this;
    }

    //寻找item，目前看来是用不到的(况且ID索引并不是很实用)
    public GameItem findItem(long id) {
        return (GameItem) (logicManager.findUpdateItem(id));
    }

    //添加物体到组件管理器
    public Scene attachComponent(Object item, String id) {
        GameComponentSystem component = findComponent(id);
        if (component != null) {
            //System.out.println("add " + component.getComponentID() + " to " + item.getTag());
            component.addItem(item);
        }
        return this;
    }

    //退之
    public Scene detachComponent(Object item, String id) {
        GameComponentSystem component = findComponent(id);
        if (component != null)
            component.removeItem(item);
        return this;
    }

    //寻之
    public GameComponentSystem findComponent(String id) {
        for (GameComponentSystem c : components.values()) {
            if (c.getComponentID().equals(id)) {
                return c;
            }
        }
        return null;
    }

    //添加组件管理器
    public Scene addComponent(GameComponentSystem c) {
        c.scene = this;
        System.out.println("init " + c.getComponentID());
        components.put(c.getLogicID(), c);
        logicManager.addItem(c);
        if (c instanceof IResDraw)
            drawManager.addItem((IResDraw) c);
        return this;
    }

    //移除组件管理器
    public Scene removeComponent(GameComponentSystem c) {
        components.remove(c.getLogicID());
        logicManager.removeItem(c);
        return this;
    }

    public JPanel getGameView() {
        return gameView;
    }

    public boolean isPaused() {
        return paused;
    }

    public Scene registerEvents() {
        return this;
    }
}