package com.masterlong.framework.components;

import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;

import java.awt.*;

/**
 * 动画播放器
 * 本来想实现一个FSM的，但是太麻烦了
 * 可以完成播放与暂停功能
 */
public class AnimationPlayer {
    protected Animation animation;//动画
    private boolean animationPaused;//是否暂停
    private int currentTotalTickCnt;//当前总帧数
    private int currentTick;//当前帧数
    private ResourceHandler resourceHandler;//绘制
    private boolean isCycled;//是否循环

    public AnimationPlayer(Animation animation) {
        setAnimation(animation);
    }

    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    public void init() {
        setAnimation(animation);
    }

    public AnimationPlayer setAnimation(Animation animation) {
        this.animation = animation;
        animationPaused = false;
        currentTotalTickCnt = animation.getTotalTicks();
        currentTick = 0;
        resourceHandler = animation.getResourceHandler();
        isCycled = true;
        return this;
    }

    public boolean isCycled() {
        return isCycled;
    }

    public AnimationPlayer setCycled(boolean cycled) {
        isCycled = cycled;
        return this;
    }

    public AnimationPlayer resetFrame() {
        currentTick = 0;
        return this;
    }

    public AnimationPlayer setPause(boolean paused) {
        animationPaused = paused;
        return this;
    }

    //绘制
    public void draw(Graphics g, int x, int y) {
        resourceHandler.draw(g, x, y, updateFrame());
    }

    public ImgArea updateFrame() {
        ImgArea frame = animation.getFrame(currentTick);
        if (!animationPaused) {
            currentTick++;
            if (isCycled)
                currentTick %= currentTotalTickCnt;
                //考虑非周期动画暂停到最后一帧
            else if (currentTick == currentTotalTickCnt) {
                currentTick--;
                setPause(true);
            }
        }
        return frame;
    }

    //绘制
    public void draw(Graphics g, int x, int y, float zx, float zy) {
        ImgArea frame = animation.getFrame(currentTick);
        resourceHandler.draw(g, x, y, frame, zx, zy);
        if (!animationPaused) {
            currentTick++;
            if (isCycled)
                currentTick %= currentTotalTickCnt;
                //考虑非周期动画暂停到最后一帧
            else if (currentTick == currentTotalTickCnt) {
                currentTick--;
                setPause(true);
            }
        }

    }

    //是否停留到最后一帧
    public boolean isOver() {
        return !isCycled && animationPaused && currentTick == currentTotalTickCnt - 1;
    }
}
