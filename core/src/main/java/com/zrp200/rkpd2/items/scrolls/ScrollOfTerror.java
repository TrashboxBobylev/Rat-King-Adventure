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

package com.zrp200.rkpd2.items.scrolls;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class ScrollOfTerror extends Scroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_TERROR;
	}

	@Override
	public void doRead() {

		detach(curUser.belongings.backpack);
		new Flare( 5, 32 ).color( 0xFF0000, true ).show( curUser.sprite, 2f );
		Sample.INSTANCE.play( Assets.Sounds.READ );
		
		int count = 0;
		Mob affected = null;
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos]) {
				Buff.affect( mob, Terror.class, Terror.DURATION ).object = curUser.id();

				if (mob.buff(Terror.class) != null){
					count++;
					affected = mob;
				}
			}
		}
		
		switch (count) {
		case 0:
			GLog.i( Messages.get(this, "none") );
			break;
		case 1:
			GLog.i( Messages.get(this, "one", affected.name()) );
			break;
		default:
			GLog.i( Messages.get(this, "many") );
		}
		identify();

		readAnimation();
	}

	@Override
	public void empoweredRead() {
		doRead();
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Dungeon.level.heroFOV[mob.pos]) {
				Terror t = mob.buff(Terror.class);
				if (t != null){
					Buff.prolong(mob, Terror.class, 15f);
					Buff.affect(mob, Paralysis.class, 5f);
				}
			}
		}
	}
	
	@Override
	public int value() {
		return isKnown() ? 40 * quantity : super.value();
	}
}
