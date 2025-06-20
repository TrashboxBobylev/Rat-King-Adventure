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

package com.zrp200.rkpd2.items.stones;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;

public class StoneOfAggression extends Runestone {
	
	{
		image = ItemSpriteSheet.STONE_AGGRESSION;
	}
	
	@Override
	protected void activate(int cell) {
		
		Char ch = Actor.findChar( cell );
		
		if (ch != null) {
			if (Char.hasProp(ch, Char.Property.BOSS) || Char.hasProp(ch, Char.Property.MINIBOSS)) {
				Buff.prolong(ch, Aggression.class, Aggression.DURATION / 4f);
			} else {
				Buff.prolong(ch, Aggression.class, Aggression.DURATION);
			}
		}

		CellEmitter.center(cell).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
		Sample.INSTANCE.play( Assets.Sounds.READ );
		
	}

	public static class Aggression extends FlavourBuff {
		
		public static final float DURATION = 20f;
		
		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		@Override
		public int icon() {
			return BuffIndicator.TARGETED;
		}

		@Override
		public float iconFadePercent() {
			if (Char.hasProp(target, Char.Property.BOSS) || Char.hasProp(target, Char.Property.MINIBOSS)){
				return Math.max(0, (DURATION/4f - visualcooldown()) / (DURATION/4f));
			} else {
				return Math.max(0, (DURATION - visualcooldown()) / DURATION);
			}
		}

		@Override
		public void detach() {
			//if our target is an enemy, reset any enemy-to-enemy aggro involving it
			if (target.isAlive()) {
				if (target.alignment == Char.Alignment.ENEMY) {
					for (Mob m : Dungeon.level.mobs) {
						if (m.alignment == Char.Alignment.ENEMY && m.isTargeting(target)) {
							m.aggro(null);
						}
						if (target instanceof Mob && ((Mob) target).isTargeting(m)){
							((Mob) target).aggro(null);
						}
					}
				}
			}
			super.detach();
			
		}

	}
	
}
