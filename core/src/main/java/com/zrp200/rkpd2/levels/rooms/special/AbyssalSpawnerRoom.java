package com.zrp200.rkpd2.levels.rooms.special;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.mobs.AbyssalSpawner;
import com.zrp200.rkpd2.actors.mobs.DemonSpawner;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.levels.rooms.Room;
import com.zrp200.rkpd2.levels.rooms.standard.entrance.EntranceRoom;
import com.zrp200.rkpd2.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

public class AbyssalSpawnerRoom extends SpecialRoom {
    @Override
    public void paint(Level level) {

        Painter.fill( level, this, Terrain.WALL );
        Painter.fill( level, this, 1, Terrain.EMPTY );

        Point c = center();
        int cx = c.x;
        int cy = c.y;

        Door door = entrance();
        door.set(Door.Type.UNLOCKED);

        AbyssalSpawner spawner = new AbyssalSpawner();
        spawner.pos = cx + cy * level.width();
        level.mobs.add( spawner );

        CustomFloor vis = new CustomFloor();
        vis.setRect(left+1, top+1, width()-2, height()-2);
        level.customTiles.add(vis);

    }

    @Override
    public boolean connect(Room room) {
        //cannot connect to entrance, otherwise works normally
        if (room instanceof EntranceRoom) return false;
        else                              return super.connect(room);
    }

    @Override
    public boolean canPlaceTrap(Point p) {
        return false;
    }

    @Override
    public boolean canPlaceWater(Point p) {
        return false;
    }

    @Override
    public boolean canPlaceGrass(Point p) {
        return false;
    }

    public static class CustomFloor extends CustomTilemap {

        {
            texture = Assets.Environment.ABYSS_SP;
        }

        @Override
        public Tilemap create() {
            Tilemap v = super.create();
            int cell = tileX + tileY * Dungeon.level.width();
            int[] map = Dungeon.level.map;
            int[] data = new int[tileW*tileH];
            for (int i = 0; i < data.length; i++){
                if (i % tileW == 0){
                    cell = tileX + (tileY + i / tileW) * Dungeon.level.width();
                }

                if (Dungeon.level.findMob(cell) instanceof DemonSpawner){
                    data[i-1] = 5 + 4*8;
                    data[i] = 6 + 4*8;
                    data[i+1] = 7 + 4*8;
                    i++;
                    cell++;
                } else if (map[cell] == Terrain.EMPTY_DECO) {
                    if (Statistics.amuletObtained){
                        data[i] = 31;
                    } else {
                        data[i] = 27;
                    }
                } else {
                    data[i] = 19;
                }

                cell++;
            }
            v.map( data, tileW );
            return v;
        }

    }
}
