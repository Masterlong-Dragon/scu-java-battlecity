package com.masterlong.framework.math;

import java.util.Objects;

/**
 * 一个朴实无华的向量类
 * 没什么用，甚至很多余
 * 但我就是要写，感觉很高级
 */
public class Direction {
    //正交的俩方向基底
    public int x, y;

    //基础方向向量
    public static final Direction UP = new Direction(0, -1);
    public static final Direction DOWN = new Direction(0, 1);
    public static final Direction LEFT = new Direction(-1, 0);
    public static final Direction RIGHT = new Direction(1, 0);
    public static final Direction ZERO = new Direction(0, 0);
    public static final Direction[] AXES = {UP, DOWN, LEFT, RIGHT, ZERO};

    //重写哈希判断
    @Override
    public boolean equals(Object o) {
        return o == this || ((o instanceof Direction) && (((Direction) o).x == x && ((Direction) o).y == y));
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    Direction() {
        x = 0;
        y = 0;
    }

    public Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //向量加
    public static Direction add(Direction d1, Direction d2) {
        return new Direction(d1.x + d2.x, d1.y + d2.y);
    }

    //向量减
    public static Direction sub(Direction d1, Direction d2) {
        return new Direction(d1.x - d2.x, d1.y - d2.y);
    }

    //向量数乘
    public static Direction multiply(Direction d, int t) {
        return new Direction(d.x * t, d.y * t);
    }

    //向量点乘
    public static int dot(Direction d1, Direction d2){
        return d1.x * d2.x + d1.y * d2.y;
    }

    //加上
    public void addOn(Direction d) {
        x += d.x;
        y += d.y;
    }

    //减去
    public void minusOn(Direction d) {
        x -= d.x;
        y -= d.y;
    }

    //乘上
    public void multiplyOn(int t) {
        x *= t;
        y *= t;
    }

    //取反
    public Direction minus() {
        return new Direction(-x, -y);
    }

    //设值
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Direction d) {
        set(d.x, d.y);
    }

    @Override
    public String toString() {
        return "Direction{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

