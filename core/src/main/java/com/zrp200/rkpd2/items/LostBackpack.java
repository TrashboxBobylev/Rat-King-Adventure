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

package com.zrp200.rkpd2.items;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.LostInventory;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.artifacts.CloakOfShadows;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.bags.MagicalHolster;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.HeroSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class LostBackpack extends Item {

	{
		image = ItemSpriteSheet.BACKPACK;

		unique = true;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		if (hero.buff(LostInventory.class) != null){
			hero.buff(LostInventory.class).detach();
		}

		MagicalHolster holster = hero.belongings.getItem(MagicalHolster.class);
		for (Item i : hero.belongings){
			if (i.keptThroughLostInventory()){
				i.keptThoughLostInvent = false; //don't reactivate, was previously activated
			} else {
				if (i instanceof EquipableItem && i.isEquipped(hero)){
					((EquipableItem) i).activate(hero);
				} else if ( i instanceof CloakOfShadows && hero.shiftedPoints(Talent.LIGHT_CLOAK, Talent.RK_FREERUNNER) > 0){
					((CloakOfShadows) i).activate(hero);
				} else if ( i instanceof HolyTome && hero.canHaveTalent(Talent.LIGHT_READING)){
					((HolyTome) i).activate(hero);
				} else if (i instanceof Wand){
					if (holster != null && holster.contains(i)){
						((Wand) i).charge(hero, MagicalHolster.HOLSTER_SCALE_FACTOR);
					} else {
						((Wand) i).charge(hero);
					}
				} else if (i instanceof MagesStaff){
					((MagesStaff) i).applyWandChargeBuff(hero);
				}
			}
		}

		hero.updateHT(false);

		Item.updateQuickslot();
		Sample.INSTANCE.play( Assets.Sounds.DEWDROP );
		hero.spendAndNext(TIME_TO_PICK_UP);
		GameScene.pickUp( this, pos );
		((HeroSprite)hero.sprite).updateArmor();
		return true;
	}
}
