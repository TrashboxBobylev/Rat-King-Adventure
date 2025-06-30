package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.Beam;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class EnrageSpell extends ClericSpell {
    public static final EnrageSpell INSTANCE = new EnrageSpell();

    @Override
    public int icon() {
        return HeroIcon.ENRAGE;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero)
                && hero.hasTalent(Talent.ENRAGE)
                && PowerOfMany.getPoweredAlly() != null;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 3;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", EnrageBuff.duration()) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        onSpellCast(tome, hero);

        Char ally = PowerOfMany.getPoweredAlly();

        if (ally != null) {
            hero.sprite.zap(ally.pos);
            hero.sprite.parent.add(
                    new Beam.HealthRay(hero.sprite.center(), ally.sprite.center()));
        } else {
            ally = Stasis.getStasisAlly();
            hero.sprite.operate(hero.pos);
            hero.sprite.parent.add(
                    new Beam.HealthRay(DungeonTilemap.tileCenterToWorld(hero.pos), hero.sprite.center()));
        }

        Buff.prolong(ally, EnrageBuff.class, EnrageBuff.duration());
        Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );

        if (SpellEmpower.isActive()) {
            GLog.p(Messages.get(SpellEmpower.class, "instant"));
            hero.next();
        } else {
            hero.spendAndNext(Actor.TICK);
        }

        onSpellCast(tome, hero);
    }

    public static class EnrageBuff extends FlavourBuff {
        public static int duration(){
            return Dungeon.hero.pointsInTalent(Talent.ENRAGE)*4;
        }

        {
            type = buffType.POSITIVE;
            announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.RAGE;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (duration() - visualcooldown()) / duration());
        }
    }
}
