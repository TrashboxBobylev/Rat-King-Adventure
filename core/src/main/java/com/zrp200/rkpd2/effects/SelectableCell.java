package com.zrp200.rkpd2.effects;

import com.zrp200.rkpd2.ui.Icons;
import com.watabou.noosa.Image;

public class SelectableCell extends Image {
    public SelectableCell(Image sprite) {
        super(Icons.get(Icons.TARGET));
        point( sprite.center(this) );
        sprite.parent.addToFront(this);
    }
}
