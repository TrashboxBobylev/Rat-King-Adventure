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

import static com.zrp200.rkpd2.Dungeon.hero;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Cooldown;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.effects.particles.SparkParticle;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class HolyLance extends MultiTargetSpell {

	public static final HolyLance INSTANCE = new HolyLance();

	@Override
	public int icon() {
		return HeroIcon.HOLY_LANCE;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo make icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	public String desc() {
		int min = 15 + 15* hero.pointsInTalent(Talent.HOLY_LANCE);
		int max = Math.round(27.5f + 27.5f* hero.pointsInTalent(Talent.HOLY_LANCE));
		return Messages.get(this, "desc", min, max) + "\n\n" + checkEmpowerMsg("cooldown") + "\n\n" + chargeUseDesc();
	}

	@Override
	public boolean isVisible(Hero hero) {
		return super.canCast(hero)
				&& hero.shiftedPoints(Talent.HOLY_LANCE) > (SpellEmpower.isActive() ? 0 : 1);
	}
	@Override
	public boolean canCast(Hero hero) {
		return isVisible(hero) && hero.buff(LanceCooldown.class) == null;
	}

	@Override
	public float chargeUse(Hero hero) {
		return totalCasts > 0 ? 2 * totalCasts : 4;
	}

	@Override
	protected int baseTargetingFlags() {
		return Ballistica.PROJECTILE;
	}

	{
		announced = true;
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		int collisionPos = targetingPos(hero, target);

		if (Actor.findChar( collisionPos ) == hero){
			GLog.i( Messages.get(Wand.class, "self_target") );
			return;
		}

		if (usesTargeting()) {
			if (Actor.findChar(collisionPos) != null) {
				QuickSlotButton.target(Actor.findChar(collisionPos));
			} else {
				QuickSlotButton.target(Actor.findChar(target));
			}
		}


		hero.sprite.zap( target, () -> {/* track animation*/} );
		hero.busy();

		Sample.INSTANCE.play(Assets.Sounds.ZAP);

		Char enemy = Actor.findChar(collisionPos);
		if (enemy != null) {
			((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).
					reset(hero.sprite,
							enemy.sprite,
							new HolyLanceVFX(),
							new Callback() {
								@Override
								public void call() {
									int min = 15 + 15* Dungeon.hero.pointsInTalent(Talent.HOLY_LANCE);
									int max = Math.round(27.5f + 27.5f* Dungeon.hero.pointsInTalent(Talent.HOLY_LANCE));
									if (Char.hasProp(enemy, Char.Property.UNDEAD) || Char.hasProp(enemy, Char.Property.DEMONIC)){
										min = max;
									}
									enemy.damage(Random.NormalIntRange(min, max), HolyLance.this);
									Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.8f, 1f) );
									Sample.INSTANCE.play( Assets.Sounds.HIT_STAB, 1, Random.Float(0.8f, 1f) );

									enemy.sprite.burst(0xFFFFFFFF, 10);
									if (enemy.isAlive() && totalCasts == 1) {
										// shoot again
										totalCasts++;
										hero.sprite.doAfterAnim(() -> onTargetSelected(tome, hero, target));
										return;
									}
									onSpellCast(tome, hero);
								}
							});
		} else {
			((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).
					reset(hero.sprite,
							target,
							new HolyLanceVFX(),
							new Callback() {
								@Override
								public void call() {
									Splash.at(target, 0xFFFFFFFF, 10);
									Dungeon.level.pressCell(collisionPos);
									onSpellCast(tome, hero);
								}
							});
		}
	}

	@Override
	public void onSpellComplete(HolyTome tome, Hero hero) {
		hero.spendAndNext(1f);
		Cooldown.affectHero(LanceCooldown.class);
	}

	public static class HolyLanceVFX extends Item {

		{
			image = ItemSpriteSheet.THROWING_SPIKE;
		}

		@Override
		public ItemSprite.Glowing glowing() {
			return new ItemSprite.Glowing(0xFFFFFF, 0.1f);
		}

		@Override
		public Emitter emitter() {
			Emitter emitter = new Emitter();
			emitter.pos( 5, 5, 0, 0);
			emitter.fillTarget = false;
			emitter.pour(SparkParticle.FACTORY, 0.025f);
			return emitter;
		}
	}

	public static class LanceCooldown extends Cooldown {

		@Override
		public int icon() {
			return BuffIndicator.TIME;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.67f, 0.67f, 0);
		}

		@Override
		public float duration() { return 15; }
	}
}
