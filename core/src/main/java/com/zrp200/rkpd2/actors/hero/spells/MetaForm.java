package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.Trinity;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import static com.zrp200.rkpd2.Dungeon.hero;

public class MetaForm extends ClericSpell {
    public static MetaForm INSTANCE = new MetaForm();

    @Override
    public int icon() {
        return HeroIcon.META_FORM;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 5;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.hasTalent(Talent.META_FORM);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        GameScene.show(new Trinity.WndTalentSelect());
        onSpellCast(tome, hero);
    }

    public static int duration(){
        return Math.round(75 + 25f* Dungeon.hero.pointsInTalent(Talent.META_FORM));
    }

    public static class MetaFormBuff extends FlavourBuff {

        {
            type = buffType.POSITIVE;
        }

        public Talent talent;

        @Override
        public int icon() {
            return BuffIndicator.TRINITY_FORM;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(1, 1, 0);
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (duration() - visualcooldown()) / duration());
        }

        public void setEffect(Talent effect){
            this.talent = effect;
        }

        @Override
        public String desc() {
            if (talent != null){
                return Messages.get(this, "desc", Messages.titleCase(talent.title()), talent.desc(
                        hero != null && (!hero.heroClass.is(talent.getHeroClass()))), dispTurns());
            }
            return super.desc();
        }

        private static final String TALENT = "talent";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(TALENT, talent);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            talent = bundle.getEnum(TALENT, Talent.class);
        }
    }
}
