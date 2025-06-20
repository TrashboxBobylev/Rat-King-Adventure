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

package com.zrp200.rkpd2.items.quest;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Bat;
import com.zrp200.rkpd2.actors.mobs.Bee;
import com.zrp200.rkpd2.actors.mobs.Crab;
import com.zrp200.rkpd2.actors.mobs.Scorpio;
import com.zrp200.rkpd2.actors.mobs.Spinner;
import com.zrp200.rkpd2.actors.mobs.Swarm;
import com.zrp200.rkpd2.actors.mobs.npcs.Blacksmith;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.levels.AbyssLevel;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.MiningLevel;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite.Glowing;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Pickaxe extends MeleeWeapon {

	{
		image = ItemSpriteSheet.PICKAXE;

		levelKnown = true;
		
		unique = true;
		bones = false;

		tier = 2;
	}

	@Override
	public int STRReq(int lvl) {
		return super.STRReq(lvl) + 2; //tier 3 strength requirement with tier 2 damage stats
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (Dungeon.level instanceof MiningLevel){
			actions.remove(AC_DROP);
			actions.remove(AC_THROW);
		}
		return actions;
	}

	@Override
	public boolean keptThroughLostInventory() {
		//pickaxe is always kept when it's needed for the mining level
		return super.keptThroughLostInventory() || Dungeon.level instanceof MiningLevel;
	}
	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	private class Pierce extends MeleeAbility {
		@Override
		protected void beforeAbilityUsed(Hero hero, Char enemy) {
			if (Char.hasProp(enemy, Char.Property.INORGANIC)
					|| enemy instanceof Swarm
					|| enemy instanceof Bee
					|| enemy instanceof Crab
					|| enemy instanceof Spinner
					|| enemy instanceof Scorpio) {
				//+(8+2*lvl) damage, equivalent to +100% damage
				dmgBoost = augment.damageFactor(8 + 2*buffedLvl());
			}
		}

		@Override
		protected void proc(Hero hero, Char enemy) {
			Buff.affect(enemy, Vulnerable.class, 3f);
		}
	} @Override
	protected MeleeAbility duelistAbility() {
		return new Pierce();
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = 8 + 2*buffedLvl();
		return Messages.get(this, "ability_desc", augment.damageFactor(min()+dmgBoost), augment.damageFactor(max()+dmgBoost));
	}

	public String upgradeAbilityStat(int level){
		int dmgBoost = 8 + 2*level;
		return augment.damageFactor(min(level)+dmgBoost) + "-" + augment.damageFactor(max(level)+dmgBoost);
	}

}
