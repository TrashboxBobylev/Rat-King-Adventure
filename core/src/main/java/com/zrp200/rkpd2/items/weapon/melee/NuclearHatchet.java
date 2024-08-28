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

package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.ConfusionGas;
import com.zrp200.rkpd2.actors.blobs.CorrosiveGas;
import com.zrp200.rkpd2.actors.blobs.StenchGas;
import com.zrp200.rkpd2.actors.blobs.ToxicGas;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;

public class NuclearHatchet extends MeleeWeapon {

	{
		image = ItemSpriteSheet.NUCLEAR_HATCHET;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 6;
		ACC = 1.5f; //50% boost to accuracy
	}

	@Override
	public int max(int lvl) {
		return  5*(tier-2) +    //20 base, down from 30
				lvl*(tier-2);   //scaling unchanged
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		GameScene.add( Blob.seed( enemy.pos, 700, CorrosiveGas.class ).setStrength((int) (2 + Dungeon.scalingDepth() /2.5f)));
		return super.warriorAttack(damage, enemy);
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected DuelistAbility duelistAbility() {
		return new Irradiate();
	}

	public static class Irradiate extends MeleeAbility {

		@Override
		public void afterHit(Char enemy, boolean hit) {
			Buff.affect(enemy, Exposed.class, Exposed.DURATION);

			//trace a ballistica to our target (which will also extend past them
			Ballistica trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
			//trim it to just be the part that goes past them
			trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.STOP_SOLID);

			ConeAOE cone = new ConeAOE( trajectory,
					7,
					90,
					Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);

			for( int cell : cone.cells ){

				//ignore caster cell
				if (cell == trajectory.sourcePos){
					continue;
				}

				//knock doors open
				if (Dungeon.level.map[cell] == Terrain.DOOR){
					Level.set(cell, Terrain.OPEN_DOOR);
					GameScene.updateMap(cell);
				}

				if (Dungeon.level.heroFOV[cell]) {
					Splash.at(cell, 0x68cf32, 25);
				}

				Char ch = Actor.findChar( cell );
				if (ch != null && ch.alignment != Char.Alignment.ALLY) {
					Buff.affect(ch, Exposed.class, Exposed.DURATION);
				}
			}
		}
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return 3;
	}

	public static class Effect extends Buff {

		{
			type = buffType.POSITIVE;
		}

		public static final float DURATION	= 50f;

		protected float left;

		private static final String LEFT	= "left";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( LEFT, left );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getFloat( LEFT );
		}

		public void set( float duration ) {
			this.left = duration;
		}

		@Override
		public boolean act() {
			GameScene.add(Blob.seed(target.pos, 75, ToxicGas.class));
			GameScene.add(Blob.seed(target.pos, 75, ConfusionGas.class));
			GameScene.add(Blob.seed(target.pos, 17, StenchGas.class));

			spend(TICK);
			left -= TICK;
			if (left <= 0){
				detach();
			}

			return true;
		}

		{
			immunities.add( ToxicGas.class );
			immunities.add( ConfusionGas.class );
			immunities.add( StenchGas.class );
		}
	}

	public static class Exposed extends FlavourBuff {

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		public static final float DURATION	= 15f;

		@Override
		public int icon() {
			return BuffIndicator.WARP;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0x377b22);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

	}
}
