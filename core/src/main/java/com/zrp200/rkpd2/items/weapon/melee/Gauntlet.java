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

package com.zrp200.rkpd2.items.weapon.melee;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class Gauntlet extends Sai {
	
	{
		image = ItemSpriteSheet.GAUNTLETS;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1.2f;
		
		tier = 5;

		//15 base, down from 30
		//+3 per level, down from +6

		//+(5+lvl) damage, roughly +50% base damage, +50% scaling
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		ArrayList<Char> affectedChars = new ArrayList<>();
		Ballistica trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
		ConeAOE cone = new ConeAOE(
				trajectory,
				5,
				90,
				Ballistica.MAGIC_BOLT
		);
		for (int cell : cone.cells){
			CellEmitter.bottom(cell).burst(Speck.factory(Speck.STEAM), 10);
			Char ch = Actor.findChar( cell );
			if (ch != null && !ch.equals(enemy)) {
				affectedChars.add(ch);
			}
		}
		for (Char ch : affectedChars){
			int dmg = Dungeon.hero.attackProc(ch, damage);
			switch (Dungeon.level.distance(ch.pos, Dungeon.hero.pos)){
				case 2: dmg *= 0.66f; break;
				case 3: dmg *= 0.33f; break;
				case 4: dmg *= 0.16f; break;
				case 5: dmg *= 0.1f; break;
			}
			dmg -= ch.drRoll();
			dmg = ch.defenseProc(Dungeon.hero, dmg);
			ch.damage(dmg, Dungeon.hero);
		}
		Sample.INSTANCE.play(Assets.Sounds.ROCKS);
		Camera.main.shake( 3, 0.7f );
		return super.warriorAttack(damage, enemy);
	}

}
