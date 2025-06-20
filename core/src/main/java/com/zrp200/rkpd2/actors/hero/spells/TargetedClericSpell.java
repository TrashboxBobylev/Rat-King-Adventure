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

package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.QuickSlotButton;

public abstract class TargetedClericSpell extends ClericSpell implements QuickSlotButton.Aimable {

	@Override
	public void onCast(HolyTome tome, Hero hero ){
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				onTargetSelected(tome, hero, cell);
			}

			@Override
			public String prompt() {
				return targetingPrompt();
			}
		});
	}

	@Override
	public int targetingFlags(){
		return Ballistica.MAGIC_BOLT;
	}

	@Override
	public int targetingPos(Hero hero, int pos) {
		return new Ballistica(hero.pos, pos, targetingFlags()).collisionPos;
	}

	protected String targetingPrompt(){
		return Messages.get(this, "prompt");
	}

	protected abstract void onTargetSelected(HolyTome tome, Hero hero, Integer target);

}
