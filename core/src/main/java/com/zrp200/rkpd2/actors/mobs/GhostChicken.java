/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 *  Shattered Pixel Dungeon
 *  Copyright (C) 2014-2022 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2022 TrashboxBobylev
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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.TimedShrink;
import com.zrp200.rkpd2.items.Generator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class GhostChicken extends AbyssalMob {

	{
		spriteClass = GhostChickenSprite.class;

		HP = HT = 3;
		defenseSkill = 40;
		baseSpeed = 3f;

		EXP = 13;

		loot = Generator.random();
		lootChance = 0.1f;

		properties.add(Property.DEMONIC);
		properties.add(Property.UNDEAD);
	}

	@Override
	protected boolean act() {
		boolean act = super.act();
		if (HT > 15){
			HP = HT = 15;
		}
		return act;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1 + abyssLevel()*2, 3 + abyssLevel()*4 );
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		Buff.prolong(enemy, TimedShrink.class, 2.5f + abyssLevel()*2.5f);
		return super.attackProc(enemy, damage);
	}

	@Override
	public int attackSkill( Char target ) {
		return 42;
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		HP--;
		if (HP <= 0) die(Dungeon.hero);
		aggro(Dungeon.hero);
		return true;
	}

	@Override
	public void hitSound(float pitch) {
		Sample.INSTANCE.play(Assets.Sounds.PUFF, 1, pitch);
	}

	@Override
	public float attackDelay() {
		return super.attackDelay()*0.25f;
	}
}
