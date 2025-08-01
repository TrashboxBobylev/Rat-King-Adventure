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

package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.SelectableCell;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.zrp200.rkpd2.Dungeon.hero;

public class SnipersMark extends FlavourBuff implements ActionIndicator.Action {

	public int object = 0;
	public int level = 0;
	public float percentDmgBonus = 0;

	public void set(int object, int level, float percentDmgBonus) {;
		this.object = object;
		this.level = level;
		this.percentDmgBonus = percentDmgBonus;
		postpone(duration());
	}

	public static void remove(Char ch) {
		int id;
		SnipersMark mark = findByID( id = ch.id() );
		if(mark == null) return;
		mark.detach();
		tryMark(FreeTarget.class, id, mark.level, mark.percentDmgBonus);
	}

	private static <T extends SnipersMark> void tryMark(Class<T> cls, int id, int level, float percentDmgBonus) {
		if (cls == FreeTarget.class && !hero.hasTalent(Talent.MULTISHOT)) return;
        //noinspection unchecked
        T[] marks = (T[]) hero.buffs(cls, true).toArray(new SnipersMark[0]);
		int excess = marks.length - maxObjects();
		if (excess >= 0) {
			// tries to remove the 'least valuable' mark.
			Arrays.sort(marks, (a, b) -> Math.abs(a.percentDmgBonus - b.percentDmgBonus) > 0.01 ? Float.compare(a.percentDmgBonus, b.percentDmgBonus) // try to preserve higher damage.
					: a.level != b.level ? Integer.compare(a.level, b.level)
					//: a instanceof FreeTarget != b instanceof FreeTarget ? a instanceof FreeTarget ? 1 : -1 // free > standard
					: Float.compare( a.cooldown(), b.cooldown() ) // older < newer
			);
			do marks[0].detach(); while (--excess >= 0);
		}
		// append the new mark
		append(hero, cls).set(id, level, percentDmgBonus);
	}

	private static SnipersMark findByID(int id) {
		for( SnipersMark mark : hero.buffs(SnipersMark.class) ) {
			if(mark.object == id) return mark;
		}
		return null;
	}

	private Char getTarget() {
		return (Char)Actor.findById(object);
	}

	// TODO should I just sync all buffs together?!

	public static void add(Char ch, int level, float percentDmgBonus) {
		addTime(level);

		int id; SnipersMark existing = findByID( id = ch.id() );
		if(existing != null) {
			existing.level = level = Math.max(existing.level, level);
			existing.percentDmgBonus = percentDmgBonus = Math.max(existing.percentDmgBonus, percentDmgBonus);
		}
		if( !ch.isAlive() ) {
			if(existing != null) existing.detach();
			tryMark(FreeTarget.class, id, level, percentDmgBonus);
		}
		else if(existing != null) {
			ActionIndicator.setAction(existing);
		}
		else {
			tryMark(SnipersMark.class, id, level, percentDmgBonus);
		}
	}

	private static final String OBJECT    = "object";
	private static final String OBJECTS	  = "objects";
	private static final String LEVEL     = "level",
			LEVELS    = LEVEL+"s";
	private static final String BONUS    = "bonus",
		BONUS_ARRAY = BONUS + "array";

	public static final float DURATION = 4f;

	{
		type = buffType.POSITIVE;
	}

	private static int maxObjects() {
		// +0 1+0 1 / +1 1+1 / +2 2+2 / +3 4+4
		return Math.max(1, 1 << hero.pointsInTalent(Talent.MULTISHOT) - 1);
	}

	// todo implement extended time again.
	public float duration() {
		return DURATION + level;
	}
	// FIXME this is really fucked up.
	protected static void addTime(int level) {
		float time = DURATION+level;
		for(SnipersMark buff : hero.buffs(SnipersMark.class)) buff.postpone( Math.min(
				buff.duration(),
				buff.cooldown() + time
		));
	}

	@Override
	public boolean attachTo(Char target) {
		ActionIndicator.setAction(this);
		return super.attachTo(target);
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( OBJECT, object );
		bundle.put( BONUS, percentDmgBonus );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );

		level = bundle.getInt(LEVEL);
		percentDmgBonus = bundle.getFloat(BONUS);

		if(this instanceof FreeTarget) return;

		if( bundle.contains(OBJECT) ) {
			object = bundle.getInt(OBJECT);
		}
		else if(bundle.contains(OBJECTS)) {
			// split between several buffs.
			int[] objects = bundle.getIntArray(OBJECTS);
			if(objects.length == 0) {
				// restore as freetarget
				Buff.affect(Char.restoring, FreeTarget.class, cooldown()).level = level;
				attachAfterRestore = false;
				return;
			}

			int[] levels;
			if( bundle.contains(LEVELS) ) {
				levels = bundle.getIntArray(LEVELS);
				level = levels[0];
			}
			else Arrays.fill(levels = new int[objects.length], level);
			float[] bonusArray;
			if (bundle.contains(BONUS_ARRAY)) {
				bonusArray = bundle.getFloatArray(BONUS_ARRAY);
			} else {
				Arrays.fill(bonusArray = new float[objects.length], percentDmgBonus);
			}

			object = objects[0];

			for(int i=1; i < objects.length; i++) {
				SnipersMark mark = Buff.append(Char.restoring, SnipersMark.class, cooldown());
				mark.object = objects[i];
				mark.level = levels[i];
				mark.percentDmgBonus = bonusArray[i];
			}
		}
		else {
			// idk where the object went?!
			// this probably never runs but just in case.
			Buff.append(Char.restoring, FreeTarget.class, cooldown()).level = level;
			attachAfterRestore = false;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.MARK;
	}

	@Override
	public float iconFadePercent() {
		float duration = duration();
		return duration == 0 ? 0 : Math.max(0, (duration - visualcooldown()) / duration);
	}

	@Override
	public String desc() {
		HeroSubClass sub = Dungeon.hero.subClass;
		String[] args = new String[5];
		args[0] = sub.title();
		args[1] = sub.isExact(HeroSubClass.SNIPER) ? "she" : "he";
		args[2] = Messages.capitalize(args[1]);
		args[3] = sub.isExact(HeroSubClass.SNIPER) ? "her" : "his";
		args[4] = dispTurns();
		return Messages.get(this, "desc", (Object[])args);
	}

	@Override
	public String actionName() {
		SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);

		if (bow == null) return null;

		switch (bow.augment){
			case NONE: default:
				return Messages.get(this, "action_name_snapshot");
			case SPEED:
				return Messages.get(this, "action_name_volley");
			case DAMAGE:
				return Messages.get(this, "action_name_sniper");
		}
	}

	@Override
	public int actionIcon() {
		return HeroIcon.SNIPERS_MARK;
	}

	@Override
	public int indicatorColor() {
		return 0x444444;
	}

	// only one action handler can be active at a time.
	private static ActionHandler actionHandler;
	private class ActionHandler { // this is an inner class to keep access to the original buff.
		SnipersMark running;
		CellSelector.TargetedListener listener;

		// todo technically it could probably work with just using #object. Look into that?
		final HashMap<SnipersMark, Char> actionMap = new HashMap();
		final SpiritBow bow = hero.belongings.getItem(SpiritBow.class);

		HashSet<SelectableCell> selected = new HashSet();
		void select(SnipersMark m, Char ch) {
			SelectableCell c = new SelectableCell(ch.sprite);
			c.hardlight(1,0,0);
			selected.add(c);

			actionMap.put(m,ch);
		}
		void destroy() {
			actionHandler = null;
			for(SelectableCell c : selected) c.killAndErase();
			GameScene.ready();
		}

		final LinkedList<SnipersMark> queue = new LinkedList( hero.buffs(SnipersMark.class) );
		{
			Collections.sort(queue, (a, b) -> {
				// fixme does not take percent damage bonus into account
				// free-targets go after standard marks, since they have to choose their targets
				if(a instanceof FreeTarget != b instanceof FreeTarget) return a instanceof FreeTarget ? 1 : -1;
				if(!(a instanceof FreeTarget)) {
					// process closest one first; this ensures correct auto-targeting for later marks
					int diff = Integer.compare(
							Dungeon.level.distance(hero.pos, a.getTarget().pos),
							Dungeon.level.distance(hero.pos, b.getTarget().pos)
					);
					if (diff != 0) return diff;
				}
				int level = Integer.compare(a.level, b.level);
				// sorted by highest to lowest level, then soonest to expire to longest to expire.
				return level != 0 ? -level : Float.compare( a.cooldown(), b.cooldown() );
			});

			// this ensures that the 'skip' button consistently works the way I want it to.
			if(queue.size() > 1
					&& SnipersMark.this instanceof FreeTarget // otherwise we could get a bug where free target is processed before standard mark.
					&& queue.peek() == SnipersMark.this) {
				queue.push( queue.remove(1) );
			}
		}

		void doAction() {
			destroy();
			if ( actionMap.isEmpty() ) return;
			bow.shotCount = actionMap.size();
			hero.busy();
			for (Map.Entry<SnipersMark, Char> mapping : actionMap.entrySet()) {
				SnipersMark mark = mapping.getKey(); Char ch = mapping.getValue();
				mark.doSniperSpecial(hero, bow, ch, actionMap.values());
			}
		}

		void next() {
			running = queue.poll();
			if(running == null) doAction();
			else {
				GameScene.clearCellSelector(true);
				running.queueAction();
			}
		}

		boolean isTargeting(Char ch) {
			return actionMap.containsValue(ch);
		}
		boolean isValidTarget(Char ch) {
			return !isTargeting(ch) && canDoSniperSpecial(bow, ch, actionMap.values());
		}
	}

	// this is the safe way to create new actions. will prevent duplication of free-targets as well.

	@Override
	public void doAction() {
		if (hero == null) return;
		if(actionHandler == null) {
			actionHandler = new ActionHandler();
			if(actionHandler.bow != null && actionHandler.bow.knockArrow() != null) actionHandler.next();
			else actionHandler = null;
		}
		// this prevents stacking.
		else if(actionHandler.listener != null) {
			// mimic the effect of a similar quickslot-based override if at all possible and attempt to do the action now.
			List<Char> highlighted = actionHandler.listener.getHighlightedTargets();
			if(highlighted.size() == 1) actionHandler.select(actionHandler.running, highlighted.get(0));
			actionHandler.doAction();
		} else {
			queueAction();
		}
	}

	protected void queueAction() {
		Char ch = getTarget();
		if(actionHandler.isValidTarget(ch)) {
			actionHandler.select(this,ch);
		}
		actionHandler.next();
	}

	@Override public boolean isSelectable() {
		return ActionIndicator.Action.super.isSelectable()
				// kinda weird to be able to select identical actions.
				&& !(ActionIndicator.action instanceof SnipersMark);
	}

	public static class FreeTarget extends SnipersMark {

		@Override
		public float duration() {
			return super.duration() * hero.pointsInTalent(Talent.MULTISHOT);
		}

		// only difference is we don't care about object at all. it just exists.

		@Override
		public void tintIcon(Image icon) {
			icon.tint(0,1f,0,1/4f);
		}

		@Override
		public String desc() {
			return super.desc() + "\n\n" + Messages.get(this,"bonus_desc");
		}

		@Override
		protected void queueAction() {
			Objects.requireNonNull(actionHandler);
			GameScene.selectCell(actionHandler.listener = new CellSelector.TargetedListener() {
				{
					conflictTolerance = actionHandler.queue.size();
					readyOnSelect = false; // manually disable the mechanic that prevents stacking.
				}

				@Override protected boolean isValidTarget(Char ch) { return actionHandler.isValidTarget(ch); }

				@Override protected boolean noTargets() {
					if(actionHandler.queue.isEmpty() && actionHandler.actionMap.isEmpty()) {
						// #destroy resets the scene, and there's no highlighted targets by definition of this scenario.
						// prompt uselessly to give user a sense of control.
						// see #onInvalid
						return false;
					}
					// this covers the edge case where you have more marks than targets.
					actionHandler.next();
					return true;
				}

				@Override protected void action(Char ch) {
					actionHandler.select(FreeTarget.this, ch);
					actionHandler.next();
				}

				@Override protected void onCancel() {
					if(actionHandler != null) actionHandler.destroy();
				}

				@Override protected void onInvalid(int cell) {
					if(actionHandler == null) return;
					Char ch = Actor.findChar(cell);
					if(ch != null) {
						String message = actionHandler.isTargeting(ch)
								? "That character is already being targeted!"
								: "That character cannot be targeted.";
						GLog.w(message);
						// if there are no targets yet, just treat this like a 'standard' cancel.
						if(!actionHandler.actionMap.isEmpty()) {
							GameScene.clearCellSelector(true);
							queueAction();
							return;
						}
					}
					actionHandler.destroy();
				}

				@Override protected boolean canIgnore(Char ch) {
					// todo do I want to just set this to blanket true?
					return super.canIgnore(ch) || actionHandler.isTargeting(ch);
				}

				@Override public String prompt() {
					return Messages.get(SpiritBow.class, "prompt");
				}
			});
		}

	}

	// this should be called before doing the sniper special
	private static boolean canDoSniperSpecial(SpiritBow bow, Char ch, Collection<Char> toIgnore) {
		SpiritBow.SpiritArrow arrow = bow.knockArrow(toIgnore);
		return ch != null && arrow != null && QuickSlotButton.autoAim(ch, arrow) != -1;
	}
	// actual sniper special
	private void doSniperSpecial(Hero hero, SpiritBow bow, Char ch, Collection<Char> toIgnore) {
		SpiritBow.SpiritArrow arrow = bow.knockArrow(toIgnore); // need a unique arrow for every character.

		int cell = QuickSlotButton.autoAim(ch, arrow);
		arrow.sniperSpecialBonusDamage = percentDmgBonus;

		Buff.detach(hero, Preparation.class); // nope!

		arrow.cast(hero, cell);

		detach();
	}
}
