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

package com.zrp200.rkpd2;

import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.scenes.WelcomeScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PlatformSupport;

public class ShatteredPixelDungeon extends Game {

	//variable constants for specific older versions of shattered, used for data conversion
	public static final int
			v0_1_0=557,
			v0_0_1=551,
			v0_0_0=550;

	public static final int v0_7_5e = 382;

	//versions older than v0.8.0b are no longer supported, and data from them is ignored
	public static final int v0_8_0b = 414;
	public static final int v0_8_1a = 422;
	public static final int v0_8_2d = 463;

	public static final int v0_9_0b  = 489;
	public static final int v0_9_1d  = 511;
	public static final int v0_9_2b  = 532;
	public static final int v0_9_3   = 544;
	
	public ShatteredPixelDungeon( PlatformSupport platform ) {
		super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );

		//v0.9.3
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.actors.mobs.Tengu.class,
				"com.zrp200.rkpd2.actors.mobs.NewTengu" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.PrisonBossLevel.class,
				"com.zrp200.rkpd2.levels.NewPrisonBossLevel" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.PrisonBossLevel.ExitVisual.class,
				"com.zrp200.rkpd2.levels.NewPrisonBossLevel$exitVisual" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.PrisonBossLevel.ExitVisualWalls.class,
				"com.zrp200.rkpd2.levels.NewPrisonBossLevel$exitVisualWalls" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.actors.mobs.DM300.class,
				"com.zrp200.rkpd2.actors.mobs.NewDM300" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CavesBossLevel.class,
				"com.zrp200.rkpd2.levels.NewCavesBossLevel" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CavesBossLevel.PylonEnergy.class,
				"com.zrp200.rkpd2.levels.NewCavesBossLevel$PylonEnergy" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CavesBossLevel.ArenaVisuals.class,
				"com.zrp200.rkpd2.levels.NewCavesBossLevel$ArenaVisuals" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CavesBossLevel.CityEntrance.class,
				"com.zrp200.rkpd2.levels.NewCavesBossLevel$CityEntrance" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CavesBossLevel.EntranceOverhang.class,
				"com.zrp200.rkpd2.levels.NewCavesBossLevel$EntranceOverhang" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CityBossLevel.class,
				"com.zrp200.rkpd2.levels.NewCityBossLevel" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CityBossLevel.CustomGroundVisuals.class,
				"com.zrp200.rkpd2.levels.NewCityBossLevel$CustomGroundVisuals" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.CityBossLevel.CustomWallVisuals.class,
				"com.zrp200.rkpd2.levels.NewCityBossLevel$CustomWallVisuals" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.HallsBossLevel.class,
				"com.zrp200.rkpd2.levels.NewHallsBossLevel" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.HallsBossLevel.CenterPieceVisuals.class,
				"com.zrp200.rkpd2.levels.NewHallsBossLevel$CenterPieceWalls" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.HallsBossLevel.CenterPieceWalls.class,
				"com.zrp200.rkpd2.levels.NewHallsBossLevel$CenterPieceWalls" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.Waterskin.class,
				"com.zrp200.rkpd2.items.DewVial" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.TengusMask.class,
				"com.zrp200.rkpd2.items.TomeOfMastery" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.KingsCrown.class,
				"com.zrp200.rkpd2.items.ArmorKit" );
		
	}
	
	@Override
	public void create() {
		super.create();

		updateSystemUI();
		SPDAction.loadBindings();
		
		Music.INSTANCE.enable( SPDSettings.music() );
		Music.INSTANCE.volume( SPDSettings.musicVol()*SPDSettings.musicVol()/100f );
		Sample.INSTANCE.enable( SPDSettings.soundFx() );
		Sample.INSTANCE.volume( SPDSettings.SFXVol()*SPDSettings.SFXVol()/100f );

		Sample.INSTANCE.load( Assets.Sounds.all );
		
	}

	public static void switchNoFade(Class<? extends PixelScene> c){
		switchNoFade(c, null);
	}

	public static void switchNoFade(Class<? extends PixelScene> c, SceneChangeCallback callback) {
		PixelScene.noFade = true;
		switchScene( c, callback );
	}
	
	public static void seamlessResetScene(SceneChangeCallback callback) {
		if (scene() instanceof PixelScene){
			((PixelScene) scene()).saveWindows();
			switchNoFade((Class<? extends PixelScene>) sceneClass, callback );
		} else {
			resetScene();
		}
	}
	
	public static void seamlessResetScene(){
		seamlessResetScene(null);
	}
	
	@Override
	protected void switchScene() {
		super.switchScene();
		if (scene instanceof PixelScene){
			((PixelScene) scene).restoreWindows();
		}
	}
	
	@Override
	public void resize( int width, int height ) {
		if (width == 0 || height == 0){
			return;
		}

		if (scene instanceof PixelScene &&
				(height != Game.height || width != Game.width)) {
			PixelScene.noFade = true;
			((PixelScene) scene).saveWindows();
		}

		super.resize( width, height );

		updateDisplaySize();

	}
	
	@Override
	public void destroy(){
		super.destroy();
		GameScene.endActorThread();
	}
	
	public void updateDisplaySize(){
		platform.updateDisplaySize();
	}

	public static void updateSystemUI() {
		platform.updateSystemUI();
	}
}