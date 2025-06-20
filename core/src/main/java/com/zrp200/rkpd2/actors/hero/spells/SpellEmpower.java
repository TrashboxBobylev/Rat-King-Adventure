package com.zrp200.rkpd2.actors.hero.spells;

import static com.zrp200.rkpd2.Dungeon.hero;

import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.GameMath;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Cooldown;
import com.zrp200.rkpd2.actors.buffs.CounterBuff;
import com.zrp200.rkpd2.actors.buffs.Recharging;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SpellEmpower extends ClericSpell {

    public static void useCharge(float charge) {
        SpellEmpower.Buff empower = hero.virtualBuff(SpellEmpower.Buff.class);
        if (empower == null) return;
        empower.countUp(charge);
        if (empower.count() >= SpellEmpower.limit()) {
            empower.detach();
        }
    }

    public static boolean isActive() {
        return hero != null && hero.virtualBuff(SpellEmpower.Buff.class) != null;
    }

    @Override
    public boolean ignoreChargeUse(HolyTome holyTome) {
        // divine advent and critical limit break ignore charges
        // as long as the tome is in a "legal" state
        return holyTome != null && holyTome.getCharges() >= 0;
    }

    public static float limit() {
        return Math.max(
                1 << hero.pointsInTalent(Talent.LIMIT_BREAK),
                2 + hero.pointsInTalent(Talent.DIVINE_ADVENT)
        );
    }

    public static float left() {
        Buff buff = hero.virtualBuff(Buff.class);
        return buff != null ? buff.left() : 0;
    }

    protected Talent talent;

    @Override
    public boolean isVisible(Hero hero) {
        return hero.hasTalent(talent);
    }

    @Override
    public boolean canCast(Hero hero) {
        return isVisible(hero) && !isActive() && hero.virtualBuff(SpellCooldown.class) == null;
    }

    @Override
    public String desc() {
        String desc = super.desc();
        HolyTome t = hero.belongings.getItem(HolyTome.class);
        if (t != null && t.isDepleted()) desc += "\n\n" + Messages.h(Messages.get(this, "depleted"));
        return desc;
    }

    @Override
    protected List<Object> getDescArgs() {
        return Collections.singletonList(
                (int)SpellEmpower.limit()
        );
    }

    @Override
    public abstract float chargeUse(Hero hero);
    protected abstract void applyBuff(Hero hero);

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        onSpellCast(tome, hero);
        ScrollOfRecharging.charge(hero);
        Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
        applyBuff(hero);
    }

    public static class DivineAdvent extends SpellEmpower {

        public static final DivineAdvent INSTANCE = new DivineAdvent();

        {
            talent = Talent.DIVINE_ADVENT;
        }

        protected void applyBuff(Hero hero) {
            // 4 / 6 / 8
            Buff.append(hero, Recharging.class, 2 * (1 + hero.pointsInTalent(talent)));
            Buff.affect(hero, Buff.class);
        }

        @Override
        public int icon() { return HeroIcon.DIVINE_ADVENT; }

        @Override
        public float chargeUse(Hero hero) {
            return 8 - hero.pointsInTalent(talent);
        }

        public static class Buff extends SpellEmpower.Buff {

            @Override
            public int icon() { return BuffIndicator.DIVINE_ADVENT; }

            @Override
            public float getTurnsPerCharge() {
                return 15;
            }

            @Override
            public void detach() {
                super.detach();
                // cooldown starts after finish
                Cooldown.affectHero(Cooldown.class);
            }
        }

        public static class Cooldown extends SpellCooldown {
            @Override
            public int icon() { return BuffIndicator.DIVINE_ADVENT; }
            public float duration() { return 100; }
        }
    }

    public static class LimitBreak extends SpellEmpower {
        public static final LimitBreak INSTANCE = new LimitBreak();

        @Override
        public void tintIcon(HeroIcon icon) {
            if (hero != null && canCast(hero) && ignoreChargeUse(hero.belongings.getItem(HolyTome.class))) {
                icon.tint(1, 0, 0, 0.4f);
            }
        }

        @Override
        public int icon() { return HeroIcon.LIMIT_BREAK; }

        {
            talent = Talent.LIMIT_BREAK;
        }

        @Override
        public boolean ignoreChargeUse(HolyTome tome) {
            return hero.isNearDeath() && super.ignoreChargeUse(tome);
        }

        @Override
        public String desc() {
            String desc = super.desc();
            HolyTome tome = hero.belongings.getItem(HolyTome.class);
            if (tome == null || !tome.isDepleted()) desc += "\n\n" + criticalDesc();
            return desc;
        }

        private String criticalDesc() {
            return Messages.get(this, hero.isNearDeath() ? "critical" : "not_critical");
        }

        // limit break's countdown starts immediately, but it doesn't last as long
        @Override
        protected List<Object> getDescArgs() {
            List<Object> args = new ArrayList(super.getDescArgs());
            args.add(hero.isNearDeath() ? 300 : 150);
            return args;
        }

        @Override
        protected void applyBuff(Hero hero) {
            Buff.affect(hero, Buff.class);
            Cooldown cd = Cooldown.affectHero(Cooldown.class);
            if (hero.isNearDeath()) cd.spend(-150);
            cd.spend(-1); // offset the instant cast
        }

        @Override
        public float chargeUse(Hero hero) {
            return hero.isNearDeath() ? 2 : 4;
        }

        public static class Buff extends SpellEmpower.Buff {
            @Override
            public int icon() { return BuffIndicator.LIMIT_BREAK; }

            @Override
            public String desc() {
                return super.desc() + "\n" + Messages.get(this, "cooldown", target.buff(Cooldown.class).iconTextDisplay());
            }

            @Override
            public float getTurnsPerCharge() {
                return 10;
            }
        }

        public static class Cooldown extends SpellCooldown {
            public int icon() {
                return target.buff(Buff.class) != null ? BuffIndicator.NONE : BuffIndicator.LIMIT_BREAK;
            }
            public float duration() {
                return 300;
            }
        }
    }

    public abstract static class Buff extends CounterBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }

        public abstract float getTurnsPerCharge();

        @Override
        public boolean act() {
            spend(TICK);
            countUp(1/getTurnsPerCharge()); // doesn't last forever
            if (left() < 0) detach();

            return true;
        }

        private int turnsUntilCost() {
            float prop = 1 - count() % 1;
            return (int)Math.ceil(getTurnsPerCharge() * prop);
        }

        public float left() {
            return limit() - count();
        }

        @Override
        public abstract int icon();

        @Override
        public void detach() {
            super.detach();
            if (!RecallInscription.INSTANCE.isVisible((Hero)target)) {
                Buff.detach(target, RecallInscription.UsedItemTracker.class);
            }
        }

        @Override
        public float iconFadePercent() {
            return GameMath.gate(0, count() / limit(), 1);
        }

        @Override
        public String iconTextDisplay() {
            return String.valueOf((int)Math.ceil(left()));
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", iconTextDisplay(), turnsUntilCost());
        }
    }

    private static abstract class SpellCooldown extends Cooldown {
        public abstract int icon();

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.67f, 0.67f, 0);
        }
    }
}
