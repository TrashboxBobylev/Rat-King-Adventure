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

package com.zrp200.rkpd2.items.rings;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.SafeCast;

public class RingOfEnergy extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_ENERGY;
		buffClass = Energy.class;
	}

    private static final float MULTIPLIER = 1.175f;

	@Override
	protected float multiplier() {
		return MULTIPLIER;
	}

    @Override
    public String upgradeStat1(int level) {
        if (cursed && cursedKnown) level = Math.min(-1, level-3);
        return formatBonus(level + 1);
    }

    @Override
	protected float cap() {
		return energyCap();
	}

	public static float energyCap() {
		return 2f;
	}

	@Override
	protected RingBuff buff( ) {
		return new Energy();
	}
	
	public static float wandChargeMultiplier( Char target ){
		float bonus = (float)Math.pow(MULTIPLIER, getBuffedBonus(target, Energy.class));

		if (target instanceof Hero && !((Hero) target).heroClass.is(HeroClass.CLERIC) && ((Hero) target).canHaveTalent(Talent.LIGHT_READING)){
			bonus *= 1f + (0.4f * ((Hero) target).pointsInTalent(Talent.LIGHT_READING)/3f);
		}

		return Math.min(bonus, energyCap()+1f);
	}

	public static float artifactChargeMultiplier( Char target ){
		float bonus = Math.min(3f, (float)Math.pow(MULTIPLIER, getBuffedBonus(target, Energy.class)));

		Hero hero = SafeCast.cast(target, Hero.class);
		if (hero != null && !hero.heroClass.isExact(HeroClass.ROGUE) && hero.hasTalent(Talent.LIGHT_CLOAK)){
			bonus *= 1f + /*(0.2f * ((Hero) target).pointsInTalent(Talent.LIGHT_CLOAK)/3f)*/ 0.1f*hero.pointsInTalent(Talent.LIGHT_CLOAK);
		}

		return Math.min(bonus, energyCap()+1f);
	}

	public static float armorChargeMultiplier( Char target ){
		return Math.min(3f, (float)Math.pow(MULTIPLIER, getBuffedBonus(target, Energy.class)));
	}

	public class Energy extends RingBuff {
	}
}
