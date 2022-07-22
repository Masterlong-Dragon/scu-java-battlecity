package com.masterlong.game.objects.mapsys;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.EventRegister;
import com.masterlong.framework.essentials.IGameEvent;
import com.masterlong.framework.essentials.SingletonManager;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.game.resources.Resource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * tileMap的基本单位，tile
 */
public class Tile {
    public enum TileType {
        BRICK,
        STONE,
        GRASS,
        WATER,
        HOME
    }

    protected TileType type;
    protected int width;
    protected String tileName;
    protected boolean collidable;
    protected AnimationPlayer animationPlayer;
    protected HashMap<Long, ArrayList<IGameEvent>> events;
    protected String tag;
    protected ImgArea imgArea;
    protected ResourceHandler resourceHandler;
    protected int layer;
    protected IGameEvent drawEvent;

    //根据传入类型初始化
    private void initByType() {
        resourceHandler = Resource.getResourceHandlerInstance();
        events = new HashMap<Long, ArrayList<IGameEvent>>();
        EventRegister eventRegister = SingletonManager.getEventRegister();
        imgArea = new ImgArea(0, 0, 0, 0, 0, 0);
        drawEvent = eventRegister.getEvent("empty");
        switch (type) {
            case BRICK:
                tileName = "brick";
                tag = "_WALL_";
                imgArea = (ImgArea) resourceHandler.getResource("brickImg");
                collidable = true;
                events.put(CollisionManager.ON_COLLISION, new ArrayList<>());
                events.get(CollisionManager.ON_COLLISION).add(eventRegister.getEvent("staticBounceBack"));
                events.get(CollisionManager.ON_COLLISION).add(eventRegister.getEvent("tileDestroy"));
                break;
            case STONE:
                tileName = "stone";
                tag = "_WALL_";
                imgArea = (ImgArea) resourceHandler.getResource("stoneImg");
                collidable = true;
                events.put(CollisionManager.ON_COLLISION, new ArrayList<>());
                events.get(CollisionManager.ON_COLLISION).add(eventRegister.getEvent("staticBounceBack"));
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
                width = animation.getFrame(0).dx;
//                events.put(ON_TILE_DRAW, new ArrayList<>());
//                events.get(ON_TILE_DRAW).add(eventRegister.getEvent("tileDrawAnimation"));
                drawEvent = eventRegister.getEvent("tileDrawAnimation");
                collidable = false;
                break;
        }
        if (width == 0)
            width = imgArea.dx;
    }

    public int getWidth() {
        return width;
    }

    public Tile(TileType tileType) {
        type = tileType;
        initByType();
    }
}