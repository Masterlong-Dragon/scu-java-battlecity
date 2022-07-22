package com.masterlong.game.objects;

import com.masterlong.framework.components.ICollider;
import com.masterlong.framework.components.PeriodicTimer;
import com.masterlong.framework.essentials.EventRegister;
import com.masterlong.framework.essentials.SingletonManager;
import com.masterlong.framework.math.Direction;

import java.util.*;

/**
 * 精灵坦克
 */
public class SpriteTank extends Tank {

    private PeriodicTimer periodicTimer;
    private boolean bounced;
    private Random rand;

    public SpriteTank(Direction pos) {
        //初始化操纵
        super(pos);
        setDirection(Direction.DOWN);
        setCategory(4);
        tag = "enemy";
    }

    public SpriteTank(int x, int y) {
        //同上
        super(x, y);
        setDirection(Direction.DOWN);
        setCategory(4);
        tag = "enemy";
    }

    public void init() {
        super.init();
        bounced = false;
        rand = SingletonManager.getRandom();
        setDirection(Direction.AXES[rand.nextInt(4)]);
        collisionFilter.put("playerBullet", 1);
        collisionFilter.put("_WALL_", 1);
        collisionFilter.put("enemy", 1);
        periodicTimer = new PeriodicTimer(this);
        //开火和转弯的定时事件
        EventRegister eventRegister = SingletonManager.getEventRegister();
        periodicTimer.addEvent("fire", eventRegister.getEvent("periodicFire"))
                .addEvent("turn", eventRegister.getEvent("periodicTurn"));
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        periodicTimer.update(clock);
    }

    @Override
    public void onCollision(ICollider item) {
        super.onCollision(item);
        if (item.getColliderTag().equals("_WALL_")) {
            setDirection(direction.minus());
            bounced = true;
        }
    }

    public boolean isBounced() {
        return bounced;
    }

    public SpriteTank setBounced(boolean bounced) {
        this.bounced = bounced;
        return this;
    }
}
