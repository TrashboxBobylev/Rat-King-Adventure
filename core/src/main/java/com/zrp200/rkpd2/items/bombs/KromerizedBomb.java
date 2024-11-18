/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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

package com.zrp200.rkpd2.items.bombs;

import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.FrostFire;
import com.zrp200.rkpd2.actors.blobs.Regrowth;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.effects.particles.BlastParticle;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.mechanics.ShadowCaster;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class KromerizedBomb extends Bomb {
	
	{
		image = ItemSpriteSheet.KROMER_BOMB;
		doNotDamageHero = true;
	}
	
	@Override
	protected void onThrow(int cell) {
		super.onThrow(cell);
		if (fuse != null){
			boolean[] FOV = new boolean[Dungeon.level.length()];
			Point c = Dungeon.level.cellToPoint(cell);
			ShadowCaster.castShadow(c.x, c.y, FOV, Dungeon.level.losBlocking, 10);

			for (int i = 0; i < FOV.length; i++) {
				if (FOV[i]) {
					if (!Dungeon.level.solid[i]) {
						//TODO better vfx?
						GameScene.add(Blob.seed(i, 2, Regrowth.class));
					}
				}
			}
		}
	}

	private static final ItemSprite.Glowing CHAOTIC = new ItemSprite.Glowing( 0.2f  );

	@Override
	public ItemSprite.Glowing glowing() {
		return CHAOTIC;
	}
	
	@Override
	public boolean explodesDestructively() {
		return false;
	}
	
	@Override
	public void explode(int cell) {
		super.explode(cell);
		
		ArrayList<Char> affected = new ArrayList<>();

		boolean[] FOV = new boolean[Dungeon.level.length()];
		Point c = Dungeon.level.cellToPoint(cell);
		ShadowCaster.castShadow(c.x, c.y, FOV, Dungeon.level.losBlocking, 10);

		for (int i = 0; i < FOV.length; i++) {
			if (FOV[i]) {
				if (Dungeon.level.heroFOV[i] && !Dungeon.level.solid[i]) {
					//TODO better vfx?
					CellEmitter.center( i ).burst( BlastParticle.FACTORY, 3 );
					Splash.at(cell, 0x00FF00, 6);
				}
				if (Dungeon.level.pit[i])
					GameScene.add(Blob.seed(i, 1, FrostFire.class));
				else
					GameScene.add(Blob.seed(i, 3, FrostFire.class));
				Char ch = Actor.findChar(i);
				if (ch != null){
					if (ch instanceof Hero) {
						Warp.inflict(30f, 1f);
						if (doNotDamageHero)
							continue;
					}
					affected.add(ch);
				}
			}
		}
		
		for (Char ch : affected){
			int power = 8 - Dungeon.level.distance(ch.pos, cell);
			// 100%/83%/67% bomb damage based on distance, but pierces armor.
			int damage = Math.round(Random.NormalIntRange( Dungeon.scalingDepth()+5, 10 + Dungeon.scalingDepth() * 2 ));
			damage = Math.round(damage * (1f - .05f*Dungeon.level.distance(cell, ch.pos)));
			if (ch.properties().contains(Char.Property.UNDEAD) || ch.properties().contains(Char.Property.DEMONIC)){
				ch.sprite.emitter().start( ShadowParticle.UP, 0.05f, 5 );

				//bomb deals an additional 30% damage to unholy enemies in its range
				ch.damage(Math.round(damage*0.3f), this);
			}
			ch.damage(Math.round(damage), this);
			Buff.prolong(ch, Blindness.class, power);
			Buff.prolong(ch, Cripple.class, power);
			if (ch == Dungeon.hero && !ch.isAlive()){
				Badges.validateDeathFromFriendlyMagic();
				Dungeon.fail(Bomb.class);
			}
		}
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (20 + 30);
	}
}
