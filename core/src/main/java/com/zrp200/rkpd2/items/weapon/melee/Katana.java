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

import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.ShadowClone;
import com.zrp200.rkpd2.actors.mobs.npcs.MirrorImage;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class Katana extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {

	{
		image = ItemSpriteSheet.KATANA;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.1f;

		tier = 4;
	}

	@Override
	public int max(int lvl) {
		return  4*(tier+1) +    //20 base, down from 25
				lvl*(tier+1);   //scaling unchanged
	}

	@Override
	public int defenseFactor( Char owner ) {
		return 3;	//3 extra defence
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {

		int attack = super.warriorAttack(damage, enemy);

		ArrayList<Integer> respawnPoints = new ArrayList<>();

		for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
			int p = enemy.pos + PathFinder.NEIGHBOURS8[i];
			if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
				respawnPoints.add( p );
			}
		}

		if (respawnPoints.size() > 0) {
			int index = Random.index( respawnPoints );

			AfterImage mob = new AfterImage();
			mob.duplicate( Dungeon.hero );
			GameScene.add( mob );
			ShadowClone.ShadowAlly.appear( mob, respawnPoints.get( index ) );

			respawnPoints.remove( index );
		}

		return attack;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		Rapier.lungeAbility(hero, target, 1.35f, 0, this);
	}

	private static class AfterImage extends MirrorImage {

		{
			actPriority = HERO_PRIO-1;
		}

		@Override
		public int attackProc(Char enemy, int damage) {
			int dmg = super.attackProc(enemy, damage)*2;
			destroy();
			sprite.die();
			return dmg;
		}

		@Override
		public boolean isInvulnerable(Class effect) {
			return true;
		}

		@Override
		public float speed() {
			return super.speed()*2f;
		}
	}
}
