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

package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.Door;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.utils.GLog;

public class Rapier extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {

	{
		image = ItemSpriteSheet.RAPIER;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.3f;

		tier = 1;

		bones = false;
	}

	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //8 base, down from 10
				lvl*(tier+1);   //scaling unchanged
	}

	@Override
	public int defenseFactor( Char owner ) {
		return 1;	//1 extra defence
	}

	@Override
	public float warriorMod() {
		return super.warriorMod()*3f;
	}

	@Override
	public float warriorDelay() {
		return 0f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(3+lvl) damage, equivalent to +67% damage, but more consistent
		int dmgBoost = augment.damageFactor(3 + level());
		lungeAbility(hero, target, 1, dmgBoost, this);
	}

	public static void lungeAbility(Hero hero, Integer target, float dmgMulti, int dmgBoost, MeleeWeapon wep){
		if (target == null){
			return;
		}

		int maxDistance = hero.heroClass.isExact(HeroClass.DUELIST) ? 3 : 2;
		int actualDistance = Dungeon.level.distance(hero.pos, target);

		if (actualDistance == 1) {
			GLog.w(Messages.get(wep, "ability_bad_position"));
			return;
		}

		Char enemy = Actor.findChar(target);
		//duelist can lunge out of her FOV, but this wastes the ability instead of cancelling if there is no target
		if (Dungeon.level.heroFOV[target] && actualDistance == 2) {
			if (enemy == null || enemy == hero || hero.isCharmedBy(enemy)) {
				GLog.w(Messages.get(wep, "ability_no_target"));
				return;
			}
		}

		int bestDistance = Integer.MAX_VALUE;


		if (hero.rooted || actualDistance > maxDistance
				|| actualDistance-(maxDistance -1) > wep.reachFactor(hero)){
			GLog.w(Messages.get(wep, "ability_bad_position"));
			if (hero.rooted) PixelScene.shake( 1, 1f );
			return;
		}

		// todo use ballistica like the other similar mechanics

		int lungeCell = -1;
		int lungeCell2 = -1;
		for (int i : PathFinder.NEIGHBOURS8){
			int pos = hero.pos+i;
			if (Dungeon.level.distance(pos, target) <= wep.reachFactor(hero)
					&& Actor.findChar(hero.pos+i) == null
					&& (Dungeon.level.passable[hero.pos+i] || (Dungeon.level.avoid[hero.pos+i] && hero.flying))){
				if (lungeCell == -1 || Dungeon.level.trueDistance(hero.pos + i, target) < Dungeon.level.trueDistance(lungeCell, target)){
					lungeCell = hero.pos + i;
				}
			}
			if (maxDistance == 3 && Dungeon.level.passable[pos] && Actor.findChar(pos) == null) {
				for (int j : PathFinder.NEIGHBOURS8) {
					if (i+j == 0) continue; // already checked
					int newPos = pos+j;
					if (Dungeon.level.distance(newPos, target) <= wep.reachFactor(hero)
							&& Actor.findChar(newPos) == null
							&& (Dungeon.level.passable[newPos] || (Dungeon.level.avoid[newPos] && hero.flying))) {
						if (lungeCell2 == -1 || Dungeon.level.trueDistance(newPos, target) < Dungeon.level.trueDistance(lungeCell2, target)) {
							lungeCell2 = newPos;
						}
					}
				}
			}
		}
		if (lungeCell == -1) lungeCell = lungeCell2;

		if (lungeCell == -1){
			GLog.w(Messages.get(wep, "ability_bad_position"));
			return;
		}

		final int dest = lungeCell;

		hero.busy();
		Sample.INSTANCE.play(Assets.Sounds.MISS);
		hero.sprite.jump(hero.pos, dest, 0, 0.1f, new Callback() {
			@Override
			public void call() {
				if (Dungeon.level.map[hero.pos] == Terrain.OPEN_DOOR) {
					Door.leave( hero.pos );
				}
				hero.pos = dest;
				Dungeon.level.occupyCell(hero);
				Dungeon.observe();

				hero.belongings.abilityWeapon = wep; //set this early to we can check canAttack
				if (enemy != null && hero.canAttack(enemy)) {
					hero.sprite.attack(enemy.pos, new Callback() {
						@Override
						public void call() {

							wep.beforeAbilityUsed(hero, enemy);
							AttackIndicator.target(enemy);
							if (hero.attack(enemy, dmgMulti, dmgBoost, Char.INFINITE_ACCURACY)) {
								Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
								if (!enemy.isAlive()) {
									wep.onAbilityKill(hero, enemy);
								}
							}
							Invisibility.dispel();
							hero.spendAndNext(hero.attackDelay());
							wep.afterAbilityUsed(hero);
						}
					});
				} else {
					wep.beforeAbilityUsed(hero, null);
					GLog.w(Messages.get(Rapier.class, "ability_no_target"));
					hero.spendAndNext(1/hero.speed());
					wep.afterAbilityUsed(hero);
				}
			}
		});
	}
}
