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

package com.zrp200.rkpd2.ui;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.SPDAction;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.windows.WndKeyBindings;
import com.watabou.input.GameAction;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

//FIXME needs a refactor, lots of weird thread interaction here.
public class AttackIndicator extends Tag {
	
	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;

	private static float delay;
	
	private static AttackIndicator instance;
	
	private CharSprite sprite = null;
	
	private Mob lastTarget;
	private ArrayList<Mob> candidates = new ArrayList<>();
	
	public AttackIndicator() {
		super( DangerIndicator.COLOR );

		synchronized (this) {
			instance = this;
			lastTarget = null;

			setSize(SIZE, SIZE);
			visible(false);
			enable(false);
		}
	}
	
	@Override
	public GameAction keyAction() {
		return SPDAction.TAG_ATTACK;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
	}
	
	@Override
	protected synchronized void layout() {
		super.layout();

		if (sprite != null) {
			if (!flipped)   sprite.x = x + (SIZE - sprite.width()) / 2f + 1;
			else            sprite.x = x + width - (SIZE + sprite.width()) / 2f - 1;
			sprite.y = y + (height - sprite.height()) / 2f;
			PixelScene.align(sprite);
		}
	}
	
	@Override
	public synchronized void update() {
		super.update();

		if (!bg.visible){
			if (sprite != null) sprite.visible = false;
			enable(false);
			if (delay > 0f) delay -= Game.elapsed;
			if (delay <= 0f) active = false;
		} else {
			delay = 0.75f;
			active = true;
			if (bg.width > 0 && sprite != null)sprite.visible = true;

			if (Dungeon.hero.isAlive()) {

				enable(Dungeon.hero.ready);

			} else {
				visible( false );
				enable( false );
			}
		}
	}
	
	private synchronized void checkEnemies() {

		candidates.clear();
		int v = Dungeon.hero.visibleEnemies();
		for (int i=0; i < v; i++) {
			Mob mob = Dungeon.hero.visibleEnemy( i );
			if ( Dungeon.hero.canAttack( mob) ) {
				candidates.add( mob );
			}
		}
		
		if (lastTarget == null || !candidates.contains( lastTarget )) {
			if (candidates.isEmpty()) {
				lastTarget = null;
			} else {
				active = true;
				lastTarget = Random.element( candidates );
				updateImage();
				flash();
			}
		} else {
			active = true;
			if (!bg.visible) {
				flash();
			}
		}
		
		visible( lastTarget != null );
		enable( bg.visible );
	}
	
	private synchronized void updateImage() {
		
		if (sprite != null) {
			sprite.killAndErase();
			sprite = null;
		}
		
		sprite = Reflection.newInstance(lastTarget.spriteClass);
		active = true;
		sprite.linkVisuals(lastTarget);
		sprite.idle();
		sprite.paused = true;
		sprite.visible = bg.visible;

		if (sprite.width() > 20 || sprite.height() > 20){
			sprite.scale.set(PixelScene.align(20f/Math.max(sprite.width(), sprite.height())));
		}

		add( sprite );

		layout();
	}
	
	private boolean enabled = true;
	private synchronized void enable( boolean value ) {
		enabled = value;
		if (sprite != null) {
			sprite.alpha( value ? ENABLED : DISABLED );
		}
	}
	
	private synchronized void visible( boolean value ) {
		bg.visible = value;
	}
	
	@Override
	protected void onClick() {
		super.onClick();
		if (enabled && Dungeon.hero.ready) {
			if (Dungeon.hero.handle( lastTarget.pos )) {
				Dungeon.hero.next();
			}
		}
	}

	@Override
	protected String hoverText() {
		return Messages.titleCase(Messages.get(WndKeyBindings.class, "tag_attack"));
	}

	public static void target(Char target ) {
		if (!(target instanceof Mob)) return;
		synchronized (instance) {
			instance.lastTarget = (Mob) target;
			instance.updateImage();

			QuickSlotButton.target(target);
		}
	}
	
	public static void updateState() {
		instance.checkEnemies();
	}
}
