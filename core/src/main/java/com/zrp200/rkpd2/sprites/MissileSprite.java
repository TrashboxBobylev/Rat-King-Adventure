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

package com.zrp200.rkpd2.sprites;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.RobotTransform;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpectralBlades;
import com.zrp200.rkpd2.actors.hero.spells.HolyLance;
import com.zrp200.rkpd2.actors.mobs.GnollGeomancer;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.quest.NerfGun;
import com.zrp200.rkpd2.items.weapon.Slingshot;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.melee.Crossbow;
import com.zrp200.rkpd2.items.weapon.melee.ExoKnife;
import com.zrp200.rkpd2.items.weapon.melee.RunicBladeMkII;
import com.zrp200.rkpd2.items.weapon.melee.TrueTerminusBlade;
import com.zrp200.rkpd2.items.weapon.missiles.Bolas;
import com.zrp200.rkpd2.items.weapon.missiles.FishingSpear;
import com.zrp200.rkpd2.items.weapon.missiles.HeavyBoomerang;
import com.zrp200.rkpd2.items.weapon.missiles.HomingBoomerang;
import com.zrp200.rkpd2.items.weapon.missiles.Javelin;
import com.zrp200.rkpd2.items.weapon.missiles.Kunai;
import com.zrp200.rkpd2.items.weapon.missiles.PhantomSpear;
import com.zrp200.rkpd2.items.weapon.missiles.Shuriken;
import com.zrp200.rkpd2.items.weapon.missiles.StarPieces;
import com.zrp200.rkpd2.items.weapon.missiles.ThrowingKnife;
import com.zrp200.rkpd2.items.weapon.missiles.ThrowingSpear;
import com.zrp200.rkpd2.items.weapon.missiles.ThrowingSpike;
import com.zrp200.rkpd2.items.weapon.missiles.Trident;
import com.zrp200.rkpd2.items.weapon.missiles.darts.CrossbowAmmo;
import com.zrp200.rkpd2.items.weapon.missiles.darts.Dart;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.watabou.noosa.Visual;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.HashMap;

public class MissileSprite extends ItemSprite implements Tweener.Listener {

	private static final float SPEED	= 240f;
	
	private Callback callback;
	
	public void reset( int from, int to, Item item, Callback listener ) {
		reset(Dungeon.level.solid[from] ? DungeonTilemap.raisedTileCenterToWorld(from) : DungeonTilemap.raisedTileCenterToWorld(from),
				Dungeon.level.solid[to] ? DungeonTilemap.raisedTileCenterToWorld(to) : DungeonTilemap.raisedTileCenterToWorld(to),
				item, listener);
	}

	public void reset( Visual from, int to, Item item, Callback listener ) {
		reset(from.center(),
				Dungeon.level.solid[to] ? DungeonTilemap.raisedTileCenterToWorld(to) : DungeonTilemap.raisedTileCenterToWorld(to),
				item, listener );
	}

	public void reset( int from, Visual to, Item item, Callback listener ) {
		reset(Dungeon.level.solid[from] ? DungeonTilemap.raisedTileCenterToWorld(from) : DungeonTilemap.raisedTileCenterToWorld(from),
				to.center(),
				item, listener );
	}

	public void reset( Visual from, Visual to, Item item, Callback listener ) {
		reset(from.center(), to.center(), item, listener );
	}

	public void reset( PointF from, PointF to, Item item, Callback listener) {
		revive();

		if (item == null)   view(0, null);
		else                view( item );

		setup( from,
				to,
				item,
				listener );
	}
	
	private static final int DEFAULT_ANGULAR_SPEED = 720;
	
	private static final HashMap<Class<?extends Item>, Integer> ANGULAR_SPEEDS = new HashMap<>();
	static {
		ANGULAR_SPEEDS.put(Dart.class,          0);
		ANGULAR_SPEEDS.put(ThrowingKnife.class, 0);
		ANGULAR_SPEEDS.put(ThrowingSpike.class, 0);
		ANGULAR_SPEEDS.put(FishingSpear.class,  0);
		ANGULAR_SPEEDS.put(ThrowingSpear.class, 0);
		ANGULAR_SPEEDS.put(Kunai.class,         0);
		ANGULAR_SPEEDS.put(Javelin.class,       0);
		ANGULAR_SPEEDS.put(Trident.class,       0);
		ANGULAR_SPEEDS.put(PhantomSpear.class,  0);
		ANGULAR_SPEEDS.put(RunicBladeMkII.RunicMissile.class, 0);
		ANGULAR_SPEEDS.put(ExoKnife.RunicMissile.class, 0);
		ANGULAR_SPEEDS.put(RobotTransform.RunicMissile.class, 0);
		ANGULAR_SPEEDS.put(TrueTerminusBlade.TerminusMissile.class, 0);

		ANGULAR_SPEEDS.put(SpiritBow.SpiritArrow.class,       0);
		ANGULAR_SPEEDS.put(ScorpioSprite.ScorpioShot.class,   0);
		ANGULAR_SPEEDS.put(RatKingBossSprite.ScorpioShot.class, 0);
		ANGULAR_SPEEDS.put(NerfGun.NerfAmmo.class,          0);

		ANGULAR_SPEEDS.put(HolyLance.HolyLanceVFX.class,      0);

		//720 is default

		ANGULAR_SPEEDS.put(GnollGeomancer.Boulder.class,   90);

		ANGULAR_SPEEDS.put(HeavyBoomerang.class,1440);
		ANGULAR_SPEEDS.put(Bolas.class,         1440);
		ANGULAR_SPEEDS.put(HomingBoomerang.class, 1440);

		ANGULAR_SPEEDS.put(Shuriken.class,      2160);
		ANGULAR_SPEEDS.put(SpectralBlades.BirbBlade.class, 0);

		ANGULAR_SPEEDS.put(TenguSprite.TenguShuriken.class,      2160);
	}

	//TODO it might be nice to have a source and destination angle, to improve thrown weapon visuals
	private void setup( PointF from, PointF to, Item item, Callback listener ){

		originToCenter();

		//adjust points so they work with the center of the missile sprite, not the corner
		from.x -= width()/2;
		to.x -= width()/2;
		from.y -= height()/2;
		to.y -= height()/2;

		this.callback = listener;

		point( from );

		PointF d = PointF.diff( to, from );
		speed.set(d).normalize().scale(SPEED);
		
		angularSpeed = DEFAULT_ANGULAR_SPEED;
		for (Class<?extends Item> cls : ANGULAR_SPEEDS.keySet()){
			if (cls.isAssignableFrom(item.getClass())){
				angularSpeed = ANGULAR_SPEEDS.get(cls);
				break;
			}
		}
		
		angle = 135 - (float)(Math.atan2( d.x, d.y ) / 3.1415926 * 180);
		
		if (d.x >= 0){
			flipHorizontal = false;
			updateFrame();
			
		} else {
			angularSpeed = -angularSpeed;
			angle += 90;
			flipHorizontal = true;
			updateFrame();
		}

		if (item instanceof GnollGeomancer.Boulder){
			angle = 0;
			flipHorizontal = false;
			updateFrame();
		}

		float speed = SPEED;
		if (item instanceof RobotTransform.RunicMissile){
			speed *= 4f;
		}
		if (item instanceof NerfGun.Disc){
			speed *= 1.75f;
		}
		if (item instanceof NerfGun.NerfDart){
			speed *= 2.25f;
		}
		if (item instanceof NerfGun.SmallDart){
			speed *= 3.66f;
		}
		if (item instanceof CrossbowAmmo
				&& Crossbow.find(Dungeon.hero) != null){
			speed *= 3f;
			
		} else if (item instanceof SpiritBow.SpiritArrow
				|| item instanceof ScorpioSprite.ScorpioShot
				|| item instanceof TenguSprite.TenguShuriken
				|| item instanceof ExoKnife.RunicMissile
				|| item instanceof TrueTerminusBlade.TerminusMissile
				|| item instanceof Slingshot.Stone){
			speed *= 1.5f;
		} else if (item instanceof SpectralBlades.BirbBlade){
			speed *= 0.66f;
		} else if (item instanceof StarPieces){
			speed *= Random.Float(0.5f, 1.75f);
		}
		
		PosTweener tweener = new PosTweener( this, to, d.length() / speed );
		tweener.listener = this;
		parent.add( tweener );
	}

	@Override
	public void onComplete( Tweener tweener ) {
		kill();
		if (callback != null) {
			callback.call();
		}
	}
}
