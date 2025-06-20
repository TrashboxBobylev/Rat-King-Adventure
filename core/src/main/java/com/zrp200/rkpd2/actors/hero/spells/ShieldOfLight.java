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

package com.zrp200.rkpd2.actors.hero.spells;

import com.watabou.noosa.Image;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.Arrays;
import java.util.List;

public class ShieldOfLight extends TargetedClericSpell {

	public static ShieldOfLight INSTANCE = new ShieldOfLight();

	@Override
	public int icon() {
		return HeroIcon.SHIELD_OF_LIGHT;
	}

	@Override
	public void tintIcon(HeroIcon icon) { if (SpellEmpower.isActive()) icon.tint(0, .33f); }

	@Override
	public int targetingFlags() {
		return Ballistica.STOP_TARGET;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && (SpellEmpower.isActive() || hero.canHaveTalent(Talent.SHIELD_OF_LIGHT));
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {

		if (target == null){
			return;
		}

		Char ch = Actor.findChar(target);
		if (ch == null || ch.alignment == Char.Alignment.ALLY || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		QuickSlotButton.target(ch);

		Sample.INSTANCE.play(Assets.Sounds.READ);
		hero.sprite.operate(hero.pos);

		affectChar(hero, ch);

		hero.busy();
		hero.sprite.operate(hero.pos);

		Char ally = PowerOfMany.getPoweredAlly();
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
			affectChar(ally, ch);
		}

		onSpellCast(tome, hero);

	}

	public void affectChar(Char ch, Char target) {
		Class<? extends ShieldOfLightTracker> buffClass = SpellEmpower.isActive() ? DivineShield.class : ShieldOfLightTracker.class;
  		ShieldOfLightTracker existing = null;
		for (ShieldOfLightTracker buff : ch.buffs(buffClass, true)) {
			if (buff.object == target.id()) {
				existing = buff;
				break;
			}
		}
		if (existing == null) (existing = Buff.append(ch, buffClass)).object = target.id();
		//1 turn less as the casting is instant
		existing.spend(existing.getDuration() - 1);
		ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.15f, 6);
	}

	public static int min() { return 2 + Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT); }
	public static int max() { return 2 * min(); }

	@Override
	protected List<Object> getDescArgs() {
		return Arrays.asList(
				min(),
				max(),
				(int) new ShieldOfLightTracker().getDuration(),
				(int) new DivineShield().getMaxDurability()
		);
	}

	// fixme it would be better if the buffs were kept on the target instead of the hero
	//  that would clean up some of the weird behavior and make it possible to show vfx for it

	public static class ShieldOfLightTracker extends FlavourBuff {

		public int object = 0;

		protected float getDuration() { return 10; }

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.LIGHT_SHIELD;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (getDuration() - visualcooldown()) / getDuration());
		}

		public static ShieldOfLightTracker find(Char ch, Char target) {
			for (ShieldOfLightTracker buff : ch.buffs(ShieldOfLightTracker.class, true)) {
				if (buff.object == target.id()) return buff;
			}
			return null;
		}

		private static final String OBJECT  = "object";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( OBJECT, object );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			object = bundle.getInt( OBJECT );
		}

	}

	public static class DivineShield extends ShieldOfLightTracker {

		{
			announced = true;
		}

		@Override
		public void tintIcon(Image icon) { icon.tint(0, .33f); }

		public float getMaxDurability() {
			// 2 / 5 / 8 hits
			return 2 + 3 * Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT);
		}
		public int getDurability() { return (int)Math.ceil(visualcooldown() / getDurabilityPerHit()); }
		private float getDurabilityPerHit() { return getDuration() / getMaxDurability(); }

		@Override
		public String desc() {
			return Messages.get(this, "desc",
					getDurability(),
					(int)Math.ceil(1 + cooldown() % getDurabilityPerHit())
			);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(getDurability());
		}

		public synchronized void use(Char ch, Callback callback) {
			if (active == this) active = null;


			if (callback != null) {
				if (!ch.sprite.visible) callback.call();
				else {
					add(new Actor() {
						{
							actPriority = VFX_PRIO;
						}
						@Override
						protected boolean act() {
							remove(this);
							return false;
						}

						@Override
						protected void onRemove() {
							ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.15f, 6);
							Sample.INSTANCE.play(Assets.Sounds.READ);
							ch.sprite.parent.add(new Delayer(0.45f) {
								@Override
								protected void onComplete() {
									callback.call();
									next();
								}
							});
						}
					});
				}
			} else {
				ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.15f, 6);
				Sample.INSTANCE.play(Assets.Sounds.READ);
			}

			int left = getDurability() - 1;
			if (left <= 0) detach();
			else {
				spend(left * getDurabilityPerHit() - cooldown());
			}
		}

		public static DivineShield find(Char ch, Char target) {
			for (DivineShield buff : ch.buffs(DivineShield.class)) {
				if (buff.object == target.id()) return buff;
			}
			if (ch == Dungeon.hero || ch.alignment != Dungeon.hero.alignment) return null;
			return find(Dungeon.hero, target);
		}

		private static ShieldOfLight.DivineShield active = null;

		// defenseProc doesn't take a target :/

		public static synchronized boolean tryActivate(Char ch, Char target) {
			active = find(ch, target);
			if (active == null) return false;
			add(() -> active = null);
			return true;
		}

		public static boolean tryUse(Char ch) {
			return tryUse(ch, null);
		}

		public static synchronized boolean tryUse(Char ch, Callback reflect) {
			if (active != null) {
				active.use(ch, reflect);
				return true;
			}
			return false;
		}

		public static boolean tryUse(Char ch, Char enemy, Callback callback) {
			return ch != enemy && tryActivate(ch, enemy) && tryUse(ch, callback);
		}
	}

}
