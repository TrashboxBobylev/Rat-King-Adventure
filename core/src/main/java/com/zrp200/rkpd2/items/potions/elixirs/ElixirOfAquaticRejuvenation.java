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

package com.zrp200.rkpd2.items.potions.elixirs;

import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.items.potions.PotionOfHealing;
import com.zrp200.rkpd2.items.quest.GooBlob;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;

public class ElixirOfAquaticRejuvenation extends Elixir {
	
	{
		//TODO finish visuals
		image = ItemSpriteSheet.ELIXIR_AQUA;
	}
	
	@Override
	public void apply(Hero hero) {
		if (Dungeon.isChallenged(Challenges.NO_HEALING)){
			PotionOfHealing.pharmacophobiaProc(hero);
		} else {
			Buff.affect(hero, AquaHealing.class).set(Math.round(hero.HT * 1.5f));
		}
		HighnessBuff.agreenalineProc();
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (30 + 30);
	}
	
	public static class AquaHealing extends Buff {
		
		{
			type = buffType.POSITIVE;
			announced = true;
		}
		
		private int left;
		
		public void set( int amount ){
			if (amount > left) left = amount;
		}
		
		@Override
		public boolean act() {
			
			if (!target.flying && Dungeon.level.water[target.pos] && target.HP < target.HT){
				float healAmt = GameMath.gate( 1, target.HT/50f, left );
				healAmt = Math.min(healAmt, target.HT - target.HP);
				if (Random.Float() < (healAmt % 1)){
					healAmt = (float)Math.ceil(healAmt);
				} else {
					healAmt = (float)Math.floor(healAmt);
				}
				target.HP += (int)healAmt;
				left -= (int)healAmt;
				target.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString((int)healAmt), FloatingText.HEALING );

				if (target.HP >= target.HT) {
					target.HP = target.HT;
					if (target instanceof Hero) {
						((Hero) target).resting = false;
					}
				}
			}
			
			if (left <= 0){
				detach();
				if (target instanceof Hero) {
					((Hero) target).resting = false;
				}
			} else {
				spend(TICK);
			}
			return true;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.HEALING;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0, 0.75f, 0.75f);
		}

		@Override
		public float iconFadePercent() {
			float max = Math.round(target.HT * 1.5f);
			return Math.max(0, (max - left) / max);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", left);
		}
		
		private static final String LEFT = "left";
		
		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( LEFT, left );
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getInt( LEFT );
			
		}
	}
	
	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfHealing.class, GooBlob.class};
			inQuantity = new int[]{1, 1};
			
			cost = 6;
			
			output = ElixirOfAquaticRejuvenation.class;
			outQuantity = 1;
		}
		
	}
	
}
