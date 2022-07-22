package com.masterlong.framework.components;

import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.math.Direction;

import java.awt.*;

/**
 * 一个用于生成只绘制动画的游戏项
 * 代替了之前游戏物体自行托管动画的方式
 */
public class AnimationItem extends GameItem {

    protected AnimationPlayer animationPlayer;
    protected int cycleCnt, cycleCntMax;
    protected boolean isCycleTimeLimited;

    public AnimationItem(Direction pos, AnimationPlayer animationPlayer) {
        super(pos, animationPlayer.getResourceHandler());
        layer = 3;
        this.animationPlayer = animationPlayer;
        cycleCnt = 0;
        cycleCntMax = 1;
        isCycleTimeLimited = false;
    }

    @Override
    public void init() {
        lastPos = new Direction(pos.x, pos.y);
        isDestroyed = false;
        callEvents(ON_INIT, this, animationPlayer, animationPlayer.animation);
        cycleCnt = 0;
    }

    public boolean isCycleTimeLimited() {
        return isCycleTimeLimited;
    }

    public AnimationItem setCycleTimeLimited(boolean cycleTimeLimited) {
        isCycleTimeLimited = cycleTimeLimited;
        return this;
    }

    public AnimationItem setCycleCnt(int cycleCntMax) {
        this.cycleCntMax = cycleCntMax;
        return this;
    }

    public AnimationItem(int x, int y, AnimationPlayer animationPlayer) {
        super(x, y, null);
        layer = 3;
        this.animationPlayer = animationPlayer;
    }

    @Override
    public void draw(Graphics g) {
        animationPlayer.draw(g, pos.x, pos.y);
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        if (animationPlayer.isOver()) {
            if (isCycleTimeLimited && cycleCnt++ < cycleCntMax) {
                animationPlayer.resetFrame();
                animationPlayer.setPause(false);
            } else
                scene.removeItem(this);
        }
    }

    @Override
    public void hang() {
        super.hang();
        animationPlayer.setPause(true);
    }

    @Override
    public void resume() {
        super.resume();
        animationPlayer.setPause(false);
    }
}
