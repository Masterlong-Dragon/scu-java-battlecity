package com.masterlong.framework.essentials;

import com.masterlong.framework.components.CollisionManager;
import com.masterlong.framework.components.IInput;
import com.masterlong.framework.components.InputManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * 调试类
 * 负责碰撞和物体管理
 */
public class GameDebug implements IInput, IResDraw {

    public Scene scene;
    private final CollisionManager collisionManager;
    private DrawManager drawManager;
    private InputManager inputManager;
    private boolean isDrawn;

    public GameDebug(Scene scene) {
        this.scene = scene;
        collisionManager = (CollisionManager) scene.findComponent("collision");
    }

    @Override
    public void keyPressed(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_D:
                isDrawn = !isDrawn;
                break;
            case KeyEvent.VK_B:
                //if (isDrawn)
                collisionManager.setDrawn(!collisionManager.isDrawn());
                break;
            case KeyEvent.VK_P:
                scene.logicManager.setActiveScene("1", true, true);
                break;
        }
    }

    @Override
    public long getInputID() {
        return -1;
    }

    @Override
    public void bind(InputManager i) {
        inputManager = i;
    }

    @Override
    public void draw(Graphics g) {
        if (isDrawn) {
            Color c = g.getColor();
            g.setColor(Color.pink);
            g.drawString("Debug mode", 15, 70);
            g.setColor(Color.LIGHT_GRAY);
            int y = 0, i = 0;
            for (i = 0; i < 2; i++)
                for (IUpdate uc : scene.logicManager.updateComponents[i].values())
                    g.drawString("running " + ((GameComponentSystem) (uc)).getComponentID(), 15, y = y + (i + 1) * 10);
            i = 1;
            int dy = (y += 20);
            g.setColor(Color.YELLOW);
            for (IUpdate u : scene.logicManager.updateItems.values())
                g.drawString("looping for " + ((u instanceof GameItem) ? ((GameItem) u).getTag() : "_LOGIC") + "@" + u.getLogicID(), 15, y = y + i * 15);
            g.setColor(Color.RED);
            i = 1;
            for (HashMap<Long, IResDraw> layer : scene.drawManager.drawLayers)
                for (IResDraw d : layer.values()) {
                    g.drawString("drawing for " + ((d instanceof GameItem) ? ((GameItem) d).getTag() : "_DRAWN") + "@" + d.getDrawID(), 15 + scene.getWidth() - 160, dy = dy + i * 15);
                }
            g.setColor(c);
        }
    }

    @Override
    public void bind(DrawManager d) {
        System.out.println("debug.");
        drawManager = d;
    }

    @Override
    public long getDrawID() {
        return -1;
    }

    @Override
    public void setLayer(int layer) {

    }

    @Override
    public int getLayer() {
        return 4;
    }

    @Override
    public void setDrawn(boolean isDrawn) {
        this.isDrawn = isDrawn;
    }

    @Override
    public boolean isDrawn() {
        return isDrawn;
    }
}
