package com.masterlong.game.objects;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.DrawManager;
import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.util.HashMap;

/**
 * 子弹类
 */
public class Bullet extends GameItem implements ICollider {

    private Direction direction;
    private HashMap<String, Integer> collisionFilter;//碰撞过滤
    private AnimationPlayer explosion;
    private AnimationItem explosionItem;
    private int subMove;
    private final AABB aabb;
    private ICollider onCollision;
    //private CollisionManager collisionManager;

    public Bullet(Direction pos, Direction direction, String tag) {
        super(pos, Resource.getResourceHandlerInstance());
        this.direction = direction;
        layer = 3;
        this.tag = tag;
        initImgArea();
        aabb = new AABB(pos.x + 2, pos.y + 2, (float) imgArea.dx / 2, (float) imgArea.dy / 2);

    }

    public Bullet(int x, int y, Direction direction, String tag) {
        super(x, y, Resource.getResourceHandlerInstance());
        this.direction = direction;
        layer = 3;
        this.tag = tag;
        initImgArea();
        aabb = new AABB(pos.x + 2, pos.y + 2, (float) imgArea.dx / 2, (float) imgArea.dy / 2);

    }

    void initImgArea() {
        imgArea = (ImgArea) resourceHandler.getResource("bulletTexture");
        explosion = new AnimationPlayer((Animation) resourceHandler.getResource("bulletExplosion"));
        explosion.setCycled(false);
    }

    public Bullet setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public void init() {
        super.init();
        collisionFilter = new HashMap<String, Integer>();
        collisionFilter.put("_WALL_", 1);//只响应墙壁
        scene.attachComponent(this, "collision");//添加碰撞组件
        subMove = 0;
    }

    //移动和爆炸销毁反应
    @Override
    public void update(long clock) {
        super.update(clock);
        pos.addOn(Direction.multiply(direction, 1 + (subMove ^= 1)));
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
    }

    @Override
    public void bind(DrawManager d) {
        super.bind(d);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("bullet " + getID() + " destroyed.");
        if (!(onCollision instanceof Tank)) {
            String bPoolExp = "bulletExplosions";
            if (onCollision instanceof CollisionManager.Wall)
                bPoolExp = "bulletExplosions2";
            explosionItem = (AnimationItem) resourceHandler.getItemByName(bPoolExp);
            scene.addItem(explosionItem.setPos(new Direction(pos.x - 14, pos.y - 14)));
        }
    }

    @Override
    public void onCollision(ICollider item) {
        //只考虑出界
        onCollision = item;
        isDestroyed = true;
        scene.detachComponent(this, "collision");
        scene.removeItem(this);
    }

    @Override
    public void bind(CollisionManager c) {
        //不用处理
        //collisionManager = c;
    }

    @Override
    public AABB getAABB() {
        //修正了一哈包围盒
        aabb.x = pos.x + 2;
        aabb.y = pos.y + 2;
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
        return false;
    }

    @Override
    public boolean isActive() {
        return !isDestroyed;
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
        return collisionFilter;
    }
}
