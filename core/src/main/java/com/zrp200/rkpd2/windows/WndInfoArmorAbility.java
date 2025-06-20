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

package com.zrp200.rkpd2.windows;

import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.TalentButton;
import com.zrp200.rkpd2.ui.TalentsPane;
import com.watabou.utils.function.Function;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndInfoArmorAbility extends WndTitledMessage {

	public WndInfoArmorAbility(ArmorAbility ability, Function<ArmorAbility, LinkedHashMap<Talent, Integer>> initializeArmorTalents){
		super( new HeroIcon(ability), Messages.titleCase(ability.name()), ability.desc());

		LinkedHashMap<Talent, Integer> talents = initializeArmorTalents.apply(ability);
		if(talents.isEmpty()) return;
		Ratmogrify.useRatroicEnergy = ability instanceof Ratmogrify;

		TalentsPane.TalentTierPane talentPane = new TalentsPane.TalentTierPane(talents, 4, TalentButton.Mode.INFO);
		talentPane.title.text( Messages.titleCase(Messages.get(WndHeroInfo.class, "talents")));
		addToBottom(talentPane, 5, 0);
	}
	public WndInfoArmorAbility(ArmorAbility ability) {
		this(ability, WndInfoArmorAbility::initializeTalents);
	}

	public static LinkedHashMap<Talent, Integer> initializeTalents(ArmorAbility ability) {
		ArrayList<LinkedHashMap<Talent, Integer>> talentList = Talent.initArmorTalents(ability);
		return talentList.size() < 4 ? new LinkedHashMap<>() : talentList.get(3);
	}

	@Override
	protected float targetHeight() {
		return super.targetHeight()-40;
	}
}
