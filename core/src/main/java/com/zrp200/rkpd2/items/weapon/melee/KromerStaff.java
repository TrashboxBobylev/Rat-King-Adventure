package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.utils.Bundle;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class KromerStaff extends MagesStaff {

    {
        image = ItemSpriteSheet.KROMER_STAFF;
        tier = 2;
    }

    public KromerStaff(){
        wand = null;
    }

    public KromerStaff(Wand wand){
        super(wand);
    }

    @Override
    public void updateWand(boolean levelled){
        if (wand != null) {
            int curCharges = wand.curCharges;
            wand.level((int) (level()));
            //gives the wand one additional max charge
            wand.maxCharges = Math.min(wand.maxCharges*2, 20);
            wand.curCharges = Math.min(curCharges + (levelled ? 2 : 0), wand.maxCharges);
            updateQuickslot();
        }
    }

    @Override
    public int min(int lvl) {
        return  tier*2 +  //4, up from 2
                lvl*(tier);    //+2, up from +1
    }

    @Override
    public int max(int lvl) {
        return  Math.round(3.5f*(tier+1)) +   //10.5 base damage, down from 15
                lvl*(tier+2);               //+4, up from +3
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        kromerProc();
        return super.proc(attacker, defender, damage);
    }

    public void kromerProc() {
        Warp.inflict(3, 1);
    }

    public void procWand(Char defender, int damage) {
        wand.onHit(this, Dungeon.hero,defender, (int) (damage*1.5f));
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (wand != null) {
            wand.maxCharges = Math.min(wand.maxCharges*2, 20);
        }
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipeBundled {

        {
            inputs =  new Class[]{MagesStaff.class, Kromer.class};
            inQuantity = new int[]{1, 1};

            cost = 20;

            output = KromerStaff.class;
            outQuantity = 1;
        }

    }
}
