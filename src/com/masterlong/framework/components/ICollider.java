package com.masterlong.framework.components;

import java.util.HashMap;

/**
 * 碰撞物体接口
 */
public interface ICollider {
    void onCollision(ICollider item);
    void bind(CollisionManager c);
    AABB getAABB();
    long getColliderID();
    String getColliderTag();
    boolean isInitiative();
    boolean isActive();
    boolean isStatic();
    boolean isRigid();
    HashMap<String, Integer> getFilter();
}
