package com.masterlong.framework.essentials.resources;

import com.masterlong.framework.essentials.GameItem;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 游戏资源管理
 */
public class ResourceHandler {
    protected Image res;//游戏主贴图
    protected HashMap<String, Object> staticResources;
    //对象池部分
    protected HashMap<String, ArrayList<GameItem>> itemPool;
    protected HashMap<String, IPoolCreate> poolCreator;

    //初始化对象池
    public void initPoolByName(String name, IPoolCreate create, int capacity) {
        if (itemPool.containsKey(name)) {
            return;
        }
        ArrayList<GameItem> list = new ArrayList<GameItem>(capacity);
        //初始化池子
        for (int i = 0; i < capacity; i++) {
            list.add(create.createItem());
        }
        itemPool.put(name, list);
        poolCreator.put(name, create);
    }

    //清空池子
    public void clearPoolByName(String name) {
        if (itemPool.containsKey(name)) {
            itemPool.remove(name);
            poolCreator.remove(name);
        }
    }

    //清空
    public void clearAllPool() {
        itemPool.clear();
        poolCreator.clear();
    }

    //根据tag获取对象
    public GameItem getItemByName(String name) {
        if (itemPool.containsKey(name)) {
            ArrayList<GameItem> list = itemPool.get(name);
            if (list.size() > 0)
                return list.remove(0);
            return poolCreator.get(name).createItem();
        }
        return null;
    }

    //归还
    public void releaseItem(String name, GameItem item) {
        if (itemPool.containsKey(name)) {
            ArrayList<GameItem> list = itemPool.get(name);
            list.add(item);
        }
    }

    //初始化
    public ResourceHandler(String path) {
        staticResources = new HashMap<String, Object>();
        itemPool = new HashMap<String, ArrayList<GameItem>>();
        poolCreator = new HashMap<String, IPoolCreate>();
    }

    //指定并绘制指定区域
    public void draw(Graphics g, int x, int y, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        g.drawImage(res, x, y, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    public void draw(Graphics g, int x, int y, ImgArea imgArea) {
        g.drawImage(res, x, y, x + imgArea.dx, y + imgArea.dy, imgArea.x, imgArea.y, imgArea.sx, imgArea.sy, null);
    }

    public void draw(Graphics g, int x, int y, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, float zx, float zy) {
        g.drawImage(res, (int) (x * zx), (int) (y * zy), (int) (dx2 * zx), (int) (dy2 * zy), sx1, sy1, sx2, sy2, null);
    }

    public void draw(Graphics g, int x, int y, ImgArea imgArea, float zx, float zy) {
        g.drawImage(res, (int) (x * zx), (int) (y * zy), (int) ((x + imgArea.dx) * zx), (int) ((y + imgArea.dy) * zy), imgArea.x, imgArea.y, imgArea.sx, imgArea.sy, null);
    }

    public Object getResource(String id) {
        return staticResources.get(id);
    }
}
