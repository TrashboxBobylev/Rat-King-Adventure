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
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.DuelistGrass;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Sickle extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SICKLE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 2;
		ACC = 0.68f; //32% penalty to accuracy
	}

	@Override
	public int max(int lvl) {
		return  Math.round(6.67f*(tier+1)) +    //20 base, up from 15
				lvl*(tier+1);                   //scaling unchanged
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		Item grass = new DuelistGrass().quantity(Random.Int(2, 3 + level()/3));

		if (grass.doPickUp(Dungeon.hero, Dungeon.hero.pos)) {
			Dungeon.hero.spend(-Item.TIME_TO_PICK_UP); //casting the spell already takes a turn
			GLog.i( Messages.capitalize(Messages.get(Dungeon.hero, "you_now_have", grass.name())) );

		} else {
			GLog.w(Messages.get(this, "cant_grab"));
			Dungeon.level.drop(grass, Dungeon.hero.pos).sprite.drop();
		}
		return super.warriorAttack(damage, enemy);
	}

	@Override
	public float warriorMod() {
		return 0.33f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	public final int abilityStat() { return abilityStat(buffedLvl()); }
	public int abilityStat(int lvl) { return augment.damageFactor(Math.round(15f + 2.5f*lvl)); }

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get(this, "ability_desc", abilityStat());
		} else {
			return Messages.get(this, "typical_ability_desc", abilityStat(0));
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(abilityStat(level));
	}

	@Override
	protected DuelistAbility duelistAbility() {
		return new MeleeAbility(abilityStat()) {

			@Override
			public float dmgMulti(Char enemy) {
				return 0;
			}

			@Override
			protected void proc(Hero hero, Char enemy) {
				Buff.affect(enemy, HarvestBleedTracker.class, 0);
			}

		};
	}

	public static class HarvestBleedTracker extends FlavourBuff{};

}
