package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.messages.Messages;
import com.watabou.utils.Bundle;

public abstract class DummyBuff extends FlavourBuff {
    {
        type = buffType.POSITIVE;
        announced = true;
    }

    public int duration;

    private static final String DURATION = "duration";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(DURATION, duration);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        duration = bundle.getInt(DURATION);
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (duration - visualcooldown()) / duration);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns());
    }

    @Override
    public void spend(float time) {
        super.spend(time);
        duration = (int) cooldown();
    }
}
