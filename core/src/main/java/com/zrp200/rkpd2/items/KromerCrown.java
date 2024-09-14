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

import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.Window;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndChooseSubclass;

import java.util.ArrayList;
import java.util.Arrays;

import static com.zrp200.rkpd2.actors.hero.HeroSubClass.NONE;

public class KromerCrown extends TengusMask {

	private static final String AC_CONNECT	= "CONNECT";
	
	{
		stackable = false;
		image = ItemSpriteSheet.KROMER_CROWN;

		defaultAction = AC_CONNECT;

		unique = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove(AC_WEAR);
		actions.add( AC_CONNECT );
		return actions;
	}

	public static ArrayList<HeroSubClass> subClasses = new ArrayList<>();

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_CONNECT )) {

				curUser = hero;
				ExploitHandler handler = Buff.affect(curUser, ExploitHandler.class);
				handler.crown = this;
				if (subClasses.isEmpty()) {

					ArrayList<HeroSubClass> heroSubClasses = new ArrayList<>(Arrays.asList(HeroSubClass.values()));
					//remove unusable classes
					heroSubClasses.remove(NONE);
					if (!curUser.isClassedLoosely(HeroClass.MAGE))
						heroSubClasses.remove(HeroSubClass.BATTLEMAGE);
					if (!curUser.isClassedLoosely(HeroClass.HUNTRESS))
						heroSubClasses.remove(HeroSubClass.SNIPER);
					if (!curUser.isClassedLoosely(HeroClass.ROGUE))
						heroSubClasses.remove(HeroSubClass.ASSASSIN);
					heroSubClasses.remove(hero.subClass);
					//remove rat king class
					heroSubClasses.remove(HeroSubClass.KING);
					if (!Badges.isUnlocked(Badges.Badge.DEFEATED_RK))
						heroSubClasses.remove(HeroSubClass.RK_CHAMPION);
					while (subClasses.size() < 3) {
						HeroSubClass chosenSub;
						do {
							chosenSub = Random.element(heroSubClasses);
							if (!subClasses.contains(chosenSub)) {
								subClasses.add(chosenSub);
								break;
							}
						} while (true);
					}
				}
				handler.subClasses = subClasses;

				GameScene.show(new WndChooseSubclass(this, hero, subClasses));
			}
	}

	private static final ItemSprite.Glowing CHAOTIC = new ItemSprite.Glowing( 0.15f  );

	@Override
	public ItemSprite.Glowing glowing() {
		return CHAOTIC;
	}

	public static void choose( Hero hero, HeroSubClass way) { HeroSubClass.set(hero, way, new KromerCrown()); }
	
	public void choose( HeroSubClass way ) {
		
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
		Sample.INSTANCE.play( Assets.Sounds.MASTERY, 1f, 0.5f );
		
		Emitter e = curUser.sprite.centerEmitter();
		e.pos(e.x-2, e.y-6, 4, 4);
		e.start(Speck.factory(Speck.MASK), 0.03f, 60);
		GameScene.flash(Window.SHPX_COLOR, true);
		Camera.main.shake(1f, 1f);
		GLog.p( Messages.get(this, way != HeroSubClass.KING ? "used" : "used_rk") );
		Buff.detach(Dungeon.hero, ExploitHandler.class);
	}

	public static class ExploitHandler extends Buff {
		{ actPriority = VFX_PRIO; }

		public KromerCrown crown;
		public ArrayList<HeroSubClass> subClasses;

		@Override
		public boolean act() {
			curUser = Dungeon.hero;
			curItem = crown;
			KromerCrown.subClasses = subClasses;

			Game.runOnRenderThread(() -> crown.execute(curUser, AC_CONNECT));
			detach();
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( "crown", crown );
			bundle.put("sub1", subClasses.get(0));
			bundle.put("sub2", subClasses.get(1));
			bundle.put("sub3", subClasses.get(2));
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			crown = (KromerCrown) bundle.get("crown");
			subClasses = new ArrayList<>();
			subClasses.add(bundle.getEnum("sub1", HeroSubClass.class));
			subClasses.add(bundle.getEnum("sub2", HeroSubClass.class));
			subClasses.add(bundle.getEnum("sub3", HeroSubClass.class));
		}
	}

	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

		{
			inputs =  new Class[]{Kromer.class, ScrollOfMetamorphosis.class, KingsCrown.class};
			inQuantity = new int[]{1, 1, 1};

			cost = 97;

			output = KromerCrown.class;
			outQuantity = 1;
		}

	}
}
