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

package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class FinalFroggitSprite extends MobSprite {

	public FinalFroggitSprite() {
		super();

		texture( Assets.Sprites.FINAL_FROGGIT );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new MovieClip.Animation( 10, true );
		idle.frames( frames, 0, 1, 0 );

		run = new MovieClip.Animation( 15, true );
		run.frames( frames, 2, 3, 4 );

		die = new MovieClip.Animation( 10, false );
		die.frames( frames, 5, 6, 7, 8 );

		attack = new MovieClip.Animation( 10, false );
		attack.frames( frames, 9, 10, 11 );

		zap = attack.clone();

		play(idle);
	}

	@Override
	public int blood() {
		return 0xFF808080;
	}

    public void zap( int cell ) {

        turnTo( ch.pos , cell );
        play( zap );

        MagicMissile.boltFromChar( parent,
                MagicMissile.FROGGIT,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((FinalFroggit)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ZAP );
    }

    @Override
    public void onComplete( MovieClip.Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }
}
