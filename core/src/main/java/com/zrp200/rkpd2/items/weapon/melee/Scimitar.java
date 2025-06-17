/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FireImbue;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.FrostImbue;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Scimitar extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {

	{
		image = ItemSpriteSheet.SCIMITAR;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.2f;

		tier = 3;
		DLY = 0.8f; //1.25x speed
	}

	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //16 base, down from 20
				lvl*(tier+1);   //scaling unchanged
	}

	static int targetNum = 0;

	@Override
	public int warriorAttack(int damage, Char enemy) {
		ArrayList<Char> targets = new ArrayList<>();

		for (int i : PathFinder.NEIGHBOURS8){
			if (Actor.findChar(Dungeon.hero.pos + i) != null) targets.add(Actor.findChar(Dungeon.hero.pos + i));
		}

		for (Char target : targets){
			int dmg = damage;
			dmg = target.defenseProc(Dungeon.hero, dmg);
			dmg -= target.drRoll();
			if ( target.buff( Vulnerable.class ) != null){
				dmg *= 1.33f;
			}

			dmg = Dungeon.hero.attackProc(target, dmg);
			target.damage( dmg, Dungeon.hero );
			if (Dungeon.hero.buff(FireImbue.class) != null)
				Dungeon.hero.buff(FireImbue.class).proc(target);
			if (Dungeon.hero.buff(FrostImbue.class) != null)
				Dungeon.hero.buff(FrostImbue.class).proc(target);
			Dungeon.hero.hitSound(Random.Float(0.87f, 1.15f));
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
			target.sprite.bloodBurstA( Dungeon.hero.sprite.center(), dmg );
			target.sprite.flash();
		}

		targetNum = targets.size();

		Dungeon.hero.sprite.centerEmitter().start( Speck.factory( Speck.CROWN ), 0.03f, 8 );
		Sample.INSTANCE.play(Assets.Sounds.CHAINS, 3);

		return 0;
	}

	@Override
	public float warriorDelay() {
		int tries = targetNum;
		targetNum = 0;
		return tries;
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target){
		return 2;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		beforeAbilityUsed(hero, null);
		Buff.prolong(hero, SwordDance.class, 4f); //4 turns as using the ability is instant
		hero.sprite.operate(hero.pos);
		hero.next();
		afterAbilityUsed(hero);
	}

	public static class SwordDance extends FlavourBuff {

		{
			announced = true;
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.DUEL_DANCE;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (5 - visualcooldown()) / 5);
		}
	}

}
