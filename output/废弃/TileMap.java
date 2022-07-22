package com.masterlong.game.objects.mapsys;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.*;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TileMap extends GameComponentSystem {

    // 图块种类
    public enum TileType {
        BRICK,
        IRON,
        GRASS,
        WATER,
        HOME
    }

    protected int[][] map;
    protected Direction pos;
    protected int width;
    protected int height;
    protected int tileWidth;
    protected final static HashMap<Integer, Tile> tileTable = initTileTable();
    protected HashMap<Integer, TileWrapperLayer> tileLayers;

    public TileMap(Direction pos) {
        ID = GameItem.generateID();
        this.pos = pos;
    }

    public TileMap(int x, int y) {
        ID = GameItem.generateID();
        pos = new Direction(x, y);
    }

    private static HashMap<Integer, Tile> initTileTable() {
        HashMap<Integer, Tile> table = new HashMap<>();
        for (TileType type : TileType.values()) {
            tileTable.put(type.ordinal(), new Tile(type));
        }
        return table;
    }

    private HashMap<Integer, TileWrapperLayer> initTileLayers() {
        HashMap<Integer, TileWrapperLayer> layers = new HashMap<>();
        for (TileType type : TileType.values()) {
            layers.put(type.ordinal(), new TileWrapperLayer(new Tile(type), pos));
        }
        return layers;
    }

    public TileMap addTile(TileType type, int x, int y) {
        TileWrapperLayer layer = tileLayers.get(type.ordinal());
        layer.addTile(x, y);
        return this;
    }

    public TileMap removeTile(int x, int y) {
        tileLayers.get(map[x][y]).removeTile(x, y);
        map[x][y] = -1;
        return this;
    }

    @Override
    public void init() {
        super.init();
        map = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                map[i][j] = -1;
            }
        }
        tileLayers = initTileLayers();
    }

    @Override
    public void clear() {
        super.clear();
        map = null;
    }

    public TileMap addTile(int x, int y, TileType type) {
        map[x][y] = type.ordinal();
        return this;
    }

    private class TileWrapperLayer implements IResDraw {

        private Tile tile;
        private Direction pos;
        private int typeID;
        private boolean isDrawn;
        private boolean isInitDrawManager;
        private boolean drawn;
        private DrawManager drawManager;
        private CollisionManager collisionManager;
        private int layer;
        private long ID;
        private ResourceHandler resourceHandler;
        private HashMap<Direction, ICollider> colliders;
        private IGameEvent createCollider;
        private Direction opDirection;

        public TileWrapperLayer(Tile tile, Direction pos) {
            this.tile = tile;
            this.pos = pos;
            typeID = tile.type.ordinal();
            isDrawn = false;
            isInitDrawManager = false;
            drawn = false;
            layer = tile.layer;
            ID = GameItem.generateID();
            resourceHandler = Resource.getResourceHandlerInstance();
            colliders = initCollideEvents();
            opDirection = new Direction(0, 0);
        }

        private TileWrapperLayer removeTile(int x, int y) {
            if (collisionManager != null) {
                opDirection.set(x, y);
                collisionManager.removeItem(colliders.get(opDirection));
            }
            map[x][y] = -1;
            return this;
        }

        private TileWrapperLayer addTile(int x, int y) {
            map[x][y] = typeID;
            if (collisionManager != null) {
                opDirection.set(x, y);
                collisionManager.addItem(colliders.get(opDirection));
            }
            return this;
        }

        private HashMap<Direction, ICollider> initCollideEvents() {
            if (!tile.collidable)
                return null;
            createCollider = new IGameEvent() {
                @Override
                public void run(Object... args) {

                }

                @Override
                public Object getInfo(Object... args) {
                    AABB aabb = (AABB) (args[0]);
                    long id = GameItem.generateID();
                    return new ICollider() {
                        @Override
                        public void onCollision(ICollider item) {
                            if (tile.events.containsKey(CollisionManager.ON_COLLISION))
                                for (IGameEvent event : tile.events.get(CollisionManager.ON_COLLISION))
                                    event.run(item, TileWrapperLayer.this);
                        }

                        @Override
                        public void bind(CollisionManager c) {
                            collisionManager = c;
                        }

                        @Override
                        public AABB getAABB() {
                            return aabb;
                        }

                        @Override
                        public long getColliderID() {
                            return id;
                        }

                        @Override
                        public String getColliderTag() {
                            return tile.tag;
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
                            return true;
                        }

                        @Override
                        public boolean isRigid() {
                            return false;
                        }

                        @Override
                        public HashMap<String, Integer> getFilter() {
                            return null;
                        }
                    };
                }
            };
            HashMap<Direction, ICollider> colliders = new HashMap<>();
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    if (map[i][j] == typeID) {
                        AABB aabb = new AABB(pos.x + i * tileWidth, pos.y + j * tileWidth, tileWidth, tileWidth);
                        long initID = GameItem.generateID();
                        colliders.put(new Direction(i, j), (ICollider) createCollider.getInfo(aabb));
                    }
                }
            }
            return colliders;
        }

        @Override
        public void draw(Graphics g) {
            if (!tile.events.containsKey(Tile.ON_TILE_DRAW))
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        if (map[j][i] == typeID) {
                            resourceHandler.draw(g, j * tileWidth + pos.x, i * tileWidth + pos.y, tile.imgArea);
                        }
                    }
                }
            else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        if (map[j][i] == typeID) {
                            resourceHandler.draw(g, j * tileWidth + pos.x, i * tileWidth + pos.y, tile.imgArea);
                            for (IGameEvent event : tile.events.get(Tile.ON_TILE_DRAW)) {
                                event.run(tile.animationPlayer, g, j * tileWidth + pos.x, i * tileWidth + pos.y);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void bind(DrawManager d) {
            drawManager = d;
            isInitDrawManager = drawn = true;
        }

        @Override
        public long getDrawID() {
            return ID;
        }

        @Override
        public void setLayer(int layer) {
            this.layer = layer;
        }

        @Override
        public int getLayer() {
            return layer;
        }

        @Override
        public void setDrawn(boolean isDrawn) {
            if (isInitDrawManager)
                if (drawn != isDrawn) {
                    if (!drawn)
                        drawManager.addItem(this);
                    else
                        drawManager.removeItem(this);
                    drawn = isDrawn;
                }

        }

        @Override
        public boolean isDrawn() {
            return isDrawn;
        }
    }

    /**
     * tileMap的基本单位，tile
     */
    private static class Tile {

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

        // 事件
        public static long ON_TILE_DRAW = EventRegister.getEventID();

        //根据传入类型初始化
        private void initByType() {
            resourceHandler = Resource.getResourceHandlerInstance();
            events = new HashMap<Long, ArrayList<IGameEvent>>();
            EventRegister eventRegister = SingletonManager.getEventRegister();
            switch (type) {
                case BRICK:
                    tileName = "brick";
                    tag = "_WALL_";
                    imgArea = (ImgArea) resourceHandler.getResource("brickImg");
                    collidable = true;
                    events.put(CollisionManager.ON_COLLISION, new ArrayList<>());
                    events.get(CollisionManager.ON_COLLISION).add(eventRegister.getEvent("staticBounceBack"));
                    events.get(CollisionManager.ON_COLLISION).add(eventRegister.getEvent("destroyByBullet"));
                    break;
                case IRON:
                    tileName = "iron";
                    tag = "_WALL_";
                    imgArea = (ImgArea) resourceHandler.getResource("ironImg");
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
                    events.put(ON_TILE_DRAW, new ArrayList<>());
                    events.get(ON_TILE_DRAW).add(eventRegister.getEvent("animationPlay"));
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
        }

        public int getWidth() {
            return width;
        }

        public Tile(TileType tileType) {
            type = tileType;
            initByType();
        }
    }
}
