/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.zrp200.rkpd2.actors.hero;

import com.watabou.noosa.Game;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.HeroIcon;

public enum HeroSubClass {

	NONE(HeroIcon.NONE),

	BERSERKER(HeroIcon.BERSERKER),
	GLADIATOR(HeroIcon.GLADIATOR),

	BATTLEMAGE(HeroIcon.BATTLEMAGE) {
		@Override public int getBonus(Item item) {
			// mage boost now also applies to staff...
			return item instanceof MagesStaff ? 2 : 0;
		}

		@Override public String desc() {
			//Include the staff effect description in the battlemage's desc if possible
			String desc = super.desc();
			if (Game.scene() instanceof GameScene){
				MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
				if (staff != null && staff.wandClass() != null){
					desc += "\n\n" + Messages.get(staff.wandClass(), "bmage_desc", Messages.titleCase(title()));
					desc = desc.replaceAll("_", "");
				}
			}
			return desc;
		}
	},
	WARLOCK(HeroIcon.WARLOCK),

	ASSASSIN(HeroIcon.ASSASSIN) {
		@Override public int getBonus(Item item) {
			// +2 to melee / +2 to thrown. total boosts = 4
			return item instanceof Weapon ? 2 : 0;
		}
	},
	FREERUNNER(HeroIcon.FREERUNNER) {
		@Override
		public int getBonus(Item item) {
			// +1 to wands* (+freerun bonus), +2 to missiles, +1 to anything with reach. total boosts = 4 before other modifiers. note that freerunner has easy access to gamebreaking mechanics.
			return item instanceof MissileWeapon ? 2
					: item instanceof Wand ? 1
					: Dungeon.hero != null && item instanceof Weapon && ((Weapon) item).reachFactor(Dungeon.hero) > 1 ? 1
					: 0;
		}
	},

	SNIPER(HeroIcon.SNIPER),
	WARDEN(HeroIcon.WARDEN),

	SPIRITUALIST(HeroIcon.SPIRITUALIST),
	BRAWLER(HeroIcon.BRAWLER),
	DECEPTICON(HeroIcon.DECEPTICON),

	KING(HeroIcon.KING);

	public static void set(Hero hero, HeroSubClass subClass) {
		hero.subClass = subClass;
		Talent.initSubclassTalents(hero);
	}

	public final int icon;

	HeroSubClass(int icon){
		this.icon = icon;
	}

	// this corresponds to the one in HeroClass
	public int getBonus(Item item) { return 0; }

	public String title() {
		return Messages.get(this, name());
	}

	public String shortDesc() {
		return Messages.get(this, name()+"_short_desc");
	}

	public String desc() {
		return Messages.get(this, name() + "_desc");
	}

	public int icon(){
		return icon;

		// fixme KING: new ItemSprite(ItemSpriteSheet.ARMOR_RAT_KING);
	}

	public Badges.Badge secretBadge() {
		switch (this) {
			case BRAWLER:
				return Badges.Badge.WON_BRAWLER;
			case SPIRITUALIST:
				return Badges.Badge.WON_SPIRITCALLER;
			case DECEPTICON:
				return Badges.Badge.WON_DECEPTICON;
			case WARLOCK:
				return Badges.Badge.WON_WARLOCK;
		}
		return null;
	}

}
