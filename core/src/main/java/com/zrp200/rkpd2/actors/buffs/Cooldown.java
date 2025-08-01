package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.messages.Messages;
import com.watabou.utils.GameMath;

import static com.zrp200.rkpd2.Dungeon.hero;

public abstract class Cooldown extends FlavourBuff {
    public static <T extends Cooldown> T affectHero(Class<T> cls) {
        T buff = Buff.affect(hero, cls);
        buff.postpone(buff.duration());
        return buff;
    }

    public abstract float duration();

    public float iconFadePercent() {
        return GameMath.gate(0, visualcooldown() / duration(), 1);
    }

    public String toString() {
        return Messages.get(this, "name");
    }

    public String desc() {
        return Messages.get(this, "desc", dispTurns(visualcooldown()));
    }
}
