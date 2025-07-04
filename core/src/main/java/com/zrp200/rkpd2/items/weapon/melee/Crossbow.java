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
import com.zrp200.rkpd2.actors.buffs.DummyBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.KindOfWeapon;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.weapon.missiles.darts.CrossbowAmmo;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;

public class Crossbow extends MeleeWeapon {
	
	{
		image = ItemSpriteSheet.CROSSBOW;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1f;
		
		//check Dart.class for additional properties
		
		tier = 4;
	}

	public static Crossbow find(Hero hero) {
		if (hero != null) for (KindOfWeapon weapon : hero.belongings.weapons()) {
			if (weapon instanceof Crossbow) return (Crossbow) weapon;
		}
		return null;
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			ChargedShot buff = hero.buff(ChargedShot.class);
			if (buff != null && find(hero) == null) {
				//clear charged shot if no crossbow is equipped
				buff.detach();
			}
			return true;
		} else {
			return false;
		}
	}


	@Override
	public float accuracyFactor(Char owner, Char target) {
		if (owner.buff(Crossbow.ChargedShot.class) != null){
			Actor.add(new Actor() {
				{ actPriority = VFX_PRIO; }
				@Override
				protected boolean act() {
					if (owner instanceof Hero && !target.isAlive()){
						onAbilityKill((Hero)owner, target);
					}
					Actor.remove(this);
					return true;
				}
			});
			return Float.POSITIVE_INFINITY;
		} else {
			return super.accuracyFactor(owner, target);
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int dmg = super.proc(attacker, defender, damage);

		//stronger elastic effect
		if (attacker.buff(ChargedShot.class) != null && !(curItem instanceof CrossbowAmmo)){
			//trace a ballistica to our target (which will also extend past them
			Ballistica trajectory = new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_TARGET);
			//trim it to just be the part that goes past them
			trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
			//knock them back along that ballistica
			WandOfBlastWave.throwChar(defender,
					trajectory,
					4,
					true,
					true,
					this);
			attacker.buff(Crossbow.ChargedShot.class).detach();
		}
		return dmg;
	}
	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //20 base, down from 25
				lvl*(tier);     //+4 per level, down from +5
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		Buff.affect(Dungeon.hero, DartSpent.class, 15f);
		return super.warriorAttack(damage, enemy);
	}

	public static class DartSpent extends DummyBuff {
		@Override
		public int icon() {
			return BuffIndicator.MARK;
		}

		@Override
		public float iconFadePercent() {
			return ((15 - cooldown()) / 15);
		}
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (hero.buff(ChargedShot.class) != null){
			GLog.w(Messages.get(this, "ability_cant_use"));
			return;
		}

		beforeAbilityUsed(hero, null);
		Buff.affect(hero, ChargedShot.class);
		hero.sprite.operate(hero.pos);
		hero.next();
		afterAbilityUsed(hero);
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get(this, "ability_desc", 3+buffedLvl(), 3+buffedLvl());
		} else {
			return Messages.get(this, "typical_ability_desc", 3, 3);
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(3 + level);
	}

	public static class ChargedShot extends Buff{

		{
			announced = true;
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.DUEL_XBOW;
		}

	}

}
