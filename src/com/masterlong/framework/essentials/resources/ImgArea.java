package com.masterlong.framework.essentials.resources;

/**
 * 贴图矩形
 */
public class ImgArea {
    public int x, y, dx, dy, sx, sy;

    public ImgArea(int x, int y, int dx, int dy, int sx, int sy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.sx = sx;
        this.sy = sy;
    }

    public ImgArea() {
    }


    public void set(int x, int y, int sx, int sy) {
        this.x = x;
        this.y = y;
        this.sx = sx;
        this.sy = sy;
    }

    public void set(int x, int y, int dx, int dy, int sx, int sy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.sx = sx;
        this.sy = sy;
    }
}
