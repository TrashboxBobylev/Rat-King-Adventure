package com.zrp200.rkpd2.items.weapon.missiles.darts;

import com.zrp200.rkpd2.actors.Char;

public interface CrossbowAmmo {
    void updateCrossbow();

    boolean crossbowHasEnchant( Char owner );
}
