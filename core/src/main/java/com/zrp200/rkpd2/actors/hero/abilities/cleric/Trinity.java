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

package com.zrp200.rkpd2.actors.hero.abilities.cleric;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.spells.BodyForm;
import com.zrp200.rkpd2.actors.hero.spells.ClericSpell;
import com.zrp200.rkpd2.actors.hero.spells.MetaForm;
import com.zrp200.rkpd2.actors.hero.spells.MindForm;
import com.zrp200.rkpd2.actors.hero.spells.SpiritForm;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.effects.Transmuting;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.armor.ClothArmor;
import com.zrp200.rkpd2.items.artifacts.Artifact;
import com.zrp200.rkpd2.items.artifacts.ChaliceOfBlood;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.artifacts.EtherealChains;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.artifacts.TalismanOfForesight;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.artifacts.UnstableSpellbook;
import com.zrp200.rkpd2.items.rings.Ring;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.wands.WandOfFireblast;
import com.zrp200.rkpd2.items.wands.WandOfRegrowth;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.items.weapon.melee.WornShortsword;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.journal.Catalog;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.ItemButton;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.TalentButton;
import com.zrp200.rkpd2.ui.TalentIcon;
import com.zrp200.rkpd2.ui.TalentsPane;
import com.zrp200.rkpd2.ui.Window;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.IconTitle;
import com.zrp200.rkpd2.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Trinity extends ArmorAbility {

	{
		baseChargeUse = 25;
	}

	private Bundlable bodyForm = null;
	private Bundlable mindForm = null;
	private Bundlable spiritForm = null;
	public  Talent    metaForm = null;

	@Override
	public void activate(ClassArmor armor, Hero hero, Integer target) {

		if (bodyForm == null && mindForm == null && spiritForm == null && metaForm == null){
			GLog.w(Messages.get(this, "no_imbue"));
		} else {
			GameScene.show(new WndUseTrinity(armor));
		}

	}

	@Override
	public int targetedPos(Char user, int dst) {
		if (mindForm != null){
			return ((Item)mindForm).targetingPos((Hero)user, dst);
		}
		return super.targetedPos(user, dst);
	}

	public class WndUseTrinity extends WndTitledMessage {

		public WndUseTrinity(ClassArmor armor) {
			super(new HeroIcon(Trinity.this),
					Messages.titleCase(Trinity.this.name()),
					Messages.get(WndUseTrinity.class, "text"));

			int top = height;

			ArrayList<Component> toAdd = new ArrayList<>();

			if (bodyForm != null){
				RedButton btnBody = null;
				if (bodyForm instanceof Weapon.Enchantment){

					btnBody = new RedButton(Messages.get(WndUseTrinity.class, "body",
							Messages.titleCase(((Weapon.Enchantment)bodyForm).name()))
							+ " " + trinityItemUseText(bodyForm.getClass()), 6){
						@Override
						protected void onClick() {
							if (Dungeon.hero.belongings.weapon() != null &&
									((Weapon)Dungeon.hero.belongings.weapon()).enchantment != null &&
									((Weapon)Dungeon.hero.belongings.weapon()).enchantment.getClass().equals(bodyForm.getClass())){
								GLog.w(Messages.get(Trinity.class, "no_duplicate"));
								hide();
							} else {
								Buff.prolong(Dungeon.hero, BodyForm.BodyFormBuff.class, BodyForm.duration()).setEffect(bodyForm);
								Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
								Weapon w = new WornShortsword();
								if (Dungeon.hero.belongings.weapon() != null) {
									w.image = Dungeon.hero.belongings.weapon().image;
								}
								w.enchant((Weapon.Enchantment) bodyForm);
								Enchanting.show(Dungeon.hero, w);
								Dungeon.hero.sprite.operate(Dungeon.hero.pos);
								Dungeon.hero.spendAndNext(1f);
								armor.charge -= trinityChargeUsePerEffect(bodyForm.getClass());
								armor.updateQuickslot();
								Invisibility.dispel();
								hide();
							}
						}
					};
					if (Dungeon.hero.belongings.weapon() != null) {
						btnBody.icon(new ItemSprite(Dungeon.hero.belongings.weapon().image, ((Weapon.Enchantment) bodyForm).glowing()));
					} else {
						btnBody.icon(new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD, ((Weapon.Enchantment) bodyForm).glowing()));
					}
				} else if (bodyForm instanceof Armor.Glyph){
					btnBody = new RedButton(Messages.get(WndUseTrinity.class, "body",
							Messages.titleCase(((Armor.Glyph)bodyForm).name()))
							+ " " + trinityItemUseText(bodyForm.getClass()), 6){
						@Override
						protected void onClick() {
							if (Dungeon.hero.belongings.armor() != null &&
									Dungeon.hero.belongings.armor().glyph != null &&
									(Dungeon.hero.belongings.armor()).glyph.getClass().equals(bodyForm.getClass())){
								GLog.w(Messages.get(Trinity.class, "no_duplicate"));
								hide();
							} else {
								Buff.prolong(Dungeon.hero, BodyForm.BodyFormBuff.class, BodyForm.duration()).setEffect(bodyForm);
								Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
								Armor a = new ClothArmor();
								if (Dungeon.hero.belongings.armor() != null) {
									a.image = Dungeon.hero.belongings.armor().image;
								}
								a.inscribe((Armor.Glyph) bodyForm);
								Enchanting.show(Dungeon.hero, a);
								Dungeon.hero.sprite.operate(Dungeon.hero.pos);
								Dungeon.hero.spendAndNext(1f);
								armor.charge -= trinityChargeUsePerEffect(bodyForm.getClass());
								armor.updateQuickslot();
								Invisibility.dispel();
								hide();
							}
						}
					};
					if (Dungeon.hero.belongings.armor() != null) {
						btnBody.icon(new ItemSprite(Dungeon.hero.belongings.armor().image, ((Armor.Glyph) bodyForm).glowing()));
					} else {
						btnBody.icon(new ItemSprite(ItemSpriteSheet.ARMOR_CLOTH, ((Armor.Glyph) bodyForm).glowing()));
					}
				}
				btnBody.multiline = true;
				btnBody.setSize(width, 100); //for text layout
				btnBody.setRect(0, top + 2, width, btnBody.reqHeight());
				toAdd.add(btnBody);
				top = (int)btnBody.bottom();

				btnBody.enable(Dungeon.hero.buff(MagicImmune.class) == null && armor.charge >= trinityChargeUsePerEffect(bodyForm.getClass()));
			}

			if (mindForm != null){
				RedButton btnMind = new RedButton(Messages.get(WndUseTrinity.class, "mind",
						Messages.titleCase(((Item)mindForm).name()))
						+ " " + trinityItemUseText(mindForm.getClass()), 6){
					@Override
					protected void onClick() {
						hide();
						MindForm.targetSelector mindEffect = new MindForm.targetSelector();
						mindEffect.setEffect(mindForm);
						GameScene.selectCell(mindEffect);
						Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
						Enchanting.show(Dungeon.hero, (Item)mindForm);
						Dungeon.hero.sprite.operate(Dungeon.hero.pos);

						if (((Item) mindForm).usesTargeting && Dungeon.quickslot.contains(armor)){
							QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(armor));
						}
					}
				};
				btnMind.icon(new ItemSprite((Item)mindForm));
				btnMind.multiline = true;
				btnMind.setSize(width, 100); //for text layout
				btnMind.setRect(0, top + 2, width, btnMind.reqHeight());
				toAdd.add(btnMind);
				top = (int)btnMind.bottom();

				btnMind.enable(armor.charge >= trinityChargeUsePerEffect(mindForm.getClass()));
				if (mindForm instanceof Wand && Dungeon.hero.buff(MagicImmune.class) != null){
					btnMind.enable(false);
				}
			}

			if (spiritForm != null){
				RedButton btnSpirit = new RedButton(Messages.get(WndUseTrinity.class, "spirit",
						Messages.titleCase(((Item)spiritForm).name()))
						+ " " + trinityItemUseText(spiritForm.getClass()), 6){
					@Override
					protected void onClick() {
						if ((Dungeon.hero.belongings.ring() != null && Dungeon.hero.belongings.ring().getClass().equals(spiritForm.getClass()))
								|| (Dungeon.hero.belongings.misc() != null && Dungeon.hero.belongings.misc().getClass().equals(spiritForm.getClass()))
								|| (Dungeon.hero.belongings.artifact() != null && Dungeon.hero.belongings.artifact().getClass().equals(spiritForm.getClass()))){
							GLog.w(Messages.get(Trinity.class, "no_duplicate"));
							hide();
							return;
						}
						Invisibility.dispel();
						//Rings and the Chalice specifically get their passive effects for 20 turns
						if (spiritForm instanceof Ring || spiritForm instanceof ChaliceOfBlood) {
							Buff.prolong(Dungeon.hero, SpiritForm.SpiritFormBuff.class, SpiritForm.SpiritFormBuff.DURATION).setEffect(spiritForm);
							Dungeon.hero.spendAndNext(1f);
						} else {
							SpiritForm.applyActiveArtifactEffect(armor, (Artifact) spiritForm);
							//turn spending is handled within the application of the artifact effect
						}
						Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
						Enchanting.show(Dungeon.hero, (Item) spiritForm);
						Dungeon.hero.sprite.operate(Dungeon.hero.pos);
						armor.charge -= trinityChargeUsePerEffect(spiritForm.getClass());
						armor.updateQuickslot();
						hide();
					}
				};
				if (spiritForm instanceof Artifact){
					((Artifact) spiritForm).resetForTrinity(SpiritForm.artifactLevel());
				}

				btnSpirit.icon(new ItemSprite((Item)spiritForm));
				btnSpirit.multiline = true;
				btnSpirit.setSize(width, 100); //for text layout
				btnSpirit.setRect(0, top + 2, width, btnSpirit.reqHeight());
				toAdd.add(btnSpirit);
				top = (int)btnSpirit.bottom();

				btnSpirit.enable(Dungeon.hero.buff(MagicImmune.class) == null && armor.charge >= trinityChargeUsePerEffect(spiritForm.getClass()));
			}

			if (metaForm != null){
				RedButton btnMeta = new RedButton(Messages.get(WndUseTrinity.class, "meta",
						Messages.titleCase((metaForm).title()))
						+ " " + trinityItemUseText(metaForm.getClass()), 6){
					@Override
					protected void onClick() {
						Invisibility.dispel();
						Buff.prolong(Dungeon.hero, MetaForm.MetaFormBuff.class, MetaForm.duration()).setEffect(metaForm);
						Dungeon.hero.spendAndNext(1f);
						Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
						Transmuting.show(Dungeon.hero, metaForm, metaForm);
						Dungeon.hero.sprite.operate(Dungeon.hero.pos);
						armor.charge -= trinityChargeUsePerEffect(metaForm.getClass());
						armor.updateQuickslot();
						hide();
					}
				};

				btnMeta.icon(new TalentIcon(metaForm));
				btnMeta.multiline = true;
				btnMeta.setSize(width, 100); //for text layout
				btnMeta.setRect(0, top + 2, width, btnMeta.reqHeight());
				toAdd.add(btnMeta);
				top = (int)btnMeta.bottom();

				btnMeta.enable(Dungeon.hero.buff(MagicImmune.class) == null && armor.charge >= trinityChargeUsePerEffect(metaForm.getClass()));
			}

			addToBottom(toAdd.toArray(new Component[0]));
			resize(width, top);

		}

	}

	private static final String BODY = "body_form";
	private static final String MIND = "mind_form";
	private static final String SPIRIT = "spirit_form";
	private static final String META = "meta_form";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (bodyForm != null)   bundle.put(BODY, bodyForm);
		if (mindForm != null)   bundle.put(MIND, mindForm);
		if (spiritForm != null) bundle.put(SPIRIT, spiritForm);
		if (metaForm != null) bundle.put(META, metaForm.name());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(BODY))  bodyForm = bundle.get(BODY);
		if (bundle.contains(MIND))  mindForm = bundle.get(MIND);
		if (bundle.contains(SPIRIT))spiritForm = bundle.get(SPIRIT);
		if (bundle.contains(META))  metaForm = bundle.getEnum(META, Talent.class);
	}

	@Override
	public int icon() {
		return HeroIcon.TRINITY;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BODY_FORM, Talent.MIND_FORM, Talent.SPIRIT_FORM, Talent.META_FORM, Talent.HEROIC_ENERGY};
	}

	public static class WndItemtypeSelect extends WndTitledMessage {

		//probably want a callback here?
		public WndItemtypeSelect(HolyTome tome, ClericSpell spell) {
			super(new HeroIcon(spell), Messages.titleCase(spell.name()), Messages.get(WndItemtypeSelect.class, "text"));

			//start by filtering and sorting
			ArrayList<Class<?>> discoveredClasses = new ArrayList<>();
			if (spell == BodyForm.INSTANCE) {
				for (Class<?> cls : Catalog.ENCHANTMENTS.items()) {
					if (Statistics.itemTypesDiscovered.contains(cls)) {
						discoveredClasses.add(cls);
					}
				}
				for (Class<?> cls : Catalog.GLYPHS.items()) {
					if (Statistics.itemTypesDiscovered.contains(cls)) {
						discoveredClasses.add(cls);
					}
				}
			} else if (spell == MindForm.INSTANCE){
				for (Class<?> cls : Catalog.WANDS.items()) {
					if (Statistics.itemTypesDiscovered.contains(cls)) {
						discoveredClasses.add(cls);
					}
				}
				for (Class<?> cls : Catalog.THROWN_WEAPONS.items()) {
					if (Statistics.itemTypesDiscovered.contains(cls)) {
						discoveredClasses.add(cls);
					}
				}
				for (Class<?> cls : Catalog.TIPPED_DARTS.items()) {
					if (Statistics.itemTypesDiscovered.contains(cls)) {
						discoveredClasses.add(cls);
					}
				}
			} else if (spell == SpiritForm.INSTANCE){
				for (Class<?> cls : Catalog.RINGS.items()) {
					if (Statistics.itemTypesDiscovered.contains(cls)) {
						discoveredClasses.add(cls);
					}
				}
				for (Class<?> cls : Catalog.ARTIFACTS.items()) {
					if (Statistics.itemTypesDiscovered.contains(cls)) {
						discoveredClasses.add(cls);
					}
					//no tome specifically
					discoveredClasses.remove(HolyTome.class);
				}
			}

			ArrayList<Item> options = new ArrayList<>();
			for (Class<?> cls : discoveredClasses){
				if (Weapon.Enchantment.class.isAssignableFrom(cls)){
					MeleeWeapon w = new WornShortsword();
					if (Dungeon.hero.belongings.weapon() != null){
						w.image = Dungeon.hero.belongings.weapon().image;
					}
					w.enchant((Weapon.Enchantment) Reflection.newInstance(cls));
					options.add(w);
				} else if (Armor.Glyph.class.isAssignableFrom(cls)) {
					Armor a = new ClothArmor();
					if (Dungeon.hero.belongings.armor() != null){
						a.image = Dungeon.hero.belongings.armor().image;
					}
					a.inscribe((Armor.Glyph) Reflection.newInstance(cls));
					options.add(a);
				} else {
					options.add((Item) Reflection.newInstance(cls));
				}
			}

			int top = height + 20;
			int left = 0;

			for (Item item : options){
				ItemButton btn = new ItemButton(){
					@Override
					protected void onClick() {
						GameScene.show(new WndItemConfirm(WndItemtypeSelect.this, item, tome, spell));
					}
				};
				btn.item(item);
				btn.slot().textVisible(false);
				btn.setRect(left, top, 19, 19);
				add(btn);

				left += 20;
				if (left >= width - 19){
					top += 20;
					left = 0;
				}
			}

			if (left > 0){
				top += 20;
				left = 0;
			}

			resize(width, top);

		}

	}

	public static class WndItemConfirm extends WndTitledMessage {

		public WndItemConfirm(Window parentWnd, Item item, HolyTome tome, ClericSpell spell){
			super(new ItemSprite(item),  Messages.titleCase(getName(item)), getText(item));

			String text;
			if (spell == BodyForm.INSTANCE){
				text = Messages.get(this, "body");
			} else if (spell == MindForm.INSTANCE){
				text = Messages.get(this, "mind");
			} else {
				text = Messages.get(this, "spirit");
			}

			RedButton btnConfirm = new RedButton(text){
				@Override
				protected void onClick() {
					parentWnd.hide();
					WndItemConfirm.this.hide();

					if (item instanceof MeleeWeapon) {
						((Trinity)Dungeon.hero.armorAbility).bodyForm = ((MeleeWeapon) item).enchantment;
					} else if (item instanceof Armor) {
						((Trinity)Dungeon.hero.armorAbility).bodyForm = ((Armor) item).glyph;
					} else if (item instanceof Wand || item instanceof MissileWeapon){
						((Trinity)Dungeon.hero.armorAbility).mindForm = item;
					} else {
						((Trinity)Dungeon.hero.armorAbility).spiritForm = item;
					}
					spell.onSpellCast(tome, Dungeon.hero);

					Dungeon.hero.sprite.operate(Dungeon.hero.pos);
					Enchanting.show(Dungeon.hero, item);
					Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				}
			};
			btnConfirm.setRect(0, height+2, width, 16);
			addToBottom(btnConfirm);

			resize(width, (int)btnConfirm.bottom());

		}

		private static String getName(Item item){
			if (item instanceof MeleeWeapon){
				return ((MeleeWeapon) item).enchantment.name();
			} else if (item instanceof Armor){
				return (((Armor) item).glyph.name());
			}
			return item.name();
		}

		private static String getText(Item item){
			if (item instanceof MeleeWeapon){
				return ((MeleeWeapon) item).enchantment.desc() + "\n\n" + trinityItemUseText(((MeleeWeapon) item).enchantment.getClass());
			} else if (item instanceof Armor){
				return ((Armor) item).glyph.desc() + "\n\n" + trinityItemUseText(((Armor) item).glyph.getClass());
			} else {
				return item.desc() + "\n\n" + trinityItemUseText(item.getClass());
			}
		}

	}

	public static class WndTalentSelect extends Window {

		public static WndTalentSelect INSTANCE;
		ArrayList<Talent> replaceOptions;

		public WndTalentSelect(){
			super();
			INSTANCE = this;

			ArrayList<Talent> possibleTalents = new ArrayList<>();

			ArrayList<Talent> allTalents = new ArrayList<>();
			for (HeroClass heroClass: HeroClass.values()){
				ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
				Talent.initClassTalents(heroClass, talents);
				for (LinkedHashMap<Talent, Integer> tier : talents){
                    allTalents.addAll(tier.keySet());
				}
			}

			for (Talent talent: allTalents){
				for(LinkedHashMap<Talent,Integer> tier : Dungeon.hero.talents)
					if(!tier.containsKey(talent))
						possibleTalents.add(talent);
			}

			ArrayList<Talent> options = new ArrayList<>();
			while (options.size() < 10){
				Talent talent = Random.element(possibleTalents);
				if (!options.contains(talent))
					options.add(talent);
			}

			replaceOptions = options;
			setup(options);
		}

		private void setup(ArrayList<Talent> replaceOptions){
			float top = 0;

			IconTitle title = new IconTitle( new HeroIcon(MetaForm.INSTANCE), Messages.titleCase(MetaForm.INSTANCE.name()) );
			title.color( TITLE_COLOR );
			title.setRect(0, 0, 120, 0);
			add(title);

			top = title.bottom() + 2;

			RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(WndTalentSelect.class, "desc"), 6);
			text.maxWidth(120);
			text.setPos(0, top);
			add(text);

			top = text.bottom() + 2;

			LinkedHashMap<Talent, Integer> talentMap = new LinkedHashMap<>();
			for (Talent talent: replaceOptions){
				talentMap.put(talent, talent.maxPoints());
			}

			TalentsPane.TalentTierPane optionsPane = new TalentsPane.TalentTierPane(talentMap, 999, TalentButton.Mode.METAFORM_SELECT);
			add(optionsPane);
			optionsPane.title.text(" ");
			optionsPane.setPos(0, top);
			optionsPane.setSize(120, optionsPane.height());
			resize((int)optionsPane.width(), (int)optionsPane.bottom());

			resize(120, (int)optionsPane.bottom());
		}
	}

	public static String trinityItemUseText(Class<?> cls ){
		float chargeUse = trinityChargeUsePerEffect(cls);
		if (Weapon.Enchantment.class.isAssignableFrom(cls) || Armor.Glyph.class.isAssignableFrom(cls)) {
			for (Class ench : Weapon.Enchantment.rare) {
				if (ench.equals(cls)) {
					return Messages.get(Trinity.class, "rare_ench_glyph_use", BodyForm.duration(), Messages.decimalFormat("#.##", chargeUse));
				}
			}
			for (Class glyph : Armor.Glyph.rare){
				if (glyph.equals(cls)){
					return Messages.get(Trinity.class, "rare_ench_glyph_use", BodyForm.duration(), Messages.decimalFormat("#.##", chargeUse));
				}
			}
			return Messages.get(Trinity.class, "ench_glyph_use", BodyForm.duration(), Messages.decimalFormat("#.##", chargeUse));
		}
		if (MissileWeapon.class.isAssignableFrom(cls)){
			return Messages.get(Trinity.class, "thrown_use", MindForm.itemLevel(), Messages.decimalFormat("#.##", chargeUse));
		}
		if (Wand.class.isAssignableFrom(cls)){
			if (cls.equals(WandOfFireblast.class) || cls.equals(WandOfRegrowth.class)){
				return Messages.get(Trinity.class, "wand_multi_use", MindForm.itemLevel(), Messages.decimalFormat("#.##", chargeUse));
			}
			return Messages.get(Trinity.class, "wand_use", MindForm.itemLevel(), Messages.decimalFormat("#.##", chargeUse));
		}
		if (Ring.class.isAssignableFrom(cls)){
			return Messages.get(Trinity.class, "ring_use", SpiritForm.ringLevel(), Messages.decimalFormat("#.##", chargeUse));
		}
		if (Artifact.class.isAssignableFrom(cls)){
			return Messages.get(Trinity.class, cls.getSimpleName() + "_use", SpiritForm.artifactLevel(), Messages.decimalFormat("#.##", chargeUse));
		}
		if (Talent.class.isAssignableFrom(cls)){
			return Messages.get(Trinity.class, "talent_use", MetaForm.duration(), Messages.decimalFormat("#.##", chargeUse));
		}
		return "error!";

	}

	public static float trinityChargeUsePerEffect(Class<?> cls){
		float chargeUse = Dungeon.hero.armorAbility.chargeUse(Dungeon.hero);
		if (Weapon.Enchantment.class.isAssignableFrom(cls) || Armor.Glyph.class.isAssignableFrom(cls)) {
			for (Class ench : Weapon.Enchantment.rare) {
				if (ench.equals(cls)) {
					return 2*chargeUse; //50 charge
				}
			}
			for (Class glyph : Armor.Glyph.rare){
				if (glyph.equals(cls)){
					return 2*chargeUse; //50 charge
				}
			}
		}
		if (cls.equals(WandOfFireblast.class) || cls.equals(WandOfRegrowth.class)){
			return 2*chargeUse;
		}
		if (Artifact.class.isAssignableFrom(cls)){
			if (cls.equals(DriedRose.class) || cls.equals(UnstableSpellbook.class)){
				return 2*chargeUse; //50 charge
			}
			if (cls.equals(EtherealChains.class) || cls.equals(TalismanOfForesight.class) || cls.equals(TimekeepersHourglass.class)){
				return 1.4f*chargeUse; //35 charge
			}
		}
		if (Talent.class.isAssignableFrom(cls)){
			return 2.68f*chargeUse; //66.6 charge
		}
		//all other effects are standard charge use, 25 at base
		return chargeUse;
	}

}
