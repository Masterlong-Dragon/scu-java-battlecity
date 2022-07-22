package com.masterlong.game.objects.mapsys;

import com.masterlong.framework.components.AABB;
import com.masterlong.framework.components.CollisionManager;
import com.masterlong.framework.components.ICollider;
import com.masterlong.framework.components.PeriodicTimer;
import com.masterlong.framework.essentials.*;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.objects.Bullet;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * 作为组件单独处理Tile，而不是把它当作普通游戏物体
 */
public class TileMapSys extends GameComponentSystem {

    private ArrayList<Tile> getTileDrawnForLayer(int layer) {
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        for (Tile tile : tileTable.values()) {
            if (tile.layer == layer) {
                tiles.add(tile);
            }
        }
        return tiles;
    }

    // 事件
    public static long ON_TILE_DRAW = EventRegister.getEventID();

    private class TileLayerDrawn implements IResDraw {
        private int layer;
        private long ID;
        private final int[][] tileMap;
        private ArrayList<Tile> tiles;
        private ResourceHandler resourceHandler;
        private DrawManager drawManager;
        private boolean drawn;
        private boolean exist;
        private TileMapSys tileMapSys;
        private Direction pos;

        private TileLayerDrawn(Direction pos, int layer, int[][] tileMap, TileMapSys tileMapSys) {
            tiles = tileMapSys.getTileDrawnForLayer(layer);
            this.tileMap = tileMap;
            this.pos = pos;
            this.tileMapSys = tileMapSys;
            this.layer = layer;
            ID = GameItem.generateID();
            drawn = true;
            exist = true;
            resourceHandler = Resource.getResourceHandlerInstance();
            EventRegister register = SingletonManager.getEventRegister();
        }

        private void hang() {
            for (Tile tile : tiles)
                if (tile.animationPlayer != null)
                    tile.animationPlayer.setPause(true);
        }

        private void resume() {
            for (Tile tile : tiles)
                if (tile.animationPlayer != null)
                    tile.animationPlayer.setPause(false);
        }

        @Override
        public void draw(Graphics g) {
            exist = false;
            // 更新tile元素状态
            for (Tile tile : tiles)
                tile.drawEvent.run(tile);
            // 绘制地图
            for (int i = 0; i < tileMap.length; i++)
                for (int j = 0; j < tileMap[i].length; j++) {
                    Tile tile = tileMapSys.getTile(i, j);
                    if (tile == null || tile.layer != layer)
                        continue;
                    resourceHandler.draw(g, pos.x + i * TILE_SIZE, pos.y + j * TILE_SIZE, tile.imgArea);
                    exist = true;
                }
            if (!exist)
                tileMapSys.removeItem(this);
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

        private TileCollideManager addCollider(Point p, IGameEvent... event) {
            Tile tile = tileMapSys.getTile(p.x, p.y);
            if (!tile.collidable)
                return this;
            if (colliders.get(p) != null)
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
            if (tile == null || !tile.collidable)
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
    private HashMap<Integer, TileLayerDrawn> tileLayers;
    private TileCollideManager tileCollideManager;
    private Tile.TileType removeType;
    private HashMap<Point, HashMap<String, IGameEvent>> updateEventTable;
    private HashMap<Point, PeriodicTimer> updateTimers;

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

    public void eventRegister() {
        IGameEvent tileDrawAnimation = new IGameEvent() {
            @Override
            public Object getInfo(Object... args) {
                return null;
            }

            @Override
            public void run(Object... args) {
                Tile tile = (Tile) args[0];
                tile.imgArea = tile.animationPlayer.updateFrame();
            }
        };
        IGameEvent tileDraw = new IGameEvent() {
            @Override
            public Object getInfo(Object... args) {
                return null;
            }

            @Override
            public void run(Object... args) {
//                Tile tile = (Tile) args[0];
//                tile.imgArea = tile.imgArea;
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
        eventRegister.register("empty", tileDraw);
    }

    @Override
    public void init() {
        super.init();
        tileTable = initTileTable();
        tileLayers = new HashMap<Integer, TileLayerDrawn>();
        updateEventTable = new HashMap<Point, HashMap<String, IGameEvent>>();
        updateTimers = new HashMap<Point, PeriodicTimer>();
        tileLayers.clear();
        tileCollideManager = new TileCollideManager(this, pos);
        tileCollideManager.init();
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                map[i][j] = 0;
            }
        }
    }

    @Override
    public void update(long clock) {
        super.update(clock);
        for (Point pos : updateTimers.keySet()) {
            PeriodicTimer timer = updateTimers.get(pos);
            timer.update(clock, pos);
        }
        for (Point pos : updateEventTable.keySet()) {
            HashMap<String, IGameEvent> events = updateEventTable.get(pos);
            for (IGameEvent event : events.values()) {
                event.run(this, clock, pos);
            }
        }
    }

    public TileMapSys loadTileMap(String fileName) {
        // tilemap系统
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String str;
            String[] lines;
            while ((str = reader.readLine()) != null) {
                lines = str.split("=");
                Tile.TileType type = Tile.TileType.valueOf(lines[0].toUpperCase(Locale.ROOT));
                lines = lines[1].split(",");
                addTile(type, Integer.parseInt(lines[0]), Integer.parseInt(lines[1]));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return this;
    }

    public TileMapSys addTile(Tile.TileType type, int x, int y) {
        map[x][y] = type.ordinal() + 1;
        int layer = tileTable.get(type).layer;
        if (!tileLayers.containsKey(layer)) {
            tileLayers.put(layer, new TileLayerDrawn(pos, layer, map, this));
            scene.drawManager.addItem(tileLayers.get(layer));
        }
        tileCollideManager.addCollider(new Point(x, y));
        return this;
    }

    public TileMapSys addTileEvent(Point pos, boolean isPeriodic, String id, IGameEvent event) {
        if (isPeriodic) {
            if (!updateTimers.containsKey(pos)) {
                updateTimers.put(pos, new PeriodicTimer(this));
            }
            updateTimers.get(pos).addEvent(id, event);
        } else {
            if (!updateEventTable.containsKey(pos)) {
                HashMap<String, IGameEvent> eventTable = new HashMap<>();
            }
            updateEventTable.get(pos).put(id, event);

        }
        return this;
    }

    public TileMapSys removeTileEvent(Point pos, boolean isPeriodic, String id) {
        if (isPeriodic) {
            updateTimers.get(pos).removeEvent(id);
        } else {
            updateEventTable.get(pos).remove(id);
            if (updateEventTable.get(pos).size() == 0) {
                updateEventTable.remove(pos);
            }
        }
        return this;
    }

    public TileMapSys removeTileEvent(Point pos) {
        if (updateEventTable.containsKey(pos)) {
            updateEventTable.remove(pos);
        }
        if (updateTimers.containsKey(pos)) {
            updateTimers.remove(pos);
        }
        return this;
    }

    public TileMapSys removeTile(int x, int y) {
        tileCollideManager.removeCollider(new Point(x, y));
        map[x][y] = 0;
        removeTileEvent(new Point(x, y));
        return this;
    }

    private Tile getTile(int x, int y) {
        int type = map[x][y] - 1;
        if (type < 0)
            return null;
        return tileTable.get(Tile.TileType.values()[type]);
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

    @Override
    public void hang() {
        super.hang();
        for (TileLayerDrawn layer : tileLayers.values()) {
            layer.hang();
        }
    }

    @Override
    public void resume() {
        super.resume();
        for (TileLayerDrawn layer : tileLayers.values()) {
            layer.resume();
        }
    }
}
