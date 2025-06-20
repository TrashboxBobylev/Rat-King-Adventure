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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Daze;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

;

public class Mace extends MeleeWeapon {

	{
		image = ItemSpriteSheet.MACE;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;

		tier = 3;
		ACC = 1.28f; //28% boost to accuracy
	}

	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //16 base, down from 20
				lvl*(tier+1);   //scaling unchanged
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		if (enemy.isAlive()){
			//trace a ballistica to our target (which will also extend past them
			Ballistica trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
			//trim it to just be the part that goes past them
			trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
			//knock them back along that ballistica
			WandOfBlastWave.throwChar(enemy, trajectory, 2, true, false, getClass());
			Buff.prolong(enemy, Vertigo.class, Random.NormalIntRange(1, 4));
		}
		return 0;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = abilityStat(levelKnown ? buffedLvl() : 0);
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min()+dmgBoost), augment.damageFactor(max()+dmgBoost));
		} else {
			return Messages.get(this, "typical_ability_desc", min(0)+dmgBoost, max(0)+dmgBoost);
		}
	}

	public int abilityStat(int level) {
		//+(5+1.5*lvl) damage, roughly +55% base dmg, +60% scaling
		return (int)Math.floor(3 + tier * 0.7f) + Math.round(1.5f*level);
	}

	protected MeleeAbility duelistAbility() {
		return new HeavyBlow();
	}

	public class HeavyBlow extends MeleeAbility {

		{
			dmgBoost = abilityStat(buffedLvl());
		}

		@Override
		protected void beforeAbilityUsed(Hero hero, Char enemy) {
			super.beforeAbilityUsed(hero, enemy);
			//no bonus damage if attack isn't a surprise
			if (enemy instanceof Mob && !((Mob) enemy).surprisedBy(hero)){
				dmgMulti = Math.min(1, dmgMulti);
				dmgBoost = 0;
			}
		}

		@Override
		public void afterHit(Char enemy, boolean hit) {
			if (enemy.isAlive()) {
				Buff.affect(enemy, Daze.class, Daze.DURATION);
			}
		}
	}

}
