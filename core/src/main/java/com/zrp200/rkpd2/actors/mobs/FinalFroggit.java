/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 *  Shattered Pixel Dungeon
 *  Copyright (C) 2014-2022 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2022 TrashboxBobylev
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

package com.zrp200.rkpd2.actors.mobs;

import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Shrink;
import com.zrp200.rkpd2.actors.buffs.TimedShrink;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.wands.WandOfPrismaticLight;
import com.zrp200.rkpd2.items.weapon.enchantments.Grim;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;

import static com.zrp200.rkpd2.Dungeon.hero;

public class FinalFroggit extends AbyssalMob implements Callback {

	private static final float TIME_TO_ZAP	= 1f;

	{
		spriteClass = FinalFroggitSprite.class;

		HP = HT = 180;
		defenseSkill = 20;

		EXP = 13;

		loot = Generator.random();
		lootChance = 1f;

		properties.add(Property.UNDEAD);
		properties.add(Property.DEMONIC);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 18, 25 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 30 + abyssLevel()*10;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0 + abyssLevel()*10, 8 + abyssLevel()*15);
	}

	@Override
	public boolean canAttack(Char enemy) {
		if (buff(ChampionEnemy.Paladin.class) != null){
			return false;
		}
		if (buff(Talent.AntiMagicBuff.class) != null){
			return super.canAttack(enemy);
		}
		return (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos);
	}

	protected boolean doAttack( Char enemy ) {

		boolean visible = fieldOfView[pos] || fieldOfView[enemy.pos];
		if (visible) {
			sprite.zap( enemy.pos );
		} else {
			zap();
		}

		return !visible;
	}

	//used so resistances can differentiate between melee and magical attacks
	public static class Bolt{}

	private void zap() {
		spend( TIME_TO_ZAP );

		if (hit( this, enemy, true )) {

			Eradication eradication = enemy.buff(Eradication.class);
			float multiplier = 1f;
			if (eradication != null){
				multiplier = (float) (Math.pow(1.2f, eradication.combo));
			}
			int damage = Random.Int( 4 + abyssLevel()*4, 10 + abyssLevel()*8 );
			if (buff(Shrink.class) != null|| enemy.buff(TimedShrink.class) != null) damage *= 0.6f;
			ChampionEnemy.AntiMagic.effect(enemy, this);

			int dmg = Math.round(damage * multiplier);

			if (enemy.buff(WarriorParry.BlockTrock.class) != null){
				enemy.sprite.emitter().burst( Speck.factory( Speck.FORGE ), 15 );
				SpellSprite.show(enemy, SpellSprite.BLOCK, 2f, 2f, 2f);
				Buff.affect(enemy, Barrier.class).incShield(Math.round(dmg*1.25f));
				hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(Math.round(dmg*1.25f)), FloatingText.SHIELDING );
				enemy.buff(WarriorParry.BlockTrock.class).triggered = true;
			} else {

				Buff.prolong(enemy, Eradication.class, Eradication.DURATION).combo++;

				enemy.damage(dmg, new Bolt());

				if (!enemy.isAlive() && enemy == Dungeon.hero) {
					Dungeon.fail(getClass());
					GLog.n(Messages.get(this, "bolt_kill"));
				}
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}
	}

	public void onZapComplete() {
		zap();
		next();
	}

	@Override
	public void call() {
		next();
	}

	{
		resistances.add( Grim.class );
		immunities.add(WandOfPrismaticLight.class);
		immunities.add(Blindness.class);
		immunities.add(Vertigo.class);
	}

	public static class Eradication extends FlavourBuff {

		public static final float DURATION = 4f;

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		public int combo;

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("combo", combo);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			combo = bundle.getInt("combo");
		}

		@Override
		public int icon() {
			return BuffIndicator.ERADICATION;
		}

		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(), (float)Math.pow(1.2f, combo));
		}
	}
}
