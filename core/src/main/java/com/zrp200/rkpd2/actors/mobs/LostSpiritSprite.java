package com.zrp200.rkpd2.actors.mobs;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class LostSpiritSprite extends MobSprite {

    private float phase;
    private boolean glowUp;
    private ItemSprite.Glowing glowing;

    public LostSpiritSprite() {
        super();

        texture( Assets.Sprites.ATTUNEMENT_SPIRIT );

        TextureFilm frames = new TextureFilm( texture, 12, 14 );

        idle = new MovieClip.Animation( 10, true );
        idle.frames( frames, 0, 0, 0, 0, 0, 0,0, 1, 2 );

        run = new MovieClip.Animation( 16, true );
        run.frames( frames, 0, 3 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 4, 5, 6 );

        zap = attack.clone();

        die = new MovieClip.Animation( 15, false );
        die.frames( frames, 7, 8, 9, 10, 11, 12, 13, 12 );

        play( idle );
    }

    public void zap( int cell ) {

        turnTo( ch.pos , cell );
        play( zap );

        MagicMissile.boltFromChar( parent,
                MagicMissile.RAINBOW,
                this,
                cell,
                new Callback() {
                    @Override
                    public void call() {
                        ((LostSpirit)ch).onZapComplete();
                    }
                } );
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
    }

    @Override
    public void onComplete( MovieClip.Animation anim ) {
        if (anim == zap) {
            idle();
        }
        super.onComplete( anim );
    }

    @Override
    public int blood() {
        return 0xFFFFFFFF;
    }

    @Override
    public void link( Char ch ) {
        super.link( ch );
        add( State.SPIRIT );
    }

    @Override
    public void die() {
        super.die();
        remove( State.SPIRIT );
    }

    @Override
    public void update() {
        super.update();
        if (glowing == null) glowing = new ItemSprite.Glowing();
        glowing.period = 0.66f;
        if (visible) {
            if (glowUp && (phase += Game.elapsed) > glowing.period) {

                glowUp = false;
                phase = glowing.period;

            } else if (!glowUp && (phase -= Game.elapsed) < 0) {

                glowUp = true;
                phase = 0;

            }

            float value = phase / glowing.period * 0.9f;
            glowing.tempColor.fromHsv(Random.Float(360), Random.Float(0.15f, 0.5f), 0.9f);
            glowing.red = glowing.tempColor.r;
            glowing.blue = glowing.tempColor.b;
            glowing.green = glowing.tempColor.g;

            rm = gm = bm = 1 - value;
            ra = glowing.red * value;
            ga = glowing.green * value;
            ba = glowing.blue * value;
        }
    }
}
