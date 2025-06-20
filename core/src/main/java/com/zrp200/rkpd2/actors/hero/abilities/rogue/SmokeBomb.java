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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blizzard;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.ConfusionGas;
import com.zrp200.rkpd2.actors.blobs.FrostFire;
import com.zrp200.rkpd2.actors.blobs.Inferno;
import com.zrp200.rkpd2.actors.blobs.Regrowth;
import com.zrp200.rkpd2.actors.blobs.SmokeScreen;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Amok;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Charm;
import com.zrp200.rkpd2.actors.buffs.Dread;
import com.zrp200.rkpd2.actors.buffs.FrostImbue;
import com.zrp200.rkpd2.actors.buffs.Haste;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MagicalSight;
import com.zrp200.rkpd2.actors.buffs.Sleep;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.npcs.NPC;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import static com.watabou.utils.Reflection.newInstance;
import static com.zrp200.rkpd2.Dungeon.hero;

public class SmokeBomb extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	@Override
	public boolean useTargeting() {
		return false;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	public static boolean isShadowStep(Hero hero) {
		return hero != null
				&& hero.hasTalent(Talent.SHADOW_STEP, Talent.SMOKE_AND_MIRRORS) && hero.invisible > 0;
	}
	@Override
	public float chargeUse(Hero hero) {
		float chargeUse = super.chargeUse(hero);
		if(isShadowStep(hero)) {
			//reduced charge use by 20%/36%/50%/60%
			chargeUse *= Math.pow(0.795, hero.pointsInTalent(Talent.SHADOW_STEP));
		}
		return chargeUse;
	}

	public static boolean isValidTarget(Hero hero, int target, int limit) {

		if (target != hero.pos && hero.rooted){
			PixelScene.shake( 1, 1f );
			return false;
		}

		PathFinder.buildDistanceMap(hero.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null), limit);

		if ( PathFinder.distance[target] == Integer.MAX_VALUE ||
				!Dungeon.level.heroFOV[target] ||
				(target != hero.pos && Actor.findChar( target ) != null)) {

			GLog.w( Messages.get(SmokeBomb.class, "fov") );
			return false;
		}
		return true;
	}

	public static void blindAdjacentMobs(Hero hero) {
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (Dungeon.level.adjacent(mob.pos, hero.pos) && mob.alignment != Char.Alignment.ALLY) {
				Buff.prolong(mob, Blindness.class, Blindness.DURATION / 2f);
				if (mob.state == mob.HUNTING) mob.state = mob.WANDERING;
				mob.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 4);
				if (hero.hasTalent(Talent.RAT_AGE)) {
					GameScene.add(Blob.seed(mob.pos, 80, Inferno.class));
					if (hero.pointsInTalent(Talent.RAT_AGE) > 1){
						GameScene.add(Blob.seed(mob.pos, 80, Blizzard.class));
					}
					if (hero.pointsInTalent(Talent.RAT_AGE) > 2){
						GameScene.add(Blob.seed(mob.pos, 80, ConfusionGas.class));
					}
					if (hero.pointsInTalent(Talent.RAT_AGE) > 3){
						GameScene.add(Blob.seed(mob.pos, 80, Regrowth.class));
					}
				}
				if (hero.hasTalent(Talent.QUANTUM_POSITION)){
					Buff.affect(mob, Vertigo.class, 3*hero.pointsInTalent(Talent.QUANTUM_POSITION));
					Buff.affect(mob, Terror.class, 3*hero.pointsInTalent(Talent.QUANTUM_POSITION)).object = hero.id();
				}
			}
		}
	}
	public static void throwSmokeBomb(Hero hero, int target) {
		CellEmitter.get( hero.pos ).burst( Speck.factory( Speck.WOOL ), 10 );
		ScrollOfTeleportation.appear( hero, target );
		Sample.INSTANCE.play( Assets.Sounds.PUFF );
		Dungeon.level.occupyCell( hero );
		Dungeon.observe();
		GameScene.updateFog();
	}


	public static <T extends Mob> void doBodyReplacement(Hero hero, Talent talent, Class<T> ninjaLogClass) {
		if(!hero.hasTalent(talent)) return;
		for (Char ch : Actor.chars()){
			if (ninjaLogClass.isInstance(ch)){
				ch.die(null);
			}
		}

		T n = newInstance(ninjaLogClass);
		n.pos = hero.pos;
		GameScene.add(n);
		Dungeon.level.occupyCell(n);
	}

	@Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target != null) {
			if(!isValidTarget(hero, target, 10)) return;

			if (!isShadowStep(hero)) {
				blindAdjacentMobs(hero);
				doBodyReplacement(hero, Talent.BODY_REPLACEMENT, NinjaLog.class);
				applyHastyRetreat(hero);

				if (hero.hasTalent(Talent.FRIGID_TOUCH)){
					for (int i = 0; i < Dungeon.level.length(); i++){
						if (Dungeon.level.insideMap(i) && Dungeon.level.heroFOV[i] && !Dungeon.level.solid[i]){
							GameScene.add(Blob.seed(i, 2 + (hero.pointsInTalent(Talent.FRIGID_TOUCH)-1)*2, FrostFire.class));
						}
					}
					Buff.affect(hero, FrostImbue.class,1 + (hero.pointsInTalent(Talent.FRIGID_TOUCH)-1)*2 );
				}
			}

			throwSmokeBomb(hero, target);
			if (!isShadowStep(hero)) {
				hero.spendAndNext(Actor.TICK);
			} else {
				hero.next();
			}
			armor.useCharge(hero,this);
		}
	}

	public static void applyHastyRetreat(Hero hero) {
		float duration = hero.shiftedPoints(Talent.HASTY_RETREAT, Talent.SMOKE_AND_MIRRORS);
		if (hero.pointsInTalent(Talent.FUN) > 2){
			GameScene.add(Blob.seed(hero.pos, 1000, SmokeScreen.class));
			Buff.affect(hero, MagicalSight.class, 3f*(hero.pointsInTalent(Talent.FUN)-2));
		}
		if(duration == 0) return;
		duration += 0.67f;
		Buff.affect(hero, Haste.class, duration);
		Buff.affect(hero, Invisibility.class, duration);
	}

	@Override
	public int icon() {
		return HeroIcon.SMOKE_BOMB;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.HASTY_RETREAT, Talent.BODY_REPLACEMENT, Talent.SHADOW_STEP, Talent.FRIGID_TOUCH, Talent.HEROIC_ENERGY};
	}

	@Override
	public boolean isTracked(Hero hero) {
		// keeps summon stats consistent.
		return Actor.containsClass(NinjaLog.class);
	}

	public static class NinjaLog extends NPC {

		{
			spriteClass = NinjaLogSprite.class;
			defenseSkill = 0;

			properties.add(Property.INORGANIC); //wood is organic, but this is accurate for game logic

			alignment = Alignment.ALLY;

			// TODO isn't it kinda weird that the two variants have the same HP?
			HT = 20;
			if (hero != null) HT *= hero.pointsInTalent(false,Talent.BODY_REPLACEMENT, Talent.SHADOWSPEC_SLICE, Talent.SMOKE_AND_MIRRORS);
			HP = HT;
		}

		{
			immunities.add(FrostFire.class);
		}

		protected Talent talent = Talent.BODY_REPLACEMENT;
		protected int drScaling = 5;

		@Override
		public int drRoll() {
			if (hero.hasTalent(Talent.SHADOWSPEC_SLICE)){
				return Random.NormalIntRange(hero.pointsInTalent(Talent.SHADOWSPEC_SLICE),
						(int)hero.pointsInTalent(Talent.SHADOWSPEC_SLICE)*5);
			}
			return super.drRoll() + Random.NormalIntRange(hero.pointsInTalent(Talent.BODY_REPLACEMENT, Talent.SMOKE_AND_MIRRORS),
					(int)hero.byTalent(Talent.BODY_REPLACEMENT, 5, Talent.SMOKE_AND_MIRRORS, 3));
		}

		{
			immunities.add( Dread.class );
			immunities.add( Terror.class );
			immunities.add( Amok.class );
			immunities.add( Charm.class );
			immunities.add( Sleep.class );
			immunities.add( AllyBuff.class );
		}

	}

	public static class NinjaLogSprite extends MobSprite {

		public NinjaLogSprite(){
			super();

			texture( Assets.Sprites.NINJA_LOG );

			TextureFilm frames = new TextureFilm( texture, 11, 12 );

			idle = new Animation( 0, true );
			idle.frames( frames, 0 );

			run = idle.clone();
			attack = idle.clone();
			zap = attack.clone();

			die = new Animation( 12, false );
			die.frames( frames, 1, 2, 3, 4 );

			play( idle );

		}

		@Override
		public void showAlert() {
			//do nothing
		}

		@Override
		public int blood() {
			return 0xFF966400;
		}

	}
}
