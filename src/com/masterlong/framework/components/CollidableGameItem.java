package com.masterlong.framework.components;

import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.essentials.IGameEvent;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;

import java.util.HashMap;

public abstract class CollidableGameItem extends GameItem implements ICollider {

    protected AABB aabb;

    public CollidableGameItem(Direction pos, ResourceHandler resourceHandler) {
        super(pos, resourceHandler);
    }

    public CollidableGameItem(int x, int y, ResourceHandler resourceHandler) {
        super(x, y, resourceHandler);
    }

    @Override
    public void onCollision(ICollider item) {
        if (events != null && events.containsKey(CollisionManager.ON_COLLISION))
            for (IGameEvent event : events.get(CollisionManager.ON_COLLISION)) {
                event.run(item, this);
            }
    }

    @Override
    public void bind(CollisionManager c) {

    }

    @Override
    public AABB getAABB() {
        aabb.x = pos.x;
        aabb.y = pos.y;
        return aabb;
    }

    @Override
    public long getColliderID() {
        return ID;
    }

    @Override
    public String getColliderTag() {
        return tag;
    }

    @Override
    public boolean isInitiative() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isRigid() {
        return false;
    }

    @Override
    public HashMap<String, Integer> getFilter() {
        return null;
    }
}
