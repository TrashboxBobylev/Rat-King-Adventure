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

package com.zrp200.rkpd2.items;

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.buffs.AscensionChallenge;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.levels.features.LevelTransition;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.AmuletScene;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.InterlevelScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.io.IOException;
import java.util.ArrayList;

public class Amulet extends Item {
	
	private static final String AC_END = "END";
	
	{
		image = ItemSpriteSheet.AMULET;
		
		unique = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.buff(AscensionChallenge.class) != null){
			actions.clear();
			if (hero.isClassedLoosely(HeroClass.RAT_KING))
				actions.add(AC_THROW);
		} else {
			actions.add(AC_END);
		}
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_END)) {
			showAmuletScene( false );
		}
	}

	@Override
	protected void onThrow(int cell) {
		if (Dungeon.level.pit[cell] || Dungeon.hero.heroClass != HeroClass.RAT_KING){
			super.onThrow( cell );
		} else {
			Dungeon.level.pressCell( cell );
			Sample.INSTANCE.play(Assets.Sounds.SHATTER);
			GameScene.flash(0xFFFFFF, true);
			InterlevelScene.curTransition = new LevelTransition(Dungeon.level, -1, LevelTransition.Type.REGULAR_ENTRANCE, 0, Dungeon.branch, LevelTransition.Type.REGULAR_EXIT);
			Statistics.deepestFloor = -1;
			InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
			Game.switchScene(InterlevelScene.class);
			Invisibility.dispel();
		}
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		Badges.validateRatKingUnlock();
		if (super.doPickUp( hero, pos )) {
			
			if (!Statistics.amuletObtained) {
				Statistics.amuletObtained = true;
				hero.spend(-TIME_TO_PICK_UP);

				//delay with an actor here so pickup behaviour can fully process.
				Actor.add(new Actor(){

					{
						actPriority = VFX_PRIO;
					}

					@Override
					protected boolean act() {
						Actor.remove(this);
						showAmuletScene( true );
						return false;
					}
				});
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private void showAmuletScene( boolean showText ) {
		AmuletScene.noText = !showText;
		Game.switchScene( AmuletScene.class, new Game.SceneChangeCallback() {
			@Override
			public void beforeCreate() {

			}

			@Override
			public void afterCreate() {
				Badges.validateVictory();
				Badges.validateSecretMastery();
					Badges.validateChampion(Challenges.activeChallenges(), Dungeon.challenges);
				try {
					Dungeon.saveAll();
					Badges.saveGlobal();
				} catch (IOException e) {
					ShatteredPixelDungeon.reportException(e);
				}
			}
		});
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (Dungeon.hero.buff(AscensionChallenge.class) == null){
			desc += "\n\n" + Messages.get(this, "desc_origins");
		} else {
			desc += "\n\n" + Messages.get(this, "desc_ascent");
		}

		return desc;
	}
}
