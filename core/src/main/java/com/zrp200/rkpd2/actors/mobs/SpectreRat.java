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

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Degrade;
import com.zrp200.rkpd2.actors.buffs.Hex;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.Shrink;
import com.zrp200.rkpd2.actors.buffs.Slow;
import com.zrp200.rkpd2.actors.buffs.TimedShrink;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.buffs.Weakness;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.PotionOfHealing;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.SpectreRatSprite;
import com.zrp200.rkpd2.utils.GLog;

import java.util.Arrays;

import static com.zrp200.rkpd2.Dungeon.hero;

public class SpectreRat extends AbyssalMob implements Callback {

	private static final float TIME_TO_ZAP	= 1f;

	{
		spriteClass = SpectreRatSprite.class;

		HP = HT = 100;
		defenseSkill = 23;
		viewDistance = Light.DISTANCE;

		EXP = 13;

		loot = Generator.Category.POTION;
		lootChance = 0.33f;

		properties.add(Property.DEMONIC);
	}

	@Override
	public int attackSkill( Char target ) {
		return 36 + abyssLevel();
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0 + abyssLevel()*5, 10 + abyssLevel()*10);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 4 + abyssLevel()*2, 14 + abyssLevel()*3 );
	}

	@Override
	public float attackDelay() {
		return super.attackDelay()*0.5f;
	}

	@Override
	public boolean canAttack(Char enemy) {
		if (buff(ChampionEnemy.Paladin.class) != null){
			return false;
		}
		if (buff(Talent.AntiMagicBuff.class) != null){
			return super.canAttack(enemy);
		}
		return super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}

	protected boolean doAttack( Char enemy ) {
		if (buff(Talent.AntiMagicBuff.class) != null){
			return super.doAttack(enemy);
		}
		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

			return super.doAttack( enemy );

		} else {

			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}

	//used so resistances can differentiate between melee and magical attacks
	public static class DarkBolt{}

	private void zap() {
		spend( TIME_TO_ZAP );

		if (hit( this, enemy, true )) {
			//TODO would be nice for this to work on ghost/statues too
			if (enemy == Dungeon.hero && enemy.buff(WarriorParry.BlockTrock.class) == null && Random.Int( 2 ) == 0) {
				Buff.prolong( enemy, Random.element(Arrays.asList(
						Blindness.class, Slow.class, Vulnerable.class, Hex.class,
						Weakness.class, Degrade.class, Cripple.class
				)), Degrade.DURATION );
				Sample.INSTANCE.play( Assets.Sounds.DEBUFF );
			}

			int dmg = Random.NormalIntRange( 14 + abyssLevel()*6, 20 + abyssLevel()*9 );
			if (buff(Shrink.class) != null|| enemy.buff(TimedShrink.class) != null) dmg *= 0.6f;
			ChampionEnemy.AntiMagic.effect(enemy, this);
			if (enemy.buff(WarriorParry.BlockTrock.class) != null){
				enemy.sprite.emitter().burst( Speck.factory( Speck.FORGE ), 15 );
				SpellSprite.show(enemy, SpellSprite.BLOCK, 2f, 2f, 2f);
				Buff.affect(enemy, Barrier.class).incShield(Math.round(dmg*1.25f));
				hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(Math.round(dmg*1.25f)), FloatingText.SHIELDING );
				enemy.buff(WarriorParry.BlockTrock.class).triggered = true;
			} else {
				enemy.damage(dmg, new DarkBolt());

				if (enemy == Dungeon.hero && !enemy.isAlive()) {
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

	@Override
	public Item createLoot(){

		if (Random.Int(3) == 0 && Random.Int(10) > Dungeon.LimitedDrops.SPECTRE_RAT.count ){
			Dungeon.LimitedDrops.SPECTRE_RAT.drop();
			return new PotionOfHealing();
		} else {
			Item i = Generator.random(Generator.Category.POTION);
			int healingTried = 0;
			while (i instanceof PotionOfHealing){
				healingTried++;
				i = Generator.random(Generator.Category.POTION);
			}

			//return the attempted healing potion drops to the pool
			if (healingTried > 0){
				for (int j = 0; j < Generator.Category.POTION.classes.length; j++){
					if (Generator.Category.POTION.classes[j] == PotionOfHealing.class){
						Generator.Category.POTION.probs[j] += healingTried;
					}
				}
			}

			return i;
		}

	}
}
