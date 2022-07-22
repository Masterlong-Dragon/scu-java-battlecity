package com.masterlong.game.objects;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.DrawManager;
import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.essentials.SingletonManager;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.util.HashMap;

/**
 * 坦克主类
 */
public abstract class Tank extends CollidableGameItem {

    public static final int maxCategory = 8;
    private int category;//坦克种类
    protected Direction direction;//坦克的行进方向
    private HashMap<Direction, Integer> drawDirectionTable;//用于创建逻辑方向和贴图方位的直接映射
    private int drawDirection;//贴图方向
    private AnimationPlayer[][] animationPlayers;
    protected int w, h;
    //protected CollisionManager collisionManager;
    protected int velocity;
    protected HashMap<String, Integer> collisionFilter;//碰撞过滤
    protected AnimationItem explosionItem;

    //初始化逻辑方向和贴图方位的映射关系
    //手动优化switch了属于是
    private void initDrawTable() {
        drawDirectionTable = new HashMap<Direction, Integer>();
        drawDirectionTable.put(Direction.UP, 0);
        drawDirectionTable.put(Direction.RIGHT, 1);
        drawDirectionTable.put(Direction.DOWN, 2);
        drawDirectionTable.put(Direction.LEFT, 3);
    }

    //构造方法1
    public Tank(Direction pos) {
        super(pos, Resource.getResourceHandlerInstance());
        category = 0;
        drawDirection = 0;
        direction = Direction.UP;//初始向上运动
        initDrawTable();
        layer = 2;
        aabb = new AABB(pos.x + 4, pos.y + 2, 26, 28);
        initAnimation();
    }

    //构造方法2
    public Tank(int x, int y) {
        super(x, y, Resource.getResourceHandlerInstance());
        category = 0;
        drawDirection = 0;
        direction = Direction.UP;//初始向上运动
        initDrawTable();
        layer = 2;
        aabb = new AABB(pos.x + 4, pos.y + 2, 26, 28);
        initAnimation();
    }

    //初始化贴图状态
    private void initAnimation() {
        animationPlayers = new AnimationPlayer[8][4];
        Animation[][] animations = (Animation[][]) resourceHandler.getResource("tankAnimations");
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 4; j++)
                animationPlayers[i][j] = new AnimationPlayer(animations[i][j]);
    }

    @Override
    public void bind(CollisionManager c) {
        //collisionManager = c;
    }

    //发射子弹
    public void fire() {
        //修正子弹发射中心
        Direction center = new Direction(pos.x + 12, pos.y + 17 - Math.abs(direction.x) * 5 - ((direction.y == -1) ? 8 : 0));
        Direction bulletPos = Direction.add(center, Direction.multiply(direction, 20));
        if (Math.abs(direction.x) == 1)
            bulletPos.addOn(Direction.multiply(direction, 2));
        else
            bulletPos.addOn(Direction.multiply(direction, -2));
        Bullet bullet = (Bullet) resourceHandler.getItemByName("bullets");
        bullet.setDirection(direction).setTag(tag + "Bullet");
        bullet.setPos(bulletPos);
        bullet.setLayer(3);
        scene.addItem(bullet);
    }

    //设置坦克种类
    public Tank setCategory(int category) {
        this.category = category;
        return this;
    }

    //获取坦克种类
    public int getCategory() {
        return category;
    }

    //设置坦克方向
    public Tank setDirection(Direction direction) {
        this.direction = direction;
        drawDirection = drawDirectionTable.get(direction);//同时获取贴图位置
        return this;
    }

    public Tank setVelocity(int velocity) {
        return this;
    }

    public int getVelocity() {
        return velocity;
    }

    //坦克移动
    public Tank move() {
        //向量运算
        pos.addOn(Direction.multiply(this.direction, velocity));
        return this;
    }

    public Tank destroyEffect() {
        if (explosionItem == null)
            explosionItem = (AnimationItem) resourceHandler.getItemByName("tankExplosions");
        explosionItem.setPos(new Direction(pos.x - 17, pos.y - 17));
        explosionItem.setLayer(2);
        scene.addItem(explosionItem);
        explosionItem = null;
        return this;
    }

    @Override
    public void init() {
        super.init();
        scene.attachComponent(this, "collision");
        velocity = 1;
        collisionFilter = new HashMap<String, Integer>();
        collisionFilter.put("_WALL_", 1);
        clearEvent(CollisionManager.ON_COLLISION);
        addEvent(CollisionManager.ON_COLLISION, SingletonManager.getEventRegister().getEvent("destroyByBullet"));
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        //animationPlayers[category][drawDirection].setPause(false);
        move();
    }

    @Override
    public GameItem destroy() {
        scene.detachComponent(this, "collision");
        return super.destroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyEffect();
    }

    @Override
    public void draw(Graphics g) {
        animationPlayers[category][drawDirection].draw(g, pos.x, pos.y);
    }

    //绑定绘制管理器，方便按需移除/加入自身
    @Override
    public void bind(DrawManager d) {
        super.bind(d);
        w = d.getW();
        h = d.getH();
    }

    @Override
    public void onCollision(ICollider item) {
        super.onCollision(item);
        if (isDestroyed && item instanceof Bullet) {
            System.out.println("hit! " + item.getAABB().x + " " + item.getAABB().y);
        }
        /*else if (item.getColliderTag().equals("_WALL_")) {
            //animationPlayers[category][drawDirection].setPause(true);
        }*/
    }

    @Override
    public AABB getAABB() {
        aabb.x = pos.x + 4;
        aabb.y = pos.y + 2;
        return aabb;
    }

    @Override
    public boolean isInitiative() {
        return true;
    }

    @Override
    public boolean isActive() {
        return !isDestroyed;
    }

    @Override
    public boolean isRigid() {
        return true;
    }

    @Override
    public HashMap<String, Integer> getFilter() {
        return collisionFilter;
    }

    @Override
    public void hang() {
        super.hang();
        animationPlayers[category][drawDirection].setPause(true);
        //scene.detachComponent(this, "collision");
    }

    @Override
    public void resume() {
        super.resume();
        animationPlayers[category][drawDirection].setPause(false);
        //scene.attachComponent(this, "collision");
    }
}
