package com.zrp200.rkpd2.actors.buffs;

import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.BrokenSeal;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.rings.RingOfFuror;
import com.zrp200.rkpd2.items.rings.RingOfWealth;
import com.zrp200.rkpd2.items.weapon.melee.WarScythe;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;

import java.text.DecimalFormat;
import java.util.HashMap;

public class BrawlerBuff extends CounterBuff implements ActionIndicator.Action {

    {
        revivePersists = true;
    }

    public int maxCharge(){
        return 15 + Dungeon.hero.pointsInTalent(Talent.POWER_STABILIZER)*5;
    }

    public float damageFactor(float damage){
        return (damage*(1f + Math.min(damageModifier()*maxCharge(), count()*damageModifier())));
    }

    public float damageModifier(){
        return 0.04f;
    }

    public void useCharge(){
        countDown(Math.min(10, count()));
    }

    @Override
    public boolean act() {
        if (count() < maxCharge()){
            countUp(getInc());
        }
        if (count() >= 10){
            ActionIndicator.setAction(this);
        }
        if (count() > maxCharge()){
            countDown(count()-maxCharge());
        }

        spend(TICK);
        return true;
    }

    @Override
    public int actionIcon() {
        return HeroIcon.BRAWLING;
    }

    @Override
    public int indicatorColor() {
        return 0xff8ab3;
    }

    @Override
    public void doAction() {
        GameScene.selectCell(new Selector());
    }

    @Override
    public boolean usable() {
        return count() >= 10;
    }

    public float getInc() {
        if (Dungeon.hero.hasTalent(Talent.IN_MY_MEMORIES)){
            BrokenSeal.WarriorShield shield = Dungeon.hero.buff(BrokenSeal.WarriorShield.class);
            if (shield != null && shield.maxShield() == shield.shielding()){
                return 0.4f * (1.15f + Dungeon.hero.pointsInTalent(Talent.IN_MY_MEMORIES)*0.1f);
            }
        }
        return 0.4f;
    }

    @Override
    public int icon() {
        return BuffIndicator.UPGRADE;
    }

    @Override
    public void tintIcon(Image icon) {
        float r,g,b;
        if (count()<=15) {
            r = 1;
            g = 0.54f * (1 - Math.max(0, count() / 10 - 0.4f));
            b = 0.70f * (1 - count() / 10 * 1.2f);
        } else {
            r = 1 * (1 - (count()-15)/maxCharge());
            g = 0;
            b = 0;
        }
        icon.hardlight(r,g,b);
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", (int)count(), new DecimalFormat("#.#").format(Math.min(damageModifier()*maxCharge(), count()*damageModifier())*100));
    }

    public static class BrawlingTracker extends Buff{}

    private void doAttack(final Char enemy) {
        AttackIndicator.target(enemy);
        boolean wasAlly = enemy.alignment == target.alignment;
        Hero hero = (Hero) target;
        float dmgMulti = 1f;
        int dmgBonus = 0;
        if (hero.pointsInTalent(Talent.IN_MY_MEMORIES) > 0){
            BrokenSeal.WarriorShield shield = hero.buff(BrokenSeal.WarriorShield.class);
            if (shield != null)
                dmgBonus += 0.4f*hero.pointsInTalent(Talent.IN_MY_MEMORIES)*shield.shielding();
        }

        hero.attack(enemy, dmgMulti, dmgBonus, hero.belongings.weapon() instanceof WarScythe ? 1f : Char.INFINITE_ACCURACY);

        Invisibility.dispel();
        BrawlerWeapon weapon = (BrawlerWeapon) (hero.belongings.weapon());
        float delay;
        if (weapon != null)
            delay = weapon.warriorDelay();
        else
            delay = 1.5f;
        delay *= RingOfFuror.attackSpeedMultiplier(hero);
        hero.spendAndNext(hero.attackDelay()*delay);

        if (!enemy.isAlive() || (!wasAlly && enemy.alignment == target.alignment)) {
            if (hero.hasTalent(Talent.HOLERIC_BURST)){
                int heal = Math.min(hero.pointsInTalent(Talent.HOLERIC_BURST)*5, hero.HT-hero.HP);
                hero.HP += heal;
                Emitter e = hero.sprite.emitter();
                if (e != null && heal > 0) e.burst(Speck.factory(Speck.HEALING), Math.max(1,Math.round(heal*2f/5)));
            }
            if (hero.pointsInTalent(Talent.PRIDE_OF_STEEL) > 1){
                Dungeon.level.drop(RingOfWealth.genConsumableDrop(hero.lvl/2), enemy.pos).sprite.drop();
                RingOfWealth.showFlareForBonusDrop(enemy.sprite);
            }
        }
        Buff.detach(hero, BrawlingTracker.class);
        Bomb.doNotDamageHero = false;
    }

    @Override
    public boolean attachTo(Char target) {
        if (count()>=10) ActionIndicator.setAction(this);
        return super.attachTo(target);
    }

    private class Selector extends CellSelector.TargetedListener {

        private HashMap<Char, Integer> targets = new HashMap<>();
        protected boolean isValidTarget(Char enemy) {
            if (enemy != null
                    && enemy.alignment != Char.Alignment.ALLY
                    && enemy != target
                    && Dungeon.level.heroFOV[enemy.pos]
                    && !target.isCharmedBy(enemy)) {
                if (target.canAttack(enemy)) {
                    targets.put(enemy, target.pos); // no need to generate a ballistica.
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onInvalid(int cell) {
            if(cell == -1) return;
            GLog.w(Messages.get(Combo.class, "bad_target"));
        }

        @Override
        protected void action(Char enemy) {
            int leapPos = targets.get(enemy);
            ((Hero)target).busy();
            Buff.affect(target, BrawlingTracker.class);
            if(leapPos != target.pos) {
                target.sprite.jump(target.pos, leapPos, () -> {
                    target.move(leapPos);
                    Dungeon.level.occupyCell(target);
                    Dungeon.observe();
                    GameScene.updateFog();
                    target.sprite.attack(enemy.pos, () -> doAttack(enemy));
                });
            } else {
                target.sprite.attack(enemy.pos, ()->doAttack(enemy));
            }
        }

        @Override
        public String prompt() {
            return Messages.get(Combo.class, "prompt");
        }
    }

    public interface BrawlerWeapon {
        default int warriorAttack(int damage, Char enemy){
            return damage;
        }

        default float warriorDelay(){
            return 1.5f;
        }
    }
}
