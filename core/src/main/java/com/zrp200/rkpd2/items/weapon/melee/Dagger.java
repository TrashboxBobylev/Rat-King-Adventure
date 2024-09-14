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
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class Dagger extends MeleeWeapon {
	
	{
		image = ItemSpriteSheet.DAGGER;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.1f;

		tier = 1;
		
		bones = false;
	}

	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //8 base, down from 10
				lvl*(tier+1);   //scaling unchanged
	}

	protected float surpriseTowardMax = 0.75f;

	@Override
	public int damageRoll(Char owner) {
		if (owner instanceof Hero) {
			Hero hero = (Hero)owner;
			Char enemy = hero.enemy();
			if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)) {
				//deals 75% toward max to max on surprise, instead of min to max.
				int diff = max() - min();
				int damage = augment.damageFactor(Random.NormalIntRange(
						min() + Math.round(diff* surpriseTowardMax),
						max()));
				int exStr = hero.STR() - STRReq();
				if (exStr > 0) {
					damage += Random.IntRange(0, exStr);
				}
				return damage;
			}
		}
		return super.damageRoll(owner);
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(Dungeon.hero)){
			//doing same damage as YAPD's dagger
			// 7.2-21.6 (+2.4/+4.8)
			return Random.NormalIntRange(Math.round(7.2f + buffedLvl()*2.4f), Math.round(21.6f + buffedLvl() * 4.8f));
		}
		return super.warriorAttack(damage, enemy);
	}
	@Override
	public String targetingPrompt() {
		return Dungeon.hero.heroClass.is(HeroClass.DUELIST) ? Messages.get(this, "prompt") :
				null;
	}

	public boolean useTargeting(){
		return false;
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target){
		return 2;
	}

	// 6 dagger / 5 dirk
	// this is also the maximum invisibility time
	protected int maxDist() { return 6; }

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (target == null) {
			target = hero.pos;
		}

		Char ch = Actor.findChar(target);

		if (ch != hero && (ch != null || !Dungeon.level.heroFOV[target] || hero.rooted)) {
			GLog.w(Messages.get(this, "ability_bad_position"));
			if (Dungeon.hero.rooted) PixelScene.shake(1, 1f);
			return;
		}

		PathFinder.buildDistanceMap(Dungeon.hero.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null), maxDist());
		int distance = PathFinder.distance[target];
		if (distance == Integer.MAX_VALUE) {
			GLog.w(Messages.get(this, "ability_bad_position"));
			return;
		}

		beforeAbilityUsed(hero, null);
		// you can trade distance for longer invis if you want.
		Buff.affect(hero, Invisibility.class, Math.max(Actor.TICK, maxDist() - distance));
		hero.next();

		if (ch == null) {
			Dungeon.hero.sprite.turnTo(Dungeon.hero.pos, target);
			Dungeon.hero.pos = target;
			Dungeon.level.occupyCell(Dungeon.hero);
			Dungeon.observe();
			GameScene.updateFog();
			Dungeon.hero.checkVisibleMobs();

			Dungeon.hero.sprite.place(Dungeon.hero.pos);
		}
		CellEmitter.get(Dungeon.hero.pos).burst(Speck.factory(Speck.WOOL), 6);
		Sample.INSTANCE.play(Assets.Sounds.PUFF);
		afterAbilityUsed(hero);
	}

}
