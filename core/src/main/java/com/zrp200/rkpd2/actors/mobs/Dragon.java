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

import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.buffs.FrostBurn;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.PotionOfExperience;
import com.zrp200.rkpd2.items.rings.RingOfWealth;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.DragonSprite;

import java.util.ArrayList;

public class Dragon extends AbyssalMob {
    {
        HP = HT = 400;
        defenseSkill = 30;
        spriteClass = DragonSprite.class;

        EXP = 50;
        baseSpeed = 1.5f;

        flying = true;
        properties.add(Property.BOSS);
        properties.add(Property.FIERY);
        properties.add(Property.DEMONIC);
        properties.add(Property.UNDEAD);
    }

    @Override
    public float attackDelay() {
        return super.attackDelay()*2f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 60 + abyssLevel()*15, 115 + abyssLevel()*30 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 40 + abyssLevel()*5;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(25 + abyssLevel()*12, 40 + abyssLevel()*12);
    }

    private int rangedCooldown = Random.NormalIntRange( 6, 9 );

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
        if (Random.Int( 2 ) == 0 && !enemy.isWet()) {
            Buff.affect(enemy, FrostBurn.class ).reignite(enemy, 10f );
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
                break;
            }
        }

        return damage;
    }

    private void zap() {
        spend( 1f );

        if (hit( this, enemy, true )) {

            rangedProc( enemy );

        } else {
            enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
        }

        rangedCooldown = Random.NormalIntRange( 9, 16 );
    }

    public void onZapComplete() {
        zap();
        next();
    }

    protected void swipeAttack(int adjacentDir1, int adjacentDir2){
        Char ch = Actor.findChar(pos + PathFinder.NEIGHBOURS9[adjacentDir1]);
        if (ch != null && ch.alignment != alignment) {
            attack(ch);
        }
        Char ch2 = Actor.findChar(pos + PathFinder.NEIGHBOURS9[adjacentDir2]);
        if (ch2 != null && ch2.alignment != alignment){
            attack(ch2);
        }
    }

    protected void rangedProc( Char enemy ) {
        if (enemy.buff(WarriorParry.BlockTrock.class) != null){
            enemy.sprite.emitter().burst( Speck.factory( Speck.FORGE ), 15 );
            SpellSprite.show(enemy, SpellSprite.BLOCK, 2f, 2f, 2f);
            enemy.buff(WarriorParry.BlockTrock.class).triggered = true;
        } else {
            if (!enemy.isWet()) {
                Buff.affect(enemy, FrostBurn.class).reignite(enemy, 12f);
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

            HP = HT = 0;
            defenseSkill = 70;
            viewDistance = Light.DISTANCE;

            EXP = 5;
            maxLvl = -2;

            properties.add(Property.DEMONIC);
            properties.add(Property.FIERY);
        }

        @Override
        public int attackSkill( Char target ) {
            return 65 + abyssLevel()*3;
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange( 16 + abyssLevel()*5, 30 + abyssLevel()*10 );
        }

        @Override
        public float attackDelay() {
            return super.attackDelay()*0.5f;
        }

        @Override
        public int drRoll() {
            return Random.NormalIntRange(12 + abyssLevel()*2, 20 + abyssLevel()*6);
        }

    }
}
