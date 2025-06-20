/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.Trinity;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.trinkets.WondrousResin;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class MindForm extends ClericSpell {

	public static MindForm INSTANCE = new MindForm();

	@Override
	public int icon() {
		return HeroIcon.MIND_FORM;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", itemLevel()) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	@Override
	public float chargeUse(Hero hero) {
		return 3;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.hasTalent(Talent.MIND_FORM);
	}

	public static int effectLevel(){
		return 2 + Dungeon.hero.pointsInTalent(Talent.MIND_FORM);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		GameScene.show(new Trinity.WndItemtypeSelect(tome, this));

	}

	public static int itemLevel(){
		return 2 + Dungeon.hero.pointsInTalent(Talent.MIND_FORM);
	}

	//TODO selecting
	public static class targetSelector extends CellSelector.Listener {

		private Bundlable effect;

		public void setEffect(Bundlable effect){
			this.effect = effect;
		}

		private Wand wand(){
			if (effect instanceof Wand){
				((Wand) effect).level(effectLevel());
				((Wand) effect).curCharges = ((Wand) effect).maxCharges;
				((Wand) effect).identify(false);
				return (Wand)effect;
			}
			return null;
		}

		private MissileWeapon thrown(){
			if (effect instanceof MissileWeapon){
				((MissileWeapon) effect).level(effectLevel());
				((MissileWeapon) effect).repair(100);
				((MissileWeapon) effect).identify(false);
				((MissileWeapon) effect).spawnedForEffect = true;
				return (MissileWeapon) effect;
			}
			return null;
		}

		@Override
		public void onSelect(Integer target) {
			if (target == null){
				return;
			}
			if (wand() != null){
				Wand wand = wand();
				if (wand.tryToZap(Dungeon.hero, target)) {

					final Ballistica shot = new Ballistica( Dungeon.hero.pos, target, wand.collisionProperties(target));
					int cell = shot.collisionPos;

					if (target == Dungeon.hero.pos || cell == Dungeon.hero.pos) {
						GLog.i( Messages.get(Wand.class, "self_target") );
						return;
					}

					Dungeon.hero.sprite.zap(cell);

					//attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
					if (Actor.findChar(target) != null)
						QuickSlotButton.target(Actor.findChar(target));
					else
						QuickSlotButton.target(Actor.findChar(cell));

					wand.fx(shot, new Callback() {
						public void call() {
							wand.onZap(shot);
							if (Random.Float() < WondrousResin.extraCurseEffectChance()){
								WondrousResin.forcePositive = true;
								CursedWand.cursedZap(wand,
										Dungeon.hero,
										new Ballistica(Dungeon.hero.pos, cell, Ballistica.MAGIC_BOLT), new Callback() {
											@Override
											public void call() {
												WondrousResin.forcePositive = false;
											}
										});
							}
							((ClassArmor)Dungeon.hero.belongings.armor()).charge -= Trinity.trinityChargeUsePerEffect(wand.getClass());
							wand.wandUsed();
						}
					});
				}
			} else if (thrown() != null){
				MissileWeapon thrown = thrown();
				thrown.cast(Dungeon.hero, target);
				((ClassArmor)Dungeon.hero.belongings.armor()).charge -= Trinity.trinityChargeUsePerEffect(thrown.getClass());
			}
		}

		@Override
		public String prompt() {
			if (wand() != null){
				return Messages.get(Wand.class, "prompt");
			} else {
				return Messages.get(Item.class, "prompt");
			}
		}
	}

}
