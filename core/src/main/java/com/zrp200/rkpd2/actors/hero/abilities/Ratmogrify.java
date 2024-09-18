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

package com.zrp200.rkpd2.actors.hero.abilities;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.AscensionChallenge;
import com.zrp200.rkpd2.actors.buffs.Berserk;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.AbyssalMob;
import com.zrp200.rkpd2.actors.mobs.Albino;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Rat;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.RatSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.TargetHealthIndicator;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Ratmogrify extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	//this is sort of hacky, but we need it to know when to use alternate name/icon for heroic energy
	public static boolean useRatroicEnergy = false;

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos(Char user, int dst) {
		return dst;
	}

	@Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {

		if (target == null){
			return;
		}

		Char ch = Actor.findChar(target);

		if (ch == null || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(this, "no_target"));
			return;
		} else if (ch == hero){
			if (!(hero.hasTalent(Talent.RATFORCEMENTS) || hero.heroClass.is(HeroClass.RAT_KING))){
				GLog.w(Messages.get(this, "self_target"));
				return;
			} else {
				ArrayList<Integer> spawnPoints = new ArrayList<>();

				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int p = hero.pos + PathFinder.NEIGHBOURS8[i];
					if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
						spawnPoints.add( p );
					}
				}

				int ratsToSpawn = hero.pointsInTalentWithInnate(HeroClass.RAT_KING,Talent.RATFORCEMENTS);

				while (ratsToSpawn > 0 && spawnPoints.size() > 0) {
					int index = Random.index( spawnPoints );

					Rat rat = Random.Int(25) == 0 ? new SummonedAlbino() : new SummonedRat();
					rat.alignment = Char.Alignment.ALLY;
					rat.state = rat.HUNTING;
					Buff.affect(rat, AscensionChallenge.AscensionBuffBlocker.class);
					GameScene.add( rat );
					ScrollOfTeleportation.appear( rat, spawnPoints.get( index ) );

					spawnPoints.remove( index );
					ratsToSpawn--;
				}

			}
		} else if (ch.alignment != Char.Alignment.ENEMY || !(ch instanceof Mob) || ch instanceof Rat){
			GLog.w(Messages.get(this, "cant_transform"));
			return;
		} else if (ch instanceof TransmogRat){
			if (((TransmogRat) ch).allied || !(hero.hasTalent(Talent.RATLOMACY) || hero.heroClass.is(HeroClass.RAT_KING))){
				GLog.w(Messages.get(this, "cant_transform"));
				return;
			} else {
				((TransmogRat) ch).makeAlly();
				ch.sprite.emitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				if (hero.pointsInTalentWithInnate(HeroClass.RAT_KING,Talent.RATLOMACY) > 1){
					Buff.affect(ch, Adrenaline.class, /*2*/4*(hero.pointsInTalentWithInnate(HeroClass.RAT_KING,Talent.RATLOMACY)-1));
				}
			}
		} else if (Char.hasProp(ch, Char.Property.MINIBOSS) || Char.hasProp(ch, Char.Property.BOSS)){
			GLog.w(Messages.get(this, "too_strong"));
			return;
		} else {
			TransmogRat rat = new TransmogRat();
			rat.setup((Mob)ch);
			rat.pos = ch.pos;

			//preserve champion enemy buffs
			HashSet<ChampionEnemy> champBuffs = ch.buffs(ChampionEnemy.class);
			for (ChampionEnemy champ : champBuffs){
				if (ch.remove(champ)) {
					ch.sprite.clearAura();
				}
			}

			Actor.remove( ch );
			ch.sprite.killAndErase();
			Dungeon.level.mobs.remove(ch);

			for (ChampionEnemy champ : champBuffs){
				ch.add(champ);
			}

			GameScene.add(rat);

			TargetHealthIndicator.instance.target(null);
			CellEmitter.get(rat.pos).burst(Speck.factory(Speck.WOOL), 4);
			Sample.INSTANCE.play(Assets.Sounds.PUFF);

			Dungeon.level.occupyCell(rat);

			//for rare cases where a buff was keeping a mob alive (e.g. gnoll brutes)
			if (!rat.isAlive()){
				rat.die(this);
			}
		}

		armor.useCharge(hero,this);
		Invisibility.dispel();
		hero.spendAndNext(Actor.TICK);

	}

	public static boolean drratedonActive(Char rat){
		return (Dungeon.hero.pointsInTalent(Talent.DRRATEDON) > 0 || Dungeon.hero.heroClass.is(HeroClass.RAT_KING))
				&& rat.alignment == Char.Alignment.ALLY
				&& (rat instanceof Ratmogrify.TransmogRat || rat instanceof Rat);
	}

	public static int drratedonEffect(Char rat){
		if (drratedonActive(rat))
			return Dungeon.hero.pointsInTalentWithInnate(HeroClass.RAT_KING, Talent.DRRATEDON);
		return 0;
	}

	@Override
	public int icon() {
		return HeroIcon.RATMOGRIFY;
	}

	@Override
	public Talent[] talents() {
		ArrayList<Talent> talents = new ArrayList<>(Arrays.asList(Talent.RATSISTANCE, Talent.RATLOMACY, Talent.RATFORCEMENTS, Talent.DRRATEDON, Talent.HEROIC_ENERGY));
		return talents.toArray(new Talent[0]);
	}

	@Override
	public boolean isTracked() {
		// yes I know this is incredibly general, but I know this will come up at some point.
		return Actor.containsClass(Rat.class) || Actor.containsClass(TransmogRat.class);
	}

	public static class TransmogRat extends Mob {

		{
			spriteClass = RatSprite.class;

			//always false, as we derive stats from what we are transmogging from (which was already added)
			firstAdded = false;
		}

		private Mob original;
		private boolean allied;

		public void setup(Mob original) {
			this.original = original;

			HP = original.HP;
			HT = original.HT;

			defenseSkill = original.defenseSkill;

			EXP = original.EXP;
			maxLvl = original.maxLvl;

			if (original.state == original.SLEEPING) {
				state = SLEEPING;
			} else if (original.state == original.HUNTING) {
				state = HUNTING;
			} else {
				state = WANDERING;
			}

			if (original instanceof AbyssalMob){
				timeLeft = 3f;
			}

			if(!isAlive()) {
				// this is actually possible.
				die(null);
			}

		}

		public Mob getOriginal(){
			if (original != null) {
				original.HP = HP;
				original.pos = pos;
			}
			return original;
		}

		private float timeLeft = 6f;

		@Override
		protected boolean act() {
			if (timeLeft <= 0){
				Mob original = getOriginal();
				this.original = null;
				original.clearTime();
				GameScene.add(original);
				if(original.HP == 0) original.die(this); // avoid shittery.

				EXP = 0;
				destroy();
				sprite.killAndErase();
				CellEmitter.get(original.pos).burst(Speck.factory(Speck.WOOL), 4);
				Sample.INSTANCE.play(Assets.Sounds.PUFF);
				return true;
			} else {
				return super.act();
			}
		}

		@Override
		public void spend(float time) {
			if (!allied || !Dungeon.hero.heroClass.is(HeroClass.RAT_KING)) timeLeft -= time;
			super.spend(time);
		}

		public void makeAlly() {
			allied = true;
			alignment = Alignment.ALLY;
			if (drratedonEffect(this) > 4 && ChampionEnemy.isChampion(this)) ChampionEnemy.rollForChampionInstantly(this);
			timeLeft = Float.POSITIVE_INFINITY;
		}

		public int attackSkill(Char target) {
			return original.attackSkill(target);
		}

		public int drRoll() {
			return original.drRoll();
		}

		private static final float RESIST_FACTOR=.85f; // .9 in shpd

		@Override
		public int damageRoll() {
			int damage = original.damageRoll();
			Berserk berserk = buff(Berserk.class);
			if (berserk != null && drratedonEffect(this) > 2) damage = berserk.damageFactor(damage);
			if (!allied && (Dungeon.hero.hasTalent(Talent.RATSISTANCE) || Dungeon.hero.heroClass.is(HeroClass.RAT_KING))){
				damage *= Math.pow(RESIST_FACTOR, Dungeon.hero.pointsInTalentWithInnate(HeroClass.RAT_KING,Talent.RATSISTANCE));
			}
			return damage;
		}

		@Override
		public int defenseProc(Char enemy, int damage) {
			if (damage > 0 && drratedonEffect(this) > 2){
				Berserk berserk = Buff.affect(this, Berserk.class);
				berserk.damage(damage);
			}
			return super.defenseProc(enemy, damage);
		}

		@Override
		public int attackProc(Char enemy, int damage) {
			return super.attackProc(enemy, damage);
		}

		@Override
		public float attackDelay() {
			return original.attackDelay();
		}

		@Override
		public void rollToDropLoot() {
			original.pos = pos;
			original.rollToDropLoot();
		}

		@Override
		public String name() {
			String name = Messages.get(this, "name", original.name());
			if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
				name = ShatteredPixelDungeon.turnIntoRrrr(name);
			}
			return name;
		}

		{
			immunities.add(AllyBuff.class);
		}

		private static final String ORIGINAL = "original";
		private static final String ALLIED = "allied";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(ORIGINAL, original);
			bundle.put(ALLIED, allied);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);

			original = (Mob) bundle.get(ORIGINAL);
			defenseSkill = original.defenseSkill;
			EXP = original.EXP;

			allied = bundle.getBoolean(ALLIED);
			if (allied) alignment = Alignment.ALLY;
		}
	}

	// summons.
	private static float getModifier() { return Math.max(1, Dungeon.scalingDepth()/5f)*.8f; }

	public interface Ratforcements { }

	public static class SummonedRat extends Rat implements Ratforcements {
		{
			HP = HT *= getModifier();

			damageRange[0] *= getModifier();
			damageRange[1] *= getModifier();
			armorRange[0] *= getModifier();
			armorRange[1] *= getModifier();

			defenseSkill *= getModifier()*3;

			EXP = Math.round(EXP * getModifier());
			maxLvl = 4 + Dungeon.scalingDepth();
		}

		@Override
		protected boolean act() {
			if (drratedonEffect(this) > 4 && ChampionEnemy.isChampion(this)) ChampionEnemy.rollForChampionInstantly(this);
			return super.act();
		}

		@Override public int attackSkill(Char target) {
			return (int)( super.attackSkill(target) * getModifier()*5 );
		}
	}
	public static class SummonedAlbino extends Albino implements Ratforcements {
		{
			HP = HT *= getModifier()*2;

			damageRange[0] *= getModifier();
			damageRange[1] *= getModifier();
			armorRange[0] *= getModifier();
			armorRange[1] *= getModifier();

			defenseSkill *= getModifier()*6;

			EXP = Math.round(EXP * getModifier());
			maxLvl = 4 + Dungeon.scalingDepth();
		}

		@Override
		protected boolean act() {
			if (drratedonEffect(this) > 4 && ChampionEnemy.isChampion(this)) ChampionEnemy.rollForChampionInstantly(this);
			return super.act();
		}

		@Override public int attackSkill(Char target) {
			return (int)( super.attackSkill(target) * getModifier()*10 );
		}
	}
}
