package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class TrappetSprite extends MobSprite {

    public TrappetSprite() {
        super();

        texture( Assets.Sprites.TRAPPET );

        TextureFilm film = new TextureFilm( texture, 12, 15 );

        idle = new MovieClip.Animation( 1, true );
        idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new MovieClip.Animation( 20, true );
        run.frames( film, 2, 3, 4, 5, 6, 7 );

        die = new MovieClip.Animation( 20, false );
        die.frames( film, 8, 9, 10, 11, 12, 11 );

        attack = new MovieClip.Animation( 15, false );
        attack.frames( film, 13, 14, 15, 0 );

        zap = attack.clone();

        play( idle );
    }

    public void zap( int cell ) {

        turnTo( ch.pos , cell );
        play( zap );

        MagicMissile.boltFromChar( parent,
                MagicMissile.ELMO,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((Trappet)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play( Assets.Sounds.ATK_SPIRITBOW );
    }

    @Override
    public void onComplete( MovieClip.Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }
}
