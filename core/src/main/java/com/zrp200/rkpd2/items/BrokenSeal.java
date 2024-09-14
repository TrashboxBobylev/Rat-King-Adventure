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

package com.zrp200.rkpd2.items;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Regeneration;
import com.zrp200.rkpd2.actors.buffs.ShieldBuff;
import com.zrp200.rkpd2.actors.hero.Belongings;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndBag;
import com.zrp200.rkpd2.windows.WndOptions;
import com.zrp200.rkpd2.windows.WndUseItem;

import java.util.ArrayList;
import java.util.Arrays;

import static com.zrp200.rkpd2.Dungeon.hero;

public class BrokenSeal extends Item {

	public static final String AC_AFFIX = "AFFIX";

	//only to be used from the quickslot, for tutorial purposes mostly.
	public static final String AC_INFO = "INFO_WINDOW";

	{
		image = ItemSpriteSheet.SEAL;

		cursedKnown = levelKnown = true;
		unique = true;
		bones = false;

		defaultAction = AC_INFO;
	}

	private Armor.Glyph glyph;

	public boolean canTransferGlyph(){
		if (glyph == null){
			return false;
		}
		if (hero.hasTalent(Talent.RUNIC_TRANSFERENCE) || hero.pointsInTalent(Talent.POWER_WITHIN) == 2){
			return true;
		} else if (hero.pointsInTalent(Talent.POWER_WITHIN) == 1
			&& (Arrays.asList(Armor.Glyph.common).contains(glyph.getClass())
				|| Arrays.asList(Armor.Glyph.uncommon).contains(glyph.getClass()))){
			return true;
		} else {
			return false;
		}
	}

	public Armor.Glyph getGlyph(){
		return glyph;
	}

	public void setGlyph( Armor.Glyph glyph ){
		this.glyph = glyph;
	}

	/** Amount of shield given by just talents */
	public static int maxShieldFromTalents(boolean armorAttached) {
		// iron will is 1 (0+1) / 3 (1+2) / 5 (2+3)
		// noble cause is 0/1/2
		return armorAttached || !hero.heroClass.is(HeroClass.WARRIOR) ?
				hero.pointsInTalent(Talent.NOBLE_CAUSE, Talent.IRON_WILL)
						+ hero.shiftedPoints(Talent.IRON_WILL)
				: 0;
	}

	public int maxShield( int armTier, int armLvl ){
		return armTier + armLvl + maxShieldFromTalents(true);
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return glyph != null ? glyph.glowing() : null;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions =  super.actions(hero);
		actions.add(AC_AFFIX);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_AFFIX)){
			curItem = this;
			GameScene.selectItem(armorSelector);
		} else if (action.equals(AC_INFO)) {
			GameScene.show(new WndUseItem(null, this));
		}
	}

	@Override
	//scroll of upgrade can be used directly once, same as upgrading armor the seal is affixed to then removing it.
	public boolean isUpgradable() {
		return level() == 0;
	}

	protected static WndBag.ItemSelector armorSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return  Messages.get(BrokenSeal.class, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Armor;
		}

		@Override
		public void onSelect( Item item ) {
			BrokenSeal seal = (BrokenSeal) curItem;
			if (item != null && item instanceof Armor) {
				Armor armor = (Armor)item;
				if (!armor.levelKnown){
					GLog.w(Messages.get(BrokenSeal.class, "unknown_armor"));

				} else if (armor.cursed && (seal.getGlyph() == null || !seal.getGlyph().curse())){
					GLog.w(Messages.get(BrokenSeal.class, "cursed_armor"));

				} else if (armor.glyph != null && seal.getGlyph() != null
						&& armor.glyph.getClass() != seal.getGlyph().getClass()) {
					GameScene.show(new WndOptions(new ItemSprite(seal),
							Messages.get(BrokenSeal.class, "choose_title"),
							Messages.get(BrokenSeal.class, "choose_desc") + (hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE) < 2 ? "\n\n" + Messages.get(BrokenSeal.class, "lose_warning"):""),
							armor.glyph.name(),
							seal.getGlyph().name()){
						@Override
						protected void onSelect(int index) {
							if (index == 0) seal.setGlyph(null);
							//if index is 1 or runic transference is maxed, then the glyph transfer happens in affixSeal

							GLog.p(Messages.get(BrokenSeal.class, "affix"));
							hero.sprite.operate(hero.pos);
							Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
							armor.affixSeal(seal);
							seal.detach(hero.belongings.backpack);
						}
					});

				} else {
					GLog.p(Messages.get(BrokenSeal.class, "affix"));
					hero.sprite.operate(hero.pos);
					Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
					armor.affixSeal((BrokenSeal)curItem);
					curItem.detach(hero.belongings.backpack);
				}
			}
		}
	};

	@Override
	public String desc() {
		HeroClass heroClass = hero.heroClass;
		return Messages.get(this, "desc",
				heroClass.is(HeroClass.WARRIOR) ? " from the glorious king of rats" : "",
				heroClass.title());
	}

	private static final String GLYPH = "glyph";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(GLYPH, glyph);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		glyph = (Armor.Glyph)bundle.get(GLYPH);
	}

	public static class WarriorShield extends ShieldBuff {

		private Armor armor;
		private float partialShield;

		private static final float RECHARGE_RATE = 30;

		public float getRechargeRate() {
			float baseSpeed = (RECHARGE_RATE * (1 - ((Hero) target).shiftedPoints(Talent.IRON_WILL) / (float) maxShield()));
			if (((Hero)target).hasTalent(Talent.WILLPOWER_OF_INJURED)){
				float boost = 0.5f * ((Hero) target).pointsInTalent(Talent.WILLPOWER_OF_INJURED);
				baseSpeed /= 1f + boost * Math.max(1f, ((float) (target.HT - target.HP) / target.HT)*1.1f);
			}
			return 1f/baseSpeed;
		}

		@Override
		public synchronized boolean act() {
			if (Regeneration.regenOn() && shielding() < maxShield()) {
				float rechargeRate = getRechargeRate();
				partialShield += rechargeRate; // this adjusts the seal recharge rate.
			}
			
			while (partialShield >= 1){
				incShield();
				partialShield--;
			}
			
			if (shielding() <= 0 && maxShield() <= 0){
				detach();
			}
			
			spend(TICK);
			return true;
		}
		
		public synchronized void supercharge(int maxShield){
			if (maxShield > shielding()){
				setShield(maxShield);
			}
		}

		public synchronized void setArmor(Armor arm){
			armor = arm;
		}

		public synchronized int maxShield() {
			//metamorphed iron will logic

			if (armor != null && armor.isEquipped((Hero)target) && armor.checkSeal() != null) {
				return armor.checkSeal().maxShield(armor.tier, armor.level());
			} else {
				return maxShieldFromTalents(false);
			}
		}
		
		@Override
		//logic edited slightly as buff should not detach
		public int absorbDamage(int dmg) {
			if (shielding() <= 0) return dmg;

			if (shielding() >= dmg){
				decShield(dmg);
				dmg = 0;
			} else {
				dmg -= shielding();
				decShield(shielding());
			}
			return dmg;
		}
	}
}
