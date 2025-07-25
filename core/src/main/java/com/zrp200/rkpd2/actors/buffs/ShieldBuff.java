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

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public abstract class ShieldBuff extends Buff {
	
	private int shielding;

	protected boolean detachesAtZero = true;

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			target.needsShieldUpdate = true;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.needsShieldUpdate = true;
		super.detach();
	}
	
	public int shielding(){
		return shielding;
	}

	public void vfx(int shield) {
		if(shield <= 0 || target == null || target.sprite == null) return;
		target.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
	}

	public void setShield( int shield, boolean force ) {
		int difference = shield - this.shielding;
		if (force || difference > 0) {
			incShield(difference, icon() != BuffIndicator.NONE);
		}
	}
	final public void setShield( int shield ) {
		setShield(shield, false);
	}

	public void incShield(){
		incShield(1);
	}

	public void incShield( int amt ){
		shielding += amt;
		if (target != null) target.needsShieldUpdate = true;
	}

	public final void incShield (int amt, boolean vfx) {
		if (vfx) vfx(amt);
		incShield(amt);
	}
//doesn't add shield, but postpones it detereorating
	public void delay( float value ){
		spend(value);
	}

	public void decShield(){
		decShield(1);
	}

	public void decShield( int amt ){
		shielding -= amt;
		if (target != null) target.needsShieldUpdate = true;
	}
	
	//returns the amount of damage leftover
	public int absorbDamage( int dmg ){
		if (shielding >= dmg){
			shielding -= dmg;
			dmg = 0;
		} else {
			dmg -= shielding;
			shielding = 0;
		}
		if (shielding <= 0 && detachesAtZero){
			detach();
		}
		if (target != null) target.needsShieldUpdate = true;
		return dmg;
	}
	
	private static final String SHIELDING = "shielding";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SHIELDING, shielding);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		shielding = bundle.getInt( SHIELDING );
	}
	
}
