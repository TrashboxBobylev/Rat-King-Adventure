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

import com.badlogic.gdx.utils.IntIntMap;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroAction;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.DeathMark;
import com.zrp200.rkpd2.actors.mobs.npcs.NPC;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.particles.BloodParticle;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Preparation extends Buff implements ActionIndicator.Action {
	
	{
		//always acts after other buffs, so invisibility effects can process first
		actPriority = BUFF_PRIO - 1;
		type = buffType.POSITIVE;
	}
	
	public enum AttackLevel{
		LVL_1( 1, 0.10f, 1),
		LVL_2( 3, 0.20f, 1),
		LVL_3( 5, 0.35f, 2),
		LVL_4( 9, 0.50f, 3);

		final int turnsReq;
		final float baseDmgBonus;
		final int damageRolls;
		
		AttackLevel( int turns, float base, int rolls){
			turnsReq = turns;
			baseDmgBonus = base;
			damageRolls = rolls;
		}

		//1st index is prep level, 2nd is talent level
		private static final float[][] KOThresholds = new float[][]{
				{.03f, .04f, .05f, .06f},
				{.10f, .13f, .17f, .20f},
				{.20f, .27f, .33f, .40f},
				{.50f, .67f, .83f, 1.0f}
		};

		public float KOThreshold(){
			if (Dungeon.hero.heroClass.is(HeroClass.ROGUE)){
				return KOThresholds[ordinal()][3];
			}
			return KOThresholds[ordinal()][Dungeon.hero.pointsInTalent(Talent.ENHANCED_LETHALITY, Talent.RK_ASSASSIN)];
		}

		//1st index is prep level, 2nd is talent level, third is type.
		private static final int[][][] blinkRanges = new int[][][]{
				{{1, 2, 3, 4}, {1, 2, 3, 4}},
				{{1, 3, 4, 6}, {2, 3, 5, 6}},
				{{2, 4, 6, 8}, {3, 5, 7,10}},
				{{2, 5, 7,10}, {4, 7,10,14}}
		};

		public int blinkDistance(){
			if (Dungeon.hero.heroClass.is(HeroClass.ROGUE)){
				return blinkRanges[1][1][ordinal()];
			}
			return blinkRanges[Dungeon.hero.pointsInTalent(Talent.ASSASSINS_REACH,Talent.RK_ASSASSIN)][Dungeon.hero.hasTalent(Talent.ASSASSINS_REACH)?1:0][ordinal()];
		}
		
		public boolean canKO(Char defender){
			if (defender.properties().contains(Char.Property.MINIBOSS)
					|| defender.properties().contains(Char.Property.BOSS)){
				return (defender.HP/(float)defender.HT) < (KOThreshold()/5f);
			} else {
				return (defender.HP/(float)defender.HT) < KOThreshold();
			}
		}
		
		public int damageRoll( Char attacker ){
			int dmg = attacker.damageRoll();
			for(int i = 1; i < getDamageRolls(); i++){
				int newDmg = attacker.damageRoll();
				if (newDmg > dmg) dmg = newDmg;
			}
			return Math.round(dmg * (1f + baseDmgBonus));
		}
		
		public static AttackLevel getLvl(int turnsInvis){
			List<AttackLevel> values = Arrays.asList(values());
			Collections.reverse(values);
			for ( AttackLevel lvl : values ){
				if (turnsInvis >= lvl.turnsReq){
					return lvl;
				}
			}
			return LVL_1;
		}

		public int getDamageRolls() {
			return damageRolls + (Dungeon.hero.hasTalent(Talent.ADAPT_AND_OVERCOME) ? 2 : 0);
		}
	}
	
	public int turnsInvis = 0;

	@Override
	public boolean usable() {
		return bundleRestoring ||
				AttackLevel.getLvl(turnsInvis).blinkDistance() > 0 && target == Dungeon.hero;
	}
	@Override
	public boolean act() {
		if (target.invisible > 0){
			turnsInvis++;
			ActionIndicator.setAction(this);
			spend(TICK);
		} else {
			detach();
		}
		return true;
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	/** if the current state of preparation is not tied to invisibility **/
	private boolean manuallySetLevel() {
		return target.invisible == 0;
	}

	/** sets the preparation level to the specified level **/
	public void setAttackLevel(int level) {
		turnsInvis = AttackLevel.values()[level - 1].turnsReq;
	}

	public int attackLevel(){
		return AttackLevel.getLvl(turnsInvis).ordinal()+1;
	}

	public int damageRoll( Char attacker ){
		return AttackLevel.getLvl(turnsInvis).damageRoll(attacker);
	}

	public boolean canKO( Char defender ){
		return !defender.isInvulnerable(target.getClass()) && AttackLevel.getLvl(turnsInvis).canKO(defender);
	}

	public boolean procKO(Char attacker, Char enemy){
		boolean assassinated = false;

		if (enemy.isAlive() && canKO(enemy)){
			enemy.HP = 0;
			if (!enemy.isAlive()) {
				enemy.die(this);
			} else {
				//helps with triggering any on-damage effects that need to activate
				enemy.damage(-1, this);
				DeathMark.processFearTheReaper(enemy, true);
			}
			assassinated = true;
			enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
		}
		if (attacker instanceof Hero && assassinated) {
			if (((Hero) attacker).hasTalent(Talent.BLOODBATH)) {
				Preparation.bloodbathProc((Hero) attacker, enemy);
			}
			if (((Hero) attacker).hasTalent(Talent.DARKENING_STEPS)){
				Buff.affect(attacker, ArtifactRecharge.class).prolong(Dungeon.hero.pointsInTalent(Talent.DARKENING_STEPS)*2);
			}
		}
		return assassinated;
	}

	@Override
	public int icon() {
		return BuffIndicator.PREPARATION;
	}
	
	@Override
	public void tintIcon(Image icon) {
		switch (AttackLevel.getLvl(turnsInvis)){
			case LVL_1:
				icon.hardlight(0f, 1f, 0f);
				break;
			case LVL_2:
				icon.hardlight(1f, 1f, 0f);
				break;
			case LVL_3:
				icon.hardlight(1f, 0.6f, 0f);
				break;
			case LVL_4:
				icon.hardlight(1f, 0f, 0f);
				break;
		}
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(manuallySetLevel() ? attackLevel() : turnsInvis);
	}

	@Override
	public String desc() {
		String cls = ((Hero)target).subClass.title();
		String desc = Messages.get(this, "desc",cls);
		
		AttackLevel lvl = AttackLevel.getLvl(turnsInvis);

		desc += "\n\n" + Messages.get(this, "desc_dmg",
				(int)(lvl.baseDmgBonus*100),
				(int)(lvl.KOThreshold()*100),
				(int)(lvl.KOThreshold()*20));
		
		if (lvl.getDamageRolls() > 1){
			desc += " " + Messages.get(this, "desc_dmg_likely");
		}
		
		if (lvl.blinkDistance() > 0){
			desc += "\n\n" + Messages.get(this, "desc_blink", lvl.blinkDistance());
		}

		if(!manuallySetLevel()) {
			desc += "\n\n" + Messages.get(this, "desc_invis_time", turnsInvis);

			if (lvl.ordinal() != AttackLevel.values().length-1){
				AttackLevel next = AttackLevel.values()[lvl.ordinal()+1];
				desc += "\n" + Messages.get(this, "desc_invis_next", next.turnsReq);
			}
		}
		
		return desc;
	}

	public static void bloodbathProc(Hero attacker, Char enemy){
		WandOfBlastWave.BlastWave.blast(enemy.pos);
		PathFinder.buildDistanceMap(enemy.pos, BArray.not(Dungeon.level.solid, null),
				1 + attacker.pointsInTalent(Talent.BLOODBATH));
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				CellEmitter.bottom(i).burst(BloodParticle.BURST, 12);
			}
		}
		for (Char ch : Actor.chars()) {
			if (ch != enemy && ch.alignment == Char.Alignment.ENEMY
					&& PathFinder.distance[ch.pos] < Integer.MAX_VALUE) {
				int aoeHit = Math.round(attacker.damageRoll());
				aoeHit *= 0.6f;
				aoeHit -= ch.drRoll();
				if (ch.buff(Vulnerable.class) != null) aoeHit *= 1.33f;
				ch.damage(aoeHit, attacker);
				ch.sprite.bloodBurstA(attacker.sprite.center(), aoeHit);
				ch.sprite.flash();

				if (!ch.isAlive()) {

				}
			}
		}
	}

	private static final String TURNS = "turnsInvis";

	private boolean bundleRestoring = false; // allows skipping of certain checks that require the game to be fully instantiated.
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsInvis = bundle.getInt(TURNS);
		// this lets us ignore checks that are impossible to make.
		bundleRestoring = true;
		ActionIndicator.setAction(this);
		bundleRestoring = false;
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS, turnsInvis);
	}

	@Override
	public Visual primaryVisual() {
		Image actionIco = new HeroIcon(this);
		tintIcon(actionIco);
		return actionIco;
	}

	@Override
	public Visual secondaryVisual() {
		if (manuallySetLevel()) return null;
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text(Integer.toString(Math.min(9, turnsInvis)));
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x444444;
	}

	@Override
	public void doAction() {
		GameScene.selectCell(new Attack());
	}

	private class Attack extends CellSelector.TargetedListener {

		IntIntMap blinkPos = new IntIntMap(); // enemy pos to blink pos

		private boolean canAttack(Char enemy) {
			return !(enemy == null || Dungeon.hero.isCharmedBy(enemy) || enemy instanceof NPC || !Dungeon.level.heroFOV[enemy.pos] || enemy == Dungeon.hero);
		}

		@Override protected void findTargets() {
			AttackLevel lvl = AttackLevel.getLvl(turnsInvis);
			PathFinder.buildDistanceMap(Dungeon.hero.pos,BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null), lvl.blinkDistance());
			super.findTargets();
		} @Override protected boolean isValidTarget(Char enemy) {
			if ( !canAttack(enemy) ){
				//GLog.w(Messages.get(Preparation.class, "no_target"));
				return false;
			}
			// check if the hero can reach them outright.
			if( Dungeon.hero.canAttack(enemy) ) {
				return true;
			}
			int dest = -1;
			for (int i : PathFinder.NEIGHBOURS8){
				int cell = enemy.pos+i;
				//cannot blink into a cell that's occupied or impassable, only over them
				if (Actor.findChar(cell) != null)     continue;
				if (!Dungeon.level.passable[cell] && !(target.flying && Dungeon.level.avoid[cell+i])) {
						continue;
					}

				if (dest == -1 || PathFinder.distance[dest] > PathFinder.distance[cell]){
					dest = cell;
					//if two cells have the same pathfinder distance, prioritize the one with the closest true distance to the hero
				} else if (PathFinder.distance[dest] == PathFinder.distance[cell]){
					if (Dungeon.level.trueDistance(Dungeon.hero.pos, dest) > Dungeon.level.trueDistance(Dungeon.hero.pos, cell)){
						dest = cell;
					}
				}
			}
			if (dest == -1 || PathFinder.distance[dest] == Integer.MAX_VALUE || Dungeon.hero.rooted){
				//GLog.w(Messages.get(Preparation.class, "out_of_reach"));
				if (Dungeon.hero.rooted) PixelScene.shake( 1, 1f );return false;
			}
			blinkPos.put(enemy.pos, dest);
			return true;
		}

		@Override
		protected void onInvalid(int cell) {
			// this just..guesses. it just checks the conditions until it gets a reasonable result.
			if(cell == -1) return;
			GLog.w(Messages.get(Preparation.class,
					canAttack(findChar(cell)) ? "out_of_reach" : "no_target"));
		}

		@Override
		protected void action(Char enemy) {
			int dest = blinkPos.get(enemy.pos,-1);
			if(dest != -1) {
				Dungeon.hero.pos = dest;
				Dungeon.level.occupyCell(Dungeon.hero);
				//prevents the hero from being interrupted by seeing new enemies
				Dungeon.observe();
				GameScene.updateFog();
				Dungeon.hero.checkVisibleMobs();
				
				Dungeon.hero.sprite.place( Dungeon.hero.pos );
				Dungeon.hero.sprite.turnTo( Dungeon.hero.pos, enemy.pos);
				CellEmitter.get( Dungeon.hero.pos ).burst( Speck.factory( Speck.WOOL ), 6 );
				Sample.INSTANCE.play( Assets.Sounds.PUFF );
			}
			Dungeon.hero.curAction = new HeroAction.Attack( enemy );
			Dungeon.hero.next();
		}

		@Override
		public String prompt() {
			return Messages.get(Preparation.class, "prompt", AttackLevel.getLvl(turnsInvis).blinkDistance());
		}
	}
}
