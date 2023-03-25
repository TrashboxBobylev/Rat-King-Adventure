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

package com.zrp200.rkpd2.items.potions;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.MindVision;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class PotionOfMindVision extends Potion {

	{
		icon = ItemSpriteSheet.Icons.POTION_MINDVIS;
	}

	@Override
	public void apply( Hero hero ) {
		identify();
		Buff.affect( hero, MindVision.class, MindVision.DURATION );
		SpellSprite.show(hero, SpellSprite.VISION, 1, 0.77f, 0.9f);
		Dungeon.observe();
		
		if (Dungeon.level.mobs.size() > 0) {
			GLog.i( Messages.get(this, "see_mobs") );
		} else {
			GLog.i( Messages.get(this, "see_none") );
		}
	}
	
	@Override
	public int value() {
		return isKnown() ? 30 * quantity : super.value();
	}
}
