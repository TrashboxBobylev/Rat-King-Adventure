package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Cooldown;
import com.zrp200.rkpd2.actors.buffs.CounterBuff;
import com.zrp200.rkpd2.actors.buffs.Recharging;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.GameMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.zrp200.rkpd2.Dungeon.hero;

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
        if (hero.buff(EmendatioIactus.Buff.class) != null)
            return EmendatioIactus.charges();
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
    public Talent talent() {
        return talent;
    }

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
            Buff.append(hero, Recharging.class, 2 * (1 + scalingPoints()));
            Buff.affect(hero, Buff.class);
        }

        @Override
        public int icon() { return HeroIcon.DIVINE_ADVENT; }

        @Override
        public float chargeUse(Hero hero) {
            return Math.max(1, 8 - scalingPoints());
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
            public float duration() { return 100 - 10*DivineAdvent.INSTANCE.scalingPoints(); }
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
            args.add((hero.isNearDeath() ? 300 : 150)-20*scalingPoints());
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
                String desc = super.desc();
                if (target.buff(Cooldown.class) != null)
                    desc += "\n" + Messages.get(this, "cooldown", target.buff(Cooldown.class).iconTextDisplay());
                return desc;
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
                return 300 - 20*LimitBreak.INSTANCE.scalingPoints();
            }
        }
    }

    public static class EmendatioIactus extends SpellEmpower {
        public static final EmendatioIactus INSTANCE = new EmendatioIactus();

        {
            talent = Talent.EMENDATIO_ICATUS;
        }

        public static class aspect_holder {
            Talent.Aspect aspect;
            int amount;

            public aspect_holder(Talent.Aspect aspect, int value) {
                this.aspect = aspect;
                this.amount = value;
            }
        }

        @Override
        public int icon() { return HeroIcon.EMENDATIO_ICATUS; }

        @Override
        public float chargeUse(Hero hero) {
            return 4;
        }

        @Override
        public int aspectRequirement() {
            return 1;
        }

        public static aspect_holder givenAspect(){
            TreeMap<Talent.Aspect, Integer> existingAspects = new TreeMap<>();

            for (int i = 0; i < hero.talents.size(); i++){
                for (Map.Entry<Talent, Integer> talentData : hero.talents.get(i).entrySet()){
                    existingAspects.put(talentData.getKey().aspect,
                            !existingAspects.containsKey(talentData.getKey().aspect) ?
                                    1 : existingAspects.get(talentData.getKey().aspect) + 1);
                }
            }

            return new aspect_holder(existingAspects.lastEntry().getKey(), existingAspects.lastEntry().getValue());
        }

        public static float charges(){
            return givenAspect().amount*(0.5f*(1+hero.pointsInTalent(INSTANCE.talent())));
        }

        @Override
        protected List<Object> getDescArgs() {
            ArrayList<Object> args = new ArrayList<>();
            aspect_holder holder = givenAspect();
            args.add(holder.aspect.toString());
            args.add(charges());
            args.add((int)(25*charges()));
            return args;
        }

        @Override
        protected void applyBuff(Hero hero) {
            Buff.affect(hero, Buff.class);
            Cooldown cd = Cooldown.affectHero(Cooldown.class);
            cd.spend(-1); // offset the instant cast
        }

        public static class Buff extends SpellEmpower.Buff {
            @Override
            public int icon() { return BuffIndicator.EMENDATIO_ICATUS; }

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
                return target.buff(Buff.class) != null ? BuffIndicator.NONE : BuffIndicator.EMENDATIO_ICATUS;
            }
            public float duration() {
                return 25*charges();
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
