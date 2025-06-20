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

package com.zrp200.rkpd2.actors.buffs;

import static com.zrp200.rkpd2.Dungeon.*;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import static com.zrp200.rkpd2.Dungeon.hero;

public class RevealedArea extends FlavourBuff{

	public static int distance() {
		// 3x3 normally, Seer Shot however gets 3x3/5x5/7x7
		return Math.max(1, hero.pointsInTalent(Talent.SEER_SHOT));
	}

	{
		type = Buff.buffType.POSITIVE;
	}

	public int pos, depth, branch;
	@SuppressWarnings("unused") public RevealedArea(){}
	public RevealedArea(int pos) {
		this.pos = pos;
		this.depth = Dungeon.depth;
		this.branch = Dungeon.branch;
		spend( duration() );
		Cooldown.affectHero(Talent.SeerShotCooldown.class);
	}

	@Override
	public void detach() {
		super.detach();
		Dungeon.observe();
		GameScene.updateFog(pos, distance()+1);
	}

	@Override
	public int icon() {
		return BuffIndicator.MIND_VISION;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0, 1, 1);
	}

	public static int duration() {
		if (hero.heroClass.isExact(HeroClass.HUNTRESS)) return 5;
		return 5*hero.pointsInTalent(Talent.SEER_SHOT, Talent.RK_WARDEN); }
	@Override
	public float iconFadePercent() {
		return Math.max(0, (duration()-visualcooldown()) / duration());
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)visualcooldown());
	}

	private static final String BRANCH = "branch";
	private static final String DEPTH = "depth";
	private static final String POS = "pos";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DEPTH, depth);
		bundle.put(BRANCH, branch);
		bundle.put(POS, pos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		depth = bundle.getInt(DEPTH);
		branch = bundle.getInt(BRANCH);
		pos = bundle.getInt(POS);
	}
}
