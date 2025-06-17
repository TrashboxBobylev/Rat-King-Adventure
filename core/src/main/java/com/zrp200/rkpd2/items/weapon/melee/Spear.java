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
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class Spear extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SPEAR;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 0.9f;

		tier = 2;
		DLY = 1.5f; //0.67x speed
		RCH = 2;    //extra reach
	}

	@Override
	public int max(int lvl) {
		return  Math.round(6.67f*(tier+1)) +    //20 base, up from 15
				lvl*Math.round(1.33f*(tier+1)); //+4 per level, up from +3
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		if (Dungeon.hero.lastMovPos != -1 &&
				Dungeon.level.distance(Dungeon.hero.lastMovPos, enemy.pos) >
						Dungeon.level.distance(Dungeon.hero.pos, enemy.pos)){
			Dungeon.hero.lastMovPos = -1;
			Sample.INSTANCE.play(Assets.Sounds.HEALTH_WARN);
			return damage*2;
		}
		return damage;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected DuelistAbility duelistAbility() {
		// 1.45 at t2, 1.3 at t5
		return new MeleeAbility(1.45f - (tier-2)/20f) {
			@Override
			protected boolean canAttack(Hero hero, Char enemy) {
				return super.canAttack(hero, enemy) && !Dungeon.level.adjacent(hero.pos, enemy.pos);
			}
			int oldPos;

			@Override
			protected void beforeAbilityUsed(Hero hero, Char target) {
				super.beforeAbilityUsed(hero, target);
				if(target != null) oldPos = target.pos;
			}

			@Override
			protected void proc(Hero hero, Char enemy) {
				if (enemy.pos != oldPos) return;
				//trace a ballistica to our target (which will also extend past them
				Ballistica trajectory = new Ballistica(hero.pos, enemy.pos, Ballistica.STOP_TARGET);
				//trim it to just be the part that goes past them
				trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
				//knock them back along that ballistica
				WandOfBlastWave.throwChar(enemy, trajectory, 1, true, false, hero.getClass());
			}
		};
	}

}
