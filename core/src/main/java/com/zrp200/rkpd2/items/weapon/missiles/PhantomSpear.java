/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.zrp200.rkpd2.items.weapon.missiles;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.npcs.NPC;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.watabou.utils.Callback;

import java.util.HashSet;

public class PhantomSpear extends MissileWeapon {

	{
		image = ItemSpriteSheet.PHANTOM_SPEAR;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1f;
		
		tier = 6;
		baseUses = 20;
		sticky = false;
		recoverable = false;
	}

	@Override
	public int max(int lvl) {
		return  5 * (tier-1) +                  //25 base, down from 30
				(tier-1) * lvl;               //scaling unchanged
	}

	private Char findChar(Ballistica path, Hero hero, HashSet<Char> existingTargets){
		for (int cell : path.path){
			Char ch = Actor.findChar(cell);
			if (ch != null){
				if (cell == path.collisionPos){
					return ch;
				} else if (ch == hero || existingTargets.contains(ch)){
					continue;
				} else if (ch.alignment != Char.Alignment.ALLY && !(ch instanceof NPC)){
					return ch;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public int throwPos(Hero user, int dst) {
		if (Dungeon.level.passable[new Ballistica( user.pos, dst, Ballistica.STOP_TARGET, false ).collisionPos])
			return new Ballistica( user.pos, dst, Ballistica.STOP_TARGET, false ).collisionPos;
		return user.pos;
	}

	@Override
	public void cast(final Hero user, final int dst ) {
		Ballistica b = new Ballistica(user.pos, dst, Ballistica.STOP_TARGET);
		final HashSet<Char> targets = new HashSet<>();

		Char enemy = findChar(b, user, targets);

		if (enemy == null){
			super.cast(user, dst);
			return;
		}

		targets.add(enemy);
		throwSound();
		QuickSlotButton.target(enemy);

		int degrees = 180;
		ConeAOE cone = new ConeAOE(b, degrees);
		for (Ballistica ray : cone.rays){
			// 1/3/5/7/9 up from 0/2/4/6/8
			Char toAdd = findChar(ray, user, targets);
			if (toAdd != null && curUser.fieldOfView[toAdd.pos]){
				targets.add(toAdd);
			}
		}

		final HashSet<Callback> callbacks = new HashSet<>();

		for (Char ch : targets) {
			Callback callback = new Callback() {
				@Override
				public void call() {

					user.shoot(ch, PhantomSpear.this);
					callbacks.remove( this );
					if (callbacks.isEmpty()) {
						Invisibility.dispel();
						if(!forceSkipDelay) {
							if (user.buff(Talent.LethalMomentumTracker.class) != null){
								user.buff(Talent.LethalMomentumTracker.class).detach();
								user.next();
							} else {
								user.spendAndNext(castDelay(user, dst));
							}
						}
						if (durability <= 0){
							detach(user.belongings.backpack);
							durability = 100;
						}
					}
				}
			};

			MissileSprite m = user.sprite.parent.recycle( MissileSprite.class );
			m.reset( user.sprite, ch.pos, this, callback );
			m.alpha(0.5f);

			callbacks.add( callback );
		}

		user.sprite.zap( enemy.pos );
		user.busy();
	}

	private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0x999999, 0.25f );

	@Override
	public ItemSprite.Glowing glowing() {
		return GREY;
	}
	
}
