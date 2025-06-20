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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Petrified;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.OmniAbility;
import com.zrp200.rkpd2.actors.mobs.Mimic;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import static com.zrp200.rkpd2.actors.hero.Talent.*;

public class Shockwave extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	public int targetedPos(Char user, int dst) {
		return new Ballistica( user.pos, dst, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	public static void activate(Hero hero, int target, int degrees, int maxDist, Callback next) {
		boolean endTurn = next == null;

		hero.busy();

		Ballistica aim = new Ballistica(hero.pos, target, Ballistica.WONT_STOP);

		int dist = Math.min(aim.dist, maxDist);

		ConeAOE cone = new ConeAOE(aim, dist, degrees,
				Ballistica.STOP_SOLID | Ballistica.STOP_TARGET);

		//cast to cells at the tip, rather than all cells, better performance.
		for (Ballistica ray : cone.outerRays){
			hero.sprite.parent.recycle( MagicMissile.class ).reset(
					MagicMissile.FORCE_CONE,
					hero.sprite,
					ray.path.get(ray.dist),
					null
			);
		}

		hero.sprite.zap(target);
		// TODO fix so that sounds can just play as soon as possible.
		Sample.INSTANCE.playDelayed(Assets.Sounds.BLAST, next == null ? 0f : 0.125f, next == null ? 1f : 3f, 0.5f);
		PixelScene.shake(2, 0.5f);
		//final zap at 2/3 distance, for timing of the actual effect
		MagicMissile.boltFromChar(hero.sprite.parent,
				MagicMissile.FORCE_CONE,
				hero.sprite,
				cone.coreRay.path.get(dist * 2 / 3),
				() -> {
					for (int cell : cone.cells){

						Char ch = Actor.findChar(cell);
						if (ch != null && ch.alignment != hero.alignment){
							int scalingStr = hero.STR()-10;
							int damage = Hero.heroDamageIntRange(5 + scalingStr, 10 + 2*scalingStr);
							float modifier = (1f + hero.byTalent(
									SHOCK_FORCE, .25f,
									AFTERSHOCK, .15f));
							damage = Math.round(damage * modifier);
							damage -= ch.drRoll();

							// note I'm not giving this to aftershock.
							if (hero.pointsInTalent(Talent.STRIKING_WAVE) == 4){
								Buff.affect(hero, Talent.StrikingWaveTracker.class, 0f);
							}

							if (Random.Int(10) < hero.byTalent(
									STRIKING_WAVE, 3,
									AFTERSHOCK, 2)){
								boolean wasEnemy = ch.alignment == Char.Alignment.ENEMY
										|| (ch instanceof Mimic && ch.alignment == Char.Alignment.NEUTRAL);
								damage = hero.attackProc(ch, damage);
								ch.damage(damage, hero);
								if(wasEnemy) switch (hero.subClass) {
									case KING: case GLADIATOR:
										Buff.affect( hero, Combo.class ).hit( ch );
								}
							} else {
								ch.damage(damage, hero);
							}
							if (ch.isAlive()){
								// fixme for wrath this should probably be delayed, though
								if (Random.Int(hero.hasTalent(SHOCK_FORCE) ? 4 : 5) < hero.pointsInTalent(SHOCK_FORCE,AFTERSHOCK)){
										Buff.affect(ch, Paralysis.class, 5f);
									Buff.affect(ch, ShockForceStunHold.class, 0f);} else {
										Buff.affect(ch, Cripple.class, 5f);
									}
									if (hero.hasTalent(Talent.COCKATRIOCIOUS)){
										Buff.affect(ch, Petrified.class, 3 + (hero.pointsInTalent(Talent.COCKATRIOCIOUS)-1)*2f);
									}
								}

						}
					}

					if(endTurn) {
						Invisibility.dispel();
						hero.spendAndNext(Actor.TICK);
						OmniAbility.markAbilityUsed(new Shockwave());
					} else next.call();

				});
	}

	// this prevents stun from being broken during wrath.
	public static class ShockForceStunHold extends FlavourBuff {{ actPriority = VFX_PRIO; }}

	public void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}
		if (target == hero.pos){
			GLog.w(Messages.get(Shockwave.class, "self_target"));
			return;
		}
		activate(hero, target,
				60 + 15*hero.shiftedPoints(EXPANDING_WAVE),
				5 + hero.shiftedPoints(EXPANDING_WAVE),
				null);
		armor.useCharge(hero, this, false);
	}

	@Override
	public int icon() {
		return HeroIcon.SHOCKWAVE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{EXPANDING_WAVE, STRIKING_WAVE, SHOCK_FORCE, Talent.COCKATRIOCIOUS, Talent.HEROIC_ENERGY};
	}
}
