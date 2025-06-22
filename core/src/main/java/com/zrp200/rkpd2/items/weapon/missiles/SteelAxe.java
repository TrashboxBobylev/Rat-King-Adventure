package com.zrp200.rkpd2.items.weapon.missiles;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Piranha;
import com.zrp200.rkpd2.items.artifacts.CloakOfShadows;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.weapon.melee.Crossbow;
import com.zrp200.rkpd2.items.weapon.missiles.darts.CrossbowAmmo;
import com.zrp200.rkpd2.levels.traps.TenguDartTrap;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SteelAxe extends MissileWeapon implements CrossbowAmmo {
    {
        image = ItemSpriteSheet.STEEL_AXE;

        hitSound = Assets.Sounds.HIT_ARROW;
        hitSoundPitch = 1.3f;

        tier = 6;

        baseUses = 40;
        sticky = false;
    }
    private boolean circling;

    @Override
    public float delayFactor(Char owner) {
        if (owner instanceof Hero && ((Hero) owner).justMoved)  return 0;
        else                                                    return super.delayFactor(owner);
    }

    @Override
    protected void rangedHit(Char enemy, int cell) {
        if(circling) {
            super.rangedHit(enemy, cell);
            return;
        }
        decrementDurability();
        if (durability > 0){
            Buff.append(Dungeon.hero, CircleBack.class).setup(this, cell, Dungeon.hero.pos, Dungeon.depth);
        }
    }

    @Override
    protected void rangedMiss(int cell) {
        if(circling) {
            super.rangedMiss(cell);
            return;
        }
        parent = null;
        Buff.append(Dungeon.hero, CircleBack.class).setup(this, cell, Dungeon.hero.pos, Dungeon.depth);
    }

    @Override
    public int min(int lvl) {
        if (bow != null){
            return  12 +                    //12 base
                    bow.buffedLvl()*2 + lvl*2; //+1 per level or bow level
        } else {
            return  6 +     //6 base, down from 12
                    lvl;    //+1, from +2
        }
    }

    @Override
    public int max(int lvl) {
        if (bow != null){
            return  30 +                       //30 base
                    6*bow.buffedLvl() + 6*lvl; //+6 per bow level, +6 per level (default scaling +2)
        } else {
            return  15 +     //2 base, down from 5
                    3*lvl;  //scaling unchanged
        }
    }

    private static Crossbow bow;

    public void updateCrossbow(){
        if (Dungeon.hero.belongings.weapon instanceof Crossbow){
            bow = (Crossbow) Dungeon.hero.belongings.weapon;
        } else {
            bow = null;
        }
    }

    public boolean crossbowHasEnchant( Char owner ){
        return bow != null && bow.enchantment != null && owner.buff(MagicImmune.class) == null;
    }

    @Override
    public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
        if (bow != null && bow.hasEnchant(type, owner)){
            return true;
        } else {
            return super.hasEnchant(type, owner);
        }
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (defender instanceof Piranha){
            damage = Math.max(damage, defender.HP/2);
        }
        if (bow != null){
            damage = bow.proc(attacker, defender, damage);
        }
        Buff.affect( defender, Bleeding.class ).set( Math.round(damage*0.6f) );
        Buff.prolong( defender, Cripple.class, Cripple.DURATION );

        return super.proc(attacker, defender, damage);
    }
    boolean cloakBoost;

    @Override
    public void onThrow(int cell) {
        updateCrossbow();
        if (Dungeon.level.pit[cell]){
            super.onThrow(cell);
            return;
        }

        rangedHit( null, cell );
        if(Dungeon.hero.heroClass.isExact(HeroClass.ROGUE) && Dungeon.hero.buff(CloakOfShadows.cloakStealth.class, false) != null) cloakBoost = true; // need to manually set this to get a consistent result. this is a flaw in my implementation of the boost mechanic.
        ArrayList<Char> targets = new ArrayList<>();
        if (Actor.findChar(cell) != null) targets.add(Actor.findChar(cell));

        for (int i : PathFinder.NEIGHBOURS8){
            if (!(Dungeon.level.traps.get(cell+i) instanceof TenguDartTrap)) Dungeon.level.pressCell(cell+i);
            if (Actor.findChar(cell + i) != null) targets.add(Actor.findChar(cell + i));
        }

        for (Char target : targets){
            curUser.shoot(target, this);
            if (target == Dungeon.hero && !target.isAlive()){
                Dungeon.fail(getClass());
                GLog.n(Messages.get(ForceCube.class, "ondeath"));
            }
        }

        cloakBoost = false;
        WandOfBlastWave.BlastWave.blast(cell);
    }

    @Override
    public int throwPos(Hero user, int dst) {
        updateCrossbow();
        return super.throwPos(user, dst);
    }

    @Override
    public void onRangedAttack(Char enemy, int cell, boolean hit) { } // custom logic.

    @Override
    public void throwSound() {
        updateCrossbow();
        if (bow != null) {
            Sample.INSTANCE.play(Assets.Sounds.ATK_CROSSBOW, 1, Random.Float(0.87f, 1.15f));
        } else {
            super.throwSound();
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public int damageRoll(Char owner) {
        if (owner instanceof Hero) {
            Hero hero = (Hero)owner;
            Char enemy = hero.enemy();
            if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)) {
                //deals 75% toward max to max on surprise, instead of min to max.
                int diff = max() - min();
                int damage = augment.damageFactor(Random.NormalIntRange(
                        min() + Math.round(diff*0.75f),
                        max()));
                int exStr = hero.STR() - STRReq();
                if (exStr > 0) {
                    damage += Random.IntRange(0, exStr);
                }
                return damage;
            }
        }
        return super.damageRoll(owner);
    }

    @Override
    public String info() {
        updateCrossbow();
        if (bow != null && !bow.isIdentified()){
            int level = bow.level();
            //temporarily sets the level of the bow to 0 for IDing purposes
            bow.level(0);
            String info = super.info();
            bow.level(level);
            return info;
        } else {
            return super.info();
        }
    }

    public static class CircleBack extends Buff {

        private SteelAxe boomerang;
        private int thrownPos;
        private int returnPos;
        private int returnDepth;

        private int left;

        public void setup( SteelAxe boomerang, int thrownPos, int returnPos, int returnDepth){
            this.boomerang = boomerang;
            this.thrownPos = thrownPos;
            this.returnPos = returnPos;
            this.returnDepth = returnDepth;
            left = 3;
        }

        public int returnPos(){
            return returnPos;
        }

        public MissileWeapon cancel(){
            detach();
            return boomerang;
        }

        @Override
        public boolean act() {
            if (returnDepth == Dungeon.depth){
                left--;
                if (left <= 0){
                    final Char returnTarget = Actor.findChar(returnPos);
                    final Char target = this.target;
                    MissileSprite visual = ((MissileSprite) Dungeon.hero.sprite.parent.recycle(MissileSprite.class));
                    visual.reset( thrownPos,
                            returnPos,
                            boomerang,
                            new Callback() {
                                @Override
                                public void call() {
                                    if (returnTarget == target){
                                        if (target instanceof Hero && boomerang.doPickUp((Hero) target)) {
                                            //grabbing the boomerang takes no time
                                            ((Hero) target).spend(-TIME_TO_PICK_UP);
                                        } else {
                                            Dungeon.level.drop(boomerang, returnPos).sprite.drop();
                                        }

                                    } else if (returnTarget != null){
                                        boomerang.circling = true;
                                        boomerang.onThrow(returnPos);
                                        boomerang.circling = false;
                                    } else {
                                        Dungeon.level.drop(boomerang, returnPos).sprite.drop();
                                    }
                                    CircleBack.this.next();
                                }
                            });
                    visual.alpha(0f);
                    float duration = Dungeon.level.trueDistance(thrownPos, returnPos) / 20f;
                    target.sprite.parent.add(new AlphaTweener(visual, 1f, duration));
                    detach();
                    return false;
                }
            }
            spend( TICK );
            return true;
        }

        private static final String BOOMERANG = "boomerang";
        private static final String THROWN_POS = "thrown_pos";
        private static final String RETURN_POS = "return_pos";
        private static final String RETURN_DEPTH = "return_depth";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(BOOMERANG, boomerang);
            bundle.put(THROWN_POS, thrownPos);
            bundle.put(RETURN_POS, returnPos);
            bundle.put(RETURN_DEPTH, returnDepth);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            boomerang = (SteelAxe) bundle.get(BOOMERANG);
            thrownPos = bundle.getInt(THROWN_POS);
            returnPos = bundle.getInt(RETURN_POS);
            returnDepth = bundle.getInt(RETURN_DEPTH);
        }
    }
}
