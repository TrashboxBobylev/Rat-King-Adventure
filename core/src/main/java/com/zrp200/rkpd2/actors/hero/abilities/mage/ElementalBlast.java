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

package com.zrp200.rkpd2.actors.hero.abilities.mage;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.blobs.Freezing;
import com.zrp200.rkpd2.actors.buffs.Amok;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.Charm;
import com.zrp200.rkpd2.actors.buffs.Corrosion;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Recharging;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.scrolls.ScrollOfMagicMapping;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.wands.WandOfCorrosion;
import com.zrp200.rkpd2.items.wands.WandOfCorruption;
import com.zrp200.rkpd2.items.wands.WandOfDisintegration;
import com.zrp200.rkpd2.items.wands.WandOfFireblast;
import com.zrp200.rkpd2.items.wands.WandOfFirebolt;
import com.zrp200.rkpd2.items.wands.WandOfFrost;
import com.zrp200.rkpd2.items.wands.WandOfLightning;
import com.zrp200.rkpd2.items.wands.WandOfLivingEarth;
import com.zrp200.rkpd2.items.wands.WandOfMagicMissile;
import com.zrp200.rkpd2.items.wands.WandOfPrismaticLight;
import com.zrp200.rkpd2.items.wands.WandOfRegrowth;
import com.zrp200.rkpd2.items.wands.WandOfTransfusion;
import com.zrp200.rkpd2.items.wands.WandOfUnstable;
import com.zrp200.rkpd2.items.wands.WandOfUnstable2;
import com.zrp200.rkpd2.items.wands.WandOfWarding;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.HighGrass;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.HashMap;

import static com.zrp200.rkpd2.actors.hero.abilities.rat_king.OmniAbility.markAbilityUsed;

public class ElementalBlast extends ArmorAbility {

	private static final HashMap<Class<?extends Wand>, Integer> effectTypes = new HashMap<>();
	static {
		effectTypes.put(WandOfMagicMissile.class,   MagicMissile.MAGIC_MISS_CONE);
		effectTypes.put(WandOfLightning.class,      MagicMissile.SPARK_CONE);
		effectTypes.put(WandOfDisintegration.class, MagicMissile.PURPLE_CONE);
		effectTypes.put(WandOfFireblast.class,      MagicMissile.FIRE_CONE);
		effectTypes.put(WandOfFirebolt.class,       MagicMissile.FIRE_CONE); // duplicate of fireblast
		effectTypes.put(WandOfCorrosion.class,      MagicMissile.CORROSION_CONE);
		effectTypes.put(WandOfBlastWave.class,      MagicMissile.FORCE_CONE);
		effectTypes.put(WandOfLivingEarth.class,    MagicMissile.EARTH_CONE);
		effectTypes.put(WandOfFrost.class,          MagicMissile.FROST_CONE);
		effectTypes.put(WandOfPrismaticLight.class, MagicMissile.RAINBOW_CONE);
		effectTypes.put(WandOfWarding.class,        MagicMissile.WARD_CONE);
		effectTypes.put(WandOfTransfusion.class,    MagicMissile.BLOOD_CONE);
		effectTypes.put(WandOfCorruption.class,     MagicMissile.SHADOW_CONE);
		effectTypes.put(WandOfRegrowth.class,       MagicMissile.FOLIAGE_CONE);
		effectTypes.put(WandOfUnstable.class,       MagicMissile.RANDOM_CONE);
		effectTypes.put(WandOfUnstable2.class,       MagicMissile.KROMER_CONE);
	}

	private static final HashMap<Class<?extends Wand>, Float> damageFactors = new HashMap<>();
	static {
		damageFactors.put(WandOfMagicMissile.class,     0.5f);
		damageFactors.put(WandOfLightning.class,        1f);
		damageFactors.put(WandOfDisintegration.class,   1f);
		damageFactors.put(WandOfFireblast.class,        1f);
		damageFactors.put(WandOfFirebolt.class,			1f); // duplicate of fireblast
		damageFactors.put(WandOfCorrosion.class,        0f);
		damageFactors.put(WandOfBlastWave.class,        0.67f);
		damageFactors.put(WandOfLivingEarth.class,      0.5f);
		damageFactors.put(WandOfFrost.class,            1f);
		damageFactors.put(WandOfPrismaticLight.class,   0.67f);
		damageFactors.put(WandOfWarding.class,          0f);
		damageFactors.put(WandOfTransfusion.class,      0f);
		damageFactors.put(WandOfCorruption.class,       0f);
		damageFactors.put(WandOfRegrowth.class,         0f);
		damageFactors.put(WandOfUnstable.class,         1f);
		damageFactors.put(WandOfUnstable2.class,         0f);
	}

	{
		baseChargeUse = 35f;
	}

	public static boolean activate(Hero hero, Callback next) {
		Ballistica aim;
		//The direction of the aim only matters if it goes outside the map
		//So we try to aim in the cardinal direction that has the most space
		int x = hero.pos % Dungeon.level.width();
		int y = hero.pos / Dungeon.level.width();

		if (Math.max(x, Dungeon.level.width()-x) >= Math.max(y, Dungeon.level.height()-y)){
			if (x > Dungeon.level.width()/2){
				aim = new Ballistica(hero.pos, hero.pos - 1, Ballistica.WONT_STOP);
			} else {
				aim = new Ballistica(hero.pos, hero.pos + 1, Ballistica.WONT_STOP);
			}
		} else {
			if (y > Dungeon.level.height()/2){
				aim = new Ballistica(hero.pos, hero.pos - Dungeon.level.width(), Ballistica.WONT_STOP);
			} else {
				aim = new Ballistica(hero.pos, hero.pos + Dungeon.level.width(), Ballistica.WONT_STOP);
			}
		}

		final Class<? extends Wand>[] wandCls = new Class[]{null};
		int minDamage = 0, maxDamage = 0;
		if (hero.belongings.getItem(MagesStaff.class) != null) {
			MagesStaff magesStaff = hero.belongings.getItem(MagesStaff.class);
			wandCls[0] = magesStaff.wandClass();
			minDamage = magesStaff.augment.damageFactor(magesStaff.min());
			maxDamage = magesStaff.augment.damageFactor(magesStaff.max());
		}

		if (wandCls[0] == null){
			next.call();
			return false;
		}

		int aoeSize = /*4*/(5 + hero.pointsInTalent(Talent.BLAST_RADIUS, Talent.AVALON_POWER_UP, Talent.RAT_BLAST))  *
				(hero.hasTalent(Talent.EMPOWERED_STRIKE_II) ? 2 : 1);

		int projectileProps = Ballistica.STOP_SOLID | Ballistica.STOP_TARGET;

		//### Special Projectile Properties ###
		//*** Wand of Disintegration ***
		if (wandCls[0] == WandOfDisintegration.class){
			projectileProps = Ballistica.STOP_TARGET;

		//*** Wand of Fireblast ***
		} else if (wandCls[0] == WandOfFireblast.class){
			projectileProps = projectileProps | Ballistica.IGNORE_SOFT_SOLID;

		//*** Wand of Warding ***
		} else if (wandCls[0] == WandOfWarding.class){
			projectileProps = Ballistica.STOP_TARGET;

		}

		ConeAOE aoe = new ConeAOE(aim, aoeSize, 360, projectileProps);

		for (Ballistica ray : aoe.outerRays){
			((MagicMissile)hero.sprite.parent.recycle( MagicMissile.class )).reset(
					effectTypes.get(wandCls[0]),
					hero.sprite,
					ray.path.get(ray.dist),
					null
			);
		}

		final float effectMulti = (1f + (0.25f*hero.byTalent(
				Talent.ELEMENTAL_POWER,1.5f,
				Talent.RAT_BLAST,1f)) *
				(hero.hasTalent(Talent.EMPOWERED_STRIKE_II) ? 2f : 1f));
		final int miscEffectMulti = (hero.hasTalent(Talent.EMPOWERED_STRIKE_II) ? 2 : 1);

		//cast a ray 2/3 the way, and do effects
		final Class<? extends Wand>[] finalWandCls = new Class[]{wandCls[0] == WandOfFirebolt.class ? WandOfFireblast.class : wandCls[0]};
		int finalMinDamage = minDamage;
		int finalMaxDamage = maxDamage;
		hero.sprite.parent.recycle( MagicMissile.class ).reset(
				effectTypes.get(wandCls[0]),
				hero.sprite,
				aim.path.get(Math.min(aoeSize / 2, aim.path.size()-1)),
				new Callback() {
					@Override
					public void call() {

						if (finalWandCls[0] == WandOfUnstable.class){
							finalWandCls[0] = Random.element(effectTypes.keySet());
						}

						int charsHit = 0;
						Freezing freeze = (Freezing)Dungeon.level.blobs.get( Freezing.class );
						Fire fire = (Fire)Dungeon.level.blobs.get( Fire.class );
						for (int cell : aoe.cells) {

							//### Cell effects ###
							//*** Wand of Lightning ***
							if (finalWandCls[0] == WandOfLightning.class){
								if (Dungeon.level.water[cell]){
									GameScene.add( Blob.seed( cell, 4 * miscEffectMulti, Electricity.class ) );
								}

							//*** Wand of Fireblast ***
							} else if (finalWandCls[0] == WandOfFireblast.class){
								if (Dungeon.level.map[cell] == Terrain.DOOR){
									Level.set(cell, Terrain.OPEN_DOOR);
									GameScene.updateMap(cell);
								}
								if (freeze != null){
									freeze.clear(cell);
								}
								if (Dungeon.level.flamable[cell]){
									GameScene.add( Blob.seed( cell, 4 * miscEffectMulti, Fire.class ) );
								}

							//*** Wand of Frost ***
							} else if (finalWandCls[0] == WandOfFrost.class){
								if (fire != null){
									fire.clear(cell);
								}

							//*** Wand of Prismatic Light ***
							} else if (finalWandCls[0] == WandOfPrismaticLight.class){
								for (int n : PathFinder.NEIGHBOURS9) {
									int c = cell+n;

									if (Dungeon.level.discoverable[c]) {
										Dungeon.level.mapped[c] = true;
									}

									int terr = Dungeon.level.map[c];
									if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

										Dungeon.level.discover(c);

										GameScene.discoverTile(c, terr);
										ScrollOfMagicMapping.discover(c);

									}
								}

							//*** Wand of Regrowth ***
							} else if (finalWandCls[0] == WandOfRegrowth.class){
								//TODO: spend 3 charges worth of regrowth energy from staff?
								if (Random.Float() < 0.33f*effectMulti) {
									HighGrass.plant(cell);
								}
							}

							//### Deal damage ###
							Char mob = Actor.findChar(cell);
							int damage = Math.round(Random.NormalIntRange(finalMinDamage, finalMaxDamage)
									* effectMulti
									* damageFactors.get(finalWandCls[0]));

							if (mob != null && damage > 0 && mob.alignment != Char.Alignment.ALLY){
								mob.damage(damage, Reflection.newInstance(finalWandCls[0]));
								charsHit++;
							}

							//### Other Char Effects ###
							if (mob != null && mob != hero){
								if (Dungeon.hero.pointsInTalent(Talent.FUN) > 1 && cell != Dungeon.hero.pos && mob.alignment == Char.Alignment.ENEMY){
									WandOfUnstable wand = new WandOfUnstable();
									wand.upgrade(hero.lvl/3);
									wand.fx(new Ballistica(hero.pos, cell, Ballistica.STOP_TARGET), () -> {
										wand.onZap(new Ballistica(hero.pos, cell, Ballistica.STOP_TARGET));
									});
									hero.busy();
								}
								//*** Wand of Lightning ***
								if (finalWandCls[0] == WandOfLightning.class){
									if (mob.isAlive() && mob.alignment != Char.Alignment.ALLY) {
										Buff.affect( mob, Paralysis.class, effectMulti*Paralysis.DURATION/2 );
									}

								//*** Wand of Fireblast ***
								} else if (finalWandCls[0] == WandOfFireblast.class){
									if (mob.isAlive() && mob.alignment != Char.Alignment.ALLY) {
										Buff.affect( mob, Burning.class ).reignite( mob );
									}

								//*** Wand of Corrosion ***
								} else if (finalWandCls[0] == WandOfCorrosion.class){
									if (mob.isAlive() && mob.alignment != Char.Alignment.ALLY) {
										Buff.affect( mob, Corrosion.class ).set(4, Math.round(6*effectMulti));
										charsHit++;
									}

								//*** Wand of Blast Wave ***
								} else if (finalWandCls[0] == WandOfBlastWave.class){
									if (mob.alignment != Char.Alignment.ALLY) {
										Ballistica aim = new Ballistica(hero.pos, mob.pos, Ballistica.WONT_STOP);
										int knockback = aoeSize + 1 - (int)Dungeon.level.trueDistance(hero.pos, mob.pos);
										knockback *= effectMulti;
										WandOfBlastWave.throwChar(mob,
												new Ballistica(mob.pos, aim.collisionPos, Ballistica.MAGIC_BOLT),
												knockback,
												true,
												true,
												new ElementalBlast());
									}

								//*** Wand of Frost ***
								} else if (finalWandCls[0] == WandOfFrost.class){
									if (mob.isAlive() && mob.alignment != Char.Alignment.ALLY) {
										Buff.affect( mob, Frost.class, effectMulti*Frost.DURATION );
									}

								//*** Wand of Prismatic Light ***
								} else if (finalWandCls[0] == WandOfPrismaticLight.class){
									if (mob.isAlive() && mob.alignment != Char.Alignment.ALLY) {
										Buff.prolong(mob, Blindness.class, effectMulti*Blindness.DURATION/2);
										charsHit++;
									}

								//*** Wand of Warding ***
								} else if (finalWandCls[0] == WandOfWarding.class){
									if (mob instanceof WandOfWarding.Ward){
										((WandOfWarding.Ward) mob).wandHeal(0, effectMulti);
										charsHit++;
									}

								//*** Wand of Transfusion ***
								} else if (finalWandCls[0] == WandOfTransfusion.class){
									if(mob.alignment == Char.Alignment.ALLY || mob.buff(Charm.class) != null){
										int healing = Math.round(10*effectMulti);
										int shielding = (mob.HP + healing) - mob.HT;
										if (shielding > 0){
											healing -= shielding;
											Buff.affect(mob, Barrier.class).setShield(shielding);
										}
										mob.HP += healing;

										mob.sprite.emitter().burst(Speck.factory(Speck.HEALING), 4);

										if (healing > 0) {
											mob.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healing), FloatingText.HEALING);
										}
									} else {
										if (!mob.properties().contains(Char.Property.UNDEAD)) {
											Charm charm = Buff.affect(mob, Charm.class, effectMulti*Charm.DURATION/2f);
											charm.object = hero.id();
											charm.ignoreHeroAllies = true;
											mob.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 3);
										} else {
											damage = Math.round(Random.NormalIntRange(finalMinDamage, finalMaxDamage) * effectMulti);
											mob.damage(damage, Reflection.newInstance(finalWandCls[0]));
											mob.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10);
										}
									}
									charsHit++;

								//*** Wand of Corruption ***
								} else if (finalWandCls[0] == WandOfCorruption.class){
									if (mob.isAlive() && mob.alignment != Char.Alignment.ALLY) {
										Buff.prolong(mob, Amok.class, effectMulti*5f);
										charsHit++;
									}

								//*** Wand of Regrowth ***
								} else if (finalWandCls[0] == WandOfRegrowth.class){
									if (mob.alignment != Char.Alignment.ALLY) {
										Buff.prolong( mob, Roots.class, effectMulti*Roots.DURATION );
										charsHit++;
									}
								} else if (finalWandCls[0] == WandOfUnstable2.class){
									if (mob.alignment != Char.Alignment.ALLY) {
										CursedWand.cursedEffect(null, Dungeon.hero, mob);
										charsHit++;
									}
								}
							}

						}

						//### Self-Effects ###
						//*** Wand of Magic Missile ***
						if (finalWandCls[0] == WandOfMagicMissile.class) {
							Buff.append(hero, Recharging.class, effectMulti* Recharging.DURATION / 2f);
							SpellSprite.show( hero, SpellSprite.CHARGE );

						//*** Wand of Living Earth ***
						} else if (finalWandCls[0] == WandOfLivingEarth.class && charsHit > 0){
							for (Mob m : Dungeon.level.mobs){
								if (m instanceof WandOfLivingEarth.EarthGuardian){
									((WandOfLivingEarth.EarthGuardian) m).setInfo(hero, 0, Math.round(effectMulti*charsHit*5));
									m.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + charsHit);
									break;
								}
							}

						//*** Wand of Frost ***
						} else if (finalWandCls[0] == WandOfFrost.class){
							if ((hero.buff(Burning.class)) != null) {
								hero.buff(Burning.class).detach();
							}

						//*** Wand of Prismatic Light ***
						} else if (finalWandCls[0] == WandOfPrismaticLight.class){
							Buff.prolong( hero, Light.class, effectMulti*50f);

						}

						// fixme rat blast has too many targets this way
						charsHit = Math.min(5, charsHit);
						if (charsHit > 0 && hero.hasTalent(Talent.REACTIVE_BARRIER, Talent.RAT_BLAST)){
							int shielding = charsHit*(int)hero.byTalent(Talent.REACTIVE_BARRIER, 3, Talent.RAT_BLAST, 2.5f);
							Buff.affect(hero, Barrier.class).setShield(shielding);
						}

						next.call();
					}
				}
		);
		hero.sprite.operate( hero.pos );
		hero.busy();

		return true;
	}
	@Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
		if(MagesStaff.getWandClass() == null) {
			GLog.w(Messages.get(this, "no_staff"));
			markAbilityUsed(this);
			return; // prevents the callback by catching it now.
		}

		activate(hero, () -> hero.spendAndNext(Actor.TICK) );

		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
		Invisibility.dispel();

		armor.useCharge(hero, this);
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		if (Game.scene() instanceof GameScene){
			MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
			if (staff != null && staff.wandClass() != null){
				desc += "\n\n" + Messages.get(staff.wandClass(), "eleblast_desc");
			} else {
				desc += "\n\n" + Messages.get(this, "generic_desc");
			}
		} else {
			desc += "\n\n" + Messages.get(this, "generic_desc");
		}
		desc += "\n\n" + Messages.get(this, "cost", (int)baseChargeUse);
		return desc;
	}

	@Override
	public int icon() {
		return HeroIcon.ELEMENTAL_BLAST;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BLAST_RADIUS, Talent.ELEMENTAL_POWER, Talent.REACTIVE_BARRIER, Talent.EMPOWERED_STRIKE_II, Talent.HEROIC_ENERGY};
	}
}
