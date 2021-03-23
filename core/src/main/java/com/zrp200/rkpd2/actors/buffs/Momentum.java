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

package com.zrp200.rkpd2.actors.buffs;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.QuickSlot;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Momentum extends Buff implements ActionIndicator.Action {
	
	{
		type = buffType.POSITIVE;

		//acts before the hero
		actPriority = HERO_PRIO+1;
	}
	
	private int momentumStacks = 0;
	private int freerunTurns = 0;
	private int freerunCooldown = 0;

	private boolean movedLastTurn = true;

	@Override
	public boolean act() {
		if (freerunCooldown > 0){
			freerunCooldown--;
		}
		if(freerunCooldown > 0 && freerunTurns == 0 && target.invisible > 0 && Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH) >= 2) freerunCooldown--; // reduce an extra time.

		if (freerunCooldown == 0 && target.invisible > 0 && Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH,Talent.RK_FREERUNNER) >= 1){
			momentumStacks = Math.min(momentumStacks + (Dungeon.hero.hasTalent(Talent.SPEEDY_STEALTH)?3:2), 10);
			movedLastTurn = true;
		}

		if (freerunTurns > 0){
			if (target.invisible == 0 || Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH,Talent.RK_FREERUNNER) < 2) {
				freerunTurns--;
				if(freerunTurns == 0) Item.updateQuickslot();
			}
		} else if (!movedLastTurn){
			momentumStacks = (int)GameMath.gate(0, momentumStacks-1, Math.round(momentumStacks * 0.667f));
			if (momentumStacks <= 0) {
				ActionIndicator.clearAction(this);
				if (freerunCooldown <= 0) detach();
			}
		}
		movedLastTurn = false;

		spend(TICK);
		return true;
	}
	
	public void gainStack(){
		movedLastTurn = true;
		if (freerunCooldown <= 0){
			postpone(target.cooldown()+(1/target.speed()));
			momentumStacks = Math.min(momentumStacks + 1, 10);
			ActionIndicator.setAction(this);
		}
	}

	public boolean freerunning(){
		return freerunTurns > 0 || Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH) == 3 && target.invisible > 0;
	}

	public float speedMultiplier(){
		if (freerunning()){
			return 2;
		} else if (target.invisible > 0 && Dungeon.hero.pointsInTalent(Talent.SPEEDY_STEALTH,Talent.RK_FREERUNNER) == 3){
			return 2;
		} else {
			return 1;
		}
	}
	
	public int evasionBonus( int heroLvl, int excessArmorStr ){
		if (freerunning()) {
			return heroLvl/2 + excessArmorStr*(
					(Random.round((4+heroLvl)*Dungeon.hero.pointsInTalent(Talent.EVASIVE_ARMOR)/15f)) // this effectively allows fractional evasion.
							+ Dungeon.hero.pointsInTalent(Talent.RK_FREERUNNER));
		} else {
			return 0;
		}
	}
	
	@Override
	public int icon() {
		return BuffIndicator.MOMENTUM;
	}
	
	@Override
	public void tintIcon(Image icon) {
		if (freerunTurns > 0){
			icon.hardlight(1,1,0);
		} else if (freerunCooldown > 0){
			icon.hardlight(0.5f,0.5f,1);
		} else {
			icon.hardlight(1f - (momentumStacks /10f),1,1f - (momentumStacks /10f));
		}
	}

	@Override
	public float iconFadePercent() {
		if (freerunTurns > 0){
			int duration = (int)Math.ceil(20*(1+Dungeon.hero.pointsInTalent(Talent.FAST_RECOVERY)/3f));
			return (duration - freerunTurns) / (float)duration;
		} else if (freerunCooldown > 0){
			return (freerunCooldown) / 30f;
		} else {
			return (10 - momentumStacks) / 10f;
		}
	}

	@Override
	public String toString() {
		if (freerunTurns > 0){
			return Messages.get(this, "running");
		} else if (freerunCooldown > 0){
			return Messages.get(this, "resting");
		} else {
			return Messages.get(this, "momentum");
		}
	}
	
	@Override
	public String desc() {
		String cls = Dungeon.hero.heroClass.title();
		if (freerunTurns > 0){
			return Messages.get(this, "running_desc", cls, freerunTurns);
		} else if (freerunCooldown > 0){
			return Messages.get(this, "resting_desc", cls, freerunCooldown);
		} else {
			return Messages.get(this, "momentum_desc", cls, momentumStacks);
		}
	}
	
	private static final String STACKS =        "stacks";
	private static final String FREERUN_TURNS = "freerun_turns";
	private static final String FREERUN_CD =    "freerun_CD";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STACKS, momentumStacks);
		bundle.put(FREERUN_TURNS, freerunTurns);
		bundle.put(FREERUN_CD, freerunCooldown);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		momentumStacks = bundle.getInt(STACKS);
		freerunTurns = bundle.getInt(FREERUN_TURNS);
		freerunCooldown = bundle.getInt(FREERUN_CD);
		if ( usable() ){
			ActionIndicator.setAction(this);
		}
		movedLastTurn = false;
	}

	@Override
	public Image getIcon() {
		Image im = new Image(Assets.Interfaces.BUFFS_LARGE, 144, 32, 16, 16);
		im.hardlight(0x99992E);
		return im;
	}

	@Override
	public void doAction() {
		freerunTurns = (int)Math.ceil(2*momentumStacks*(1+Dungeon.hero.pointsInTalent(Talent.FAST_RECOVERY)/3f));
		//cooldown is functionally 10+2*stacks when active effect ends
		freerunCooldown = 10 + 2*momentumStacks + freerunTurns;
		Sample.INSTANCE.play(Assets.Sounds.MISS, 1f, 0.8f);
		target.sprite.emitter().burst(Speck.factory(Speck.JET), 5+ momentumStacks);
		momentumStacks = 0;
		Item.updateQuickslot();
		BuffIndicator.refreshHero();
		ActionIndicator.clearAction(this);
	}
	public boolean usable() {
		return momentumStacks > 0 && freerunTurns <= 0;
	}

}
