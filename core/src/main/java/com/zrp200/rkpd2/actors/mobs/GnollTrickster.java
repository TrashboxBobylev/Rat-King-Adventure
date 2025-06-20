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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.actors.mobs.npcs.Ghost;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.GnollTricksterSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class GnollTrickster extends Gnoll {

	{
		spriteClass = GnollTricksterSprite.class;

		HP = HT = 20;
		defenseSkill = 5;

		EXP = 5;

		WANDERING = new Wandering();
		state = WANDERING;

		//at half quantity, see createLoot()
		loot = Generator.Category.MISSILE;
		lootChance = 1f;

		properties.add(Property.MINIBOSS);
	}

	private int combo = 0;

	@Override
	public int damageRoll() {
		if (alignment == Alignment.ALLY){
			return Random.NormalIntRange( 8 + Math.max(0, (Dungeon.scalingDepth() -25)*2/5),
					16 + Math.max(0, (Dungeon.scalingDepth() -25)*5/5) );
		}
		return super.damageRoll();
	}

	@Override
	public int attackSkill( Char target ) {
		if (alignment == Alignment.ALLY){
			return (int) (Dungeon.hero.attackSkill(target) * 0.75f);
		}
		return 16;
	}

	@Override
	public int defenseSkill(Char enemy) {
		if (alignment == Alignment.ALLY){
			return (int) (Dungeon.hero.defenseSkill(enemy) * 0.75f);
		}
		return super.defenseSkill(enemy);
	}

	@Override
	public boolean canAttack( Char enemy ) {
        if (buff(ChampionEnemy.Paladin.class) != null){
            return false;
        }
		return !Dungeon.level.adjacent( pos, enemy.pos )
				&& (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		//The gnoll's attacks get more severe the more the player lets it hit them
		combo++;
		int effect = Random.Int(4)+combo;

		if (effect > 2) {

			if (effect >=6 && enemy.buff(Burning.class) == null){

				if (Dungeon.level.flamable[enemy.pos])
					GameScene.add(Blob.seed(enemy.pos, 4, Fire.class));
				Buff.affect(enemy, Burning.class).reignite( enemy );

			} else
				Buff.affect( enemy, Poison.class).set((effect-2) );

		}
		return damage;
	}

	@Override
	protected boolean getCloser( int target ) {
		combo = 0; //if he's moving, he isn't attacking, reset combo.
		if (buff(ChampionEnemy.Paladin.class) != null){
			return true;
		}
		if (state == HUNTING) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}

	@Override
	public void aggro(Char ch) {
		//cannot be aggroed to something it can't see
		//skip this check if FOV isn't initialized
		if (ch == null || fieldOfView == null
				|| fieldOfView.length != Dungeon.level.length() || fieldOfView[ch.pos]) {
			super.aggro(ch);
		}
	}
	
	@Override
	public Item createLoot() {
		if (alignment != Alignment.ALLY){
			MissileWeapon drop = (MissileWeapon)super.createLoot();
			//half quantity, rounded up
			drop.quantity((drop.quantity()+1)/2);
			return drop;
		}
		return null;
	}
	
	@Override
	public void die( Object cause ) {
		super.die( cause );
		if (alignment != Alignment.ALLY)
			Ghost.Quest.process();
	}

	protected class Wandering extends Mob.Wandering{
		@Override
		protected int randomDestination() {
			//of two potential wander positions, picks the one closest to the hero
			int pos1 = super.randomDestination();
			int pos2 = super.randomDestination();
			PathFinder.buildDistanceMap(Dungeon.hero.pos, Dungeon.level.passable);
			if (PathFinder.distance[pos2] < PathFinder.distance[pos1]){
				return pos2;
			} else {
				return pos1;
			}
		}
	}

	private static final String COMBO = "combo";
	private static final String RAT_ALLY = "ally";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(COMBO, combo);
		if (alignment == Alignment.ALLY) bundle.put(RAT_ALLY, true);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		combo = bundle.getInt( COMBO );
		if (bundle.contains(RAT_ALLY)) alignment = Alignment.ALLY;
	}

}
