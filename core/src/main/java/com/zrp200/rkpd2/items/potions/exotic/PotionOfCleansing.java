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

package com.zrp200.rkpd2.items.potions.exotic;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.LostInventory;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

public class PotionOfCleansing extends ExoticPotion {
	
	{
		icon = ItemSpriteSheet.Icons.POTION_CLEANSE;
	}
	
	@Override
	public void apply( Hero hero ) {
		identify();
		
		cleanse( hero );
		new Flare( 6, 32 ).color(0xFF4CD2, true).show( curUser.sprite, 2f );
	}
	
	@Override
	public void shatter(int cell) {
		if (Actor.findChar(cell) == null){
			super.shatter(cell);
		} else {
			splash( cell );
			if (Dungeon.level.heroFOV[cell]) {
				Sample.INSTANCE.play(Assets.Sounds.SHATTER);
				identify();
			}
			
			if (Actor.findChar(cell) != null){
				cleanse(Actor.findChar(cell));
			}
		}
	}

	public static void cleanse(Char ch){
		cleanse(ch, Cleanse.DURATION);
	}

	public static void cleanse(Char ch, float duration){
		for (Buff b : ch.buffs()){
			if (b.type == Buff.buffType.NEGATIVE
					&& !(b instanceof AllyBuff)
					&& !(b instanceof LostInventory)){
				b.detach();
			}
			if (b instanceof Hunger){
				((Hunger) b).satisfy(Hunger.STARVING);
			}
		}
		Buff.affect(ch, Cleanse.class, duration);
	}

	public static class Cleanse extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		public static final float DURATION = 5f;

		@Override
		public int icon() {
			return BuffIndicator.IMMUNITY;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0f, 2f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

	}
}
