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
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;

public class PotionOfExperience extends Potion implements Hero.Doom {

	{
		icon = ItemSpriteSheet.Icons.POTION_EXP;

		bones = true;

		talentFactor = 2f;
	}
	
	@Override
	public void apply( Hero hero ) {
		identify();
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.LEVELLING_DOWN))
			hero.sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(5 + 5* hero.lvl), FloatingText.EXPERIENCE);
		else
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(5 + 5* hero.lvl), FloatingText.EXPERIENCE);
		hero.earnExp( 5 + 5* hero.lvl, getClass() );
		new Flare( 6, 32 ).color(0xFFFF00, true).show( curUser.sprite, 2f );
	}

	public static void levelUp(int level){
		for (int i = 0; i < level; i++){
			Dungeon.hero.earnExp( 5 + 5* Dungeon.hero.lvl, PotionOfExperience.class );
		}
	}
	
	@Override
	public int value() {
		return isKnown() ? 50 * quantity : super.value();
	}

	@Override
	public int energyVal() {
		return isKnown() ? 10 * quantity : super.energyVal();
	}

	@Override
	public void onDeath() {

		Badges.validateDeathFromSacrifice();

		Dungeon.fail( this );
		GLog.n( Messages.get(this, "ondeath") );
	}
}
