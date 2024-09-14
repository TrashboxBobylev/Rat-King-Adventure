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

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.DwarfKing;
import com.zrp200.rkpd2.actors.mobs.Mimic;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;

//generic class for buffs which convert an enemy into an ally
// There is a decent amount of logic that ties into this, which is why it has its own abstract class
public abstract class AllyBuff extends Buff{

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)){
			target.alignment = Char.Alignment.ALLY;
			if (target.buff(PinCushion.class) != null){
				target.buff(PinCushion.class).detach();
			}
			if(target instanceof DwarfKing.Subject) { // DK logic
				new DwarfKing().yell( Messages.get(DwarfKing.class,"corrupted",target.name()));
			}
			return true;
		} else {
			return false;
		}
	}

	//for when applying an ally buff should also cause that enemy to give exp/loot as if they had died
	//consider that chars with the ally alignment do not drop items or award exp on death
	public static void affectAndLoot(Mob enemy, Hero hero, Class<?extends AllyBuff> buffCls){
		boolean wasEnemy = enemy.alignment == Char.Alignment.ENEMY || enemy instanceof Mimic;
		Buff.affect(enemy, buffCls);

		if (enemy.buff(buffCls) != null && wasEnemy){
			enemy.rollToDropLoot();

			Statistics.enemiesSlain++;
			Badges.validateMonstersSlain();
			Statistics.qualifiedForNoKilling = false;

			AscensionChallenge.processEnemyKill(enemy);

			int exp = hero.lvl <= enemy.maxLvl ? enemy.EXP : 0;
			if (exp > 0) {
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(exp), FloatingText.EXPERIENCE);
			}
			hero.earnExp(exp, enemy.getClass());

			if (hero.subClass.is(HeroSubClass.MONK)){
				Buff.affect(hero, MonkEnergy.class).gainEnergy(enemy);
			}
			if (HighnessBuff.isEnergized() && Dungeon.hero.hasTalent(Talent.SLASH_RUNNER)){
				Buff.affect( Dungeon.hero, MeleeWeapon.Charger.class ).gainCharge(0.5f);
			}
		}
	}

}
