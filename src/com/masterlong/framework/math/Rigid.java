package com.masterlong.framework.math;

import com.masterlong.framework.components.AABB;
import com.masterlong.framework.components.CollisionManager;
import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.components.ICollider;

import java.util.HashMap;

/**
 * 刚体碰撞模拟
 */
public class Rigid {

    private static void reArrangeItems(GameItem collideObj, ICollider collider) {
        Direction moveDirection = collideObj.getDirection();//获得当前碰撞物的移动方向
        AABB collideR = ((ICollider) (collideObj)).getAABB();
        AABB collideOnR = collider.getAABB();
        int dx = 0, dy = 0;
        //对方退后
        if (moveDirection.x > 0)
            //x + w = sx
            dx = (int) (collideOnR.x - collideR.width - collideR.x);
        else if (moveDirection.x < 0)
            //x = sx + sw
            dx = (int) (collideOnR.x + collideOnR.width - collideR.x);
        if (moveDirection.y > 0)
            //y + h = sy
            dy = (int) (collideOnR.y - collideR.height - collideR.y);
        else if (moveDirection.y < 0)
            //y = sy + sh
            dy = (int) (collideOnR.y + collideOnR.height - collideR.y);
        collideObj.setPos(Direction.add(collideObj.getLastPos(), new Direction(dx + moveDirection.x, dy + moveDirection.y)));
    }

    public static void staticBounceBack(ICollider item, ICollider collider, boolean callBack) {
        //一个比较粗糙的只适合于动碰静被阻拦的算法
        GameItem obj = (GameItem) item;
        /*Direction moveDirection = obj.getDirection();//获得当前碰撞物的移动方向
        int dx = 0, dy = 0;
        Direction lastPos = obj.getLastPos();
        AABB r = item.getAABB();
        AABB onCollider = collider.getAABB();*/
        reArrangeItems(obj, collider);
        //如果要通知碰撞物
        if (callBack) {
            //根据filter判断到底要不要通知
            HashMap<String, Integer> filter = item.getFilter();
            if (filter == null)
                item.onCollision(collider);
            else {
                Integer fMode = filter.get(CollisionManager.filterMode);
                Integer filterVal = filter.get(collider.getColliderTag());
                fMode = fMode == null ? 0 : fMode;
                switch (fMode) {
                    case 0:
                        if (filterVal != null && filterVal == 1)
                            item.onCollision(collider);
                        break;
                    case 1:
                        if (filterVal == null || filterVal != 1)
                            item.onCollision(collider);
                        break;
                }
            }
        }
    }


    //默认二者必须都为GameItem
    public static void dynamicBounceBack(ICollider collide, ICollider collideOn) {
        GameItem collideObj = (GameItem) collide;//碰撞物体
        GameItem collideOnObj = (GameItem) collideOn;//自身
        AABB collideR = collide.getAABB();
        AABB collideOnR = collideOn.getAABB();
        Direction self = collideOnObj.getDirection();
        Direction opposite = collideObj.getDirection();
        boolean reSelf = false, reOpposite = false;
        //各退一步
        //考虑谁的原本运行方向下不会发生碰撞，谁就正常运行
        //否则大家就停住
        //先测试对方
        collideOnR.x -= self.x;
        collideOnR.y -= self.y;
        //仍然碰撞
        reSelf = collideOnR.overlaps(collideR);
        //恢复
        collideOnR.x += self.x;
        collideOnR.y += self.y;
        //再测试自己
        collideR.x -= opposite.x;
        collideR.y -= opposite.y;
        //仍然碰撞
        reOpposite = (collideR.overlaps(collideOnR));
        if (reSelf && reOpposite) {
            reArrangeItems(collideOnObj, collide);
        } else {
            if (reSelf) {
                //自己停住
                reArrangeItems(collideOnObj, collide);
                if (reOpposite) {
                    //对方停住
                    reArrangeItems(collideObj, collideOn);
                }
            }

        }
    }
}
