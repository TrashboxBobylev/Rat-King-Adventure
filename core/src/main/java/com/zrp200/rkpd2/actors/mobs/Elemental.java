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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.blobs.Freezing;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Chill;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.hero.spells.ShieldOfLight;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Lightning;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.effects.TargetedCell;
import com.zrp200.rkpd2.effects.particles.ElmoParticle;
import com.zrp200.rkpd2.items.potions.PotionOfFrost;
import com.zrp200.rkpd2.items.potions.PotionOfLiquidFlame;
import com.zrp200.rkpd2.items.quest.Embers;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTransmutation;
import com.zrp200.rkpd2.items.trinkets.RatSkull;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.weapon.enchantments.Shocking;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ElementalSprite;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class Elemental extends Mob {

	{
		HP = HT = 60;
		defenseSkill = 20;
		
		EXP = 10;
		maxLvl = 20;
		
		flying = true;
	}

	protected boolean summonedALly;
	
	@Override
	public int damageRoll() {
		if (!summonedALly) {
			return Random.NormalIntRange(20, 25);
		} else {
			int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
			return Random.NormalIntRange(5*regionScale, 5 + 5*regionScale);
		}
	}
	
	@Override
	public int attackSkill( Char target ) {
		if (!summonedALly) {
			return 25;
		} else {
			int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
			return 5 + 5*regionScale;
		}
	}

	public void setSummonedALly(){
		summonedALly = true;
		//sewers are prison are equivalent, otherwise scales as normal (2/2/3/4/5)
		int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
		defenseSkill = 5*regionScale;
		HT = 15*regionScale;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 5);
	}
	
	protected int rangedCooldown = Random.NormalIntRange( 3, 5 );
	
	@Override
	protected boolean act() {
		if (state == HUNTING){
			rangedCooldown--;
		}
		
		return super.act();
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}

	@Override
	public boolean canAttack(Char enemy) {
		if (buff(ChampionEnemy.Paladin.class) != null){
			return false;
		}
		if (super.canAttack(enemy)){
			return true;
		} else {
			return rangedCooldown < 0 && new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos;
		}
	}
	
	protected boolean doAttack( Char enemy ) {
		
		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| rangedCooldown > 0
				|| new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos != enemy.pos) {
			
			return super.doAttack( enemy );
			
		} else {
			
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zapOrReflect();
				return true;
			}
		}
	}

	protected boolean isZapVisible() {
		return sprite != null && (sprite.visible || enemy.sprite.visible);
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		meleeProc( enemy, damage );
		
		return damage;
	}

	protected boolean isReflectable() {
		return false;
	}

	private void reflectZap() {
		Char enemy = this.enemy;
		this.enemy = this;
		zap();
		this.enemy = enemy;
	}

	protected void zapOrReflect() {
        if (isReflectable() && ShieldOfLight.DivineShield.tryUse(enemy, this, () -> {
            if (isZapVisible()) {
                ((ElementalSprite) sprite).doZap(enemy.sprite, pos, this::reflectZap);
            } else {
                reflectZap();
            }
        })) {
			Invisibility.dispel(this);
			return;
		}
        zap();
    }

	protected void zap() {
		spend( 1f );

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {
			if (enemy.buff(WarriorParry.BlockTrock.class) != null){
				enemy.sprite.emitter().burst( Speck.factory( Speck.FORGE ), 15 );
				SpellSprite.show(enemy, SpellSprite.BLOCK, 2f, 2f, 2f);
				enemy.buff(WarriorParry.BlockTrock.class).triggered = true;
			} else {
				ChampionEnemy.AntiMagic.effect(enemy, this);
				rangedProc(enemy);
			}
			
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}

		rangedCooldown = Random.NormalIntRange( 3, 5 );
	}
	
	public void onZapComplete() {
		zapOrReflect();
		next();
	}
	
	@Override
	public boolean add( Buff buff ) {
		if (harmfulBuffs.contains( buff.getClass() )) {
			damage( Random.NormalIntRange( HT/2, HT * 3/5 ), buff );
			return false;
		} else {
			return super.add( buff );
		}
	}
	
	protected abstract void meleeProc( Char enemy, int damage );
	protected abstract void rangedProc( Char enemy );
	
	protected ArrayList<Class<? extends Buff>> harmfulBuffs = new ArrayList<>();
	
	private static final String COOLDOWN = "cooldown";
	private static final String SUMMONED_ALLY = "summoned_ally";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( COOLDOWN, rangedCooldown );
		bundle.put( SUMMONED_ALLY, summonedALly);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( COOLDOWN )){
			rangedCooldown = bundle.getInt( COOLDOWN );
		}
		summonedALly = bundle.getBoolean( SUMMONED_ALLY );
		if (summonedALly){
			setSummonedALly();
		}
	}
	
	public static class FireElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Fire.class;
			
			loot = PotionOfLiquidFlame.class;
			lootChance = 1/8f;
			
			properties.add( Property.FIERY );
			
			harmfulBuffs.add( com.zrp200.rkpd2.actors.buffs.Frost.class );
			harmfulBuffs.add( Chill.class );
		}
		
		@Override
		protected void meleeProc( Char enemy, int damage ) {
			if (Random.Int( 2 ) == 0 && !Dungeon.level.water[enemy.pos]) {
				Buff.affect( enemy, Burning.class ).reignite( enemy );
				if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
			}
		}
		
		@Override
		protected void rangedProc( Char enemy ) {
			if (!Dungeon.level.water[enemy.pos]) {
				Buff.affect( enemy, Burning.class ).reignite( enemy, 4f );
			}
			if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
		}
	}
	
	//used in wandmaker quest, a fire elemental with lower ACC/EVA/DMG, no on-hit fire
	// and a unique 'fireball' style ranged attack, which can be dodged
	public static class NewbornFireElemental extends FireElemental {
		
		{
			spriteClass = ElementalSprite.NewbornFire.class;

			defenseSkill = 12;

			properties.add(Property.MINIBOSS);
		}

		private int targetingPos = -1;

		@Override
		protected boolean act() {
			//fire a charged attack instead of any other action, as long as it is possible to do so
			if (targetingPos != -1 && state == HUNTING){
				//account for bolt hitting walls, in case position suddenly changed
				targetingPos = new Ballistica( pos, targetingPos, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos;
				if (sprite != null && (sprite.visible || Dungeon.level.heroFOV[targetingPos])) {
					sprite.zap( targetingPos );
					return false;
				} else {
					zap();
					return true;
				}
			} else {

				if (state != HUNTING){
					targetingPos = -1;
				}

				return super.act();
			}
		}

		@Override
		public boolean canAttack( Char enemy ) {
			if (super.canAttack(enemy)){
				return true;
			} else {
				return rangedCooldown < 0 && new Ballistica( pos, enemy.pos, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos == enemy.pos;
			}
		}

		@Override
		protected void zapOrReflect() {
			zap(); // no reflecting
		}

		protected boolean doAttack(Char enemy ) {

			if (rangedCooldown > 0) {

				return super.doAttack( enemy );

			} else if (new Ballistica( pos, enemy.pos, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos == enemy.pos) {

				//set up an attack for next turn
				ArrayList<Integer> candidates = new ArrayList<>();
				for (int i : PathFinder.NEIGHBOURS8){
					int target = enemy.pos + i;
					if (target != pos && new Ballistica(pos, target, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET).collisionPos == target){
						candidates.add(target);
					}
				}

				if (!candidates.isEmpty()){
					targetingPos = Random.element(candidates);

					for (int i : PathFinder.NEIGHBOURS9){
						if (!Dungeon.level.solid[targetingPos + i]) {
							sprite.parent.addToBack(new TargetedCell(targetingPos + i, 0xFF0000));
						}
					}

					GLog.n(Messages.get(this, "charging"));
					spend(GameMath.gate(attackDelay(), (int)Math.ceil(Dungeon.hero.cooldown()), 3*attackDelay()));
					Dungeon.hero.interrupt();
					return true;
				} else {
					rangedCooldown = 1;
					return super.doAttack(enemy);
				}


			} else {

				if (sprite != null && (sprite.visible || Dungeon.level.heroFOV[targetingPos])) {
					sprite.zap( targetingPos );
					return false;
				} else {
					zap();
					return true;
				}

			}
		}

		@Override
		protected void zap() {
			if (targetingPos != -1) {
				spend(1f);

				Invisibility.dispel(this);

				for (int i : PathFinder.NEIGHBOURS9) {
					if (!Dungeon.level.solid[targetingPos + i]) {
						CellEmitter.get(targetingPos + i).burst(ElmoParticle.FACTORY, 5);
						if (Dungeon.level.water[targetingPos + i]) {
							GameScene.add(Blob.seed(targetingPos + i, 2, Fire.class));
						} else {
							GameScene.add(Blob.seed(targetingPos + i, 8, Fire.class));
						}

						Char target = Actor.findChar(targetingPos + i);
						if (target != null && target != this) {
							Buff.affect(target, Burning.class).reignite(target);
						}
					}
				}
				Sample.INSTANCE.play(Assets.Sounds.BURNING);
			}

			targetingPos = -1;
			rangedCooldown = Random.NormalIntRange( 3, 5 );
		}

		@Override
		public int attackSkill(Char target) {
			if (!summonedALly) {
				return 15;
			} else {
				return super.attackSkill(target);
			}
		}

		@Override
		public int damageRoll() {
			if (!summonedALly) {
				return Random.NormalIntRange(10, 12);
			} else {
				return super.damageRoll();
			}
		}

		@Override
		protected void meleeProc(Char enemy, int damage) {
			//no fiery on-hit unless it is an ally summon
			if (summonedALly) {
				super.meleeProc(enemy, damage);
			}
		}

		@Override
		public void die(Object cause) {
			super.die(cause);
			if (alignment == Alignment.ENEMY) {
				Dungeon.level.drop( new Embers(), pos ).sprite.drop();
				Statistics.questScores[1] = 2000;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						Music.INSTANCE.fadeOut(1f, new Callback() {
							@Override
							public void call() {
								if (Dungeon.level != null) {
									Dungeon.level.playLevelMusic();
								}
							}
						});
					}
				});
			}
		}

		@Override
		public boolean reset() {
			return !summonedALly;
		}

		@Override
		public String description() {
			String desc = super.description();

			if (summonedALly){
				desc += " " + Messages.get(this, "desc_ally");
			} else {
				desc += " " + Messages.get(this, "desc_boss");
			}

			return desc;
		}

		private static final String TARGETING_POS = "targeting_pos";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TARGETING_POS, targetingPos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			targetingPos = bundle.getInt(TARGETING_POS);
		}
	}

	//not a miniboss, no ranged attack, otherwise a newborn elemental
	public static class AllyNewBornElemental extends NewbornFireElemental {

		{
			rangedCooldown = Integer.MAX_VALUE;

			properties.remove(Property.MINIBOSS);
		}

	}
	
	public static class FrostElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Frost.class;
			
			loot = PotionOfFrost.class;
			lootChance = 1/8f;
			
			properties.add( Property.ICY );
			
			harmfulBuffs.add( Burning.class );
		}
		
		@Override
		protected void meleeProc( Char enemy, int damage ) {
			if (Random.Int( 3 ) == 0 || Dungeon.level.water[enemy.pos]) {
				Freezing.freeze( enemy.pos );
				if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
			}
		}
		
		@Override
		protected void rangedProc( Char enemy ) {
			Freezing.freeze( enemy.pos );
			if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
		}
	}
	
	public static class ShockElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Shock.class;
			
			loot = ScrollOfRecharging.class;
			lootChance = 1/4f;
			
			properties.add( Property.ELECTRIC );
		}
		
		@Override
		protected void meleeProc( Char enemy, int damage ) {
			ArrayList<Char> affected = new ArrayList<>();
			ArrayList<Lightning.Arc> arcs = new ArrayList<>();
			Shocking.arc( this, enemy, 2, affected, arcs );
			
			if (!Dungeon.level.water[enemy.pos]) {
				affected.remove( enemy );
			}
			
			for (Char ch : affected) {
				ch.damage( Math.round( damage * 0.4f ), new Shocking() );
				if (ch == Dungeon.hero && !ch.isAlive()){
					Dungeon.fail(this);
					GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
				}
			}

			boolean visible = sprite.visible || enemy.sprite.visible;
			for (Char ch : affected){
				if (ch.sprite.visible) visible = true;
			}

			if (visible) {
				sprite.parent.addToFront(new Lightning(arcs, null));
				Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
			}
		}
		
		@Override
		protected void rangedProc( Char enemy ) {
			Buff.affect( enemy, Blindness.class, Blindness.DURATION/2f );
			if (enemy == Dungeon.hero) {
				GameScene.flash(0x80FFFFFF);
			}
		}
	}
	
	public static class ChaosElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Chaos.class;
			
			loot = ScrollOfTransmutation.class;
			lootChance = 1f;
		}
		
		@Override
		protected void meleeProc( Char enemy, int damage ) {
			Ballistica aim = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);
			//TODO shortcutting the fx seems fine for now but may cause problems with new cursed effects
			//of course, not shortcutting it means actor ordering issues =S
			CursedWand.randomValidEffect(null, this, aim, false).effect(null, this, aim, false);
		}

		@Override
		protected void zap() {
			spend( 1f );

			Invisibility.dispel(this);
			Char enemy = this.enemy;
			//skips accuracy check, always hits
			rangedProc( enemy );

			rangedCooldown = Random.NormalIntRange( 3, 5 );
		}

		@Override
		public void onZapComplete() {
			zap();
			//next(); triggers after wand effect
		}

		@Override
		protected void rangedProc( Char enemy ) {
			CursedWand.cursedZap(null, this, new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET), new Callback() {
				@Override
				public void call() {
					next();
				}
			});
		}
	}
	
	public static Class<? extends Elemental> random(){
		float altChance = 1/50f * RatSkull.exoticChanceMultiplier();
		if (Random.Float() < altChance){
			return ChaosElemental.class;
		}
		
		float roll = Random.Float();
		if (roll < 0.4f){
			return FireElemental.class;
		} else if (roll < 0.8f){
			return FrostElemental.class;
		} else {
			return ShockElemental.class;
		}
	}
}
