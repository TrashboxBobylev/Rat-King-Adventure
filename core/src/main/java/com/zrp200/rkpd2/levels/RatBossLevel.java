/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.levels;

import com.watabou.glwrap.Blending;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Bones;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.GamesInProgress;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.RatKingBoss;
import com.zrp200.rkpd2.actors.mobs.YogDzewa;
import com.zrp200.rkpd2.actors.mobs.npcs.RatKing;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.particles.FlameParticle;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.levels.features.LevelTransition;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.SurfaceScene;
import com.zrp200.rkpd2.tiles.CustomTilemap;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.utils.DungeonSeed;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Assets.Environment.TILES_HALLS;
import static com.zrp200.rkpd2.Assets.Environment.WATER_HALLS;

public class RatBossLevel extends Level {

	{
		color1 = 0x801500;
		color2 = 0xa68521;

		viewDistance = 8;
	}

	private static final int WIDTH = 40;
	private static final int HEIGHT = 40;

	private static final int ROOM_LEFT		= WIDTH / 2 - 4;
	private static final int ROOM_RIGHT		= WIDTH / 2 + 4;
	private static final int ROOM_TOP		= 8;
	private static final int ROOM_BOTTOM	= ROOM_TOP + 8;

	@Override
	public String tilesTex() {
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.REVERSE))
			return TILES_HALLS;
		return Assets.Environment.TILES_SEWERS;
	}

	@Override
	public String waterTex() {
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.REVERSE))
			return WATER_HALLS;
		return Assets.Environment.WATER_SEWERS;
	}

	@Override
	public int entrance() {
		LevelTransition l = getTransition(LevelTransition.Type.REGULAR_EXIT);
		if (l != null){
			return l.cell();
		}
		return 0;
	}

	@Override
	public int exit() {
		LevelTransition l = getTransition(LevelTransition.Type.SURFACE);
		if (l != null){
			return l.cell();
		}
		return 0;
	}

	@Override
	protected boolean build() {

		setSize(WIDTH, HEIGHT);

		for (int i = 0; i < 5; i++) {

			int top;
			int bottom;

			if (i == 0 || i == 4){
				top = Random.IntRange(ROOM_TOP-1, ROOM_TOP+3);
				bottom = Random.IntRange(ROOM_BOTTOM+2, ROOM_BOTTOM+6);
			} else if (i == 1 || i == 3){
				top = Random.IntRange(ROOM_TOP-5, ROOM_TOP-1);
				bottom = Random.IntRange(ROOM_BOTTOM+6, ROOM_BOTTOM+10);
			} else {
				top = Random.IntRange(ROOM_TOP-6, ROOM_TOP-3);
				bottom = Random.IntRange(ROOM_BOTTOM+8, ROOM_BOTTOM+12);
			}

			Painter.fill(this, 4 + i * 5, top, 5, bottom - top + 1, Terrain.EMPTY);

			if (i == 2) {
				int entrance = (6 + i * 5) + (bottom - 1) * width();
				transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_EXIT));
			}

		}

		boolean[] patch = Patch.generate(width, height, 0.10f, 0, true);
		for (int i = 0; i < length(); i++) {
			if (map[i] == Terrain.EMPTY && patch[i]) {
				map[i] = Terrain.STATUE;
			}
		}

		map[entrance()] = Terrain.EXIT;

		Painter.fill(this, ROOM_LEFT-1, ROOM_TOP-1, 11, 11, Terrain.EMPTY );

		patch = Patch.generate(width, height, 0.20f, 3, true);
		for (int i = 0; i < length(); i++) {
			if ((map[i] == Terrain.EMPTY || map[i] == Terrain.STATUE) && patch[i]) {
				map[i] = Terrain.WATER;
			}
		}

		for (int i = 0; i < length(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int(4) == 0) {
				map[i] = Terrain.EMPTY_DECO;
			}
		}

		Painter.fill(this, ROOM_LEFT, ROOM_TOP, 9, 9, Terrain.EMPTY_SP );

		Painter.fill(this, ROOM_LEFT, ROOM_TOP, 9, 2, Terrain.WALL_DECO );
		Painter.fill(this, ROOM_LEFT, ROOM_BOTTOM-1, 2, 2, Terrain.WALL_DECO );
		Painter.fill(this, ROOM_RIGHT-1, ROOM_BOTTOM-1, 2, 2, Terrain.WALL_DECO );

		Painter.fill(this, ROOM_LEFT+3, ROOM_TOP+2, 3, 4, Terrain.EMPTY );

		int exitCell = width/2 + ((ROOM_TOP+1) * width);
		LevelTransition exit = new LevelTransition(this, exitCell, LevelTransition.Type.SURFACE);
		exit.top--;
		exit.left--;
		exit.right++;
		transitions.add(exit);

		CustomTilemap vis = new CenterPieceVisuals();
		vis.pos(ROOM_LEFT, ROOM_TOP+1);
		customTiles.add(vis);

		vis = new CenterPieceWalls();
		vis.pos(ROOM_LEFT, ROOM_TOP);
		customWalls.add(vis);

		//basic version of building flag maps for the pathfinder test
		for (int i = 0; i < length; i++){
			passable[i]	= ( Terrain.flags[map[i]] & Terrain.PASSABLE) != 0;
		}

		//ensures a path to the exit exists
		return (PathFinder.getStep(entrance(), exit(), passable) != -1);
	}

	@Override
	protected void createMobs() {
	}

	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createItems() {
		Random.pushGenerator(Random.Long());
		ArrayList<Item> bonesItems = Bones.get();
		if (bonesItems != null) {
			int pos;
			do {
				pos = randomRespawnCell(null);
			} while (pos == entrance());
			for (Item i : bonesItems) {
				drop(i, pos).setHauntedIfCursed().type = Heap.Type.REMAINS;
			}
		}
		Random.popGenerator();
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		int cell;
		do {
			cell = Random.Int( length() );
		} while ((Dungeon.level == this && heroFOV[cell])
				|| !passable[cell]
				|| (Char.hasProp(ch, Char.Property.LARGE) && !openSpace[cell])
				|| Actor.findChar( cell ) != null);
		return cell;
	}

	@Override
	public void occupyCell( Char ch ) {
		super.occupyCell( ch );

		if (map[entrance()] == Terrain.EXIT && map[exit()] != Terrain.ENTRANCE
				&& ch == Dungeon.hero && Dungeon.level.distance(ch.pos, entrance()) >= 2) {
			seal();
		}
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition.type == LevelTransition.Type.SURFACE){
			Statistics.ascended = true;
			Badges.silentValidateHappyEnd();
			Dungeon.win( RatKing.class );
			Dungeon.deleteGame( GamesInProgress.curSlot, true );
			Game.switchScene( SurfaceScene.class );
			return true;
		} else {
			return super.activateTransition(hero, transition);
		}
	}

	@Override
	public void seal() {
		super.seal();
		int entrance = entrance();
		set( entrance, Terrain.EMPTY_SP );
		GameScene.updateMap( entrance );
		CellEmitter.get( entrance ).start( FlameParticle.FACTORY, 0.1f, 10 );

		Dungeon.observe();

		RatKingBoss boss = new RatKingBoss();
		ChampionEnemy.rollWarp(boss);
		boss.pos = exit() + width*3;
		GameScene.add( boss );
	}

	@Override
	public void unseal() {
		super.unseal();
		set( entrance(), Terrain.ENTRANCE );
		GameScene.updateMap( entrance() );

		set( exit(), Terrain.EXIT );
		GameScene.updateMap( exit() );

		CellEmitter.get(exit()-1).burst(ShadowParticle.UP, 25);
		CellEmitter.get(exit()).burst(ShadowParticle.UP, 100);
		CellEmitter.get(exit()+1).burst(ShadowParticle.UP, 25);
		for( CustomTilemap t : customTiles){
			if (t instanceof CenterPieceVisuals){
				((CenterPieceVisuals) t).updateState();
			}
		}
		for( CustomTilemap t : customWalls){
			if (t instanceof CenterPieceWalls){
				((CenterPieceWalls) t).updateState();
			}
		}

		Dungeon.observe();
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		for (Mob m : mobs){
			if (m instanceof YogDzewa){
				((YogDzewa) m).updateVisibility(this);
			}
		}
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.WATER:
				return Messages.get(RatBossLevel.class, "water_name");
			case Terrain.GRASS:
				return Messages.get(SewerLevel.class, "grass_name");
			case Terrain.HIGH_GRASS:
				return Messages.get(SewerLevel.class, "high_grass_name");
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Messages.get(SewerLevel.class, "statue_name");
			default:
				return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
			case Terrain.WATER:
				return Messages.get(RatBossLevel.class, "water_desc");
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Messages.get(SewerLevel.class, "statue_desc");
			case Terrain.BOOKSHELF:
				return Messages.get(SewerLevel.class, "bookshelf_desc");
			default:
				return super.tileDesc( tile );
		}
	}

	@Override
	public Group addVisuals () {
		super.addVisuals();
		addHallsVisuals( this, visuals );
		return visuals;
	}

	public static void addHallsVisuals( Level level, Group group ) {
		for (int i=0; i < level.length(); i++) {
			if (level.map[i] == Terrain.WATER) {
				group.add( new Stream( i ) );
			}
		}
	}

	private static class Stream extends Group {

		private int pos;

		private float delay;

		public Stream( int pos ) {
			super();

			this.pos = pos;

			delay = Random.Float( 2 );
		}

		@Override
		public void update() {

			if (!Dungeon.level.water[pos]){
				killAndErase();
				return;
			}

			if (visible = (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {

				super.update();

				if ((delay -= Game.elapsed) <= 0) {

					delay = Random.Float( 1 );

					PointF p = DungeonTilemap.tileToWorld( pos );
					recycle( FireParticle.class ).reset(
							p.x + Random.Float( DungeonTilemap.SIZE ),
							p.y + Random.Float( DungeonTilemap.SIZE ) );
				}
			}
		}

		@Override
		public void draw() {
			Blending.setLightMode();
			super.draw();
			Blending.setNormalMode();
		}
	}

	public static class FireParticle extends PixelParticle.Shrinking {

		public FireParticle() {
			super();

			color( 0x4a7561 );
			lifespan = 1.5f;

			acc.set( 0, +100 );
		}

		public void reset( float x, float y ) {
			revive();

			this.x = x;
			this.y = y;

			left = lifespan;

			speed.set( 0, -40 );
			size = 4;
		}

		@Override
		public void update() {
			super.update();
			float p = left / lifespan;
			am = p > 0.8f ? (1 - p) * 6 : 1;
		}
	}

	public static class CenterPieceVisuals extends CustomTilemap {

		{
			texture = Assets.Environment.SEWERS_SP;

			tileW = 9;
			tileH = 8;
		}

		private static final int[] map = new int[]{
				 8,  9, 10, 11, 11, 11, 12, 13, 14,
				16, 17, 18, 27, 19, 27, 20, 21, 22,
				24, 25, 26, 19, 19, 19, 28, 29, 30,
				24, 25, 26, 19, 19, 19, 28, 29, 30,
				24, 25, 26, 19, 19, 19, 28, 29, 30,
				24, 25, 34, 35, 35, 35, 34, 29, 30,
				40, 41, 36, 36, 36, 36, 36, 40, 41,
				48, 49, 36, 36, 36, 36, 36, 48, 49
		};

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			updateState();
			return v;
		}

		private void updateState(){
			if (vis != null){
				int[] data = map.clone();
				if (Dungeon.level.map[Dungeon.level.entrance] == Terrain.ENTRANCE) {
					data[4] = 19;
					data[12] = data[14] = 31;
				}
				vis.map(data, tileW);
			}
		}
	}

	public static class CenterPieceWalls extends CustomTilemap {

		{
			texture = Assets.Environment.SEWERS_SP;

			tileW = 9;
			tileH = 9;
		}

		private static final int[] map = new int[]{
				-1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1,
				32, 33, -1, -1, -1, -1, -1, 32, 33,
				40, 41, -1, -1, -1, -1, -1, 40, 41,
		};

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			updateState();
			return v;
		}

		private void updateState(){
			if (vis != null){
				int[] data = map.clone();
				if (Dungeon.level.map[Dungeon.level.entrance] == Terrain.ENTRANCE) {
					data[3] = 1;
					data[4] = 0;
					data[5] = 2;
					data[13] = 23;
				}
				vis.map(data, tileW);
			}
		}

	}
}
