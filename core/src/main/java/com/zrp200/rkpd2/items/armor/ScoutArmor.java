package com.zrp200.rkpd2.items.armor;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class ScoutArmor extends Armor {

    protected static final String AC_SPECIAL = "SPECIAL";
    protected static SpiritBow bow;

    {
        levelKnown = true;
        cursedKnown = true;
        defaultAction = AC_SPECIAL;
        image = ItemSpriteSheet.ARMOR_SCOUT;

        bones = false;
        unique = true;

        usesTargeting = true;
    }

    public ScoutArmor() {
        super( 1 );
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_SPECIAL);
        return actions;
    }

    public static class ScoutCooldown extends FlavourBuff {

        public static final float DURATION = 25f;
        public int icon() { return BuffIndicator.TIME; }
        public void tintIcon(Image icon) { icon.hardlight(0x360235); }
        public float iconFadePercent() { return Math.max(0, 1 - (visualcooldown() / 25)); }
        public String toString() { return Messages.get(this, "name"); }
        public String desc() { return Messages.get(this, "desc", dispTurns(visualcooldown())); }
    };

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_SPECIAL)){

            if (hero.buff(ScoutCooldown.class) != null){
                GLog.w( Messages.get(ScoutArmor.class, "not_ready"));
                return;
            }
            bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
            if (bow == null && Dungeon.hero.belongings.weapon instanceof SpiritBow){
                bow = (SpiritBow) Dungeon.hero.belongings.weapon;
            }
            if (bow == null){
                GLog.w( Messages.get(ScoutArmor.class, "no_bow"));
            }
            else GameScene.selectCell(shooter);
        }
    }

    public static float startingBoost(){
        return 1.32f + 0.13f * Dungeon.hero.pointsInTalent(Talent.LIKE_A_BULLET);
    }

    public static float distanceMultiplier(){
        return 1.13f + 0.05f * Dungeon.hero.pointsInTalent(Talent.LIKE_A_BULLET);
    }

    public static float maxDamage(){
        return 3.5f + 0.25f * Dungeon.hero.pointsInTalent(Talent.HEROIC_ARCHERY);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc",
                Math.round(100 * (startingBoost()-1)),
                Math.round(100 * (distanceMultiplier()-1)),
                Math.round(100 * maxDamage()));
    }

    public int DRMax(int lvl){

        int max = 3 + lvl + augment.defenseFactor(lvl);
        if (lvl > max){
            return ((lvl - max)+1)/2;
        } else {
            return max;
        }
    }

    public int DRMin(int lvl){

        int max = DRMax(lvl);
        if (lvl >= max){
            return (lvl + 1 - max);
        } else {
            return lvl + 1;
        }
    }

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null && bow != null) {
                SpiritBow.superShot = true;
                bow.knockArrow().cast(curUser, target);
                Buff.affect(curUser, ScoutCooldown.class, ScoutCooldown.DURATION);
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };


}
