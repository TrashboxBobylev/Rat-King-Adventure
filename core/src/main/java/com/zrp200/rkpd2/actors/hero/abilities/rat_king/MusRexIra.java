package com.zrp200.rkpd2.actors.hero.abilities.rat_king;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.LockedFloor;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.NaturesPower;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpiritHawk;
import com.zrp200.rkpd2.actors.hero.abilities.mage.WarpBeacon;
import com.zrp200.rkpd2.actors.hero.abilities.mage.WildMagic;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.ShadowClone;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.SmokeBomb;
import com.zrp200.rkpd2.actors.hero.abilities.warrior.Endure;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Swiftthistle;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.InterlevelScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndOptions;

import java.util.ArrayList;

import static com.zrp200.rkpd2.actors.hero.abilities.rogue.ShadowClone.getShadowAlly;

public class MusRexIra extends ArmorAbility {

    {
        baseChargeUse = 20f;
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {

        if (target == null){
            return;
        }

        if (target == hero.pos){
            //summon dumb raven and shadow clone
            SpiritHawk.HawkAlly ally = SpiritHawk.getHawk();

            if (ally == null) {
                ArrayList<Integer> spawnPoints = new ArrayList<>();
                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                    int p = hero.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
                        spawnPoints.add(p);
                    }
                }

                if (!spawnPoints.isEmpty()){
                    armor.charge -= chargeUse(hero)/2;

                    ally = new SpiritHawk.HawkAlly();
                    ally.pos = Random.element(spawnPoints);
                    GameScene.add(ally);

                    ScrollOfTeleportation.appear(ally, ally.pos);
                    Dungeon.observe();

                    Invisibility.dispel();
                    hero.spendAndNext(Actor.TICK);

                } else {
                    GLog.w(Messages.get(SpiritHawk.class, "no_space"));
                }
            }

            ShadowClone.ShadowAlly ShAlly = getShadowAlly();

            if (ShAlly == null) {
                ArrayList<Integer> spawnPoints = new ArrayList<>();
                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                    int p = hero.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar(p) == null && Dungeon.level.passable[p]) {
                        spawnPoints.add(p);
                    }
                }

                if (!spawnPoints.isEmpty()) {
                    armor.charge -= chargeUse(hero) / 2;

                    ShAlly = new ShadowClone.ShadowAlly(hero.lvl);
                    ShAlly.pos = Random.element(spawnPoints);
                    GameScene.add(ShAlly);

                    ShadowClone.ShadowAlly.appear(ShAlly, ShAlly.pos);

                    Invisibility.dispel();
                    hero.spendAndNext(Actor.TICK);

                } else {
                    GLog.w(Messages.get(ShadowClone.class, "no_space"));
                }
            }
                if (armor.charge >= chargeUse(hero)*3) {
                    //endure
                    Buff.prolong(hero, Endure.EndureTracker.class, 13f);

                    Combo combo = hero.buff(Combo.class);
                    if (combo != null) {
                        combo.addTime(3f);
                    }
                    armor.charge -= chargeUse(hero) * 2;
                    hero.sprite.operate(hero.pos);
                    hero.spendAndNext(3f);
                }
                if (armor.charge >= chargeUse(hero)) {
                    //nature's power
                    Buff.prolong(hero, NaturesPower.naturesPowerTracker.class, NaturesPower.naturesPowerTracker.DURATION + 3);
                    hero.buff(NaturesPower.naturesPowerTracker.class).extensionsLeft = 2;
                    hero.sprite.operate(hero.pos);
                    Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
                    hero.sprite.emitter().burst(LeafParticle.GENERAL, 10);

                    armor.charge -= chargeUse(hero);
                }
                armor.updateQuickslot();

                Invisibility.dispel();
        } else if (Actor.findChar(target) != null) {
            //death mark and wild magic
            //also ally targeting
            SpiritHawk.HawkAlly ally = SpiritHawk.getHawk();

            if (ally != null){
                ally.directTocell(target);
            }

            ShadowClone.ShadowAlly shadowAlly = getShadowAlly();

            if (shadowAlly != null){
                shadowAlly.directTocell(target);
            }

            ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
            Random.shuffle(wands);

            float chargeUsePerShot = (float)Math.pow(0.563f, hero.pointsInTalent(Talent.CONSERVED_MAGIC));

            for (Wand w : wands.toArray(new Wand[0])){
                if (w.curCharges() < 1 && w.partialCharge < chargeUsePerShot){
                    wands.remove(w);
                }
            }

            int maxWands = 4 + Dungeon.hero.pointsInTalent(Talent.FIRE_EVERYTHING, Talent.ASTRAL_CHARGE);
            if (hero.hasTalent(Talent.ELDRITCH_BLESSING)) maxWands += 2 + hero.pointsInTalent(Talent.ELDRITCH_BLESSING)/2;

            if (wands.size() < maxWands){
                ArrayList<Wand> dupes = new ArrayList<>(wands);

                for (Wand w : dupes.toArray(new Wand[0])){
                    float totalCharge = w.curCharges() + w.partialCharge;
                    if (totalCharge < 2*chargeUsePerShot){
                        dupes.remove(w);
                    }
                }

                Random.shuffle(dupes);
                while (!dupes.isEmpty() && wands.size() < maxWands){
                    wands.add(dupes.remove(0));
                }
            }

            if (wands.size() == 0){
                GLog.w(Messages.get(WildMagic.class, "no_wands"));
            } else {

                hero.busy();

                Random.shuffle(wands);

                Buff.affect(hero, WildMagic.WildMagicTracker.class, 0f);

                armor.charge -= chargeUse(hero)*2;

                WildMagic.zapWand(wands, hero, target);
            }
        } else {
            //set or tp to beacon
            if (hero.buff(WarpBeacon.WarpBeaconTracker.class) != null){
                final WarpBeacon.WarpBeaconTracker tracker = hero.buff(WarpBeacon.WarpBeaconTracker.class);

                GameScene.show( new WndOptions(
                        new Image(hero.sprite),
                        Messages.titleCase(name()),
                        Messages.get(WarpBeacon.class, "window_desc", tracker.depth),
                        Messages.get(WarpBeacon.class, "window_tele"),
                        Messages.get(WarpBeacon.class, "window_clear"),
                        Messages.get(WarpBeacon.class, "window_cancel")){

                    @Override
                    protected void onSelect(int index) {
                        if (index == 0){

						/*if (tracker.depth != Dungeon.depth && !hero.hasTalent(Talent.LONGRANGE_WARP)){
							GLog.w( Messages.get(WarpBeacon.class, "depths") );
							return;
						}*/

                            float chargeNeeded = chargeUse(hero)*2;

                            if (tracker.depth != Dungeon.depth){
                                // changed from shattered
                                if (hero.canHaveTalent(Talent.LONGRANGE_WARP))
                                    chargeNeeded *= 1.75f - 0.25*Dungeon.hero.pointsInTalent(Talent.LONGRANGE_WARP);
                                else if (hero.hasTalent(Talent.ASTRAL_CHARGE))
                                    chargeNeeded *= 1.833f - 0.333f*Dungeon.hero.pointsInTalent(Talent.ASTRAL_CHARGE);
                            }

                            // TODO fix for supercharge
                            if (armor.charge < chargeNeeded){
                                GLog.w( Messages.get(ClassArmor.class, "low_charge") );
                                return;
                            }

                            armor.charge -= chargeNeeded;
                            armor.updateQuickslot();

                            if (tracker.depth == Dungeon.depth){
                                if (hero.hasTalent(Talent.SHADOWSPEC_SLICE)) {
                                    for (Char ch : Actor.chars()){
                                        if (ch instanceof SmokeBomb.NinjaLog){
                                            ch.die(null);
                                        }
                                    }

                                    SmokeBomb.NinjaLog n = new SmokeBomb.NinjaLog();
                                    n.pos = hero.pos;
                                    GameScene.add(n);
                                }

                                Char existing = Actor.findChar(tracker.pos);

                                Invisibility.dispel();
                                ScrollOfTeleportation.appear(hero, tracker.pos);
                                if (hero.hasTalent(Talent.CHRONO_SCREW)){
                                    Buff.affect(hero, Swiftthistle.TimeBubble.class).reset(1
                                            + 1.5f*(hero.pointsInTalent(Talent.CHRONO_SCREW)-1));
                                }

                                if (existing != null && existing != hero){
                                    if (hero.canHaveTalent(Talent.TELEFRAG) || hero.hasTalent(Talent.ASTRAL_CHARGE)){
                                    int heroHP = hero.HP + hero.shielding();
                                    int heroDmg = Math.round(1.666f + 3.333f*Math.max(
                                            hero.shiftedPoints(Talent.TELEFRAG),
                                            hero.pointsInTalent(Talent.ASTRAL_CHARGE)));
                                    hero.damage(Math.min(heroDmg, heroHP-1), new WarpBeacon());

                                    int damage = Random.NormalIntRange(10*Math.max(
                                            hero.shiftedPoints(Talent.TELEFRAG),
                                            hero.pointsInTalent(Talent.ASTRAL_CHARGE)), 15*Math.max(
                                                    hero.shiftedPoints(Talent.TELEFRAG),
                                                    hero.pointsInTalent(Talent.ASTRAL_CHARGE)));
                                    existing.sprite.flash();
                                    existing.sprite.bloodBurstA(existing.sprite.center(), damage);
                                    existing.damage(damage, new WarpBeacon());

                                    Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH);
                                    Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
                                    }

                                    if (existing.isAlive()){
                                        Char toPush = Char.hasProp(existing, Char.Property.IMMOVABLE) ? hero : existing;

                                        ArrayList<Integer> candidates = new ArrayList<>();
                                        for (int n : PathFinder.NEIGHBOURS8) {
                                            int cell = tracker.pos + n;
                                            if (!Dungeon.level.solid[cell] && Actor.findChar( cell ) == null
                                                    && (!Char.hasProp(toPush, Char.Property.LARGE) || Dungeon.level.openSpace[cell])) {
                                                candidates.add( cell );
                                            }
                                        }
                                        Random.shuffle(candidates);

                                        if (!candidates.isEmpty()){
                                            Actor.addDelayed( new Pushing( toPush, toPush.pos, candidates.get(0) ), -1 );

                                            toPush.pos = candidates.get(0);
                                            Dungeon.level.occupyCell(toPush);
                                            hero.next();

                                        }
                                    }
                                }

                                Dungeon.observe();

                            } else {

                                if (hero.buff(LockedFloor.class) != null){
                                    GLog.w( Messages.get(WarpBeacon.class, "locked_floor") );
                                    return;
                                }

                                Invisibility.dispel();
                                Buff buff = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
                                if (buff != null) buff.detach();
                                buff = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
                                if (buff != null) buff.detach();

                                InterlevelScene.mode = InterlevelScene.Mode.RETURN;
                                InterlevelScene.returnDepth = tracker.depth;
                                InterlevelScene.returnPos = tracker.pos;
                                Game.switchScene( InterlevelScene.class );
                            }

                        } else if (index == 1){
                            hero.buff(WarpBeacon.WarpBeaconTracker.class).detach();
                        }
                    }
                } );

            } else {
                if (!Dungeon.level.mapped[target] && !Dungeon.level.visited[target]){
                    return;
                }

                if (Dungeon.level.distance(hero.pos, target) > 3 + 3*hero.pointsInTalent(Talent.REMOTE_BEACON)){
                    GLog.w( Messages.get(WarpBeacon.class, "too_far") );
                    return;
                }

                PathFinder.buildDistanceMap(target, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                if (Dungeon.level.pit[target] ||
                        (Dungeon.level.solid[target] && !Dungeon.level.passable[target]) ||
                        PathFinder.distance[hero.pos] == Integer.MAX_VALUE){
                    GLog.w( Messages.get(WarpBeacon.class, "invalid_beacon") );
                    return;
                }

                WarpBeacon.WarpBeaconTracker tracker = new WarpBeacon.WarpBeaconTracker();
                tracker.pos = target;
                tracker.depth = Dungeon.depth;
                tracker.attachTo(hero);

                hero.sprite.operate(target);
                Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
                Invisibility.dispel();
                hero.spendAndNext(Actor.TICK);
            }
        }
    }

    @Override public int icon() { return HeroIcon.MUS_REX; }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.BLOODFLARE_SKIN, Talent.ASTRAL_CHARGE, Talent.SHADOWSPEC_SLICE, Talent.SILVA_RANGE, Talent.HEROIC_ENERGY};
    }
}
