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

package com.zrp200.rkpd2.items.potions;

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class PotionOfStrength extends Potion {

	{
		icon = ItemSpriteSheet.Icons.POTION_STRENGTH;

		unique = true;

		talentFactor = 2f;
	}
	
	@Override
	public void apply( Hero hero ) {
		identify();

		hero.STR++;
		hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, "1", FloatingText.STRENGTH);

		GLog.p( Messages.get(this, "msg", hero.STR()) );
		
		Badges.validateStrengthAttained();
		Badges.validateDuelistUnlock();
	}

	@Override
	public int value() {
		return isKnown() ? 50 * quantity : super.value();
	}

	@Override
	public int energyVal() {
		return isKnown() ? 10 * quantity : super.energyVal();
	}
}
