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

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Barkskin extends Buff {
	
	{
		type = buffType.POSITIVE;
	}

	private int level = 0;
	private int interval = 1;
	
	@Override
	public boolean act() {
		if (target.isAlive()) {

			spend( interval );
			if (--level <= 0) {
				detach();
			}
			
		} else {
			
			detach();
			
		}
		
		return true;
	}
	
	public int level() {
		return level;
	}
	
	public void set( int value, int time ) {
		if (level <= value) {
			level = value;
			interval = time;
			spend(time - cooldown() - 1);
		}
	}

	public void delay( float value ){
		spend(value);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.BARKSKIN;
	}

	private static final float BARKSKIN_MODIFIER = .8f;
	public static int getGrassDuration(Hero hero) {
		float modifier = 0;
		if(hero.hasTalent(Talent.BARKSKIN, Talent.RK_WARDEN)) {
			modifier = hero.byTalent(
					Talent.BARKSKIN, BARKSKIN_MODIFIER,
					Talent.RK_WARDEN, .5f);
		} else if(hero.canHaveTalent(Talent.BARKSKIN)) {
			// level-shifted, but with +1 being twice as effective.
			// I think this is the proper way to do level-shifting.
			modifier = BARKSKIN_MODIFIER/2;
		}
		return (int)(hero.lvl*modifier);
	}

	@Override
	public float iconFadePercent() {
		if (target instanceof Hero){
			Hero hero = (Hero)target;
			int max = Math.max(getGrassDuration(hero), 2+hero.lvl/3);
			return Math.max(0, (max-level)/max);
		}
		return 0;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(level);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", level, dispTurns(visualcooldown()));
	}
	
	private static final String LEVEL	    = "level";
	private static final String INTERVAL    = "interval";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( INTERVAL, interval );
		bundle.put( LEVEL, level );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		interval = bundle.getInt( INTERVAL );
		level = bundle.getInt( LEVEL );
	}

	//These two methods allow for multiple instances of barkskin to stack in terms of duration
	// but only the stronger bonus is applied

	public static int currentLevel(Char ch ){
		int level = 0;
		for (Barkskin b : ch.buffs(Barkskin.class)){
			level = Math.max(level, b.level);
		}
		return level;
	}

	//reset if a matching buff exists, otherwise append
	public static void conditionallyAppend(Char ch, int level, int interval){
		for (Barkskin b : ch.buffs(Barkskin.class)){
			if (b.interval == interval){
				b.set(level, interval);
				return;
			}
		}
		Buff.append(ch, Barkskin.class).set(level, interval);
	}
}
