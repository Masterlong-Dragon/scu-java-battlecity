package com.masterlong.game.objects;

import com.masterlong.framework.components.PeriodicTimer;
import com.masterlong.framework.essentials.*;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.objects.mapsys.Tile;
import com.masterlong.game.objects.mapsys.TileMapSys;
import com.masterlong.game.resources.Resource;

import java.awt.*;
import java.util.Random;

/**
 * 精灵坦克工厂
 */
public class SpriteTankFactory extends GameComponentSystem {

    private boolean isDestroyed;
    private final Random rand;
    private Direction[] generatedPos;//定点生成
    private int gapLength;
    private PeriodicTimer periodicTimer;
    private long ID;
    private Point pos;
    private Scene scene;

    public SpriteTankFactory(Point pos, Scene scene) {
        rand = new Random(System.nanoTime());
        ID = GameItem.generateID();
        componentID = "spriteTankFactory";
        this.pos = pos;
        this.scene = scene;
    }

    @Override
    public void update(long clock) {
        //每1秒有50%概率随机在三个点位的其中一个生成坦克
        periodicTimer.update(clock);
    }

    @Override
    public void init() {
        // 生成三个图块
        // 不参与绘制
        gapLength = 14;
        /*for (int i = -gapLength; i <= gapLength; i += gapLength)
            scene.addItem(new Tile(Tile.TileType.GRASS, pos.x + i, pos.y));
        */
        TileMapSys tileMapSys = (TileMapSys) scene.findComponent("tileMapSys");
        for (int i = -gapLength; i <= gapLength; i += gapLength)
            tileMapSys.addTile(Tile.TileType.GRASS, pos.x + i, pos.y);
        generatedPos = new Direction[]{tileMapSys.getRealPos(pos.x - gapLength, pos.y), tileMapSys.getRealPos(pos.x, pos.y), tileMapSys.getRealPos(pos.x + gapLength, pos.y)};
        periodicTimer = new PeriodicTimer();
        //每1秒有50%概率随机在三个点位的其中一个生成坦克
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        IGameEvent generate = new IGameEvent() {
            @Override
            public void run(Object... args) {
                if (rand.nextInt(2) == 0) {
                    Direction direction = new Direction(0, 0);
                    int index = rand.nextInt(3);
                    direction.set(generatedPos[index]);
                    SpriteTank spriteTank = (SpriteTank) resourceHandler.getItemByName("enemies");
                    spriteTank.setPos(direction);
                    scene.addItem(spriteTank);
                }
            }

            @Override
            public Object getInfo(Object... args) {
                return 1000L;
            }
        };
        periodicTimer.addEvent("generate", generate);
    }

    @Override
    public void hang() {
    }

    @Override
    public void resume() {

    }

    @Override
    public void bind(LogicManager l) {
    }

    @Override
    public long getLogicID() {
        return ID;
    }


    @Override
    public void onDestroy() {
        isDestroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }
}
