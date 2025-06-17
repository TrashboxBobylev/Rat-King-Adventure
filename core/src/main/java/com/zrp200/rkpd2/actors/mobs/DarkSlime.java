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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.items.quest.GooBlob;
import com.zrp200.rkpd2.items.rings.RingOfElements;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.Door;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CausticSlimeSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DarkSlime extends Mob {

	{
		spriteClass = CausticSlimeSprite.class;

		HP = HT = 64;
		defenseSkill = 23;

		EXP = 12;
		maxLvl = 25;

		loot = new GooBlob();
		lootChance = 0.1f;
		properties.add(Property.ACIDIC);
		properties.add(Property.DEMONIC);
		properties.add(Property.INORGANIC);
	}

	private static final float SPLIT_DELAY	= 1f;
	
	int generation	= 0;
	
	private static final String GENERATION	= "generation";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GENERATION, generation );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		generation = bundle.getInt( GENERATION );
		if (generation > 0) EXP = 0;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange(18, 23);
	}

    @Override
    public void damage(int dmg, Object src) {
        for (Class c : RingOfElements.RESISTS){
            if (c.isAssignableFrom(src.getClass()) && src instanceof Wand){
                dmg *= 3f;
            }
        }
        super.damage(dmg, src);
    }

    @Override
	public int defenseProc( Char enemy, int damage ) {

		if (HP >= damage + 2) {
			ArrayList<Integer> candidates = new ArrayList<>();
			boolean[] solid = Dungeon.level.passable;
			
			int[] neighbours = {pos + 1, pos - 1, pos + Dungeon.level.width(), pos - Dungeon.level.width()};
			for (int n : neighbours) {
				if (solid[n] && Actor.findChar( n ) == null) {
					candidates.add( n );
				}
			}
	
			if (candidates.size() > 0) {

				DarkSlime clone = split();
                HP = HT;
				HT *= 0.8f;
				clone.HT = clone.HP = HT;
				clone.pos = Random.element( candidates );
				clone.state = clone.HUNTING;
				Dungeon.level.pressCell(clone.pos);
				
				if (Dungeon.level.map[clone.pos] == Terrain.DOOR) {
					Door.enter( clone.pos );
				}
				
				GameScene.add( clone, SPLIT_DELAY );
				Actor.addDelayed( new Pushing( clone, pos, clone.pos ), -1 );

				HP = HT;
			}
		}
		
		return super.defenseProc(enemy, damage);
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 38;
	}
	
	private DarkSlime split() {
		DarkSlime clone = new DarkSlime();
		clone.generation = generation + 1;
		clone.EXP = 0;
		if (buff( Burning.class ) != null) {
			Buff.affect( clone, Burning.class ).reignite( clone );
		}
		if (buff( Poison.class ) != null) {
			Buff.affect( clone, Poison.class ).set(2);
		}
		if (buff(Corruption.class ) != null) {
			Buff.affect( clone, Corruption.class);
		}
		return clone;
	}
}
