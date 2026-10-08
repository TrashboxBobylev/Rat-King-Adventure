package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MutareArtem extends ClericSpell {

    public static final MutareArtem INSTANCE = new MutareArtem();

    @Override
    public int icon() {
        return HeroIcon.MUTARE_ARTEM;
    }

    @Override
    public void tintIcon(HeroIcon icon) {
        // todo make icon
        if (SpellEmpower.isActive()) icon.tint(0, .33f);
    }

    @Override
    public Talent.Aspect aspect() {
        return Talent.Aspect.NEUTRAL;
    }

    @Override
    public float chargeUse(Hero hero) {
        return Math.max(1, 5 - scalingPoints());
    }

    @Override
    public int aspectRequirement() {
        return 3;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) &&
                (hero.subClass.is(HeroSubClass.CENOBITE) || Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.CLERIC) || PetitioMagica.containsSpell(hero, this));
    }

    private static int talentAmount(int tier){
        switch (tier){
            case 0:
                return 5;
            case 1:
                return 6;
            case 2:
                return 8;
            case 3:
                return 5;
        }
        return 0;
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        if (SpellEmpower.isActive()){
            int index = Random.Int(3);
            if (Dungeon.hero.talents.get(index).size() <= talentAmount(index + 1)*2){
                HeroClass cls;
                Talent randomTalent = null;
                ArrayList<Talent> allTalents = new ArrayList<>();
                for (LinkedHashMap<Talent, Integer> tier : hero.talents)
                    allTalents.addAll(tier.keySet());
                while (randomTalent == null || allTalents.contains(randomTalent)) {
                    do {
                        cls = Random.element(HeroClass.values());
                    } while (cls == Dungeon.hero.heroClass || (index == 2 && cls == HeroClass.RAT_KING));
                    randomTalent = Random.element(Talent.talentList(cls, index + 1));
                }
                Dungeon.hero.talents.get(index).put(randomTalent, 0);
                Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
                Dungeon.hero.sprite.emitter().burst(Speck.factory(Speck.STAR), 40);
            } else {
                GLog.n(Messages.get(MutareArtem.class, "too_many_talents"));
            }
        } else {
            ScrollOfMetamorphosis.mutareArtem = true;
            GameScene.show(new ScrollOfMetamorphosis.WndMetamorphChoose());
        }

        hero.spend(1f);
        hero.busy();
        hero.sprite.operate(hero.pos);

        onSpellCast(tome, hero);
    }
}
