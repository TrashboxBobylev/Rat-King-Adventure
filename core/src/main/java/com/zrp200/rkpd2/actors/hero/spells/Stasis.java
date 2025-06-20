/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.LifeLink;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.npcs.DirectableAlly;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Stasis extends ClericSpell {

	public static Stasis INSTANCE = new Stasis();

	@Override
	public int icon() {
		return HeroIcon.STASIS;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", 20 + 20*Dungeon.hero.pointsInTalent(Talent.STASIS)) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero)
				&& hero.hasTalent(Talent.STASIS)
				&& (PowerOfMany.getPoweredAlly() != null || hero.buff(StasisBuff.class) != null);
	}

	@Override
	public float chargeUse(Hero hero) {
		if (hero.buff(StasisBuff.class) != null){
			return 0;
		}
		return super.chargeUse(hero);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		onSpellCast(tome, hero);

		if (hero.buff(StasisBuff.class) != null){
			hero.sprite.operate(hero.pos);
			hero.buff(StasisBuff.class).act();
			return;
		}

		Char ally = PowerOfMany.getPoweredAlly();

		hero.sprite.zap(ally.pos);
		MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.LIGHT_MISSILE, ally.sprite, hero.pos, null);

		LinkedHashSet<Buff> buffs = ally.buffs();
		Actor.remove(ally);
		ally.sprite.killAndErase();
		ally.sprite = null;
		Dungeon.level.mobs.remove(ally);
		for (Buff b : buffs){
			if (b.type == Buff.buffType.POSITIVE || b.revivePersists) {
				ally.add(b);
			}
		}
		ally.clearTime();

		Buff.prolong(hero, StasisBuff.class, 20 + 20*hero.pointsInTalent(Talent.STASIS)).stasisAlly = (Mob)ally;
		Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

		if (hero.buff(LifeLink.class) != null && hero.buff(LifeLink.class).object == ally.id()){
			hero.buff(LifeLink.class).detach();
		}

		if (SpellEmpower.isActive()) {
			// cast instantly
			GLog.p(Messages.get(SpellEmpower.class, "instant"));
			hero.next();
		} else {
			hero.spendAndNext(Actor.TICK);
		}

		Dungeon.observe();
		GameScene.updateFog();

	}

	public static Char getStasisAlly(){
		if (Dungeon.hero != null && Dungeon.hero.buff(StasisBuff.class) != null){
			return Dungeon.hero.buff(StasisBuff.class).stasisAlly;
		}
		return null;
	}

	public static class StasisBuff extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.MANY_POWER;
		}

		@Override
		public float iconFadePercent() {
			int duration = 20 + 20*Dungeon.hero.pointsInTalent(Talent.STASIS);
			return Math.max(0, (duration - visualcooldown()) / duration);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Messages.titleCase(stasisAlly.name()), dispTurns());
		}

		@Override
		public boolean act() {
			ArrayList<Integer> spawnPoints = new ArrayList<>();
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				int p = target.pos + PathFinder.NEIGHBOURS8[i];
				if (Actor.findChar(p) == null
						&& (Dungeon.level.passable[p] || (stasisAlly.flying && Dungeon.level.avoid[p])) ){
					spawnPoints.add(p);
				}
			}
			if (spawnPoints.isEmpty()){
				spawnPoints.add(target.pos + PathFinder.NEIGHBOURS8[Random.Int(8)]);
			}
			stasisAlly.pos = Random.element(spawnPoints);
			GameScene.add(stasisAlly);

			if (stasisAlly instanceof DirectableAlly){
				((DirectableAlly) stasisAlly).clearDefensingPos();
			}

			if (stasisAlly.buff(LifeLink.class) != null){
				Buff.prolong(Dungeon.hero, LifeLink.class, stasisAlly.buff(LifeLink.class).cooldown()).object = stasisAlly.id();
			}

			ScrollOfTeleportation.appear(stasisAlly, stasisAlly.pos);
			Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

			return super.act();
		}

		Mob stasisAlly;

		private static final String ALLY = "ally";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(ALLY, stasisAlly);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			stasisAlly = (Mob)bundle.get(ALLY);
		}
	}

}
