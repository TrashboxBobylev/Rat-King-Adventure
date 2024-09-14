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

package com.zrp200.rkpd2.actors.hero.abilities.rogue;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpiritHawk;
import com.zrp200.rkpd2.actors.mobs.npcs.DirectableAlly;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.particles.SmokeParticle;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.armor.glyphs.AntiMagic;
import com.zrp200.rkpd2.items.armor.glyphs.Brimstone;
import com.zrp200.rkpd2.items.weapon.missiles.Kunai;
import com.zrp200.rkpd2.levels.CityLevel;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.hero;

public class ShadowClone extends ArmorAbility {

	@Override
	public String targetingPrompt() {
		if (getShadowAlly() == null) {
			return super.targetingPrompt();
		} else {
			return Messages.get(this, "prompt");
		}
	}

	@Override
	public boolean useTargeting(){
		return false;
	}

	{
		baseChargeUse = 35f;
	}

	@Override
	public float chargeUse(Hero hero) {
		if (getShadowAlly() == null) {
			return super.chargeUse(hero);
		} else {
			return 0;
		}
	}

	@Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
		ShadowAlly ally = getShadowAlly();

		if (ally != null){
			if (target != null) {
				ally.directTocell(target);
			}
		} else {
			ArrayList<Integer> spawnPoints = new ArrayList<>();
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				int p = hero.pos + PathFinder.NEIGHBOURS8[i];
				if (Actor.findChar(p) == null && Dungeon.level.passable[p]) {
					spawnPoints.add(p);
				}
			}

			if (!spawnPoints.isEmpty()){
				armor.useCharge(hero, this);

				ally = new ShadowAlly(hero.lvl);
				ally.pos = Random.element(spawnPoints);
				GameScene.add(ally);

				ShadowAlly.appear(ally, ally.pos);

				Invisibility.dispel();
				hero.spendAndNext(Actor.TICK);

			} else {
				GLog.w(Messages.get(SpiritHawk.class, "no_space"));
			}
		}

	}

	@Override
	public int icon() {
		return HeroIcon.SHADOW_CLONE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.SHADOW_BLADE, Talent.CLONED_ARMOR, Talent.PERFECT_COPY, Talent.DAR_MAGIC, Talent.HEROIC_ENERGY};
	}

	public static ShadowAlly getShadowAlly(){
		for (Char ch : Actor.chars()){
			if (ch instanceof ShadowAlly){
				return (ShadowAlly) ch;
			}
		}
		return null;
	}
	@Override public boolean isActive() { return getShadowAlly() != null; }

	public static class ShadowAlly extends DirectableAlly {

		{
			spriteClass = ShadowSprite.class;

			HP = HT = 80;

			immunities.add(AllyBuff.class);

			properties.add(Property.INORGANIC);
		}

		@Override
		public String name() {
			return Messages.get(this, "name", hero.heroClass.title());
		}

		public ShadowAlly(){
			super();
		}

		public ShadowAlly( int heroLevel ){
			super();
			int hpBonus = 15 + 5*heroLevel;
			hpBonus = Math.round(/*0.1f*/1/8f * hero.pointsInTalent(Talent.PERFECT_COPY) * hpBonus);
			hpBonus = Math.round(0.1f * Dungeon.hero.pointsInTalent(Talent.BLOODFLARE_SKIN) * hpBonus);
			if (hpBonus > 0){
				HT += hpBonus;
				HP += hpBonus;
			}
			defenseSkill = heroLevel + 5; //equal to base hero defense skill
		}

		@Override
		public boolean canAttack(Char enemy) {
			if (Dungeon.hero.pointsInTalent(Talent.DAR_MAGIC) > 0 || Dungeon.hero.pointsInTalent(Talent.SILVA_RANGE) > 0){
				Ballistica attack = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE);
				return attack.collisionPos == enemy.pos;
			}
			return super.canAttack(enemy);
		}

		protected boolean doAttack( Char enemy ) {
			if (Dungeon.level.adjacent(pos, enemy.pos)){
				return super.doAttack( enemy );
			} else {

				if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
					if (Dungeon.hero.pointsInTalent(Talent.DAR_MAGIC) < 3) {
						((MissileSprite) sprite.parent.recycle(MissileSprite.class)).
								reset(sprite,
										enemy.sprite,
										new Kunai(),
										new Callback() {
											@Override
											public void call() {
												attack(enemy,
														Dungeon.hero.byTalent(Talent.DAR_MAGIC, 0.25f,
																				Talent.SILVA_RANGE, 0.125f),
														0, 1);
												spend(attackDelay());
												next();
											}
										});
					} else {
						//teleport
						int bestPos = -1;
						for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
							int p = enemy.pos + PathFinder.NEIGHBOURS8[i];
							if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
								bestPos = p;
							}
						}
						if (bestPos != -1){
							appear(this, bestPos);
							sprite.attack(enemy.pos, new Callback() {
								@Override
								public void call() {
									attack(enemy);
									spend(attackDelay());

											if (Dungeon.hero.pointsInTalent(Talent.DAR_MAGIC) == 4){
												int count = 32;
												int newPos = -1;
												for (int i : PathFinder.NEIGHBOURS8){
													if (Actor.findChar( Dungeon.hero.pos + i ) == null && Dungeon.level.passable[Dungeon.hero.pos + i]){
														newPos = Dungeon.hero.pos + i;
													}
												}

												if (newPos != -1) {

													if (Dungeon.level.heroFOV[pos]) CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);
													appear(ShadowAlly.this, newPos);
													if (Dungeon.level.heroFOV[pos]) CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);
												}


											}
									next();
								}
							});
							return false;
//							spend(attackDelay());
						}
					}
					return false;
				} else {
					return super.doAttack(enemy);
				}
			}
		}

		@Override
		protected boolean act() {
			int oldPos = pos;
			boolean result = super.act();
			//partially simulates how the hero switches to idle animation
			if ((pos == target || oldPos == pos) && sprite.looping()){
				sprite.idle();
			}
			return result;
		}

		@Override
		public void defendPos(int cell) {
			GLog.i(Messages.get(this, "direct_defend"));
			super.defendPos(cell);
		}

		@Override
		public void followHero() {
			GLog.i(Messages.get(this, "direct_follow"));
			super.followHero();
		}

		@Override
		public void targetChar(Char ch) {
			GLog.i(Messages.get(this, "direct_attack"));
			super.targetChar(ch);
		}

		@Override
		public int attackSkill(Char target) {
			return defenseSkill+5; //equal to base hero attack skill
		}

		@Override
		public int damageRoll() {
			int damage = Random.NormalIntRange(10, 20);
			int heroDamage = hero.damageRoll();
			heroDamage /= hero.attackDelay(); //normalize hero damage based on atk speed
			heroDamage = Math.round(/*0.08f*/0.1f * hero.pointsInTalent(Talent.SHADOW_BLADE) * heroDamage);
			if (heroDamage > 0){
				damage += heroDamage;
			}
			if (Dungeon.hero.pointsInTalent(Talent.DAR_MAGIC) == 3){
				damage *= 0.75f;
			}
			return damage;
		}

		@Override
		public int attackProc( Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			if ((Dungeon.hero.canHaveTalent(Talent.SHADOWSPEC_SLICE) &&
					(Random.Int(4) < Dungeon.hero.pointsInTalent(Talent.SHADOWSPEC_SLICE)
							&& Dungeon.hero.belongings.weapon != null)) ||
					(Random.Int(4) < hero.pointsInTalent(Talent.SHADOW_BLADE)
					&& hero.belongings.weapon() != null)){
				return hero.belongings.weapon().proc( this, enemy, damage );
			} else {
				return damage;
			}
		}

		@Override
		public int drRoll() {
			int dr = super.drRoll();
			int heroRoll = hero.drRoll();
			if (Dungeon.hero.canHaveTalent(Talent.CLONED_ARMOR))
				heroRoll = Math.round(/*0.12f*/0.16f * hero.pointsInTalent(Talent.CLONED_ARMOR) * heroRoll);
			else
				heroRoll = Math.round(0.125f * Dungeon.hero.pointsInTalent(Talent.SHADOWSPEC_SLICE) * heroRoll);
			if (heroRoll > 0){
				dr += heroRoll;
			}
			return dr;
		}

		@Override
		public boolean isImmune(Class effect) {
			if (effect == Burning.class
					&& Random.Int(4) < Dungeon.hero.pointsInTalent(Talent.CLONED_ARMOR)
					&& Dungeon.hero.belongings.armor() != null
					&& Dungeon.hero.belongings.armor().hasGlyph(Brimstone.class, this)){
				return true;
			}
			return super.isImmune(effect);
		}

		@Override
		public int defenseProc(Char enemy, int damage) {
			damage = super.defenseProc(enemy, damage);
			if (Random.Int(4) < hero.pointsInTalent(Talent.CLONED_ARMOR)
					&& hero.belongings.armor() != null){
				return hero.belongings.armor().proc( enemy, this, damage );
			} else {
				return damage;
			}
		}

		@Override
		public void damage(int dmg, Object src) {

			//TODO improve this when I have proper damage source logic
			if (Random.Int(4) < Dungeon.hero.pointsInTalent(Talent.CLONED_ARMOR)
					&& Dungeon.hero.belongings.armor() != null
					&& Dungeon.hero.belongings.armor().hasGlyph(AntiMagic.class, this)
					&& AntiMagic.RESISTS.contains(src.getClass())){
				dmg -= AntiMagic.drRoll(Dungeon.hero, Dungeon.hero.belongings.armor().buffedLvl());
				dmg = Math.max(dmg, 0);
			}

			super.damage(dmg, src);
		}

		@Override
		public float speed() {
			float speed = super.speed();

			//moves 2 tiles at a time when returning to the hero
			if (state == WANDERING
					&& defendingPos == -1
					&& Dungeon.level.distance(pos, Dungeon.hero.pos) > 1){
				speed *= 2;
			}

			return speed;
		}

		@Override
		public boolean canInteract(Char c) {
			if (super.canInteract(c)){
				return true;
			} else if (Dungeon.level.distance(pos, c.pos) <= Math.max(
					Dungeon.hero.shiftedPoints(Talent.PERFECT_COPY),
					Dungeon.hero.pointsInTalent(Talent.ASTRAL_CHARGE))) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean interact(Char c) {

			if (!hero.hasTalent(Talent.PERFECT_COPY)){
				return super.interact(c);
			}

			//some checks from super.interact
			if (!Dungeon.level.passable[pos] && !c.flying){
				return true;
			}

			if (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[c.pos]
					|| c.properties().contains(Property.LARGE) && !Dungeon.level.openSpace[pos]){
				return true;
			}

			int curPos = pos;

			//warp instantly with the clone
			PathFinder.buildDistanceMap(c.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
			if (PathFinder.distance[pos] == Integer.MAX_VALUE){
				return true;
			}
			appear(this, hero.pos);
			appear(hero, curPos);
			Dungeon.observe();
			GameScene.updateFog();
			return true;
		}

		public static void appear(Char ch, int pos) {

			ch.sprite.interruptMotion();

			if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[ch.pos]){
				Sample.INSTANCE.play(Assets.Sounds.PUFF);
			}

			ch.move( pos );
			if (ch.pos == pos) ch.sprite.place( pos );

			if (Dungeon.level.heroFOV[pos] || ch == hero ) {
				ch.sprite.emitter().burst(SmokeParticle.FACTORY, 10);
			}
		}

		@Override
		public String description() {
			if (Dungeon.hero.heroClass.is(HeroClass.RAT_KING)){
				return Messages.get(this, "desc_rat");
			}
			return super.description();
		}

		private static final String DEF_SKILL = "def_skill";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(DEF_SKILL, defenseSkill);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			defenseSkill = bundle.getInt(DEF_SKILL);
		}
	}

	public static class ShadowSprite extends MobSprite {

		private Emitter smoke;

		public ShadowSprite() {
			super();

			texture( hero.heroClass.spritesheet() );
			copyAnimations(hero.sprite);

			die.frames( idle.frames[0] );
			die.delay = 1/20f;

			idle();
			resetColor();
		}

		@Override
		public void onComplete(Tweener tweener) {
			super.onComplete(tweener);
		}

		@Override
		public void resetColor() {
			super.resetColor();
			alpha(0.8f);
			brightness(0.0f);
		}

		@Override
		public void link( Char ch ) {
			super.link( ch );
			renderShadow = false;

			if (smoke == null) {
				smoke = emitter();
				smoke.pour( CityLevel.Smoke.factory, 0.2f );
			}
		}

		@Override
		public void update() {

			super.update();

			if (smoke != null) {
				smoke.visible = visible;
			}
		}

		@Override
		public void kill() {
			super.kill();

			if (smoke != null) {
				smoke.on = false;
			}
		}
	}
}
