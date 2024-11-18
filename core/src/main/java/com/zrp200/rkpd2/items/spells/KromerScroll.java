package com.zrp200.rkpd2.items.spells;

import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.scrolls.ScrollOfUpgrade;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.traps.DistortionTrap;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class KromerScroll extends Spell{
    {
        image = ItemSpriteSheet.EXOTIC_KROMER;
    }

    private static final ItemSprite.Glowing CHAOTIC = new ItemSprite.Glowing( 0.2f  );

    @Override
    public ItemSprite.Glowing glowing() {
        return CHAOTIC;
    }

    @Override
    public int value() {
        return quantity * Random.Int(6, 1341);
    }

    @Override
    protected void onCast(Hero hero) {
        Warp.inflict(150, 10f);
        for (int i = 0; i < Dungeon.level.length(); i++){
            if (Dungeon.level.passable[i] && !Dungeon.level.heroFOV[i]){
                boolean doors = false;
                for (int l : PathFinder.NEIGHBOURS8){
                    if (Dungeon.level.insideMap(i + l) && Dungeon.level.map[i + l] == Terrain.DOOR){
                        doors = true;
                    }
                }
                if (!doors)
                    new DistortionTrap().set(i).activate();
            }
        }
        for (Item item : Dungeon.hero.belongings){
            if (item.isUpgradable()){
                if (item instanceof MissileWeapon){
                    item.level(item.level()+1);
                } else if (item instanceof Weapon && ((Weapon) item).enchantment != null) {
                    ((Weapon) item).upgrade(true);
                } else if (item instanceof Armor && ((Armor) item).glyph != null) {
                    ((Armor) item).upgrade(true);
                } else {
                    item.upgrade();
                }
            }
        }
        detach( curUser.belongings.backpack );
        updateQuickslot();
        hero.spendAndNext( 1f );
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{ScrollOfUpgrade.class, Kromer.class};
            inQuantity = new int[]{1, 1};

            cost = Random.Int(2, 20);

            output = KromerScroll.class;
            outQuantity = 1;
        }

    }
}
