package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.particles.Emitter;

public class RobotTransform extends Buff {
    @Override
    public boolean attachTo(Char target) {
        target.flying = true;
        return super.attachTo(target);
    }

    @Override
    public void detach() {
        target.flying = false;
        super.detach();
    }

    public static class RunicMissile extends Item {
        {
            image = ItemSpriteSheet.WAND_PRISMATIC_LIGHT;
        }

        @Override
        public Emitter emitter() {
            Emitter e = new Emitter();
            e.pos(7.5f, 7.5f);
            e.fillTarget = true;
            e.autoKill = false;
            e.pour(MagicMissile.WhiteParticle.FACTORY, 0.0075f);
            return e;
        }
    }
}
