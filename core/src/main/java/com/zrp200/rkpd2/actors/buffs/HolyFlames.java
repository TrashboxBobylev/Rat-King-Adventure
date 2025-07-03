package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.Brute;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

public class HolyFlames extends Buff implements Hero.Doom, DamageOverTimeEffect {

    public static final float DURATION = 8f;

    public float left;
    private int burnIncrement = 0; //for tracking burning of hero items

    private static final String LEFT	= "left";
    private static final String BURN	= "burnIncrement";

    {
        type = buffType.NEGATIVE;
        actPriority = BUFF_PRIO - 1;
        announced = true;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( LEFT, left );
        bundle.put( BURN, burnIncrement );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        left = bundle.getFloat( LEFT );
        burnIncrement = bundle.getInt( BURN );
    }

    @Override
    public boolean attachTo(Char target) {
        Buff.detach( target, Chill.class);
        for (Buff b: target.buffs()){
            if (b.type == buffType.POSITIVE && !(b instanceof Brute.BruteRage))
                b.detach();
        }

        return super.attachTo(target);
    }

    @Override
    public boolean act() {

        if (target.isAlive() && !target.isImmune(getClass())) {
            float damage = Random.NormalIntRange( 1, 3 + Dungeon.scalingDepth()/4 );
            damage += burnIncrement;
            HashSet<Char.Property> props = target.properties();
            if (props.contains(Char.Property.DEMONIC) || props.contains(Char.Property.UNDEAD))
                damage *= 1.5f;
            damage *= (1 + Dungeon.hero.pointsInTalent(Talent.PYROMANIAC, Talent.RK_FIRE)*0.125f);

            burnIncrement++;

            Buff.detach( target, Chill.class);
            for (Buff b: target.buffs()){
                if (b.type == buffType.POSITIVE && !(b instanceof Brute.BruteRage))
                    b.detach();
            }

            target.damage((int) damage, this );

        } else {
            detach();
        }

        spend( TICK );
        left -= TICK;

        if (left <= 0) {
            detach();
        }

        return true;
    }

    public void reignite( ) {
        reignite(2 + Dungeon.hero.pointsInTalent(Talent.EXORCISM)*2 );
    }

    public void reignite( float duration ) {
        if (left < duration) left = duration;
    }

    public static void proc(Char ch){
        if (Dungeon.hero.hasTalent(Talent.EXORCISM) && ch.alignment == Char.Alignment.ENEMY)
            Buff.affect(ch, HolyFlames.class).reignite();
    }

    public void extend( float duration ) {
        left += duration;
    }

    @Override
    public int icon() {
        return BuffIndicator.HOLYFLAMES;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - left) / DURATION);
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString((int)left);
    }

    @Override
    public void fx(boolean on) {
        if (on) target.sprite.add(CharSprite.State.HOLYBURNING);
        else target.sprite.remove(CharSprite.State.HOLYBURNING);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns(left));
    }

    @Override
    public void onDeath() {

        Badges.validateDeathFromFire();

        Dungeon.fail( this );
        GLog.n( Messages.get(this, "ondeath") );
    }
}
