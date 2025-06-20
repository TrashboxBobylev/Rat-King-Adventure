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

package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GravityChaosTracker extends Buff {

	{
		actPriority = BUFF_PRIO-10; //acts after other buffs
	}

	//lasts an average of 100 turns, with high variance
	public int left = Random.NormalIntRange(30, 70);

	public boolean positiveOnly;

	int idx;
	//used when tracking characters blocked by other characters
	ArrayList<Char> blocked = new ArrayList<>();

	@Override
	public boolean act() {

		//wait until all sprites have finished moving
		for (Char ch : Actor.chars()){
			try {
				synchronized (ch.sprite) {
					if (ch.sprite.isMoving) {
						ch.sprite.wait();
					}
				}
			} catch (InterruptedException e) {

			}
		}

		if (!blocked.isEmpty()){
			boolean blockedremoved = false;
			for (Char ch : blocked.toArray(new Char[0])){
				Ballistica path = new Ballistica(ch.pos, ch.pos + PathFinder.NEIGHBOURS8[idx], Ballistica.MAGIC_BOLT);
				if (!(path.dist == 1 && Actor.findChar(path.collisionPos) != null)){
					if (ch instanceof Hero) ((Hero) ch).interrupt();
					WandOfBlastWave.throwChar(ch, path, 3, false, false, this);
					blocked.remove(ch);
					blockedremoved = true;
				}
			}
			if (!blockedremoved || blocked.isEmpty()){
				blocked.clear();
				left--;
				if (left <= 0){
					GLog.w(Messages.get(CursedWand.class, "gravity_end"));
					Sample.INSTANCE.play(Assets.Sounds.DEGRADE);
					detach();
				} else {
					spend(Random.IntRange(1, 3));
				}
				return true;
			} else {
				return true;
			}
		}

		idx = Random.Int(PathFinder.NEIGHBOURS8.length);
		for (Char ch : Actor.chars()){
			if (Char.hasProp(ch, Char.Property.IMMOVABLE) ||
					(positiveOnly && ch.alignment == Char.Alignment.ALLY)){
				continue;
			} else {
				if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).SLEEPING){
					((Mob) ch).state = ((Mob) ch).WANDERING;
				}
				Ballistica path = new Ballistica(ch.pos, ch.pos + PathFinder.NEIGHBOURS8[idx], Ballistica.MAGIC_BOLT);
				if (path.dist == 1 && Actor.findChar(path.collisionPos) != null){
					blocked.add(ch);
				} else {
					if (ch instanceof Hero) ((Hero) ch).interrupt();
					WandOfBlastWave.throwChar(ch, path, 3, false, false, this);
				}
			}
		}

		//if anything was blocked, we don't spend here and re-act
		// so we can try to re-push chars blocked by other chars
		if (blocked.isEmpty()){
			left--;
			if (left <= 0){
				GLog.w(Messages.get(CursedWand.class, "gravity_end"));
				Sample.INSTANCE.play(Assets.Sounds.DEGRADE);
				detach();
			} else {
				spend(Random.IntRange(1, 3));
			}
		}
		return true;

	}

	private static final String LEFT = "left";
	private static final String POSITIVE_ONLY = "positive_only";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left);
		bundle.put(POSITIVE_ONLY, positiveOnly);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getInt(LEFT);
		positiveOnly = bundle.getBoolean(POSITIVE_ONLY);
	}
}
