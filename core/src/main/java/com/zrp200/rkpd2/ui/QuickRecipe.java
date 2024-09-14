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

import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.items.ArcaneResin;
import com.zrp200.rkpd2.items.DuelistGrass;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.KingsCrown;
import com.zrp200.rkpd2.items.KromerCrown;
import com.zrp200.rkpd2.items.KromerMask;
import com.zrp200.rkpd2.items.LiquidMetal;
import com.zrp200.rkpd2.items.Recipe;
import com.zrp200.rkpd2.items.TengusMask;
import com.zrp200.rkpd2.items.artifacts.KromerCloak;
import com.zrp200.rkpd2.items.artifacts.SoulOfYendor;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.food.Blandfruit;
import com.zrp200.rkpd2.items.food.Food;
import com.zrp200.rkpd2.items.food.MeatPie;
import com.zrp200.rkpd2.items.food.MysteryMeat;
import com.zrp200.rkpd2.items.food.Pasty;
import com.zrp200.rkpd2.items.food.StewedMeat;
import com.zrp200.rkpd2.items.potions.AlchemicalCatalyst;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.items.potions.brews.BlizzardBrew;
import com.zrp200.rkpd2.items.potions.brews.CausticBrew;
import com.zrp200.rkpd2.items.potions.brews.InfernalBrew;
import com.zrp200.rkpd2.items.potions.brews.ShockingBrew;
import com.zrp200.rkpd2.items.potions.elixirs.DoNotDieElixir;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfArcaneArmor;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfDragonsBlood;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfIcyTouch;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfMight;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfToxicEssence;
import com.zrp200.rkpd2.items.potions.elixirs.KromerPotion;
import com.zrp200.rkpd2.items.potions.exotic.ExoticPotion;
import com.zrp200.rkpd2.items.quest.RedCrystal;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.exotic.ExoticScroll;
import com.zrp200.rkpd2.items.spells.Alchemize;
import com.zrp200.rkpd2.items.spells.AquaBlast;
import com.zrp200.rkpd2.items.spells.ArcaneCatalyst;
import com.zrp200.rkpd2.items.spells.BeaconOfReturning;
import com.zrp200.rkpd2.items.spells.CurseInfusion;
import com.zrp200.rkpd2.items.spells.FeatherFall;
import com.zrp200.rkpd2.items.spells.KromerScroll;
import com.zrp200.rkpd2.items.spells.MagicalInfusion;
import com.zrp200.rkpd2.items.spells.PhaseShift;
import com.zrp200.rkpd2.items.spells.Pipisfusion;
import com.zrp200.rkpd2.items.spells.ReclaimTrap;
import com.zrp200.rkpd2.items.spells.Recycle;
import com.zrp200.rkpd2.items.spells.ScammingSpell;
import com.zrp200.rkpd2.items.spells.SummonElemental;
import com.zrp200.rkpd2.items.spells.TelekineticGrab;
import com.zrp200.rkpd2.items.spells.WildEnergy;
import com.zrp200.rkpd2.items.stones.Runestone;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.wands.WandOfUnstable2;
import com.zrp200.rkpd2.items.weapon.KromerBow;
import com.zrp200.rkpd2.items.weapon.Slingshot;
import com.zrp200.rkpd2.items.weapon.melee.Dagger2;
import com.zrp200.rkpd2.items.weapon.melee.KromerStaff;
import com.zrp200.rkpd2.items.weapon.melee.TerminusBlade;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.scenes.AlchemyScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.windows.WndBag;
import com.zrp200.rkpd2.windows.WndInfoItem;

import java.util.ArrayList;
import java.util.Arrays;

public class QuickRecipe extends Component {
	
	private ArrayList<Item> ingredients;
	
	private ArrayList<ItemSlot> inputs;
	private QuickRecipe.arrow arrow;
	private ItemSlot output;
	
	public QuickRecipe(Recipe.SimpleRecipe r){
		this(r, r.getIngredients(), r.sampleOutput(null));
	}

	public QuickRecipe(Recipe.SimpleRecipeBundled r){
		this(r, r.getIngredients(), r.sampleOutput(r.getIngredients()));
	}

	public QuickRecipe(Recipe r, ArrayList<Item> inputs, final Item output) {
		
		ingredients = inputs;
		int cost = r.cost(inputs);
		boolean hasInputs = true;
		this.inputs = new ArrayList<>();
		for (final Item in : inputs) {
			anonymize(in);
			ItemSlot curr;
			curr = new ItemSlot(in) {
				{
					hotArea.blockLevel = PointerArea.NEVER_BLOCK;
				}

				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront(new WndInfoItem(in));
				}
			};
			
			ArrayList<Item> similar = Dungeon.hero.belongings.getAllSimilar(in);
			int quantity = 0;
			for (Item sim : similar) {
				//if we are looking for a specific item, it must be IDed
				if (sim.getClass() != in.getClass() || (sim.isIdentified() && !sim.isEquipped(Dungeon.hero))) quantity += sim.quantity();
			}
			
			if (quantity < in.quantity()) {
				curr.sprite.alpha(0.3f);
				hasInputs = false;
			}
			curr.showExtraInfo(false);
			add(curr);
			this.inputs.add(curr);
		}
		
		if (cost > 0) {
			arrow = new arrow(Icons.get(Icons.ARROW), cost);
			arrow.hardlightText(0x44CCFF);
		} else {
			arrow = new arrow(Icons.get(Icons.ARROW));
		}
		if (hasInputs) {
			arrow.icon.tint(1, 1, 0, 1);
			if (!(ShatteredPixelDungeon.scene() instanceof AlchemyScene)) {
				arrow.enable(false);
			}
		} else {
			arrow.icon.color(0, 0, 0);
			arrow.enable(false);
		}
		add(arrow);
		
		anonymize(output);
		this.output = new ItemSlot(output){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndInfoItem(output));
			}
		};
		if (!hasInputs){
			this.output.sprite.alpha(0.3f);
		}
		this.output.showExtraInfo(false);
		add(this.output);
		
		layout();
	}
	
	@Override
	protected void layout() {
		
		height = 16;
		width = 0;

		int padding = inputs.size() == 1 ? 8 : 0;

		for (ItemSlot item : inputs){
			item.setRect(x + width + padding, y, 16, 16);
			width += 16 + padding;
		}
		
		arrow.setRect(x + width, y, 14, 16);
		width += 14;
		
		output.setRect(x + width, y, 16, 16);
		width += 16;

		width += padding;
	}
	
	//used to ensure that un-IDed items are not spoiled
	private void anonymize(Item item){
		if (item instanceof Potion){
			((Potion) item).anonymize();
		} else if (item instanceof Scroll){
			((Scroll) item).anonymize();
		}
	}
	
	public class arrow extends IconButton {
		
		BitmapText text;
		
		public arrow(){
			super();
		}
		
		public arrow( Image icon ){
			super( icon );
		}
		
		public arrow( Image icon, int count ){
			super( icon );
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;

			text = new BitmapText( Integer.toString(count), PixelScene.pixelFont);
			text.measure();
			add(text);
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			if (text != null){
				text.x = x;
				text.y = y;
				PixelScene.align(text);
			}
		}
		
		@Override
		protected void onPointerUp() {
			icon.brightness(1f);
		}

		@Override
		protected void onClick() {
			super.onClick();
			
			//find the window this is inside of and close it
			Group parent = this.parent;
			while (parent != null){
				if (parent instanceof Window){
					((Window) parent).hide();
					break;
				} else {
					parent = parent.parent;
				}
			}
			
			((AlchemyScene)ShatteredPixelDungeon.scene()).populate(ingredients, Dungeon.hero.belongings);
		}
		
		public void hardlightText(int color ){
			if (text != null) text.hardlight(color);
		}
	}
	
	//gets recipes for a particular alchemy guide page
	//a null entry indicates a break in section
	public static ArrayList<QuickRecipe> getRecipes( int pageIdx ){
		ArrayList<QuickRecipe> result = new ArrayList<>();
		switch (pageIdx){
			case 0: default:
				result.add(new QuickRecipe( new Potion.SeedToPotion(), new ArrayList<>(Arrays.asList(new Plant.Seed.PlaceHolder().quantity(3))), new WndBag.Placeholder(ItemSpriteSheet.POTION_HOLDER){
					@Override
					public String name() {
						return Messages.get(Potion.SeedToPotion.class, "name");
					}

					@Override
					public String info() {
						return "";
					}
				}));
				result.add(new QuickRecipe( new DuelistGrass.SeedDuplicationRecipe(), new ArrayList<>(Arrays.asList(
						new Plant.Seed.PlaceHolder(), new DuelistGrass().quantity(5)
				)), new Plant.Seed.PlaceHolder().quantity(2)));
				return result;
			case 1:
				Recipe r = new Scroll.ScrollToStone();
				for (Class<?> cls : Generator.Category.SCROLL.classes){
					Scroll scroll = (Scroll) Reflection.newInstance(cls);
					if (!scroll.isKnown()) scroll.anonymize();
					ArrayList<Item> in = new ArrayList<Item>(Arrays.asList(scroll));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 2:
				result.add(new QuickRecipe( new StewedMeat.oneMeat() ));
				result.add(new QuickRecipe( new StewedMeat.twoMeat() ));
				result.add(new QuickRecipe( new StewedMeat.threeMeat() ));
				result.add(null);
				result.add(new QuickRecipe( new MeatPie.Recipe(),
						new ArrayList<Item>(Arrays.asList(new Pasty(), new Food(), new MysteryMeat.PlaceHolder())),
						new MeatPie()));
				result.add(null);
				result.add(new QuickRecipe( new Blandfruit.CookFruit(),
						new ArrayList<>(Arrays.asList(new Blandfruit(), new Plant.Seed.PlaceHolder())),
						new Blandfruit(){

							public String name(){
								return Messages.get(Blandfruit.class, "cooked");
							}
							
							@Override
							public String info() {
								return "";
							}
						}));
				return result;
			case 3:
				r = new ExoticPotion.PotionToExotic();
				for (Class<?> cls : Generator.Category.POTION.classes){
					Potion pot = (Potion) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(pot));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 4:
				r = new ExoticScroll.ScrollToExotic();
				for (Class<?> cls : Generator.Category.SCROLL.classes){
					Scroll scroll = (Scroll) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(scroll));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 5:
				r = new Bomb.EnhanceBomb();
				int i = 0;
				for (Class<?> cls : Bomb.EnhanceBomb.validIngredients.keySet()){
					if (i == 2){
						result.add(null);
						i = 0;
					}
					Item item = (Item) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(new Bomb(), item));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
					i++;
				}
				return result;
			case 6:
				result.add(new QuickRecipe( new LiquidMetal.Recipe(),
						new ArrayList<Item>(Arrays.asList(new MissileWeapon.PlaceHolder())),
						new LiquidMetal()));
				result.add(new QuickRecipe( new LiquidMetal.Recipe(),
						new ArrayList<Item>(Arrays.asList(new MissileWeapon.PlaceHolder().quantity(2))),
						new LiquidMetal()));
				result.add(new QuickRecipe( new LiquidMetal.Recipe(),
						new ArrayList<Item>(Arrays.asList(new MissileWeapon.PlaceHolder().quantity(3))),
						new LiquidMetal()));
				result.add(null);
				result.add(null);
				result.add(new QuickRecipe( new ArcaneResin.Recipe(),
						new ArrayList<Item>(Arrays.asList(new Wand.PlaceHolder())),
						new ArcaneResin()));
				result.add(null);
				result.add(new QuickRecipe(new KromerBow.Recipe()));
				result.add(new QuickRecipe(new KromerStaff.Recipe()));
				result.add(new QuickRecipe(new KromerCloak.Recipe()));
				result.add(new QuickRecipe(new Slingshot.Recipe()));
				if (Dungeon.depth > 26){
					result.add(new QuickRecipe(new TerminusBlade.Recipe()));
				}
				result.add(new QuickRecipe(new Dagger2.Recipe()));
				result.add(new QuickRecipe(new WandOfUnstable2.Recipe()));
				return result;
			case 7:
				result.add(new QuickRecipe(new AlchemicalCatalyst.Recipe(), new ArrayList<>(Arrays.asList(new Potion.PlaceHolder(), new Plant.Seed.PlaceHolder())), new AlchemicalCatalyst()));
				result.add(new QuickRecipe(new AlchemicalCatalyst.Recipe(), new ArrayList<>(Arrays.asList(new Potion.PlaceHolder(), new Runestone.PlaceHolder())), new AlchemicalCatalyst()));
				result.add(null);
				result.add(null);
				result.add(new QuickRecipe(new ArcaneCatalyst.Recipe(), new ArrayList<>(Arrays.asList(new Scroll.PlaceHolder(), new Runestone.PlaceHolder())), new ArcaneCatalyst()));
				result.add(new QuickRecipe(new ArcaneCatalyst.Recipe(), new ArrayList<>(Arrays.asList(new Scroll.PlaceHolder(), new Plant.Seed.PlaceHolder())), new ArcaneCatalyst()));
				return result;
			case 8:
				result.add(new QuickRecipe(new CausticBrew.Recipe()));
				result.add(new QuickRecipe(new BlizzardBrew.Recipe()));
				result.add(new QuickRecipe(new InfernalBrew.Recipe()));
				result.add(new QuickRecipe(new ShockingBrew.Recipe()));
				result.add(null);
				result.add(null);
				result.add(new QuickRecipe(new ElixirOfHoneyedHealing.Recipe()));
				result.add(new QuickRecipe(new ElixirOfAquaticRejuvenation.Recipe()));
				result.add(new QuickRecipe(new ElixirOfMight.Recipe()));
				result.add(new QuickRecipe(new ElixirOfDragonsBlood.Recipe()));
				result.add(new QuickRecipe(new ElixirOfIcyTouch.Recipe()));
				result.add(new QuickRecipe(new ElixirOfToxicEssence.Recipe()));
				result.add(new QuickRecipe(new ElixirOfArcaneArmor.Recipe()));
				result.add(new QuickRecipe(new DoNotDieElixir.Recipe()));
				result.add(new QuickRecipe(new KromerPotion.Recipe()));
				return result;
			case 9:
				result.add(new QuickRecipe(new TelekineticGrab.Recipe()));
				result.add(new QuickRecipe(new PhaseShift.Recipe()));
				result.add(new QuickRecipe(new WildEnergy.Recipe()));
				result.add(new QuickRecipe(new BeaconOfReturning.Recipe()));
				result.add(new QuickRecipe(new SummonElemental.Recipe()));
				result.add(null);
				result.add(new QuickRecipe(new AquaBlast.Recipe()));
				result.add(new QuickRecipe(new ReclaimTrap.Recipe()));
				result.add(new QuickRecipe(new ScammingSpell.Recipe()));
				result.add(new QuickRecipe(new FeatherFall.Recipe()));
				result.add(null);
				result.add(new QuickRecipe(new Alchemize.Recipe()));
				result.add(new QuickRecipe(new MagicalInfusion.Recipe()));
				result.add(new QuickRecipe(new CurseInfusion.Recipe()));
				result.add(new QuickRecipe(new Recycle.Recipe()));
				result.add(null);
				result.add(new QuickRecipe(new Pipisfusion.Recipe()));
				result.add(new QuickRecipe(new KromerScroll.Recipe()));
				result.add(null);
				result.add(null);
				if (Dungeon.depth > 26) {
					result.add(new QuickRecipe(new SoulOfYendor.Recipe()));
				}
				if (Dungeon.hero.belongings.getSimilar(new KingsCrown()) != null){
					result.add(new QuickRecipe(new KromerCrown.Recipe()));
				}
				if (Dungeon.hero.belongings.getSimilar(new TengusMask()) != null){
					result.add(new QuickRecipe(new KromerMask.Recipe()));
				}
				if (Dungeon.depth > 26){
					result.add(new QuickRecipe(new RedCrystal.StrengthRecipe()));
				}
				return result;
		}
	}
	
}
