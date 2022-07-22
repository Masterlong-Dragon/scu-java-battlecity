package com.masterlong.framework.essentials;

import java.util.Random;

/**
 * 没有什么用 就是练习一下单例模式
 * (其实也是有点用的，每个场景初始化不用通过传参获得单例对象)
 */
public class SingletonManager {
    // 基于枚举的单例设计模式
    private enum SingletonEnum {
        INSTANCE;
        private final SceneManager sceneManager;
        private final DrawManager drawManager;
        private final LogicManager logicManager;
        private final EventRegister eventRegister;
        private final Random random;

        SingletonEnum() {
            sceneManager = new SceneManager();
            drawManager = new DrawManager();
            logicManager = new LogicManager();
            eventRegister = new EventRegister();
            random = new Random();
            random.setSeed(System.currentTimeMillis());
        }

        private SceneManager getSceneManager() {
            return sceneManager;
        }

        private DrawManager getDrawManager() {
            return drawManager;
        }

        private LogicManager getLogicManager() {
            return logicManager;
        }

        private EventRegister getEventRegister() {
            return eventRegister;
        }

        private Random getRandom() {
            return random;
        }

    }

    public static SceneManager getSceneManager() {
        return SingletonEnum.INSTANCE.getSceneManager();
    }

    public static DrawManager getDrawManager() {
        return SingletonEnum.INSTANCE.getDrawManager();
    }

    public static LogicManager getLogicManager() {
        return SingletonEnum.INSTANCE.getLogicManager();
    }

    public static EventRegister getEventRegister() {
        return SingletonEnum.INSTANCE.getEventRegister();
    }

    public static Random getRandom() {
        return SingletonEnum.INSTANCE.getRandom();
    }

}
