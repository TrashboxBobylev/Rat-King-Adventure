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

package com.zrp200.rkpd2.items.food;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.buffs.ArtifactRecharge;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.Holiday;
import com.watabou.noosa.audio.Sample;

public class Pasty extends Food {

	{
		reset();

		energy = Hunger.STARVING;

		bones = true;
	}
	
	@Override
	public void reset() {
		super.reset();
		switch(Holiday.getCurrentHoliday()){
			case NONE: default:
				image = ItemSpriteSheet.PASTY;
				break;
			case LUNAR_NEW_YEAR:
				image = ItemSpriteSheet.STEAMED_FISH;
				break;
			case APRIL_FOOLS:
				image = ItemSpriteSheet.CHOC_AMULET;
				break;
			case EASTER:
				image = ItemSpriteSheet.EASTER_EGG;
				break;
			case HALLOWEEN:
				image = ItemSpriteSheet.PUMPKIN_PIE;
				break;
			case WINTER_HOLIDAYS:
				image = ItemSpriteSheet.CANDY_CANE;
				break;
		}
	}
	
	@Override
	protected void satisfy(Hero hero) {
		if (Holiday.getCurrentHoliday() == Holiday.LUNAR_NEW_YEAR){
			//main item only clears 300 hunger on lunar new year...
			energy = Hunger.HUNGRY;
		}

		super.satisfy(hero);
		
		switch(Holiday.getCurrentHoliday()){
			default:
				break; //do nothing extra
			case LUNAR_NEW_YEAR:
				//...but it also awards an extra item that restores 150 hunger
				FishLeftover left = new FishLeftover();
				if (!left.collect()){
					Dungeon.level.drop(left, hero.pos).sprite.drop();
				}
				break;
			case APRIL_FOOLS:
				Sample.INSTANCE.play(Assets.Sounds.MIMIC);
			case EASTER:
				ArtifactRecharge.chargeArtifacts(hero, 2f);
				ScrollOfRecharging.charge( hero );
				break;
			case HALLOWEEN:
				//heals for 5% max hp, min of 3
				int toHeal = Math.max(3, hero.HT/20);
				hero.HP = Math.min(hero.HP + toHeal, hero.HT);
				hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(toHeal), FloatingText.HEALING );
				break;
			case WINTER_HOLIDAYS:
				hero.belongings.charge(0.5f); //2 turns worth
				ScrollOfRecharging.charge( hero );
				break;
		}
	}

	@Override
	public String name() {
		String name = super.name();
		switch(Holiday.getCurrentHoliday()){
			case NONE: default:
				return super.name();
			case LUNAR_NEW_YEAR:
				name = Messages.get(this, "fish_name");
			case APRIL_FOOLS:
				name = Messages.get(this, "amulet_name");
			case EASTER:
				name = Messages.get(this, "egg_name");
			case HALLOWEEN:
				name = Messages.get(this, "pie_name");
			case WINTER_HOLIDAYS:
				name = Messages.get(this, "cane_name");
		}
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
			return ShatteredPixelDungeon.turnIntoRrrr(name);
		}
		return name;
	}

	@Override
	public String info() {
		switch(Holiday.getCurrentHoliday()){
			case NONE: default:
				return super.info();
			case LUNAR_NEW_YEAR:
				return Messages.get(this, "fish_desc");
			case APRIL_FOOLS:
				return Messages.get(this, "amulet_desc");
			case EASTER:
				return Messages.get(this, "egg_desc");
			case HALLOWEEN:
				return Messages.get(this, "pie_desc");
			case WINTER_HOLIDAYS:
				return Messages.get(this, "cane_desc");
		}
	}
	
	@Override
	public int value() {
		return 20 * quantity;
	}

	public static class FishLeftover extends Food {

		{
			image = ItemSpriteSheet.FISH_LEFTOVER;
			energy = Hunger.HUNGRY/2;
		}

		@Override
		public int value() {
			return 10 * quantity;
		}
	}
}
