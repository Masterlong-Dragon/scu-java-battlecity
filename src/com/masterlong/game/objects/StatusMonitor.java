package com.masterlong.game.objects;

import com.masterlong.framework.components.*;
import com.masterlong.framework.essentials.AliveMonitor;
import com.masterlong.framework.essentials.GameComponentSystem;
import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.essentials.Scene;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.objects.mapsys.Tile;
import com.masterlong.game.objects.mapsys.TileMapSys;
import com.masterlong.game.resources.Resource;

import java.awt.event.KeyEvent;

/**
 * 局势监控
 * 目前只负责坦克重生
 */
public class StatusMonitor extends GameComponentSystem implements IInput {

    private final AnimationItem rebirthAnimationItem;
    private final AnimationPlayer animationPlayer;
    private final PlayerTank playerTank;
    private AliveMonitor aliveMonitor;
    private CollisionManager collisionManager;
    private SpriteTankFactory spriteTankFactory;
    private TileMapSys tileMapSys;
    private final Scene scene;
    private final Direction rebirthPos;

    public StatusMonitor(Scene scene, Direction rebirthPos, PlayerTank playerTank) {
        ID = GameItem.generateID();
        componentID = "statusMonitor";
        isDestroyed = false;
        this.playerTank = playerTank;
        ResourceHandler resourceHandler = Resource.getResourceHandlerInstance();
        Animation rebirthAnimation = (Animation) resourceHandler.getResource("rebirthAnimation");
        this.scene = scene;
        this.rebirthPos = rebirthPos;
        animationPlayer = new AnimationPlayer(rebirthAnimation);
        animationPlayer.setCycled(false);
        rebirthAnimationItem = new AnimationItem(new Direction(rebirthPos.x, rebirthPos.y), animationPlayer) {
            @Override
            public void onDestroy() {
                super.onDestroy();
                playerTank.setPos(rebirthPos);
                scene.attachComponent(playerTank, "input");
                scene.attachComponent(playerTank, "collision");
                aliveMonitor.removeItem(playerTank);
                playerTank.setDirection(Direction.UP);
                playerTank.setDrawn(true);
            }
        };
        rebirthAnimationItem.setCycleTimeLimited(true).setCycleCnt(3).setLayer(1);
    }

    @Override
    public void init() {
        super.init();
        scene.attachComponent(this, "input");
        aliveMonitor = (AliveMonitor) scene.findComponent("aliveMonitor");
        collisionManager = (CollisionManager) scene.findComponent("collision");
        spriteTankFactory = (SpriteTankFactory) scene.findComponent("spriteTankFactory");
        tileMapSys = (TileMapSys) scene.findComponent("tileMapSys");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scene.detachComponent(this, "input");
    }

    public void playerTankRebirth() {
        System.out.println("Player Tank Reborn");
        animationPlayer.setPause(false).resetFrame();
        scene.addItem(rebirthAnimationItem);
        /*for (int i = 20; i < 26; i++)
            tileMapSys.addTile(Tile.TileType.BRICK, i, 30);
        for(int i = 31; i < 35; i++)
            tileMapSys.addTile(Tile.TileType.BRICK,20, i);
        for(int i = 31; i < 35; i++)
            tileMapSys.addTile(Tile.TileType.BRICK,25, i);*/
    }

    @Override
    public void keyPressed(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_R: {
                aliveMonitor.hangOnAllItems().addItem(collisionManager).addItem(spriteTankFactory).addItem(tileMapSys);
                break;
            }
            case KeyEvent.VK_C: {
                aliveMonitor.resumeAllItems().removeItem(collisionManager).removeItem(spriteTankFactory).removeItem(tileMapSys);
                break;
            }
        }
    }

    @Override
    public long getInputID() {
        return ID;
    }

    @Override
    public void bind(InputManager i) {

    }
}