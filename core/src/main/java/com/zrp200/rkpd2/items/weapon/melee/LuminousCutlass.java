/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Lightning;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.particles.SparkParticle;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class LuminousCutlass extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {

	{
		image = ItemSpriteSheet.LUMINOUS_CUTLASS;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.2f;

		tier = 6;
		DLY = 0.8f; //1.25x speed
	}

	@Override
	public int max(int lvl) {
		return  4*(tier) +    //16 base, down from 20
				lvl*(tier);   //scaling unchanged
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {

			affected.clear();
			arcs.clear();

			arc(attacker, defender, attacker.buff(BrawlerBuff.BrawlingTracker.class) != null ? 100000 : 3, affected, arcs);

			affected.remove(defender); //defender isn't hurt by lightning
			for (Char ch : affected) {
				if (ch.alignment != attacker.alignment) {
					int dmg = damage;
					if (enchantment != null) {
						dmg = enchantment.proc(this, attacker, ch, damage);
					}
					ch.damage(Math.round(dmg * 0.5f), this);
				}
			}

			attacker.sprite.parent.addToFront( new Lightning( arcs, null ) );
			Sample.INSTANCE.play( Assets.Sounds.ZAP );
			return super.proc(attacker, defender, damage);
	}

	private ArrayList<Char> affected = new ArrayList<>();

	private ArrayList<Lightning.Arc> arcs = new ArrayList<>();

	public static void arc( Char attacker, Char defender, int dist, ArrayList<Char> affected, ArrayList<Lightning.Arc> arcs ) {

		affected.add(defender);

		defender.sprite.centerEmitter().burst(SparkParticle.FACTORY, 12);
		defender.sprite.centerEmitter().burst(MagicMissile.MagicParticle.FACTORY, 12);
		defender.sprite.flash();

		PathFinder.buildDistanceMap( defender.pos, BArray.not( Dungeon.level.solid, null ), dist );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Char n = Actor.findChar(i);
				if (n != null && n != attacker && !affected.contains(n)) {
					arcs.add(new Lightning.Arc(defender.sprite.center(), n.sprite.center()));
					arc(attacker, n, arcs.size()+3, affected, arcs);
				}
			}
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return enchantment != null && (cursedKnown || !enchantment.curse()) ?
				new ItemSprite.Glowing(enchantment.glowing().color, 0.33f*enchantment.glowing().period) : WHITE;
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		return damage*2;
	}

	private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF, 0.33f );

	@Override
	protected DuelistAbility duelistAbility() {
		return new ShortCircuit();
	}

	public static class ShortCircuit extends MeleeAbility {

		@Override
		public void afterHit(Char enemy, boolean hit) {
			LuminousCutlass weapon = (LuminousCutlass) weapon();
			weapon.affected.clear();
			weapon.arcs.clear();

			arc(Dungeon.hero, enemy, 3, weapon.affected, weapon.arcs);

			for (Char ch : weapon.affected) {
				if (ch.alignment != Dungeon.hero.alignment) {
					GameScene.add( Blob.seed( ch.pos, 3, Electricity.class ) );
					Buff.affect( ch, Paralysis.class, Paralysis.DURATION/3 );
				}
			}
		}
	}
}
