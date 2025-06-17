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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Berserk;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.RatSprite;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Rat extends Mob {

	{
		spriteClass = RatSprite.class;
		
		HP = HT = 8;
		defenseSkill = 2;
		
		maxLvl = 5;
	}

	@Override
	public String name() {
		Alignment alignment1 = alignment;
		if (Dungeon.depth == 0){
			alignment1 = alignment == Alignment.ENEMY ? Alignment.ALLY : Alignment.ENEMY;
		}
		String name = super.name();
		String prefixed = Messages.get(this, alignment1.toString().toLowerCase(), name);
        if (prefixed.isEmpty()) return name;
		else {
			if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
				prefixed = ShatteredPixelDungeon.turnIntoRrrr(prefixed);
			}
			return prefixed;
		}
    }

	@Override
	public String description() {
		Alignment alignment1 = alignment;
		if (Dungeon.depth == 0){
			alignment1 = alignment == Alignment.ENEMY ? Alignment.ALLY : Alignment.ENEMY;
		}
		String bonus_desc = Messages.get(this,"desc_"+alignment1.toString().toLowerCase(), false);
		String desc = super.description();
		if(!bonus_desc.isEmpty()) desc += "\n\n" + bonus_desc;
		return desc;
	}

	@Override
	protected boolean act() {
		if (Dungeon.level.heroFOV[pos] && Dungeon.hero.armorAbility instanceof Ratmogrify && Dungeon.depth != 0){
			alignment = Alignment.ALLY;
			if (state == SLEEPING) state = WANDERING;
		}
		return super.act();
	}

	// technically this behavior could be generalized to all mobs, but this is not the mod to do that.
	protected float[] // this change lets me use fractional values....
			damageRange = {1,4},
			armorRange  = {0,1};

	@Override
	public int damageRoll() {
		int damage = Math.round(Random.NormalFloat(damageRange[0], damageRange[1]));
		Berserk berserk = buff(Berserk.class);
		if (berserk != null && Ratmogrify.drratedonEffect(this) > 2) damage = berserk.damageFactor(damage);
		return damage;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (damage > 0 && Ratmogrify.drratedonEffect(this) > 2){
			Berserk berserk = Buff.affect(this, Berserk.class);
			berserk.damage(damage);
		}
		return super.defenseProc(enemy, damage);
	}

	@Override
	public int attackSkill( Char target ) {
		return 8;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Math.round(Random.NormalFloat(armorRange[0], armorRange[1]));
	}

	private static final String RAT_ALLY = "rat_ally";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (alignment == Alignment.ALLY) bundle.put(RAT_ALLY, true);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(RAT_ALLY)) alignment = Alignment.ALLY;
	}
}
