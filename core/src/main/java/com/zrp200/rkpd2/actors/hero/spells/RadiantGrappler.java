package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.HolyFlames;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Chains;
import com.zrp200.rkpd2.effects.Effects;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.items.Dewdrop;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.artifacts.EtherealChains;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.keys.Key;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.levels.traps.Trap;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Languages;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.HashSet;

public class RadiantGrappler extends TargetedClericSpell {
    public static final RadiantGrappler INSTANCE = new RadiantGrappler();

    @Override
    public int icon() {
        return HeroIcon.RADIANT_GRAPPLER;
    }

    @Override
    public void tintIcon(HeroIcon icon) {
        // todo make icon
        if (SpellEmpower.isActive()) icon.tint(0, .33f);
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && (hero.hasTalent(Talent.RADIANT_GRAPPLER) || SpellEmpower.isActive());
    }

    @Override
    public float chargeUse(Hero hero) {
        if (SpellEmpower.isActive()){
            return 3;
        }
        return 1;
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        if (SpellEmpower.isActive()){
            final HashSet<Heap> targets = new HashSet<>();
            final HashSet<Callback> callbacks = new HashSet<>();
            for (Heap h : Dungeon.level.heaps.valueList()){
                if (h.type == Heap.Type.HEAP && Dungeon.level.heroFOV[h.pos]) {
                    targets.add(h);
                }
            }
            if (!targets.isEmpty()) {
                for (Heap h : targets) {
                    Callback callback = () -> {
                        Item item = h.peek();
                        if (item.doPickUp(hero, h.pos)) {
                            h.pickUp();
                            hero.spend(-Item.TIME_TO_PICK_UP); //casting the spell already takes a turn
                            GLog.i(Messages.capitalize(Messages.get(hero, "you_now_have", item.name())));
                        } else {
                            GLog.w(Messages.capitalize(Messages.get(hero, "you_cant_have", item.name())));
                            h.sprite.drop();
                        }
                        callbacks.remove(this);
                        if (callbacks.isEmpty()) {
                            hero.spendAndNext(1f);
                        }
                    };
                    hero.sprite.parent.add(new Chains(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(h.pos), Effects.Type.RADIANT_GRAPPLER, callback));
                    Sample.INSTANCE.play(Assets.Sounds.CHAINS);
                    Sample.INSTANCE.play(Assets.Sounds.MISS);
                    callbacks.add(callback);
                }
            }
            hero.sprite.zap(hero.pos);
            hero.busy();
            onSpellCast(tome, hero);
        } else {
            super.onCast(tome, hero);
        }
    }

    @Override
    protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
        if (target == null)
            return;

        if (Dungeon.level.solid[target] || !Dungeon.level.visited[target]){
            GLog.w(Messages.get(this, "invalid_target"));
            return;
        }

        //chains cannot be used to go where it is impossible to walk to
        PathFinder.buildDistanceMap(target, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
        if (PathFinder.distance[hero.pos] == Integer.MAX_VALUE){
            GLog.w( Messages.get(EtherealChains.class, "cant_reach") );
            return;
        }

        final Ballistica chain = new Ballistica(hero.pos, target, Ballistica.FRIENDLY_PROJECTILE);

        boolean success = false;
        if (Actor.findChar( chain.collisionPos ) != null && Dungeon.hero.pointsInTalent(Talent.RADIANT_GRAPPLER) > 1){
            success = chainEnemy( chain, hero, Actor.findChar( chain.collisionPos ));
        } else if (Dungeon.level.heaps.get( chain.collisionPos ) != null) {
            success = chainItem( chain, hero, Dungeon.level.heaps.get( chain.collisionPos ) );
        } else if (Dungeon.level.traps.get( chain.collisionPos ) != null) {
            success = chainTrap( chain, hero, Dungeon.level.traps.get( chain.collisionPos ) );
        }
        else if (Dungeon.hero.pointsInTalent(Talent.RADIANT_GRAPPLER) > 1) {
            success = chainLocation( chain, hero );
        }
        if (success) {
            Sample.INSTANCE.play(Assets.Sounds.CHAINS);
            Sample.INSTANCE.play(Assets.Sounds.MISS);
            onSpellCast(tome, hero);
        }
    }

    //pulls an enemy to a position along the chain's path, as close to the hero as possible
    private static boolean chainEnemy(Ballistica chain, final Hero hero, final Char enemy ){

        if (enemy.properties().contains(Char.Property.IMMOVABLE)) {
            GLog.w( Messages.get(EtherealChains.class, "cant_pull") );
            return false;
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
            return false;
        }

        final int pulledPos = bestPos;

        hero.busy();
        hero.sprite.parent.add(new Chains(hero.sprite.center(), enemy.sprite.center(), Effects.Type.RADIANT_GRAPPLER, new Callback() {
            public void call() {
                Actor.add(new Pushing(enemy, enemy.pos, pulledPos, new Callback() {
                    public void call() {
                        Dungeon.level.occupyCell(enemy);
                    }
                }));
                enemy.pos = pulledPos;
                HolyFlames.proc(enemy);
                Dungeon.observe();
                GameScene.updateFog();
                hero.spendAndNext(1f);
            }
        }));
        return true;
    }

    //pulls the hero along the chain to the collisionPos, if possible.
    private static boolean chainLocation( Ballistica chain, final Hero hero ){

        //don't pull if rooted
        if (hero.rooted){
            GLog.w( Messages.get(EtherealChains.class, "rooted") );
            return false;
        }

        //don't pull if the collision spot is in a wall
        if (Dungeon.level.solid[chain.collisionPos]){
            GLog.i( Messages.get(EtherealChains.class, "inside_wall"));
            return false;
        }

        final int newHeroPos = chain.collisionPos;

        hero.busy();
        hero.sprite.parent.add(new Chains(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(newHeroPos), Effects.Type.RADIANT_GRAPPLER, new Callback() {
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
        return true;
    }

    //get an item at position of
    private static boolean chainItem( Ballistica chain, final Hero hero, Heap heap ){

        final int newHeroPos = chain.collisionPos;

        if (heap.type != com.zrp200.rkpd2.items.Heap.Type.HEAP){
            GLog.i( Messages.get(RadiantGrappler.class, "locked_item"));
            return false;
        }

        hero.busy();
        hero.sprite.parent.add(new Chains(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(newHeroPos), Effects.Type.RADIANT_GRAPPLER, new Callback() {
            public void call() {
                Item item = heap.peek();
                if (item.doPickUp( hero )) {
                    heap.pickUp();
                    hero.spend(-Item.TIME_TO_PICK_UP); //casting the spell already takes a turn

                    if (item instanceof Dewdrop
                            || item instanceof TimekeepersHourglass.sandBag
                            || item instanceof DriedRose.Petal
                            || item instanceof Key) {
                        //Do Nothing
                    } else {

                        //TODO make all unique items important? or just POS / SOU?
                        boolean important = item.unique && item.isIdentified() &&
                                (item instanceof Scroll || item instanceof Potion);
                        if (important) {
                            GLog.p( Messages.get(hero, "you_now_have", item.name()) );
                        } else {
                            GLog.i( Messages.get(hero, "you_now_have", item.name()) );
                        }
                    }
                } else {

                    if (item instanceof Dewdrop
                            || item instanceof TimekeepersHourglass.sandBag
                            || item instanceof DriedRose.Petal
                            || item instanceof Key) {
                        //Do Nothing
                    } else {
                        //TODO temporary until 0.8.0a, when all languages will get this phrase
                        if (Messages.lang() == Languages.ENGLISH) {
                            GLog.newLine();
                            GLog.n(Messages.get(this, "you_cant_have", item.name()));
                        }
                    }

                    heap.sprite.drop();
                }
            }
        }));
        return true;
    }

    //activate the trap at position
    private static boolean chainTrap( Ballistica chain, final Hero hero, Trap trap ){
        hero.busy();
        hero.sprite.parent.add(new Chains(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(chain.collisionPos), Effects.Type.RADIANT_GRAPPLER, new Callback() {
            public void call() {
                Dungeon.level.pressCell(chain.collisionPos);
                hero.spendAndNext(1f);
            }
        }));
        return true;
    }

    public static class GrapplerItem extends Item {
        {
            image = ItemSpriteSheet.GRAPPLER;
            defaultAction = AC_THROW;
            stackable = true;
        }

        @Override
        public boolean isUpgradable() {
            return false;
        }

        @Override
        public void doThrow(Hero hero) {
            GameScene.selectCell(caster);
        }

        private CellSelector.Listener caster = new CellSelector.Listener(){

            @Override
            public void onSelect(Integer target) {
                if (target == null)
                    return;

                if (Dungeon.level.solid[target] || !Dungeon.level.visited[target] || !Dungeon.level.mapped[target]){
                    GLog.w(Messages.get(RadiantGrappler.class, "invalid_target"));
                    return;
                }

                //chains cannot be used to go where it is impossible to walk to
                PathFinder.buildDistanceMap(target, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                if (PathFinder.distance[Dungeon.hero.pos] == Integer.MAX_VALUE){
                    GLog.w( Messages.get(EtherealChains.class, "cant_reach") );
                    return;
                }

                final Ballistica chain = new Ballistica(curUser.pos, target, Ballistica.FRIENDLY_PROJECTILE);

                boolean success;
                if (Actor.findChar( chain.collisionPos ) != null){
                    success = chainEnemy( chain, curUser, Actor.findChar( chain.collisionPos ));
                } else if (Dungeon.level.heaps.get( chain.collisionPos ) != null) {
                    success = chainItem( chain, curUser, Dungeon.level.heaps.get( chain.collisionPos ) );
                } else if (Dungeon.level.traps.get( chain.collisionPos ) != null) {
                    success = chainTrap( chain, curUser, Dungeon.level.traps.get( chain.collisionPos ) );
                }
                else {
                    success = chainLocation( chain, curUser );
                }
                if (success) {
                    throwSound();
                    detach(curUser.belongings.backpack);
                    updateQuickslot();

                    Sample.INSTANCE.play(Assets.Sounds.MISS);
                }

            }

            @Override
            public String prompt() {
                return Messages.get(EtherealChains.class, "prompt");
            }
        };

        @Override
        public boolean isIdentified() {
            return true;
        }

        @Override
        public int value() {
            return 10 * quantity;
        }
    }
}
