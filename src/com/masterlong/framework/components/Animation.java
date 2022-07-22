package com.masterlong.framework.components;

import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;

import java.util.ArrayList;

/**
 * 一个简陋的帧动画类
 */
public class Animation {
    private final ResourceHandler resourceHandler;
    private final ArrayList<ImgArea> frames;//所有帧
    private final ArrayList<Integer> framePerTick;//每游戏帧对应动画帧
    private int totalTicks;//总帧数

    public Animation(ResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
        totalTicks = 0;
        frames = new ArrayList<ImgArea>();
        framePerTick = new ArrayList<Integer>();
    }

    //清空
    public Animation clear() {
        totalTicks = 0;
        frames.clear();
        framePerTick.clear();
        return this;
    }

    public Animation addFrame(int lastTicks, ImgArea area) {
        //生成对应的数组标记
        //占用空间++++ 但是我喜欢，因为时间O(1)且没有逻辑运算
        int frameCnt = frames.size();
        for (int p = framePerTick.size(), rend = totalTicks + lastTicks; p < rend; p++) {
            framePerTick.add(frameCnt);
        }
        totalTicks += lastTicks;
        frames.add(area);
        return this;
    }

    public Animation init(int[] lastTicks, ImgArea... areas) {
        //同理
        frames.clear();
        if (areas.length > 0 && lastTicks.length > 0) {
            int totalLength = Math.min(areas.length, lastTicks.length);
            for (int i = 0; i < totalLength; i++) {
                for (int j = totalTicks, rend = totalTicks + lastTicks[i]; j < rend; j++)
                    framePerTick.add(i);
                totalTicks += lastTicks[i];
                frames.add(areas[i]);
            }
        } else System.out.println("wrong args.");
        return this;
    }


    public Animation init(int[] lastTicks, ArrayList<ImgArea> areas) {
        //同理
        frames.clear();
        if (areas.size() > 0 && lastTicks.length > 0) {
            int totalLength = Math.min(areas.size(), lastTicks.length);
            for (int i = 0; i < totalLength; i++) {
                for (int j = totalTicks, rend = totalTicks + lastTicks[i]; j < rend; j++)
                    framePerTick.add(i);
                totalTicks += lastTicks[i];
                frames.add(areas.get(i));
            }
        } else System.out.println("wrong args.");
        return this;
    }

    //获取指定帧
    public ImgArea getFrame(int tick) {
        return frames.get(framePerTick.get(tick));
    }

    //获取指定帧
    public ImgArea getFrameAt(int index) {
        return frames.get(index);
    }

    //总时间帧数
    public int getTotalTicks() {
        return totalTicks;
    }

    //获取资源handler
    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }
}
