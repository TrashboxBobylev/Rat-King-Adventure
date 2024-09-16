package com.zrp200.rkpd2.actors.mobs;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.npcs.DirectableAlly;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.EchSprite;

import java.util.ArrayList;
import java.util.Collections;

import static com.zrp200.rkpd2.Dungeon.level;

public class Ech extends DirectableAlly {

    {
        spriteClass = EchSprite.class;

        alignment = Alignment.ALLY;

        update();
        HP = HT;
        viewDistance = Light.DISTANCE;

        maxLvl = -1;

        baseSpeed = 1f;
    }

    public void update(){
        HT = (int) (8 * getModifier());
        defenseSkill = (int) ((int) (5 * getModifier())*
                (Dungeon.hero.heroClass.isExact(HeroClass.ROGUE) ? 1.5f : 1));
    }

    private static double getModifier() { return Math.max(1, Dungeon.scalingDepth()/4d); }

    @Override
    public float spawningWeight() {
        return 0;
    }
    protected int[]
            damageRange = {1,5},
            armorRange  = {1,3};

    @Override
    public int damageRoll() {
        int damage = Random.NormalIntRange((int) (damageRange[0] * getModifier()), (int) (damageRange[1] * getModifier()));
        if (!level.adjacent(pos, enemy.pos)) damage /= 2;
        return damage;
    }

    @Override
    public int attackSkill( Char target ) {
        return Random.round((float) (8*getModifier())*
                (Dungeon.hero.heroClass.isExact(HeroClass.ROGUE) ? 1.5f : 1));
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.round(Random.NormalIntRange(armorRange[0], armorRange[1])*
                        (Dungeon.hero.heroClass.isExact(HeroClass.WARRIOR) ? 2 : 1));
    }

    @Override
    public boolean isImmune(Class effect) {
        return Dungeon.hero.isImmune(effect);
    }

    @Override
    public float resist(Class effect) {
        return Dungeon.hero.resist(effect);
    }

    public void hitSound( float pitch ){
        Sample.INSTANCE.play(Assets.Sounds.HIT, 1, pitch*0.5f);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        damage = Talent.onAttackProc(Dungeon.hero, enemy, damage);
        SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
        if (bow != null && bow.enchantment != null && Dungeon.hero.buff(MagicImmune.class) == null) {
            damage = bow.enchantment.proc(bow, this, enemy, damage);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public boolean canAttack(Char enemy) {
        if (Dungeon.hero.heroClass.isExact(HeroClass.MAGE)){
            return super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
        }
        else return super.canAttack(enemy);
    }

    @Override
    public float speed() {
        float speed = super.speed();

        //moves 2 tiles at a time when returning to the hero
        if (state == WANDERING && defendingPos == -1){
            speed *= 2;
        }

        return speed;
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        Buff.affect(Dungeon.hero, EchDied.class).depth = Dungeon.depth;
        Talent.onFoodEaten(Dungeon.hero, 0, null);
    }

    public static class EchDied extends Buff {

        public int depth;

        {
            actPriority = HERO_PRIO + 1;
        }

        @Override
        public boolean act() {
            if (Dungeon.depth != depth){
                ArrayList<Integer> candidatePositions = new ArrayList<>();
                for (int i : PathFinder.NEIGHBOURS8) {
                    if (!level.solid[i+target.pos] && level.findMob(i+target.pos) == null){
                        candidatePositions.add(i+target.pos);
                    }
                }
                Collections.shuffle(candidatePositions);
                Ech ech = new Ech();
                ech.state = ech.WANDERING;

                if (!candidatePositions.isEmpty()){
                    ech.pos = candidatePositions.remove(0);
                } else {
                    ech.pos = target.pos;
                }

                if (ech.fieldOfView == null || ech.fieldOfView.length != level.length()){
                    ech.fieldOfView = new boolean[level.length()];
                }
                GameScene.add(ech);
                level.updateFieldOfView( ech, ech.fieldOfView );
                detach();
                return true;
            }

            spend( TICK );

            return true;
        }

        private static final String DEPTH = "depth";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(DEPTH, depth);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            depth = bundle.getInt(DEPTH);
        }
    }
}
