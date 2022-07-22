package com.masterlong.framework.components;

import com.masterlong.framework.math.Direction;

import java.util.Objects;

/**
 * aabb盒子
 */
public class AABB {
    public float x, y, width, height;

    public AABB(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Direction getCenter() {
        return new Direction((int) (x + width / 2), (int) (y + height / 2));
    }

    //容纳
    public boolean contains(AABB r) {
        return this.width > 0 && this.height > 0 && r.width > 0 && r.height > 0
                && r.x >= this.x && r.x + r.width <= this.x + this.width
                && r.y >= this.y && r.y + r.height <= this.y + this.height;
    }

    //相交
    //注意排除相切的情况
    public boolean overlaps(AABB r) {
        if (this.width > 0 && this.height > 0 && r.width > 0 && r.height > 0) {
            if (this.x >= r.x + r.width || r.x >= this.x + this.width) return false;
            return !(this.y >= r.y + r.height) && !(r.y >= this.y + this.height);
        } else return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AABB aabb = (AABB) o;
        return Float.compare(aabb.x, x) == 0 && Float.compare(aabb.y, y) == 0 && Float.compare(aabb.width, width) == 0 && Float.compare(aabb.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y + " w: " + width + " h: " + height;
    }
}
