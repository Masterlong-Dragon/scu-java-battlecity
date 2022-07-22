package com.masterlong.game.objects;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.essentials.IGameEvent;
import com.masterlong.framework.math.Direction;

import java.awt.event.KeyEvent;
import java.util.*;

/**
 * 玩家坦克
 * 增加键盘控制器
 */
public class PlayerTank extends Tank implements IInput {

    private long coolDownInterval;
    private CollisionManager collisionManager;
    //按键输入相关
    private int lastKey = KeyEvent.VK_UP;
    public int currentKey = lastKey;
    public boolean activated;
    private List<Direction> directions;
    private List<Integer> defaultKeys;
    private HashMap<Integer, Direction> tankDirectionTable;//键盘方位对照表
    private Direction direction = Direction.UP;
    private PeriodicTimer periodicTimer;
    private IGameEvent tankReborn;

    public PlayerTank(Direction pos) {
        super(pos);
        tag = "player";
    }

    //设置自定义键位
    void setKeyCollections(List<Integer> keys) {
        //清除原先储存的键位
        tankDirectionTable.clear();
        Iterator<Integer> keyIter = keys.iterator();
        for (Direction dir : directions) {
            if (keyIter.hasNext())
                tankDirectionTable.put(keyIter.next(), dir);
            else return;
        }

    }

    public PlayerTank(int x, int y) {
        super(x, y);
        tag = "player";
    }

    @Override
    public void init() {
        super.init();
        coolDownInterval = 500L;
        //手动优化switch了属于是
        tankDirectionTable = new HashMap<Integer, Direction>();
        directions = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
        defaultKeys = Arrays.asList(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
        //初始化设置自定义键位
        setKeyCollections(defaultKeys);
        activated = false;
        collisionFilter.put("enemyBullet", 1);
        collisionFilter.put("enemy", 1);
        scene.attachComponent(this, "input");
        periodicTimer = new PeriodicTimer();
        //射击冷却时间
        periodicTimer.addEvent("fire", new IGameEvent() {
            @Override
            public void run(Object... args) {
                fire();
            }

            @Override
            public Object getInfo(Object... args) {
                return coolDownInterval;
            }
        });
        tankReborn = new IGameEvent() {
            @Override
            public void run(Object... args) {
                ((StatusMonitor) (scene.findComponent("statusMonitor"))).playerTankRebirth();
                PlayerTank.this.isDestroyed = false;
                currentKey = lastKey = KeyEvent.VK_UP;
            }

            @Override
            public Object getInfo(Object... args) {
                return true;
            }
        };
    }

    @Override
    public void update(long clock) {
        super.update(clock);
    }

    @Override
    public void bind(CollisionManager c) {
        collisionManager = c;
    }

    //优化 只是将坦克移除出渲染和更新队列
    @Override
    public GameItem destroy() {
        scene.detachComponent(this, "input");
        scene.detachComponent(this, "collision");
        //移除渲染和更新队列
        setDrawn(false);
        scene.findComponent("aliveMonitor").addItem(this);
        explosionItem = (AnimationItem) resourceHandler.getItemByName("tankExplosions")
                .addEvent(GameItem.ON_DESTROY, tankReborn);
        destroyEffect();
        return this;
    }

    @Override
    public void keyPressed(KeyEvent key) {
        //减少一直按下的逻辑判断
        currentKey = key.getKeyCode();
        if (currentKey != lastKey)
            if ((direction = tankDirectionTable.get(currentKey)) != null) {
                setDirection(direction);//设置方向
                lastKey = currentKey;
                return;
            }
        //很不舒服，Java没有c/cpp的函数指针，也没有c#的委托，实现不了我喜欢的奇技淫巧，除非用反射
        //本来可以一步到位所有按键映射到对应方法上的，确信
        switch (currentKey) {
            case KeyEvent.VK_Q:
                setCategory((getCategory() + 1) & (Tank.maxCategory - 1));//取余2次幂的优化形式
                break;
            case KeyEvent.VK_I:
                setDrawn(!isDrawn());
                break;
            case KeyEvent.VK_SPACE: {
                periodicTimer.update(System.currentTimeMillis());
            }
            break;
        }
    }

    @Override
    public long getInputID() {
        return ID;
    }

    @Override
    public void bind(InputManager i) {

    }

    @Override
    public void hang() {
        super.hang();
        scene.detachComponent(this, "input");
    }

    @Override
    public void resume() {
        super.resume();
        scene.attachComponent(this, "input");
    }
}
