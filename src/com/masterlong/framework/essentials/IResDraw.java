package com.masterlong.framework.essentials;

import java.awt.*;

/**
 * 可绘制物体的接口
 */
public interface IResDraw {
    void draw(Graphics g);
    void bind(DrawManager d);
    long getDrawID();
    void setLayer(int layer);
    int getLayer();
    void setDrawn(boolean isDrawn);
    boolean isDrawn();
}
