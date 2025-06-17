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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Charm;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfIdentify;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.scrolls.ScrollOfUpgrade;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.SuccubusSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Succubus extends Mob {

	private int blinkCooldown = 0;
	
	{
		spriteClass = SuccubusSprite.class;
		
		HP = HT = 80;
		defenseSkill = 25;
		viewDistance = Light.DISTANCE;
		
		EXP = 12;
		maxLvl = 25;
		
		loot = Generator.Category.SCROLL;
		lootChance = 0.33f;

		properties.add(Property.DEMONIC);
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 25, 30 );
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		
		if (enemy.buff(Charm.class) != null ){
			int shield = (HP - HT) + (5 + damage);
			if (shield > 0){
				HP = HT;
				if (shield < 5){
					sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(5-shield), FloatingText.HEALING);
				}

				Buff.affect(this, Barrier.class).setShield(shield);
			} else {
				HP += 5 + damage;
				sprite.showStatusWithIcon(CharSprite.POSITIVE, "5", FloatingText.HEALING);
			}
			if (Dungeon.level.heroFOV[pos]) {
				Sample.INSTANCE.play( Assets.Sounds.CHARMS );
			}
		} else if (Random.Int( 3 ) == 0) {
			Charm c = Buff.affect( enemy, Charm.class, Charm.DURATION/2f );
			c.object = id();
			c.ignoreNextHit = true; //so that the -5 duration from succubus hit is ignored
			if (Dungeon.level.heroFOV[enemy.pos]) {
				enemy.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
				Sample.INSTANCE.play(Assets.Sounds.CHARMS);
			}
		}
		
		return damage;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (buff(ChampionEnemy.Paladin.class) != null){
			return true;
		}
		if (fieldOfView[target] && Dungeon.level.distance( pos, target ) > 2 && blinkCooldown <= 0 && buff(Talent.AntiMagicBuff.class) == null) {

			if (blink( target )) {
				spend(-1 / speed());
				return true;
			} else {
				return false;
			}
			
		} else {

			blinkCooldown--;
			return super.getCloser( target );
			
		}
	}
	
	private boolean blink( int target ) {
		
		Ballistica route = new Ballistica( pos, target, Ballistica.PROJECTILE);
		int cell = route.collisionPos;

		//can't occupy the same cell as another char, so move back one.
		if (Actor.findChar( cell ) != null && cell != this.pos)
			cell = route.path.get(route.dist-1);

		if (Dungeon.level.avoid[ cell ] || (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[cell])){
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.NEIGHBOURS8) {
				cell = route.collisionPos + n;
				if (Dungeon.level.passable[cell]
						&& Actor.findChar( cell ) == null
						&& (!properties().contains(Property.LARGE) || Dungeon.level.openSpace[cell])) {
					candidates.add( cell );
				}
			}
			if (candidates.size() > 0)
				cell = Random.element(candidates);
			else {
				blinkCooldown = Random.IntRange(4, 6);
				return false;
			}
		}
		
		ScrollOfTeleportation.appear( this, cell );

		blinkCooldown = Random.IntRange(4, 6);
		return true;
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 40;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 10);
	}

	@Override
	public Item createLoot() {
		Class<?extends Scroll> loot;
		do{
			loot = (Class<? extends Scroll>) Random.oneOf(Generator.Category.SCROLL.classes);
		} while (loot == ScrollOfIdentify.class || loot == ScrollOfUpgrade.class);

		return Reflection.newInstance(loot);
	}

	{
		immunities.add( Charm.class );
	}

	private static final String BLINK_CD = "blink_cd";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BLINK_CD, blinkCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		blinkCooldown = bundle.getInt(BLINK_CD);
	}
}
