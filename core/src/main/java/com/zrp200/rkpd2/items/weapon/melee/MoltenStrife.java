package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.effects.particles.SmokeParticle;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.bombs.ShrapnelBomb;
import com.zrp200.rkpd2.items.weapon.enchantments.Blazing;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MoltenStrife extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {
    {
        image = ItemSpriteSheet.MOLTEN_STRIFE;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.6f;

        tier = 6;
    }

    @Override
    public int max(int lvl) {
        return 4*(tier+1) + tier*lvl;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (Random.Int(5+buffedLvl()) < (buffedLvl()+1)) {
            Bomb.doNotDamageHero = true;
            new Bomb().explode(defender.pos);
            Bomb.doNotDamageHero = false;
        }

        return super.proc(attacker, defender, damage);
    }

    @Override
    public String statsInfo() {
        if (isIdentified())
            return Messages.get(this, "stats_desc", new DecimalFormat("#.#").
                    format(100 * ((buffedLvl()+1f) / (5f+buffedLvl()))));
        return Messages.get(this, "stats_desc", new DecimalFormat("#.#").format(20));
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        Bomb.doNotDamageHero = true;
        new ShrapnelBomb().explode(enemy.pos);
        new ShrapnelBomb().explode(enemy.pos);
        Bomb.doNotDamageHero = false;
        return super.warriorAttack(damage, enemy);
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target){
        return 4;
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        beforeAbilityUsed(hero, null);
        Enchanting.show(hero, new MoltenStrife(){
            @Override
            public ItemSprite.Glowing glowing() {
                return Blazing.ORANGE;
            }
        });
        hero.busy();
        hero.sprite.operate(hero.pos, () -> {
            ArrayList<Char> affected = new ArrayList<>();
            Sample.INSTANCE.play( Assets.Sounds.BLAST, 3f );
            PathFinder.buildDistanceMap( hero.pos, BArray.not( Dungeon.level.solid, null ), hero.viewDistance );
            for (int i = 0; i < PathFinder.distance.length; i++) {
                if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                    if (Dungeon.level.heroFOV[i]) {
                        CellEmitter.get(i).burst(SmokeParticle.FACTORY, 5);
                    }
                    Char ch = Actor.findChar(i);
                    if (ch != null){
                        if (ch instanceof Hero) {
                            continue;
                        }
                        affected.add(ch);
                    }
                }
            }

            for (Char ch : affected){
                // 125% bomb damage that pierces armor.
                int damage = Math.round(Random.NormalIntRange( Dungeon.scalingDepth()+5, 10 + Dungeon.scalingDepth() * 2 )*1.25f);
                ch.damage(damage, this);
            }

            hero.sprite.attack(hero.pos, () -> {
                hero.sprite.idle();
                hero.spendAndNext(Actor.TICK);
                afterAbilityUsed(hero);
            });
        });
    }


}
