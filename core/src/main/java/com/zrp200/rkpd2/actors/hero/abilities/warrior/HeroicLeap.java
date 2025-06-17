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

package com.zrp200.rkpd2.actors.hero.abilities.warrior;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.TimedShrink;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.OmniAbility;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class HeroicLeap extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public float chargeUse( Hero hero ) {
		float chargeUse = super.chargeUse(hero);
		if (hero.buff(DoubleJumpTracker.class) != null){
			chargeUse -= chargeUse*0.5f*(Math.min(2, hero.pointsInTalent(Talent.DOUBLE_JUMP)));
		}
		if (hero.buff(TripleJumpTracker.class) != null){
			//reduced charge use by 24%/42%
			chargeUse *= Math.pow(0.76, hero.shiftedPoints(Talent.DOUBLE_JUMP)-3);
		}
		return chargeUse;
	}

	@Override
	public void activate( ClassArmor armor, Hero hero, Integer target ) {
		if (target != null) {

			if (hero.rooted){
				PixelScene.shake( 1, 1f );
				return;
			}

			Ballistica route = new Ballistica(hero.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
			int cell = route.collisionPos;

			//can't occupy the same cell as another char, so move back one.
			int backTrace = route.dist-1;
			while (Actor.findChar( cell ) != null && cell != hero.pos) {
				cell = route.path.get(backTrace);
				backTrace--;
			}

			armor.useCharge( hero, this, false );

			final int dest = cell;
			hero.busy();
			hero.sprite.jump(hero.pos, cell, () -> {
				hero.move(dest);
				Dungeon.level.occupyCell(hero);
				Dungeon.observe();
				GameScene.updateFog();

					for (int i : PathFinder.NEIGHBOURS8) {
						Char mob = Actor.findChar(hero.pos + i);
						if (mob != null && mob != hero && mob.alignment != Char.Alignment.ALLY) {
							if (hero.hasTalent(Talent.ALICE_GAMBIT)){
								Buff.affect(mob, TimedShrink.class, hero.pointsInTalent(Talent.ALICE_GAMBIT)*2.5f);
							}
							if (hero.canHaveTalent(Talent.BODY_SLAM)){
								int points = hero.shiftedPoints(Talent.BODY_SLAM);
							int damage = Random.NormalIntRange(points, 4*points);
								damage += Math.round(hero.drRoll()*0.25f*points);
								damage = Talent.heroicEnchantingProc(hero, mob, damage);
								damage -= mob.drRoll();
								mob.damage(damage, hero);
							}
							if (mob.pos == hero.pos + i && hero.hasTalent(Talent.IMPACT_WAVE)){
								Ballistica trajectory = new Ballistica(mob.pos, mob.pos + i, Ballistica.MAGIC_BOLT);
								int strength = 1+hero.pointsInTalent(Talent.IMPACT_WAVE);
								strength *= 1.5; // 3/4/6 instead of 2/3/4
								WandOfBlastWave.throwChar(mob, trajectory, strength, true, true, HeroicLeap.this);
								// 40/60/80/100
								if (Random.Int(5) < 1+hero.pointsInTalent(Talent.IMPACT_WAVE)){
									Buff.prolong(mob, Vulnerable.class, 5f); // 3 -> 5
								}
							}
						}
					}

				WandOfBlastWave.BlastWave.blast(dest);
				PixelScene.shake(2, 0.5f);

				Invisibility.dispel();
				hero.spendAndNext(Actor.TICK);


					if (hero.buff(DoubleJumpTracker.class) != null){
						hero.buff(DoubleJumpTracker.class).detach();
						if (hero.pointsInTalent(Talent.DOUBLE_JUMP) > 2){
							Buff.affect(hero, TripleJumpTracker.class, 5);
						}
					} else
						if (hero.canHaveTalent(Talent.DOUBLE_JUMP) && hero.buff(TripleJumpTracker.class) == null) {
							Buff.affect(hero, DoubleJumpTracker.class, 5);
						} else if (hero.buff(TripleJumpTracker.class) != null){
						hero.buff(TripleJumpTracker.class).detach();
					}

				OmniAbility.markAbilityUsed(HeroicLeap.this);
			});
		}
	}

	public static class DoubleJumpTracker extends FlavourBuff{};
	public static class TripleJumpTracker extends FlavourBuff{};

	@Override
	public int icon() {
		return HeroIcon.HEROIC_LEAP;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BODY_SLAM, Talent.IMPACT_WAVE, Talent.DOUBLE_JUMP, Talent.ALICE_GAMBIT, Talent.HEROIC_ENERGY};
	}
}
