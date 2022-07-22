package com.masterlong.game.resources;

import com.masterlong.framework.components.Animation;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.essentials.resources.ResourceHandler;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.objects.mapsys.TileMapSys;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * 单例的资源管理器
 * 减少参数传递
 */
public class Resource extends ResourceHandler {

    //基于枚举的安全单例 (枚举类型可以避免多线程问题)
    private enum Singleton {
        INSTANCE;
        private final Resource instance;
        private final TileMapSys mapInstance;

        Singleton() {
            instance = new Resource("robots_sprite.png");
            mapInstance = new TileMapSys(47, 35, new Direction(9, 4));
        }

        private ResourceHandler getResourceHandlerInstance() {
            return instance;
        }

        private TileMapSys getMapInstance() {
            return mapInstance;
        }
    }

    public static ResourceHandler getResourceHandlerInstance() {
        return Singleton.INSTANCE.getResourceHandlerInstance();
    }

    public static TileMapSys getMapInstance() {
        return Singleton.INSTANCE.getMapInstance();
    }

    public Resource(String path) {
        super(path);
        File f = new File(path);
        try {
            res = ImageIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        staticResources.put("texture", res);
        //坦克动画
        ImgArea[][][] tankImgAreas = new ImgArea[8][4][2];
        Animation[][] tankAnimations = new Animation[8][4];
        for (int i = 0; i < 8; i++) {
            //4个方向两种状态
            //计算贴图位置
            for (int j = 0; j < 4; j++) {
                int x = i * 34 * 8 + j * 68;
                int y;
                if ((y = i / 4 * 34) == 34)
                    x -= 68 * 16;
                tankImgAreas[i][j][0] = new ImgArea(x, y, 34, 34, x + 34, y + 34);
                tankImgAreas[i][j][1] = new ImgArea(x + 34, y, 34, 34, x + 68, y + 34);
                tankAnimations[i][j] = new Animation(this);
                tankAnimations[i][j].addFrame(5, tankImgAreas[i][j][0]).addFrame(3, tankImgAreas[i][j][1]);
            }
        }
        //修正一个偏差
        //c2 left flag 0
        tankImgAreas[2][3][0].x += 1;
        tankImgAreas[2][3][0].sx += 1;
        //resources.put("tankTextures", tankImgAreas);
        staticResources.put("tankAnimations", tankAnimations);
        //子弹贴图
        staticResources.put("bulletTexture", new ImgArea(170, 204, 10, 10, 170 + 34, 204 + 34));
        //子弹爆炸
        Animation bulletExplosion = new Animation(this);
        for (int i = 0; i < 3; i++) {
            bulletExplosion.addFrame(6, new ImgArea((20 + i) * 34, 4 * 34, 38, 38, (20 + i + 1) * 34, 5 * 34));
        }
        staticResources.put("bulletExplosion", bulletExplosion);
        // 子弹爆炸二
        Animation bulletExplosion2 = new Animation(this);
        for (int i = 0; i < 4; i++) {
            bulletExplosion2.addFrame(6, new ImgArea((16 + i) * 34, 4 * 34, 38, 38, (16 + i + 1) * 34, 5 * 34));
        }
        staticResources.put("bulletExplosion2", bulletExplosion2);
        //坦克爆炸
        Animation tankExplosion = new Animation(this);
        for (int i = 0; i < 2; i++) {
            tankExplosion.addFrame(10, new ImgArea(23 * 34 + i * 68, 4 * 34, 68, 68, 23 * 34 + (i + 1) * 68, 4 * 34 + 68));
        }
        staticResources.put("tankExplosion", tankExplosion);
        //玩家坦克生成点
        ImgArea homeSpot = new ImgArea(34 * 19, 34 * 5, 17 * 4, 17 * 3, 34 * 20, 34 * 6);
        staticResources.put("homeSpot", homeSpot);
        //重生动画
        Animation rebirthAnimation = new Animation(this);
        for (int i = 0; i < 3; i++) {
            rebirthAnimation.addFrame(5, new ImgArea(13 * 34 + i * 34, 34 * 7, 34, 34, 13 * 34 + i * 34 + 34, 34 * 7 + 34));
        }
        staticResources.put("rebirthAnimation", rebirthAnimation);
        ////////////////////////////////////////////////////////////////////////////////////////////////
        //地图图块
        staticResources.put("brickImg", new ImgArea(34 * 18 + 1, 34 * 5 + 1, 17, 17, 34 * 18 + 34 / 2, 34 * 5 + 34 / 2));
        staticResources.put("grassImg", new ImgArea(34 * 4, 34 * 7, 34, 34, 34 * 5, 34 * 8));
        staticResources.put("stoneImg", new ImgArea(1, 34 * 6 + 1, 17, 17, 17, 34 * 6 + 17));
        //水
        Animation waterAnimation = new Animation(this);
        for (int i = 0; i < 2; i++) {
            waterAnimation.addFrame(25, new ImgArea(i * 34, 34 * 7, 34, 34, (i + 1) * 34, 34 * 8));
        }
        staticResources.put("waterAnimation", waterAnimation);
    }
}
