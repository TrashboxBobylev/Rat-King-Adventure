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

package com.zrp200.rkpd2.items.potions.exotic;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.journal.Catalog;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class PotionOfDragonsBreath extends ExoticPotion {
	
	{
		icon = ItemSpriteSheet.Icons.POTION_DRGBREATH;
	}

	protected static boolean identifiedByUse = false;

	@Override
	//need to override drink so that time isn't spent right away
	protected void drink(final Hero hero) {

		if (!isKnown()) {
			identify();
			curItem = detach( hero.belongings.backpack );
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}

		GameScene.selectCell(targeter);
	}
	
	private CellSelector.Listener targeter = new CellSelector.Listener() {

		private boolean showingWindow = false;
		private boolean potionAlreadyUsed = false;

		@Override
		public void onSelect(final Integer cell) {

			if (showingWindow){
				return;
			}
			if (potionAlreadyUsed){
				potionAlreadyUsed = false;
				return;
			}

			if (cell == null && identifiedByUse){
				showingWindow = true;
				ShatteredPixelDungeon.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndOptions(new ItemSprite(PotionOfDragonsBreath.this),
								Messages.titleCase(name()),
								Messages.get(ExoticPotion.class, "warning"),
								Messages.get(ExoticPotion.class, "yes"),
								Messages.get(ExoticPotion.class, "no") ) {
							@Override
							protected void onSelect( int index ) {
								showingWindow = false;
								switch (index) {
									case 0:
										curUser.spendAndNext(1f);
										identifiedByUse = false;
										break;
									case 1:
										GameScene.selectCell( targeter );
										break;
								}
							}
							public void onBackPressed() {}
						} );
					}
				});
			} else if (cell != null) {
				if (!identifiedByUse) {
					curItem.detach(curUser.belongings.backpack);
				}
				potionAlreadyUsed = true;
				identifiedByUse = false;
				curUser.busy();
				Sample.INSTANCE.play( Assets.Sounds.DRINK );
				curUser.sprite.operate(curUser.pos, new Callback() {
					@Override
					public void call() {

						curUser.sprite.idle();
						curUser.sprite.zap(cell);
						Sample.INSTANCE.play( Assets.Sounds.BURNING );

						final Ballistica bolt = new Ballistica(curUser.pos, cell, Ballistica.WONT_STOP);

						int maxDist = 6;
						int dist = Math.min(bolt.dist, maxDist);

						final ConeAOE cone = new ConeAOE(bolt, 6, 60, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET | Ballistica.IGNORE_SOFT_SOLID);

						//cast to cells at the tip, rather than all cells, better performance.
						for (Ballistica ray : cone.outerRays){
							((MagicMissile)curUser.sprite.parent.recycle( MagicMissile.class )).reset(
									MagicMissile.FIRE_CONE,
									curUser.sprite,
									ray.path.get(ray.dist),
									null
							);
						}
						
						MagicMissile.boltFromChar(curUser.sprite.parent,
								MagicMissile.FIRE_CONE,
								curUser.sprite,
								bolt.path.get(dist / 2),
								new Callback() {
									@Override
									public void call() {
										ArrayList<Integer> adjacentCells = new ArrayList<>();
										for (int cell : cone.cells){
											//ignore caster cell
											if (cell == bolt.sourcePos){
												continue;
											}

											//knock doors open
											if (Dungeon.level.map[cell] == Terrain.DOOR){
												Level.set(cell, Terrain.OPEN_DOOR);
												GameScene.updateMap(cell);
											}

											//only ignite cells directly near caster if they are flammable
											if (Dungeon.level.adjacent(bolt.sourcePos, cell) && !Dungeon.level.flamable[cell]){
												adjacentCells.add(cell);
											} else {
												GameScene.add( Blob.seed( cell, 5, Fire.class ) );
											}
											
											Char ch = Actor.findChar( cell );
											if (ch != null) {
												
												Buff.affect( ch, Burning.class ).reignite( ch );
												Buff.affect(ch, Cripple.class, 5f);
											}
										}

										//ignite cells that share a side with an adjacent cell, are flammable, and are further from the source pos
										//This prevents short-range casts not igniting barricades or bookshelves
										for (int cell : adjacentCells){
											for (int i : PathFinder.NEIGHBOURS4){
												if (Dungeon.level.trueDistance(cell+i, bolt.sourcePos) > Dungeon.level.trueDistance(cell, bolt.sourcePos)
														&& Dungeon.level.flamable[cell+i]
														&& Fire.volumeAt(cell+i, Fire.class) == 0){
													GameScene.add( Blob.seed( cell+i, 5, Fire.class ) );
												}
											}
										}

										curUser.spendAndNext(1f);

										if (!anonymous) {
											Catalog.countUse(PotionOfDragonsBreath.class);
											if (Random.Float() < talentChance) {
												Talent.onPotionUsed(curUser, curUser.pos, talentFactor);
											}
										}
									}
								});
						
					}
				});
			}
		}
		
		@Override
		public String prompt() {
			return Messages.get(PotionOfDragonsBreath.class, "prompt");
		}
	};
}
