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

package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.FrostFire;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.RatKingBoss;
import com.zrp200.rkpd2.actors.mobs.Thief;
import com.zrp200.rkpd2.effects.particles.ElmoParticle;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.food.ChargrilledMeat;
import com.zrp200.rkpd2.items.food.FrozenCarpaccio;
import com.zrp200.rkpd2.items.food.MysteryMeat;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfUpgrade;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FrostBurn extends Buff implements Hero.Doom, DamageOverTimeEffect {
	
	private static final float DURATION = 8f;
	
	private float left;
	
	//for tracking burning of hero items
	private int burnIncrement = 0;
	
	private static final String LEFT	= "left";
	private static final String BURN	= "burnIncrement";

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
		bundle.put( BURN, burnIncrement );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		left = bundle.getFloat( LEFT );
		burnIncrement = bundle.getInt( BURN );
	}

	@Override
	public boolean act() {
		
		if (target.isAlive() && !target.isImmune(getClass())) {
			
			int damage = Random.NormalIntRange( 1, 2 + Dungeon.scalingDepth() /4 );

			if (target instanceof Hero) {
				
				Hero hero = (Hero)target;

				hero.damage( damage, this );
				burnIncrement++;

				//at 4+ turns, there is a (turns-3)/3 chance an item burns
				if (Random.Int(3) < (burnIncrement - 3)){
					burnIncrement = 0;

					ArrayList<Item> burnable = new ArrayList<>();
					//does not reach inside of containers
					for (Item i : hero.belongings.backpack.items){
						if ((i instanceof Scroll && !(i instanceof ScrollOfUpgrade))
								|| i instanceof MysteryMeat){
							burnable.add(i);
						}
					}

					if (!burnable.isEmpty()){
						Item toBurn = Random.element(burnable).detach(hero.belongings.backpack);
						GLog.w( Messages.get(this, "burnsup", Messages.capitalize(toBurn.toString())) );
						if (toBurn instanceof MysteryMeat){
							FrozenCarpaccio steak = new FrozenCarpaccio();
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

			if (target instanceof Thief) {

				Item item = ((Thief) target).item;

				if (item instanceof Scroll &&
						!(item instanceof ScrollOfUpgrade)) {
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
		
		if (Dungeon.level.flamable[target.pos] && Blob.volumeAt(target.pos, FrostFire.class) == 0) {
			GameScene.add( Blob.seed( target.pos, 4, FrostFire.class ) );
		}
		
		spend( TICK );
		left -= TICK;
		
		if (left <= 0) {
			
			detach();
		}
		
		return true;
	}

    //reduces speed by 10% for every turn remaining, capping at 50%
    public float speedFactor(){
		float mod = 0.1f;
		if (target instanceof RatKingBoss){
			mod = 0.05f;
		}
		return Math.max(0.5f, 1 - left*mod);
    }
	
	public void reignite( Char ch ) {
		reignite( ch, DURATION );
	}
	
	public void reignite( Char ch, float duration ) {
		left = duration;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.FROSTBURN;
	}

	@Override
	public void fx(boolean on) {
		if (on) {
            target.sprite.add(CharSprite.State.CHILLED);
		    target.sprite.add(CharSprite.State.FROSTBURNING);
		}

		else {
		    target.sprite.remove(CharSprite.State.FROSTBURNING);
            target.sprite.remove(CharSprite.State.CHILLED);
		}
	}

	@Override
	public String heroMessage() {
		return Messages.get(this, "heromsg");
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns(left+1f), new DecimalFormat("#.##").format((1f-speedFactor())*100f));
    }

	@Override
	public void onDeath() {
		
		Badges.validateDeathFromFire();
		
		Dungeon.fail( getClass() );
		GLog.n( Messages.get(this, "ondeath") );
	}
}
