package com.masterlong.game.objects.mapsys;

import com.masterlong.framework.components.AABB;
import com.masterlong.framework.components.CollisionManager;
import com.masterlong.framework.components.ICollider;
import com.masterlong.framework.essentials.*;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.objects.Bullet;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.util.HashMap;

/**
 * 作为组件单独处理Tile，而不是把它当作普通游戏物体
 */
public class TileMapSys extends GameComponentSystem {

    private class TileLayerDrawn implements IResDraw {

        private IGameEvent drawEvent;
        private int layer;
        private long ID;
        private final int[][] tileMap;
        private Tile tile;
        private ResourceHandler resourceHandler;
        private DrawManager drawManager;
        private boolean drawn;
        private boolean exist;
        private TileMapSys tileMapSys;
        private Direction pos;

        private TileLayerDrawn(Direction pos, Tile tile, int[][] tileMap, TileMapSys tileMapSys) {
            this.tile = tile;
            this.tileMap = tileMap;
            this.pos = pos;
            this.tileMapSys = tileMapSys;
            layer = tile.layer;
            ID = GameItem.generateID();
            drawn = true;
            exist = true;
            resourceHandler = Resource.getResourceHandlerInstance();
            EventRegister register = SingletonManager.getEventRegister();
            if (tile.animationPlayer != null) {
                drawEvent = register.getEvent("tileDrawAnimation");
            } else {
                drawEvent = register.getEvent("tileDraw");
            }
        }

        @Override
        public void draw(Graphics g) {
            drawEvent.run(g, tileMap, tile, exist);
            if (!exist)
                tileMapSys.removeItem(tile.type);
        }

        @Override
        public void bind(DrawManager d) {
            drawManager = d;
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
            if (drawn != isDrawn) {
                if (isDrawn)
                    drawManager.addItem(this);
                else
                    drawManager.removeItem(this);
                drawn = isDrawn;
            }
        }

        @Override
        public boolean isDrawn() {
            return drawn;
        }


    }

    private class TileCollideManager {
        private HashMap<Point, ICollider> colliders;
        private TileMapSys tileMapSys;
        private Direction pos;
        private CollisionManager collisionManager;

        private TileCollideManager(TileMapSys tileMapSys, Direction pos) {
            this.tileMapSys = tileMapSys;
            colliders = new HashMap<Point, ICollider>();
            this.pos = pos;
        }

        private TileCollideManager init() {
            collisionManager = (CollisionManager) scene.findComponent("collision");
            colliders.clear();
            return this;
        }

        private TileCollideManager addCollider(Point p) {
            Tile tile = tileMapSys.getTile(p.x, p.y);
            if (!tile.collidable)
                return this;
            long cID = GameItem.generateID();
            AABB aabb = new AABB(pos.x + p.x * TILE_SIZE, pos.y + p.y * TILE_SIZE, tile.width, tile.width);
            ICollider collider = new ICollider() {
                @Override
                public void onCollision(ICollider item) {
                    for (IGameEvent event : tile.events.get(CollisionManager.ON_COLLISION))
                        event.run(item, this, tileMapSys);
                }

                @Override
                public void bind(CollisionManager c) {

                }

                @Override
                public AABB getAABB() {
                    return aabb;
                }

                @Override
                public long getColliderID() {
                    return cID;
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
            colliders.put(p, collider);
            collisionManager.addItem(collider);
            return this;
        }

        private TileCollideManager removeCollider(Point p) {
            Tile tile = tileMapSys.getTile(p.x, p.y);
            if (!tile.collidable)
                return this;
            collisionManager.removeItem(colliders.get(p));
            colliders.remove(p);
            return this;
        }
    }

    private int[][] map;
    private int mapWidth;
    private int mapHeight;
    private Direction pos;
    private static final int TILE_SIZE = 17;
    private static HashMap<Tile.TileType, Tile> tileTable;
    private HashMap<Tile.TileType, TileLayerDrawn> tileLayers;
    private TileCollideManager tileCollideManager;
    private Tile.TileType removeType;
    private ResourceHandler resourceHandler;

    private HashMap<Tile.TileType, Tile> initTileTable() {
        HashMap<Tile.TileType, Tile> table = new HashMap<>();
        for (Tile.TileType type : Tile.TileType.values()) {
            table.put(type, new Tile(type));
        }
        return table;
    }

    public TileMapSys(int mapWidth, int mapHeight, Direction pos) {
        super();
        componentID = "tileMapSys";
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.pos = pos;
        map = new int[mapWidth][mapHeight];
    }

    public void eventRegister(){
        IGameEvent tileDrawAnimation = new IGameEvent() {
            @Override
            public Object getInfo(Object... args) {
                return null;
            }

            @Override
            public void run(Object... args) {
                Boolean exist = (Boolean) args[3];
                int[][] tileMap = (int[][]) args[1];
                Tile tile = (Tile) args[2];
                exist = false;
                ImgArea frame = tile.animationPlayer.updateFrame();
                for (int i = 0; i < tileMap.length; i++) {
                    for (int j = 0; j < tileMap[i].length; j++) {
                        if (tileMap[i][j] == tile.type.ordinal() + 1) {
                            resourceHandler.draw((Graphics) args[0], pos.x + i * TILE_SIZE, pos.y + j * TILE_SIZE, frame);
                            exist = true;
                        }
                    }
                }
            }
        };
        IGameEvent tileDraw = new IGameEvent() {
            @Override
            public Object getInfo(Object... args) {
                return null;
            }

            @Override
            public void run(Object... args) {
                Boolean exist = (Boolean) args[3];
                int[][] tileMap = (int[][]) args[1];
                Tile tile = (Tile) args[2];
                exist = false;
                for (int i = 0; i < tileMap.length; i++) {
                    for (int j = 0; j < tileMap[i].length; j++) {
                        if (tileMap[i][j] == tile.type.ordinal() + 1) {
                            resourceHandler.draw((Graphics) args[0], pos.x + i * TILE_SIZE, pos.y + j * TILE_SIZE, tile.imgArea);
                            exist = true;
                        }
                    }
                }
            }
        };
        IGameEvent tileDestroy = new IGameEvent() {
            @Override
            public void run(Object... args) {
                if (!(args[0] instanceof Bullet))
                    return;
                TileMapSys tileMapSys = (TileMapSys) args[2];  // 获取地图系统
                AABB aabb = ((ICollider) args[1]).getAABB();
                Point point = tileMapSys.getTilePos((int) aabb.x, (int) aabb.y);
                tileMapSys.removeTile(point.x, point.y);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        EventRegister eventRegister = SingletonManager.getEventRegister();
        eventRegister.register("tileDestroy", tileDestroy);
        eventRegister.register("tileDrawAnimation", tileDrawAnimation);
        eventRegister.register("tileDraw", tileDraw);
    }

    @Override
    public void init() {
        super.init();
        tileTable = initTileTable();
        tileLayers = new HashMap<Tile.TileType, TileLayerDrawn>();
        resourceHandler = Resource.getResourceHandlerInstance();
        tileLayers.clear();
        tileCollideManager = new TileCollideManager(this, pos);
        tileCollideManager.init();
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                map[i][j] = 0;
            }
        }
    }

    public TileMapSys addTile(Tile.TileType type, int x, int y) {
        map[x][y] = type.ordinal() + 1;
        if (!tileLayers.containsKey(type)) {
            tileLayers.put(type, new TileLayerDrawn(pos, tileTable.get(type), map, this));
            scene.drawManager.addItem(tileLayers.get(type));
        }
        tileCollideManager.addCollider(new Point(x, y));
        return this;
    }

    public TileMapSys removeTile(int x, int y) {
        tileCollideManager.removeCollider(new Point(x, y));
        map[x][y] = 0;
        return this;
    }

    private Tile getTile(int x, int y) {
        return tileTable.get(Tile.TileType.values()[map[x][y] - 1]);
    }

    public Point getTilePos(int x, int y) {
        int rx = x - pos.x;
        int ry = y - pos.y;
        if (rx != 0)
            rx /= TILE_SIZE;
        if (ry != 0)
            ry /= TILE_SIZE;
        return new Point(rx, ry);
    }

    public Direction getRealPos(int x, int y) {
        return new Direction(x * TILE_SIZE + pos.x, y * TILE_SIZE + pos.y);
    }

    @Override
    public void doRemoveList() {
        while ((removeType = (Tile.TileType) removeList.poll()) != null) {
            TileLayerDrawn layer = tileLayers.get(removeType);
            scene.drawManager.removeItem((IResDraw) layer);
            tileLayers.remove(removeType);
        }
    }
}
