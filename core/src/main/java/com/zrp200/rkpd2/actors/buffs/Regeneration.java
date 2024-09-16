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

package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.rings.RingOfEnergy;
import com.zrp200.rkpd2.levels.Terrain;

public class Regeneration extends Buff {
	
	{
		//unlike other buffs, this one acts after the hero and takes priority against other effects
		//healing is much more useful if you get some of it off before taking damage
		actPriority = HERO_PRIO - 1;
	}
	
	private static final float REGENERATION_DELAY = 10;
	
	@Override
	public boolean act() {
		if (target.isAlive()) {

			if (target.HP < regencap() && !((Hero)target).isStarving()) {
				if (regenOn()) {
					Hunger hunger = target.buff(Hunger.class);
					if (hunger != null && hunger.accumulatingDamage > 0){
						hunger.accumulatingDamage = Math.max(0, hunger.accumulatingDamage-2);
					} else {
						target.HP += 1;
						if (target.HP == regencap()) {
							((Hero) target).resting = false;
						}
					}
				}
			}

			float delay = getRegenDelay(target);
			spend( delay );
			
		} else {
			
			diactivate();
			
		}
		
		return true;
	}

	public static float getRegenDelay(Char target) {
		RegenerationBuff regenBuff = target.buff( RegenerationBuff.class);
		float delay = REGENERATION_DELAY;
		if (regenBuff != null && target.buff(MagicImmune.class) == null) {
			if (regenBuff.isCursed()) {
				delay *= 1.5f;
			} else {
				//15% boost at +0, scaling to a 500% boost at +10
				delay -= 1.33f + regenBuff.itemLevel()*0.667f;
				delay /= RingOfEnergy.artifactChargeMultiplier(target);
			}
		}
		if (Dungeon.hero.hasTalent(Talent.NATURE_AID_2) && Dungeon.level.map[Dungeon.hero.pos] == Terrain.FURROWED_GRASS){
			delay *= 1 - Dungeon.hero.pointsInTalent(Talent.NATURE_AID_2)*0.35f;
		}
		if (!Dungeon.hero.heroClass.is(HeroClass.WARRIOR) && Dungeon.hero.hasTalent(Talent.WILLPOWER_OF_INJURED)){
			float boost = 0.5f * ((Hero) target).pointsInTalent(Talent.WILLPOWER_OF_INJURED);
			delay /= 1f + boost * Math.max(1f, ((float) (target.HT - target.HP) / target.HT)*1.1f);
		}
		if (Dungeon.hero.hasTalent(Talent.RK_PALADIN)){
			delay /= 1f + 0.075f*RKChampionBuff.rkPaladinUniqueAllies()*Dungeon.hero.pointsInTalent(Talent.RK_PALADIN);
		}
		return delay;
	}

	public int regencap(){
		return target.HT;
	}

	public static boolean regenOn(){
		LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
		if (lock != null && !lock.regenOn()){
			return false;
		}
		return true;
	}
}
