/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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
import com.watabou.utils.*;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.blobs.ToxicGas;
import com.zrp200.rkpd2.actors.buffs.*;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.DeathMark;
import com.zrp200.rkpd2.actors.hero.abilities.warrior.Endure;
import com.zrp200.rkpd2.actors.mobs.*;
import com.zrp200.rkpd2.actors.mobs.npcs.MirrorImage;
import com.zrp200.rkpd2.actors.mobs.npcs.PrismaticImage;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.armor.glyphs.AntiMagic;
import com.zrp200.rkpd2.items.armor.glyphs.Potential;
import com.zrp200.rkpd2.items.armor.glyphs.Viscosity;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.food.Food;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfCleansing;
import com.zrp200.rkpd2.items.quest.NerfGun;
import com.zrp200.rkpd2.items.quest.nerfEnchants.Dreamful;
import com.zrp200.rkpd2.items.rings.RingOfElements;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRetribution;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfChallenge;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfSirensSong;
import com.zrp200.rkpd2.items.stones.StoneOfAggression;
import com.zrp200.rkpd2.items.wands.*;
import com.zrp200.rkpd2.items.weapon.Slingshot;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.enchantments.*;
import com.zrp200.rkpd2.items.weapon.melee.RoundShield;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.darts.ShockingDart;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.Chasm;
import com.zrp200.rkpd2.levels.features.Door;
import com.zrp200.rkpd2.levels.traps.GrimTrap;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.utils.BArray;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;

import java.util.Arrays;
import java.util.HashSet;

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
	
	private HashSet<Buff> buffs = new HashSet<>();
	
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
		if (heap != null && heap.type == Heap.Type.HEAP) {
			int n;
			do {
				n = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			} while (!Dungeon.level.passable[n] && !Dungeon.level.avoid[n]);
			Dungeon.level.drop( heap.pickUp(), n ).sprite.drop( pos );
		}
	}

	public String name(){
		return Messages.get(this, "name");
	}

	public boolean canInteract(Char c){
		if (Dungeon.level.adjacent( pos, c.pos )){
			return true;
		} else if (c instanceof Hero
				&& alignment == Alignment.ALLY
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

		int curPos = pos;

		//warp instantly with allies in this case
		if (c == hero && (hero.pointsInTalent(Talent.RK_PALADIN) == 3 || hero.hasTalent(Talent.ALLY_WARP,Talent.RK_WARLOCK))){
			PathFinder.buildDistanceMap(c.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
			if (PathFinder.distance[pos] == Integer.MAX_VALUE){
				return true;
			}
			ScrollOfTeleportation.appear(this, c.pos);
			ScrollOfTeleportation.appear(c, curPos);
			Dungeon.observe();
			GameScene.updateFog();
			return true;
		}

		//can't swap places if one char has restricted movement
		if (rooted || c.rooted || buff(Vertigo.class) != null || c.buff(Vertigo.class) != null){
			return true;
		}

		moveSprite( pos, c.pos );
		move( c.pos );

		c.sprite.move( c.pos, curPos );
		c.move( curPos );
		
		c.spend( 1 / c.speed() );

		if (c == hero){
			if (hero.isSubclassed(HeroSubClass.FREERUNNER)){
				Buff.affect(hero, Momentum.class).gainStack();
			}

			hero.busy();
		}

		return true;
	}
	
	protected boolean moveSprite( int from, int to ) {
		
		if (sprite.isVisible() && (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to])) {
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
				if (Random.Int(22) < hero.pointsInTalent(Talent.RK_PALADIN)){
					Talent.onFoodEaten(hero, 300, new Food());
				}
			}

			return false;

		} else if (hit( this, enemy, accMulti )) {
			
			int dr = Math.round(enemy.drRoll() * AscensionChallenge.statModifier(enemy));
			if (enemy instanceof Mob) dr *= ((Mob) enemy).scaleFactor;

			Barkskin bark = enemy.buff(Barkskin.class);
			if (bark != null)   dr += Random.NormalIntRange( 0 , bark.level() );

			Blocking.BlockBuff block = enemy.buff(Blocking.BlockBuff.class);
			if (block != null)  dr += block.blockingRoll();
			if (enemy.buff(Shrink.class) != null || enemy.buff(TimedShrink.class) != null) dr *= 0.5f;

			if (this instanceof Hero){
				Hero hero = (Hero)this;
				if (hero.belongings.weapon() instanceof MissileWeapon
						&& (hero.isSubclassed(HeroSubClass.SNIPER) || hero.isSubclassed(HeroSubClass.KING))
						&& !Dungeon.level.adjacent(hero.pos, enemy.pos)){
					dr = 0;
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
					if (this == hero && hero.hasTalent(Talent.RK_ASSASSIN)) {
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
				ActionIndicator.clearAction(brawler);
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
			if (endure != null) dmg = endure.damageFactor(dmg);

			//enemy endure
			endure = enemy.buff(Endure.EndureTracker.class);
			if (endure != null){
				dmg = endure.adjustDamageTaken(dmg);
			}

			if (enemy.buff(Shrink.class) != null || enemy.buff(TimedShrink.class) != null) dmg *= 1.4f;

			if (enemy.buff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}

			if ( buff(Weakness.class) != null ){
				dmg *= 0.67f;
			}

			int effectiveDamage = enemy.defenseProc( this, Math.round(dmg) );
			if (Ratmogrify.drratedonActive(this)){
				dr = 0;
			}
			if (enemy.buff(GodSlayerBurning.class) != null){
				dr = 0;
			}
			effectiveDamage = Math.max( effectiveDamage - dr, 0 );
			if (buff(BrawlerBuff.BrawlingTracker.class) != null && this instanceof Hero) {
				if (hero.hasTalent(Talent.PRIDE_OF_STEEL)) {
					effectiveDamage += dr;
				}
			}
			if (enemy instanceof Hero && (((Hero) enemy).hasTalent(Talent.BRAVERY))){
				Berserk b = Buff.affect(enemy, Berserk.class);
				b.damage(Math.round(
							(0.75f+((Hero) enemy).pointsInTalent(Talent.BRAVERY)/2f) *effectiveDamage));
			}



			//vulnerable specifically applies after armor reductions
			if ( enemy.buff( Vulnerable.class ) != null){
				effectiveDamage *= 1.33f;
			}
			
			effectiveDamage = attackProc( enemy, effectiveDamage );


			if (visibleFight) {
				if (effectiveDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
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

			enemy.damage( effectiveDamage, this );
			if (this instanceof Hero && buff(ChampionEnemy.Giant.class) != null){
				if (Random.Int(15) < hero.pointsInTalent(Talent.RK_GIANT)){
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
				enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
			}

			enemy.sprite.bloodBurstA( sprite.center(), effectiveDamage );
			enemy.sprite.flash();

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
					Dungeon.fail( getClass() );
					GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
					
				} else if (this == hero) {
					GLog.i( Messages.capitalize(Messages.get(Char.class, "defeat", enemy.name())) );
				}

				if (this instanceof Hero && ((Hero) this).hasTalent(Talent.ENHANCED_LETHALITY) &&
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
		return defRoll;
	}



	public static boolean hit( Char attacker, Char defender, boolean magic ) {
		return hit(attacker, defender, magic ? 2f : 1f);
	}

	public static boolean hit( Char attacker, Char defender, float accMulti ) {
		float acuStat = attacker.attackSkill( defender );
		float defStat = defender.defenseSkill( attacker );


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
		return 0;
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

		return speed;
	}

	public float attackDelay() {
		float delay = TICK;
		if(buff(Adrenaline.class) != null) delay /= 1.5f;
		if (Ratmogrify.drratedonEffect(this) > 1) delay /= 1.33f;
		return delay;
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
		dmg = (int)Math.ceil(dmg / AscensionChallenge.statModifier(this));

		if (!(src instanceof LifeLink) && buff(LifeLink.class) != null){
			HashSet<LifeLink> links = buffs(LifeLink.class);
			for (LifeLink link : links.toArray(new LifeLink[0])){
				if (Actor.findById(link.object) == null){
					links.remove(link);
					link.detach();
				}
			}
			int linkedDmg = (int)Math.ceil(dmg / (float)(links.size()+1));
			for (LifeLink link : links){
				Char ch = (Char)Actor.findById(link.object);
				// this reduces the effectiveness of life link for redirecting huge hits.
				int recieved = Math.min(ch.HP,linkedDmg);
				dmg -= recieved;
				ch.damage(recieved, link);
				if (!ch.isAlive()){
					link.detach();
				}
			}
		}
		if (!(src instanceof DwarfKing.KingDamager)) {
			if (this.buff(Doom.class) != null && !isImmune(Doom.class)) {
				dmg *= 2;
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

		if (buff( Paralysis.class ) != null) {
			buff( Paralysis.class ).processDamage(dmg);
		}

		Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
		if (endure != null){
			dmg = endure.enforceDamagetakenLimit(dmg);
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

		if (sprite != null) {
			sprite.showStatus(HP > HT / 2 ?
							CharSprite.WARNING :
							CharSprite.NEGATIVE,
					Integer.toString(dmg + shielded));
		}

		if (HP < 0 && buff(NoDeath.class) == null) HP = 0;

		if (!isAlive()) {
			if (Dungeon.specialSeed == DungeonSeed.SpecialSeed.ALLIES && Random.Int(2) == 0 && src instanceof Hero){
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
		
		super.spend( time / timeScale );
	}

	public synchronized HashSet<Buff> buffs() {
		return new HashSet<>(buffs);
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

	public synchronized void add( Buff buff ) {

		if (buff(PotionOfCleansing.Cleanse.class) != null) { //cleansing buff
			if (buff.type == Buff.buffType.NEGATIVE
					&& !(buff instanceof AllyBuff)
					&& !(buff instanceof LostInventory)){
				return;
			}
		}

		buffs.add( buff );
		if (Actor.chars().contains(this)) Actor.add( buff );

		if (sprite != null && buff.announced)
			switch(buff.type){
				case POSITIVE:
					sprite.showStatus(CharSprite.POSITIVE, buff.toString());
					break;
				case NEGATIVE:
					sprite.showStatus(CharSprite.NEGATIVE, buff.toString());
					break;
				case NEUTRAL: default:
					sprite.showStatus(CharSprite.NEUTRAL, buff.toString());
					break;
			}

	}
	
	public synchronized void remove( Buff buff ) {
		
		buffs.remove( buff );
		Actor.remove( buff );

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
		return false;
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
