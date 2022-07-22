package com.masterlong.framework.essentials;

import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 抽象的游戏物体
 * 抄的unity GameObject(确信
 */
public abstract class GameItem implements IResDraw, IUpdate {

    public static long ON_INIT = EventRegister.getEventID(), ON_DESTROY = EventRegister.getEventID();

    protected HashMap<Long, ArrayList<IGameEvent>> events;
    protected Direction pos;//位置
    protected Direction lastPos;//下一帧的位置
    protected ResourceHandler resourceHandler;//资源持有管理器
    protected ImgArea imgArea;
    protected DrawManager drawManager;//绘制管理器
    protected LogicManager logicManager;
    protected Scene scene;
    protected boolean isInitDrawManager;
    protected final long ID;
    protected String poolName;
    protected int layer;
    protected boolean drawn;//是否加入绘制序列

    protected boolean isDestroyed;
    private static long idCounter = 0;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    protected String tag;

    public static long generateID() {
        return idCounter++;
    }

    public GameItem(Direction pos, ResourceHandler resourceHandler) {
        this.pos = pos;
        this.resourceHandler = resourceHandler;
        ID = generateID();
        isDestroyed = false;
        tag = "";
    }

    public GameItem(int x, int y, ResourceHandler resourceHandler) {
        pos = new Direction(x, y);
        this.resourceHandler = resourceHandler;
        ID = generateID();
        isDestroyed = false;
        tag = "";
    }

    public long getID() {
        return ID;
    }

    /*public void destroy() {
        isDestroyed = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }*/

    public Direction getLastPos() {
        return lastPos;
    }

    public Direction getPos() {
        return pos;
    }

    public GameItem setPos(Direction pos) {
        this.pos.set(pos);
        return this;
    }


    public Direction getDirection() {
        return Direction.sub(pos, lastPos);
    }

    public void bind(Scene scene) {
        this.scene = scene;
    }

    public GameItem addEvent(long eventType, IGameEvent event) {
        if (events == null)
            events = new HashMap<Long, ArrayList<IGameEvent>>();
        if (!events.containsKey(eventType))
            events.put(eventType, new ArrayList<IGameEvent>());
        events.get(eventType).add(event);
        return this;
    }

    public GameItem removeEvent(long eventType, IGameEvent event) throws NullPointerException {
        if (events == null)
            throw new NullPointerException("events is null");
        if (!events.containsKey(eventType))
            throw new NullPointerException("eventType does not exist");
        events.get(eventType).remove(event);
        return this;
    }

    public GameItem clearEvent(long eventType) {
        if (events == null)
            return this;
        if (!events.containsKey(eventType))
            return this;
        events.get(eventType).clear();
        return this;
    }

    public GameItem destroy() {
        isDestroyed = true;
        scene.removeItem(this);
        return this;
    }

    @Override
    public void draw(Graphics g) {
        resourceHandler.draw(g, pos.x, pos.y, imgArea);
    }

    @Override
    public void bind(DrawManager d) {
        drawManager = d;
        isInitDrawManager = drawn = true;
    }

    @Override
    public long getDrawID() {
        return ID;
    }

    @Override
    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public void setDrawn(boolean isDrawn) {
        if (isInitDrawManager)
            if (drawn != isDrawn) {
                if (!drawn)
                    drawManager.addItem(this);
                else
                    drawManager.removeItem(this);
                drawn = isDrawn;
            }
    }

    @Override
    public boolean isDrawn() {
        return drawn;
    }

    @Override
    public void update(long clock) {
        //lastPos.set(pos);
        lastPos.x = pos.x;
        lastPos.y = pos.y;
    }

    @Override
    public void init() {
        lastPos = new Direction(pos.x, pos.y);
        isDestroyed = false;
        callEvents(ON_INIT, this);
    }

    protected void callEvents(long eventType, Object... args) {
        if (events != null && events.containsKey(eventType)) {
            Iterator<IGameEvent> iterator = events.get(eventType).iterator();
            while (iterator.hasNext()) {
                IGameEvent event = iterator.next();
                event.run(args);
                if ((boolean) event.getInfo())
                    iterator.remove();
            }
        }
    }

    @Override
    public void bind(LogicManager l) {
        logicManager = l;
    }

    @Override
    public long getLogicID() {
        return ID;
    }

    @Override
    public void onDestroy() {
        isDestroyed = true;
        callEvents(ON_DESTROY, this);
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

    @Override
    public String toString() {
        return "GameItem tag:" + tag;
    }

    public String getPoolName() {
        return poolName;
    }

    public GameItem setPoolName(String poolName) {
        this.poolName = poolName;
        return this;
    }

}
