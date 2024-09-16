package com.zrp200.rkpd2.actors.buffs;

import com.watabou.noosa.Image;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.mobs.Wraith;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.WraithSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;

public class SpiritBuff extends CounterBuff implements ActionIndicator.Action {

    private final int MAXCHARGE = 45;

    {
        revivePersists = true;
    }

    @Override
    public boolean act() {
        if (count() < MAXCHARGE*2){
            countUp(1);
        }
        if (count() >= MAXCHARGE){
            ActionIndicator.setAction(this);
        }

        spend(TICK);
        return true;
    }

    @Override
    public boolean attachTo(Char target) {
        if (count() >= MAXCHARGE){
            ActionIndicator.setAction(this);
        }
        return super.attachTo(target);
    }

    @Override
    public boolean usable() {
        return count() >= MAXCHARGE;
    }

    @Override
    public int icon() {
        return BuffIndicator.CORRUPT;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }

    @Override
    public void tintIcon(Image icon) {
        if (count() >= MAXCHARGE) icon.hardlight(0xa9a9a9);
    }

    @Override
    public float iconFadePercent() {
        return 1f - (Math.min(count(), Math.max(0, count()-MAXCHARGE))/ (MAXCHARGE));
    }

    @Override
    public Image primaryVisual() {
        return new WraithSprite();
    }

    @Override
    public int indicatorColor() {
        return 0xB0B0B0;
    }

    @Override
    public void doAction() {
        int pos;
        int tries = 20;
        do{
            pos = Dungeon.hero.pos + PathFinder.NEIGHBOURS8[Random.Int(PathFinder.NEIGHBOURS8.length)];
            tries --;
        } while (tries > 0 && (!Dungeon.level.heroFOV[pos] || Dungeon.level.solid[pos] || Actor.findChar( pos ) != null));
        if ((!Dungeon.level.solid[pos] || Dungeon.level.passable[pos]) && Actor.findChar( pos ) == null) {

            Wraith w = new Wraith();
            w.adjustStats(Dungeon.scalingDepth());
            w.pos = pos;
            w.state = w.HUNTING;
            GameScene.add( w, 1f);
            Dungeon.level.occupyCell(w);

            w.sprite.alpha( 0 );
            w.sprite.parent.add( new AlphaTweener( w.sprite, 1, 0.5f ) );

            w.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
            Buff.affect(w, WraithMark.class);
            countDown(MAXCHARGE);
            if (count() < MAXCHARGE)
                ActionIndicator.clearAction(this);
            BuffIndicator.refreshHero();
        }
    }

    public static class WraithMark extends DLCAllyBuff {};
}
