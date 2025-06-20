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
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.DwarfKing;
import com.zrp200.rkpd2.items.BrokenSeal;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndCombo;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashMap;

import static com.zrp200.rkpd2.Dungeon.hero;

public class Combo extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
	}

	private int count = 0;
	private float comboTime = 0f;
	private float initialComboTime = baseComboTime();

	private static float baseComboTime() {
		return 5f+(hero != null ? hero.pointsInTalent(Talent.SKILL) : 0);
	}

	@Override
	public int icon() {
		return BuffIndicator.COMBO;
	}

	@Override
	public void tintIcon(Image icon) {
		ComboMove move = getHighestMove();
		if (move != null){
			icon.hardlight(move.tintColor);
		} else {
			icon.resetColor();
		}
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (initialComboTime - comboTime)/ initialComboTime);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)comboTime);
	}

	public void resetTime(float time, boolean force) {
		if (force || time > comboTime) {
			comboTime = initialComboTime = time;
		}
	}
	public void resetTime(boolean force) {
		resetTime(baseComboTime(), force);
	}

	public void resetTime() {
		resetTime(false);
	}

	public void hit( Char enemy ) {

		if(hero.pointsInTalent(Talent.SKILL) == 3 && Random.Int(3) == 0) count++;

		if (!enemy.isAlive() || (enemy.buff(Corruption.class) != null && enemy.HP == enemy.HT)){
			Hero hero = (Hero)target;
			resetTime(15 * hero.shiftedPoints(Talent.CLEAVE,Talent.RK_GLADIATOR), false);
		} else {
			resetTime(hero.subClass != HeroSubClass.GLADIATOR);
		}
		incCombo();
	}
	public void incCombo() {
		count++;

		if ((getHighestMove() != null)) {

			ActionIndicator.setAction( this );
			Badges.validateMasteryCombo( count );

			GLog.p( Messages.get(this, "combo", count) );

		}

		BuffIndicator.refreshHero(); //refresh the buff visually on-hit

	}

	public void miss() {
		if(((Hero)target).pointsInTalent(Talent.SKILL) >= 2 && Random.Int(3) == 0) {
			resetTime();
			incCombo();
		}
	}

	public void addTime( float time ){
		comboTime += time;
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	@Override
	public boolean act() {
		comboTime-=TICK;
		spend(TICK);
		if (comboTime <= 0) {
			if (Dungeon.hero.hasTalent(Talent.BATTLE_TENDENCY)){
				comboTime = (float) Math.pow(2, Dungeon.hero.pointsInTalent(Talent.BATTLE_TENDENCY)-2);
				count--;
				if (count <= 0)
					detach();
			}
			else detach();
		}
		return true;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc",((Hero)target).subClass.title(), count, dispTurns(comboTime));
	}

	private static final String COUNT = "count";
	private static final String TIME  = "combotime";
	private static final String INITIAL_TIME  = "initialComboTime";

	private static final String CLOBBER_USED = "clobber_used";
	private static final String PARRY_USED   = "parry_used";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(COUNT, count);
		bundle.put(TIME, comboTime);
		bundle.put(INITIAL_TIME, initialComboTime);

		bundle.put(CLOBBER_USED, clobberUsed);
		bundle.put(PARRY_USED, parryUsed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		count = bundle.getInt( COUNT );
		comboTime = bundle.getFloat( TIME );

		initialComboTime = bundle.getFloat( INITIAL_TIME );

		clobberUsed = bundle.getBoolean(CLOBBER_USED) ? 1 : bundle.getInt(CLOBBER_USED);
		parryUsed = bundle.getBoolean(PARRY_USED) ? 1 : bundle.getInt(PARRY_USED);

		if (getHighestMove() != null) ActionIndicator.setAction(this);
	}


	@Override
	public int actionIcon() {
		return HeroIcon.COMBO;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text( Integer.toString(count) );
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		ComboMove best = getHighestMove();
		if (best == null) {
			return 0xDFDFDF;
		} else {
			//take the tint color and darken slightly to match buff icon
			int r = (int) ((best.tintColor >> 16) * 0.875f);
			int g = (int) (((best.tintColor >> 8) & 0xFF) * 0.875f);
			int b = (int) ((best.tintColor & 0xFF) * 0.875f);
			return (r << 16) + (g << 8) + b;
		}
	}

	@Override
	public void doAction() {
		GameScene.show(new WndCombo(this));
	}

	@Override
	public boolean usable() {
		return getHighestMove() != null;
	}

	public enum ComboMove {
		CLOBBER(2, 0x00FF00),
		SLAM   (4, 0xCCFF00),
		PARRY  (6, 0xFFFF00),
		CRUSH  (8, 0xFFCC00),
		FURY   (10, 0xFF0000);

		public int comboReq, tintColor;

		ComboMove(int comboReq, int tintColor){
			this.comboReq = comboReq;
			this.tintColor = tintColor;
		}

		public String title(){
			return Messages.get(this, name() + ".name");
		}

		public String desc(int count){
			// ensures proper descriptions
			count = Math.max(count, comboReq);

			// note, this includes a desc fix for clobber that causes it to already proc one earlier than expected
			int enhancedCombo = hero.shiftedPoints(Talent.ENHANCED_COMBO);
			int baseEnhancedCombo = hero.pointsInTalent(Talent.RK_GLADIATOR);
			// leap distance
			int reqCount =
					enhancedCombo >= 1 ? 5 - (enhancedCombo - 1) :
					baseEnhancedCombo == 3 ? 3 :
							-1;

            Object[] args = {reqCount == 0 ? 0 : count / reqCount, null};
			switch (this){
				case CLOBBER:
					// 8 / 6 / 4 / 2
					reqCount = baseEnhancedCombo >= 1 ? 6 :
							enhancedCombo > 0 ? 8 - 2 * (enhancedCombo - 1) :
									-1;
                    break;
				case SLAM:
                    args[count >= reqCount ? 1 : 0] = count * 20;
                    break;
				case PARRY:
					reqCount = baseEnhancedCombo >= 2 ? 9 :
							// - / 12 / 9 / 6
							enhancedCombo > 1 ? 12 - 3 * (enhancedCombo - 2) :
									-1;
                    break;
				case CRUSH:
                    args[count >= reqCount ? 1 : 0] = count * 25;
                    break;
			}
			boolean empowered = reqCount > 0 && count >= reqCount;
            return Messages.get(this, name() + (empowered ? ".empower_desc" : ".desc"), args);
		}

	}

	private int clobberUsed = 0;
	private int parryUsed = 0;

	public ComboMove getHighestMove(){
		ComboMove best = null;
		for (ComboMove move : ComboMove.values()){
			if (count >= move.comboReq){
				best = move;
			}
		}
		return best;
	}

	public int getComboCount(){
		return count;
	}

	public boolean canUseMove(ComboMove move){
		int times = Math.max(1, hero.pointsInTalent(Talent.ENHANCED_COMBO));
		if (move == ComboMove.CLOBBER && clobberUsed >= times)   return false;
		if (move == ComboMove.PARRY && parryUsed >= times)       return false;
		return move.comboReq <= count;
	}

	public void useMove(ComboMove move){
		if (move == ComboMove.PARRY){
			parryUsed++;
			resetTime();
			Invisibility.dispel();
			Buff.affect(target, ParryTracker.class, Actor.TICK);
			((Hero)target).spendAndNext(Actor.TICK);
			hero.busy();
		} else {
			moveBeingUsed = move;
			GameScene.selectCell(new Selector());
		}
	}

	public static class ParryTracker extends FlavourBuff{
		{ actPriority = HERO_PRIO+1;}

		public boolean parried;

		@Override
		public void detach() {
			if (!parried && target.buff(Combo.class) != null) target.buff(Combo.class).detach();
			super.detach();
		}
	}

	public static class RiposteTracker extends Buff{
		{ actPriority = VFX_PRIO;}

		public Char enemy;

		@Override
		public boolean act() {
			if (target.buff(Combo.class) != null) {
				moveBeingUsed = ComboMove.PARRY;
				target.sprite.attack(enemy.pos, new Callback() {
					@Override
					public void call() {
						target.buff(Combo.class).doAttack(enemy);
						next();
					}
				});
				detach();
				return false;
			} else {
				detach();
				return true;
			}
		}
	}

	private static ComboMove moveBeingUsed;

	private void doAttack(final Char enemy) {

		AttackIndicator.target(enemy);
		Buff.detach(target, Preparation.class); // not the point, otherwise this would be all that's done.

		boolean wasAlly = enemy.alignment == target.alignment;
		Hero hero = (Hero) target;

		float dmgMulti = 1f;
		int dmgBonus = 0;
		//variance in damage dealt
		switch (moveBeingUsed) {
			case CLOBBER:
				dmgMulti = 0;
				break;
			case SLAM:
					// reroll armor for gladiator
					dmgBonus = target.drRoll();
					if(hero.hasTalent(Talent.SKILL)) dmgBonus = Math.max(dmgBonus, target.drRoll());
					dmgBonus = Math.round(dmgBonus * count / 5f);
				break;
			case CRUSH:
				dmgMulti = 0.25f * count;
				break;
			case FURY:
				dmgMulti = 0.6f;
				break;
		}

		int oldPos = enemy.pos;
		if (hero.attack(enemy, dmgMulti, dmgBonus, Char.INFINITE_ACCURACY, hero.hasTalent(Talent.SKILL)?2:1)){
			//special on-hit effects
			int enhancedCombo = hero.shiftedPoints(Talent.ENHANCED_COMBO);
			int baseEnhancedCombo = hero.pointsInTalent(Talent.RK_GLADIATOR);
			switch (moveBeingUsed) {
				case CLOBBER:
					//trace a ballistica to our target (which will also extend past them
					Ballistica trajectory = new Ballistica(target.pos, enemy.pos, Ballistica.STOP_TARGET);
					//trim it to just be the part that goes past them
					trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
					//knock them back along that ballistica, ensuring they don't fall into a pit
					int dist = 2;
					if (enemy.isAlive() && count >= (
							enhancedCombo > 0 ? 8 - 2 * (enhancedCombo - 1) :
									baseEnhancedCombo >= 1 ? 6 :
											count + 1
					)) {
						dist++;
						Buff.prolong(enemy, Vertigo.class, 3);
					} else if (!enemy.flying) {
						while (dist > trajectory.dist ||
								(dist > 0 && Dungeon.level.pit[trajectory.path.get(dist)])) {
							dist--;
						}
					}
					if (!wasAlly) hit(enemy); // prevent skill talent from weirdly interacting with clobber
					if (enemy.pos == oldPos) {
						WandOfBlastWave.throwChar(enemy, trajectory, dist, true, false, hero);
					}
					break;
				case PARRY:
					hit(enemy);
					break;
				case CRUSH:
					WandOfBlastWave.BlastWave.blast(enemy.pos);
					PathFinder.buildDistanceMap(target.pos, BArray.not(Dungeon.level.solid, null), 3);
					for (Char ch : Actor.chars()) {
						if (ch != enemy && ch.alignment == Char.Alignment.ENEMY
								&& PathFinder.distance[ch.pos] < Integer.MAX_VALUE) {
							int aoeHit = Math.round(target.damageRoll() * 0.25f * count);
							aoeHit /= 2;
							aoeHit -= ch.drRoll();
							if (ch.buff(Vulnerable.class) != null) aoeHit *= 1.33f;
							if (ch instanceof DwarfKing){
								//change damage type for DK so that crush AOE doesn't count for DK's challenge badge
								ch.damage(aoeHit, this);
							} else {
								ch.damage(aoeHit, target);
							}
							ch.sprite.bloodBurstA(target.sprite.center(), aoeHit);
							ch.sprite.flash();

							if (!ch.isAlive()) {
								if (hero.hasTalent(Talent.LETHAL_DEFENSE,Talent.RK_GLADIATOR) && hero.buff(BrokenSeal.WarriorShield.class) != null){
									BrokenSeal.WarriorShield shield = hero.buff(BrokenSeal.WarriorShield.class);
									int shieldAmt = Math.round(shield.maxShield() * hero.pointsInTalent(Talent.LETHAL_DEFENSE,Talent.RK_GLADIATOR)
											/(hero.hasTalent(Talent.LETHAL_DEFENSE)?2f:3f));
									shield.supercharge(shieldAmt);
								}
							}
						}
					}
					break;
				default:
					//nothing
					break;
			}
		}

		Invisibility.dispel();

		//Post-attack behaviour
		switch(moveBeingUsed){
			case CLOBBER:
				clobberUsed++;
				if (getHighestMove() == null) ActionIndicator.clearAction(Combo.this);
				hero.spendAndNext(hero.attackDelay());
				break;

			case PARRY:
				//do nothing
				break;

			case FURY:
				count--;
				//fury attacks as many times as you have combo count
				if (count > 0 && enemy.isAlive() && hero.canAttack(enemy) &&
						(wasAlly || enemy.alignment != target.alignment)){
					target.sprite.attack(enemy.pos, new Callback() {
						@Override
						public void call() {
							doAttack(enemy);
						}
					});
				} else {
					detach();
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
					ActionIndicator.clearAction(Combo.this);
					hero.spendAndNext(hero.attackDelay());
				}
				break;

			default:
				detach();
				ActionIndicator.clearAction(Combo.this);
				hero.spendAndNext(hero.attackDelay());
				break;
		}

		if (!enemy.isAlive() || (!wasAlly && enemy.alignment == target.alignment)) {
			if (hero.hasTalent(Talent.LETHAL_DEFENSE,Talent.RK_GLADIATOR) && hero.buff(BrokenSeal.WarriorShield.class) != null){
				BrokenSeal.WarriorShield shield = hero.buff(BrokenSeal.WarriorShield.class);
				int shieldAmt =Math.round(shield.maxShield() *
						(hero.pointsInTalent(Talent.LETHAL_DEFENSE) / 2f + hero.pointsInTalent(Talent.RK_GLADIATOR)/3f));
				shield.supercharge(shieldAmt);
			}
		}

	}

	// more than just a selector
	private class Selector extends CellSelector.TargetedListener {
		private int getLeapDistance() {
			int factor = hero.hasTalent(Talent.RK_GLADIATOR) ? 3 :
					hero.canHaveTalent(Talent.ENHANCED_COMBO) ? 6 - hero.shiftedPoints(Talent.ENHANCED_COMBO) :
					0;
			return factor <= 0 ? 0 : count / factor;
		}

		private HashMap<Char, Integer> targets = new HashMap<>();
		protected boolean isValidTarget(Char enemy) {
			int pos = Leap.findLeapPos(target, enemy, getLeapDistance());
			if (pos < 0) return false;
			targets.put(enemy, pos);
			return true;
		}

		@Override
		protected void onInvalid(int cell) {
			if(cell == -1) return;
			Leap.onInvalid(Messages.get(Combo.this, "bad_target"));
		}

		@Override
		protected void action(Char enemy) {
			Leap.execute((Hero) target, enemy, targets.get(enemy), () -> doAttack(enemy));
		}

		@Override
		public String prompt() {
			return Messages.get(Combo.class, "prompt");
		}
	}
	public interface Leap extends Callback {

		static int findLeapPos(Char target, int dest, int leapDistance) {
			return findLeapPos(target, new Char() {{pos = dest;}}, leapDistance, false);
		}

		static int findLeapPos(Char target, Char enemy, int leapDistance) {
			if (enemy != null
					&& enemy.alignment != Char.Alignment.ALLY
					&& enemy != target
					&& Dungeon.level.heroFOV[enemy.pos]
					&& !target.isCharmedBy(enemy)) {
				return findLeapPos(target, enemy, leapDistance, true);
			}
			return -1;
		}

		static int findLeapPos(Char target, Char enemy, int leapDistance, boolean willAttack) {
			if (willAttack ? target.canAttack(enemy) : Dungeon.level.adjacent(target.pos, enemy.pos)) {
				return target.pos;
			} else if (!target.rooted && leapDistance > 0) {
				// friendlier version of combo leap that doesn't require actually colliding with the target
				// as a note, shattered would force the final position to be adjacent to the target
				// this behavior isn't retained by this implementation

				// fixme really this should not be using auto-aim but its own custom logic,
				//  as it is attempting to auto-aim to an illegal position (the enemy pos),
				//  which means that some tiles may be passed over when the hero has increased attack range.
				//  instead, it should attempt to target the leap tile directly.

				int to = QuickSlotButton.autoAim(enemy);

                Ballistica b = new Ballistica(target.pos, to == -1 ? enemy.pos : to, Ballistica.PROJECTILE);

				// if the target was never in the field of view to begin with, treat any valid leap to a space out of view as valid
				boolean targetInFOV = willAttack;
                int leapPos;
                do {
					if (!targetInFOV) targetInFOV = Dungeon.level.heroFOV[b.path.get(b.dist)];
					if (--b.dist <= 0) return -1;
                    leapPos = b.path.get(b.dist);
                } while (
                        !Dungeon.level.passable[leapPos] ||
                                (!target.flying && Dungeon.level.avoid[leapPos]) ||
                                // ballistica's dist isn't a good measure of distance, so it needs to be checked per tile
                                Dungeon.level.distance(target.pos, leapPos) > leapDistance
                );
                if (willAttack) {
                    int initialPos = target.pos;
                    try {
                        target.pos = leapPos;
                        if (target.canAttack(enemy)) return leapPos;
                    } finally {
						target.pos = initialPos;
					}
                } else if (!targetInFOV || Dungeon.level.adjacent(leapPos, enemy.pos)) {
                    return leapPos;
                }
			}
			return -1;
		}

		static void execute(Hero target, int leapPos, Leap onLeapComplete) {
			((Hero)target).busy();
			if(leapPos != target.pos) {
				target.sprite.jump(target.pos, leapPos, () -> {
					target.move(leapPos);
					Dungeon.level.occupyCell(target);
					Dungeon.observe();
					GameScene.updateFog();
					onLeapComplete.call();
				});
			} else {
				onLeapComplete.call();
			}
		}
		static void execute(Hero target, Char enemy, int leapPos, Leap doAttack) {
			execute(target, leapPos, () -> target.sprite.attack(enemy.pos, doAttack));
		}

		static void onInvalid(String message) {
			if (hero.rooted) {
				PixelScene.shake(1, 1);
			}
			GLog.w(message);
		}
	}
}
