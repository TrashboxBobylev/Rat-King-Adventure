package com.zrp200.rkpd2.items.artifacts;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.LockedFloor;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.rings.RingOfEnergy;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.utils.GLog;

public class KromerCloak extends CloakOfShadows {

    {
        image = ItemSpriteSheet.KROMER_CLOAK;
        charge = Math.min(level()+5, 15);
        partialCharge = 0;
        chargeCap = Math.min(level()+5, 15);
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new cloakRecharge();
    }

    @Override
    protected ArtifactBuff activeBuff( ) {
        return new cloakStealth();
    }

    @Override
    public Item upgrade() {
        Item result = super.upgrade();
        chargeCap = Math.min(chargeCap + 1, 15);
        return result;
    }

    public class cloakRecharge extends CloakOfShadows.cloakRecharge implements ActionIndicator.Action {
        @Override
        public boolean act() {
            if (charge < chargeCap) {
                LockedFloor lock = target.buff(LockedFloor.class);
                if (activeBuff == null && (lock == null || lock.regenOn())) {
                    float missing = (chargeCap - charge);
                    if (level() > 7) missing += 8*(level() - 7)/3f;
                    float turnsToCharge = (25 - missing);
                    if(((Hero)target).heroClass.isExact(HeroClass.ROGUE)
                            && !((Hero) target).hasTalent(Talent.EFFICIENT_SHADOWS)) turnsToCharge /= ROGUE_BOOST;
                    turnsToCharge /= RingOfEnergy.artifactChargeMultiplier(target);
                    float chargeToGain = (1f / turnsToCharge);
                    if (!isEquipped(Dungeon.hero)){
                        chargeToGain *= Dungeon.hero.byTalent(
                                Talent.LIGHT_CLOAK, LC_FACTOR,
                                Talent.RK_FREERUNNER, LC_FACTOR_RK);
                    }
                    partialCharge += chargeToGain;
                }

                if (partialCharge >= 1) {
                    charge++;
                    partialCharge -= 1;
                    if (charge == chargeCap){
                        partialCharge = 0;
                    }

                }
            } else
                partialCharge = 0;

            if (cooldown > 0)
                cooldown --;

            updateQuickslot();
            if ((int) (charge * getChargeEfficiency()) >= 1
                    && Dungeon.hero.hasTalent(Talent.THINKING_WITH_PORTALS)){
                ActionIndicator.setAction(this);
            } else {
                ActionIndicator.clearAction(this);
            }

            spend( TICK );

            return true;
        }
    }

    public class cloakStealth extends CloakOfShadows.cloakStealth {

        @Override
        public boolean act(){
            turnsToCost--;
            Hero target = (Hero)this.target;

            if (turnsToCost <= 0){
                charge--;
                Warp.inflict(2, stealthDuration());
                if (charge < 0) {
                    charge = 0;
                    detach();
                    GLog.w(Messages.get(this, "no_charge"));
                    target.interrupt();
                } else {
                    //target hero level is 1 + 2*cloak level
                    int lvlDiffFromTarget = target.lvl - (1+level()*2);
                    //plus an extra one for each level after 6
                    if (level() >= 7){
                        lvlDiffFromTarget -= level()-6;
                    }
                    if (lvlDiffFromTarget >= 0){
                        exp += Math.round(13f * Math.pow(1.1f, lvlDiffFromTarget));
                    } else {
                        exp += Math.round(13f * Math.pow(0.75f, -lvlDiffFromTarget));
                    }

                    int expPerLevel = 40;
                    if (exp >= (level() + 1) * expPerLevel && level() < levelCap) {
                        upgrade();
                        exp -= level() * expPerLevel;
                        GLog.p(Messages.get(this, "levelup"));

                    }
                    turnsToCost = (int) stealthDuration();
                }
                updateQuickslot();
            }

            spend( TICK );

            return true;
        }
    }

    public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipeBundled {

        {
            inputs =  new Class[]{CloakOfShadows.class, Kromer.class};
            inQuantity = new int[]{1, 1};

            cost = 20;

            output = KromerCloak.class;
            outQuantity = 1;
        }

    }
}
