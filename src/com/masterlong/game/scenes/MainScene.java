package com.masterlong.game.scenes;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.*;
import com.masterlong.framework.essentials.resources.IPoolCreate;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;
import com.masterlong.framework.math.Rigid;
import com.masterlong.game.objects.*;
import com.masterlong.game.objects.mapsys.Tile;
import com.masterlong.game.objects.mapsys.TileMapSys;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.util.Random;


/**
 * 游戏场景1
 */
public class MainScene extends Scene {
    public MainScene(GameView gameView) {
        super(gameView);
    }

    @Override
    public Scene registerEvents() {
        EventRegister eventRegister = SingletonManager.getEventRegister();
        eventRegister.unregisterAll();
        // 基础事件注册
        IGameEvent staticBounceBack = new IGameEvent() {
            @Override
            public void run(Object... args) {
                Rigid.staticBounceBack((ICollider) args[0], (ICollider) args[1], true);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        IGameEvent destroyByBullet = new IGameEvent() {
            @Override
            public void run(Object... args) {
                if (!((GameItem) args[1]).isDestroyed() && args[0] instanceof Bullet) {
                    ((ICollider) args[0]).onCollision((ICollider) args[1]);
                    ((GameItem) args[1]).destroy();
                }
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        IGameEvent animationPlay = new IGameEvent() {
            @Override
            public void run(Object... args) {
                ((AnimationPlayer) args[0]).draw((Graphics) args[1], (int) args[2], (int) args[3]);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        IGameEvent animationInit = new IGameEvent() {
            @Override
            public void run(Object... args) {
                ((AnimationPlayer) args[1]).init();
                ((AnimationPlayer) args[1]).setCycled(false);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        // 初始化归还事件
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        IGameEvent releaseToPool = new IGameEvent() {
            @Override
            public void run(Object... args) {
                resourceHandler.releaseItem(((GameItem) args[0]).getPoolName(), (GameItem) args[0]);
            }

            @Override
            public Object getInfo(Object... args) {
                return false;
            }
        };
        // 坦克行为
        Random sysRandom = SingletonManager.getRandom();
        IGameEvent periodicFire = new IGameEvent() {
            @Override
            public void run(Object... args) {
                Tank tank = (Tank) args[0];
                if (sysRandom.nextInt(4) == 0)
                    tank.fire();
            }

            @Override
            public Object getInfo(Object... args) {
                return 500L;
            }
        };
        IGameEvent periodicTurn = new IGameEvent() {
            @Override
            public void run(Object... args) {
                SpriteTank tank = (SpriteTank) args[0];
                if (!tank.isDestroyed()) {
                    if (!tank.isBounced())
                        tank.setDirection(Direction.AXES[sysRandom.nextInt(4)]);
                    else
                        tank.setBounced(false);
                }
            }

            @Override
            public Object getInfo(Object... args) {
                return 3000L;
            }
        };
        // 添加到事件注册器
        eventRegister.register("staticBounceBack", staticBounceBack);
        eventRegister.register("destroyByBullet", destroyByBullet);
        eventRegister.register("animationPlay", animationPlay);
        eventRegister.register("animationInit", animationInit);
        eventRegister.register("releaseToPool", releaseToPool);
        eventRegister.register("periodicFire", periodicFire);
        eventRegister.register("periodicTurn", periodicTurn);
        return super.registerEvents();
    }

    @Override
    public void init(Object... args) {
        super.init();
        //playerTank.keyMonitor.setKeyCollections(Arrays.asList(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D));
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        //初始化场景组件
        addComponent(new CollisionManager(width, height))
                .addComponent(new InputManager(gameView.getGameFrame()));
        TileMapSys tileMapSys = Resource.getMapInstance();
        tileMapSys.eventRegister();
        addComponent(tileMapSys);
        tileMapSys.loadTileMap("map.txt");
        //初始化地图
        int brickWidth = ((ImgArea) resourceHandler.getResource("brickImg")).dx;
        PlayerTank playerTank = new PlayerTank(width / 2 - 34 / 2, height - brickWidth * 6 + 2 - 40);//在200, 200处生成一辆坦克
        addComponent(new SpriteTankFactory(new Point(22, 5), this));
        addComponent(new StatusMonitor(this, new Direction(width / 2 - 34 / 2, height - brickWidth * 6 + 2 - 40), playerTank));
        addItem(new HomeSpot(new Direction(width / 2 - brickWidth * 6 / 2, height - brickWidth * 5)));
        addItem(playerTank);
        ////////////////////////////////////////////////////////////////////////////////////////////////
        GameDebug gameDebug = new GameDebug(this);
        findComponent("input").addItem(gameDebug);
        drawManager.addItem(gameDebug);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        EventRegister eventRegister = SingletonManager.getEventRegister();
        IGameEvent releaseToPool = eventRegister.getEvent("releaseToPool");
        IGameEvent animationInit = eventRegister.getEvent("animationInit");
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //对象池分配资源
        //要是能用宏就好了，省一堆操作
        //在onDestroy下归还资源
        resourceHandler.initPoolByName("bullets", new IPoolCreate() {
                    @Override
                    public GameItem createItem() {
                        Bullet bullet = new Bullet(0, 0, Direction.UP, "");
                        bullet.setPoolName("bullets");
                        return bullet.addEvent(GameItem.ON_DESTROY, releaseToPool);
                    }
                }
                , 100);
        resourceHandler.initPoolByName("enemies", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                SpriteTank spriteTank = new SpriteTank(0, 0);
                spriteTank.setPoolName("enemies");
                return spriteTank.addEvent(GameItem.ON_DESTROY, releaseToPool);
            }
        }, 50);
        resourceHandler.initPoolByName("bulletExplosions", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                AnimationPlayer animationPlayer = new AnimationPlayer((Animation) resourceHandler.getResource("bulletExplosion"));
                AnimationItem animationItem = new AnimationItem(new Direction(0, 0),
                        animationPlayer);
                return animationItem
                        .setPoolName("bulletExplosions")
                        .addEvent(GameItem.ON_INIT, animationInit)
                        .addEvent(GameItem.ON_DESTROY, releaseToPool);
            }
        }, 50);
        resourceHandler.initPoolByName("bulletExplosions2", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                AnimationPlayer animationPlayer = new AnimationPlayer((Animation) resourceHandler.getResource("bulletExplosion2"));
                AnimationItem animationItem = new AnimationItem(new Direction(0, 0),
                        animationPlayer);
                return animationItem
                        .setPoolName("bulletExplosions2")
                        .addEvent(GameItem.ON_INIT, animationInit)
                        .addEvent(GameItem.ON_DESTROY, releaseToPool);
            }
        }, 100);
        resourceHandler.initPoolByName("tankExplosions", new IPoolCreate() {
            @Override
            public GameItem createItem() {
                AnimationPlayer animationPlayer = new AnimationPlayer((Animation) resourceHandler.getResource("tankExplosion"));
                AnimationItem animationItem = new AnimationItem(new Direction(0, 0),
                        animationPlayer);
                return animationItem
                        .setPoolName("tankExplosions")
                        .addEvent(GameItem.ON_INIT, animationInit)
                        .addEvent(GameItem.ON_DESTROY, releaseToPool);

            }
        }, 100);
    }
}
