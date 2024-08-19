package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.ArcaneArmor;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Daze;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.Bee;
import com.zrp200.rkpd2.actors.mobs.Crab;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Scorpio;
import com.zrp200.rkpd2.actors.mobs.Spinner;
import com.zrp200.rkpd2.actors.mobs.Swarm;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.armor.glyphs.Viscosity;
import com.zrp200.rkpd2.items.stones.StoneOfAggression;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.enchantments.Kinetic;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public class RoyalBrand extends Crossbow implements Talent.SpellbladeForgeryWeapon {
    {
        image = ItemSpriteSheet.ROYAL_SWORD;
        hitSound = Assets.Sounds.HIT_STRONG;
        hitSoundPitch = 1.4f;

        tier = 6;
        ACC = 1.24f;
        DLY = 0.8f;
        RCH = 3;
    }

    @Override
    public int STRReq(int lvl) {
        return super.STRReq(lvl)+1;
    }

    @Override
    public int max(int lvl) {
        return  3*(tier+1) +    //21 damage,
                lvl*(tier+2);     //+8 per level, down from +7
    }

    @Override
    public int defenseFactor( Char owner ) {
        return 2+2*buffedLvl();    //6 extra defence, plus 3 per level;
    }

    public String statsInfo(){
        if (isIdentified()){
            return Messages.get(this, "stats_desc", 2+2*buffedLvl());
        } else {
            return Messages.get(this, "typical_stats_desc", 2);
        }
    }

    @Override
    public int damageRoll(Char owner) {
        if (owner instanceof Hero) {
            Hero hero = (Hero)owner;
            Char enemy = hero.enemy();
            if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)) {
                //deals 50% toward max to max on surprise, instead of min to max.
                int diff = max() - min();
                int damage = augment.damageFactor(Random.NormalIntRange(
                        min() + Math.round(diff*0.50f),
                        max()));
                int exStr = hero.STR() - STRReq();
                if (exStr > 0) {
                    damage += Random.IntRange(0, exStr);
                }
                return damage;
            }
        }
        return super.damageRoll(owner);
    }

    static int targetNum = 0;

    //this will be a really fun one
    @Override
    public int warriorAttack(int damage, Char enemy) {
        int conservedDamage = 0;
        if (Dungeon.hero.buff(Kinetic.ConservedDamage.class) != null) {
            conservedDamage = Dungeon.hero.buff(Kinetic.ConservedDamage.class).damageBonus();
            Dungeon.hero.buff(Kinetic.ConservedDamage.class).detach();
        }

        if (damage > enemy.HP){
            int extraDamage = (damage - enemy.HP)*2;

            Buff.affect(Dungeon.hero, Kinetic.ConservedDamage.class).setBonus(extraDamage);
        }
        damage += conservedDamage;
        damage += damage * 2 * (1 - (Dungeon.hero.HP / Dungeon.hero.HT));
        if (enchantment != null && Dungeon.hero.buff(MagicImmune.class) == null) {
            damage = enchantment.proc( this, Dungeon.hero, enemy, damage );
        }
        int lvl = Math.max(0, Dungeon.hero.STR() - STRReq());
        if (Random.Int(lvl + 12) >= 10) {
            Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC);
            damage*=2;
        }
        if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(Dungeon.hero)){
            Buff.prolong(enemy, Blindness.class, 4f);
            Buff.prolong(enemy, Cripple.class, 4f);
        }
        Buff.affect(enemy, Bleeding.class).set(damage);
        ArrayList<Char> affectedChars = new ArrayList<>();
        Ballistica trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
        ConeAOE cone = new ConeAOE(
                trajectory,
                5,
                90,
                Ballistica.STOP_SOLID | Ballistica.STOP_TARGET
        );
        for (int cell : cone.cells){
            CellEmitter.bottom(cell).burst(Speck.factory(Speck.STEAM), 10);
            Char ch = Actor.findChar( cell );
            if (ch != null && !ch.equals(enemy)) {
                affectedChars.add(ch);
            }
        }
        for (Char ch : affectedChars){
            int dmg = Dungeon.hero.attackProc(ch, damage);
            switch (Dungeon.level.distance(ch.pos, Dungeon.hero.pos)){
                case 2: dmg *= 0.66f; break;
                case 3: dmg *= 0.33f; break;
                case 4: dmg *= 0.16f; break;
                case 5: dmg *= 0.1f; break;
            }
            dmg -= ch.drRoll();
            dmg = ch.defenseProc(Dungeon.hero, dmg);
            ch.damage(dmg, Dungeon.hero);
        }
        Sample.INSTANCE.play(Assets.Sounds.ROCKS);
        Camera.main.shake( 3, 0.7f );
        Buff.affect(enemy, StoneOfAggression.Aggression.class, StoneOfAggression.Aggression.DURATION / 5);
        Buff.affect(Dungeon.hero, ArcaneArmor.class).set(damage/3, 40);
        Buff.prolong(enemy, Vulnerable.class, damage);
        Dungeon.hero.sprite.centerEmitter().start( Speck.factory( Speck.CROWN ), 0.03f, 8 );
        Sample.INSTANCE.play(Assets.Sounds.CHAINS, 3);
        if (enemy.isAlive()){
            //trace a ballistica to our target (which will also extend past them
            trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
            //trim it to just be the part that goes past them
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
            //knock them back along that ballistica
            WandOfBlastWave.throwChar(enemy, trajectory, 2, true, false, getClass());
            Buff.prolong(enemy, Vertigo.class, Random.NormalIntRange(1, 4));
        }
        Buff.affect(Dungeon.hero, Barrier.class).setShield(damage / 6 + 1 + Dungeon.hero.drRoll()/2);
        if (Dungeon.hero.lastMovPos != -1 &&
                Dungeon.level.distance(Dungeon.hero.lastMovPos, enemy.pos) >
                        Dungeon.level.distance(Dungeon.hero.pos, enemy.pos) && Dungeon.hero.buff(RoundShield.Block.class) == null){
            Dungeon.hero.lastMovPos = -1;
            //knock out target and get blocking
            trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
            WandOfBlastWave.throwChar(enemy, trajectory, 1, true, true, getClass());
            Buff.affect(Dungeon.hero, RoundShield.Block.class, 8f);
            damage*=1.5;
        }
        Buff.affect(enemy, Viscosity.DeferedDamage.class).prolong(damage*2);
        Buff.affect(enemy, Paralysis.class, 3.5f);
        return super.warriorAttack(damage, enemy);
    }

    @Override
    public float warriorDelay() {
        return 3;
    }

    // Royal Brand's super-mega-ultimate duelist ability
    public static class DuelistInfo {
        public static float powerModifier(Hero hero){
            if (hero.buff(Charger.class) != null){
                return hero.buff(Charger.class).charges[0] / 5f;
            } else {
                return 2f;
            }
        }

        public static float swordDanceDuration(Hero hero) {
            return swordDanceDuration(powerModifier(hero));
        }

        public static float swordDanceDuration(float powerModifier){
            return 3f * powerModifier;
        }

        public static float dazeDuration(Hero hero){
            return dazeDuration(powerModifier(hero));
        }

        public static float dazeDuration(float powerModifier){
            return 4f * powerModifier;
        }

        public static float spearBoost(Hero hero) {
            return spearBoost(powerModifier(hero));
        }

        public static float spearBoost(float powerModifier){
            return powerModifier / 2.5f;
        }

        public static float greataxeBoost(Hero hero){
            return greataxeBoost(powerModifier(hero));
        }

        public static float greataxeBoost(float powerModifier){
            return powerModifier / 2.5f;
        }

        public static float maceBoost(Hero hero){
            return maceBoost(powerModifier(hero));
        }

        public static float maceBoost(float powerModifier){
            return powerModifier / 3.5f;
        }

        public static float pickaxeBoost(Hero hero){
            return pickaxeBoost(powerModifier(hero));
        }

        public static float pickaxeBoost(float powerModifier){
            return powerModifier / 3.5f;
        }

        public static float comboStrikeBoost(Hero hero){
            return comboStrikeBoost(powerModifier(hero));
        }

        public static float comboStrikeBoost(float powerModifier){
            return powerModifier / 8f;
        }

        public static int sneakDistance(Hero hero){
            return sneakDistance(powerModifier(hero));
        }

        public static int sneakDistance(float powerModifier){
            return Math.round(powerModifier * 4f);
        }

        public static float bleedDamage(Hero hero){
            return bleedDamage(powerModifier(hero));
        }

        public static float bleedDamage(float powerModifier){
            return powerModifier * 0.5f;
        }

        public static float dmgMultiplier(Hero hero, Char enemy, Weapon wep, float powerModifier){
            float multi = 1f;

            if (Dungeon.level.distance(hero.pos, enemy.pos) == wep.RCH) {
                multi += spearBoost(powerModifier);
            }

            if (hero.HP < hero.HT / 2){
                multi += greataxeBoost(powerModifier);
            }

            if (((Mob)enemy).surprisedBy(hero)){
                multi += maceBoost(powerModifier);
            }

            if (Char.hasProp(enemy, Char.Property.INORGANIC)
                    || enemy instanceof Swarm
                    || enemy instanceof Bee
                    || enemy instanceof Crab
                    || enemy instanceof Spinner
                    || enemy instanceof Scorpio) {
                multi += pickaxeBoost(powerModifier);
            }

            if (hero.buff(Sai.ComboStrikeTracker.class) != null){
                multi += comboStrikeBoost(powerModifier)*(hero.buff(Sai.ComboStrikeTracker.class).totalHits());
                hero.buff(Sai.ComboStrikeTracker.class).detach();
            }

            return multi;
        }

        public static void afterHit(Hero hero, Char enemy, Weapon weapon, float powerModifier){
            if (enemy.isAlive()){
                Buff.affect(enemy, Daze.class, DuelistInfo.dazeDuration(powerModifier));
                if (Dungeon.level.distance(hero.pos, enemy.pos) == weapon.RCH) {
                    //trace a ballistica to our target (which will also extend past them
                    Ballistica trajectory = new Ballistica(hero.pos, enemy.pos, Ballistica.STOP_TARGET);
                    //trim it to just be the part that goes past them
                    trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
                    //knock them back along that ballistica
                    WandOfBlastWave.throwChar(enemy, trajectory, weapon.RCH-1, true, false, hero.getClass());
                }
            } else {
                onAbilityKill(hero, enemy);
            }
            whipEffect(hero, enemy.pos, weapon, powerModifier);
        }

        public static void whipEffect(Hero hero, int target, Weapon weapon, float powerModifier){
            ArrayList<Char> targets = new ArrayList<>();

            for (Char ch : Actor.chars()){
                if (ch.alignment == Char.Alignment.ENEMY
                        && !hero.isCharmedBy(ch)
                        && Dungeon.level.heroFOV[ch.pos]
                        && hero.canAttack(ch)){
                    targets.add(ch);
                }
            }

            if (targets.isEmpty()) {
                return;
            }

            weapon.throwSound();
            hero.sprite.attack(hero.pos, new Callback() {
                @Override
                public void call() {
                    for (Char ch : targets) {
                        Buff.affect(ch, Sickle.HarvestBleedTracker.class, 0).bleedFactor = 1f;
                        hero.attack(ch, bleedDamage(powerModifier), 0, Char.INFINITE_ACCURACY);
                        if (!ch.isAlive()){
                            onAbilityKill(hero, ch);
                        }
                    }
                    hero.next();
                    Invisibility.dispel();
                }
            });
        }

    }

    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    //does NOT work with elite dexterity (otherwise I will lose my sanity)
    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        final float powerModifier = DuelistInfo.powerModifier(hero);

        float attackDelay = delayFactor(hero)*2;

        Buff.affect(hero, RunicBlade.RunicSlashTracker.class);

        beforeAbilityUsed(hero, null);

        if (hero.buff(ChargedShot.class) == null){
            Buff.affect(hero, ChargedShot.class);
        }

        Buff.prolong(hero, Scimitar.SwordDance.class, DuelistInfo.swordDanceDuration(powerModifier) + attackDelay);
        Buff.prolong(hero, Quarterstaff.DefensiveStance.class, DuelistInfo.swordDanceDuration(powerModifier) + attackDelay);

        for (int i = 0; i < ((powerModifier)); i++){
            Buff.append(hero, RoundShield.GuardTracker.class, 6 + Math.max(0, attackDelay - 1));
        }

        Char enemy = Actor.findChar(target);

        hero.belongings.abilityWeapon = this;
        boolean canDirectlyAttack = enemy != null && hero.canAttack(enemy);
        if (!canDirectlyAttack){
            hero.belongings.abilityWeapon = null;
        }
        hero.busy();

        hero.sprite.operate(hero.pos, () -> {
            if (canDirectlyAttack && target != hero.pos){
                hero.sprite.attack(enemy.pos, () -> {
                    AttackIndicator.target(enemy);
                    boolean hit = hero.attack(enemy, DuelistInfo.dmgMultiplier(hero, enemy, this, powerModifier), 0, Char.INFINITE_ACCURACY);
                    if (hit) {
                        DuelistInfo.afterHit(hero, enemy, this, powerModifier);
                    }
                    Buff.detach(hero, RunicBlade.RunicSlashTracker.class);
                    afterAbilityUsed(hero);
                    Invisibility.dispel();
                    hero.spend(hero.attackDelay()*2);
                });
            } else if ((enemy != null || !Dungeon.level.heroFOV[target] || hero.rooted)) {
                GLog.w(Messages.get(Dagger.class, "ability_bad_position"));
                if (Dungeon.hero.rooted) PixelScene.shake(1, 1f);
                Buff.detach(hero, RunicBlade.RunicSlashTracker.class);
                hero.sprite.idle();
                hero.next();
            } else {
                PathFinder.buildDistanceMap(Dungeon.hero.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null), DuelistInfo.sneakDistance(hero));
                int distance = PathFinder.distance[target];
                if (distance == Integer.MAX_VALUE) {
                    GLog.w(Messages.get(Dagger.class, "ability_bad_position"));
                    Buff.detach(hero, RunicBlade.RunicSlashTracker.class);
                    hero.next();
                    return;
                }

                beforeAbilityUsed(hero, null);
                // you can trade distance for longer invis if you want.
                Buff.affect(hero, Invisibility.class, Math.max(Actor.TICK, DuelistInfo.sneakDistance(hero) - distance + attackDelay));
                hero.next();

                Dungeon.hero.sprite.turnTo(Dungeon.hero.pos, target);
                Dungeon.hero.pos = target;
                Dungeon.level.occupyCell(Dungeon.hero);
                Dungeon.observe();
                GameScene.updateFog();
                Dungeon.hero.checkVisibleMobs();

                Dungeon.hero.sprite.place(Dungeon.hero.pos);
                CellEmitter.get(Dungeon.hero.pos).burst(Speck.factory(Speck.WOOL), 6);
                Sample.INSTANCE.play(Assets.Sounds.PUFF);
                Buff.detach(hero, RunicBlade.RunicSlashTracker.class);
                hero.spend(hero.attackDelay()*2);
                hero.sprite.idle();
                afterAbilityUsed(hero);
            }
        });
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target) {
        return (int) Math.max(1, hero.buff(Charger.class).charges[0]);
    }
}
