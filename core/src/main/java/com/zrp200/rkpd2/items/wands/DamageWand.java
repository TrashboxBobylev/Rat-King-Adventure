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

package com.zrp200.rkpd2.items.wands;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Preparation;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.buffs.WandEmpower;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;

//for wands that directly damage a target
//wands with AOE effects count here (e.g. fireblast), but wands with indrect damage do not (e.g. venom, transfusion)
public abstract class DamageWand extends Wand{

	public int min(){
		int dmg = min(buffedLvl());
		if (Dungeon.hero.hasTalent(Talent.ARCANITY_ENSUES)){
			BrawlerBuff buff = Dungeon.hero.buff(BrawlerBuff.class);
			dmg += Math.round(0.2f*Dungeon.hero.pointsInTalent(Talent.ARCANITY_ENSUES)*buff.damageFactor(dmg));
		}
		return dmg;
	}

	public abstract int min(int lvl);

	public int max(){
		int dmg = max(buffedLvl());
		if (Dungeon.hero.hasTalent(Talent.ARCANITY_ENSUES)){
			BrawlerBuff buff = Dungeon.hero.buff(BrawlerBuff.class);
			dmg += Math.round(0.2f*Dungeon.hero.pointsInTalent(Talent.ARCANITY_ENSUES)*buff.damageFactor(dmg));
		}
		return dmg;
	}

	public abstract int max(int lvl);

	public int damageRoll(){
		return damageRoll(buffedLvl());
	}

	public int damageRoll(int lvl){
		if (RobotBuff.isVehicle()){
			return -1;
		}
		int dmg = Random.NormalIntRange(min(lvl), max(lvl));
		WandEmpower emp = Dungeon.hero.buff(WandEmpower.class);
		if (emp != null){
			dmg += emp.dmgBoost;
			emp.left--;
			if (emp.left <= 0) {
				emp.detach();
			}
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
		}
		return dmg;
	}

	public void procKO(Char enemy){
		if (Dungeon.hero.pointsInTalent(Talent.ADAPT_AND_OVERCOME) >= 2 &&
				Dungeon.hero.buff(Preparation.class) != null){
			boolean result = Dungeon.hero.buff(Preparation.class).procKO(Dungeon.hero, enemy);
			if (result) Dungeon.hero.buff(Preparation.class).detach();
		}
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max());
		else {
			int baseLevel = Dungeon.hero != null ? Dungeon.hero.getBonus(this) : 0;
			return Messages.get(this, "stats_desc", min(baseLevel), max(baseLevel));
		}
	}
}
