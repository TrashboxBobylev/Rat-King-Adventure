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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.zrp200.rkpd2.utils.GLog;

public class Radiance extends ClericSpell {

	public static final Radiance INSTANCE = new Radiance();

	@Override
	public int icon() {
		return HeroIcon.RADIANCE;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo make icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.subClass == HeroSubClass.PRIEST;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		GameScene.flash( 0x80FFFFFF );
		Sample.INSTANCE.play(Assets.Sounds.BLAST);

		if (Dungeon.level.viewDistance < 6 ){
			Buff.prolong(hero, Light.class, Dungeon.isChallenged(Challenges.DARKNESS) ? 20 : 100);
		}

		if (SpellEmpower.isActive()) {
			// note: is this really a good idea?
			GLog.p(Messages.get(this, "empowered"));
		}

		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos]) {
				if (SpellEmpower.isActive()) {
					// trigger illuminated effect for free
					mob.damage(hero.lvl, GuidingLight.INSTANCE);
				}
				Buff.affect(mob, GuidingLight.Illuminated.class);
				Buff.affect(mob, GuidingLight.WasIlluminatedTracker.class);
				Buff.affect(mob, Paralysis.class, 3f);
			}
		}

		hero.spend( 1f );
		hero.busy();
		hero.sprite.operate(hero.pos);

		onSpellCast(tome, hero);

	}
}
