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

import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class Kinetic extends Weapon.Enchantment {
	
	private static ItemSprite.Glowing YELLOW = new ItemSprite.Glowing( 0xFFFF00 );
	
	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		
		int conservedDamage = 0;
		if (attacker.buff(ConservedDamage.class) != null) {
			conservedDamage = attacker.buff(ConservedDamage.class).damageBonus();
			attacker.buff(ConservedDamage.class).detach();
		}

		//use a tracker so that we can know the true final damage
		Buff.affect(attacker, KineticTracker.class).conservedDamage = conservedDamage;
		
		return damage + conservedDamage;
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return YELLOW;
	}

	public static class KineticTracker extends Buff {

		{
			actPriority = Actor.VFX_PRIO;
		}

		public int conservedDamage;

		@Override
		public boolean act() {
			detach();
			return true;
		}
	};

	public static class ConservedDamage extends Buff {

		{
			type = buffType.POSITIVE;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.WEAPON;
		}
		
		@Override
		public void tintIcon(Image icon) {
			if (preservedDamage >= 10){
				icon.hardlight(1f, 0f, 0f);
			} else if (preservedDamage >= 5) {
				icon.hardlight(1f, 1f - (preservedDamage - 5f)*.2f, 0f);
			} else {
				icon.hardlight(1f, 1f, 1f - preservedDamage*.2f);
			}
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(damageBonus());
		}
		
		private float preservedDamage;
		
		public void setBonus(int bonus){
			preservedDamage = bonus;
		}

		public void addBonus(int bonus){
			preservedDamage += bonus;
		}

		public int damageBonus(){
			return (int)Math.ceil(preservedDamage);
		}
		
		@Override
		public boolean act() {
			preservedDamage -= Math.max(preservedDamage*.025f, 0.1f);
			if (preservedDamage <= 0) detach();
			
			spend(TICK);
			return true;
		}
public void delay( float value ){
			spend(value);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", damageBonus());
		}
		
		private static final String PRESERVED_DAMAGE = "preserve_damage";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(PRESERVED_DAMAGE, preservedDamage);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			if (bundle.contains(PRESERVED_DAMAGE)){
				preservedDamage = bundle.getFloat(PRESERVED_DAMAGE);
			} else {
				preservedDamage = cooldown()/10;
				spend(cooldown());
			}
		}
	}
}
