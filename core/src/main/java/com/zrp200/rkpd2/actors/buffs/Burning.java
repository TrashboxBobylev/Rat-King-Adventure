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
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.Thief;
import com.zrp200.rkpd2.effects.particles.ElmoParticle;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.armor.glyphs.Brimstone;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.food.ChargrilledMeat;
import com.zrp200.rkpd2.items.food.FrozenCarpaccio;
import com.zrp200.rkpd2.items.food.MysteryMeat;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Burning extends Buff implements Hero.Doom, DamageOverTimeEffect {
	
	public static final float DURATION = 8f;
	
	public float left;
	private boolean acted = false; //whether the debuff has done any damage at all yet
	private int burnIncrement = 0; //for tracking burning of hero items
	
	private static final String LEFT	= "left";
	private static final String ACTED	= "acted";
	private static final String BURN	= "burnIncrement";

	{
		type = buffType.NEGATIVE;
		actPriority = BUFF_PRIO - 1;
		announced = true;
	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
		bundle.put( ACTED, acted );
		bundle.put( BURN, burnIncrement );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		left = bundle.getFloat( LEFT );
		acted = bundle.getBoolean( ACTED );
		burnIncrement = bundle.getInt( BURN );
	}

	@Override
	public boolean attachTo(Char target) {
		Buff.detach( target, Chill.class);

		return super.attachTo(target);
	}

	@Override
	public boolean act() {

		if (acted && Dungeon.level.water[target.pos] && !target.flying){
			detach();
		} else if (target.isAlive() && !target.isImmune(getClass())) {

			acted = true;
			int damage = Random.NormalIntRange( 1, 3 + Dungeon.scalingDepth()/4 );
			damage *= (1 + Dungeon.hero.pointsInTalent(Talent.PYROMANIAC, Talent.RK_FIRE)*0.125f);

			Buff.detach( target, Chill.class);

			if (target instanceof Hero
					&& target.buff(TimekeepersHourglass.Stasis.class) == null
					&& target.buff(TimeStasis.class) == null) {
				
				Hero hero = (Hero)target;

				hero.damage( damage, this );
				burnIncrement++;

				//at 4+ turns, there is a (turns-3)/3 chance an item burns
				if (Random.Int(3) < (burnIncrement - 3)){
					burnIncrement = 0;

					ArrayList<Item> burnable = new ArrayList<>();
					//does not reach inside of containers
					if (!hero.belongings.lostInventory()) {
						for (Item i : hero.belongings.backpack.items) {
							if (!i.unique && (i instanceof Scroll || i instanceof MysteryMeat || i instanceof FrozenCarpaccio)) {
								burnable.add(i);
							}
						}
					}

					if (!burnable.isEmpty()){
						Item toBurn = Random.element(burnable).detach(hero.belongings.backpack);
						GLog.w( Messages.capitalize(Messages.get(this, "burnsup", toBurn.title())) );
						if (toBurn instanceof MysteryMeat || toBurn instanceof FrozenCarpaccio){
							ChargrilledMeat steak = new ChargrilledMeat();
							if (!steak.collect( hero.belongings.backpack )) {
								Dungeon.level.drop( steak, hero.pos ).sprite.drop();
							}
						}
						Heap.burnFX( hero.pos );
					}
				}
				
			} else {
				target.damage( damage, this );
			}

			if (target instanceof Thief && ((Thief) target).item != null) {

				Item item = ((Thief) target).item;

				if (!item.unique && item instanceof Scroll) {
					target.sprite.emitter().burst( ElmoParticle.FACTORY, 6 );
					((Thief)target).item = null;
				} else if (item instanceof MysteryMeat) {
					target.sprite.emitter().burst( ElmoParticle.FACTORY, 6 );
					((Thief)target).item = new ChargrilledMeat();
				}

			}

		} else {

			detach();
		}
		
		if (Dungeon.level.flamable[target.pos] && Blob.volumeAt(target.pos, Fire.class) == 0) {
			GameScene.add( Blob.seed( target.pos, 4, Fire.class ) );
		}
		
		spend( TICK );
		left -= TICK;
		
		if (left <= 0 ||
			(Dungeon.level.water[target.pos] && !target.flying)) {
			
			detach();
		}
		
		return true;
	}
	
	public void reignite( Char ch ) {
		reignite( ch, DURATION );
	}
	
	public void reignite( Char ch, float duration ) {
		if (ch.isImmune(Burning.class)){
			if (ch.glyphLevel(Brimstone.class) >= 0){
				//generate avg of 1 shield per turn per 50% boost, to a max of 4x boost
				float shieldChance = 2*(Armor.Glyph.genericProcChanceMultiplier(ch) - 1f);
				int shieldCap = Math.round(shieldChance*4f);
				int shieldGain = (int)shieldChance;
				if (Random.Float() < shieldChance%1) shieldGain++;
				if (shieldCap > 0 && shieldGain > 0){
					Barrier barrier = Buff.affect(ch, Barrier.class);
					if (barrier.shielding() < shieldCap){
						barrier.incShield(1);
					}
				}
			}
		}
		if (left < duration) left = duration;
		acted = false;
	}

	public void extend( float duration ) {
		left += duration;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.FIRE;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - left) / DURATION);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)left);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.BURNING);
		else target.sprite.remove(CharSprite.State.BURNING);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(left));
	}

	@Override
	public void onDeath() {
		
		Badges.validateDeathFromFire();
		
		Dungeon.fail( this );
		GLog.n( Messages.get(this, "ondeath") );
	}
}
