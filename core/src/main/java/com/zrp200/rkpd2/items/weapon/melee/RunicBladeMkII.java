/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2020 Trashbox Bobylev
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

package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.effects.particles.RunicParticle;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.Arrays;

public class RunicBladeMkII extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {

    private static final String AC_ZAP = "ZAP";

    {
        image = ItemSpriteSheet.RUNIC_BLADE_MK2;

        tier = 6;

        defaultAction = AC_ZAP;
        usesTargeting = true;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (charged) {
            actions.add( AC_ZAP );
        }

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_ZAP )) {

            curUser = hero;
            curItem = this;
            GameScene.selectCell( zapper );

        }
    }

    public boolean charged = true;

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("charge", charged);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        charged = bundle.getBoolean("charge");
    }

    //Essentially it's a tier 4 weapon, with tier 3 base max damage, and tier 5 scaling.
    //equal to tier 4 in damage at +5

    @Override
    public int max(int lvl) {
        int i = 6 * (tier-2) +                    //24 base, down from 30
                Math.round(lvl * (tier)); //+6 per level, up from +7
        if (!charged) i = 6 * (tier) + //36
                Math.round(lvl * (tier+2)); //+8
        return i;
    }

    public boolean tryToZap(Hero owner){

        if (owner.buff(MagicImmune.class) != null){
            GLog.w( Messages.get(this, "no_magic") );
            return false;
        }

        if (!isEquipped(owner)){
            GLog.w( Messages.get(this, "no_equip") );
            return false;
        }

        if (charged){
            return true;
        } else {
            GLog.w(Messages.get(this, "fizzles"));
            return false;
        }
    }

    @Override
    public String info() {
        String info = super.info();
        if (!charged){
            RunicCooldown cooldown = Dungeon.hero.buff(RunicCooldown.class);
            if (cooldown != null){
                info += "\n\n" + Messages.get(this, "cooldown", cooldown.cooldown()+1f);
            }
        }
        return info;
    }

    protected static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {

                //FIXME this safety check shouldn't be necessary
                //it would be better to eliminate the curItem static variable.
                final RunicBladeMkII curBlade;
                if (curItem instanceof RunicBladeMkII) {
                    curBlade = (RunicBladeMkII) curItem;
                } else {
                    return;
                }

                final Ballistica shot = new Ballistica( curUser.pos, target, Ballistica.FRIENDLY_PROJECTILE);
                final int cell = shot.collisionPos;

                if (target == curUser.pos || cell == curUser.pos) {
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                curUser.sprite.zap(cell);

                //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                if (Actor.findChar(target) != null)
                    QuickSlotButton.target(Actor.findChar(target));
                else
                    QuickSlotButton.target(Actor.findChar(cell));

                if (curBlade.tryToZap(curUser)) {

                    curUser.busy();
                    Invisibility.dispel();

                    if (curBlade.cursed){
                        if (!curBlade.cursedKnown){
                            GLog.n(Messages.get(Wand.class, "curse_discover", curBlade.name()));
                        }
                    } else {
                        Sample.INSTANCE.play(Assets.Sounds.ZAP);
                        ((MissileSprite) curUser.sprite.parent.recycle(MissileSprite.class)).
                                reset(curUser.sprite,
                                        cell,
                                        new RunicMissile(),
                                        new Callback() {
                                            @Override
                                            public void call() {
                                                Char enemy = Actor.findChar( cell );
                                                if (enemy != null && enemy != curUser) {
                                                    if (Char.hit(curUser, enemy, true)) {
                                                        int dmg = curBlade.damageRoll(curUser);
                                                        enemy.damage(dmg, curBlade);
                                                        if (curUser.subClass.is(HeroSubClass.GLADIATOR)) Buff.affect( curUser, Combo.class ).hit( enemy );
                                                        curBlade.proc(curUser, enemy, dmg);
                                                        Buff.affect(enemy, Talent.AntiMagicBuff.class, 6f);
                                                        Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC);
                                                    } else {
                                                        enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
                                                        Combo combo = curUser.buff(Combo.class);
                                                        if (combo != null) combo.miss( );
                                                    }
                                                } else {
                                                    Dungeon.level.pressCell(cell);
                                                }
                                                Splash.at(cell, Random.element(Arrays.asList(0xb588b1, 0x38c3c3, 0x2c9999, 0x6d5e80)) , 15);
                                                curBlade.charged = false;
                                                updateQuickslot();
                                                int slot = Dungeon.quickslot.getSlot(curBlade);
                                                if (slot != -1){
                                                    Dungeon.quickslot.clearSlot(slot);
                                                    updateQuickslot();
                                                    Dungeon.quickslot.setSlot( slot, curBlade );
                                                    updateQuickslot();
                                                }
                                                Buff.affect(curUser, RunicCooldown.class, 15*curBlade.delayFactor(curUser));
                                                curUser.spendAndNext(curBlade.delayFactor(curUser));
                                            }
                                        });
                    }
                    curBlade.cursedKnown = true;

                }

            }
        }

        @Override
        public String prompt() {
            return Messages.get(RunicBlade.class, "prompt");
        }
    };

    @Override
    public Emitter emitter() {
        if (!charged) return null;
        Emitter emitter = new Emitter();
        emitter.pos(12f, 1f);
        emitter.fillTarget = false;
        emitter.pour(StaffParticleFactory, 0.1f);
        return emitter;
    }

    public final Emitter.Factory StaffParticleFactory = new Emitter.Factory() {
        @Override
        //reimplementing this is needed as instance creation of new staff particles must be within this class.
        public void emit(Emitter emitter, int index, float x, float y ) {
            StaffParticle c = (StaffParticle)emitter.getFirstAvailable(StaffParticle.class);
            if (c == null) {
                c = new StaffParticle();
                emitter.add(c);
            }
            c.reset(x, y);
        }

        @Override
        //some particles need light mode, others don't
        public boolean lightMode() {
            return true;
        }
    };

    //determines particle effects to use based on wand the staff owns.
    public class StaffParticle extends PixelParticle {

        private float minSize;
        private float maxSize;
        public float sizeJitter = 0;

        public StaffParticle(){
            super();
        }

        public void reset( float x, float y ) {
            revive();

            speed.set(0);

            this.x = x;
            this.y = y;

            color( Random.element(Arrays.asList(0xb588b1, 0x38c3c3, 0x2c9999, 0x6d5e80)) );
            am = 0.85f;
            setLifespan(3f);
            speed.polar(Random.Float(PointF.PI2), 0.3f);
            setSize( 1f, 2f);
            radiateXY(2.5f);
        }

        public void setSize( float minSize, float maxSize ){
            this.minSize = minSize;
            this.maxSize = maxSize;
        }

        public void setLifespan( float life ){
            lifespan = left = life;
        }

        public void shuffleXY(float amt){
            x += Random.Float(-amt, amt);
            y += Random.Float(-amt, amt);
        }

        public void radiateXY(float amt){
            float hypot = (float)Math.hypot(speed.x, speed.y);
            this.x += speed.x/hypot*amt;
            this.y += speed.y/hypot*amt;
        }

        @Override
        public void update() {
            super.update();
            size(minSize + (left / lifespan)*(maxSize-minSize) + Random.Float(sizeJitter));
        }
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (!charged){
            attacker.buff(RunicCooldown.class).time -= 1;
            defender.sprite.emitter().burst(RunicParticle.FACTORY, Random.Int(10, 15));
        }
        return super.proc(attacker, defender, damage);
    }

    public void recharge(){
        charged = true;
    }

    public static class RunicCooldown extends FlavourBuff {

        @Override
        public void detach() {
            RunicBladeMkII runicBlade = Dungeon.hero.belongings.getItem(RunicBladeMkII.class);
            if (runicBlade != null){
                runicBlade.recharge();
                int slot = Dungeon.quickslot.getSlot(runicBlade);
                if (slot != -1){
                    Dungeon.quickslot.clearSlot(slot);
                    updateQuickslot();
                    Dungeon.quickslot.setSlot( slot, runicBlade );
                    updateQuickslot();
                }
            }
            SpellSprite.show(Dungeon.hero, SpellSprite.CHARGE);
            Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
            super.detach();
        }
    }

    public static class RunicMissile extends Item {
        {
            image = ItemSpriteSheet.RUNIC_BLADE;
        }

        @Override
        public Emitter emitter() {
                Emitter e = new Emitter();
                e.pos(7.5f, 7.5f);
                e.fillTarget = false;
                e.pour(RunicParticle.FACTORY, 0.005f);
                return e;
        }
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        //reapply runic's recharge to half it
        RunicCooldown cooldown = Dungeon.hero.buff(RunicCooldown.class);
        if (cooldown != null){
            cooldown.detach();
        }
        return super.warriorAttack(damage, enemy);
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        //we apply here because of projecting
        RunicBlade.RunicSlashTracker tracker = Buff.affect(hero, RunicBlade.RunicSlashTracker.class, 5f + delayFactor(hero));
        boolean abilityUsed = duelistAbility().execute(hero, target, this);
        if (!abilityUsed) tracker.detach();
    }

    @Override
    protected DuelistAbility duelistAbility() {
        return new MeleeAbility(1f);
    }

    @Override
    protected void afterAbilityUsed(Hero hero) {
        //reapply runic's recharge to half it
        RunicCooldown cooldown = Dungeon.hero.buff(RunicCooldown.class);
        if (cooldown != null){
            cooldown.detach();
        }
        super.afterAbilityUsed(hero);
    }
}