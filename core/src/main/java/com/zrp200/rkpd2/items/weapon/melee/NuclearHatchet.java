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

package com.zrp200.rkpd2.items.weapon.melee;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.CorrosiveGas;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class NuclearHatchet extends MeleeWeapon {

	{
		image = ItemSpriteSheet.NUCLEAR_HATCHET;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 6;
		ACC = 1.5f; //50% boost to accuracy
	}

	@Override
	public int max(int lvl) {
		return  4*(tier-3) +    //12 base, down from 15
				lvl*(tier-3);   //scaling unchanged
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		GameScene.add( Blob.seed( enemy.pos, 500, CorrosiveGas.class ).setStrength((int) (2 + Dungeon.getDepth() /2.5f)));
		return super.warriorAttack(damage, enemy);
	}
}
