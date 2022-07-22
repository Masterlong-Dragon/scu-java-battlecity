package com.masterlong.framework.essentials;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 场景管理器
 * 场景管理器持有唯一的drawManager和loopManager
 * 场景使能时，将场景自身保有的渲染和逻辑对象赋予两个管理器
 */
public class SceneManager {

    public DrawManager drawManager;
    public LogicManager logicManager;
    private GameView gameView;
    private Thread loopThread;
    private HashMap<String, Scene> scenes;
    private Scene activeScene, emptyScene;
    private String inActive;
    private volatile boolean ready;
    private final long delay = 16 * 1000000;
    //用于绘制和逻辑线程的读写互斥
    //绘制线程是awt的paint线程 保证在解算完成之后再绘制图片
    private final ReadWriteLock readWriteLock;

    //一个小trick了属于是
    //一开始的时候用一个空场景顶替
    //重写draw和takeControl方法 避免获取不到缓冲绘图句柄尴尬
    //这样的话只需要获取一次缓冲graphics，不用每帧都判断一哈offImage要不要创建
    public static class EmptyScene extends Scene {
        public EmptyScene(GameView gameView) {
            super(gameView);
        }

        @Override
        public void init(Object... args) {
            drawManager = new DrawManager(gameView, null) {

                @Override
                public void draw(Graphics g) {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getW(), getH());
                    System.out.println("empty");
                }
            };
        }

        @Override
        public void takeControl() {
        }
    }

    protected SceneManager() {
        //初始化锁状态
        readWriteLock = new ReentrantReadWriteLock();
    }

    public void init(GameView gameView) {
        //drawManager.init();
        this.gameView = gameView;
        scenes = new HashMap<String, Scene>();
        //获取两个重要管理器的单例
        //并初始化它们
        drawManager = SingletonManager.getDrawManager();
        drawManager.init(gameView, readWriteLock.readLock());
        logicManager = SingletonManager.getLogicManager();
        logicManager.init(gameView, readWriteLock.writeLock(), this);
        ready = true;
        emptyScene = new EmptyScene(gameView);
        emptyScene.init();
        activeScene = emptyScene;
        loopThread = new Thread(new Runnable() {
            private long clock, delta = 0;

            @Override
            public void run() {
                while (ready) {
                    try {
                        //主逻辑
                        //这个其实算fixed update, 强行稳定帧速度
                        clock = System.nanoTime();
                        logicManager.update(clock / 1000000);
                        delta = delay - (System.nanoTime() - clock);
                        if (delta > 0) {
                            Thread.sleep(delta / 1000000);//同上
                            while ((System.nanoTime() - clock) < delay) {
                                // 使用循环，精确控制每帧绘制时长
                            }
                        } else
                            System.out.println("warning: delta < 0");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });//逻辑主线程//主循环逻辑线程
    }

    public void addScene(Scene scene, String id) {
        if (!scenes.containsKey(id)) {
            scenes.put(id, scene);
        }
    }

    //奇妙的切换场景
    public void setActiveScene(String id, boolean clear, boolean init, Object... args) {
        Scene temp = activeScene;
        boolean switched;
        //如果已经在运行场景
        //先一定要上锁
        if (switched = (activeScene != emptyScene)) {
            readWriteLock.writeLock().lock();
        }
        if (inActive == null || !inActive.equals(id)) {
            inActive = id;
            if (clear)
                temp.clear();
            temp = scenes.get(id);
        } else {
            activeScene = emptyScene;
            if (clear)
                temp.clear();
        }
        if (init)
            temp.init(args);
        //恢复内容控制
        //上锁防止冲突
        logicManager.clear();
        temp.takeControl();
        drawManager.init(temp.getWidth(), temp.getHeight());
        activeScene = temp;
        if (switched) {
            readWriteLock.writeLock().unlock();
        }
        if (activeScene != emptyScene && !loopThread.isAlive())
            loopThread.start();
    }

    public void drawScene(Graphics g) {
        activeScene.drawManager.draw(g);
    }

    public void adjustViewPort() {
        System.out.println("adjusting view port……");
        readWriteLock.readLock().lock();
        drawManager.init(activeScene.getWidth(), activeScene.getHeight());
        readWriteLock.readLock().unlock();
    }
}
