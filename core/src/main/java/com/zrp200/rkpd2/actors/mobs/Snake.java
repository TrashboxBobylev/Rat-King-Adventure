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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.journal.Document;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.SnakeSprite;
import com.watabou.utils.Random;

public class Snake extends Mob {
	
	{
		spriteClass = SnakeSprite.class;
		
		HP = HT = 4;
		defenseSkill = 25;
		
		EXP = 2;
		maxLvl = 7;
		
		loot = Generator.Category.SEED;
		lootChance = 0.25f;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 4 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	private static int dodges = 0;

	@Override
	public String defenseVerb() {
		dodges++;
		if ((dodges >= 2 && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS))
				|| (dodges >= 4 && !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_1))){
			GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS);
			dodges = 0;
		}
		return super.defenseVerb();
	}
}
