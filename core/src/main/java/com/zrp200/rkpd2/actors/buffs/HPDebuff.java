package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class HPDebuff extends CounterBuff {

    {
        type = buffType.NEGATIVE;
    }

    protected float left = 0;

    private static final String LEFT	= "left";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( LEFT, left );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        left = bundle.getFloat( LEFT );
        if (target instanceof Hero) ((Hero) target).updateHT(false);
    }

    @Override
    public void countUp(float inc) {
        super.countUp(inc);
        if (left == 0)
            left += 7;
        else
            left += 4;
    }

    @Override
    public void detach() {
        countDown(count());
        Dungeon.hero.updateHT(false);
        super.detach();
    }

    @Override
    public int icon() {
        return BuffIndicator.HERB_HEALING;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0xff1a1a);
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", (int)count(), dispTurns(left));
    }

    @Override
    public boolean act() {
        if (target.isAlive()) {

            spend( TICK );
            Dungeon.hero.updateHT(false);

            if ((left -= TICK) <= 0) {
                detach();
            }

        } else {

            detach();

        }

        return true;
    }
}
