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

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;

import java.util.HashSet;

public class Buff extends Actor {
	
	public Char target;

	{
		actPriority = BUFF_PRIO; //low priority, towards the end of a turn
	}

	//determines how the buff is announced when it is shown.
	public enum buffType {POSITIVE, NEGATIVE, NEUTRAL}
	public buffType type = buffType.NEUTRAL;
	
	//whether or not the buff announces its name
	public boolean announced = false;

	//whether a buff should persist through revive effects for the hero
	public boolean revivePersists = false;
	
	protected HashSet<Class> resistances = new HashSet<>();
	
	public HashSet<Class> resistances() {
		return new HashSet<>(resistances);
	}
	
	protected HashSet<Class> immunities = new HashSet<>();
	
	public HashSet<Class> immunities() {
		return new HashSet<>(immunities);
	}

	protected HashSet<Char.Property> properties = new HashSet<>();

	public HashSet<Char.Property> properties() {
		return new HashSet<>(properties);
	}

	// used during restoring, when true, is not attached afterwards.
	public boolean attachAfterRestore = true;

	public boolean attachTo( Char target ) {

		if (target.isImmune( getClass() )) {
			return false;
		}
		
		this.target = target;

		if (target.add( this )){
			if (target.sprite != null) fx( true );
			return true;
		} else {
			this.target = null;
			return false;
		}
	}
	
	public void detach() {
		if (target.remove( this ) && target.sprite != null) fx( false );
	}
	
	@Override
	public boolean act() {
		diactivate();
		return true;
	}

	@Override // need to be public, alternatively could have prolong/affect affect all instances instead of just one.
	public void postpone(float time) { super.postpone(time); }
	@Override public void spend(float time) { super.spend(time); }

	public int icon() {
		return BuffIndicator.NONE;
	}

	//some buffs may want to tint the base texture color of their icon
	public void tintIcon( Image icon ){
		//do nothing by default
	}

	//percent (0-1) to fade out out the buff icon, usually if buff is expiring
	public float iconFadePercent(){
		return 0;
	}

	//text to display on large buff icons in the desktop UI
	public String iconTextDisplay(){
		return "";
	}

	//visual effect usually attached to the sprite of the character the buff is attacked to
	public void fx(boolean on) {
		//do nothing by default
	}

	public String heroMessage(){
		String msg = Messages.get(this, "heromsg");
		if (msg.isEmpty()) {
			return null;
		} else {
			return msg;
		}
	}

	public String name() {
		return Messages.get(this, "name");
	}

	@Override public String toString() {
		String name = name();
		//noinspection StringEquality
        if (name != Messages.NO_TEXT_FOUND) {
			if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
				name = ShatteredPixelDungeon.turnIntoRrrr(name);
			}
			return name;
		}
        return super.toString();
    }

	public String desc(){
		String desc = Messages.get(this, "desc");
		//noinspection StringEquality
		return desc != Messages.NO_TEXT_FOUND ? desc : "";
	}

	//to handle the common case of showing how many turns are remaining in a buff description.
	protected String dispTurns(float input){
		return Messages.decimalFormat("#.##", input);
	}

	//buffs act after the hero, so it is often useful to use cooldown+1 when display buff time remaining
	public float visualcooldown(){
		return cooldown()+1f;
	}

	//creates a fresh instance of the buff and attaches that, this allows duplication.
	public static<T extends Buff> T append( Char target, Class<T> buffClass ) {
		T buff = Reflection.newInstance(buffClass);
		buff.attachTo( target );
		return buff;
	}

	public static<T extends FlavourBuff> T append( Char target, Class<T> buffClass, float duration ) {
		T buff = append( target, buffClass );
		float time = duration * target.resist(buffClass);
		buff.spend(time);
		return buff;
	}

	//same as append, but prevents duplication.
	public static<T extends Buff> T affect( Char target, Class<T> buffClass ) {
		T buff = target.buff( buffClass );
		if (buff != null) {
			return buff;
		} else {
			return append( target, buffClass );
		}
	}
	
	public static<T extends FlavourBuff> T affect( Char target, Class<T> buffClass, float duration ) {
		T buff = affect( target, buffClass );
		float time = duration * target.resist(buffClass) *
				(buff.type == buffType.NEGATIVE && Dungeon.hero.pointsInTalent(Talent.LASER_PRECISION) == 3
						&& target.alignment == Char.Alignment.ENEMY ? 1.5f : 1);
		if (target instanceof Hero && Dungeon.isChallenged(Challenges.ALLERGY) && buff.type == buffType.NEGATIVE){
			time *= 2;
		}
		buff.spend(time);
		return buff;
	}

	//postpones an already active buff, or creates & attaches a new buff and delays that.
	public static<T extends FlavourBuff> T prolong( Char target, Class<T> buffClass, float duration ) {
		T buff = affect( target, buffClass );
		float time = duration * target.resist(buffClass);
		if (target instanceof Hero && Dungeon.isChallenged(Challenges.ALLERGY) && buff.type == buffType.NEGATIVE){
			time *= 2;
		}
		buff.postpone(time);
		return buff;
	}

	public static<T extends CounterBuff> T count( Char target, Class<T> buffclass, float count ) {
		T buff = affect( target, buffclass );
		buff.countUp( count );
		return buff;
	}
	
	public static void detach( Char target, Class<? extends Buff> cl ) {
		for ( Buff b : target.buffs( cl )){
			b.detach();
		}
	}
}
