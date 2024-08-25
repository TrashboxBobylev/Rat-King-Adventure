package com.zrp200.rkpd2.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class VineParticle extends PixelParticle {

    public static final Emitter.Factory FACTORY = new Emitter.Factory() {
        @Override
        public void emit( Emitter emitter, int index, float x, float y ) {
            emitter.recycle( VineParticle.class ).reset( x, y );
        }
        @Override
        public boolean lightMode() {
            return true;
        }
    };

    public VineParticle() {
        super();

        lifespan = 0.8f;
        color( ColorMath.random( 0x004400, 0x88CC44 ) );
        speed.set( 0, -1 );
    }

    private float offs;

    public void reset( float x, float y ) {
        revive();

        this.x = x;
        this.y = y;

        offs = -Random.Float( lifespan );
        left = lifespan - offs;
    }

    @Override
    public void update() {
        super.update();

        float p = left / lifespan;
        am = p < 0.5f ? p : 1 - p;
        scale.x = (1 - p) * 5.5f;
        scale.y = 9 + (1 - p) * 9;
    }
}
