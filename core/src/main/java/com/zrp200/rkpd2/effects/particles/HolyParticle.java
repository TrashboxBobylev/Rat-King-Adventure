package com.zrp200.rkpd2.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class HolyParticle extends PixelParticle {
    public static final Emitter.Factory FACTORY = new Emitter.Factory() {
        @Override
        public void emit( Emitter emitter, int index, float x, float y ) {
            emitter.recycle( HolyParticle.class ).reset( x, y );
        }
        @Override
        public boolean lightMode() {
            return true;
        }
    };

    public HolyParticle() {
        super();

        lifespan = 0.4f;
        color( ColorMath.random(0x7F7F00, 0xFFFF15 ) );
        acc.set( 0, -55 );
    }

    private float offs;

    public void reset( float x, float y ) {
        revive();

        this.x = x;
        this.y = y * 1.025f;

        offs = -Random.Float( lifespan );
        left = lifespan - offs;
        speed.set( 0 );
    }

    @Override
    public void update() {
        super.update();

        float p = left / lifespan;
        am = p > 0.8f ? (1 - p) * 5 : 1;
        scale.x = (1 - p) * 3f;
        scale.y = 8 + (1 - p) * 10;
    }
}
