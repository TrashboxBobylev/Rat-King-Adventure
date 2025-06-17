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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.items.bags.MagicalHolster;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndBag;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class ArcaneResin extends Item {

	{
		image = ItemSpriteSheet.ARCANE_RESIN;

		stackable = true;

		defaultAction = AC_APPLY;

		bones = true;
	}

	private static final String AC_APPLY = "APPLY";

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_APPLY );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_APPLY)) {

			curUser = hero;
			GameScene.selectItem( itemSelector );

		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 30*quantity();
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(ArcaneResin.class, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return MagicalHolster.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Wand && item.isIdentified();
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null && item instanceof Wand) {
				Wand w = (Wand)item;

				int resinToUse = w.level()+1;

				if (quantity() < resinToUse){
					GLog.w(Messages.get(ArcaneResin.class, "not_enough"));

				} else {

					if (resinToUse < quantity()){
						quantity(quantity()-resinToUse);
					} else {
						detachAll(Dungeon.hero.belongings.backpack);
					}

					w.resinBonus++;
					w.curCharges++;
					w.updateLevel();
					Item.updateQuickslot();

					curUser.sprite.operate(curUser.pos);
					Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
					curUser.sprite.emitter().start( Speck.factory( Speck.UP ), 0.2f, 3 );

					curUser.spendAndNext(Actor.TICK);
					GLog.p(Messages.get(ArcaneResin.class, "apply"));
				}
			}
		}
	};

	public static class Recipe extends com.zrp200.rkpd2.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return ingredients.size() == 1
					&& ingredients.get(0) instanceof Wand
					&& ingredients.get(0).isIdentified()
					&& !ingredients.get(0).cursed;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 5;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			Item result = sampleOutput(ingredients);

			ingredients.get(0).quantity(0);

			return result;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			Wand w = (Wand)ingredients.get(0);
			int level = w.level() - w.resinBonus;

			Item output = new ArcaneResin().quantity(2*(level+1));

			// metamorph effect
			if (!Dungeon.hero.heroClass.is(HeroClass.MAGE))
			{
				int boost = (int)Dungeon.hero.byTalent(
						// +1/+2/+4 enjoy your +3 wands!
						Talent.WAND_PRESERVATION, 2f,
						Talent.POWER_WITHIN, 1f); // +1/+2
				if (Dungeon.hero.shiftedPoints(Talent.WAND_PRESERVATION) == 1) boost++;
				if (boost > 0) output.quantity(output.quantity() + boost);
			}

			return output;
		}
	}

}
