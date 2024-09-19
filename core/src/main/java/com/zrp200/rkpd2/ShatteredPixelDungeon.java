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

import com.badlogic.gdx.utils.StringBuilder;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.scenes.TitleScene;
import com.zrp200.rkpd2.scenes.WelcomeScene;

import static com.watabou.utils.Bundle.addAlias;

@SuppressWarnings("unused")
public class ShatteredPixelDungeon extends Game {

	//variable constants for specific older versions of shattered, used for data conversion
	public static final int
			RKA_2_0 = 803,
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
	//versions older than v1.2.3 are no longer supported, and data from them is ignored
	public static final int v1_2_3  = v0_3_0/*628*/; //v1.2.3 is kept for now, for old rankings score logic
	public static final int v1_3_2  = V1_0_0/*648*/;
	public static final int v1_4_3  = 668;


	public static final int v2_0_2 = 700;
	public static final int v2_3_0 = V2_0_0;


    public ShatteredPixelDungeon( PlatformSupport platform ) {
		super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );

		//pre-v2.3.0
		addAlias(
				com.zrp200.rkpd2.actors.buffs.Bleeding.class,
				"com.zrp200.rkpd2.levels.features.Chasm$FallBleed" );
		addAlias(
				com.zrp200.rkpd2.items.bombs.Bomb.ConjuredBomb.class,
				"com.zrp200.rkpd2.items.bombs.Bomb$MagicalBomb" );

		//pre-v2.2.0
		addAlias(
				com.zrp200.rkpd2.items.weapon.curses.Dazzling.class,
				"com.zrp200.rkpd2.items.weapon.curses.Exhausting" );
		addAlias(
				com.zrp200.rkpd2.items.weapon.curses.Explosive.class,
				"com.zrp200.rkpd2.items.weapon.curses.Fragile" );

		//pre-v1.2.0
		addAlias(
				com.zrp200.rkpd2.items.weapon.missiles.darts./*CleansingDart*/DreamDart.class,
				"com.zrp200.rkpd2.items.weapon.missiles.darts.SleepDart" );

		addAlias(
				com.zrp200.rkpd2.levels.rooms.special.CrystalVaultRoom.class,
				"com.zrp200.rkpd2.levels.rooms.special.VaultRoom" );

		// no idea if this is needed.
		addAlias(com.zrp200.rkpd2.actors.buffs.SoulMark.class,
				"com.zrp200.rkpd2.actors.buffs.SoulMark.DelayedMark");

		//pre-v1.1.0
		addAlias(
				com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfDread.class,
				"com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfPetrification" );
		addAlias(
				com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfSirensSong.class,
				"com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfAffection" );
		addAlias(
				com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfChallenge.class,
				"com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfConfusion" );
		addAlias(
				com.zrp200.rkpd2.items.potions.exotic.PotionOfDivineInspiration.class,
				"com.zrp200.rkpd2.items.potions.exotic.PotionOfHolyFuror" );
		addAlias(
				com.zrp200.rkpd2.items.potions.exotic.PotionOfMastery.class,
				"com.zrp200.rkpd2.items.potions.exotic.PotionOfAdrenalineSurge" );
		addAlias(
				ScrollOfMetamorphosis.class,
				"com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfPolymorph" );

		addAlias(
				com.zrp200.rkpd2.levels.rooms.quest.BlacksmithRoom.QuestEntrance.class,
				"com.zrp200.rkpd2.levels.rooms.standard.BlacksmithRoom$QuestEntrance" );
		addAlias(
				com.zrp200.rkpd2.levels.rooms.quest.BlacksmithRoom.class,
				"com.zrp200.rkpd2.levels.rooms.standard.BlacksmithRoom" );
		addAlias(
				com.zrp200.rkpd2.levels.rooms.quest.MassGraveRoom.class,
				"com.zrp200.rkpd2.levels.rooms.special.MassGraveRoom" );
		addAlias(
				com.zrp200.rkpd2.levels.rooms.quest.MassGraveRoom.Bones.class,
				"com.zrp200.rkpd2.levels.rooms.special.MassGraveRoom$Bones" );
		addAlias(
				com.zrp200.rkpd2.levels.rooms.quest.RitualSiteRoom.class,
				"com.zrp200.rkpd2.levels.rooms.standard.RitualSiteRoom" );
		addAlias(
				com.zrp200.rkpd2.levels.rooms.quest.RitualSiteRoom.RitualMarker.class,
				"com.zrp200.rkpd2.levels.rooms.standard.RitualSiteRoom$RitualMarker" );
		addAlias(
				com.zrp200.rkpd2.levels.rooms.quest.RotGardenRoom.class,
				"com.zrp200.rkpd2.levels.rooms.special.RotGardenRoom" );
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
		StringBuilder builder = new StringBuilder();
		for (char lol : name.toCharArray()) {
			if (Character.isLetter(lol))
				builder.append('r');
			else
				builder.append(lol);
		}
		name = builder.toString();
		return name;
	}
}