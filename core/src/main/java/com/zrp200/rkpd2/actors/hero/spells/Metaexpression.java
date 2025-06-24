package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.trinkets.Trinket;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.Arrays;
import java.util.List;

public class Metaexpression extends ClericSpell {

    public static final Metaexpression INSTANCE = new Metaexpression();

    public static final int DURATION = 100;

    @Override
    public int icon() {
        return HeroIcon.METAEXPRESSION;
    }

    @Override
    public void tintIcon(HeroIcon icon) {
        // todo actual icon
        if (SpellEmpower.isActive()) icon.tint(0, .33f);
    }

    @Override
    public float chargeUse(Hero hero) {
        return 1 + hero.pointsInTalent(Talent.METAEXPRESSION);
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.METAEXPRESSION);
    }

    @Override
    protected List<Object> getDescArgs() {
        int talentLvl = Dungeon.hero.pointsInTalent(Talent.METAEXPRESSION);
        if (SpellEmpower.isActive())
            talentLvl += 2;
        return Arrays.asList(talentLvl, DURATION);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        Trinket coolTrinket = (Trinket) Generator.random(Generator.Category.TRINKET);
        coolTrinket.level(hero.pointsInTalent(Talent.METAEXPRESSION));
        if (SpellEmpower.isActive()) {
            coolTrinket.upgrade(2);
        }
        coolTrinket.identify();
        Buff.detach(hero, TrinketHolder.class);
        TrinketHolder holder = Buff.affect(hero, TrinketHolder.class, DURATION);
        holder.trinket = coolTrinket;

        Enchanting.show(hero, coolTrinket);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        GLog.p(Messages.get(this, "trinket", Messages.titleCase(coolTrinket.title())));

        hero.spend( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);

        onSpellCast(tome, hero);
    }

    public static class TrinketHolder extends FlavourBuff {

        {
            type = buffType.POSITIVE;
        }

        public Trinket trinket;

        @Override
        public int icon() {
            return BuffIndicator.TRINKETHOLD;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

        private static final String TRINKET = "trinket";

        @Override
        public String desc() {
            return Messages.get(this, "desc", Messages.titleCase(trinket.title()), trinket.statsDesc(), dispTurns());
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(TRINKET, trinket);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            trinket = (Trinket) bundle.get(TRINKET);
        }
    }
}
