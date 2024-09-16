package com.zrp200.rkpd2.actors.mobs;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blizzard;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.CorrosiveGas;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.blobs.Freezing;
import com.zrp200.rkpd2.actors.blobs.GodSlayerFire;
import com.zrp200.rkpd2.actors.blobs.Inferno;
import com.zrp200.rkpd2.actors.blobs.SmokeScreen;
import com.zrp200.rkpd2.actors.blobs.ToxicGas;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Charm;
import com.zrp200.rkpd2.actors.buffs.Chill;
import com.zrp200.rkpd2.actors.buffs.Corrosion;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.actors.buffs.FrostBurn;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.LockedFloor;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Petrified;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.actors.buffs.PowerfulDegrade;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.buffs.Sleep;
import com.zrp200.rkpd2.actors.buffs.Slow;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.WarpedEnemy;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.LegacyWrath;
import com.zrp200.rkpd2.effects.Beam;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Lightning;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.TargetedCell;
import com.zrp200.rkpd2.effects.particles.GodfireParticle;
import com.zrp200.rkpd2.effects.particles.SparkParticle;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.wands.WandOfCorrosion;
import com.zrp200.rkpd2.items.wands.WandOfFireblast;
import com.zrp200.rkpd2.items.wands.WandOfFrost;
import com.zrp200.rkpd2.items.wands.WandOfLightning;
import com.zrp200.rkpd2.items.wands.WandOfMagicMissile;
import com.zrp200.rkpd2.items.wands.WandOfPrismaticLight;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.enchantments.Grim;
import com.zrp200.rkpd2.items.weapon.enchantments.Unstable;
import com.zrp200.rkpd2.items.weapon.missiles.PhantomSpear;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.traps.CorrosionTrap;
import com.zrp200.rkpd2.levels.traps.CursingTrap;
import com.zrp200.rkpd2.levels.traps.DisarmingTrap;
import com.zrp200.rkpd2.levels.traps.DisintegrationTrap;
import com.zrp200.rkpd2.levels.traps.DistortionTrap;
import com.zrp200.rkpd2.levels.traps.FlashingTrap;
import com.zrp200.rkpd2.levels.traps.FrostTrap;
import com.zrp200.rkpd2.levels.traps.GrimTrap;
import com.zrp200.rkpd2.levels.traps.RockfallTrap;
import com.zrp200.rkpd2.levels.traps.StormTrap;
import com.zrp200.rkpd2.levels.traps.Trap;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.sprites.RatKingBossSprite;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.BossHealthBar;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.FunctionalStuff;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class RatKingBoss extends Mob {

    public int phase = -1;
    public static final int EMPEROR = 0;
    public static final int GLADIATOR = 1;
    public static final int BATTLEMAGE = 2;
    public static final int ASSASSIN = 3;
    public static final int SNIPER = 4;

    enum MagicAttack{
        MAGIC_MISSILE(0xFFFFFF, MagicMissile.MAGIC_MISSILE),
        FIREBLAST(0xff7f00, MagicMissile.FIRE_CONE),
        FROST(0x66b3ff, MagicMissile.FROST),
        CORROSION(0xae7b49, MagicMissile.CORROSION),
        POISON(0x57135d, MagicMissile.WARD),
        LIGHTNING(0x87e6e6, MagicMissile.FROGGIT),
        BLAST_WAVE(0xb3a284, MagicMissile.FORCE),
        PRISMATIC(0xcccccc, MagicMissile.RAINBOW),
        RED_FIRE(0xff0000, MagicMissile.BLOOD_CONE),
        RAT_KING(0xffc61a, MagicMissile.ELMO);

        int color;
        int boltType;

        MagicAttack(int color, int boltType){
            this.color = color;
            this.boltType = boltType;
        }
    }
    public MagicAttack attack;
    public boolean magicPrepare = false;
    public boolean phase2Notice = false;
    public boolean phase3Notice = false;
    public int[] magicCastPos = {-1, -1};
    public int phantomSpearPositions = -1;

    {
        HP = HT = 2000 + Challenges.activeChallenges()*240;
        if (rkMode()){
            HP = HT = 3200 + Challenges.activeChallenges()*360;
        }
        spriteClass = RatKingBossSprite.class;

        HUNTING = new Hunting();
        state = HUNTING;
        alignment = Alignment.ENEMY;

        viewDistance = 12;
        defenseSkill = 35;
        if (rkMode()){
            defenseSkill = 40;
        }

        properties.add(Property.BOSS);
        properties.add(Property.MINIBOSS);

        immunities.add(Frost.class);
    }
    private float summonCooldown;
    private static final int MIN_SUMMON_CD = 7;
    private static final int MAX_SUMMON_CD = 13;

    private float abilityCooldown = 0;
    private static final int MIN_ABILITY_CD = 7;
    private static final int MAX_ABILITY_CD = 12;
    private ArrayList<Integer> targetedCells = new ArrayList<>();
    private int lastHeroPos;

    public static class EmperorRat extends Ratmogrify.SummonedRat{
        {
            alignment = Alignment.ENEMY;
        }
    }

    public static class EmperorAlbinoRat extends Ratmogrify.SummonedAlbino{
        {
            alignment = Alignment.ENEMY;
        }
    }

    private ArrayList<Class> regularSummons = new ArrayList<>();
    {
        if (Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)){
            for (int i = 0; i < 6; i++){
                if (i >= 3){
                    regularSummons.add(ThreadRipper.class);
                }
                if (i >= 4){
                    regularSummons.add(EmperorAlbinoRat.class);
                }
                regularSummons.add(EmperorRat.class);
            }
        } else {
            for (int i = 0; i < 8; i++){
                if (i >= 6){
                    regularSummons.add(ThreadRipper.class);
                }
                regularSummons.add(EmperorRat.class);
            }
        }
        Random.shuffle(regularSummons);
    }

    @Override
    public boolean canAttack(Char enemy) {
        if (phase == BATTLEMAGE) return false;
        if (phase == SNIPER) return (super.canAttack(enemy) || new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
        if (phase == EMPEROR) return false;
        return super.canAttack(enemy);
    }

    @Override
    public float attackDelay() {
        float attackDelay = super.attackDelay();
        attackDelay *= Dungeon.hero.attackDelay();
        if (phase == ASSASSIN) return attackDelay *0.5f;
        return attackDelay;
    }

    @Override
    public float speed() {
        float speed = super.speed();
        speed *= Dungeon.hero.speed();
        if (phase == GLADIATOR){
            return speed * 1.25f;
        }
        if (phase == ASSASSIN){
            return speed *1.5f;
        }
        return speed;
    }

    public boolean rkMode() {return Dungeon.hero.heroClass == HeroClass.RAT_KING;}

    public boolean phase3(){
        return phase3Notice;
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (phase == GLADIATOR){
            if (isChallenged(Challenges.NO_SCROLLS) && Random.Int(2) == 0)
                Buff.prolong(enemy, PowerfulDegrade.class, 3f);
            if (isChallenged(Challenges.NO_ARMOR) && Random.Int(2) == 0) {
                Buff.prolong(enemy, Vulnerable.class, 3f);
                Buff.prolong(enemy, Charm.class, 2f).object = id();
            }
            if (isChallenged(Challenges.NO_FOOD) && Random.Int(2) == 0){
                if (enemy instanceof Hero){
                    enemy.buff(Hunger.class).affectHunger(-30);
                }
                int healAmt = Math.round(damage*0.75f);
                healAmt = Math.min( healAmt, enemy.HT - enemy.HP );

                if (healAmt > 0 && isAlive()) {
                    HP += healAmt;
                    sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 1 );
                    sprite.showStatus( CharSprite.POSITIVE, Integer.toString( healAmt ) );
                }
            }
        }
        if (phase == ASSASSIN && Dungeon.level.adjacent(pos, Dungeon.hero.pos)){
            ((Hunting)state).teleport();
        } else if (phase == ASSASSIN){
            if (isChallenged(Challenges.NO_SCROLLS) && Random.Int(2) == 0)
                Buff.prolong(enemy, PowerfulDegrade.class, 5f);
            if (isChallenged(Challenges.NO_ARMOR) && Random.Int(2) == 0) {
                Buff.prolong(enemy, Vulnerable.class, 2f);
                Buff.prolong(enemy, Charm.class, 3f).object = id();
            }
        }
        if (phase == SNIPER){
            Buff.prolong(enemy, Vulnerable.class, 4f);
            Unstable.getRandomEnchant(new SpiritBow()).proc(new SpiritBow(), this, Dungeon.hero, damageRoll());
            if (isChallenged(Challenges.NO_SCROLLS) && Random.Int(2) == 0)
                Buff.prolong(enemy, PowerfulDegrade.class, 5f);
            if (isChallenged(Challenges.NO_ARMOR) && Random.Int(2) == 0) {
                Buff.prolong(enemy, Vulnerable.class, 2f);
                Buff.prolong(enemy, Charm.class, 3f).object = id();
            }
        }
        return super.attackProc(enemy, damage);
    }

    public static class LastStand extends FlavourBuff {
        @Override
        public int icon() {
            return BuffIndicator.LASTSTAND;
        }

        @Override
        public void detach() {
            super.detach();
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if (mob instanceof RatKingBoss){
                    mob.sprite.flash();
                    mob.sprite.bloodBurstA(target.sprite.center(), target.HT*2);
                    Sample.INSTANCE.play(Assets.Sounds.BLAST);
                    mob.die(Dungeon.hero);
                }
            }
        }

        @Override
        public void tintIcon(Image icon) {
            int duration = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 80 : 60;
            float color = 1 - (Math.max(0, (duration - visualcooldown() * 1.2f) / duration));
            icon.hardlight(color, color, 0);
        }

        @Override
        public String toString() {
            return Messages.get(this, "name");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }
    }

    @Override
    public void damage(int dmg, Object src) {
        dmg *= Math.max(0.45f, 1 - (HP * 1.f / HT));
        LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
        if (lock != null && !isImmune(src.getClass())) lock.addTime(dmg*1.5f);
        if (phase == ASSASSIN){
            dmg *= 1.75f;
        }
        if (HP - dmg <= 1 && !phase3Notice){
            dmg = 0;
            HP = 1;
            GameScene.flash(0xFFFF00);
            yell(Messages.get(this, "enraged_2"));
            phase3Notice = true;
            phase = -1;
            Buff.detach(this, PhaseTracker.class);
            Buff.affect(this, PhaseTracker.class, 0);
            Buff.affect(Dungeon.hero, LastStand.class, Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 80 : 60);
        }
        super.damage(dmg, src);
        if (HP < HT * 0.5f && !phase2Notice){
                BossHealthBar.bleed(true);
                GameScene.flash(0xFFFF00);
                sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Goo.class, "enraged"));
                yell(Messages.get(this, "enraged"));
                phase2Notice = true;
        }
    }

    @Override
    public int drRoll() {
        int dr = Random.NormalIntRange((phase == GLADIATOR ? 10 : 0), 22 + (phase == GLADIATOR ? 10 : 0));
        if (HP < HT*0.5f && phase == GLADIATOR){
            dr = Random.NormalIntRange(15, 40);
        }
        return super.drRoll() + dr;
    }

    @Override
    public int damageRoll() {
        if (phase == GLADIATOR){
            if (HP < HT*0.5f){
                return Random.NormalIntRange(58, 77);
            }
            if (phase3()){
                return Random.NormalIntRange(69, 100);
            }
            return Random.NormalIntRange(36, 48);
        }
        if (phase == ASSASSIN){
            if (phase3()){
                return Random.NormalIntRange(20, 45);
            }
            return Random.NormalIntRange(12, 28);
        }
        if (phase == SNIPER){
            if (phase3()){
                return Random.NormalIntRange(9, 20);
            }
            return Random.NormalIntRange(5, 14);
        }
        return super.damageRoll();
    }

    @Override
    public void notice() {
        super.notice();
        Buff.affect(this, PhaseTracker.class, 0);
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            if (HP <= HT/2) BossHealthBar.bleed(true);
            if (HP == HT) {
                for (Char ch : Actor.chars()){
                    if (ch instanceof DriedRose.GhostHero){
                        ((DriedRose.GhostHero) ch).sayBoss();
                    }
                }
            } else {
                yell(Messages.get(this, "notice_have", Dungeon.hero.name()));
            }
        }
    }

    public boolean isChallenged( int mask ) {
        if (phase3()) return true;
        if (mask == Challenges.STRONGER_BOSSES && rkMode()) return true;
        return (Dungeon.challenges & mask) != 0;
    }

    @Override
    public void die( Object cause ) {

        for (Mob mob : (Iterable<Mob>)Dungeon.level.mobs.clone()) {
            if (mob.alignment == Alignment.ENEMY && mob != this) {
                mob.maxLvl = -2;
                mob.die( cause );
            }
        }
        phase = -1;
        ((RatKingBossSprite)sprite).changeSprite(phase);
        Badges.validateRatKing();
        Statistics.bossScores[5] += 10000;

        GameScene.bossSlain();
        Dungeon.level.unseal();
        super.die( cause );
    }

    public void changePhase(){
        if (++phase > 4) phase = 0;
        ((RatKingBossSprite)sprite).changeSprite(phase);
    }

    @Override
    protected int modifyDamage(int dmg, Object src) {
        if (phase3()){
            return 0;
        }
        return super.modifyDamage(dmg, src);
    }

    @Override
    public boolean isImmune(Class effect) {
        if (phase3() && !(effect == PhaseTracker.class)){
            return true;
        }
        return super.isImmune(effect);
    }

    public static class PhaseTracker extends FlavourBuff{
        @Override
        public void detach() {
            super.detach();
            ((RatKingBoss)target).changePhase();
            Sample.INSTANCE.play(Assets.Sounds.CHALLENGE, 2f, 0.85f);
            int duration = (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 35 : 20) / (((RatKingBoss) target).phase3() ? 2 : 1);
            if (target.buff(WarpedEnemy.BossEffect.class) != null){
                duration *= Random.Float(0.25f, 0.9f);
            }
            Buff.affect(target, PhaseTracker.class,
                    duration);
            for (Heap heap : Dungeon.level.heaps.valueList()){
                for (Item i :heap.items){
                    if (i instanceof Tengu.ShockerAbility.ShockerItem){
                        heap.remove(i);
                    }
                }
            }
            for (Blob blob : Dungeon.level.blobs.values()/*.toArray(new Blob[0])*/){
                if (blob instanceof Tengu.ShockerAbility.ShockerBlob || blob instanceof Tengu.BombAbility.BombBlob){
                    blob.fullyClear();
                }
            }
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if (mob instanceof Wraith) mob.die(null);
            }
            FunctionalStuff.forEach(FunctionalStuff.extract(target.buffs(),
                    (buff) -> buff instanceof Tengu.ShockerAbility || buff instanceof Tengu.BombAbility),
                    Buff::detach);
            FunctionalStuff.forEach(FunctionalStuff.extract(Dungeon.hero.buffs(),
                    (buff) -> buff instanceof Tengu.ShockerAbility || buff instanceof Tengu.BombAbility),
                    Buff::detach);
        }
    }

    @Override
    public void spend(float time) {
        if (buff(Vertigo.class) != null) time *= 0.333f;
        if (buff(WarpedEnemy.BossEffect.class) != null) time *= Random.Float(0.33f, 1.25f);
        super.spend(time);
    }

    @Override
    protected boolean getCloser(int target) {
        boolean result = super.getCloser(target);
        if (!result){
            int count = 32;
            int newPos;
            do {
                newPos = Dungeon.level.randomRespawnCell( this );
                if (count-- <= 0) {
                    break;
                }
            } while (newPos == -1 || Dungeon.level.distance(newPos, pos) < (count/3));

            if (newPos != -1) {

                if (Dungeon.level.heroFOV[pos]) CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);
                pos = newPos;
                sprite.place( pos );
                sprite.visible = Dungeon.level.heroFOV[pos];
                if (Dungeon.level.heroFOV[pos]) CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);

            }
        }
        return result;
    }

    @Override
    public int attackSkill(Char target) {
        if (phase3()){
            return INFINITE_ACCURACY;
        }
        if (HP < HT * 0.5f && phase == SNIPER){
            return INFINITE_ACCURACY;
        }
        if (target instanceof Wraith){
            return 160;
        }
        return 45;
    }

    public boolean haventSeen = true;

    @Override
    protected boolean act() {
        if (Dungeon.level.heroFOV[pos] && haventSeen) {
            notice();
            haventSeen = false;
        }
        if (phase == EMPEROR || phase3()){
            while (summonCooldown <= 0){

                Class<?extends Mob> cls = regularSummons.remove(0);
                Mob summon = Reflection.newInstance(cls);
                regularSummons.add(cls);

                int spawnPos = -1;
                for (int i : PathFinder.NEIGHBOURS8){
                    if (Actor.findChar(pos+i) == null && Dungeon.level.passable[pos+i]){
                        if (spawnPos == -1 || Dungeon.level.trueDistance(Dungeon.hero.pos, spawnPos) > Dungeon.level.trueDistance(Dungeon.hero.pos, pos+i)){
                            spawnPos = pos + i;
                        }
                    }
                }

                if (spawnPos != -1) {
                    summon.pos = spawnPos;
                    GameScene.add( summon );
                    Actor.addDelayed( new Pushing( summon, pos, summon.pos ), -1 );
                    summon.beckon(Dungeon.hero.pos);

                    summonCooldown += Random.NormalFloat(MIN_SUMMON_CD, MAX_SUMMON_CD);
                    if (HP < HT*0.5f){
                        new DistortionTrap().set(pos).activate();
                    }
                } else {
                    break;
                }
            }
            if (summonCooldown > 0) summonCooldown--;
        }
        return super.act();
    }

    protected boolean doAttack(Char enemy ) {
        if (phase == SNIPER) {
                if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                    sprite.zap(enemy.pos);
                    return false;
                } else {
                    zap();
                    return true;
                }
        }
        return super.doAttack(enemy);
    }

    private void zap() {
        spend( phase3() ? 0.5f : 0.66f );

        attack(enemy);
    }

    public void onZapComplete() {
        zap();
        next();
    }

    @Override
    public String description() {
        String description = super.description();
        if (phase3()){
            description = Messages.get(this, "phase3");
        }
        switch (phase){
            case EMPEROR:
                description += "\n\n" + Messages.get(this, "emperor");
                if (Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)){
                    description += "\n" + Messages.get(this, "faster_summons");
                }
                break;
            case GLADIATOR:
                description += "\n\n" + Messages.get(this, "gladiator");
                if (Dungeon.isChallenged(Challenges.NO_FOOD)){
                    description += "\n" + Messages.get(this, "healing");
                }
                break;
            case BATTLEMAGE:
                description += "\n\n" + Messages.get(this, "battlemage"); break;
            case ASSASSIN:
                description += "\n\n" + Messages.get(this, "assassin");
                if (Dungeon.isChallenged(Challenges.DARKNESS)){
                    description += "\n" + Messages.get(this, "dark");
                }
                break;
            case SNIPER:
                description += "\n\n" + Messages.get(this, "sniper"); break;
        }
        if (Dungeon.isChallenged(Challenges.NO_SCROLLS)){
            description += "\n\n" + Messages.get(this, "runes");
        }
        if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
            description += "\n\n" + Messages.get(this, "bad_boss");
        }
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            description += "\n\n" + Messages.get(this, "armor");
        }
        return description;
    }

    private static final String PHASE = "phase";
    private static final String HAVESEEN = "haveseen";
    private static final String ATTACK = "attack";
    private static final String MAGIC_POS = "magicPos";
    private static final String SPEAR_POS = "spearPos";
    private static final String REGULAR_SUMMONS = "summons";
    private static final String SUMMON_CD = "summonCD";
    private static final String TARGETED_CELLS = "targetCells";
    private static final String TARGETED_DEST = "targetDest";
    private static final String MAGIC_PREPARE = "magicOn";
    private static final String PHASE2 = "phase2";
    private static final String PHASE3 = "phase3";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, phase);
        bundle.put(HAVESEEN, haventSeen);
        bundle.put(ATTACK, attack);
        bundle.put(MAGIC_POS, magicCastPos);
        bundle.put(SPEAR_POS, phantomSpearPositions);
        bundle.put(REGULAR_SUMMONS, regularSummons.toArray(new Class[0]));
        bundle.put(SUMMON_CD, summonCooldown);
        int[] bundleArr = new int[targetedCells.size()];
        for (int i = 0; i < targetedCells.size(); i++){
            bundleArr[i] = targetedCells.get(i);
        }
        bundle.put(TARGETED_CELLS, bundleArr);
        bundle.put(TARGETED_DEST, lastHeroPos);
        bundle.put(MAGIC_PREPARE, magicPrepare);
        bundle.put(PHASE2, phase2Notice);
        bundle.put(PHASE3, phase3Notice);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        phase = bundle.getInt(PHASE);
        haventSeen = bundle.getBoolean(HAVESEEN);
        attack = bundle.getEnum(ATTACK, MagicAttack.class);
        magicCastPos = bundle.getIntArray(MAGIC_POS);
        phantomSpearPositions = bundle.getInt(SPEAR_POS);
        regularSummons.clear();
        Collections.addAll(regularSummons, bundle.getClassArray(REGULAR_SUMMONS));
        summonCooldown = bundle.getFloat(SUMMON_CD);
        for (int i : bundle.getIntArray(TARGETED_CELLS)){
            targetedCells.add(i);
        }
        lastHeroPos = bundle.getInt(TARGETED_DEST);
        magicPrepare = bundle.getBoolean(MAGIC_PREPARE);
        phase2Notice = bundle.getBoolean(PHASE2);
        phase3Notice = bundle.getBoolean(PHASE3);
    }

    public static class SniperCurse extends FlavourBuff{
    }

    //rat king is always hunting
    private class Hunting extends Mob.Hunting{

        public boolean doCharging(){
            int oldPos = pos;
            if (target != -1 && getCloser( target )) {

                if (Dungeon.level.water[pos] && buff(ChampionEnemy.Flowing.class) != null){
                    spend(0.01f / speed());
                }
                else spend( 1 / speed() );
                return moveSprite( oldPos,  pos );

            } else {

                spend( TICK );
                return true;
            }
        }

        public boolean doMagic() {
            if (!magicPrepare) {
                ArrayList<MagicAttack> possibleAttacks = new ArrayList<>(Arrays.asList(
                        MagicAttack.MAGIC_MISSILE, MagicAttack.FIREBLAST, MagicAttack.FROST,
                        MagicAttack.POISON, MagicAttack.BLAST_WAVE, MagicAttack.LIGHTNING
                ));
                if (Dungeon.isChallenged(Challenges.DARKNESS))
                    possibleAttacks.add(MagicAttack.PRISMATIC);
                if (Dungeon.isChallenged(Challenges.NO_HERBALISM))
                    possibleAttacks.add(MagicAttack.RED_FIRE);
                if (Dungeon.isChallenged(Challenges.NO_HEALING))
                    possibleAttacks.add(MagicAttack.CORROSION);
                if (Dungeon.isChallenged(Challenges.NO_SCROLLS))
                    possibleAttacks.add(MagicAttack.RAT_KING);
                if (phase3()){
                    possibleAttacks.clear();
                    possibleAttacks.addAll(Arrays.asList(MagicAttack.values()));
                }
                attack = Random.element(possibleAttacks);
                for (int i = (HP < HT * 0.5f ? 0 : 1); i < magicCastPos.length; i++) {
                    magicCastPos[i] = enemy.pos;
                    if (i == 0) {
                        magicCastPos[i] = enemy.pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
                    }
                    Game.scene().addToFront(new TargetedCell(magicCastPos[i], attack.color));
                }
                magicPrepare = true;
                spend(TICK);
                return true;
            } else {
                int dest;
                for (int i = (HP < HT * 0.5f ? 0 : 1); i < magicCastPos.length; i++) {
                    dest = magicCastPos[i];
                    final int attackNum = i;
                    int finalDest = dest;
                        sprite.zap(dest, () -> MagicMissile.boltFromChar(sprite.parent, attack.boltType, sprite, finalDest,
                                () -> {
                                    ChampionEnemy.AntiMagic.effect(enemy, RatKingBoss.this);
                                    sprite.idle();
                                    Char ch = Actor.findChar(finalDest);
                                    switch (attack) {
                                        case MAGIC_MISSILE:
                                            if (ch != null) {
                                                ch.damage(Random.NormalIntRange(19, 38), new WandOfMagicMissile());
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            break;
                                        case FIREBLAST:
                                            for (int ii : PathFinder.NEIGHBOURS4) {
                                                if (Dungeon.level.insideMap(finalDest + ii) && !Dungeon.level.solid[finalDest + ii]) {
                                                    GameScene.add(Blob.seed(finalDest + ii, 3, Fire.class));
                                                }
                                            }
                                            if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                GameScene.add(Blob.seed(finalDest, 40, Inferno.class));
                                            }
                                            if (ch != null) {
                                                ch.damage((int) (Random.NormalIntRange(13, 29) *
                                                        (1 + 0.125f * Dungeon.hero.pointsInTalent(Talent.PYROMANIAC))), new WandOfFireblast());
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            break;
                                        case FROST:
                                            for (int k : PathFinder.NEIGHBOURS4) {
                                                if (Dungeon.level.insideMap(finalDest + k) && !Dungeon.level.solid[finalDest + k]) {
                                                    GameScene.add(Blob.seed(finalDest + k, 3, Freezing.class));
                                                }
                                            }
                                            if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                GameScene.add(Blob.seed(finalDest, 40, Blizzard.class));
                                            }
                                            if (ch != null) {
                                                ch.damage(Random.NormalIntRange(9, 24), new WandOfFrost());
                                                Buff.affect(ch, FrostBurn.class).reignite(ch);
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            break;
                                        case POISON:
                                            if (ch != null) {
                                                ch.damage(Random.NormalIntRange(6, 26), new Poison());
                                                Buff.affect(ch, Poison.class).set(20);
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                GameScene.add(Blob.seed(finalDest, 200, ToxicGas.class));
                                            }
                                            break;
                                        case LIGHTNING:
                                            if (ch != null) {
                                                ch.sprite.centerEmitter().burst(SparkParticle.FACTORY, 5);
                                                ch.damage(Random.NormalIntRange(30, 56), new WandOfLightning());
                                                ch.sprite.parent.addToFront(new Lightning(ch.pos, ch.pos, null));
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                for (int k : PathFinder.NEIGHBOURS4) {
                                                    if (Dungeon.level.insideMap(finalDest + k) && !Dungeon.level.solid[finalDest + k]) {
                                                        GameScene.add(Blob.seed(finalDest + k, 3, Electricity.class));
                                                    }
                                                }
                                            }
                                            break;
                                        case CORROSION:
                                            if (ch != null) {
                                                ch.damage(Random.NormalIntRange(10, 25), new WandOfCorrosion());
                                                Buff.affect(ch, Corrosion.class).set(3, 6);
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                GameScene.add(Blob.seed(finalDest, 100, CorrosiveGas.class));
                                            }
                                            break;
                                        case PRISMATIC:
                                            if (ch != null) {
                                                ch.damage(Random.NormalIntRange(12, 27), new WandOfPrismaticLight());
                                                Buff.affect(ch, Blindness.class, 10f);
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                GameScene.add(Blob.seed(finalDest, 100, SmokeScreen.class));
                                            }
                                            break;
                                        case BLAST_WAVE:
                                            WandOfBlastWave.BlastWave.blast(finalDest);
                                            for (int ii : PathFinder.NEIGHBOURS4) {
                                                Char c = Actor.findChar(finalDest + ii);
                                                if (c != null) {
                                                    Ballistica trajectory = new Ballistica(pos, c.pos, Ballistica.STOP_TARGET);
                                                    trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
                                                    WandOfBlastWave.throwChar(c, trajectory, 3, true, true, getClass());
                                                    if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                        Buff.affect(c, Paralysis.class, 1f);
                                                    }
                                                    if (c instanceof Hero)
                                                        Statistics.bossScores[5] -= 300;
                                                }
                                            }
                                            break;
                                        case RED_FIRE:
                                            for (int ii : PathFinder.NEIGHBOURS4) {
                                                if (Dungeon.level.insideMap(finalDest + ii) && !Dungeon.level.solid[finalDest + ii]) {
                                                    GameScene.add(Blob.seed(finalDest + ii, 3, GodSlayerFire.class));
                                                }
                                            }
                                            if (isChallenged(Challenges.STRONGER_BOSSES)) {
                                                GameScene.add(Blob.seed(finalDest, 400, Inferno.class));
                                            }
                                            if (ch != null) {
                                                ch.damage((int) (Random.NormalIntRange(19, 41) *
                                                        (1 + 0.125f * Dungeon.hero.pointsInTalent(Talent.PYROMANIAC))), new GodfireParticle());
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            break;
                                        case RAT_KING:
                                            sprite.parent.add(new Beam.RatRay(sprite.center(), DungeonTilemap.raisedTileCenterToWorld(finalDest)));
                                            if (ch != null) {
                                                ch.damage(Random.NormalIntRange(56, 100), new LegacyWrath());
                                                Buff.affect(ch, PowerfulDegrade.class, 20f);
                                                if (ch instanceof Hero)
                                                    Statistics.bossScores[5] -= 300;
                                            }
                                            break;
                                    }
                                    if (attackNum == 1){
                                        spend(TICK);
                                        next();
                                    }
                                }));
                        Sample.INSTANCE.play(Assets.Sounds.ZAP);
                }
                magicPrepare = false;
                return false;
            }
        }

        public boolean doRogue(){
            if (((HP < HT*0.5f) || Random.Int(3) == 0) || phantomSpearPositions != -1){
                return teleport();
            } else {
                return doCharging();
            }
        }

        public boolean teleport() {
            sprite.operate(enemy.pos, () -> {});
            if (phantomSpearPositions == -1) {
                PathFinder.buildDistanceMap(enemy.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null),
                        isChallenged(Challenges.DARKNESS) ? 0 : 1);
                int k;
                do {
                    k = Random.Int(PathFinder.distance.length);
                } while (PathFinder.distance[k] >= Integer.MAX_VALUE);
                Ballistica path = new Ballistica(pos, k, Ballistica.STOP_CHARS | Ballistica.STOP_TARGET);
                int collisionPos = path.collisionPos;
                if (collisionPos != Dungeon.hero.pos) {
                    //create a new collision pos
                    int newSourcePos = path.sourcePos;
                    for (int l = 0; l < path.path.size(); l++) {
                        if (path.path.get(l) == collisionPos) {
                            if (l - 1 > -1) {
                                newSourcePos = path.path.get(l - 1);
                            }
                        }
                    }
                    Ballistica path2 = new Ballistica(newSourcePos, path.collisionPos, Ballistica.MAGIC_BOLT);
                    collisionPos = path2.collisionPos;
                }
                phantomSpearPositions = collisionPos;
                Ballistica path2 = new Ballistica(pos, collisionPos, Ballistica.STOP_TARGET);
                for (int l = 0; l < path2.dist + 2; l++) {
                    Game.scene().addToFront(new TargetedCell(path2.path.get(l), 0xa9ded3));
                }
                spend(TICK*1);
                next();
                return true;
            }
            else sprite.doAfterAnim( () -> {
                    final int psp = phantomSpearPositions;
                    Callback callback = new Callback() {
                        @Override
                        public void call() {
                            Char ch = Actor.findChar(psp);
                            if (ch != null && ch != RatKingBoss.this) {
                                attack(ch);
                                Buff.detach(ch, Light.class);
                                if (isChallenged(Challenges.DARKNESS)) {
                                    Buff.affect(ch, Blindness.class, 10f);
                                }
                                if (ch instanceof Hero)
                                    Statistics.bossScores[5] -= 500;
                            }
                            next();
                        }
                    };
                    if (psp != -1) {
                        MissileSprite m = sprite.parent.recycle(MissileSprite.class);
                        Game.scene().addToFront(m);
                        m.reset(sprite, psp, new PhantomSpear(), callback);
                    }

                CellEmitter.bottom(pos).burst(Speck.factory(Speck.WOOL, true), 5);
                Sample.INSTANCE.play(Assets.Sounds.PUFF);
                do {
                    pos = Dungeon.level.randomDestination(RatKingBoss.this);
                } while (Actor.findChar(pos) == Dungeon.hero || Actor.findChar(pos) == enemy);
                ScrollOfTeleportation.appear(RatKingBoss.this, pos);
                if (enemy != null)
                 Actor.addDelayed(new Actor() {
                    @Override
                    protected boolean act() {
                        switch (Random.Int(2)){
                            case 0:
                                if (buff(Tengu.BombAbility.class) == null)
                                    Tengu.throwBomb(RatKingBoss.this, enemy); break;
                            case 1:
                                if (buff(Tengu.BombAbility.class) == null)
                                    Tengu.throwShocker(RatKingBoss.this, enemy); break;
                        }
                        diactivate();
                        return true;
                    }
                }, 1);



                phantomSpearPositions = -1;
            });

            return false;
        }

        /*for (int i = 0; i < 3; i++){
            int collisionPos = new Ballistica(pos, phantomSpearPositions[i], Ballistica.MAGIC_BOLT).collisionPos;
            MissileSprite m = sprite.parent.recycle(MissileSprite.class);
            int finalI = i;
            m.reset(sprite, collisionPos, new PhantomSpear(),
                    () -> {
                        Char ch = Actor.findChar(collisionPos);
                        if (ch != null) {
                            attack(ch);
                            Buff.detach(ch, Light.class);
                            if (isChallenged(Challenges.DARKNESS)) {
                                Buff.affect(ch, Blindness.class, 10f);
                                phantomSpearPositions[finalI] = -1;
                            }
                        }
                    });
            m.alpha(0.5f);
        }
        next();
    }*/

        public void doYogLasers(){
            boolean terrainAffected = false;
            HashSet<Char> affected = new HashSet<>();
            //delay fire on a rooted hero
            if (!enemy.rooted) {
                for (int i : targetedCells) {
                    Ballistica b = new Ballistica(i, lastHeroPos, Ballistica.WONT_STOP);
                    //shoot beams
                    sprite.parent.add(new Beam.RatRay(DungeonTilemap.raisedTileCenterToWorld(i), DungeonTilemap.raisedTileCenterToWorld(b.collisionPos)));
                    for (int p : b.path) {
                        Char ch = Actor.findChar(p);
                        if (ch != null && (ch.alignment != alignment || ch instanceof Bee)) {
                            affected.add(ch);
                        }
                        if (Dungeon.level.flamable[p]) {
                            Dungeon.level.destroy(p);
                            GameScene.updateMap(p);
                            terrainAffected = true;
                        }
                    }
                }
                if (terrainAffected) {
                    Dungeon.observe();
                }
                for (Char ch : affected) {
                    if (isChallenged(Challenges.STRONGER_BOSSES)){
                        ch.damage(Random.NormalIntRange(40, 69), new Eye.DeathGaze());
                    } else {
                        ch.damage(Random.NormalIntRange(60, 121), new Eye.DeathGaze());
                    }
                    if (isChallenged(Challenges.NO_SCROLLS) && ch instanceof Hero){
                        Buff.affect(ch, PowerfulDegrade.class, 12f);
                    }
                    if (ch instanceof Hero)
                        Statistics.bossScores[5] -= 400;

                    if (Dungeon.level.heroFOV[pos]) {
                        ch.sprite.flash();
                        CellEmitter.center(pos).burst(Speck.factory(Speck.COIN), Random.IntRange(2, 3));
                    }
                    if (!ch.isAlive() && ch == Dungeon.hero) {
                        Dungeon.fail(getClass());
                        GLog.n(Messages.get(Char.class, "kill", name()));
                    }
                }
                targetedCells.clear();
            }

            if (abilityCooldown <= 0 && HP < HT*0.8f){
                lastHeroPos = enemy.pos;

                int beams = (int) (4 + (HP * 1.0f / HT)*4);
                for (int i = 0; i < beams; i++){
                    int randompos = Random.Int(Dungeon.level.width()) + Dungeon.level.width()*2;
                    targetedCells.add(randompos);
                }

                for (int i : targetedCells){
                    Ballistica b = new Ballistica(i, Dungeon.hero.pos, Ballistica.WONT_STOP);
                    for (int p : b.path){
                        Game.scene().addToFront(new TargetedCell(p, 0xFF0000));
                    }
                }

                spend(TICK*1.5f);
                Dungeon.hero.interrupt();

                abilityCooldown += Random.NormalFloat(MIN_ABILITY_CD - 2*(1 - (HP * 1f / HT)), MAX_ABILITY_CD - 5*(1 - (HP * 1f / HT)));
            } else {
                spend(TICK);
            }
            if (abilityCooldown > 0) abilityCooldown--;
        }

        public boolean doSniper(){
            if (enemySeen && !isCharmedBy( enemy ) && canAttack( enemy )) {
                target = enemy.pos;
                return doAttack(Dungeon.hero);
            } else {
                target = enemy.pos;
                if (enemy.buff(SniperCurse.class) == null) {
                    Buff.affect(enemy, SniperCurse.class, 6f);
                } else if (enemy.buff(SniperCurse.class).cooldown() < 2){
                    int bestPos = enemy.pos;
                    for (int i : PathFinder.NEIGHBOURS8){
                        if (Dungeon.level.passable[pos + i]
                                && Actor.findChar(pos+i) == null
                                && Dungeon.level.trueDistance(pos+i, enemy.pos) > Dungeon.level.trueDistance(bestPos, enemy.pos)){
                            bestPos = pos+i;
                        }
                    }

                    if (enemy.buff(MagicImmune.class) != null){
                        bestPos = enemy.pos;
                    }

                    if (bestPos != enemy.pos){
                        ScrollOfTeleportation.appear(enemy, bestPos);
                        if (enemy instanceof Hero){
                            ((Hero) enemy).interrupt();
                            Dungeon.observe();
                        }
                    }
                }
            }
            int traps = Random.IntRange(1, 4);
            for (int i = 0; i < traps; i++){
                int trapPos;
                do {
                    trapPos = Dungeon.level.randomDestination(RatKingBoss.this);
                } while (Actor.findChar(pos) == Dungeon.hero || Actor.findChar(pos) == enemy);
                Trap t = ((Trap) Reflection.newInstance(Random.element(trapClasses)));
                Dungeon.level.setTrap(t, trapPos);
                Dungeon.level.map[t.pos] = t.visible ? Terrain.TRAP : Terrain.SECRET_TRAP;
                t.reveal();
            }
            spend(TICK);
            return true;
        }

        protected Class[] trapClasses = new Class[]{
                FrostTrap.class, StormTrap.class, CorrosionTrap.class,  DisintegrationTrap.class,
                RockfallTrap.class, FlashingTrap.class,
                DisarmingTrap.class, CursingTrap.class, GrimTrap.class, DistortionTrap.class };

        //prevents rare infinite loop cases
        private boolean recursing = false;

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {

            enemySeen = enemyInFOV;
            if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {
                target = Dungeon.hero.pos;
                return doAttack(enemy);
            } else {
                if (enemyInFOV) {
                    target = enemy.pos;
                } else {
                    chooseEnemy();
                    if (enemy == null){
                        //if nothing else can be targeted, target hero
                        enemy = Dungeon.hero;
                    }
                    target = enemy.pos;
                }
            }
            if (phase > -1 && HP < HT*0.5f){
                doYogLasers();
            }
            if (phase == GLADIATOR) return doCharging();
            if (phase == BATTLEMAGE) return doMagic();
            if (phase == ASSASSIN) return doRogue();
            if (phase == SNIPER) {
                return doSniper();
            }

            if (phase3()) return doCharging();

            spend( TICK );
            return true;
        }

    }

    @Override
    public float resistanceValue(Class effect) {
        if (effect == Paralysis.class){
            return 0.33f;
        }
        if (effect == Grim.class){
            return 0.1f;
        }
        return super.resistanceValue(effect);
    }

    {
        immunities.add(Sleep.class);
        immunities.add(Petrified.class);

        resistances.add(Terror.class);
        resistances.add(Charm.class);
        resistances.add(Vertigo.class);
        resistances.add(Cripple.class);
        resistances.add(Paralysis.class);
        resistances.add(Corrosion.class);
        resistances.add(Chill.class);
        resistances.add(Frost.class);
        resistances.add(Roots.class);
        resistances.add(Slow.class);
    }

}
