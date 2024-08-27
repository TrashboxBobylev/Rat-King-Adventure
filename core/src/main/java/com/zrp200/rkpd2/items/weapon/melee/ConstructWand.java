/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2020 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.Barkskin;
import com.zrp200.rkpd2.actors.buffs.Bless;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Statue;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.StatueSprite;

import java.util.ArrayList;

public class ConstructWand extends MeleeWeapon {

	{
		image = ItemSpriteSheet.CONSTRUCT_WAND;
		hitSound = Assets.Sounds.HIT_STRONG;
		hitSoundPitch = 0.85f;

		tier = 6;
	}

    @Override
    public int max(int lvl) {
        return Math.round(4.5f*(tier+1) + (tier)*lvl);
    }

    @Override
    public String statsInfo() {
        return Messages.get(this, "stats_desc", 7 + Dungeon.scalingDepth() * 3);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        for (int i : PathFinder.NEIGHBOURS9){

            if (!Dungeon.level.solid[attacker.pos + i]
                    && !Dungeon.level.pit[attacker.pos + i]
                    && Actor.findChar(attacker.pos + i) == null
                    && attacker == Dungeon.hero) {

                GuardianKnight guardianKnight = new GuardianKnight();
                guardianKnight.weapon = this;
                guardianKnight.pos = attacker.pos + i;
                guardianKnight.aggro(defender);
                GameScene.add(guardianKnight);
                Dungeon.level.occupyCell(guardianKnight);

                CellEmitter.get(guardianKnight.pos).burst(Speck.factory(Speck.EVOKE), 4);
                break;
            }
        }
        return super.proc(attacker, defender, damage);
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        for (int i : PathFinder.NEIGHBOURS9){

            if (!Dungeon.level.solid[Dungeon.hero.pos + i]
                    && !Dungeon.level.pit[Dungeon.hero.pos + i]
                    && Actor.findChar(Dungeon.hero.pos + i) == null) {

                GuardianKnight guardianKnight = new GuardianKnight();
                guardianKnight.weapon = this;
                guardianKnight.pos = Dungeon.hero.pos + i;
                guardianKnight.HP = guardianKnight.HT = guardianKnight.HT / 2;
                guardianKnight.aggro(enemy);
                GameScene.add(guardianKnight);
                Dungeon.level.occupyCell(guardianKnight);

                CellEmitter.get(guardianKnight.pos).burst(Speck.factory(Speck.EVOKE), 4);
            }
        }
        return 0;
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target){
        return 5;
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        Invisibility.dispel();
        beforeAbilityUsed(hero, null);
        Enchanting.show(hero, new ConstructWand(){
            @Override
            public ItemSprite.Glowing glowing() {
                return new ItemSprite.Glowing( 0x0000FF );
            }
        });
        ArrayList<GuardianKnight> badBoys = new ArrayList<>(9);
        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
        for (int i : PathFinder.NEIGHBOURS9){

            if (!Dungeon.level.solid[Dungeon.hero.pos + i]
                    && !Dungeon.level.pit[Dungeon.hero.pos + i]
                    && Actor.findChar(Dungeon.hero.pos + i) == null) {

                GuardianKnight guardianKnight = new GuardianKnight();
                guardianKnight.weapon = this;
                guardianKnight.pos = Dungeon.hero.pos + i;
                GameScene.add(guardianKnight);
                Dungeon.level.occupyCell(guardianKnight);

                CellEmitter.get(guardianKnight.pos).burst(Speck.factory(Speck.EVOKE), 4);

                badBoys.add(guardianKnight);
            }
        }
        hero.busy();
        hero.sprite.operate(hero.pos, () -> {
            Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
            Buff.affect(hero, ConstructStasis.class, 10f);
            for (GuardianKnight knight: badBoys){
                Buff.affect(knight, Adrenaline.class, 10f);
                Buff.affect(knight, Bless.class, 10f);
                Buff.affect(knight, MagicImmune.class, 10f);
                Barkskin.conditionallyAppend(knight, 2 + hero.lvl/3, 2);
            }
            hero.next();
            afterAbilityUsed(hero);
        });
    }

    public static class ConstructStasis extends FlavourBuff implements TimekeepersHourglass.Stasis {
        @Override
        public boolean attachTo(Char target) {
            if (super.attachTo(target)){
                target.invisible++;
                target.paralysed++;
                Dungeon.observe();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void detach() {
            if (target.invisible > 0) target.invisible--;
            if (target.paralysed > 0) target.paralysed--;
            super.detach();
            target.sprite.idle();
            Dungeon.observe();
        }
    }

    public static class GuardianKnight extends Statue {
        {
            state = WANDERING;
            spriteClass = GuardianSprite.class;
            alignment = Alignment.ALLY;
        }

        public GuardianKnight() {
            HP = HT = 7 + Dungeon.scalingDepth() *3;
            defenseSkill = Dungeon.scalingDepth();
        }

        @Override
        public int damageRoll() {
            return Math.round(super.damageRoll()*0.67f);
        }

        @Override
        public void die(Object cause) {
            weapon = null;
            super.die(cause);
        }

        @Override
        public int drRoll() {
            return Random.Int(0, Dungeon.scalingDepth());
        }
    }

    public static class GuardianSprite extends StatueSprite {

        public GuardianSprite(){
            super();
            tint(0x84d4f6, 0.4f);
        }

        @Override
        public void resetColor() {
            super.resetColor();
            tint(0x84d4f6, 0.4f);
        }
    }
}
