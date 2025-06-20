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

package com.zrp200.rkpd2.items.potions;

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Drowsy;
import com.zrp200.rkpd2.actors.buffs.Healing;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.actors.buffs.Slow;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.Weakness;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class PotionOfHealing extends Potion {

	{
		icon = ItemSpriteSheet.Icons.POTION_HEALING;

		bones = true;
	}
	
	@Override
	public void apply( Hero hero ) {
		identify();
		cure( hero );
		heal( hero );
	}

	public static void heal( Char ch ){
		if (ch == Dungeon.hero && Dungeon.isChallenged(Challenges.NO_HEALING)){
			pharmacophobiaProc(Dungeon.hero);
		} else {
			//starts out healing 30 hp, equalizes with hero health total at level 11
			Healing healing = Buff.affect(ch, Healing.class);
			healing.setHeal((int) (0.8f * ch.HT + 14), 0.25f, 0);
			healing.applyVialEffect();
			if (ch == Dungeon.hero){
				GLog.p( Messages.get(PotionOfHealing.class, "heal") );
			}
		}
	}

	public static void pharmacophobiaProc( Hero hero ){
		// harms the hero for ~40% of their max HP in poison
		Buff.affect( hero, Poison.class).set(4 + hero.lvl/2);
	}
	
	public static void cure( Char ch ) {
		if (!Dungeon.isChallenged(Challenges.ALLERGY)) {
			Buff.detach(ch, Poison.class);
			Buff.detach(ch, Cripple.class);
			Buff.detach(ch, Weakness.class);
			Buff.detach(ch, Vulnerable.class);
			Buff.detach(ch, Bleeding.class);
			Buff.detach(ch, Blindness.class);
			Buff.detach(ch, Drowsy.class);
			Buff.detach(ch, Slow.class);
			Buff.detach(ch, Vertigo.class);
		}
	}

	@Override
	public int value() {
		return isKnown() ? 30 * quantity : super.value();
	}
}
