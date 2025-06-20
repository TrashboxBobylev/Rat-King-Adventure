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

package com.zrp200.rkpd2.items.scrolls.exotic;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.PrismaticGuard;
import com.zrp200.rkpd2.actors.hero.spells.Stasis;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.npcs.PrismaticImage;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class ScrollOfPrismaticImage extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_PRISIMG;
	}
	
	@Override
	public void doRead() {

		detach(curUser.belongings.backpack);
		boolean found = false;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
			if (m instanceof PrismaticImage){
				found = true;
				m.HP = m.HT;
				m.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(m.HT), FloatingText.HEALING );
			}
		}

		if (!found){
			if (Stasis.getStasisAlly() instanceof PrismaticImage){
				found = true;
				Stasis.getStasisAlly().HP = Stasis.getStasisAlly().HT;
			}
		}
		
		if (!found) {
			Buff.affect(curUser, PrismaticGuard.class).set( PrismaticGuard.maxHP( curUser ) );
		}

		identify();
		
		Sample.INSTANCE.play( Assets.Sounds.READ );
	
		readAnimation();
	}
}
