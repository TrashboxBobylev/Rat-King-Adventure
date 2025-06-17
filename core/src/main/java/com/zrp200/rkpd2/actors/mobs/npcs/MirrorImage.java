/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
<<<<<<< shpd/master:core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/MirrorImage.java
 * Copyright (C) 2014-2024 Evan Debenham
=======
 * Copyright (C) 2014-2022 Evan Debenham
>>>>>>> HEAD:core/src/main/java/com/zrp200/rkpd2/actors/mobs/npcs/MirrorImage.java
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

package com.zrp200.rkpd2.actors.mobs.npcs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Fury;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.KindOfWeapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Random;

public class MirrorImage extends AbstractMirrorImage {
	
	{
		HP = HT = 1;
	}

	
	public void duplicate( Hero hero ) {
		super.duplicate(hero);
		Buff.affect(this, MirrorInvis.class, Short.MAX_VALUE);
	}
	
	@Override
	public int damageRoll() {
		int damage;
		if (hero.belongings.weapon() != null){
			damage = hero.belongings.weapon().damageRoll(this);
		} else {
			damage = hero.damageRoll(); //handles ring of force
		}
		damage *= 1f + 0.2f*hero.pointsInTalent(Talent.SPECTRE_ALLIES);
		return (damage+1)/2; //half hero damage, rounded up
	}

	@Override
	public int attackSkill(Char target) {
		int attackSkill = super.attackSkill(target);
		// benefits from weapon
		if(hero != null && hero.belongings.attackingWeapon() != null) {
			attackSkill *= hero.belongings.attackingWeapon().accuracyFactor(this, target);
		}
		return attackSkill;
	}

	@Override
    public float attackDelay() {
		return hero.attackDelay(); //handles ring of furor
	}
	
	@Override
	public boolean canAttack(Char enemy) {
		return super.canAttack(enemy) ||
				(hero.belongings.weapon() != null && hero.belongings.weapon().canReach(this, enemy.pos)) ||
				(hero.hasTalent(Talent.SPECTRE_ALLIES) && KindOfWeapon.canReach(this, enemy.pos, 1 + hero.pointsInTalent(Talent.SPECTRE_ALLIES)));
	}

	@Override
	public void damage(int dmg, Object src) {
		super.damage(dmg, src);
		if (HP * 2 < HT && Dungeon.hero.pointsInTalent(Talent.RK_SPLINT) >= 2){
			Buff.affect(this, Fury.class);
		}
	}

	@Override
	public int drRoll() {
		int dr = super.drRoll();
		if (hero != null && hero.belongings.weapon() != null){
			dr += Random.NormalIntRange(0, hero.belongings.weapon().defenseFactor(this)/2);
		}
		return dr;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		MirrorInvis buff = buff(MirrorInvis.class);
		if (buff != null){
			buff.detach();
		}
		damage = super.attackProc(enemy, damage);
		if (hero.belongings.weapon() != null){
			damage = hero.belongings.weapon().proc( this, enemy, damage );
			if (!enemy.isAlive() && enemy == Dungeon.hero){
				Dungeon.fail(this);
				GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
			}
		}
		return damage;
	}
	
	public static class MirrorInvis extends Invisibility {
		
		{
			announced = false;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}
}