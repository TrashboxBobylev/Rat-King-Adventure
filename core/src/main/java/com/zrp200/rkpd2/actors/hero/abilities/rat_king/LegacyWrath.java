package com.zrp200.rkpd2.actors.hero.abilities.rat_king;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.BlobImmunity;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FireImbue;
import com.zrp200.rkpd2.actors.buffs.FrostImbue;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.TimedShrink;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.SmokeBomb;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Rat;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.armor.HuntressArmor;
import com.zrp200.rkpd2.items.armor.MageArmor;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.missiles.Shuriken;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// here we fucking go. this is a legacy ability to correspond with the previous mechanics.

public class LegacyWrath extends ArmorAbility {

    {
        baseChargeUse = 100;
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(SmokeBomb.class, "prompt");
    }

    @Override
    public Talent[] talents() { return new Talent[]{
            Talent.AURIC_TESLA, Talent.QUANTUM_POSITION, Talent.RAT_AGE, Talent.AVALON_POWER_UP, Talent.HEROIC_ENERGY}; }

    private static final float JUMP_DELAY=2f;

    @Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
        if(target == null) return;

        boolean[] stages = new boolean[3]; // jump/molten/blades

        if( stages[0] = target != hero.pos ) {
            if( !SmokeBomb.isValidTarget(hero, target, 6) ) return;

            if (Actor.findChar(target) != null) { // use heroic leap mechanics instead.
                Ballistica route = new Ballistica(hero.pos, target, Ballistica.STOP_TARGET);
                //can't occupy the same cell as another char, so move back one until it is valid.
                int i = 0;
                while (Actor.findChar(target) != null && target != hero.pos) {
                    target = route.path.get(route.dist - ++i);
                }
            }

            SmokeBomb.blindAdjacentMobs(hero);
            hero.sprite.turnTo(hero.pos, target);
            SmokeBomb.throwSmokeBomb(hero, target);
            hero.move(target);
            CellEmitter.center(hero.pos).burst(Speck.factory(Speck.DUST), 10);
            Camera.main.shake(2, 0.5f);
        }
        // now do mage
        if(stages[1] = MageArmor.doMoltenEarth()) {
            Dungeon.observe();
            hero.sprite.remove(CharSprite.State.INVISIBLE); // you still benefit from initial invisibiilty, even if you can't see it visually.
            hero.sprite.operate(hero.pos,()->{}); // handled.
            MageArmor.playMoltenEarthFX();
        }
        // warrior. this is delayed so the burst burning damage doesn't cancel this out instantly.
        if(stages[0]) for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
            Char mob = Actor.findChar(hero.pos + PathFinder.NEIGHBOURS8[i]);
            if (mob != null && mob != hero && mob.alignment != Char.Alignment.ALLY) {
                if (hero.hasTalent(Talent.AURIC_TESLA)) {
                    int damage = hero.drRoll();
                    damage = Math.round(damage*0.25f*hero.pointsInTalent(Talent.AURIC_TESLA));
                    mob.damage(damage, hero);
                    Buff.prolong(mob, Paralysis.class, hero.pointsInTalent(Talent.AURIC_TESLA)+2);
                }
                if (hero.hasTalent(Talent.AVALON_POWER_UP)) {
                    Ballistica trajectory = new Ballistica(mob.pos, mob.pos + i, Ballistica.MAGIC_BOLT);
                    int strength = 1 + hero.pointsInTalent(Talent.AVALON_POWER_UP)*2;
                    WandOfBlastWave.throwChar(mob, trajectory, strength, true, true, getClass());
                }
                if (hero.hasTalent(Talent.RAT_AGE)){
                        Buff.prolong(mob, TimedShrink.class, 1 + hero.pointsInTalent(Talent.RAT_AGE));
                        int scalingStr = hero.STR()-10;
                        int damage = Random.NormalIntRange(scalingStr + hero.pointsInTalent(Talent.RAT_AGE) - 1,
                                (3 + hero.pointsInTalent(Talent.RAT_AGE) - 1)*scalingStr);
                        damage -= mob.drRoll();

                        mob.damage(damage, hero);
                }
            }
        }
        // huntress
        HashMap<Callback, Mob> targets = new HashMap<>();
        for (Mob mob : Dungeon.level.mobs) {
            if (Dungeon.level.distance(hero.pos, mob.pos) <= 6 + hero.pointsInTalent(Talent.QUANTUM_POSITION)*3
                    && Dungeon.level.heroFOV[mob.pos]
                    && mob.alignment == Char.Alignment.ENEMY) {

                Callback callback = new Callback() {
                    @Override
                    public void call() {
                        hero.attack( targets.get( this ) );
                        Invisibility.dispel();
                        targets.remove( this );
                        if (targets.isEmpty()) finish(armor, hero, stages);
                    }
                };
                if (Dungeon.hero.hasTalent(Talent.AVALON_POWER_UP)) {
                    SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
                    if (bow == null && Dungeon.hero.belongings.weapon instanceof SpiritBow) {
                        bow = (SpiritBow) Dungeon.hero.belongings.weapon;
                    }
                    if (bow != null && Random.Int(6) < Dungeon.hero.pointsInTalent(Talent.AVALON_POWER_UP)) {
                        SpiritBow.SpiritArrow spiritArrow = bow.knockArrow();
                        spiritArrow.forceSkipDelay = true;
                        spiritArrow.cast(hero, mob.pos);
//								hero.spend(-hero.cooldown());
                    }
                }
                targets.put( callback, mob );
            }
        }
        // this guarentees proper sequence of events for spectral blades
        if ( stages[2] = targets.size() > 0 ) {
            // turn towards the average point of all enemies being shot at.
            Point sum = new Point(); for(Mob mob : targets.values()) sum.offset(Dungeon.level.cellToPoint(mob.pos));
            sum.scale(1f/targets.size());
            // wait for user sprite to finish doing what it's doing, then start shooting.
            hero.sprite.doAfterAnim( () -> hero.sprite.zap(Dungeon.level.pointToCell(sum), ()->{
                Shuriken proto = new Shuriken();
                for(Map.Entry<Callback, Mob> entry : targets.entrySet())
                    ( (MissileSprite)hero.sprite.parent.recycle( MissileSprite.class ) )
                            .reset( hero.sprite, entry.getValue().pos, proto, entry.getKey() );
            }));
            hero.busy();
        } else { // still need to finish, but also need to indicate that there was no enemies to shoot at.
            if( stages[1] ) Invisibility.dispel();
            else GLog.w( Messages.get(HuntressArmor.class, "no_enemies") );
            finish(armor, hero, stages);
        }
    }

    private void finish(ClassArmor armor, Hero hero, boolean[] stages) {
        hero.sprite.doAfterAnim(hero.sprite::idle); // because I overrode the default behavior I need to do this.

        int delay = 0;
        if(stages[1]) delay++;
        if(stages[2]) delay += hero.attackDelay();
        if(stages[0]) Actor.addDelayed(new Actor() {
            { actPriority = HERO_PRIO; } // this is basically the hero acting.
            @Override
            protected boolean act() {
                Buff.prolong(hero, Invisibility.class, Invisibility.DURATION/4f);
                if (hero.hasTalent(Talent.AURIC_TESLA)){
                    Buff.prolong(hero, Adrenaline.class, hero.pointsInTalent(Talent.AURIC_TESLA)*1.5f);
                }
                if (hero.hasTalent(Talent.RAT_AGE)){
                    Buff.affect(hero, FireImbue.class).set(9f);
                    if (hero.pointsInTalent(Talent.RAT_AGE) > 1){
                        Buff.affect(hero, FrostImbue.class, 9f);
                    }
                    if (hero.pointsInTalent(Talent.RAT_AGE) > 2){
                        Buff.affect(hero, BlobImmunity.class, 9f);
                    }
                }

                ArrayList<Integer> spawnPoints = new ArrayList<>();

                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                    int p = hero.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                        spawnPoints.add( p );
                    }
                }

                int ratsToSpawn = 1;

                while (ratsToSpawn > 0 && spawnPoints.size() > 0) {
                    int index = Random.index( spawnPoints );

                    Rat rat = Random.Int(10) == 0 ? new Ratmogrify.SummonedAlbino() : new Ratmogrify.SummonedRat();
                    rat.alignment = Char.Alignment.ALLY;
                    rat.state = rat.HUNTING;
                    GameScene.add( rat );
                    ScrollOfTeleportation.appear( rat, spawnPoints.get( index ) );

                    spawnPoints.remove( index );
                    ratsToSpawn--;
                }

                remove(this);
                return true;
            }
        },(delay += JUMP_DELAY)-1);

        hero.spendAndNext(delay);
        for(boolean stage : stages) if(stage) { armor.useCharge(hero, this); return; }
    }

    @Override public int icon() { return HeroIcon.LEGACYWRATH; }
}
