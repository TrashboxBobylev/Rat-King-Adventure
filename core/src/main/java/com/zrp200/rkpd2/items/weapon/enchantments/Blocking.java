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

package com.zrp200.rkpd2.items.weapon.enchantments;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ShieldBuff;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

public class Blocking extends Weapon.Enchantment {
	
	private static ItemSprite.Glowing BLUE = new ItemSprite.Glowing( 0x0000FF );
	
	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		
		int level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 10%
		// lvl 1 ~ 12%
		// lvl 2 ~ 14%
		float procChance = (level+4f)/(level+40f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance){
			float powerMulti = Math.max(1f, procChance);
			BlockBuff b = Buff.affect(attacker, BlockBuff.class);
			int shield = Math.round(powerMulti * (2 + weapon.buffedLvl()));
			b.setShield(shield);
			attacker.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 5);
		}
		
		return damage;
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return BLUE;
	}
	
	public static class BlockBuff extends ShieldBuff {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public boolean act() {
			detach();
			return true;
		}

		@Override
		public void setShield(int shield, boolean force) {
			super.setShield(shield, force);
			postpone(5f);
		}

		@Override
		public void fx(boolean on) {
			if (on) {
				target.sprite.add(CharSprite.State.SHIELDED);
			} else if (target.buff(Barrier.class) == null) {
				target.sprite.remove(CharSprite.State.SHIELDED);
			}
		}

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}
		
		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.5f, 1f, 2f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (5f - visualcooldown()) / 5f);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)visualcooldown());
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", shielding(), dispTurns(visualcooldown()));
		}
	
	}
}
