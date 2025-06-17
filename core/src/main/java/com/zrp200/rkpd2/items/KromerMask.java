/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.scrolls.ScrollOfUpgrade;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.Window;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndChooseClass;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class KromerMask extends Item {

	private static final String AC_CONNECT	= "CONNECT";
	
	{
		stackable = false;
		image = ItemSpriteSheet.KROMER_MASK;

		defaultAction = AC_CONNECT;

		unique = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_CONNECT );
		return actions;
	}

	public static ArrayList<HeroClass> classes = new ArrayList<>();

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_CONNECT )) {

				curUser = hero;
				ExploitHandler handler = Buff.affect(curUser, ExploitHandler.class);
				handler.crown = this;
				if (classes.isEmpty()) {

					ArrayList<HeroClass> heroClasses = new ArrayList<>(Arrays.asList(HeroClass.values()));
					//remove unusable classes
					if (Random.Int(5) > 0 && !Badges.isUnlocked(Badges.Badge.DEFEATED_RK_FOREVER)) heroClasses.remove(HeroClass.RAT_KING);
					heroClasses.remove(hero.heroClass);
					while (classes.size() < 2) {
						HeroClass chosenSub;
						do {
							chosenSub = Random.element(heroClasses);
							if (!classes.contains(chosenSub)) {
								classes.add(chosenSub);
								break;
							}
						} while (true);
					}
				}
				handler.classes = classes;

				GameScene.show(new WndChooseClass(this, hero, classes));

		}
	}

	private static final ItemSprite.Glowing CHAOTIC = new ItemSprite.Glowing( 0.15f  );

	@Override
	public ItemSprite.Glowing glowing() {
		return CHAOTIC;
	}

	public static void choose( Hero hero, HeroClass way) { HeroClass.giveSecondClass(way); }
	
	public void choose( HeroClass way ) {
		
		detach( curUser.belongings.backpack );
		for (Item item: curUser.belongings.backpack){
			if (item instanceof KromerCrown){
				item.detach(curUser.belongings.backpack);
			}
		}
		
		curUser.spend( Actor.TICK );
		curUser.busy();
		
		choose(curUser, way);
		
		curUser.sprite.operate( curUser.pos );
		Sample.INSTANCE.play( Assets.Sounds.MASTERY, 2f, 0.66f );
		
		Emitter e = curUser.sprite.centerEmitter();
		e.pos(e.x-2, e.y-6, 4, 4);
		e.start(Speck.factory(Speck.MASK), 0.005f, 60);
		GameScene.flash(Window.SHPX_COLOR, true);
		Camera.main.shake(2f, 1f);
		GLog.p( Messages.get(this, "used") );
		Buff.detach(curUser, ExploitHandler.class);
	}

	public static class ExploitHandler extends Buff {
		{ actPriority = VFX_PRIO; }

		public KromerMask crown;
		public ArrayList<HeroClass> classes;

		@Override
		public boolean act() {
			curUser = Dungeon.hero;
			curItem = crown;
			KromerMask.classes = classes;

			Game.runOnRenderThread(() -> crown.execute(curUser, AC_CONNECT));
			detach();
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( "crown", crown );
			bundle.put("sub1", classes.get(0));
			bundle.put("sub2", classes.get(1));
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			crown = (KromerMask) bundle.get("crown");
			classes = new ArrayList<>();
			classes.add(bundle.getEnum("sub1", HeroClass.class));
			classes.add(bundle.getEnum("sub2", HeroClass.class));
		}
	}

	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

		{
			inputs =  new Class[]{Kromer.class, ScrollOfUpgrade.class, TengusMask.class};
			inQuantity = new int[]{1, 1, 1};

			cost = 48;

			output = KromerMask.class;
			outQuantity = 1;
		}

	}
}
