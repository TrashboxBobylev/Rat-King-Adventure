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

package com.zrp200.rkpd2.actors.hero.abilities;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.LegacyWrath;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.MusRexIra;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import static com.zrp200.rkpd2.Dungeon.hero;

public abstract class ArmorAbility implements Bundlable {

	protected float baseChargeUse = 35;

	public void use( ClassArmor armor, Hero hero ){
		if (targetingPrompt() == null){
			activate(armor, hero, hero.pos);
		} else {
			GameScene.selectCell(new CellSelector.Listener() {
				@Override
				public void onSelect(Integer cell) {
					activate(armor, hero, cell);
				}

				@Override
				public String prompt() {
					return targetingPrompt();
				}
			});
		}
	}

	//leave null for no targeting
	public String targetingPrompt(){
		return null;
	}

	public boolean useTargeting(){
		return targetingPrompt() != null;
	}

	public int targetedPos( Char user, int dst ){
		return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
	}

	// shpd reduced charge use by 12%/23%/32%/40%
	// rkpd2 reduced charge use by 16/30/40/50%
	private static final double HEROIC_ENERGY_REDUCTION = 0.839;

	public float chargeUse( Hero hero ){
		float chargeUse = baseChargeUse;
		if (hero.hasTalent(Talent.EMPOWERED_STRIKE_II)){
			chargeUse = 85 - hero.pointsInTalent(Talent.EMPOWERED_STRIKE_II)*5;
		}
		if (hasTalent(Talent.HEROIC_ENERGY) && hero.hasTalent(Talent.HEROIC_ENERGY)){
			chargeUse *= Math.pow( HEROIC_ENERGY_REDUCTION, hero.pointsInTalent(Talent.HEROIC_ENERGY));
		}
		return chargeUse;
	}

	public abstract void activate(ClassArmor armor, Hero hero, Integer target);

	public String name(){
		return Messages.get(this, "name");
	}

	public String shortDesc(){
		return Messages.get(this, "short_desc");
	}

	public String desc(){
		if (this instanceof LegacyWrath || this instanceof MusRexIra){
			return Messages.get(this, "desc");
		}
		return Messages.get(this, "desc") + "\n\n" + Messages.get(this, "cost",
				hero != null ? (int)chargeUse(hero) : (int)baseChargeUse);
	}

	public int icon(){
		return HeroIcon.NONE;
	}

	public abstract Talent[] talents();
	public final boolean hasTalent(Talent talent) {
		for(Talent t : talents()) if(t == talent) return true;
		return false;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
	}

	// for ClassArmor.java
	public String actionName() { return name().toUpperCase(); }
	/** causes OmniAbility to trick the game into thinking you have talents for this ArmorAbility.
	 * Careful with using things like hero#byTalent though, as this may cause unexpected behavior (both talents being active.)
	 **/
	public boolean isTracked(Hero hero) { return false; }
	/** lets omniability select this manually even when not its stored armor ability. **/
	public boolean isActive(Hero hero) { return false; }

	// all objects of a given armor ability are equal. they're all interchangeable.
	@Override public boolean equals(Object other) {
		return other != null && getClass().equals(other.getClass());
	}
	@Override public int hashCode() { return getClass().hashCode(); }

}
