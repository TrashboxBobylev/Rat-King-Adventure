package com.zrp200.rkpd2.sprites;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.mobs.RatKingBoss;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.scenes.GameScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class RatKingBossSprite extends CharSprite {

    private int cellToAttack;

    public RatKingBossSprite() {
        super();

        texture( Assets.Sprites.RATBOSS );

        changeSprite(0);
    }

    public void changeSprite(int phase) {
        TextureFilm film = new TextureFilm( texture, 16, 17 );

        int offset = Math.max(0, phase*16);

        idle = new Animation( 2, true );
        idle.frames( film, offset+0, offset+0, offset+0, offset+1 );

        run = new Animation( 10, true );
        run.frames( film, offset+6, offset+7, offset+8, offset+9, offset+10 );

        attack = new Animation( 15, false );
        attack.frames( film, offset+2, offset+3, offset+4, offset+5, offset+0 );

        die = new Animation( 10, false );
        die.frames( film, offset+11,offset+12,offset+13,offset+14 );

        zap = attack.clone();

        operate = new Animation( 8, false );
        operate.frames( film, offset+2,offset+6,offset+2,offset+6);

        play( idle );

    }

    @Override
    public void link(Char ch) {
        super.link(ch);
        changeSprite(((RatKingBoss)ch).phase);
    }

    @Override
    public void update() {
        super.update();
        if (ch != null) {
            if (ch.HP < ch.HT * 0.5f){
                if (parent != null && aura == null) aura(0xFFFF00);
            }
            if (((RatKingBoss) ch).phase3()){
                hardlight(0xFFFF00);
            }
            ((GameScene) Game.scene()).tint.changeColor(phaseColor(
                    ((RatKingBoss) ch).phase
            ));
            if (Dungeon.hero.heroClass == HeroClass.RAT_KING){
                alpha(0.5f);
            }
        }
    }

    public void zap( int cell ) {
        turnTo( ch.pos, cell );
        play( zap );
        cellToAttack = cell;
    }

    @Override
    public void onComplete( Animation anim ) {
        if (anim == zap && ch != null && ((RatKingBoss)ch).phase == RatKingBoss.SNIPER) {
            idle();

            parent.recycle( MissileSprite.class ).
                    reset( this, cellToAttack, new ScorpioShot(), new Callback() {
                        @Override
                        public void call() {
                            ((RatKingBoss)ch).onZapComplete();
                        }
                    } );
        } else {
            super.onComplete( anim );
        }
    }

    public class ScorpioShot extends Item {
        {
            image = ItemSpriteSheet.SR_RANGED;
        }
    }

    public static int phaseColor(int phase){
        switch (phase){
            case RatKingBoss.GLADIATOR:
                return 0xff667f;
            case RatKingBoss.BATTLEMAGE:
                return 0x5ce6cf;
            case RatKingBoss.ASSASSIN:
                return 0x060803;
            case RatKingBoss.SNIPER:
                return 0xb8e65c;
            default:
                return 0x000000;
        }
    }


}
