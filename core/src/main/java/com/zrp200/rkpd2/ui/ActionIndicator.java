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

import com.watabou.input.GameAction;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.SPDAction;
import com.zrp200.rkpd2.actors.buffs.Berserk;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.Momentum;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.buffs.Preparation;
import com.zrp200.rkpd2.actors.buffs.RKChampionBuff;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.buffs.SnipersMark;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.items.artifacts.CloakOfShadows;
import com.zrp200.rkpd2.items.artifacts.KromerCloak;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;

public class ActionIndicator extends Tag {

	Visual primaryVis;
	Visual secondVis;

	public static Action action;
	public static ActionIndicator instance;

	public ActionIndicator() {
		super( 0 );

		instance = this;

		setSize( SIZE, SIZE );
		visible = false;
	}
	
	@Override
	public GameAction keyAction() {
		return SPDAction.TAG_ACTION;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		instance = null;

		longClickListener.destroy();
	}
	
	@Override
	protected synchronized void layout() {
		super.layout();
		
		if (primaryVis != null){
			if (!flipped)   primaryVis.x = x + (SIZE - primaryVis.width()) / 2f + 1;
			else            primaryVis.x = x + width - (SIZE + primaryVis.width()) / 2f - 1;
			primaryVis.y = y + (height - primaryVis.height()) / 2f;
			PixelScene.align(primaryVis);
			if (secondVis != null){
				if (secondVis.width() > 16) secondVis.x = primaryVis.center().x - secondVis.width()/2f;
				else                        secondVis.x = primaryVis.center().x + 8 - secondVis.width();
				if (secondVis instanceof BitmapText){
					//need a special case here for text unfortunately
					secondVis.y = primaryVis.center().y + 8 - ((BitmapText) secondVis).baseLine();
				} else {
					secondVis.y = primaryVis.center().y + 8 - secondVis.height();
				}
				PixelScene.align(secondVis);
			}
		}
	}
	
	private boolean needsRefresh = false;
	
	@Override
	public void update() {
		super.update();

		synchronized (ActionIndicator.class) {
			// auto clear unusable actions
			if (action != null && !action.usable()) clearAction(action);

			if (!visible && action != null) {
				visible = true;
				needsRefresh = true;
				flash();
			} else {
				visible = action != null;
			}

			if (needsRefresh) {
				if (primaryVis != null) {
					primaryVis.destroy();
					primaryVis.killAndErase();
					primaryVis = null;
				}
				if (secondVis != null) {
					secondVis.destroy();
					secondVis.killAndErase();
					secondVis = null;
				}
				if (action != null) {
					primaryVis = action.primaryVisual();
					add(primaryVis);

					secondVis = action.secondaryVisual();
					if (secondVis != null) {
						add(secondVis);
					}

					setColor(action.indicatorColor());
				}

				layout();
				needsRefresh = false;
			}

			if (!Dungeon.hero.ready) {
				if (primaryVis != null) primaryVis.alpha(0.5f);
				if (secondVis != null) secondVis.alpha(0.5f);
			} else {
				if (primaryVis != null) primaryVis.alpha(1f);
				if (secondVis != null) secondVis.alpha(1f);
			}
		}

	}

	@Override
	protected void onClick() {
		super.onClick();
		if (action != null && Dungeon.hero.ready) {
			action.doAction();
		}
	}

	@Override
	protected String hoverText() {
		String text = (action == null ? null : action.actionName());
		if (text != null){
			return Messages.titleCase(text);
		} else {
			return null;
		}
	}

	@Override
	protected boolean onLongClick() {
		return findAction(true);
	}

	private final Button longClickListener = new Button() {
		@Override public GameAction keyAction() { return SPDAction.TAG_CYCLE; }
		@Override protected void onClick()      { findAction(true); }
	};

	public static boolean setAction(Action action){
		if(action == null || !action.usable()) return false;
		synchronized (ActionIndicator.class) {
			ActionIndicator.action = action;
			refresh();
		}
		return true;
	}

	// list of action buffs that we should replace it with.
	private static final Class<?extends Buff>[] actionBuffClasses = new Class[]{
			Preparation.class,
			SnipersMark.class,
			Combo.class,
			Momentum.class,
			MonkEnergy.class,
			Berserk.class,
			MeleeWeapon.Charger.class,
			RobotBuff.class,
			HighnessBuff.class,
			BrawlerBuff.class,
			RKChampionBuff.class,
			WarriorParry.class,
			CloakOfShadows.cloakRecharge.class,
			KromerCloak.cloakRecharge.class
	};
	private static boolean findAction(boolean cycle) {
		if(Dungeon.hero == null) return false;
		if(action == null) cycle = false;
		int start = -1;
		if(cycle) while(++start < actionBuffClasses.length && !actionBuffClasses[start].isInstance(action));

		for(int i = (start+1)%actionBuffClasses.length; i != start && i < actionBuffClasses.length; i++) {
			for(Buff b : Dungeon.hero.buffs(actionBuffClasses[i])) {
				if( b != action && setAction( (Action) b ) ) return true;
			}
			if(cycle && i+1 == actionBuffClasses.length) i = -1;
		}
		return false;
	}

	public static void clearAction(){
		clearAction(null);
	}

	public static void clearAction(Action action){
		synchronized (ActionIndicator.class) {
			if (action == null || ActionIndicator.action == action) {
				ActionIndicator.action = null;
				findAction(false);
			}
		}
	}

	public static void refresh(){
		synchronized (ActionIndicator.class) {
			if (instance != null) {
				instance.needsRefresh = true;
			}
		}
	}

	public interface Action {

		default String actionName() { return Messages.get(this, "action_name"); };

		default int actionIcon(){
			return HeroIcon.NONE;
		}

		//usually just a static icon, unless overridden
		default Visual primaryVisual(){
			return new HeroIcon(this);
		}

		//a smaller visual on the bottom-right, usually a tiny icon or bitmap text
		default Visual secondaryVisual(){
			return null; //no second visual by default
		}

		int indicatorColor();

		void doAction();

		default boolean usable() { return true; }
		default boolean isSelectable() { return action != this && usable(); }
	}

}
