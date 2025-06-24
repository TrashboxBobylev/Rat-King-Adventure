package com.zrp200.rkpd2.levels;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.quest.Chaosstone;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.levels.painters.PrisonPainter;
import com.zrp200.rkpd2.levels.rooms.Room;
import com.zrp200.rkpd2.levels.rooms.special.AbyssalSpawnerRoom;
import com.zrp200.rkpd2.levels.traps.CorrosionTrap;
import com.zrp200.rkpd2.levels.traps.CursingTrap;
import com.zrp200.rkpd2.levels.traps.DisarmingTrap;
import com.zrp200.rkpd2.levels.traps.DisintegrationTrap;
import com.zrp200.rkpd2.levels.traps.DistortionTrap;
import com.zrp200.rkpd2.levels.traps.FlashingTrap;
import com.zrp200.rkpd2.levels.traps.FrostTrap;
import com.zrp200.rkpd2.levels.traps.GrimTrap;
import com.zrp200.rkpd2.levels.traps.GuardianTrap;
import com.zrp200.rkpd2.levels.traps.PitfallTrap;
import com.zrp200.rkpd2.levels.traps.RockfallTrap;
import com.zrp200.rkpd2.levels.traps.StormTrap;
import com.zrp200.rkpd2.levels.traps.WarpingTrap;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Halo;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class AbyssLevel extends RegularLevel {

    {
        color1 = 0x232424;
        color2 = 0x3e4040;
        viewDistance = 2;
    }

    public static final int BRANCH = 10;

    @Override
    public void playLevelMusic() {
        Music.INSTANCE.playTracks(
                new String[]{Assets.Music.ABYSS_1, Assets.Music.ABYSS_2, Assets.Music.ABYSS_3,
                        Assets.Music.ABYSS_4, Assets.Music.ABYSS_5},
                new float[]{1, 1, 1, 1, 0.25f},
                true);
    }

    @Override
    protected ArrayList<Room> initRooms() {
        ArrayList<Room> rooms = super.initRooms();

        rooms.add(new AbyssalSpawnerRoom());

        return rooms;
    }

    @Override
    public void create() {
        addItemToSpawn(Generator.random(Generator.Category.FOOD));
        addItemToSpawn( new com.zrp200.rkpd2.items.Torch() );
        for (int i = 0; i < GameMath.gate(1, Dungeon.scalingDepth() / 5 - 5
                - (Dungeon.scalingDepth() % 5 == 0 ? 1 : 0), Integer.MAX_VALUE); i++){
            addItemToSpawn(new Chaosstone());
            if (Random.Int(2) == 0) addItemToSpawn(new Chaosstone());
        }
        super.create();
    }

    @Override
    protected int standardRooms(boolean forceMax) {
        if (forceMax) return 18;
        return Math.max(30, 23 + Dungeon.depth/3)+Random.chances(new float[]{3, 2, 1, 1, 1});
    }

    @Override
    protected int specialRooms(boolean forceMax) {
        if (forceMax) return 3;
        //2 to 4, average 2.5
        return 2 + Random.chances(new float[]{1, 1});
    }

    public Actor addRespawner() {
        if (Dungeon.isChallenged(Challenges.KROMER)){
            return super.addRespawner();
        }
        return null;
    }

    @Override
    protected Painter painter() {
        return new PrisonPainter()
                .setWater(feeling == Feeling.WATER ? 0.90f : 0.30f, 4)
                .setGrass(feeling == Feeling.GRASS ? 0.80f : 0.20f, 3)
                .setTraps(nTraps(), trapClasses(), trapChances());
    }

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_ABYSS;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_ABYSS;
    }

    public static final Class[] trapClasses = {
            FrostTrap.class, StormTrap.class, CorrosionTrap.class, DisintegrationTrap.class,
            RockfallTrap.class, FlashingTrap.class, GuardianTrap.class,
            DisarmingTrap.class, WarpingTrap.class, CursingTrap.class, GrimTrap.class, PitfallTrap.class, DistortionTrap.class};

    @Override
    protected Class<?>[] trapClasses() {
        return trapClasses;
    }

    @Override
    protected float[] trapChances() {
        return new float[]{
                2, 2, 2, 2,
                2, 2, 2,
                1, 1, 1, 1, 1, 1 };
    }

    @Override
    public String tileName( int tile ) {
        switch (tile) {
            case Terrain.WATER:
                return Messages.get(AbyssLevel.class, "water_name");
            default:
                return super.tileName( tile );
        }
    }

    @Override
    public String tileDesc(int tile) {
        return Messages.get(AbyssLevel.class, "not_recognizable");
    }

    @Override
    public Group addVisuals() {
        super.addVisuals();
        addPrisonVisuals(this, visuals);
        return visuals;
    }

    public static void addPrisonVisuals(Level level, Group group){
        for (int i=0; i < level.length(); i++) {
            if (level.map[i] == Terrain.WALL_DECO) {
                group.add( new Torch( i ) );
            }
        }
    }

    public static class Torch extends Emitter {

        private int pos;

        public Torch( int pos ) {
            super();

            this.pos = pos;

            PointF p = DungeonTilemap.tileCenterToWorld( pos );
            pos( p.x - 1, p.y + 2, 2, 0 );

            pour( ShadowParticle.UP, 0.15f );

            add( new Halo( 12, 0x6b6b6b, 0.4f ).point( p.x, p.y + 1 ) );
        }

        @Override
        public void update() {
            if (visible = (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
                super.update();
            }
        }
    }
}