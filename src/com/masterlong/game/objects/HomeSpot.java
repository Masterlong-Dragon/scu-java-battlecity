package com.masterlong.game.objects;

import com.masterlong.framework.essentials.GameItem;
import com.masterlong.framework.essentials.resources.ImgArea;
import com.masterlong.framework.math.Direction;
import com.masterlong.game.objects.mapsys.Tile;
import com.masterlong.game.objects.mapsys.TileMapSys;
import com.masterlong.game.resources.Resource;

import java.awt.*;

public class HomeSpot extends GameItem {

    private Direction homePos;

    public HomeSpot(Direction pos) {
        super(pos, Resource.getResourceHandlerInstance());
    }

    @Override
    public void init() {
        super.init();
        imgArea = (ImgArea) resourceHandler.getResource("homeSpot");
        int brickWidth = ((ImgArea) resourceHandler.getResource("brickImg")).dx;
        TileMapSys tileMapSys = (TileMapSys) scene.findComponent("tileMapSys");
        for (int i = 20; i < 26; i++)
            tileMapSys.addTile(Tile.TileType.BRICK, i, 30);
        for(int i = 31; i < 35; i++)
            tileMapSys.addTile(Tile.TileType.BRICK,20, i);
        for(int i = 31; i < 35; i++)
            tileMapSys.addTile(Tile.TileType.BRICK,25, i);
        homePos = new Direction(pos.x + brickWidth, pos.y + brickWidth);
    }

    @Override
    public void draw(Graphics g) {
        resourceHandler.draw(g, homePos.x, homePos.y, imgArea);
    }
}
