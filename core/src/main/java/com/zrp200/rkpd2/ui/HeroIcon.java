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

package com.zrp200.rkpd2.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;

//icons for hero subclasses and abilities atm, maybe add classes?
public class HeroIcon extends Image {

	private static TextureFilm film;
	private static final int SIZE = 16;

	//transparent icon
	public static final int NONE    = 63;

	//subclasses
	public static final int BERSERKER   = 0;
	public static final int GLADIATOR   = 1;
	public static final int BATTLEMAGE  = 2;
	public static final int WARLOCK     = 3;
	public static final int ASSASSIN    = 4;
	public static final int FREERUNNER  = 5;
	public static final int SNIPER      = 6;
	public static final int WARDEN      = 7;
	public static final int CHAMPION    = 8;
	public static final int MONK        = 9;
	public static final int KING = 14;

	//abilities
	public static final int HEROIC_LEAP     = 16;
	public static final int SHOCKWAVE       = 17;
	public static final int ENDURE          = 18;
	public static final int ELEMENTAL_BLAST = 19;
	public static final int WILD_MAGIC      = 20;
	public static final int WARP_BEACON     = 21;
	public static final int SMOKE_BOMB      = 22;
	public static final int DEATH_MARK      = 23;
	public static final int SHADOW_CLONE    = 24;
	public static final int SPECTRAL_BLADES = 25;
	public static final int NATURES_POWER   = 26;
	public static final int SPIRIT_HAWK     = 27;
	public static final int CHALLENGE       = 28;
	public static final int ELEMENTAL_STRIKE= 29;
	public static final int FEINT           = 30;
	public static final int RATMOGRIFY      = 31;
	public static final int WRATH = 15;

	//action indicator visuals
	public static final int BERSERK         = 32;
	public static final int COMBO           = 33;
	public static final int PREPARATION     = 34;
	public static final int MOMENTUM        = 35;
	public static final int SNIPERS_MARK    = 36;
	public static final int WEAPON_SWAP     = 37;
	public static final int MONK_ABILITIES  = 38;

	//RKA hero/ability icons
	public static final int BRAWLER         = imageAt(0, 6);
	public static final int SPIRITUALIST    = imageAt(1, 6);
	public static final int DECEPTICON      = imageAt(2, 6);
	public static final int HIGHNESS        = imageAt(3, 6);
	public static final int CHAMP           = imageAt(5, 6);

	public static final int LEGACYWRATH     = imageAt(6, 6);
	public static final int MUS_REX         = imageAt(7, 6);

	public static final int BRAWLING        = imageAt(0, 7);
	public static final int CLOAK_TELEPORT  = imageAt(1, 7);
	public static final int BLOCKING        = imageAt(2, 7);
	public static final int HIGHNESS_STOP   = imageAt(3, 7);


	public HeroIcon(HeroSubClass subCls){
		super( Assets.Interfaces.HERO_ICONS );
		if (film == null){
			film = new TextureFilm(texture, SIZE, SIZE);
		}
		frame(film.get(subCls.icon()));
	}

	public HeroIcon(ArmorAbility abil){
		super( Assets.Interfaces.HERO_ICONS );
		if (film == null){
			film = new TextureFilm(texture, SIZE, SIZE);
		}
		frame(film.get(abil.icon()));
	}

	public HeroIcon(ActionIndicator.Action action){
		super( Assets.Interfaces.HERO_ICONS );
		if (film == null){
			film = new TextureFilm(texture, SIZE, SIZE);
		}
		frame(film.get(action.actionIcon()));
	}

	public static int imageAt(int x, int y){
		return x + y*8;
	}

}
