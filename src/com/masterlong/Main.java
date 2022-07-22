package com.masterlong;

import com.masterlong.framework.essentials.GameFrame;
import com.masterlong.game.myGameFrame;

/**
 * 仅作为程序入口的主类
 */
public class Main {

    public static void main(String[] args) {
        // write your code here
        // 主游戏视图
        GameFrame frame = new myGameFrame("坦克大战", 800, 600 + 20);
    }
}
