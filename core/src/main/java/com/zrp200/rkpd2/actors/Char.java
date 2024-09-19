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

package com.zrp200.rkpd2.actors;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.blobs.StormCloud;
import com.zrp200.rkpd2.actors.blobs.ToxicGas;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Amok;
import com.zrp200.rkpd2.actors.buffs.ArcaneArmor;
import com.zrp200.rkpd2.actors.buffs.ArtifactRecharge;
import com.zrp200.rkpd2.actors.buffs.AscensionChallenge;
import com.zrp200.rkpd2.actors.buffs.Barkskin;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Berserk;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Bless;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Charm;
import com.zrp200.rkpd2.actors.buffs.Chill;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.Corrosion;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.DamageOverTimeEffect;
import com.zrp200.rkpd2.actors.buffs.Daze;
import com.zrp200.rkpd2.actors.buffs.Doom;
import com.zrp200.rkpd2.actors.buffs.Dread;
import com.zrp200.rkpd2.actors.buffs.FireImbue;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.actors.buffs.FrostBurn;
import com.zrp200.rkpd2.actors.buffs.FrostImbue;
import com.zrp200.rkpd2.actors.buffs.Fury;
import com.zrp200.rkpd2.actors.buffs.Haste;
import com.zrp200.rkpd2.actors.buffs.Hex;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.LifeLink;
import com.zrp200.rkpd2.actors.buffs.LostInventory;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.MagicalSleep;
import com.zrp200.rkpd2.actors.buffs.Momentum;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.buffs.NoDeath;
import com.zrp200.rkpd2.actors.buffs.Ooze;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Petrified;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.actors.buffs.Preparation;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.buffs.ShieldBuff;
import com.zrp200.rkpd2.actors.buffs.Shrink;
import com.zrp200.rkpd2.actors.buffs.Slow;
import com.zrp200.rkpd2.actors.buffs.SnipersMark;
import com.zrp200.rkpd2.actors.buffs.SoulMark;
import com.zrp200.rkpd2.actors.buffs.Speed;
import com.zrp200.rkpd2.actors.buffs.Stamina;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.buffs.TimedShrink;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.WarpedEnemy;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.buffs.Weakness;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.actors.hero.abilities.duelist.Challenge;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.DeathMark;
import com.zrp200.rkpd2.actors.hero.abilities.warrior.Endure;
import com.zrp200.rkpd2.actors.mobs.CrystalSpire;
import com.zrp200.rkpd2.actors.mobs.DwarfKing;
import com.zrp200.rkpd2.actors.mobs.Elemental;
import com.zrp200.rkpd2.actors.mobs.GhostChicken;
import com.zrp200.rkpd2.actors.mobs.GnollGeomancer;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Necromancer;
import com.zrp200.rkpd2.actors.mobs.RatKingBoss;
import com.zrp200.rkpd2.actors.mobs.Tengu;
import com.zrp200.rkpd2.actors.mobs.npcs.MirrorImage;
import com.zrp200.rkpd2.actors.mobs.npcs.PrismaticImage;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.particles.FlameParticle;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.DuelistGrass;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.armor.glyphs.AntiMagic;
import com.zrp200.rkpd2.items.armor.glyphs.Potential;
import com.zrp200.rkpd2.items.armor.glyphs.Viscosity;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.food.Food;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfCleansing;
import com.zrp200.rkpd2.items.quest.NerfGun;
import com.zrp200.rkpd2.items.quest.Pickaxe;
import com.zrp200.rkpd2.items.quest.nerfEnchants.Dreamful;
import com.zrp200.rkpd2.items.rings.RingOfElements;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRetribution;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfChallenge;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfSirensSong;
import com.zrp200.rkpd2.items.stones.StoneOfAggression;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.wands.WandOfFireblast;
import com.zrp200.rkpd2.items.wands.WandOfFirebolt;
import com.zrp200.rkpd2.items.wands.WandOfFrost;
import com.zrp200.rkpd2.items.wands.WandOfLightning;
import com.zrp200.rkpd2.items.wands.WandOfLivingEarth;
import com.zrp200.rkpd2.items.weapon.Slingshot;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.enchantments.Blazing;
import com.zrp200.rkpd2.items.weapon.enchantments.Grim;
import com.zrp200.rkpd2.items.weapon.enchantments.Kinetic;
import com.zrp200.rkpd2.items.weapon.enchantments.Shocking;
import com.zrp200.rkpd2.items.weapon.melee.BloomingPick;
import com.zrp200.rkpd2.items.weapon.melee.NuclearHatchet;
import com.zrp200.rkpd2.items.weapon.melee.RoundShield;
import com.zrp200.rkpd2.items.weapon.melee.Sickle;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.darts.ShockingDart;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.Chasm;
import com.zrp200.rkpd2.levels.features.Door;
import com.zrp200.rkpd2.levels.traps.GeyserTrap;
import com.zrp200.rkpd2.levels.traps.GnollRockfallTrap;
import com.zrp200.rkpd2.levels.traps.GrimTrap;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Earthroot;
import com.zrp200.rkpd2.plants.Swiftthistle;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static com.zrp200.rkpd2.Dungeon.hero;

public abstract class Char extends Actor {
	
	public int pos = 0;
	
	public CharSprite sprite;
	
	public int HT;
	public int HP;
	
	public float baseSpeed	= 1;
	protected PathFinder.Path path;

	public int paralysed	    = 0;
	public boolean rooted		= false;
	public boolean flying		= false;
	public int invisible		= 0;

	public boolean isWet(){
		return Dungeon.level.water[pos];
	}

    //these are relative to the hero
	public enum Alignment{
		ENEMY,
		NEUTRAL,
		ALLY
	}
	public Alignment alignment;
	
	public int viewDistance	= 8;
	
	public boolean[] fieldOfView = null;
	
	private LinkedHashSet<Buff> buffs = new LinkedHashSet<>();
	
	@Override
	protected boolean act() {
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		//throw any items that are on top of an immovable char
		if (properties().contains(Property.IMMOVABLE)){
			throwItems();
		}
		if (HP == 0 && buff(DeathMark.DeathMarkTracker.class) != null){
			if (hero.hasTalent(Talent.CATACLYSMIC_ENERGY)){
				new Bomb().explode(pos);
			}
		}
		return false;
	}

	protected void throwItems(){
		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null && heap.type == Heap.Type.HEAP
				&& !(heap.peek() instanceof Tengu.BombAbility.BombItem)
				&& !(heap.peek() instanceof Tengu.ShockerAbility.ShockerItem)) {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[pos+n]){
					candidates.add(pos+n);
				}
			}
			if (!candidates.isEmpty()){
				Dungeon.level.drop( heap.pickUp(), Random.element(candidates) ).sprite.drop( pos );
			}
		}
	}

	public String name(){
		String name = Messages.get(this, "name");
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
			name = ShatteredPixelDungeon.turnIntoRrrr(name);
		}
		return name;
	}

	public boolean canInteract(Char c){
		if (Dungeon.level.adjacent( pos, c.pos )){
			return true;
		} else if (c instanceof Hero
				&& alignment == Alignment.ALLY
				&& !hasProp(this, Property.IMMOVABLE)
				&& Dungeon.level.distance(pos, c.pos) <= getMaxDistance()){
			return true;
		} else {
			return false;
		}
	}

	public int getMaxDistance() {
		if (hero.pointsInTalent(Talent.RK_PALADIN) == 3){
			return 3;
		}
		return Math.max(4* hero.pointsInTalent(Talent.ALLY_WARP), 2* hero.pointsInTalent(Talent.RK_WARLOCK));
	}

	//swaps places by default
	public boolean interact(Char c){

		//don't allow char to swap onto hazard unless they're flying
		//you can swap onto a hazard though, as you're not the one instigating the swap
		if (!Dungeon.level.passable[pos] && !c.flying){
			return true;
		}

		//can't swap into a space without room
		if (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[c.pos]
			|| c.properties().contains(Property.LARGE) && !Dungeon.level.openSpace[pos]){
			return true;
		}

		//we do a little raw position shuffling here so that the characters are never
		// on the same cell when logic such as occupyCell() is triggered
		int oldPos = pos;
		int newPos = c.pos;

		//can't swap or ally warp if either char is immovable
		if (hasProp(this, Property.IMMOVABLE) || hasProp(c, Property.IMMOVABLE)){
			return true;
		}

		//warp instantly with allies in this case
		if (c == hero && (hero.pointsInTalent(Talent.RK_PALADIN) == 3 || hero.hasTalent(Talent.ALLY_WARP,Talent.RK_WARLOCK))){
			PathFinder.buildDistanceMap(c.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
			if (PathFinder.distance[pos] == Integer.MAX_VALUE){
				return true;
			}
			pos = newPos;
			c.pos = oldPos;
			ScrollOfTeleportation.appear(this, newPos);
			ScrollOfTeleportation.appear(c, oldPos);
			Dungeon.observe();
			GameScene.updateFog();
			return true;
		}

		//can't swap places if one char has restricted movement
		if (rooted || c.rooted || buff(Vertigo.class) != null || c.buff(Vertigo.class) != null){
			return true;
		}

		c.pos = oldPos;
		moveSprite( oldPos, newPos );
		move( newPos );

		c.pos = newPos;

		c.sprite.move( newPos, oldPos );
		c.move( oldPos );
		
		c.spend( 1 / c.speed() );

		if (c == hero){
			if (hero.subClass.is(HeroSubClass.FREERUNNER)){
				Buff.affect(hero, Momentum.class).gainStack();
			}

			hero.busy();
		}

		return true;
	}
	
	protected boolean moveSprite( int from, int to ) {
		
		if (sprite.isVisible() && sprite.parent != null && (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.turnTo(from, to);
			sprite.place( to );
			return true;
		}
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(Assets.Sounds.HIT, 1, pitch);
	}

	public boolean blockSound( float pitch ) {
		return false;
	}
	
	protected static final String POS       = "pos";
	protected static final String TAG_HP    = "HP";
	protected static final String TAG_HT    = "HT";
	protected static final String TAG_SHLD  = "SHLD";
	protected static final String BUFFS	    = "buffs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		bundle.put( POS, pos );
		bundle.put( TAG_HP, HP );
		bundle.put( TAG_HT, HT );
		bundle.put( BUFFS, buffs );
	}

	public static Char restoring = null; // get a reference to the current character while restoring them.
	@Override
	public void restoreFromBundle( Bundle bundle ) {

		super.restoreFromBundle( bundle );

		restoring = this;

		pos = bundle.getInt( POS );
		HP = bundle.getInt( TAG_HP );
		HT = bundle.getInt( TAG_HT );

		for (Bundlable b : bundle.getCollection( BUFFS )) {
			if (b != null && ((Buff)b).attachAfterRestore) {
				((Buff)b).attachTo( this );
			}
		}
		restoring = null;
	}

	final public boolean attack( Char enemy ){
		return attack(enemy, 1f, 0f, 1f);
	}

	final public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
		return attack(enemy, dmgMulti, dmgBonus, accMulti, 1);
	}

	public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti, int rolls) {

		if (enemy == null) return false;
		
		boolean visibleFight = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos];

		if (enemy.isInvulnerable(getClass())) {

			if (visibleFight) {
				enemy.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "invulnerable") );

				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
			}

			if (enemy.alignment == Alignment.ALLY && hero.buff(ChampionEnemy.Paladin.class) != null && hero.hasTalent(Talent.RK_PALADIN)){
				if (Random.Int(20) < hero.pointsInTalent(Talent.RK_PALADIN)){
					Talent.onFoodEaten(hero, 300, new Food());
				}
			}

			return false;

		} else if (hit( this, enemy, accMulti, false )) {
			
			int dr = Math.round(enemy.drRoll() * AscensionChallenge.statModifier(enemy));
			if (enemy.buff(ScrollOfSirensSong.Enthralled.class) != null) dr *= 0.25f;
			if (enemy instanceof Mob) dr *= ((Mob) enemy).scaleFactor;

			if (this instanceof Hero){
				Hero h = (Hero)this;
				if ((h.belongings.attackingWeapon() instanceof MissileWeapon && !Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.HUNTRESS))
						&& h.subClass.is(HeroSubClass.SNIPER)
						&& !Dungeon.level.adjacent(h.pos, enemy.pos)){
					dr = 0;
				}

				if (h.buff(MonkEnergy.MonkAbility.UnarmedAbilityTracker.class) != null){
					dr = 0;
				} else if (h.subClass.is(HeroSubClass.MONK)) {
					//3 turns with standard attack delay
					Buff.prolong(h, MonkEnergy.MonkAbility.JustHitTracker.class, 4f);
				}

				if (hero.belongings.weapon() instanceof Slingshot.Stone){
					dr = Random.Int(0, dr);
				}
				if (hero.belongings.weapon() instanceof NerfGun.NerfAmmo){
					dr = 0;
				}
			}
			if (this instanceof RatKingBoss && ((RatKingBoss) this).phase == 4){
				dr = 0;
			}
			if (this instanceof GhostChicken){
				dr = 0;
			}

			//we use a float here briefly so that we don't have to constantly round while
			// potentially applying various multiplier effects
			float dmg = 0;
			Preparation prep = buff(Preparation.class);
			while(rolls-- > 0) {
				if (prep != null) {
					dmg = Math.max(dmg, prep.damageRoll(this));
					if (this == hero && hero.hasTalent(Talent.BOUNTY_HUNTER, Talent.RK_ASSASSIN)) {
						Buff.affect(hero, Talent.BountyHunterTracker.class, 0.0f);
					}
				} else {
					dmg = Math.max(dmg,damageRoll());
				}
			}

			dmg = Math.round(dmg*dmgMulti);

			Berserk berserk = buff(Berserk.class);
			if (berserk != null) dmg = berserk.damageFactor((int)dmg);

			BrawlerBuff brawler = buff(BrawlerBuff.class);
			if (brawler != null){
				dmg = brawler.damageFactor(dmg);
				brawler.useCharge();
				ActionIndicator.refresh();
			}

			if (buff( Fury.class ) != null) {
				dmg *= 1.75f;
			}

			for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
				dmg *= buff.meleeDamageFactor();
			}

			dmg *= AscensionChallenge.statModifier(this);

			//flat damage bonus is applied after positive multipliers, but before negative ones
			dmg += dmgBonus;

			//friendly endure
			Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
			if (endure != null) {
				dmg = endure.damageFactor(dmg);
				if (this instanceof Hero && ((Hero) this).hasTalent(Talent.DEMONSHADER)){
					enemy.damage(Random.NormalIntRange(1 + (hero.pointsInTalent(Talent.DEMONSHADER)-1)*2, 8 + (hero.pointsInTalent(Talent.DEMONSHADER)-1)*5), new Burning());
					enemy.sprite.emitter().burst(FlameParticle.FACTORY, Random.Int(2, 4 + ((Hero) this).pointsInTalent(Talent.DEMONSHADER)));
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
					if (hero.buff(Berserk.class) != null){
						hero.buff(Berserk.class).damage(Math.round(dmg / hero.buff(Berserk.class).damageMult() * (hero.pointsInTalent(Talent.DEMONSHADER)*0.25f)));
						SpellSprite.show(hero, SpellSprite.BERSERK);
					}
					if (hero.buff(Combo.class) != null){
						hero.buff(Combo.class).addTime(hero.pointsInTalent(Talent.DEMONSHADER) * 2);
					}
				}
			}

			//enemy endure
			endure = enemy.buff(Endure.EndureTracker.class);
			if (endure != null){
				dmg = endure.adjustDamageTaken(dmg);
			}

			if (enemy.buff(Shrink.class) != null || enemy.buff(TimedShrink.class) != null) dmg *= 1.4f;

			if (enemy.buff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}

			if (enemy.buff(MonkEnergy.MonkAbility.Meditate.MeditateResistance.class) != null){
				dmg *= 0.2f;
			}
			if (enemy.buff(DuelistGrass.GrassitateResistance.class) != null){
				dmg *= 0.6f;
			}

			if ( buff(Weakness.class) != null ){
				dmg *= 0.67f;
			}

			int effectiveDamage = enemy.defenseProc( this, Math.round(dmg) );
			//do not trigger on-hit logic if defenseProc returned a negative value
			if (effectiveDamage >= 0) {
				effectiveDamage = Math.max(effectiveDamage - dr, 0);

				if (enemy.buff(Viscosity.ViscosityTracker.class) != null) {
					effectiveDamage = enemy.buff(Viscosity.ViscosityTracker.class).deferDamage(effectiveDamage);
					enemy.buff(Viscosity.ViscosityTracker.class).detach();
				}//vulnerable specifically applies after armor reductions
			if ( enemy.buff( Vulnerable.class ) != null){
				effectiveDamage *= 1.33f;
			}

			effectiveDamage = attackProc( enemy, effectiveDamage );

}
			if (visibleFight) {
				if ((effectiveDamage > 0 && enemy.buff(WarriorParry.BlockTrock.class) == null) || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
					hitSound(Random.Float(0.87f, 1.15f));
				}
			}
			if (buff(BrawlerBuff.BrawlingTracker.class) != null && this instanceof Hero){
				if (hero.pointsInTalent(Talent.PRIDE_OF_STEEL) > 2) {
					Buff.affect(enemy, StoneOfAggression.Aggression.class, 4f);
				}
				effectiveDamage = ((BrawlerBuff.BrawlerWeapon)((Hero) this).belongings.weapon()).warriorAttack(effectiveDamage, enemy);
			}
			if (RobotBuff.isRobot()) {
				if (this instanceof Hero) {
					float debuffBoost = 0f;
					for (Buff buff : enemy.buffs()) {
						if (buff.type == Buff.buffType.NEGATIVE) {
							debuffBoost += RobotBuff.damageModifier();
						}
					}
					effectiveDamage *= 1f + debuffBoost;
				} else if (this instanceof Mob && hero.hasTalent(Talent.MECHANICAL_POWER)){
					effectiveDamage *= 0.75f;
				}
			}

			// If the enemy is already dead, interrupt the attack.
			// This matters as defence procs can sometimes inflict self-damage, such as armor glyphs.
			if (!enemy.isAlive()){
				return true;
			}
			if (Dungeon.isChallenged(Challenges.NO_ACCURACY)){
				effectiveDamage *= GameMath.gate(0.75f,
						acuRoll(this, attackSkill( enemy ), accMulti)/
						defRoll(this, enemy, enemy.defenseSkill(this), accMulti), 1.25f);
			}

			if (enemy.buff(WarriorParry.BlockTrock.class) != null && effectiveDamage >= 0){
				enemy.sprite.emitter().burst( Speck.factory( Speck.FORGE ), 15 );
				SpellSprite.show(enemy, SpellSprite.BLOCK, 2f, 2f, 2f);
				Buff.affect(enemy, Barrier.class).setShield(Math.round(effectiveDamage*1.25f));
				hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(Math.round(effectiveDamage*1.25f)), FloatingText.SHIELDING );
				enemy.buff(WarriorParry.BlockTrock.class).triggered = true;
			} else {
				enemy.damage(effectiveDamage, this);
			}
			if (this instanceof Hero && buff(ChampionEnemy.Giant.class) != null){
				if (Random.Int(13) < hero.pointsInTalent(Talent.RK_GIANT)){
					Buff.affect(enemy, Paralysis.class, 5);
				}
			}

			if (buff(FireImbue.class) != null)
				buff(FireImbue.class).proc(enemy);
			if (buff(FrostImbue.class) != null)
				buff(FrostImbue.class).proc(enemy);

			if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
				enemy.HP = 0;
				if (!enemy.isAlive()) {
					enemy.die(this);
				} else {
					//helps with triggering any on-damage effects that need to activate
					enemy.damage(-1, this);
					DeathMark.processFearTheReaper(enemy, true);
				}
				if (enemy.sprite != null) {
					enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
				}
			}

			Talent.CombinedLethalityTriggerTracker combinedLethality = buff(Talent.CombinedLethalityTriggerTracker.class);
			if (combinedLethality != null){
				if ( enemy.isAlive() && enemy.alignment != alignment && !Char.hasProp(enemy, Property.BOSS)
						&& !Char.hasProp(enemy, Property.MINIBOSS) && this instanceof Hero &&
						(enemy.HP/(float)enemy.HT) <= 0.4f*((Hero)this).pointsInTalent(Talent.COMBINED_LETHALITY)/3f) {
					enemy.HP = 0;
					if (!enemy.isAlive()) {
						enemy.die(this);
					} else {
						//helps with triggering any on-damage effects that need to activate
						enemy.damage(-1, this);
						DeathMark.processFearTheReaper(enemy, false);
					}
					if (enemy.sprite != null) {
						enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Talent.CombinedLethalityTriggerTracker.class, "executed"));
					}
				}
				combinedLethality.detach();
			}

			if (enemy.sprite != null) {
				enemy.sprite.bloodBurstA(sprite.center(), effectiveDamage);
				enemy.sprite.flash();
			}

			if (prep != null) prep.procKO(this, enemy);

			if (!enemy.isAlive() && visibleFight) {
				if (enemy == hero) {
					
					if (this == hero) {
						return true;
					}

					if (this instanceof WandOfLivingEarth.EarthGuardian
							|| this instanceof MirrorImage || this instanceof PrismaticImage){
						Badges.validateDeathFromFriendlyMagic();
					}
					Dungeon.fail( this );
					GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
					
				} else if (this == hero) {
					GLog.i( Messages.capitalize(Messages.get(Char.class, "defeat", enemy.name())) );
				}

				if (this instanceof Hero && ((Hero) this).hasTalent(Talent.BLOODBATH) &&
						buff(Preparation.class) != null && Random.Float() < 0.4f){
					Preparation.bloodbathProc((Hero) this, enemy);
				}
				if (this instanceof Hero && ((Hero) this).hasTalent(Talent.DARKENING_STEPS) &&
						buff(Preparation.class) != null && Random.Float() < 0.4f){
					Buff.affect(this, ArtifactRecharge.class).prolong(Dungeon.hero.pointsInTalent(Talent.DARKENING_STEPS)*2);
				}
			}
			
			return true;
			
		} else {

			enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.defenseVerb() );
			if (visibleFight) {
				//TODO enemy.defenseSound? currently miss plays for monks/crab even when the parry
				if (enemy.buff(RoundShield.Block.class)!=null) {
					enemy.buff(RoundShield.Block.class).detach();
					Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY, 1);
				} else {
					Sample.INSTANCE.play(Assets.Sounds.MISS);
				}
			}
			
			return false;
			
		}
	}

	public boolean canAttack(Char enemy) {
		if (enemy == null || pos == enemy.pos || !Actor.chars().contains(enemy)) {
			return false;
		}

		//can always attack adjacent enemies
		if (Dungeon.level.adjacent(pos, enemy.pos)) {
			return true;
		}
		return false;
	}

	public static int INFINITE_ACCURACY = 1_000_000;
	public static int INFINITE_EVASION = 1_000_000;

	public static float acuRoll(Char attacker, float acuStat, float accMulti){
		float acuRoll = Random.Float( acuStat );
		if (attacker.buff(Bless.class) != null) acuRoll *= 1.25f;
		if (attacker.buff(  Hex.class) != null) acuRoll *= 0.8f;
		if (attacker.buff(Shrink.class)!= null || attacker.buff(TimedShrink.class)!= null) acuRoll *= 0.6f;
		for (ChampionEnemy buff : attacker.buffs(ChampionEnemy.class)){
			acuRoll *= buff.evasionAndAccuracyFactor();
		}
		if (HighnessBuff.isPartying(attacker)) acuRoll *= 1.4f;
		return acuRoll*accMulti;
	}

	public static float defRoll(Char attacker, Char defender, float defStat, float accMulti) {
		float defRoll = Random.Float( defStat );
		if (defender == hero && hero.hasTalent(Talent.SCOURGING_THE_UNIVERSE) && accMulti == 2f) {
			defRoll *= 2;
		}
		else if (defender == hero && hero.pointsInTalent(Talent.SCOURGING_THE_UNIVERSE) > 1 && !Dungeon.level.adjacent(attacker.pos, defender.pos)){
			defRoll *= 1.5f;
		}
		if (defender.buff(Bless.class) != null) defRoll *= 1.25f;
		if (defender.buff(  Hex.class) != null) defRoll *= 0.8f;
		if (defender.buff(Shrink.class)!= null || defender.buff(TimedShrink.class)!= null) defRoll *= 0.8f;
		for (ChampionEnemy buff : defender.buffs(ChampionEnemy.class)){
			defRoll *= buff.evasionAndAccuracyFactor();
		}
		if (HighnessBuff.isPartying(attacker)) defRoll *= 1.4f;
		return defRoll;
	}



	public static boolean hit( Char attacker, Char defender, boolean magic ) {
		return hit(attacker, defender, magic ? 2f : 1f, magic);
	}

	public static boolean hit( Char attacker, Char defender, float accMulti, boolean magic ) {
		float acuStat = attacker.attackSkill( defender );
		float defStat = defender.defenseSkill( attacker );

		if (defender instanceof Hero && ((Hero) defender).damageInterrupt){
			((Hero) defender).interrupt();
		}

		//invisible chars always hit (for the hero this is surprise attacking)
		if (attacker.invisible > 0 && attacker.canSurpriseAttack()){
			acuStat = INFINITE_ACCURACY;
		}

		if (defender.buff(MonkEnergy.MonkAbility.Focus.FocusBuff.class) != null && !magic){
			defStat = INFINITE_EVASION;
			defender.buff(MonkEnergy.MonkAbility.Focus.FocusBuff.class).detach();
			Buff.affect(defender, MonkEnergy.MonkAbility.Focus.FocusActivation.class, 0);
		}

		//if accuracy or evasion are large enough, treat them as infinite.
		//note that infinite evasion beats infinite accuracy
		if (defStat >= INFINITE_EVASION || defender.buff(RoundShield.Block.class) != null){
			return false;
		} else if (acuStat >= INFINITE_ACCURACY){
			return true;
		}

		float acuRoll = Random.Float( acuStat );
		if (attacker.buff(Bless.class) != null) acuRoll *= 1.25f;
		if (attacker.buff(  Hex.class) != null) acuRoll *= 0.8f;
		if (attacker.buff(Shrink.class)!= null || attacker.buff(TimedShrink.class)!= null) acuRoll *= 0.6f;
		if (attacker.buff(ScrollOfSirensSong.Enthralled.class) != null) acuRoll *= 1.25f;
		if (attacker.buff( Daze.class) != null) acuRoll *= 0.5f;
		for (ChampionEnemy buff : attacker.buffs(ChampionEnemy.class)){
			acuRoll *= buff.evasionAndAccuracyFactor();
		}
acuRoll *= AscensionChallenge.statModifier(attacker);

		float defRoll = Random.Float( defStat );
		if (defender == hero && hero.hasTalent(Talent.SCOURGING_THE_UNIVERSE) && accMulti == 2f) {
			defRoll *= 2;
		}
		else if (defender == hero && hero.pointsInTalent(Talent.SCOURGING_THE_UNIVERSE) > 1 && !Dungeon.level.adjacent(attacker.pos, defender.pos)){
			defRoll *= 1.5f;
		}
		if (defender.buff(Bless.class) != null) defRoll *= 1.25f;
		if (defender.buff(  Hex.class) != null) defRoll *= 0.8f;
		if (defender.buff(Shrink.class)!= null || defender.buff(TimedShrink.class)!= null) defRoll *= 0.8f;
		if (defender.buff(ScrollOfSirensSong.Enthralled.class) != null) defRoll *= 1.25f;
		if (defender.buff( Daze.class) != null) defRoll *= 0.5f;
		for (ChampionEnemy buff : defender.buffs(ChampionEnemy.class)){
			defRoll *= buff.evasionAndAccuracyFactor();
		}

		if (Dungeon.isChallenged(Challenges.NO_ACCURACY)){
			return true;
		}

		defRoll *= AscensionChallenge.statModifier(defender);

		return (acuRoll * accMulti) >= defRoll;
	}
	
	public int attackSkill( Char target ) {
		return 0;
	}
	
	public int defenseSkill( Char enemy ) {
		return 0;
	}

	public String defenseVerb() {
		if (buff(RoundShield.Block.class) != null) return Messages.get(Hero.class, "absorbed");
		return Messages.get(this, "def_verb");
	}
	
	public int drRoll() {
		int dr = 0;

		dr += Random.NormalIntRange( 0 , Barkskin.currentLevel(this) );

		if (hero.hasTalent(Talent.RK_SPLINT) && alignment == Alignment.ALLY && this != hero){
			dr += Math.round(hero.drRoll() / 3f *(hero.pointsInTalent(Talent.RK_SPLINT)));
		}

		return dr;
	}
	
	public int damageRoll() {
		return 1;
	}
	
	//TODO it would be nice to have a pre-armor and post-armor proc.
	// atm attack is always post-armor and defence is already pre-armor
	
	public int attackProc( Char enemy, int damage ) {
		if ( buff(Weakness.class) != null ){
			damage *= 0.67f;
		}
		if (Ratmogrify.drratedonEffect(this) > 3){
			SoulMark.process(enemy, 3,1, true);
		}
		if (Ratmogrify.drratedonActive(this)){
			if (Random.Int(3) < Dungeon.hero.pointsInTalent(Talent.RK_SNIPER)
					|| Dungeon.hero.hasTalent(Talent.SHARED_ENCHANTMENT) && Random.Int(4) <= Dungeon.hero.pointsInTalent(Talent.SHARED_ENCHANTMENT))
				{
					SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
					if (bow == null && hero.belongings.weapon instanceof SpiritBow){
						bow = (SpiritBow) hero.belongings.weapon;
					}
					if (bow != null && bow.enchantment != null && Dungeon.hero.buff(MagicImmune.class) == null) {
						damage = bow.enchantment.proc(bow, this, enemy, damage);
					}
				}
		}
		for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
			buff.onAttackProc( enemy );
		}
		if (buff(WarpedEnemy.class) != null){
			Buff.affect(enemy, Weakness.class, 3);
		}

		if(alignment == Alignment.ALLY && hero.hasTalent(Talent.WARLOCKS_TOUCH)) {
			// warlock+allies can soul mark by simply attacking via warlock's touch.

			float shift=.05f, scaling=.1f;
			// 15/25/35 for melee and spirit bow, 20/35/50 for thrown weapons. Not sure if this is a good gimmick or if I'm forcing a synergy here.
			if(this == hero && hero.belongings.thrownWeapon != null && !(hero.belongings.thrownWeapon instanceof SpiritBow.SpiritArrow) ) {
				// thrown weapons have a slight boost.
				scaling *= 1.5f;
			}
			SoulMark.process(enemy,
					-4, // 10 - 4 = 6 turns
					shift + scaling*hero.pointsInTalent(Talent.WARLOCKS_TOUCH),
					true, false);
		}

		return damage;
	}
	
	public int defenseProc( Char enemy, int damage ) {

		Earthroot.Armor armor = buff( Earthroot.Armor.class );
		if (armor != null) {
			damage = armor.absorb( damage );
		}

		return damage;
	}
	
	public float speed() {
		float speed = baseSpeed;
		if ( buff( Cripple.class ) != null ) speed /= 2f;
		if ( buff( Stamina.class ) != null) speed *= 1.5f;
		if ( buff( Adrenaline.class ) != null) speed *= 2f;
		if ( buff( Haste.class ) != null) speed *= 3f;
		if ( buff( Dread.class ) != null) speed *= 2f;
		if (Dungeon.isChallenged(Challenges.FORGET_PATH)) speed *= 1.2f;
		if (Ratmogrify.drratedonEffect(this) > 1) {
			speed *= 3f;
			Momentum momentum = Dungeon.hero.buff(Momentum.class);
			if (momentum != null){
				speed *= momentum.speedMultiplier();
			}
		}
		if ( buff( ChampionEnemy.Swiftness.class ) != null) speed *= 2f;
		if(buff(HighnessBuff.class) != null && buff(HighnessBuff.class).state == HighnessBuff.State.ENERGIZED){
			speed *= 1.5f;
		}

		return speed;
	}

	public float attackDelay() {
		float delay = TICK;
		if(buff(Adrenaline.class) != null) delay /= 1.5f;
		if (Ratmogrify.drratedonEffect(this) > 1) delay /= 1.33f;
		if ( buff( ChampionEnemy.Swiftness.class ) != null) delay /= 1.5f;
		return delay;
	}

	//currently only used by invisible chars, or by the hero
	public boolean canSurpriseAttack(){
		return true;
	}

	//used so that buffs(Shieldbuff.class) isn't called every time unnecessarily
	private int cachedShield = 0;
	public boolean needsShieldUpdate = true;
	
	public int shielding(){
		if (!needsShieldUpdate){
			return cachedShield;
		}
		
		cachedShield = 0;
		for (ShieldBuff s : buffs(ShieldBuff.class)){
			cachedShield += s.shielding();
		}
		needsShieldUpdate = false;
		return cachedShield;
	}

	// splitting damage into different parts.
	protected int modifyDamage(int dmg, Object src) {

		if (buff(ChampionEnemy.Giant.class) != null && this instanceof Hero){
			int points = ((Hero)this).pointsInTalent(Talent.RK_GIANT);
			if (points > 0){
				Buff.affect(this, Kinetic.ConservedDamage.class).setBonus((int) (0.25f*points*dmg));
			}
		}
		if (buff(ChampionEnemy.Paladin.invulnerability.class) != null){
			dmg /= 4;
		}

		if (!(src instanceof Hunger) && !(src instanceof Viscosity.DeferedDamage)) {
			for (ChampionEnemy buff : buffs(ChampionEnemy.class)) {
				dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
			}
		}

		if (!(src instanceof LifeLink) && buff(LifeLink.class) != null) {
			HashSet<LifeLink> links = buffs(LifeLink.class);
			for (LifeLink link : links.toArray(new LifeLink[0])) {
				if (Actor.findById(link.object) == null) {
					links.remove(link);
					link.detach();
				}
			}
			int linkedDmg = (int) Math.ceil(dmg / (float) (links.size() + 1));
			for (LifeLink link : links) {
				Char ch = (Char) Actor.findById(link.object);
				if (ch == null) continue;
				// this reduces the effectiveness of life link for redirecting huge hits.
				int recieved = Math.min(ch.HP, linkedDmg);
				dmg -= recieved;
				ch.damage(recieved, link);
				if (!ch.isAlive()) {
					link.detach();
				}
			}
		}
		if (!(src instanceof DwarfKing.KingDamager)) {
			if (this.buff(Doom.class) != null && !isImmune(Doom.class)) {
				dmg *= 1.67f;
			}
			if (buff(Petrified.class) != null) {
				dmg *= 0.5f;
			}
			if (alignment != Alignment.ALLY && this.buff(DeathMark.DeathMarkTracker.class) != null) {
				dmg *= DeathMark.damageMultiplier();
			}
			if (this.buff(WarpedEnemy.class) != null){
				dmg *= 0.75f;
			}
			if (this.buff(BloomingPick.VineCovered.class) != null){
				dmg *= 0.25f;
			}
			if (this.buff(NuclearHatchet.Exposed.class) != null && src instanceof DamageOverTimeEffect){
				dmg *= 2.5f;
			}

			Class<?> srcClass = src.getClass();
			if (isImmune(srcClass)) {
				dmg = 0;
			} else {
				dmg = Math.round(dmg * resist(srcClass));
				if (hero.pointsInTalent(Talent.LASER_PRECISION) > 1 && src instanceof Buff && !(src instanceof DwarfKing.KingDamager)) {
					dmg /= 2;
				}
			}

			//TODO improve this when I have proper damage source logic
			if (AntiMagic.RESISTS.contains(src.getClass()) && buff(ArcaneArmor.class) != null) {
				dmg -= Random.NormalIntRange(0, buff(ArcaneArmor.class).level());
				if (dmg < 0) dmg = 0;
			}
		}
		if (this instanceof Hero && Dungeon.isChallenged(Challenges.UNSTABLE_DAMAGE)){
			dmg *= Random.Float(0.5f, 2f);
		}
		return dmg;
	}
	protected void onDamage(int dmg, Object src) {
		int initialHP = HP;

		SoulMark soulMark = buff(SoulMark.class);
		if( soulMark != null && !(src instanceof Char) ) soulMark.proc(src,this,dmg);

		Terror t = buff(Terror.class);
		if (t != null){
			t.recover();
		}
		Dread d = buff(Dread.class);
		if (d != null){
			d.recover();
		}
		Charm c = buff(Charm.class);
		if (c != null){
			c.recover(src);
		}
		if (this.buff(Frost.class) != null){
			Buff.detach( this, Frost.class );
		}
		if (this.buff(MagicalSleep.class) != null){
			Buff.detach(this, MagicalSleep.class);
		}

		if (buff(Sickle.HarvestBleedTracker.class) != null){
			if (isImmune(Bleeding.class)){
				sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(Messages.get(this, "immune")));
				buff(Sickle.HarvestBleedTracker.class).detach();
				return;
			}

			Bleeding b = buff(Bleeding.class);
			if (b == null){
				b = new Bleeding();
			}
			b.announced = false;
			b.set(dmg*buff(Sickle.HarvestBleedTracker.class).bleedFactor, Sickle.HarvestBleedTracker.class);
			b.attachTo(this);
			sprite.showStatus(CharSprite.WARNING, Messages.titleCase(b.name()) + " " + (int)b.level());
			buff(Sickle.HarvestBleedTracker.class).detach();
			return;
		}

		if (buff( Paralysis.class ) != null) {
			buff( Paralysis.class ).processDamage(dmg);
		}

		int shielded = dmg;
		//FIXME: when I add proper damage properties, should add an IGNORES_SHIELDS property to use here.
		if (!(src instanceof Hunger)){
			for (ShieldBuff s : buffs(ShieldBuff.class)){
				dmg = s.absorbDamage(dmg);
				if (dmg == 0) break;
			}
		}
		shielded -= dmg;
		HP -= dmg;

		if (HP > 0 && buff(Grim.GrimTracker.class) != null){

			float finalChance = buff(Grim.GrimTracker.class).maxChance;
			finalChance *= (float)Math.pow( ((HT - HP) / (float)HT), 2);

			if (Random.Float() < finalChance) {
				int extraDmg = Math.round(HP*resist(Grim.class));
				dmg += extraDmg;
				HP -= extraDmg;

				sprite.emitter().burst( ShadowParticle.UP, 5 );
				if (!isAlive() && buff(Grim.GrimTracker.class).qualifiesForBadge){
					Badges.validateGrimWeapon();
				}
			}
		}

		if (HP < 0 && src instanceof Char && alignment == Alignment.ENEMY){
			if (((Char) src).buff(Kinetic.KineticTracker.class) != null){
				int dmgToAdd = -HP;
				dmgToAdd -= ((Char) src).buff(Kinetic.KineticTracker.class).conservedDamage;
				dmgToAdd = Math.round(dmgToAdd * Weapon.Enchantment.genericProcChanceMultiplier((Char) src));
				if (dmgToAdd > 0) {
					Buff.affect((Char) src, Kinetic.ConservedDamage.class).setBonus(dmgToAdd);
				}
				((Char) src).buff(Kinetic.KineticTracker.class).detach();
			}
		}

		if (sprite != null) {
			//defaults to normal damage icon if no other ones apply
			int                                                         icon = FloatingText.PHYS_DMG;
			if (NO_ARMOR_PHYSICAL_SOURCES.contains(src.getClass()))     icon = FloatingText.PHYS_DMG_NO_BLOCK;
			if (AntiMagic.RESISTS.contains(src.getClass()))             icon = FloatingText.MAGIC_DMG;
			if (src instanceof Pickaxe)                                 icon = FloatingText.PICK_DMG;

			//special case for sniper when using ranged attacks
			if (src == Dungeon.hero
					&& Dungeon.hero.subClass.is(HeroSubClass.SNIPER)
					&& !Dungeon.level.adjacent(Dungeon.hero.pos, pos)
					&& Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon){
				icon = FloatingText.PHYS_DMG_NO_BLOCK;
			}

			if (src instanceof Hunger)                                  icon = FloatingText.HUNGER;
			if (src instanceof Burning)                                 icon = FloatingText.BURNING;
			if (src instanceof Chill || src instanceof Frost)        icon = FloatingText.FROST;
			if (src instanceof GeyserTrap || src instanceof StormCloud) icon = FloatingText.WATER;
			if (src instanceof Burning)                                 icon = FloatingText.BURNING;
			if (src instanceof Electricity)                             icon = FloatingText.SHOCKING;
			if (src instanceof Bleeding)                                icon = FloatingText.BLEEDING;
			if (src instanceof ToxicGas)                                icon = FloatingText.TOXIC;
			if (src instanceof Corrosion)                               icon = FloatingText.CORROSION;
			if (src instanceof Poison)                                  icon = FloatingText.POISON;
			if (src instanceof Ooze)                                    icon = FloatingText.OOZE;
			if (src instanceof Viscosity.DeferedDamage)                 icon = FloatingText.DEFERRED;
			if (src instanceof Corruption)                              icon = FloatingText.CORRUPTION;
			if (src instanceof AscensionChallenge)                      icon = FloatingText.AMULET;

			sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(dmg + shielded), icon);
		}

		if (HP < 0 && buff(NoDeath.class) == null) HP = 0;

		if (!isAlive()) {
			if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.ALLIES) && Random.Int(3) == 0 && src instanceof Hero){
				if (!isImmune(ScrollOfSirensSong.Enthralled.class)){
					HP = HT;
					AllyBuff.affectAndLoot((Mob) this, hero, ScrollOfSirensSong.Enthralled.class);
				} else {
					Buff.affect( this, Charm.class, Charm.DURATION ).object = hero.id();
					die(src);
				}
				sprite.centerEmitter().burst( Speck.factory( Speck.HEART ), 10 );
			} else if (buff(WarpedEnemy.class) != null && Random.Int(3) == 0){
				ScrollOfTeleportation.teleportChar(this);
				HT /= 2;
				HP = HT;
			} else {
				die(src);
			}
		} else if (HP == 0 && buff(DeathMark.DeathMarkTracker.class) != null){
			DeathMark.processFearTheReaper(this, initialHP != 0);
		}
	}

	//these are misc. sources of physical damage which do not apply armor, they get a different icon
	private static HashSet<Class> NO_ARMOR_PHYSICAL_SOURCES = new HashSet<>();
	{
		NO_ARMOR_PHYSICAL_SOURCES.add(CrystalSpire.SpireSpike.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.Boulder.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.GnollRockFall.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollRockfallTrap.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.KingDamager.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.Summoning.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Chasm.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(WandOfBlastWave.Knockback.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Heap.class); //damage from wraiths attempting to spawn from heaps
		NO_ARMOR_PHYSICAL_SOURCES.add(Necromancer.SummoningBlockDamage.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DriedRose.GhostHero.NoRoseDamage.class);
	}


	public void damage( int dmg, Object src ) {

		if (!isAlive() || dmg < 0) {
			return;
		}

		if(isInvulnerable(src.getClass())){
			sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
			return;
		}

		onDamage(modifyDamage(dmg,src),src);
	}

	public void destroy() {
		HP = 0;
		Actor.remove( this );

		for (Char ch : Actor.chars().toArray(new Char[0])){
			if (ch.buff(Charm.class) != null && ch.buff(Charm.class).object == id()){
				ch.buff(Charm.class).detach();
			}
			if (ch.buff(Dread.class) != null && ch.buff(Dread.class).object == id()){
				ch.buff(Dread.class).detach();
			}
			if (ch.buff(Terror.class) != null && ch.buff(Terror.class).object == id()){
				ch.buff(Terror.class).detach();
			}
			// todo check if this is even correct src 965289eba61df8fa67e2d35c2b79337f0afe59f4
			if (ch.buff(Talent.FollowupStrikeTracker.class) != null
					&& ch.buff(Talent.FollowupStrikeTracker.class).object == id()){
				ch.buff(Talent.FollowupStrikeTracker.class).detach();
			}
			if (ch.buff(Talent.DeadlyFollowupTracker.class) != null
					&& ch.buff(Talent.DeadlyFollowupTracker.class).object == id()){
				ch.buff(Talent.DeadlyFollowupTracker.class).detach();
			}
		}
		// the current setup makes it impossible for anyone but Dungeon.hero to use Sniper's Mark properly.
		// If it were to be given to multiple characters it would need another refactor.
		SnipersMark.remove(this);
	}
	
	public void die( Object src ) {
		destroy();
		if (src != Chasm.class) sprite.die();
	}

	//we cache this info to prevent having to call buff(...) in isAlive.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications
	public boolean deathMarked = false;

	public boolean isAlive() {
		return HP > 0 || deathMarked;
	}

	public boolean isActive() {
		return isAlive();
	}

	@Override
	public void spendConstant(float time) {
		TimekeepersHourglass.timeFreeze freeze = buff(TimekeepersHourglass.timeFreeze.class);
		if (freeze != null) {
			freeze.processTime(time);
			return;
		}

		Swiftthistle.TimeBubble bubble = buff(Swiftthistle.TimeBubble.class);
		if (bubble != null){
			bubble.processTime(time);
			return;
		}

		super.spendConstant(time);
	}

	@Override
    public void spend(float time) {
		float timeScale = 1f;
		if (buff( Slow.class ) != null) {
			timeScale *= 0.5f;
			//slowed and chilled do not stack
		} else if (buff( Chill.class ) != null) {
			timeScale *= buff( Chill.class ).speedFactor();
		}  else if (buff(FrostBurn.class) != null){
			timeScale *= buff( FrostBurn.class ).speedFactor();
		}
		if (buff( Speed.class ) != null) {
			timeScale *= 2.0f;
		}
		if (buff(WarpedEnemy.class) != null){
			timeScale *= 1.25f;
		}
		if (HighnessBuff.isPartying(this) && hero.pointsInTalent(Talent.PARTY_FEELING) > 2) timeScale *= 1.5f;

		super.spend( time / timeScale );
	}

	public synchronized LinkedHashSet<Buff> buffs() {
		return new LinkedHashSet<>(buffs);
	}

	@SuppressWarnings("unchecked")
	//returns all buffs assignable from the given buff class if not strict, or of the class if strict.
	public synchronized <T extends Object> HashSet<T> buffs( Class<T> c, boolean strict ) {
		HashSet<T> filtered = new HashSet<>();
		for (Object b : buffs) {
			if (strict ? b.getClass() == c : c.isInstance( b )) {
				filtered.add( (T)b );
			}
		}
		return filtered;
	}

	public synchronized <T extends Buff> HashSet<T> buffs( Class<T> c ) {
		return buffs(c, false);
	}

	@SuppressWarnings("unchecked")
	public synchronized final <T extends Object> T buff(Class<T> c) {
		return buff(c, true);
	}

	@SuppressWarnings("unchecked")
	//returns an instance of the specific buff class, if it exists. Not just assignable
	public synchronized <T extends Object> T buff( Class<T> c, boolean matchClass ) {
		for (Buff b : buffs) {
			if (matchClass ? b.getClass() == c: c.isAssignableFrom(b.getClass())) {
				return (T)b;
			}
			if (c.isInterface()){
				Class[] interfaces = b.getClass().getInterfaces();
				for (Class clazz : interfaces){
					if (clazz.isAssignableFrom(c)){
						return (T)b;
					}
				}
			}
		}
		return null;
	}

	public synchronized boolean isCharmedBy( Char ch ) {
		int chID = ch.id();
		for (Buff b : buffs) {
			if (b instanceof Charm && ((Charm)b).object == chID) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean add( Buff buff ) {

		if (buff(PotionOfCleansing.Cleanse.class) != null) { //cleansing buff
			if (buff.type == Buff.buffType.NEGATIVE
					&& !(buff instanceof AllyBuff)
					&& !(buff instanceof LostInventory)){
				return false;
			}
		}

		if (sprite != null && buff(Challenge.SpectatorFreeze.class) != null){
			return false; //can't add buffs while frozen and game is loaded
		}

		buffs.add( buff );
		if (Actor.chars().contains(this)) Actor.add( buff );

		if (sprite != null && buff.announced) {
			switch (buff.type) {
				case POSITIVE:
					sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(buff.name()));
					break;
				case NEGATIVE:
					sprite.showStatus(CharSprite.WARNING, Messages.titleCase(buff.name()));
					break;
				case NEUTRAL:
				default:
					sprite.showStatus(CharSprite.NEUTRAL, Messages.titleCase(buff.name()));
					break;
			}
		}

		return true;

	}
	
	public synchronized boolean remove( Buff buff ) {
		
		buffs.remove( buff );
		Actor.remove( buff );

		return true;
	}
	
	public synchronized void remove( Class<? extends Buff> buffClass ) {
		for (Buff buff : buffs( buffClass )) {
			remove( buff );
		}
	}
	
	@Override
	protected synchronized void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
			buff.detach();
		}
	}

	public boolean cellIsPathable( int cell ){
		if (!Dungeon.level.passable[cell]){
			if (flying || buff(Amok.class) != null){
				if (!Dungeon.level.avoid[cell]){
					return false;
				}
			} else {
				return false;
			}
		}
		if (Char.hasProp(this, Property.LARGE) && !Dungeon.level.openSpace[cell]){
			return false;
		}
		if (Actor.findChar(cell) != null){
			return false;
		}

		return true;
	}

	public synchronized void updateSpriteState() {
		for (Buff buff:buffs) {
			buff.fx( true );
		}
	}
	
	public float stealth() {
		return 0;
	}

	public void move( int step ) {
		move( step, true );
	}

	//travelling may be false when a character is moving instantaneously, such as via teleportation
	public void move( int step, boolean travelling ) {

		if (travelling && Dungeon.level.adjacent( step, pos ) && buff( Vertigo.class ) != null) {
			sprite.interruptMotion();
			int newPos = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			if (!(Dungeon.level.passable[newPos] || Dungeon.level.avoid[newPos])
					|| (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[newPos])
					|| Actor.findChar( newPos ) != null)
				return;
			else {
				sprite.move(pos, newPos);
				step = newPos;
			}
		}

		if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
			Door.leave( pos );
		}

		pos = step;
		
		if (this != hero) {
			sprite.visible = Dungeon.level.heroFOV[pos];
		}
		
		Dungeon.level.occupyCell(this );
	}
	
	public int distance( Char other ) {
		return Dungeon.level.distance( pos, other.pos );
	}

	public boolean[] modifyPassable( boolean[] passable){
		//do nothing by default, but some chars can pass over terrain that others can't
		return passable;
	}

	public void onMotionComplete() {
		//Does nothing by default
		//The main actor thread already accounts for motion,
		// so calling next() here isn't necessary (see Actor.process)
	}
	
	public void onAttackComplete() {
		next();
	}
	
	public void onOperateComplete() {
		next();
	}
	
	protected final HashSet<Class> resistances = new HashSet<>();

	public float resistanceValue(Class effect){
		if (Dungeon.isChallenged(Challenges.BURN)){
			return 0.33f;
		}
		return 0.5f;
	}

	//returns percent effectiveness after resistances
	//TODO currently resistances reduce effectiveness by a static 50%, and do not stack.
	public float resist( Class effect ){
		HashSet<Class> resists = new HashSet<>(resistances);
		for (Property p : properties()){
			resists.addAll(p.resistances());
		}
		for (Buff b : buffs()){
			resists.addAll(b.resistances());
		}
		
		float result = 1f;
		for (Class c : resists){
			if (c.isAssignableFrom(effect)){
				result *= resistanceValue(effect);
			}
		}
		return result * RingOfElements.resist(this, effect);
	}
	
	protected final HashSet<Class> immunities = new HashSet<>();
	
	public boolean isImmune(Class effect ){
		HashSet<Class> immunes = new HashSet<>(immunities);
		for (Property p : properties()){
			immunes.addAll(p.immunities());
		}
		for (Buff b : buffs()){
			immunes.addAll(b.immunities());
		}
		
		for (Class c : immunes){
			if (c.isAssignableFrom(effect)){
				return true;
			}
		}
		return false;
	}

	//similar to isImmune, but only factors in damage.
	//Is used in AI decision-making
	public boolean isInvulnerable( Class effect ){
		return buff(Challenge.SpectatorFreeze.class) != null;
	}

	protected HashSet<Property> properties = new HashSet<>();

	public HashSet<Property> properties() {
		HashSet<Property> props = new HashSet<>(properties);
		for (Buff b : buffs()){
			props.addAll(b.properties());
		}
		return props;
	}

	public enum Property{
		BOSS ( new HashSet<Class>( Arrays.asList(Grim.class, GrimTrap.class, ScrollOfRetribution.class, ScrollOfPsionicBlast.class, Dreamful.class)),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
		MINIBOSS ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
		BOSS_MINION,
		UNDEAD,
		DEMONIC,
		INORGANIC ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(Bleeding.class, ToxicGas.class, Poison.class) )),
		BLOB_IMMUNE ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(Blob.class) )),
		FIERY ( new HashSet<Class>( Arrays.asList(WandOfFireblast.class, Elemental.FireElemental.class, WandOfFirebolt.class)),
				new HashSet<Class>( Arrays.asList(Burning.class, Blazing.class))),
		ICY ( new HashSet<Class>( Arrays.asList(WandOfFrost.class, Elemental.FrostElemental.class)),
				new HashSet<Class>( Arrays.asList(Frost.class, Chill.class))),
		ACIDIC ( new HashSet<Class>( Arrays.asList(Corrosion.class)),
				new HashSet<Class>( Arrays.asList(Ooze.class))),
		ELECTRIC ( new HashSet<Class>( Arrays.asList(WandOfLightning.class, Shocking.class, Potential.class, Electricity.class, ShockingDart.class, Elemental.ShockElemental.class )),
				new HashSet<Class>()),
		LARGE,
		IMMOVABLE;
		
		private HashSet<Class> resistances;
		private HashSet<Class> immunities;
		
		Property(){
			this(new HashSet<Class>(), new HashSet<Class>());
		}
		
		Property( HashSet<Class> resistances, HashSet<Class> immunities){
			this.resistances = resistances;
			this.immunities = immunities;
		}
		
		public HashSet<Class> resistances(){
			return new HashSet<>(resistances);
		}
		
		public HashSet<Class> immunities(){
			return new HashSet<>(immunities);
		}

	}

	public static boolean hasProp( Char ch, Property p){
		return (ch != null && ch.properties().contains(p));
	}
}
