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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.npcs.PrismaticImage;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class PrismaticGuard extends Buff {
	
	{
		type = buffType.POSITIVE;
	}
	
	private float HP;

	private float powerOfManyTurns = 0;

	@Override
	public boolean act() {

		if (target.buff(Talent.HelperToHeroReviveCooldown.class) != null){
			spend(TICK);
			return true;
		}

		Hero hero = (Hero)target;
		
		Mob closest = null;
		int v = hero.visibleEnemies();
		for (int i=0; i < v; i++) {
			Mob mob = hero.visibleEnemy( i );
			if ( mob.isAlive() && !mob.isInvulnerable(PrismaticImage.class)
					&& mob.state != mob.PASSIVE && mob.state != mob.WANDERING && mob.state != mob.SLEEPING && !hero.mindVisionEnemies.contains(mob)
					&& (closest == null || Dungeon.level.distance(hero.pos, mob.pos) < Dungeon.level.distance(hero.pos, closest.pos))) {
				closest = mob;
			}
		}
		
		if (closest != null && Dungeon.level.distance(hero.pos, closest.pos) < 5){
			//spawn guardian
			int bestPos = -1;
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				int p = hero.pos + PathFinder.NEIGHBOURS8[i];
				if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
					if (bestPos == -1 || Dungeon.level.trueDistance(p, closest.pos) < Dungeon.level.trueDistance(bestPos, closest.pos)){
						bestPos = p;
					}
				}
			}
			if (bestPos != -1) {
				PrismaticImage pris = new PrismaticImage();
				pris.duplicate(hero, (int)Math.floor(HP) );
				if (powerOfManyTurns > 0){
					Buff.affect(pris, PowerOfMany.PowerBuff.class, powerOfManyTurns);
				}
				pris.state = pris.HUNTING;
				GameScene.add(pris, 1);
				ScrollOfTeleportation.appear(pris, bestPos);
				
				detach();
			} else {
				spend( TICK );
			}
			
			
		} else {
			spend(TICK);
		}

		if (HP < maxHP() && Regeneration.regenOn()){
			float regenAmount = 1f/10f;
			if (target instanceof Hero && ((Hero) target).pointsInTalent(Talent.HELPER_TO_HERO) > 1){
				regenAmount = 1f/Regeneration.getRegenDelay(target);
				if (((Hero) target).pointsInTalent(Talent.HELPER_TO_HERO) > 2){
					regenAmount *= 2f;
				}
			}
			HP += regenAmount;
		}
		if (powerOfManyTurns > 0){
			powerOfManyTurns--;
			if (powerOfManyTurns <= 0){
				powerOfManyTurns = 0;
				BuffIndicator.refreshHero();
			}
		}

		return true;
	}
	
	public void set( int HP ){
		this.HP = HP;
		powerOfManyTurns = 0;
	}

	public void set( PrismaticImage img){
		this.HP = img.HP;
		if (img.buff(PowerOfMany.PowerBuff.class) != null){
			powerOfManyTurns = img.buff(PowerOfMany.PowerBuff.class).cooldown()+1;
		} else {
			powerOfManyTurns = 0;
		}
	}
	
	public int maxHP(){
		return maxHP((Hero)target);
	}
	
	public static int maxHP( Hero hero ){
		return 10 + (int)Math.floor(hero.lvl * 2.5f); //half of hero's HP
	}

	public boolean isEmpowered(){
		return powerOfManyTurns > 0;
	}

	@Override
	public int icon() {
		return BuffIndicator.ARMOR;
	}
	
	@Override
	public void tintIcon(Image icon) {
		if (isEmpowered()){
			icon.hardlight(3f, 3f, 2f);
		} else {
			icon.hardlight(1f, 1f, 2f);
            if (target.buff(Talent.HelperToHeroReviveCooldown.class) != null)
                icon.tint(0x000000, 0.5f);
		}
	}

	@Override
	public float iconFadePercent() {
		return 1f - HP/(float)maxHP();
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)HP);
	}
	
	@Override
	public String desc() {
        if (target.buff(Talent.HelperToHeroReviveCooldown.class) != null)
            return Messages.get(this, "desc_recover", (int)HP, maxHP());
		String desc = Messages.get(this, "desc", (int)HP, maxHP());
		if (isEmpowered()){
			desc += "\n\n" + Messages.get(this, "desc_many", (int)powerOfManyTurns);
		}
		return desc;
	}
	
	private static final String HEALTH = "hp";
	private static final String POWER_TURNS = "power_turns";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HEALTH, HP);
		bundle.put(POWER_TURNS, powerOfManyTurns);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		HP = bundle.getFloat(HEALTH);
		powerOfManyTurns = bundle.getFloat(POWER_TURNS);
	}
}
