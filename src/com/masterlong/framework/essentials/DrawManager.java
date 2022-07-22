package com.masterlong.framework.essentials;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;

/**
 * 渲染对象管理器
 */
public class DrawManager {

    protected ArrayList<HashMap<Long, IResDraw>> drawLayers;//管理所有的可绘制物体

    private Color bgColor;
    private Image offScreenImage;//缓冲图像
    private Graphics gOffScreen;//缓冲graphics……句柄(不知道Java怎么称呼)?
    private GameView gameView;//游戏窗体
    private int viewPortW;
    private int viewPortH;//视觉实际宽和高
    private int width;
    private int height;
    private float zoomX;
    private float zoomY;
    public final static int maxLayerCount = 5;
    private long clock, delta;
    private final long delay = 8 * 1000000;
    private Lock readLock;//读锁

    public int getW() {
        return viewPortW;
    }

    public int getH() {
        return viewPortH;
    }

    public int zoomX(int x) {
        return (int) (x * zoomX);
    }

    public int zoomY(int y) {
        return (int) (y * zoomY);
    }

    public float getZoomX() {
        return zoomX;
    }

    public float getZoomY() {
        return zoomY;
    }

    //初始化
    protected DrawManager(GameView gameView, Lock readLock) {
        //初始化渲染层
        /*drawLayers = new ArrayList<ConcurrentHashMap<Long, IResDraw>>();
        for (int i = 0; i < maxLayerCount; i++)
            drawLayers.add(new ConcurrentHashMap<Long, IResDraw>());*/
        //双缓冲及窗口设置
        init(gameView, readLock);
    }

    protected DrawManager(){

    }

    public void init(GameView gameView, Lock readLock){
        offScreenImage = null;
        gOffScreen = null;
        this.gameView = gameView;
        this.readLock = readLock;
        viewPortW = gameView.getWidth();
        viewPortH = gameView.getHeight();
    }

    public void setBackgroundColor(Color c) {
        bgColor = c;
    }

    public void init(int width, int height) {
        //排除一哈空场景
        if(width == 0 || height == 0)
            return;
        this.width = width;
        this.height = height;
        viewPortW = gameView.getWidth();
        viewPortH = gameView.getHeight();
        zoomX = (float) viewPortW / width;
        zoomY = (float) viewPortH / height;
        //稍微减少一哈耗用
        if(offScreenImage == null || offScreenImage.getWidth(null) != width || offScreenImage.getHeight(null) != height) {
            offScreenImage = gameView.createImage(width, height);
            gOffScreen = offScreenImage.getGraphics();
        }
        gameView.repaint();
    }

    //添加绘制物体
    public DrawManager addItem(IResDraw drawItem, int layer) {
        drawItem.setLayer(layer);//设置层数
        HashMap<Long, IResDraw> drawLayer = drawLayers.get(layer);//获得当前层
        drawLayer.put(drawItem.getDrawID(), drawItem);//添加
        drawItem.bind(this);//绑定
        return this;
    }

    public DrawManager addItem(IResDraw drawItem) {
        HashMap<Long, IResDraw> drawLayer = drawLayers.get(drawItem.getLayer());//获得当前层
        drawLayer.put(drawItem.getDrawID(), drawItem);//添加
        drawItem.bind(this);//绑定
        return this;
    }

    //移除渲染队列
    public DrawManager removeItem(IResDraw drawItem) {
        int layer = drawItem.getLayer();
        long id = drawItem.getDrawID();
        drawLayers.get(layer).remove(id);
        return this;
    }

    //全部清除
    public void clear() {
        /*for (ConcurrentHashMap<Long, IResDraw> layer : drawLayers)
            layer.clear();
        drawLayers.clear();*/
    }

    //获取
    public IResDraw findItem(int layer, long id) {
        return drawLayers.get(layer).get(id);
    }

    //绘制图像
    public void draw(Graphics g) {
        //独立获取时钟
        //尝试同loop的update同步 减去makeUp
        clock = System.nanoTime();
        //读锁
        readLock.lock();
        //初始化
        g.setColor(bgColor);
        //清屏重绘
        gOffScreen.fillRect(0, 0, width, height);
        for (HashMap<Long, IResDraw> layer : drawLayers)
            for (IResDraw items : layer.values())
                items.draw(gOffScreen);
        g.drawImage(offScreenImage, 0, 0, viewPortW, viewPortH, 0, 0, width, height, null);
        readLock.unlock();
        delta = delay - (System.nanoTime() - clock);
        try {
            if (delta < 0)
                delta = 0;
            Thread.sleep(delta / 1000000);//同上
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while ((System.nanoTime() - clock) < delay) {
            // 使用循环，精确控制每帧绘制时长
        }

        gameView.repaint();
    }

    public void setDrawLayers(ArrayList<HashMap<Long, IResDraw>> drawLayers) {
        this.drawLayers = drawLayers;
    }
}
