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
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.AscendedForm;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.utils.GLog;

public class Judgement extends ClericSpell {

	public static Judgement INSTANCE = new Judgement();

	@Override
	public int icon() {
		return HeroIcon.JUDGEMENT;
	}

	@Override
	public float chargeUse(Hero hero) {
		return 3;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero)
				&& hero.hasTalent(Talent.JUDGEMENT)
				&& hero.buff(AscendedForm.AscendBuff.class) != null;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		hero.sprite.attack(hero.pos, new Callback() {
			@Override
			public void call() {
				GameScene.flash( 0x80FFFFFF );
				Sample.INSTANCE.play(Assets.Sounds.BLAST);

				int damageBase = 5 + 5*hero.pointsInTalent(Talent.JUDGEMENT);
				damageBase += 5*hero.buff(AscendedForm.AscendBuff.class).spellCasts;

				boolean empowered = SpellEmpower.isActive();
				boolean affected = false;

				for (Char ch : Actor.chars()){
					if (ch.alignment != hero.alignment && Dungeon.level.heroFOV[ch.pos]){
						affected = true;
						int damage = Random.NormalIntRange(damageBase, 2*damageBase);
						if (empowered) {
							Buff.affect(ch, GuidingLight.Illuminated.class);
							Buff.affect(ch, GuidingLight.WasIlluminatedTracker.class);
							damage += hero.lvl;
							// vfx?
						}
						ch.damage( damage, Judgement.this);
					}
				}

				if (affected && empowered) {
					GLog.p(Messages.get(Radiance.class, "empowered"));
				}

				hero.spendAndNext( 1f );
				onSpellCast(tome, hero);

				hero.buff(AscendedForm.AscendBuff.class).spellCasts = 0;

			}
		});
		hero.busy();

	}

	@Override
	public String desc() {
		int baseDmg = 5 + 5*Dungeon.hero.pointsInTalent(Talent.JUDGEMENT);
		int totalBaseDmg = baseDmg;
		if (Dungeon.hero.buff(AscendedForm.AscendBuff.class) != null) {
			totalBaseDmg += 5 * Dungeon.hero.buff(AscendedForm.AscendBuff.class).spellCasts;
		}

		return Messages.get(this, "desc", baseDmg, 2*baseDmg, totalBaseDmg, 2*totalBaseDmg) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}
}
