/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
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

package com.zrp200.rkpd2.plants;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.BlobImmunity;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.MagicalSleep;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class Dreamfoil extends Plant {

	{
		image = 7;
		seedClass = Seed.class;
	}

	@Override
	public void activate( Char ch ) {

		if (ch != null) {
			if (ch instanceof Mob) {
				Buff.affect(ch, MagicalSleep.class);
			} else if (ch instanceof Hero) {
				GLog.i( Messages.get(this, "refreshed") );

				if (((Hero)ch).subClass == HeroSubClass.WARDEN || ((Hero)ch).subClass == HeroSubClass.KING){
					Buff.affect(ch, BlobImmunity.class, BlobImmunity.DURATION/2f);
				}
			}
		}
	}

	public static class Seed extends Plant.Seed {
		{
			image = ItemSpriteSheet.SEED_MAGEROYAL;

			plantClass = Dreamfoil.class;
		}
	}
}