package com.masterlong.framework.essentials;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏主窗体
 * v 1.5
 * 用户需要自行继承并于构造函数设置游戏场景
 */
public class GameFrame extends JFrame {

    protected GameView gameView, emptyView;

    public GameFrame(String str, int viewPortW, int viewPortH) {
        super(str);
        setSize(viewPortW, viewPortH);//设置窗口大小
        setDefaultCloseOperation(EXIT_ON_CLOSE);//默认关闭
        setBackground(Color.BLACK);//设置背景色
        //setUndecorated(true);//去掉窗口边框
        //setResizable(false);//禁止缩放窗口
        setVisible(true);//窗口可见
    }


    public void setGameView(GameView gameView) {
        this.gameView = gameView;
        gameView.gameFrame = this;
        setContentPane(gameView);
        //强制更新组件
        revalidate();
    }

}
