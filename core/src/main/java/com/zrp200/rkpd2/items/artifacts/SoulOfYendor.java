/*
 * Pixel Dungeon
 *   * Copyright (C) 2012-2015 Oleg Dolya
 *   *
 *   * Shattered Pixel Dungeon
 *   * Copyright (C) 2014-2019 Evan Debenham
 *   *
 *   * Summoning Pixel Dungeon
 *   * Copyright (C) 2019-2020 TrashboxBobylev
 *   *
 *   * This program is free software: you can redistribute it and/or modify
 *   * it under the terms of the GNU General Public License as published by
 *   * the Free Software Foundation, either version 3 of the License, or
 *   * (at your option) any later version.
 *   *
 *   * This program is distributed in the hope that it will be useful,
 *   * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   * GNU General Public License for more details.
 *   *
 *   * You should have received a copy of the GNU General Public License
 *   * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.items.artifacts;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.LockedFloor;
import com.zrp200.rkpd2.actors.buffs.RegenerationBuff;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.buffs.Slow;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.npcs.Shopkeeper;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Chains;
import com.zrp200.rkpd2.effects.Effects;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.Surprise;
import com.zrp200.rkpd2.items.Amulet;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.food.Food;
import com.zrp200.rkpd2.items.quest.Chaosstone;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.rings.RingOfEnergy;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfIdentify;
import com.zrp200.rkpd2.items.scrolls.ScrollOfMagicMapping;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRemoveCurse;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.scrolls.exotic.ExoticScroll;
import com.zrp200.rkpd2.levels.traps.Trap;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Earthroot;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.plants.Rotberry;
import com.zrp200.rkpd2.scenes.AlchemyScene;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.InterlevelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndOptions;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class SoulOfYendor extends Artifact implements AlchemyScene.ToolkitLike {

    private static final String AC_USE = "USE";
    private static final String AC_USEAGAIN = "USEAGAIN";
    public int lastAction = -1;

    {
        image = ItemSpriteSheet.ARTIFACT_OMNI;

        exp = 0;
        levelCap = 10;

        charge = 100;
        partialCharge = 0;
        chargeCap = 100;

        unique = true;
        bones = false;

        defaultAction = AC_USEAGAIN;
    }

    @Override
    public void activate(Char ch) {
        super.activate(ch);
        if (activeBuff != null)
            activeBuff.attachTo(ch);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)){
            if (activeBuff != null){
                activeBuff.detach();
                activeBuff = null;
            }
            return true;
        } else
            return false;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && !cursed && (charge > 0)) {
            actions.add(AC_USE);
            actions.add(AC_USEAGAIN);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_USE)){
            GameScene.show(
                    new WndOptions(Messages.get(SoulOfYendor.class, "usage_title"),
                            Messages.get(SoulOfYendor.class, "usage_message"),
                            Messages.get(SoulOfYendor.class, "horn_of_plenty"),
                            Messages.get(SoulOfYendor.class, "alchemist_toolkit"),
                            Messages.get(SoulOfYendor.class, "ethereal_chains"),
                            Messages.get(SoulOfYendor.class, "sandals_of_nature"),
                            Messages.get(SoulOfYendor.class, "timekeeper_hourglass"),
                            Messages.get(SoulOfYendor.class, "unstable_spellbook"),
                            Messages.get(SoulOfYendor.class, "master_armband")){
                        @Override
                        protected boolean enabled(int index) {
                            switch (index) {
                                case 0:
                                    return charge >= (100 / (level()+10)) && !cursed;
                                case 1:
                                default:
                                    return charge >= 1 && !cursed;
                                case 3:
                                case 4:
                                    return charge >= 2 && !cursed;
                                case 5:
                                case 6:
                                    return charge >= 3 && !cursed;
                            }
                        }

                        @Override
                        protected void onSelect(int index) {
                            curUser = hero;
                            lastAction = index;
                            doAction(index, curUser);
                        }
                    });
        } else if (action.equals(AC_USEAGAIN)){
            if (lastAction == -1){
                execute(hero, AC_USE);
            } else {
                doAction(lastAction, hero);
            }
        }
    }

    private static final String ACTION = "lastAction";
    private static final String BUFF =      "buff";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( ACTION, lastAction );
        if (activeBuff != null)
            bundle.put( BUFF , activeBuff );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        lastAction = bundle.getInt(ACTION);
        //these buffs belong to hourglass, need to handle unbundling within the hourglass class.
        if (bundle.contains( BUFF )){
            Bundle buffBundle = bundle.getBundle( BUFF );

            if (buffBundle.contains( timeFreeze.PRESSES ))
                activeBuff = new timeFreeze();
            else
                activeBuff = new timeStasis();

            activeBuff.restoreFromBundle(buffBundle);
        }
    }

    public void doAction(int index, final Hero hero){
        switch (index){
            case 0:
                //Horn of Plenty
                if (!isEquipped(hero)) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
                else if (charge == 0)  GLog.i( Messages.get(HornOfPlenty.class, "no_food") );
                else {
                    //consume as much food as it takes to be full, to a minimum of 1

                    int satietyPerCharge = (int) (Hunger.STARVING/100f);
                    if (Dungeon.isChallenged(Challenges.NO_FOOD)){
                        satietyPerCharge /= 3;
                    }

                    Hunger hunger = Buff.affect(Dungeon.hero, Hunger.class);
                    int chargesToUse = Math.max( 1, hunger.hunger() / satietyPerCharge);
                    if (chargesToUse > charge) chargesToUse = charge;
                    hunger.satisfy(satietyPerCharge * chargesToUse);

                    Talent.onFoodEaten(hero, satietyPerCharge * chargesToUse, this);

                    Statistics.foodEaten++;

                    charge -= chargesToUse;

                    hero.sprite.operate(hero.pos);
                    hero.busy();
                    SpellSprite.show(hero, SpellSprite.FOOD);
                    Sample.INSTANCE.play(Assets.Sounds.EAT);
                    GLog.i( Messages.get(HornOfPlenty.class, "eat") );

                    hero.spend(Food.TIME_TO_EAT);

                    Badges.validateFoodEaten();

                    updateQuickslot();
                }
                break;
            case 1:
                //Alchemist Toolkit
                if (!isEquipped(hero))              GLog.i( Messages.get(this, "need_to_equip") );
                else if (cursed)                    GLog.w( Messages.get(this, "cursed") );
                else {
                    AlchemyScene.assignToolkit(this);
                    Game.switchScene(AlchemyScene.class);
                }
            case 2:
                //Ethereal Chains
                curUser = hero;

                if (!isEquipped( hero )) {
                    GLog.i( Messages.get(Artifact.class, "need_to_equip") );
                    QuickSlotButton.cancel();

                } else if (charge < 1) {
                    GLog.i( Messages.get(this, "no_charge") );
                    QuickSlotButton.cancel();

                } else if (cursed) {
                    GLog.w( Messages.get(this, "cursed") );
                    QuickSlotButton.cancel();

                } else {
                    GameScene.selectCell(caster);
                }
                break;
            case 3:
                //Sandals of Nature
                if (!isEquipped( hero )) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
                else if (charge == 0)    GLog.i( Messages.get(SandalsOfNature.class, "no_charge") );
                else {
                    Buff.prolong(hero, Roots.class, Roots.DURATION);
                    Buff.affect(hero, Earthroot.Armor.class).level(charge);
                    CellEmitter.bottom(hero.pos).start(MagicMissile.EarthParticle.FACTORY, 0.05f, 8);
                    Camera.main.shake(1, 0.4f);
                    charge = 0;
                    updateQuickslot();
                }
                break;
            case 4:
                //Timekeeper Hourglass
                if (!isEquipped( hero ))        GLog.i( Messages.get(Artifact.class, "need_to_equip") );
                else if (activeBuff != null) {
                    if (activeBuff instanceof timeStasis) { //do nothing
                    } else {
                        activeBuff.detach();
                        GLog.i( Messages.get(TimekeepersHourglass.class, "deactivate") );
                    }
                } else if (charge <= 0)         GLog.i( Messages.get(TimekeepersHourglass.class, "no_charge") );
                else if (cursed)                GLog.i( Messages.get(TimekeepersHourglass.class, "cursed") );
                else GameScene.show(
                            new WndOptions( Messages.get(SoulOfYendor.class, "name"),
                                    Messages.get(TimekeepersHourglass.class, "prompt"),
                                    Messages.get(TimekeepersHourglass.class, "stasis"),
                                    Messages.get(TimekeepersHourglass.class, "freeze")) {
                                @Override
                                protected void onSelect(int index) {
                                    if (index == 0) {
                                        GLog.i( Messages.get(TimekeepersHourglass.class, "onstasis") );
                                        GameScene.flash(0xFFFFFF);
                                        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

                                        activeBuff = new timeStasis();
                                        activeBuff.attachTo(Dungeon.hero);
                                    } else if (index == 1) {
                                        GLog.i( Messages.get(TimekeepersHourglass.class, "onfreeze") );
                                        GameScene.flash(0xFFFFFF);
                                        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

                                        activeBuff = new timeFreeze();
                                        activeBuff.attachTo(Dungeon.hero);
                                        ((timeFreeze)activeBuff).processTime(0f);
                                    }
                                }
                            }
                    );
                break;
            case 5:
                //Unstable Spellbook
                if (hero.buff( Blindness.class ) != null) GLog.w( Messages.get(UnstableSpellbook.class, "blinded") );
                else if (!isEquipped( hero ))             GLog.i( Messages.get(Artifact.class, "need_to_equip") );
                else if (charge <= 3)                     GLog.i( Messages.get(UnstableSpellbook.class, "no_charge") );
                else if (cursed)                          GLog.i( Messages.get(UnstableSpellbook.class, "cursed") );
                else {
                    charge -= 3;

                    Scroll scroll;
                    do {
                        scroll = (Scroll) Generator.randomUsingDefaults(Generator.Category.SCROLL);
                    } while (scroll == null
                            //reduce the frequency of these scrolls by half
                            ||((scroll instanceof ScrollOfIdentify ||
                            scroll instanceof ScrollOfRemoveCurse ||
                            scroll instanceof ScrollOfMagicMapping) && Random.Int(2) == 0)
                            //don't roll teleportation scrolls on boss floors
                            || (scroll instanceof ScrollOfTeleportation && Dungeon.bossLevel()));

                    scroll.anonymize();
                    curItem = scroll;
                    curUser = hero;

                    //if there are charges left and the scroll has been given to the book
                    if (charge >= 3) {
                        final Scroll fScroll = scroll;

                        final UnstableSpellbook.ExploitHandler handler = Buff.affect(hero, UnstableSpellbook.ExploitHandler.class);
                        handler.scroll = scroll;

                        GameScene.show(new WndOptions(
                                Messages.get(UnstableSpellbook.class, "prompt"),
                                Messages.get(UnstableSpellbook.class, "read_empowered"),
                                scroll.trueName(),
                                Messages.get(ExoticScroll.regToExo.get(scroll.getClass()), "name")){
                            @Override
                            protected void onSelect(int index) {
                                handler.detach();
                                if (index == 1){
                                    Scroll scroll = Reflection.newInstance(ExoticScroll.regToExo.get(fScroll.getClass()));
                                    charge -= 3;
                                    scroll.doRead();
                                } else {
                                    fScroll.doRead();
                                }
                            }

                            @Override
                            public void onBackPressed() {
                                //do nothing
                            }
                        });
                    } else {
                        scroll.doRead();
                    }
                    updateQuickslot();
                }
                break;
            case 6:
                curUser = hero;

                if (!isEquipped( hero )) {
                    GLog.i( Messages.get(Artifact.class, "need_to_equip") );
                    usesTargeting = false;

                } else if (charge < 3) {
                    GLog.i( Messages.get(this, "no_charge") );
                    usesTargeting = false;

                } else if (cursed) {
                    GLog.w( Messages.get(this, "cursed") );
                    usesTargeting = false;

                } else {
                    usesTargeting = true;
                    GameScene.selectCell(armband_targeter);
                }
        }
    }

    private CellSelector.Listener armband_targeter = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer target) {

            if (target == null) {
                return;
            } else if (!Dungeon.level.adjacent(curUser.pos, target) || Actor.findChar(target) == null){
                GLog.w( Messages.get(MasterThievesArmband.class, "no_target") );
            } else {
                Char ch = Actor.findChar(target);
                if (ch instanceof Shopkeeper){
                    GLog.w( Messages.get(MasterThievesArmband.class, "steal_shopkeeper") );
                } else if (ch.alignment != Char.Alignment.ENEMY){
                    GLog.w( Messages.get(MasterThievesArmband.class, "no_target") );
                } else if (ch instanceof Mob) {
                    curUser.busy();
                    curUser.sprite.attack(target, new Callback() {
                        @Override
                        public void call() {
                            Sample.INSTANCE.play(Assets.Sounds.HIT);

                            boolean surprised = ((Mob) ch).surprisedBy(curUser, false);
                            float lootMultiplier = 1f + 0.2f*level();
                            int debuffDuration = 6 + level();

                            if (surprised){
                                lootMultiplier += 0.5f;
                                Surprise.hit(ch);
                                Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
                                debuffDuration += 2;
                            }

                            float lootChance = ((Mob) ch).lootChance() * lootMultiplier;

                            if (Dungeon.hero.lvl > ((Mob) ch).maxLvl + 2) {
                                lootChance = 0;
                            } else if (ch.buff(MasterThievesArmband.StolenTracker.class) != null){
                                lootChance = 0;
                            }

                            if (lootChance == 0){
                                GLog.w(Messages.get(MasterThievesArmband.class, "no_steal"));
                            } else if (Random.Float() <= lootChance){
                                Item loot = ((Mob) ch).createLoot();
                                if (Challenges.isItemBlocked(loot)){
                                    GLog.i(Messages.get(MasterThievesArmband.class, "failed_steal"));
                                    Buff.affect(ch, MasterThievesArmband.StolenTracker.class).setItemStolen(false);
                                } else {
                                    if (loot.doPickUp(curUser)) {
                                        //item collection happens instantly
                                        curUser.spend(-TIME_TO_PICK_UP);
                                    } else {
                                        Dungeon.level.drop(loot, curUser.pos).sprite.drop();
                                    }
                                    GLog.i(Messages.get(MasterThievesArmband.class, "stole_item", loot.name()));
                                    Buff.affect(ch, MasterThievesArmband.StolenTracker.class).setItemStolen(true);
                                }
                            } else {
                                GLog.i(Messages.get(MasterThievesArmband.class, "failed_steal"));
                                Buff.affect(ch, MasterThievesArmband.StolenTracker.class).setItemStolen(false);
                            }

                            Buff.prolong(ch, Blindness.class, debuffDuration);
                            Buff.prolong(ch, Cripple.class, debuffDuration);

                            charge -= 3;
                            Talent.onArtifactUsed(Dungeon.hero);
                            Item.updateQuickslot();
                            curUser.next();
                        }
                    });

                }
            }

        }

        @Override
        public String prompt() {
            return Messages.get(MasterThievesArmband.class, "prompt");
        }
    };

    public class timeStasis extends ArtifactBuff {

        {
            type = buffType.POSITIVE;
        }

        @Override
        public boolean attachTo(Char target) {

            if (super.attachTo(target)) {

                int usedCharge = 5;
                //buffs always act last, so the stasis buff should end a turn early.
                spend((usedCharge) - 1);
                ((Hero) target).spendAndNext(usedCharge);

                //shouldn't punish the player for going into stasis frequently
                Hunger hunger = Buff.affect(target, Hunger.class);
                if (hunger != null && !hunger.isStarving())
                    hunger.satisfy(5*usedCharge);

                charge -= usedCharge;

                target.invisible++;

                updateQuickslot();

                Dungeon.observe();

                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean act() {
            detach();
            return true;
        }

        @Override
        public void detach() {
            if (target.invisible > 0)
                target.invisible --;
            super.detach();
            activeBuff = null;
            Dungeon.observe();
        }

        @Override
        public void fx(boolean on) {
            if (on) target.sprite.add( CharSprite.State.INVISIBLE );
            else if (target.invisible == 0) target.sprite.remove( CharSprite.State.INVISIBLE );
        }
    }

    public class timeFreeze extends ArtifactBuff implements TimekeepersHourglass.TimeFreezing {

        {
            type = buffType.POSITIVE;
        }

        float turnsToCost = 0f;

        ArrayList<Integer> presses = new ArrayList<>();

        public void processTime(float time){
            turnsToCost -= time;

            //use 1/1,000 to account for rounding errors
            while (turnsToCost < -0.001f){
                turnsToCost += 2f;
                charge -= 2;
            }

            updateQuickslot();

            if (charge <= 0){
                charge = 0;
                detach();
            }

        }

        public void setDelayedPress(int cell){
            if (!presses.contains(cell))
                presses.add(cell);
        }

        public void triggerPresses(){
            for (int cell : presses){
                Trap t = Dungeon.level.traps.get(cell);
                if (t != null){
                    t.trigger();
                }
                Plant p = Dungeon.level.plants.get(cell);
                if (p != null){
                    p.trigger();
                }
            }

            presses = new ArrayList<>();
        }

        public void disarmPresses(){
            for (int cell : presses){
                Trap t = Dungeon.level.traps.get(cell);
                if (t != null && t.disarmedByActivation) {
                    t.disarm();
                }

                Plant p = Dungeon.level.plants.get(cell);
                if (p != null && !(p instanceof Rotberry)) {
                    Dungeon.level.uproot(cell);
                }
            }

            presses = new ArrayList<>();
        }

        @Override
        public void detach(){
            updateQuickslot();
            super.detach();
            activeBuff = null;
            triggerPresses();
            target.next();
        }

        @Override
        public void fx(boolean on) {
            Emitter.freezeEmitters = on;
            if (on){
                for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                    if (mob.sprite != null) mob.sprite.add(CharSprite.State.PARALYSED);
                }
            } else {
                for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                    if (mob.paralysed <= 0) mob.sprite.remove(CharSprite.State.PARALYSED);
                }
            }
        }

        private static final String PRESSES = "presses";
        private static final String TURNSTOCOST = "turnsToCost";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);

            int[] values = new int[presses.size()];
            for (int i = 0; i < values.length; i ++)
                values[i] = presses.get(i);
            bundle.put( PRESSES , values );

            bundle.put( TURNSTOCOST , turnsToCost);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);

            int[] values = bundle.getIntArray( PRESSES );
            for (int value : values)
                presses.add(value);

            turnsToCost = bundle.getFloat( TURNSTOCOST );
        }
    }

    public CellSelector.Listener caster = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer target) {
            if (target != null && (Dungeon.level.visited[target] || Dungeon.level.mapped[target])){

                //chains cannot be used to go where it is impossible to walk to
                PathFinder.buildDistanceMap(target, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                if (PathFinder.distance[curUser.pos] == Integer.MAX_VALUE){
                    GLog.w( Messages.get(EtherealChains.class, "cant_reach") );
                    return;
                }

                final Ballistica chain = new Ballistica(curUser.pos, target, Ballistica.STOP_TARGET);

                if (Actor.findChar( chain.collisionPos ) != null){
                    chainEnemy( chain, curUser, Actor.findChar( chain.collisionPos ));
                } else {
                    chainLocation( chain, curUser );
                }
                throwSound();
                Sample.INSTANCE.play( Assets.Sounds.CHAINS );

            }

        }

        @Override
        public String prompt() {
            return Messages.get(EtherealChains.class, "prompt");
        }
    };

    //pulls an enemy to a position along the chain's path, as close to the hero as possible
    private void chainEnemy( Ballistica chain, final Hero hero, final Char enemy ){

        if (enemy.properties().contains(Char.Property.IMMOVABLE)) {
            GLog.w( Messages.get(EtherealChains.class, "cant_pull") );
            return;
        }

        int bestPos = -1;
        for (int i : chain.subPath(1, chain.dist)){
            //prefer to the earliest point on the path
            if (!Dungeon.level.solid[i]
                    && Actor.findChar(i) == null
                    && (!Char.hasProp(enemy, Char.Property.LARGE) || Dungeon.level.openSpace[i])){
                bestPos = i;
                break;
            }
        }

        if (bestPos == -1) {
            GLog.i(Messages.get(EtherealChains.class, "does_nothing"));
            return;
        }

        final int pulledPos = bestPos;

        int chargeUse = Dungeon.level.distance(enemy.pos, pulledPos);
        if (chargeUse > charge) {
            GLog.w( Messages.get(EtherealChains.class, "no_charge") );
            return;
        } else {
            charge -= chargeUse;
            updateQuickslot();
        }

        hero.busy();
        hero.sprite.parent.add(new Chains(hero.sprite.center(), enemy.sprite.center(), Effects.Type.ETHEREAL_CHAIN,  new Callback() {
            public void call() {
                Actor.add(new Pushing(enemy, enemy.pos, pulledPos, new Callback() {
                    public void call() {
                        Dungeon.level.occupyCell(enemy);
                    }
                }));
                enemy.pos = pulledPos;
                Dungeon.observe();
                GameScene.updateFog();
                hero.spendAndNext(1f);
            }
        }));
    }

    //pulls the hero along the chain to the collisionPos, if possible.
    private void chainLocation( Ballistica chain, final Hero hero ){

        //don't pull if rooted
        if (hero.rooted){
            GLog.w( Messages.get(EtherealChains.class, "rooted") );
            return;
        }

        //don't pull if the collision spot is in a wall
        if (Dungeon.level.solid[chain.collisionPos]){
            GLog.i( Messages.get(EtherealChains.class, "inside_wall"));
            return;
        }

        //don't pull if there are no solid objects next to the pull location
        boolean solidFound = false;
        for (int i : PathFinder.NEIGHBOURS8){
            if (Dungeon.level.solid[chain.collisionPos + i]){
                solidFound = true;
                break;
            }
        }
        if (!solidFound){
            GLog.i( Messages.get(EtherealChains.class, "nothing_to_grab") );
            return;
        }

        final int newHeroPos = chain.collisionPos;

        int chargeUse = Dungeon.level.distance(hero.pos, newHeroPos);
        if (chargeUse > charge){
            GLog.w( Messages.get(EtherealChains.class, "no_charge") );
            return;
        } else {
            charge -= chargeUse;
            updateQuickslot();
        }

        hero.busy();
        hero.sprite.parent.add(new Chains(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(newHeroPos), Effects.Type.ETHEREAL_CHAIN, new Callback() {
            public void call() {
                Actor.add(new Pushing(hero, hero.pos, newHeroPos, new Callback() {
                    public void call() {
                        Dungeon.level.occupyCell(hero);
                    }
                }));
                hero.spendAndNext(1f);
                hero.pos = newHeroPos;
                Dungeon.observe();
                GameScene.updateFog();
            }
        }));
    }

    @Override
    public boolean isUpgradable() {
        return level() < 10;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new omniBuff();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (charge < chargeCap){
            partialCharge += 0.1f;
            if (partialCharge >= 1){
                partialCharge--;
                charge++;
                if (charge >= 100) charge = 100;
                updateQuickslot();
            }
        }
        //grants 5 turns of healing up-front
        float healDelay = 10f - level()*0.9f;
        healDelay /= amount;
        //effectively 1HP at lvl 0-5, 2HP lvl 6-8, 3HP lvl 9, and 5HP lvl 10.
        target.HP = Math.min( target.HT, target.HP + (int)Math.ceil(5/healDelay));
    }

    public int availableEnergy(){
        return charge;
    }

    public int consumeEnergy(int amount){
        int result = amount - charge;
        charge = Math.max(0, charge - amount);
        return Math.max(0, result);
    }

    public class omniBuff extends ArtifactBuff
            implements RegenerationBuff,
                SandalsOfNature.NaturalismBuff, MasterThievesArmband.ThieveryBuff {
        @Override
        public boolean act() {

            LockedFloor lock = target.buff(LockedFloor.class);
            if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
                //90 turns to charge at full, 60 turns to charge at 0/10
                float chargeGain = 0.2f + 0.075f*level();
                chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);
                partialCharge += chargeGain;

                while (partialCharge >= 1) {
                    partialCharge --;
                    charge ++;

                    if (charge == chargeCap){
                        partialCharge = 0;
                    }
                }
            } else if (cursed && Random.Int(10) == 0) {
                ((Hero) target).spend(TICK);
                Buff.prolong( target, Slow.class, 10f);

            }

            updateQuickslot();

            spend( TICK );

            return true;
        }


        @Override
        public void charge(float amount) {

        }

        @Override
        public int natureLevel() {
            return (int) (level()/2.5f);
        }

        public boolean steal(Item item){
            int chargesUsed = chargesToUse(item);
            float stealChance = stealChance(item);
            if (Random.Float() > stealChance){
                return false;
            } else {
                charge -= chargesUsed;
                GLog.i(Messages.get(MasterThievesArmband.class, "stole_item", item.name()));

                Talent.onArtifactUsed(Dungeon.hero);
                return true;
            }
        }

        public float stealChance(Item item){
            int chargesUsed = chargesToUse(item);
            float val = chargesUsed * (3 + level()/8f);
            return Math.min(1f, val/item.value());
        }

        public int chargesToUse(Item item){
            int value = item.value();
            float valUsing = 0;
            int chargesUsed = 0;
            while (valUsing < value && chargesUsed < charge){
                valUsing += 3 + level()/8f;
                chargesUsed++;
            }
            return chargesUsed;
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing(  );
    }

    @Override
    public String desc() {
        String desc = super.desc();

        if ( isEquipped (Dungeon.hero) ){
            if (cursed){
                desc += "\n\n" + Messages.get(this, "desc_cursed");
            } else {
                desc += "\n\n" + Messages.get(this, "desc_worn");
            }
        }

        return desc;
    }

    @Override
    protected void onThrow(int cell) {
        if (Dungeon.level.pit[cell] || Dungeon.hero.heroClass != HeroClass.RAT_KING){
            super.onThrow( cell );
        } else {
            Dungeon.level.pressCell( cell );
            Sample.INSTANCE.play(Assets.Sounds.SHATTER);
            GameScene.flash(0xFFFFFF, true);
            Dungeon.depth = -1;
            Statistics.deepestFloor = -1;
            InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
            Game.switchScene(InterlevelScene.class);
            Invisibility.dispel();
        }
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{Chaosstone.class, Kromer.class, Amulet.class};
            inQuantity = new int[]{1, 1, 1};

            cost = 101;

            output = SoulOfYendor.class;
            outQuantity = 1;
        }

    }

    public static boolean create(int pos, Item origin){

        for (int i : PathFinder.NEIGHBOURS4){
            Heap heap = Dungeon.level.heaps.get(pos + i);
            if (heap == null && !Dungeon.level.solid[pos + i]){
                pos = pos + i;
                break;
            }
        }

        ArrayList<Class<? extends Artifact>> artifacts = new ArrayList<>();
        ArrayList<Integer> artifactLevels = new ArrayList<>();
        artifacts.add(EtherealChains.class);
        artifacts.add(AlchemistsToolkit.class);
        artifacts.add(HornOfPlenty.class);
        artifacts.add(ChaliceOfBlood.class);
        artifacts.add(MasterThievesArmband.class);
        artifacts.add(SandalsOfNature.class);
        artifacts.add(TimekeepersHourglass.class);
        artifacts.add(UnstableSpellbook.class);
        boolean circleOfItems = true;
        for (int i : PathFinder.NEIGHBOURS8){
            Heap heap = Dungeon.level.heaps.get(pos + i);
            if (heap == null) circleOfItems = false;
        }
        if (circleOfItems){
            for (int i : PathFinder.NEIGHBOURS8){
                Heap heap = Dungeon.level.heaps.get(pos + i);
                if (heap != null) scanForArtifact(heap, artifacts, artifactLevels);
            }
        } else {
            return false;
        }
        if (artifacts.isEmpty()){
            int averageLevel = 0;
            for (int i : artifactLevels){
                averageLevel += i;
            }
            averageLevel /= 8;
            Dungeon.level.drop(new SoulOfYendor().upgrade(averageLevel), pos).sprite.drop();
            GameScene.flash(0xFFFFFF);
            Sample.INSTANCE.play(Assets.Sounds.BOSS);
            origin.detach(Dungeon.hero.belongings.backpack);
        } else {
            return false;
        }
        return true;
    }

    private static void scanForArtifact(Heap heap, ArrayList<Class<? extends Artifact>> requiredArtifacts,
                                        ArrayList<Integer> artifactLevels){
        for (Item item: heap.items){
            for (Class<? extends Artifact> artifactClass : requiredArtifacts){
                if (artifactClass.isAssignableFrom(item.getClass())){
                    heap.items.remove(item);
                    artifactLevels.add(item.level());
                    if (heap.items.isEmpty()) {
                        heap.destroy();
                    }
                    requiredArtifacts.remove(artifactClass);
                    break;
                }
            }
        }
    }
}
