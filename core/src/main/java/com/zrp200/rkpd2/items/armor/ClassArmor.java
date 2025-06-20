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

package com.zrp200.rkpd2.items.armor;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Regeneration;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.OmniAbility;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.Trinity;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.BrokenSeal;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.rings.RingOfEnergy;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.HeroSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndBag;
import com.zrp200.rkpd2.windows.WndChooseAbility;
import com.zrp200.rkpd2.windows.WndInfoArmorAbility;
import com.zrp200.rkpd2.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.hero;
import static com.zrp200.rkpd2.Dungeon.quickslot;
import static com.zrp200.rkpd2.actors.hero.abilities.rat_king.OmniAbility.additionalActions;

abstract public class ClassArmor extends Armor {

	private static final String AC_ABILITY = "ABILITY";
	private static final String AC_TRANSFER = "TRANSFER";

	{
		levelKnown = true;
		cursedKnown = true;
		defaultAction = AC_ABILITY;

		bones = false;
	}

	private Charger charger;
	public float charge = 0;

	public ClassArmor() {
		super( 5 );
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		charger = new Charger();
		charger.attachTo(ch);
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {
			if (charger != null){
				charger.detach();
				charger = null;
			}
			return true;

		} else {
			return false;

		}
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return user.armorAbility.targetedPos(user, dst);
	}

	public static ClassArmor upgrade (Hero owner, Armor armor ) {

		ClassArmor classArmor = null;

		switch (owner.heroClass) {
			case WARRIOR:
				classArmor = new WarriorArmor();
				break;
			case ROGUE:
				classArmor = new RogueArmor();
				break;
			case MAGE:
				classArmor = new MageArmor();
				break;
			case HUNTRESS:
				classArmor = new HuntressArmor();
				break;
			case DUELIST:
				classArmor = new DuelistArmor();
				break;
			case CLERIC:
				classArmor = new ClericArmor();
				break;
			case RAT_KING:
				classArmor = new RatKingArmor();

		}

		BrokenSeal seal = armor.checkSeal();
		if (seal != null) {
			classArmor.affixSeal(seal);
		}

		classArmor.level(armor.trueLevel());
		classArmor.tier = armor.tier;
		classArmor.augment = armor.augment;
		classArmor.inscribe(armor.glyph);
		if (armor.seal != null) {
			classArmor.seal = armor.seal;
		}
		classArmor.glyphHardened = armor.glyphHardened;
		classArmor.cursed = armor.cursed;
		classArmor.curseInfusionBonus = armor.curseInfusionBonus;
		classArmor.masteryPotionBonus = armor.masteryPotionBonus;
		if (armor.levelKnown && armor.cursedKnown) {
			classArmor.identify();
		} else {
			classArmor.levelKnown = armor.levelKnown;
			classArmor.cursedKnown = true;
		}

		classArmor.charge = 50;

		return classArmor;
	}

	private static final String ARMOR_TIER	= "armortier";
	private static final String CHARGE	    = "charge";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARMOR_TIER, tier );
		bundle.put( CHARGE, charge );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		tier = bundle.getInt( ARMOR_TIER );
		charge = bundle.getFloat(CHARGE);
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_ABILITY );
		actions.addAll( additionalActions().keySet() );
		actions.add( AC_TRANSFER );
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (hero.armorAbility != null && action.equals(AC_ABILITY)){
			return hero.armorAbility.actionName();
		} else {
			String actionName = super.actionName(action, hero);
			//overriding the default to suppress NULL so that OmniAbility can communicate with itself.
			//noinspection StringEquality
			return actionName != Messages.NO_TEXT_FOUND ? actionName : action;
		}
	}

	@Override
	public String status() {
		return Messages.format( "%.0f%%", Math.floor(charge) );
	}

	@Override
	public void execute(Hero hero) {
		if(additionalActions().isEmpty()) super.execute(hero);
		else {
			// omni-ability default action.
			OmniAbility omniAbility = (OmniAbility)hero.armorAbility;
			ArmorAbility activeAbility = omniAbility.activeAbility();
			usesTargeting = false; // manually handled.
			GameScene.show(new WndChooseAbility(null,this, omniAbility.name(),false) {
				@Override protected ArrayList<ArmorAbility> getArmorAbilities() {
					ArrayList<ArmorAbility> abilities = new ArrayList<>();
					abilities.add(activeAbility);
					abilities.addAll( OmniAbility.activeAbilities() );
					return abilities;
				}
				@Override protected WndInfoArmorAbility getAbilityInfo(ArmorAbility ability) {
					return new WndInfoArmorAbility(ability, OmniAbility::transferTalents);
				}
				@Override protected void selectAbility(ArmorAbility ability) {
					hide();
					if(ability.equals(activeAbility)) ClassArmor.super.execute(hero);
					else execute(hero, ability.actionName());
					// due to the delay, we need to manually enable targeting.
					int slotIdx = quickslot.getSlot(ClassArmor.this);
					if(slotIdx != -1 && usesTargeting) QuickSlotButton.useTargeting(slotIdx);
				}
			});
		}
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_ABILITY)){

			useAbility(hero, hero.armorAbility);
		} else if(additionalActions().containsKey(action)) {
			useAbility(hero, additionalActions().get(action));
		} else if (action.equals(AC_TRANSFER)){
			GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.CROWN),
					Messages.get(ClassArmor.class, "transfer_title"),
					Messages.get(ClassArmor.class, "transfer_desc"),
					Messages.get(ClassArmor.class, "transfer_prompt"),
					Messages.get(ClassArmor.class, "transfer_cancel")){
				@Override
				protected void onSelect(int index) {
					if (index == 0){
						GameScene.selectItem(new WndBag.ItemSelector() {
							@Override
							public String textPrompt() {
								return Messages.get(ClassArmor.class, "transfer_prompt");
							}

							@Override
							public boolean itemSelectable(Item item) {
								return item instanceof Armor;
							}

							@Override
							public void onSelect(Item item) {
								if (item == null || item == ClassArmor.this) return;

								Armor armor = (Armor)item;
								armor.detach(hero.belongings.backpack);
								if (hero.belongings.armor == armor){
									hero.belongings.armor = null;
									if (hero.sprite instanceof HeroSprite) {
										((HeroSprite) hero.sprite).updateArmor();
									}
								}
								level(armor.trueLevel());
								tier = armor.tier;
								augment = armor.augment;
								cursed = armor.cursed;
								curseInfusionBonus = armor.curseInfusionBonus;
								masteryPotionBonus = armor.masteryPotionBonus;
								if (armor.checkSeal() != null) {
									inscribe(armor.glyph);
									seal = armor.checkSeal();
								} else if (checkSeal() != null){
									//automates the process of detaching the seal manually
									// and re-affixing it to the new armor
									if (seal.level() > 0){
										int newLevel = trueLevel() + 1;
										level(newLevel);
										Badges.validateItemLevelAquired(ClassArmor.this);
									}

									//if both source and destination armor have glyphs
									// we assume the player wants the glyph on the destination armor
									// they can always manually detach first if they don't.
									// otherwise we automate glyph transfer just like upgrades
									if (armor.glyph == null && seal.canTransferGlyph()){
										//do nothing, keep our glyph
									} else {
										inscribe(armor.glyph);
										seal.setGlyph(null);
									}
								} else {
									inscribe(armor.glyph);
								}

								if (armor.levelKnown && armor.cursedKnown) {
									identify();
								} else {
									levelKnown = armor.levelKnown;
									cursedKnown = true;
								}

								GLog.p( Messages.get(ClassArmor.class, "transfer_complete") );
								hero.sprite.operate(hero.pos);
								hero.sprite.emitter().burst( Speck.factory( Speck.CROWN), 12 );
								Sample.INSTANCE.play( Assets.Sounds.EVOKE );
								hero.spend(Actor.TICK);
								hero.busy();

							}
						});
					}
				}
			});
		}
	}
	private void useAbility(Hero hero, ArmorAbility armorAbility) {
		//for pre-0.9.3 saves
		if (armorAbility == null){
			GameScene.show(new WndChooseAbility(null, this));
		} else {
			if (charge < armorAbility.chargeUse(hero)) {
				/*usesTargeting = false;
				GLog.w( Messages.get(this, "low_charge") );*/
				GLog.n("Rat King: I don't have time for this nonsense! I have a kingdom to run! CLASS ARMOR SUPERCHAARGE!!");
				charge += 100;
				hero.HP = Math.max( Math.min(hero.HP,1), hero.HP*2/3 );
				updateQuickslot();
				ScrollOfRecharging.charge(hero);
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
			}
			usesTargeting = armorAbility.useTargeting();
			armorAbility.use(this, hero);
		}
	}

	public void useCharge(Hero hero, ArmorAbility armorAbility, boolean recordUsed) {
		if (hero.armorAbility != null)
			charge -= armorAbility.chargeUse(hero);
		// This is a trigger for OmniAbility to transition to a new ability.
		if(recordUsed) OmniAbility.markAbilityUsed(armorAbility);
		updateQuickslot();
	}
	public void useCharge(Hero hero, ArmorAbility armorAbility) {
		useCharge(hero, armorAbility, true);
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (hero != null && hero.belongings.contains(this)) {
			ArmorAbility ability = hero.armorAbility;
			if (ability != null) {
				desc += "\n\n" + ability.shortDesc();
				float chargeUse = ability.chargeUse(hero);
				//trinity has variable charge cost
				if (!(ability instanceof Trinity)) {
					desc += " " + Messages.get(this, "charge_use", Messages.decimalFormat("#.##", chargeUse));
				}
			} else {
				desc += "\n\n" + "_" + Messages.get(this, "no_ability") + "_";
			}
		}

		return desc;
	}

	@Override
	public int value() {
		return 0;
	}

	public class Charger extends Buff {

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				//if we're loading in and the hero has partially spent a turn, delay for 1 turn
				if (target instanceof Hero && Dungeon.hero == null && cooldown() == 0 && target.cooldown() > 0) {
					spend(TICK);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean act() {
			if (charge < 100 && Regeneration.regenOn()) {
				float chargeGain = 100 / 500f; //500 turns to full charge
				chargeGain *= RingOfEnergy.armorChargeMultiplier(target);
				charge += chargeGain;
				updateQuickslot();
				if (charge > 100) {
					charge = 100;
				}
			}
			spend(TICK);
			return true;
		}
	}
}
