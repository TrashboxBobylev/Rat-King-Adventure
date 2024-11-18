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

import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.Bestiary;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.ThreadRipper;
import com.zrp200.rkpd2.actors.mobs.npcs.MirrorImage;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.ShieldHalo;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.bombs.ArcaneBomb;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.wands.WandOfMagicMissile;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.BuffIndicator;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class ChampionEnemy extends Buff {

	{
		type = buffType.POSITIVE;
	}

	public int color;

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(color);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.aura( color );
		else target.sprite.clearAura();
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		if (target instanceof Hero && ((Hero) target).heroClass.is(HeroClass.RAT_KING))
			return Messages.get(this, "desc_rk");
		return Messages.get(this, "desc");
	}

	public String descRK() {
		return Messages.get(this, "desc_rk");
	}

	public void onAttackProc(Char enemy ){

	}

	public boolean canAttackWithExtraReach( Char enemy ){
		return false;
	}

	public float meleeDamageFactor(){
		return 1f;
	}

	public float damageTakenFactor(){
		return 1f;
	}

	public float evasionAndAccuracyFactor(){
		return 1f;
	}

	{
		immunities.add(AllyBuff.class);
	}

	public static boolean isChampion(Mob m){
		boolean isChamp = false;
		for (ChampionEnemy buff : m.buffs(ChampionEnemy.class)){
			isChamp = true;
		}
		return !isChamp;
	}

	public static void rollForChampion(Mob m){
		if (Dungeon.mobsToChampion <= 0) Dungeon.mobsToChampion = 4;

		Dungeon.mobsToChampion--;
		Class<? extends ChampionEnemy> title = getTitle();

		if (Dungeon.mobsToChampion <= 0 && Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)){
			makeChampion(m, title);
			if (m instanceof ThreadRipper) makeChampion(m, title);
			m.state = m.WANDERING;
		}
		rollWarp(m);
	}

	public static void rollWarp(Mob m){
		int warpAmount = 0;
		if (Dungeon.hero.buff(Warp.class) != null){
			warpAmount = Math.round(5 + Math.min(35, Dungeon.hero.buff(Warp.class).getStacks() / 4));
			if (m.properties().contains(Char.Property.BOSS)){
				warpAmount = Math.round(10 + Math.min(45, Dungeon.hero.buff(Warp.class).getStacks() / 5));
			}
		}
		boolean shouldBeWarped = Random.Int(100) < warpAmount;
		if (shouldBeWarped){
			Buff.affect(m, WarpedEnemy.class);
		}
	}

	public static Class[] championTitles = {
		Blazing.class, Projecting.class, AntiMagic.class, Giant.class,
		Blessed.class, Growing.class, Cursed.class, Splintering.class,
		Stone.class, Flowing.class, Voodoo.class, Explosive.class,
		Swiftness.class, Reflective.class, Paladin.class
	};

	public static Class[] heroTitles = {
			Blazing.class, Projecting.class, AntiMagic.class,
			Giant.class, Blessed.class, Cursed.class,
			Splintering.class, Paladin.class
	};

	public static String getRKDesc(Class<? extends ChampionEnemy> title){
		ChampionEnemy titleBuff = Reflection.newInstance(title);
		return "_" + Messages.titleCase(titleBuff.toString()) + "_\n" + titleBuff.descRK();
	}

	public static int getTitleColor(Class<? extends ChampionEnemy> title){
		ChampionEnemy titleBuff = Reflection.newInstance(title);
		return titleBuff.color;
	}

	private static Class<? extends ChampionEnemy> getTitle(){
		return Random.element(championTitles);
	}

	private static void makeChampion(Mob m, Class<? extends ChampionEnemy> title) {
		Buff.affect(m, title);
	}

	public static void rollForChampionInstantly(Mob m){
			makeChampion(m, getTitle());
			m.state = m.WANDERING;
	}

	public static class Blazing extends ChampionEnemy {

		{
			color = 0xFF8800;
		}

		@Override
		public void onAttackProc(Char enemy) {
			if (target instanceof Hero){
				boolean doubleFire = ((Hero) target).pointsInTalent(Talent.RK_FIRE) == 3;
				if (Random.Int(6) < ((Hero) target).pointsInTalent(Talent.RK_FIRE)){
					Buff.affect(enemy, GodSlayerBurning.class).reignite(enemy, doubleFire ? 11 : 7);
				}
				else Buff.affect(enemy, Burning.class).reignite(enemy, doubleFire ? 11 : 7);
			}
			else if (!Dungeon.level.water[enemy.pos]) {
				Buff.affect(enemy, Burning.class).reignite(enemy);
			}
		}

		@Override
		public float damageTakenFactor() {
			if (target instanceof Hero){
				if (Random.Int(4) < ((Hero) target).pointsInTalent(Talent.RK_FIRE)){
					for (int i : PathFinder.NEIGHBOURS9) {
						if (!Dungeon.level.solid[target.pos + i]) {
							GameScene.add(Blob.seed(target.pos + i, 2, Fire.class));
						}
					}
				}
			}
			return super.damageTakenFactor();
		}

		@Override
		public void detach() {
			//don't trigger when killed by being knocked into a pit
			if (target.flying || !Dungeon.level.pit[target.pos]) {
				for (int i : PathFinder.NEIGHBOURS9) {
					if (!Dungeon.level.solid[target.pos + i] && !Dungeon.level.water[target.pos + i]) {
						GameScene.add(Blob.seed(target.pos + i, 2, Fire.class));
					}
				}
			}
			super.detach();
		}

		@Override
		public float meleeDamageFactor() {
			if (target instanceof Hero){
				return 1.0f;
			}
			return 1.25f;
		}

		{
			immunities.add(Burning.class);
		}
	}

	public static class Paladin extends ChampionEnemy {
		{
			color = 0xfff2aa;
		}

		@Override
		public boolean act() {
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
				if (mob.alignment == target.alignment && mob.buff(Paladin.class) == null &&
				!Char.hasProp(mob, Char.Property.BOSS) && target.fieldOfView != null && target.fieldOfView[mob.pos]){
					Buff.prolong(mob, invulnerability.class, 2f);
				}
			}
			spend(TICK);
			return true;
		}

		@Override
		public boolean attachTo(Char target) {
			target.viewDistance = 8;
			return super.attachTo(target);
		}

		@Override
		public float damageTakenFactor() {
			if (target instanceof Hero){
				return 1.0f;
			}
			return 0.5f;
		}

		public static class invulnerability extends FlavourBuff{}
	}

	public static class Cursed extends ChampionEnemy {

		{
			color = 0x181212;
		}

		@Override
		public void onAttackProc(Char enemy) {
			if (target instanceof Hero && ((Hero) target).hasTalent(Talent.RK_CURSED)) {
				CursedWand.eldritchLevel = ((Hero) target).pointsInTalent(Talent.RK_CURSED);
			}
			CursedWand.cursedEffect(null, target, enemy);
			CursedWand.eldritchLevel = 0;
		}
	}

	public static class Projecting extends ChampionEnemy {

		{
			color = 0x8800FF;
		}

		@Override
		public float meleeDamageFactor() {
			if (target instanceof Hero){
				return 1.0f;
			}
			return 1.25f;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 4){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 4);

				return PathFinder.distance[target.pos] <= 4;
			}
		}
	}

	public static class Splintering extends ChampionEnemy {

		{
			color = 0xbfba72;
		}

		@Override
		public float damageTakenFactor() {
			if (target.HP >= 2) {
				ArrayList<Integer> candidates = new ArrayList<>();

				int[] neighbours = {target.pos + 1, target.pos - 1, target.pos + Dungeon.level.width(), target.pos - Dungeon.level.width()};
				for (int n : neighbours) {
					//TODO: port to summoning
					if (Dungeon.level.passable[n] && Actor.findChar( n ) == null) {
						candidates.add( n );
					}
				}

				if (candidates.size() > 0) {
					if (target instanceof Hero){
						MirrorImage clone = new MirrorImage();
						if (target.HP > 0) {
							clone.duplicate((Hero) target);
							clone.HP = clone.HT = target.HP / (Dungeon.hero.pointsInTalent(Talent.RK_SPLINT) == 3 ? 4 : 5);
							clone.pos = Random.element(candidates);
							clone.state = clone.HUNTING;

							Dungeon.level.occupyCell(clone);

							GameScene.add(clone, Dungeon.hero.hasTalent(Talent.RK_SPLINT) ? 0 : 1);
							Actor.addDelayed(new Pushing(clone, target.pos, clone.pos), -1);
						}
					}
	    			else {
						Mob clone = (Mob) Reflection.newInstance(target.getClass());
						if (target.HP > 0) {
							clone.HP = target.HP / 2;
							clone.pos = Random.element(candidates);
							clone.state = clone.HUNTING;

							Dungeon.level.occupyCell(clone);

							GameScene.add(clone, 1f);
							Actor.addDelayed(new Pushing(clone, target.pos, clone.pos), -1);

							target.HP -= clone.HP;
						}
					}
				}
			}
			return 1f;
		}

	}

	public static class AntiMagic extends ChampionEnemy {

		{
			color = 0x00FF00;
		}

		@Override
		public float damageTakenFactor() {
			if (target instanceof Hero){
				return 1.0f;
			}
			return 0.5f;
		}

		@Override
		public HashSet<Class> resistances() {
			if (target instanceof Hero){
				return com.zrp200.rkpd2.items.armor.glyphs.AntiMagic.RESISTS;
			}
			return super.resistances();
		}

		@Override
		public HashSet<Class> immunities() {
			if (target instanceof Hero){
				return new HashSet<>();
			}
			return super.immunities;
		}

		@Override
		public boolean attachTo(Char target) {
			if (target instanceof Hero){
				resistances.addAll(com.zrp200.rkpd2.items.armor.glyphs.AntiMagic.RESISTS);
				immunities.remove(com.zrp200.rkpd2.items.armor.glyphs.AntiMagic.RESISTS);
			}
			return super.attachTo(target);
		}

		public static void effect(Char hero, Char enemy){
			if (hero instanceof Hero && hero.buff(AntiMagic.class) != null && ((Hero) hero).pointsInTalent(Talent.RK_ANTIMAGIC) > 0){
				int dmg = ((Hero) hero).pointsInTalent(Talent.RK_ANTIMAGIC) * 2;

				int heal = Math.min(dmg, hero.HT-hero.HP);
				hero.HP += heal;
				Emitter e = hero.sprite.emitter();
				if (e != null && heal > 0) e.burst(Speck.factory(Speck.HEALING), Math.max(1,Math.round(heal*2f/5)));

				if (dmg > 0)
					enemy.damage(dmg + Dungeon.hero.pointsInTalent(Talent.RK_ANTIMAGIC)*2, new WandOfMagicMissile());

				ShieldHalo shield;

				GameScene.effect(shield = new ShieldHalo(hero.sprite));
				shield.hardlight(0x00CC00);
				shield.putOut();

				if (((Hero) hero).pointsInTalent(Talent.RK_ANTIMAGIC) == 3){
					for (Wand.Charger c : hero.buffs(Wand.Charger.class)){
						c.gainCharge(0.33f);
					}
				}
			}
		}

		{
			immunities.addAll(com.zrp200.rkpd2.items.armor.glyphs.AntiMagic.RESISTS);
		}

	}

	public static class Explosive extends ChampionEnemy {

		{
			color = 0xff4400;
		}

		@Override
		public void detach() {
			new ArcaneBomb().explode(target.pos);
			super.detach();
		}
	}

	public static class Reflective extends ChampionEnemy {
		{
			color = 0x981a47;
		}
	}

	public static class Voodoo extends ChampionEnemy {

		{
			color = 0x3d0082;
		}

		@Override
		public float damageTakenFactor() {
			return 1.25f;
		}

		@Override
		public void detach() {
			ArrayList<Class<?extends Mob>> mobsToSpawn;

			mobsToSpawn = Bestiary.getMobRotation(Dungeon.depth);

			Mob clone = Reflection.newInstance(mobsToSpawn.remove(0));
			ChampionEnemy.rollForChampion(clone);
			clone.HP = clone.HT = Math.round(clone.HT);
			clone.pos = target.pos;
			clone.state = clone.HUNTING;
			clone.alignment = target.alignment;

			Dungeon.level.occupyCell(clone);
			GameScene.add( clone );
			super.detach();
		}
	}

	public static class Stone extends ChampionEnemy {
		{
			color = 0x727272;
		}

		@Override
		public float damageTakenFactor() {
			return Math.max(0.1f, (target.HP * 1f /target.HT));
		}
	}

	public static class Flowing extends ChampionEnemy {
		{
			color = 0xb7f5ff;
		}
	}

	//Also makes target large, see Char.properties()
	public static class Giant extends ChampionEnemy {

		{
			color = 0x0088FF;
		}

		{
			properties.add(Char.Property.LARGE);
		}

		@Override
		public boolean attachTo(Char target) {
			if (target instanceof Hero){
				properties.remove(Char.Property.LARGE);
			}
			return super.attachTo(target);
		}

		@Override
		public float damageTakenFactor() {
			if (target instanceof Hero){
				return 0.5f;
			}
			return 0.2f;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 2){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 2);

				return PathFinder.distance[target.pos] <= 2;
			}
		}
	}

	public static class Swiftness extends ChampionEnemy {
		{
			color = 0x2900ff;
		}

		@Override
		public boolean attachTo(Char target) {
			Buff.affect(target, Shrink.class);
			return super.attachTo(target);
		}
	}

	public static class Blessed extends ChampionEnemy {

		{
			color = 0xFFFF00;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return 4f;
		}
	}

	public static class Growing extends ChampionEnemy {

		{
			color = 0xFF0000;
		}

		private float multiplier = 1.19f;

		@Override
		public boolean act() {
			multiplier += 0.01f;
			spend(4*TICK);
			return true;
		}

		@Override
		public float meleeDamageFactor() {
			return multiplier;
		}

		@Override
		public float damageTakenFactor() {
			return 1f/multiplier;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return multiplier;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int)(100*(multiplier-1)), (int)(100*(1 - 1f/multiplier)));
		}

		private static final String MULTIPLIER = "multiplier";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(MULTIPLIER, multiplier);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			multiplier = bundle.getFloat(MULTIPLIER);
		}
	}

}
