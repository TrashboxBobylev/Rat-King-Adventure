/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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
import com.zrp200.rkpd2.scenes.TitleScene;
import com.zrp200.rkpd2.scenes.WelcomeScene;
import com.badlogic.gdx.utils.StringBuilder;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Random;

@SuppressWarnings("unused")
public class ShatteredPixelDungeon extends Game {

	//variable constants for specific older versions of shattered, used for data conversion
	public static final int
			RKA_2_2 = 847,
			RKA_2_0 = 803,
			V3_0_0=839,
			V2_0_0=765,
			V1_0_0=653,
			v0_3_0=616,
			v0_2_0=597,
			vDLC_1_5=777,
			vDLC_1_4_10=623,
			vDLC_1_4=600,
			vDLC_1_3=575,
			vDLC_1_2=560,
			v0_1_1=559,
			v0_1_0=557,
			v0_0_1=551,
			v0_0_0=550;

	// shattered versions
	//rankings from v1.2.3 and older use a different score formula, so this reference is kept
	public static final int v1_2_3  = v0_3_0/*628*/; //v1.2.3 is kept for now, for old rankings score logic
	public static final int v1_3_2  = V1_0_0/*648*/;
	public static final int v1_4_3  = 668;

	public static final int v2_3_2 = V2_0_0;
	public static final int v2_4_2 = 782;
	public static final int v2_5_4 = 802;
	//savegames from versions older than v2.3.2 are no longer supported, and data from them is ignored

	public static final int v2_0_2 = 700;
	public static final int v2_3_0 = V2_0_0;
public static final int v3_0_0 = 831;


    public ShatteredPixelDungeon( PlatformSupport platform ) {
		super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );

		//pre-v2.5.3
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.stones.StoneOfDetectMagic.class,
				"com.zrp200.rkpd2.items.stones.StoneOfDisarming" );

		//pre-v2.5.2
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.bombs.FlashBangBomb.class,
				"com.zrp200.rkpd2.items.bombs.ShockBomb" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.bombs.SmokeBomb.class,
				"com.zrp200.rkpd2.items.bombs.Flashbang" );

		//pre-v2.5.0
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.actors.mobs.MobSpawner.class,
				"com.zrp200.rkpd2.levels.Level$Respawner" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.actors.buffs.Invulnerability.class,
				"com.zrp200.rkpd2.actors.buffs.AnkhInvulnerability" );

		//pre-v2.4.0
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.potions.brews.UnstableBrew.class,
				"com.zrp200.rkpd2.items.potions.AlchemicalCatalyst" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.spells.UnstableSpell.class,
				"com.zrp200.rkpd2.items.spells.ArcaneCatalyst" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.potions.elixirs.ElixirOfFeatherFall.class,
				"com.zrp200.rkpd2.items.spells.FeatherFall" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.potions.elixirs.ElixirOfFeatherFall.FeatherBuff.class,
				"com.zrp200.rkpd2.items.spells.FeatherFall$FeatherBuff" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.items.potions.brews.AquaBrew.class,
				"com.zrp200.rkpd2.items.spells.AquaBlast" );

		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.rooms.standard.entrance.EntranceRoom.class,
				"com.zrp200.rkpd2.levels.rooms.standard.EntranceRoom" );
		com.watabou.utils.Bundle.addAlias(
				com.zrp200.rkpd2.levels.rooms.standard.exit.ExitRoom.class,
				"com.zrp200.rkpd2.levels.rooms.standard.ExitRoom" );
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

	@Override
	public void finish() {
		if (!DeviceCompat.isiOS()) {
			super.finish();
		} else {
			//can't exit on iOS (Apple guidelines), so just go to title screen
			switchScene(TitleScene.class);
		}
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

	public static String turnIntoRrrr(String name){
		StringBuilder superBuilder = new StringBuilder();
		for (String word: Game.platform.splitforTextBlock(name, true)){
			StringBuilder builder = new StringBuilder();
			char[] letters = word.toCharArray();
			for (int i = 0; i < letters.length; i++){
				if (i == 0 && Character.isLetter(letters[i]) && letters[i] != 'x' && Random.Int(3) == 0){
					builder.append(Character.isUpperCase(letters[i]) ? "R" : "r");
					if ("AEIOUaeiou".indexOf(letters[i]) != -1){
						builder.append(letters[i]);
					}
				} else {
					builder.append(letters[i]);
				}
			}
			superBuilder.append(builder.toString());
		}
		name = superBuilder.toString();
		return name;
	}
}