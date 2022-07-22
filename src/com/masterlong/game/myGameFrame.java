package com.masterlong.game;

import com.masterlong.framework.essentials.*;
import com.masterlong.game.resources.Resource;
import com.masterlong.game.scenes.MainScene;

import java.awt.*;

/**
 * 主窗口
 */
public class myGameFrame extends GameFrame {

    public myGameFrame(String str, int w, int h) {
        super(str, w, h);
        setBackground(Color.BLACK);
        gameView = new GameView(w, h, Resource.getResourceHandlerInstance());
        setGameView(gameView);
        Scene scene1 = new MainScene(gameView);
        Scene scene2 = new MainScene(gameView);
        SceneManager sceneManager = SingletonManager.getSceneManager();
        sceneManager.addScene(scene1, "1");
        sceneManager.addScene(scene2, "2");
        sceneManager.setActiveScene("1", false, true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //只是用来抹平底下莫名其妙的白线
        g.drawLine(0, getHeight() - 7, getWidth(), getHeight() - 7);
    }
}
