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

package com.zrp200.rkpd2.actors.hero.abilities.huntress;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.BlobImmunity;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.mobs.npcs.DirectableAlly;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.particles.ShaftParticle;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.artifacts.TalismanOfForesight;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpiritHawk extends ArmorAbility {

	@Override
	public String targetingPrompt() {
		if (getHawk() == null) {
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
		if (getHawk() == null) {
			return super.chargeUse(hero);
		} else {
			return 0;
		}
	}

	@Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
		HawkAlly ally = getHawk();

		if (ally != null){
			if (target == null){
				return;
			} else {
				ally.directTocell(target);
			}
		} else {
			ArrayList<Integer> spawnPoints = new ArrayList<>();
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				int p = hero.pos + PathFinder.NEIGHBOURS8[i];
				if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
					spawnPoints.add(p);
				}
			}

			if (!spawnPoints.isEmpty()){
				armor.useCharge(hero, this);

				ally = new HawkAlly();
				ally.pos = Random.element(spawnPoints);
				GameScene.add(ally);

				ScrollOfTeleportation.appear(ally, ally.pos);
				Dungeon.observe();

				Invisibility.dispel();
				hero.spendAndNext(Actor.TICK);

			} else {
				GLog.w(Messages.get(this, "no_space"));
			}
		}

	}

	@Override
	public int icon() {
		return HeroIcon.SPIRIT_HAWK;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.EAGLE_EYE, Talent.GO_FOR_THE_EYES, Talent.SWIFT_SPIRIT, Talent.BEAK_OF_POWER, Talent.HEROIC_ENERGY};
	}

	public static HawkAlly getHawk(){
		for (Char ch : Actor.chars()){
			if (ch instanceof HawkAlly){
				return (HawkAlly) ch;
			}
		}
		return null;
	}
	@Override public boolean isActive(Hero hero) { return getHawk() != null; }

	public static class HawkAlly extends DirectableAlly {

		{
			spriteClass = HawkSprite.class;

			HP = HT = 10;
			defenseSkill = 60;

			flying = true;
			if (Dungeon.hero != null) {
				viewDistance = (int) GameMath.gate(6, 6 + Dungeon.hero.pointsInTalent(Talent.EAGLE_EYE, Talent.SHADOWSPEC_SLICE), 8);
				baseSpeed = 2f + Dungeon.hero.pointsInTalent(Talent.SWIFT_SPIRIT) / 2f;
			} else {
				viewDistance = 6;
				baseSpeed = 2f;
			}
			attacksAutomatically = false;

			immunities.addAll(new BlobImmunity().immunities());
			immunities.add(AllyBuff.class);
		}

		@Override
		public float attackDelay() {
			if (buff(Talent.LethalMomentumTracker.class) != null){
				buff(Talent.LethalMomentumTracker.class).detach();
				return 0;
			}
			return super.attackDelay();
		}

		@Override
		public int attackSkill(Char target) {
			return 60;
		}

		private int dodgesUsed = 0;
		private float timeRemaining = 100f;

		@Override
		public int defenseSkill(Char enemy) {
			if (Dungeon.hero.hasTalent(Talent.SWIFT_SPIRIT) &&
					dodgesUsed < 2*Dungeon.hero.pointsInTalent(Talent.SWIFT_SPIRIT, Talent.BLOODFLARE_SKIN)) {
				dodgesUsed++;
				return Char.INFINITE_EVASION;
			}
			return super.defenseSkill(enemy);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange(5, 10);
		}

		@Override
		public int attackProc(Char enemy, int damage) {
			damage = super.attackProc( enemy, damage );
			switch (Dungeon.hero.pointsInTalent(Talent.GO_FOR_THE_EYES)) {
				case 1:
					Buff.prolong(enemy, Blindness.class, 2);
					break;
				case 2:
					Buff.prolong(enemy, Blindness.class, 5);
					break;
				case 3:
					Buff.prolong(enemy, Blindness.class, 5);
					Buff.prolong(enemy, Cripple.class, 2);
					break;
				case 4:
					Buff.prolong(enemy, Blindness.class, 5);
					Buff.prolong(enemy, Cripple.class, 5);
					break;
				default:
					//do nothing
			}
			if (Dungeon.hero.hasTalent(Talent.GO_FOR_THE_EYES, Talent.SHADOWSPEC_SLICE)) {
				Buff.prolong( enemy, Blindness.class, 2*Dungeon.hero.pointsInTalent(Talent.GO_FOR_THE_EYES, Talent.SHADOWSPEC_SLICE) );
			}
			if (Dungeon.hero.hasTalent(Talent.BEAK_OF_POWER)){
				Buff.append(Dungeon.hero, TalismanOfForesight.CharAwareness.class,
						Dungeon.hero.pointsInTalent(Talent.BEAK_OF_POWER)*5).charID = enemy.id();
				if (Random.Int(5) < Dungeon.hero.pointsInTalent(Talent.BEAK_OF_POWER)){
					SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
					if (bow == null && Dungeon.hero.belongings.weapon instanceof SpiritBow){
						bow = (SpiritBow) Dungeon.hero.belongings.weapon;
					}
					if (bow != null) damage = bow.proc( this, enemy, damage );
				}
			}

			return damage;
		}

		@Override
		public boolean canAttack(Char enemy) {
			if (Dungeon.hero.pointsInTalent(Talent.BEAK_OF_POWER) > 3){
				Ballistica attack = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE);
				return attack.collisionPos == enemy.pos;
			}
			return super.canAttack(enemy);
		}

		protected boolean doAttack( Char enemy ) {
			if (Dungeon.level.adjacent(pos, enemy.pos)){
				return super.doAttack( enemy );
			} else if (Dungeon.hero.pointsInTalent(Talent.BEAK_OF_POWER) > 3) {

				if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						((MissileSprite) sprite.parent.recycle(MissileSprite.class)).
								reset(sprite,
										enemy.sprite,
										new SpiritBow().knockArrow(),
										new Callback() {
											@Override
											public void call() {
												attack(enemy,
														1,
														0, 1);
												spend(attackDelay());
												next();
											}
										});
					return false;
				} else {
					return super.doAttack(enemy);
				}
			}
			return super.doAttack(enemy);
		}

		@Override
		protected boolean act() {
			if (timeRemaining <= 0){
				die(null);
				Dungeon.hero.interrupt();
				return true;
			}
			viewDistance = 6+Dungeon.hero.pointsInTalent(Talent.EAGLE_EYE);
			baseSpeed = 2f + Dungeon.hero.pointsInTalent(Talent.SWIFT_SPIRIT, Talent.BLOODFLARE_SKIN)/2f;
			boolean result = super.act();
			Dungeon.level.updateFieldOfView( this, fieldOfView );
			GameScene.updateFog(pos, viewDistance+(int)Math.ceil(speed()));
			return result;
		}

		@Override
		public void die(Object cause) {
			flying = false;
			super.die(cause);
		}

		@Override
        public void spend(float time) {
			super.spend(time);
			timeRemaining -= time;
		}

		@Override
		public void destroy() {
			super.destroy();
			Dungeon.observe();
			GameScene.updateFog();
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
		public String name() {
			if (Dungeon.hero.heroClass.is(HeroClass.RAT_KING)){
				String nameRat = Messages.get(this, "name_rat");
				if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
					nameRat = ShatteredPixelDungeon.turnIntoRrrr(nameRat);
				}
				return nameRat;
			}
			return super.name();
		}

		@Override
		public String description() {
			String message = Messages.get(this, "desc", (int)timeRemaining);
            if (Dungeon.hero.heroClass.is(HeroClass.RAT_KING)){
                message = Messages.get(this, "desc_rat", (int)timeRemaining);
            }
            if (Actor.chars().contains(this)){
				message += "\n\n" + Messages.get(this, "desc_remaining", (int)timeRemaining);
				if (dodgesUsed < 2*Dungeon.hero.pointsInTalent(Talent.SWIFT_SPIRIT)){
					message += "\n" + Messages.get(this, "desc_dodges", (2*Dungeon.hero.pointsInTalent(Talent.SWIFT_SPIRIT) - dodgesUsed));
				}
			}
			return message;
		}

		private static final String DODGES_USED     = "dodges_used";
		private static final String TIME_REMAINING  = "time_remaining";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(DODGES_USED, dodgesUsed);
			bundle.put(TIME_REMAINING, timeRemaining);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			dodgesUsed = bundle.getInt(DODGES_USED);
			timeRemaining = bundle.getFloat(TIME_REMAINING);
		}
	}

	public static class HawkSprite extends MobSprite {

		public HawkSprite() {
			super();

			texture( Assets.Sprites.SPIRIT_HAWK );
			if (Dungeon.hero != null && Dungeon.hero.heroClass.is(HeroClass.RAT_KING)){
				texture(Assets.Sprites.ROYAL_OWL);
			}

			TextureFilm frames = new TextureFilm( texture, 15, 15 );

			int c = 0;

			idle = new Animation( 6, true );
			idle.frames( frames, 0, 1 );

			run = new Animation( 8, true );
			run.frames( frames, 0, 1 );

			attack = new Animation( 12, false );
			attack.frames( frames, 2, 3, 0, 1 );

			die = new Animation( 12, false );
			die.frames( frames, 4, 5, 6 );

			play( idle );
		}

		@Override
		public int blood() {
			return 0xFF00FFFF;
		}

		@Override
		public void die() {
			super.die();
			emitter().start( ShaftParticle.FACTORY, 0.3f, 4 );
			emitter().start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
		}
	}
}
