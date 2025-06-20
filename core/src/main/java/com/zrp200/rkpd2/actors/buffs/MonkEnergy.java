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
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.SmokeScreen;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.Ghoul;
import com.zrp200.rkpd2.actors.mobs.RipperDemon;
import com.zrp200.rkpd2.actors.mobs.Wraith;
import com.zrp200.rkpd2.actors.mobs.YogDzewa;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.rings.RingOfForce;
import com.zrp200.rkpd2.items.spells.AquaBlast;
import com.zrp200.rkpd2.items.stones.StoneOfClairvoyance;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.Door;
import com.zrp200.rkpd2.levels.traps.GeyserTrap;
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
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndMonkAbilities;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;

import java.util.HashMap;

import static com.zrp200.rkpd2.Dungeon.hero;

public class MonkEnergy extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public float energy;
	public int cooldown;
	private float incHeal = 1, incShield = 1;

	private static final float MAX_COOLDOWN = 5;

	@Override
	public int icon() {
		return BuffIndicator.MONK_ENERGY;
	}

	@Override
	public void tintIcon(Image icon) {
		if (cooldown > 0){
			icon.hardlight(0.33f, 0.33f, 1f);
		} else {
			icon.resetColor();
		}
		if (MonkEnergy.isFeelingEmpowered(Dungeon.level.feeling)){
			if (Dungeon.level.feeling != Level.Feeling.NONE){
				int color = 0x000000;
				switch (Dungeon.level.feeling){
					case DARK: color = 0xFF5500; break;
					case GRASS: color = 0x00E500; break;
					case LARGE: color = 0xFFC61A; break;
					case CHASM: color = 0xAB00FF; break;
					case WATER: color = 0xB2E9FF; break;
					case SECRETS: color = 0x00A0FF; break;
					case TRAPS: color = 0xFF2A00; break;
				}
				icon.tint(color, 0.5f);
			}
		}
	}

	@Override
	public float iconFadePercent() {
		return GameMath.gate(0, cooldown/MAX_COOLDOWN, 1);
	}

	@Override
	public String iconTextDisplay() {
		if (cooldown > 0){
			return Integer.toString(cooldown);
		} else {
			return "";
		}
	}

	@Override
	public boolean act() {
		if (cooldown > 0){
			cooldown--;
			ActionIndicator.setAction(this);
			BuffIndicator.refreshHero();
		}

		if (MonkEnergy.isFeelingEmpowered(Level.Feeling.WATER) && Dungeon.level.water[target.pos]){
			Barrier barrier = Buff.affect(target, Barrier.class);
			if (barrier.shielding() < target.HT / 10) {
				incShield += 1/2f;
			}
			if (incShield >= 1) {
				incShield = 0;
				barrier.incShield(1);
			} else {
				barrier.incShield(0); //resets barrier decay
			}
		}

		spend(TICK);
		return true;
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc", (int)energy, energyCap());
		if (cooldown > 0){
			desc += "\n\n" + Messages.get(this, "desc_cooldown", cooldown);
		}
		if (hero.hasTalent(Talent.ATTUNEXPLORATION) && Dungeon.level.feeling != Level.Feeling.NONE){
			desc += "\n\n" + Messages.get(this, "desc_" + Dungeon.level.feeling.name());
		}
		return desc;
	}

	public static String ENERGY = "energy";
	public static String COOLDOWN = "cooldown";
	//for water feeling
	private static final String
			BARRIER_INC = "barrier_inc",
			HEAL_INC = "incHeal";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ENERGY, energy);
		bundle.put(COOLDOWN, cooldown);
		bundle.put( BARRIER_INC, incShield );
		bundle.put( HEAL_INC, incHeal );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		energy = bundle.getFloat(ENERGY);
		cooldown = bundle.getInt(COOLDOWN);
		incShield = bundle.getFloat( BARRIER_INC );
		incHeal = bundle.getFloat( HEAL_INC );

		ActionIndicator.setAction(this);
	}

	@Override
	public boolean usable() {
		return energy > 0 && cooldown < 1;
	}

	public void gainEnergy(Char enemy ){
		if (target == null) return;

		if (!Regeneration.regenOn()){
			return; //to prevent farming boss minions
		}

		float energyGain;

		//bosses and minibosses give extra energy, certain enemies give half, otherwise give 1
		if (Char.hasProp(enemy, Char.Property.BOSS))            energyGain = 5;
		else if (Char.hasProp(enemy, Char.Property.MINIBOSS))   energyGain = 3;
		else if (enemy instanceof Ghoul)                        energyGain = 0.5f;
		else if (enemy instanceof RipperDemon)                  energyGain = 0.5f;
		else if (enemy instanceof YogDzewa.Larva)               energyGain = 0.5f;
		else if (enemy instanceof Wraith)                       energyGain = 0.5f;
		else if (enemy instanceof Hero)							energyGain = 2.5f;
		else                                                    energyGain = 1;

		float enGainMulti = 1f;
		if (target instanceof Hero && !(enemy instanceof Hero)) {
			Hero hero = (Hero) target;
			if (hero.hasTalent(Talent.UNENCUMBERED_SPIRIT)) {
				int points = hero.pointsInTalent(Talent.UNENCUMBERED_SPIRIT);

				if (hero.belongings.armor() != null){
					if (hero.belongings.armor().tier <= 1 && points >= 3){
						enGainMulti += 1.00f;
					} else if (hero.belongings.armor().tier <= 2 && points >= 2){
						enGainMulti += 0.75f;
					} else if (hero.belongings.armor().tier <= 3 && points >= 1){
						enGainMulti += 0.50f;
					}
				}

				if (hero.belongings.weapon() instanceof MeleeWeapon
						&& (hero.buff(RingOfForce.BrawlersStance.class) == null
						|| !hero.buff(RingOfForce.BrawlersStance.class).active)){
					if (((MeleeWeapon) hero.belongings.weapon()).tier <= 1 && points >= 3){
						enGainMulti += 1.00f;
					} else if (((MeleeWeapon) hero.belongings.weapon()).tier <= 2 && points >= 2){
						enGainMulti += 0.75f;
					} else if (((MeleeWeapon) hero.belongings.weapon()).tier <= 3 && points >= 1){
						enGainMulti += 0.50f;
					}
				}

			}
		}
		energyGain *= 1+(enGainMulti-1)*2;

		energy = Math.min(energy+energyGain, energyCap());

		if (energy >= 1 && cooldown == 0){
			ActionIndicator.setAction(this);
		}
		BuffIndicator.refreshHero();
	}

	//10 at base, 20 at level 30
	public int energyCap(){
		return Math.max(20, 10 + hero.lvl);
	}

	public void abilityUsed( MonkAbility abil ){
		energy -= abil.energyCost();

		if (target instanceof Hero && ((Hero) target).hasTalent(Talent.COMBINED_ENERGY)
				&& abil.energyCost() >= 5-((Hero) target).pointsInTalent(Talent.COMBINED_ENERGY)) {
			Talent.CombinedEnergyAbilityTracker tracker = target.buff(Talent.CombinedEnergyAbilityTracker.class);
			if (tracker == null || !tracker.wepAbilUsed){
				Buff.prolong(target, Talent.CombinedEnergyAbilityTracker.class, 5f).monkAbilused = true;
			} else {
				tracker.monkAbilused = true;
				processCombinedEnergy(tracker);
			}
		}

		ActionIndicator.refresh();
		BuffIndicator.refreshHero();
	}

	public boolean abilitiesEmpowered( Hero hero ){
		//100%/80%/60% energy at +1/+2/+3
		return energy/energyCap() >= 1.2f - 0.2f*hero.pointsInTalent(Talent.MONASTIC_VIGOR);
	}

	public void processCombinedEnergy(Talent.CombinedEnergyAbilityTracker tracker){
		energy = Math.min(energy+1, energyCap());
		tracker.detach();
		if (energy >= 1){
			ActionIndicator.setAction(this);
		}
		BuffIndicator.refreshHero();
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.MONK_ABILITIES;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text( Integer.toString((int)energy) );
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		if (abilitiesEmpowered(hero)){
			return 0xAAEE22;
		} else {
			return 0xA08840;
		}
	}

	@Override
	public void doAction() {
		GameScene.show(new WndMonkAbilities(this));
	}

	public static abstract class MonkAbility {

		public static MonkAbility[] abilities = new MonkAbility[]{
				new Flurry(),
				new Focus(),
				new Dash(),
				new DragonKick(),
				new Meditate()
		};

		public String name(){
			return Messages.get(this, "name");
		}

		public String desc(){
			if (Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero)){
				return Messages.get(this, "empower_desc");
			} else {
				return Messages.get(this, "desc");
			}
		}

		public abstract int energyCost();

		public boolean usable(MonkEnergy buff){
			return buff.energy >= energyCost();
		}

		public String targetingPrompt(){
			return null; //return a string if uses targeting
		}

		public abstract void doAbility(Hero hero, Integer target );

		public static abstract class TargetedMonkAbility extends MonkAbility {

			public String isValidTarget(Char enemy) {
				if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[enemy.pos]) {
					return "ability_no_target";
				}

				UnarmedAbilityTracker tracker = Buff.affect(hero, UnarmedAbilityTracker.class);
				boolean canAttack = hero.canAttack(enemy);
				tracker.detach();
				if (!canAttack){
					return "ability_target_range";
				}
				return null;
			}
			protected abstract void doAbility(Char ch, UnarmedAbilityTracker tracker, boolean empowered);

			@Override
			public final String targetingPrompt() { return null; }

			@Override
			public final void doAbility(Hero hero, Integer target) {
				GameScene.selectCell(new CellSelector.TargetedListener() {

					@Override
					protected void action(Char enemy) {
						UnarmedAbilityTracker tracker = Buff.affect(hero, UnarmedAbilityTracker.class);
						hero.sprite.attack(enemy.pos, () -> {
							AttackIndicator.target(enemy);
							doAbility(enemy, tracker, Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero));
						});
					}

					@Override
					protected boolean isValidTarget(Char ch) {
						if (ch == null) return false;
						String failureReason = TargetedMonkAbility.this.isValidTarget(ch);
						if (failureReason == null) return true;
						reason.put(ch.pos, failureReason);
						return false;
					}

					private final HashMap<Integer, String> reason = new HashMap();

					@Override
					protected void onInvalid(int cell) {
						GLog.w(Messages.get(reason.containsKey(cell) ? reason.get(cell) : "ability_no_target", MeleeWeapon.class));
					}

					@Override
					public String prompt() { return Messages.get(MeleeWeapon.class, "prompt"); }
				});
			}
		}

		public static class UnarmedAbilityTracker extends FlavourBuff{};

		public static class FlurryEmpowerTracker extends FlavourBuff{};

		public static class FlurryCooldownTracker extends FlavourBuff{};

		public static class Flurry extends TargetedMonkAbility {

			@Override
			public int energyCost() {
				return 1;
			}

            @Override
            public String isValidTarget(Char enemy) {
                if (Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero)){
                    Buff.affect(hero, FlurryEmpowerTracker.class, 0f);
                }
                String res = super.isValidTarget(enemy);
                if (res != null) Buff.detach(hero, FlurryEmpowerTracker.class);
                return res;
            }

            @Override
			public boolean usable(MonkEnergy buff) {
				return super.usable(buff) && buff.target.buff(FlurryCooldownTracker.class) == null;
			}

			@Override
			public String desc() {
				if (Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero)){
					//1.5x hero unarmed damage (rounds the result)
					return Messages.get(this, "empower_desc", 2, Math.round(1.5f*(hero.STR()-8)));
				} else {
					//1.5x hero unarmed damage (rounds the result)
					return Messages.get(this, "desc", 2, Math.round(1.5f*(hero.STR()-8)));
				}

			}

			@Override
			public void doAbility(Char enemy, UnarmedAbilityTracker tracker, boolean empowered) {

				if (empowered){
					Buff.affect(hero, FlurryEmpowerTracker.class, 0f);
				}

				hero.attack(enemy, 1.5f, 0, Char.INFINITE_ACCURACY);
				if (MonkEnergy.isFeelingEmpowered(Level.Feeling.DARK)){
					hero.attack(enemy, 1, 0, Char.INFINITE_ACCURACY);
				}

				if (enemy.isAlive()){
					hero.sprite.attack(enemy.pos, () -> {
						hero.attack(enemy, 1.5f, 0, Char.INFINITE_ACCURACY);
						Invisibility.dispel();
						hero.next();
						tracker.detach();
						Buff.affect(hero, MonkEnergy.class).abilityUsed(Flurry.this);
						if (hero.buff(FlurryEmpowerTracker.class) != null) {
							hero.buff(FlurryEmpowerTracker.class).detach();
						}
						Buff.affect(hero, FlurryCooldownTracker.class, 0f);
					});
				} else {
					Invisibility.dispel();
					hero.next();
					tracker.detach();
					Buff.affect(hero, MonkEnergy.class).abilityUsed(Flurry.this);
					if (hero.buff(FlurryEmpowerTracker.class) != null) {
						hero.buff(FlurryEmpowerTracker.class).detach();
					}
					Buff.affect(hero, FlurryCooldownTracker.class, 0f);
				}
			}
		}

		public static class Focus extends MonkAbility {

			@Override
			public int energyCost() {
				return 2;
			}

			@Override
			public boolean usable(MonkEnergy buff) {
				return super.usable(buff) && buff.target.buff(FocusBuff.class) == null;
			}

			@Override
			public void doAbility(Hero hero, Integer target) {
				Buff.affect(hero, FocusBuff.class);

				if (Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero)){
					hero.next();
				} else {
					hero.spendAndNext(1f);
				}
				Buff.affect(hero, MonkEnergy.class).abilityUsed(this);
			}

			public static class FocusBuff extends Buff {

				{
					type = buffType.POSITIVE;
					announced = true;
				}

				@Override
				public int icon() {
					return BuffIndicator.MIND_VISION;
				}

				@Override
				public void tintIcon(Image icon) {
					icon.hardlight(0.25f, 1.5f, 1f);
				}

			}

		}

		public static class Dash extends MonkAbility {

			@Override
			public int energyCost() {
				return 3;
			}

			@Override
			public String targetingPrompt() {
				return Messages.get(this, "prompt");
			}

			@Override
			public void doAbility(Hero hero, Integer target) {
				if (target == null || target == -1){
					return;
				}

				int range = 4;
				if (Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero)){
					range += 4;
				}

				if (hero.rooted){
					PixelScene.shake( 1, 1f );
					GLog.w(Messages.get(MeleeWeapon.class, "ability_target_range"));
					return;
				}

				if (Dungeon.level.distance(hero.pos, target) > range){
					GLog.w(Messages.get(MeleeWeapon.class, "ability_target_range"));
					return;
				}

				if (Actor.findChar(target) != null){
					GLog.w(Messages.get(MeleeWeapon.class, "ability_occupied"));
					return;
				}

				Ballistica dash = new Ballistica(hero.pos, target, Ballistica.PROJECTILE);

				if (!dash.collisionPos.equals(target)
						|| (Dungeon.level.solid[target] && !Dungeon.level.passable[target])){
					GLog.w(Messages.get(MeleeWeapon.class, "ability_target_range"));
					return;
				}

				hero.busy();
				Sample.INSTANCE.play(Assets.Sounds.MISS);
				hero.sprite.emitter().start(Speck.factory(Speck.JET), 0.01f, Math.round(4 + 2*Dungeon.level.trueDistance(hero.pos, target)));
				hero.sprite.jump(hero.pos, target, 0, 0.1f, new Callback() {
					@Override
					public void call() {
						if (Dungeon.level.map[hero.pos] == Terrain.OPEN_DOOR) {
							Door.leave( hero.pos );
						}
						hero.pos = target;
						Dungeon.level.occupyCell(hero);
						hero.next();
						if (MonkEnergy.isFeelingEmpowered(Level.Feeling.CHASM)){
							for (int cell : dash.path){
								GameScene.add( Blob.seed( cell, 90, SmokeScreen.class ) );
							}
						}
					}
				});

				Buff.affect(hero, MonkEnergy.class).abilityUsed(this);
			}
		}

		public static class DragonKick extends TargetedMonkAbility {

			public int min(){
				return 3 * (MonkEnergy.isFeelingEmpowered(Level.Feeling.LARGE) ? 2 : 1);
			}

			public int max(){
				return 3*(hero.STR()-8) * (MonkEnergy.isFeelingEmpowered(Level.Feeling.LARGE) ? 2 : 1);
			}

			@Override
			public int energyCost() {
				if (MonkEnergy.isFeelingEmpowered(Level.Feeling.LARGE))
					return 6;
				return 4;
			}

			@Override
			public String desc() {
				if (Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero)){
					//9x hero unarmed damage
					return Messages.get(this, "empower_desc", 9, 9*(hero.STR()-8));
				} else {
					//6x hero unarmed damage
					return Messages.get(this, "desc", 6, 6*(hero.STR()-8));
				}
			}

			@Override
			public void doAbility(Char enemy, UnarmedAbilityTracker tracker, boolean empowered) {

				int oldPos = enemy.pos;
				if (hero.attack(enemy, empowered ? 9f : 6f, 0, Char.INFINITE_ACCURACY)){
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				}

				int knockPower = Math.round(6 * (MonkEnergy.isFeelingEmpowered(Level.Feeling.LARGE) ? 1.5f : 1f));

				if (oldPos == enemy.pos){
					//trace a ballistica to our target (which will also extend past them
					Ballistica trajectory = new Ballistica(hero.pos, enemy.pos, Ballistica.STOP_TARGET);
					//trim it to just be the part that goes past them
					trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
					//knock them back along that ballistica
					WandOfBlastWave.throwChar(enemy, trajectory, knockPower, true, false, hero);

					if (trajectory.dist > 0 && enemy.isActive()) {
						Buff.affect(enemy, Paralysis.class, Math.min( knockPower, trajectory.dist));
					}
				}
				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
				tracker.detach();
				Buff.affect(hero, MonkEnergy.class).abilityUsed(DragonKick.this);

				if (empowered){
					for (Char ch : Actor.chars()){
						if (ch != enemy
								&& ch.alignment == Char.Alignment.ENEMY
								&& Dungeon.level.adjacent(ch.pos, hero.pos)){
							//trace a ballistica to our target (which will also extend past them
							Ballistica trajectory = new Ballistica(hero.pos, ch.pos, Ballistica.STOP_TARGET);
							//trim it to just be the part that goes past them
							trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
							//knock them back along that ballistica
							WandOfBlastWave.throwChar(ch, trajectory, knockPower, true, false, hero);

							if (trajectory.dist > 0 && enemy.isActive()) {
								Buff.affect(ch, Paralysis.class, Math.min( knockPower, trajectory.dist));
							}
						}
					}
				}
			}
		}

		public static class Meditate extends MonkAbility {

			@Override
			public int energyCost() {
				return 5;
			}

			@Override
			public void doAbility(Hero hero, Integer target) {

				hero.sprite.operate(hero.pos);
				GameScene.flash(0x88000000, false);
				Sample.INSTANCE.play(Assets.Sounds.SCAN);

				for (Buff b : hero.buffs()){
					if (b.type == Buff.buffType.NEGATIVE
							&& !(b instanceof AllyBuff)
							&& !(b instanceof LostInventory)){
						b.detach();
					}
				}

				//we process this as 5x wait actions instead of one 5 tick action to prevent
				// effects like time freeze from eating the whole action duration
				int meditateTime = 5;
				if (MonkEnergy.isFeelingEmpowered(Level.Feeling.SECRETS))
					meditateTime = 1;
				for (int i = 0; i < meditateTime; i++) hero.spendConstant(Actor.TICK);

				if (Buff.affect(hero, MonkEnergy.class).abilitiesEmpowered(hero) && !MonkEnergy.isFeelingEmpowered(Level.Feeling.SECRETS)){
					int toHeal = Math.round((hero.HT - hero.HP)/5f);
					if (toHeal > 0) {
						Buff.affect(hero, Healing.class).setHeal(toHeal, 0, 1);
					}
					Buff.affect(hero, MeditateResistance.class, hero.cooldown());
				}

				Actor.addDelayed(new Actor() {

					{
						actPriority = VFX_PRIO;
					}

					@Override
					protected boolean act() {
						if (MonkEnergy.isFeelingEmpowered(Level.Feeling.SECRETS)){
							Buff.affect(hero, MagicalSight.class, 2f);
							StoneOfClairvoyance.doClairvoyanceEffect(hero.pos, 12);
						} else {
							Buff.affect(hero, Recharging.class, 8f);
							Buff.affect(hero, ArtifactRecharge.class).extend(8f).ignoreHornOfPlenty = false;
						}
						Actor.remove(this);
						if (MonkEnergy.isFeelingEmpowered(Level.Feeling.WATER)){
							GeyserTrap geyser = new GeyserTrap();
							geyser.pos = hero.pos;
							geyser.source = new AquaBlast();
							geyser.activate();
						}
						return true;
					}
				}, hero.cooldown()-1);

				hero.next();
				hero.busy();
				Buff.affect(hero, MonkEnergy.class).abilityUsed(this);
			}

			public static class MeditateResistance extends FlavourBuff{
				{
					actPriority = HERO_PRIO+1; //ends just before the hero acts
				}
			};
		}

	}

	public static boolean isFeelingEmpowered(Level.Feeling feeling){
		if (!hero.hasTalent(Talent.ATTUNEXPLORATION))
			return false;

		return Dungeon.level != null && Dungeon.level.feeling == feeling;
	}
}
