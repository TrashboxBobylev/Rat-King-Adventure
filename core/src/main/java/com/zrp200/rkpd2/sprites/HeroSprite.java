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

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.buffs.RobotTransform;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.items.weapon.melee.ExoKnife;
import com.zrp200.rkpd2.scenes.GameScene;

public class HeroSprite extends CharSprite {
	
	private static final int FRAME_WIDTH	= 12;
	private static final int FRAME_HEIGHT	= 15;

	public int frameWidth() { return FRAME_WIDTH; }
	public int frameHeight() { return FRAME_HEIGHT; }
	
	protected int runFramerate	= 20;
	
	protected TextureFilm tiers;
	
	protected Animation fly;
	protected Animation read;
	private int cellToAttack;

	public HeroSprite() {
		super();
		
		texture( Dungeon.hero.heroClass.spritesheet() );
		updateArmor();
		
		link( Dungeon.hero );

		if (ch.isAlive())
			idle();
		else
			die();
	}
	
	public void updateArmor() {

		TextureFilm film = new TextureFilm( tiers(), Dungeon.hero.tier(), FRAME_WIDTH, FRAME_HEIGHT );
		
		idle = new Animation( 1, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );
		
		run = new Animation( runFramerate, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );
		
		die = new Animation( 20, false );
		die.frames( film, 8, 9, 10, 11, 12, 11 );

		attack = new Animation(  RobotBuff.isVehicle() ? 50 : 15, false );
		attack.frames( film, 13, 14, 15, 0 );
		
		zap = attack.clone();
		
		operate = new Animation( 8, false );
		operate.frames( film, 16, 17, 16, 17 );
		
		fly = new Animation( 1, true );
		fly.frames( film, 18 );

		read = new Animation( 20, false );
		read.frames( film, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19 );
		
		if (Dungeon.hero.isAlive())
			idle();
		else
			die();
	}

	@Override
	public void attack( int cell ) {
		cellToAttack = cell;
		super.attack( cell );
	}

	@Override
	public void attack(int cell, Callback callback) {
		cellToAttack = cell;
		super.attack(cell, callback);
	}

	@Override
	public void onComplete(Animation anim) {
		if (ch instanceof Hero && anim == attack){
			if (((Hero) ch).belongings.weapon instanceof ExoKnife) {
				if ((ch.buff(BrawlerBuff.BrawlingTracker.class) != null)){
					super.onComplete(anim);
					return;
				}
				parent.recycle(MissileSprite.class).
						reset(this, cellToAttack, new ExoKnife.RunicMissile(), new Callback() {
							@Override
							public void call() {
								if (animCallback != null) {
									Callback executing = animCallback;
									animCallback = null;
									executing.call();
									return;
								}
								ch.onAttackComplete();
							}
						});
			} else if (RobotBuff.isVehicle()){
				parent.recycle(MissileSprite.class).
						reset(this, cellToAttack, new RobotTransform.RunicMissile(), ch::onAttackComplete);
			} else {
				super.onComplete(anim);
			}
		} else {
			super.onComplete( anim );
		}
	}

	@Override
	public void place( int p ) {
		super.place( p );
		if (Game.scene() instanceof GameScene) Camera.main.panFollow(this, 5f);
	}

	@Override
	public void move( int from, int to ) {
		super.move( from, to );
		if (ch != null && ch.flying && Dungeon.hero.subClass != HeroSubClass.DECEPTICON) {
			play( fly );
		}
		Camera.main.panFollow(this, 20f);
	}

	@Override
	public void idle() {
		super.idle();
		if (ch != null && ch.flying && Dungeon.hero.subClass != HeroSubClass.DECEPTICON) {
			play( fly );
		}
	}

	@Override
	public void jump( int from, int to, float height, float duration,  Callback callback ) {
		super.jump( from, to, height, duration, callback );
		play( fly );
		Camera.main.panFollow(this, 20f);
	}

	public synchronized void read() {
		animCallback = new Callback() {
			@Override
			public void call() {
				idle();
				ch.onOperateComplete();
			}
		};
		play( read );
	}

	@Override
	public void bloodBurstA(PointF from, int damage) {
		//Does nothing.

		/*
		 * This is both for visual clarity, and also for content ratings regarding violence
		 * towards human characters. The heroes are the only human or human-like characters which
		 * participate in combat, so removing all blood associated with them is a simple way to
		 * reduce the violence rating of the game.
		 */
	}

	@Override
	public void update() {
		sleeping = ch.isAlive() && ((Hero)ch).resting;
		
		super.update();
	}
	
	public void sprint( float speed ) {
		run.delay = 1f / speed / runFramerate;
	}
	
	public TextureFilm tiers() {
		if (tiers == null) {
			tiers = tiers(Assets.Sprites.ROGUE, FRAME_HEIGHT);
		}
		
		return tiers;
	}
	public static TextureFilm tiers(String spritesheet, int frameHeight) {
		SmartTexture texture = TextureCache.get( spritesheet );
		return new TextureFilm( texture, texture.width, frameHeight );
	}

	public static Image avatar( HeroClass cl, int armorTier ) {
		int frameHeight = FRAME_HEIGHT;
		int frameWidth = FRAME_WIDTH;
		if(cl == HeroClass.RAT_KING) {
			frameHeight = 17;
			frameWidth = 16;
			armorTier = 0;
		};
		RectF patch = tiers(cl.spritesheet(), frameHeight).get( armorTier );
		Image avatar = new Image( cl.spritesheet() );
		RectF frame = avatar.texture.uvRect( 1, 0, frameWidth, frameHeight );
		frame.shift( patch.left, patch.top );
		avatar.frame( frame );
		
		return avatar;
	}
}
