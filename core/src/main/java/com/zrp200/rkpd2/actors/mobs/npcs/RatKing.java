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

package com.zrp200.rkpd2.actors.mobs.npcs;

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.items.Amulet;
import com.zrp200.rkpd2.items.KingsCrown;
import com.zrp200.rkpd2.items.artifacts.SoulOfYendor;
import com.zrp200.rkpd2.items.remains.CrownShard;
import com.zrp200.rkpd2.levels.features.LevelTransition;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.InterlevelScene;
import com.zrp200.rkpd2.sprites.RatKingSprite;
import com.zrp200.rkpd2.utils.Holiday;
import com.zrp200.rkpd2.windows.WndInfoArmorAbility;
import com.zrp200.rkpd2.windows.WndOptions;
import com.zrp200.rkpd2.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

public class RatKing extends NPC {

	{
		spriteClass = RatKingSprite.class;
		
		state = SLEEPING;
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}
	
	@Override
	public float speed() {
		return 2f;
	}
	
	@Override
	protected Char chooseEnemy() {
		return null;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//do nothing
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}
	
	@Override
	public boolean reset() {
		return true;
	}

	//***This functionality is for when rat king may be summoned by a distortion trap

	@Override
	protected void onAdd() {
		super.onAdd();
		if (firstAdded && Dungeon.depth != 5){
			yell(Messages.get(this, "confused"));
		}
	}

	// this isn't stored currently
	private boolean discussedShard = false;

	@Override
	protected boolean act() {
		if (Dungeon.depth < 5){
			if (pos == Dungeon.level.exit()){
				destroy();
				sprite.killAndErase();
			} else {
				target = Dungeon.level.exit();
			}
		} else if (Dungeon.depth > 5){
			if (pos == Dungeon.level.entrance()){
				destroy();
				sprite.killAndErase();
			} else {
				target = Dungeon.level.entrance();
			}
		}
		return super.act();
	}

	//***

	@Override
	public boolean interact(Char c) {
		sprite.turnTo( pos, c.pos );

		if (c != Dungeon.hero){
			return super.interact(c);
		}

		if (Dungeon.hero.belongings.getItem(Amulet.class) != null || Dungeon.hero.belongings.getItem(SoulOfYendor.class) != null) {
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndQuest(RatKing.this, Messages.get(RatKing.this,"amulet")){
						@Override
						public void hide() {
							if (Dungeon.hero.belongings.getItem(Amulet.class) != null)
								Dungeon.hero.belongings.getItem(Amulet.class).detach(Dungeon.hero.belongings.backpack);
							else
								Dungeon.hero.belongings.getItem(SoulOfYendor.class).detach(Dungeon.hero.belongings.backpack);
							Statistics.deepestFloor = -1;
							InterlevelScene.curTransition = new LevelTransition(Dungeon.level, -1, LevelTransition.Type.REGULAR_ENTRANCE, 0, Dungeon.branch, LevelTransition.Type.REGULAR_EXIT);
							InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
							Game.switchScene(InterlevelScene.class);
						}
					});
				}
			});
			return true;
		}

		KingsCrown crown = Dungeon.hero.belongings.getItem(KingsCrown.class);
		if (state == SLEEPING) {
			notice();
			yell( Messages.get(this, "not_sleeping") );
			state = WANDERING;
		} else if (crown != null){
			if (Dungeon.hero.belongings.armor() == null){
				yell( Messages.get(RatKing.class, "crown_clothes") );
			} else {
				Badges.validateRatmogrify();
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show(new WndOptions(
								sprite(),
								Messages.titleCase(name()),
								Messages.get(RatKing.class, "crown_desc"),
								Messages.get(RatKing.class, "crown_yes"),
								Messages.get(RatKing.class, "crown_info"),
								Messages.get(RatKing.class, "crown_no")
						){
							@Override
							protected void onSelect(int index) {
								if (index == 0){
									crown.upgradeArmor(Dungeon.hero, Dungeon.hero.belongings.armor(), new Ratmogrify());
									((RatKingSprite)sprite).resetAnims();
									yell(Messages.get(RatKing.class, "crown_thankyou"));
								} else if (index == 1) {
									GameScene.show(new WndInfoArmorAbility(new Ratmogrify()));
								} else {
									yell(Messages.get(RatKing.class, "crown_fine"));
								}
							}
						});
					}
				});
			}
		} else if (!discussedShard && Dungeon.hero.belongings.getItem(CrownShard.class) != null) {
			yell(Messages.get(this, "crownshard"));
			discussedShard = true;
		}
		else if (Dungeon.hero.armorAbility instanceof Ratmogrify) {
			yell( Messages.get(RatKing.class, "crown_after") );
		} else {
			yell( Messages.get(this, "what_is_it") );
		}
		return true;
	}
	
	@Override
	public String description() {
		if (Holiday.getCurrentHoliday() == Holiday.WINTER_HOLIDAYS){
			return Messages.get(this, "desc_festive");
		} else {
			return super.description();
		}
	}
}
