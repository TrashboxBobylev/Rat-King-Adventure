/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 *  Shattered Pixel Dungeon
 *  Copyright (C) 2014-2022 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2022 TrashboxBobylev
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

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.buffs.FrostBurn;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.Shrink;
import com.zrp200.rkpd2.actors.buffs.TimedShrink;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.PotionOfExperience;
import com.zrp200.rkpd2.items.rings.RingOfWealth;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.DragonSprite;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public class Dragon extends AbyssalMob {
    {
        HP = HT = 320;
        defenseSkill = 45;
        spriteClass = DragonSprite.class;

        EXP = 20;
        baseSpeed = 2f;

        flying = true;
        properties.add(Property.BOSS);
        properties.add(Property.FIERY);
        properties.add(Property.DEMONIC);
        properties.add(Property.UNDEAD);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 46 + abyssLevel()*12, 90 + abyssLevel()*25 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 70 + abyssLevel()*5;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(20 + abyssLevel()*10, 35 + abyssLevel()*10);
    }

    private int rangedCooldown = Random.NormalIntRange( 1, 3 );

    @Override
    protected boolean act() {
        if (state == HUNTING){
            rangedCooldown--;
        }

        return super.act();
    }

    @Override
    public Item createLoot(){
        int rolls = 30;
        ((RingOfWealth)(new RingOfWealth().upgrade(10))).buff().attachTo(this);
        ArrayList<Item> bonus = RingOfWealth.tryForBonusDrop(this, rolls);
        if (bonus != null && !bonus.isEmpty()) {
            for (Item b : bonus) Dungeon.level.drop(b, pos).sprite.drop();
            RingOfWealth.showFlareForBonusDrop(sprite);
        }
        return null;
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        if (Dungeon.isChallenged(Challenges.NO_LEVELS))
            new PotionOfExperience().apply(Dungeon.hero);
    }

    @Override
    public boolean canAttack(Char enemy) {
        if (buff(ChampionEnemy.Paladin.class) != null){
            return false;
        }
        if (rangedCooldown <= 0 && buff(Talent.AntiMagicBuff.class) == null) {
            return super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos;
        } else {
            return super.canAttack( enemy );
        }
    }

    protected boolean doAttack( Char enemy ) {

        if ((Dungeon.level.adjacent( pos, enemy.pos ) || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos != enemy.pos) || buff(Talent.AntiMagicBuff.class) != null) {

            return super.doAttack( enemy );

        } else {

            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                zap();
                return true;
            }
        }
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );
        meleeProc( enemy, damage );

        return damage;
    }

    private void zap() {
        spend( 2f );

        if (hit( this, enemy, true )) {

            rangedProc( enemy );

        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }

        rangedCooldown = Random.NormalIntRange( 3, 5 );
    }

    public void onZapComplete() {
        zap();
        next();
    }

    protected void meleeProc( Char enemy, int damage ) {
        if (Random.Int( 2 ) == 0 && !enemy.isWet()) {
            Buff.affect( enemy, FrostBurn.class ).reignite( enemy, 10f );
            Splash.at( enemy.sprite.center(), sprite.blood(), 5);
        }
        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
            if (pos + PathFinder.NEIGHBOURS8[i] == enemy.pos) {
                switch (i) {
                    case 0:
                        swipeAttack(1, 3);
                        break;
                    case 1:
                        swipeAttack(0, 2);
                        break;
                    case 2:
                        swipeAttack(1, 5);
                        break;
                    case 3:
                        swipeAttack(0, 6);
                        break;
                    case 4:
                        swipeAttack(2, 8);
                        break;
                    case 5:
                        swipeAttack(3, 7);
                        break;
                    case 6:
                        swipeAttack(6, 8);
                        break;
                    case 7:
                        swipeAttack(5, 7);
                        break;
                }
                return;
            }
        }
    }

    protected void swipeAttack(int adjacentDir1, int adjacentDir2){
        Char ch = Actor.findChar(pos + PathFinder.NEIGHBOURS9[adjacentDir1]);
        if (ch != null && ch.alignment != alignment) {
            bite(ch);
        }
        Char ch2 = Actor.findChar(pos + PathFinder.NEIGHBOURS9[adjacentDir2]);
        if (ch2 != null && ch2.alignment != alignment){
            bite(ch2);
        }
    }

    private void bite(Char enemy){
        boolean visibleFight = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos];

        if (enemy.isInvulnerable(getClass())) {

            if (visibleFight) {
                enemy.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "invulnerable") );

                Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
            }
        } else if (hit( this, enemy, false )) {

            int dr = enemy.drRoll();
            if (enemy.buff(Shrink.class) != null || enemy.buff(TimedShrink.class) != null) dr *= 0.5f;
            int dmg = damageRoll();
            if (enemy.buff(Shrink.class) != null || enemy.buff(TimedShrink.class) != null) dmg *= 1.4f;

            int effectiveDamage = enemy.defenseProc(this, dmg);
            effectiveDamage = Math.max(effectiveDamage - dr, 0);

            if (enemy.buff(Vulnerable.class) != null) {
                effectiveDamage *= 1.33f;
            }

            if (visibleFight) {
                if (effectiveDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
                    hitSound(Random.Float(0.87f, 1.15f));
                }
            }

            enemy.damage(effectiveDamage, this);
            if (enemy.buff(WarriorParry.BlockTrock.class) != null)
                enemy.buff(WarriorParry.BlockTrock.class).triggered = true;

            enemy.sprite.bloodBurstA(sprite.center(), effectiveDamage);
            enemy.sprite.flash();

            if (!enemy.isAlive() && visibleFight) {
                if (enemy == Dungeon.hero) {

                    Dungeon.fail(getClass());
                    GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name())));

                }
            }
        } else {

            if (visibleFight) {
                String defense = enemy.defenseVerb();
                enemy.sprite.showStatus( CharSprite.NEUTRAL, defense );

                Sample.INSTANCE.play(Assets.Sounds.MISS);

            }
        }
    }

    protected void rangedProc( Char enemy ) {
        if (enemy.buff(WarriorParry.BlockTrock.class) != null){
            enemy.sprite.emitter().burst( Speck.factory( Speck.FORGE ), 15 );
            SpellSprite.show(enemy, SpellSprite.BLOCK, 2f, 2f, 2f);
            enemy.buff(WarriorParry.BlockTrock.class).triggered = true;
        } else {
            if (!enemy.isWet()) {
                Buff.affect(enemy, FrostBurn.class).reignite(enemy, 8f);
            }

            Splash.at(enemy.sprite.center(), sprite.blood(), 5);

            ArrayList<Integer> candidates = new ArrayList<>();
            boolean[] solid = Dungeon.level.solid;

            int[] neighbours = {enemy.pos + 1, enemy.pos - 1, enemy.pos + Dungeon.level.width(), enemy.pos - Dungeon.level.width()};
            for (int n : neighbours) {
                if (!solid[n] && Actor.findChar(n) == null) {
                    candidates.add(n);
                }
            }
            ChampionEnemy.AntiMagic.effect(enemy, this);

            if (candidates.size() > 0) {

                SmallDragon clone = spawn();
                clone.pos = Random.element(candidates);
                clone.state = clone.HUNTING;

                Dungeon.level.occupyCell(clone);

                GameScene.add(clone);
                clone.sprite.jump(pos, clone.pos, new Callback() {
                    @Override
                    public void call() {
                    }
                });
            }
        }
    }

    private SmallDragon spawn() {
        SmallDragon clone = new SmallDragon();
        if (buff(Corruption.class ) != null) {
            Buff.affect( clone, Corruption.class);
        }
        return clone;
    }

    private static final String COOLDOWN = "cooldown";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( COOLDOWN, rangedCooldown );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        if (bundle.contains( COOLDOWN )){
            rangedCooldown = bundle.getInt( COOLDOWN );
        }
    }

    public static class SmallDragonSprite extends DragonSprite{
        public SmallDragonSprite() {
            super();
            scale.x = 0.5f;
            scale.y = 0.5f;
        }
    }

    public static class SmallDragon extends AbyssalMob {

        {
            spriteClass = SmallDragonSprite.class;

            HP = HT = 85;
            defenseSkill = 60;
            viewDistance = Light.DISTANCE;

            EXP = 5;
            maxLvl = -2;

            properties.add(Property.DEMONIC);
            properties.add(Property.FIERY);
        }

        @Override
        public int attackSkill( Char target ) {
            return 55 + abyssLevel()*3;
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange( 25 + abyssLevel()*9, 48 + abyssLevel()*16 );
        }

        @Override
        public int drRoll() {
            return Random.NormalIntRange(19 + abyssLevel()*4, 28 + abyssLevel()*10);
        }

    }
}
