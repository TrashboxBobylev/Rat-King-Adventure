package com.zrp200.rkpd2.items.weapon;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.PowerfulDegrade;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.ThrowingStone;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public class Slingshot extends Weapon {

    private static final int MAX_VOLUME = 1;
    private static final String AC_SHOOT = "SHOOT";
    public int charge;

    {
        image = ItemSpriteSheet.KROMER_SLINGSHOT;
        defaultAction = AC_SHOOT;
        usesTargeting = true;
        levelKnown = true;
        bones = false;
        Stone.slingshot = this;
    }

    @Override
    public int STRReq() {
        return Dungeon.hero.STR();
    }

    @Override
    public int STRReq(int lvl) {
        return STRReq();
    }

    @Override
    public int min(int lvl) {
        return (STRReq() - 10)*7 + 10 + (curseInfusionBonus ? 1 : 0);
    }

    @Override
    public int max(int lvl) {
        return 12 + (STRReq() - 10)*7 + (curseInfusionBonus ? 2 : 0);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.remove(AC_EQUIP);
        actions.add(AC_SHOOT);
        return actions;
    }

    @Override
    protected void onThrow(int cell) {
        Stone.slingshot = null;
        super.onThrow(cell);
    }

    @Override
    public boolean doPickUp(Hero hero) {
        Stone.slingshot = this;
        return super.doPickUp(hero);
    }

    @Override
    public void doDrop(Hero hero) {
        Stone.slingshot = null;
        super.doDrop(hero);
    }

    @Override
    public String status() {
        return Messages.format( "%d/%d", charge, MAX_VOLUME );
    }

    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (action.equals(AC_SHOOT)) {

            curUser = hero;
            curItem = this;
            GameScene.selectCell( shooter );

        }
    }

    private static final String VOLUME	= "volume";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( VOLUME, charge );
        levelKnown = true;
        cursedKnown = true;
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        charge	= bundle.getInt( VOLUME );
        levelKnown = true;
        cursedKnown = true;
        level(1);
        Stone.slingshot = this;
    }

    @Override
    public int level() {
        if (Dungeon.hero.buff(PowerfulDegrade.class) != null) return 0;
        int i = (Dungeon.hero != null ) ? Dungeon.hero.STR() - 10 : 0;
        return i;
    }

    @Override
    public int damageRoll(Char owner) {
        return augment.damageFactor(super.damageRoll(owner));
    }

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target ) {
            if (target != null) {
                if (charge > 0) {
                    final Stone stone = new Stone();
                    target = new Ballistica( curUser.pos, target, Ballistica.PROJECTILE ).collisionPos;
                    charge -= 1;
                    updateQuickslot();
                    curUser.sprite.zap(target);
                    final float delay = baseDelay( curUser );
                    final int cell = target;
                    curUser.sprite.parent.recycle(MissileSprite.class).
                            reset(curUser.sprite,
                                    target,
                                    stone,
                                    new Callback() {
                                        @Override
                                        public void call() {
                                            curUser.spendAndNext(delay);
                                            stone.onThrow(cell);
                                        }
                                    });
                } else if (charge == 0) {
                    Messages.get(Slingshot.class, "no_charge");
                }
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };

    @Override
    public String info() {
        String info = desc();

        info += "\n\n" + Messages.get( Slingshot.class, "stats",
                Math.round(augment.damageFactor((STRReq() - 10)*7 + 11 + (curseInfusionBonus ? 1 : 0))),
                STRReq());

        switch (augment) {
            case SPEED:
                info += "\n\n" + Messages.get(Weapon.class, "faster");
                break;
            case DAMAGE:
                info += "\n\n" + Messages.get(Weapon.class, "stronger");
                break;
            case NONE:
        }

        if (enchantment != null && (cursedKnown || !enchantment.curse())){
            info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
            info += " " + Messages.get(enchantment, "desc");
        }

        if (cursed && isEquipped( Dungeon.hero )) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
        } else if (cursedKnown && cursed) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed");
        } else if (!isIdentified() && cursedKnown){
            info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
        }

        info += "\n\n" + Messages.get(MissileWeapon.class, "distance");

        return info;
    }

    public void collectStone(int quantity) {
        charge += quantity;
        if (charge >= MAX_VOLUME) {
            charge = MAX_VOLUME;
        }

        updateQuickslot();
    }

    public boolean isFull() {
        return charge >= MAX_VOLUME;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    public static class Stone extends MissileWeapon {
        {
            image = ItemSpriteSheet.THROWING_STONE;
            sticky = false;
            unique = true;
            hitSound = Assets.Sounds.HIT_CRUSH;
        }

        @Override
        public float durabilityPerUse() {
            return 0;
        }

        @Override
        public String info() {
            return desc();
        }

        private static Slingshot slingshot;

        @Override
        public int damageRoll(Char owner) {
            if (slingshot != null) return slingshot.damageRoll( owner);
            else return 0;
        }

        @Override
        public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
            if (slingshot != null) return slingshot.hasEnchant(type, owner);
            else return false;
        }

        @Override
        public int proc(Char attacker, Char defender, int damage) {
            Warp.inflict(17, 1.5f);

            if (slingshot != null) return slingshot.proc(attacker, defender, damage);
            else return damage;
        }

        @Override
        public float baseDelay(Char user) {
            if (slingshot != null) return slingshot.baseDelay(user);
            else return 1f;
        }

        @Override
        public int STRReq(int lvl) {
            if (slingshot != null)return slingshot.STRReq(lvl);
            else return 10;
        }


        @Override
        public boolean doPickUp( Hero hero ) {
            slingshot = hero.belongings.getItem(Slingshot.class);

            if (slingshot != null) {
                if (!slingshot.isFull()) {

                    slingshot.collectStone(quantity);

                } else {

                    GLog.i(Messages.get(this, "already_full"));
                    return false;

                }
            } else {

                GLog.i(Messages.get(this, "cant_pickup"));
                return false;

            }

            GameScene.pickUp( this, hero.pos );
            Sample.INSTANCE.play( Assets.Sounds.DEWDROP );
            hero.spendAndNext( TIME_TO_PICK_UP );

            return true;
        }
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipeBundled {

        {
            inputs =  new Class[]{ThrowingStone.class, Kromer.class};
            inQuantity = new int[]{1, 1};

            cost = 20;

            output = Slingshot.class;
            outQuantity = 1;
        }

        @Override
        public Item sampleOutput(ArrayList<Item> ingredients) {
            Slingshot item = (Slingshot) super.sampleOutput(ingredients);
            item.charge = 1;
            return item;
        }
    }
}
