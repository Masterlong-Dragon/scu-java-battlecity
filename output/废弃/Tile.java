package com.masterlong.game.objects.mapsys;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.EventRegister;
import com.masterlong.framework.essentials.IGameEvent;
import com.masterlong.framework.essentials.SingletonManager;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.util.HashMap;

/**
 * tileMap的基本单位，tile
 */
public class Tile extends CollidableGameItem {

    // 图块种类
    public enum TileType {
        BRICK,
        IRON,
        GRASS,
        WATER,
        HOME
    }

    protected TileType type;
    protected int width;
    protected String tileName;
    protected boolean collidable;
    protected AnimationPlayer animationPlayer;

    // 事件
    public static long ON_TILE_DRAW = EventRegister.getEventID();

    //根据传入类型初始化
    private void initByType() {
        events = new HashMap<>();
        EventRegister eventRegister = SingletonManager.getEventRegister();
        switch (type) {
            case BRICK:
                tileName = "brick";
                tag = "_WALL_";
                imgArea = (ImgArea) resourceHandler.getResource("brickImg");
                collidable = true;
                addEvent(CollisionManager.ON_COLLISION, eventRegister.getEvent("staticBounceBack"))
                        .addEvent(CollisionManager.ON_COLLISION, eventRegister.getEvent("destroyByBullet"));
                break;
            case IRON:
                tileName = "iron";
                tag = "_WALL_";
                imgArea = (ImgArea) resourceHandler.getResource("ironImg");
                collidable = true;
                addEvent(CollisionManager.ON_COLLISION, eventRegister.getEvent("staticBounceBack"));
                break;
            case GRASS:
                tileName = "grass";
                imgArea = (ImgArea) resourceHandler.getResource("grassImg");
                collidable = false;
                layer = 4;
                break;
            case WATER:
                tileName = "water";
                Animation animation = (Animation) resourceHandler.getResource("waterAnimation");
                animationPlayer = new AnimationPlayer(animation);
                animationPlayer.setCycled(true);
                addEvent(ON_TILE_DRAW, eventRegister.getEvent("animationPlay"));
                /*addEvent(ON_COLLISION, new IItemEvent() {
                    @Override
                    public void run(Object... args) {
                        if(args[0] instanceof Tank) {
                            Tank tank = (Tank) args[0];
                            tank.destroy();
                        }
                    }
                });*/
                imgArea = new ImgArea(0, 0, 0, 0, 0, 0);
                width = animation.getFrame(0).dx;
                collidable = true;
                break;
        }
        if (width == 0)
            width = imgArea.dx;
        aabb = new AABB(pos.x, pos.y, width, width);
    }

    public int getWidth() {
        return width;
    }

    public Tile(TileType tileType, Direction pos) {
        super(pos, Resource.getResourceHandlerInstance());
        type = tileType;
        initByType();
    }

    public Tile(TileType tileType, int x, int y) {
        super(x, y, Resource.getResourceHandlerInstance());
        type = tileType;
        initByType();
    }

    @Override
    public void hang() {
        super.hang();
        if (animationPlayer != null)
            animationPlayer.setPause(true);
    }

    @Override
    public void resume() {
        super.resume();
        if (animationPlayer != null)
            animationPlayer.setPause(false);
    }

    @Override
    public void init() {
        super.init();
        if (collidable)
            scene.attachComponent(this, "collision");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (collidable)
            scene.detachComponent(this, "collision");
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (events.containsKey(ON_TILE_DRAW))
            for (IGameEvent event : events.get(ON_TILE_DRAW)) {
                event.run(animationPlayer, g, pos.x, pos.y);
            }
    }


    @Override
    public boolean isStatic() {
        return true;
    }
}
