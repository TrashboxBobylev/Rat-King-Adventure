/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 *  Shattered Pixel Dungeon
 *  Copyright (C) 2014-2022 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2022 TrashboxBobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;

//same deal as Shrink, but has duration
public class TimedShrink extends FlavourBuff {

	public static final float DURATION = 20f;

	{
		type = buffType.POSITIVE;
		announced = true;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.MOMENTUM;
	}
	
	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

    @Override
    public void tintIcon(Image icon) {
        icon.tint(0.5f, 0, 1, 0.75f);
    }

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

    @Override
    public void fx(boolean on) {
        if (on) target.sprite.add(CharSprite.State.SHRUNK);
        else target.sprite.remove(CharSprite.State.SHRUNK);
    }

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}
}
