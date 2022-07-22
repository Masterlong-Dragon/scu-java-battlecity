package com.masterlong.framework.essentials;

import com.masterlong.framework.essentials.resources.ResourceHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * GameView视图
 */
public class GameView extends JPanel {

    protected ResourceHandler resourceHandler;//资源管理器
    protected SceneManager sceneManager;//场景管理器
    protected GameFrame gameFrame;

    public GameView(int viewPortW, int viewPortH, ResourceHandler resourceHandler) {
        super();
        setPreferredSize(new Dimension(viewPortW, viewPortH));//设置窗口大小

        this.resourceHandler = resourceHandler;
        sceneManager = SingletonManager.getSceneManager();
        sceneManager.init(this);
        //缩放窗口事件
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                sceneManager.adjustViewPort();
            }
        });
        //setResizable(false);//禁止缩放窗口
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    @Override
    public void paintComponent(Graphics g) {
        //super.paint(g);
        //开始绘制
        sceneManager.drawScene(g);
    }
}