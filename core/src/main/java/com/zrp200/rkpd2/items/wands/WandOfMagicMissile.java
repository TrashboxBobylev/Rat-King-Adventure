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

package com.zrp200.rkpd2.items.wands;

import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;

public class WandOfMagicMissile extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_MAGIC_MISSILE;
	}

	public int min(int lvl){
		return 2+lvl;
	}

	public int max(int lvl){
		return 8+2*lvl;
	}
	
	@Override
	public void onZap(Ballistica bolt) {
				
		Char ch = Actor.findChar( bolt.collisionPos );
		if (ch != null) {

			int damage = damageRoll();
			wandProc(ch, chargesPerCast(), damage);
			ch.damage(damage, this);
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f) );

			ch.sprite.burst(0xFFFFFFFF, buffedLvl() / 2 + 2);

			//apply the magic charge buff if we have another wand in inventory of a lower level, or already have the buff
			for (Wand.Charger wandCharger : curUser.buffs(Wand.Charger.class)){
				if (wandCharger.wand().buffedLvl() < buffedLvl() || curUser.buff(MagicCharge.class) != null){
					Buff.prolong(curUser, MagicCharge.class, MagicCharge.DURATION).setup(this);
					break;
				}
			}

		} else {
			Dungeon.level.pressCell(bolt.collisionPos);
		}
	}

	@Override
	public void onHit(Weapon staff, Char attacker, Char defender, int damage) {
		SpellSprite.show(attacker, SpellSprite.CHARGE);
		for (Wand.Charger c : attacker.buffs(Wand.Charger.class)){
			if (c.wand() != this){
				c.gainCharge(0.5f * Weapon.Enchantment.procChanceMultiplier(attacker));
			}
		}
	}
	
	protected int initialCharges() {
		return 3;
	}

	public static class MagicCharge extends FlavourBuff {

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		public static float DURATION = 4f;

		private int level = 0;
		private Wand wandJustApplied; //we don't bundle this as it's only used right as the buff is applied

		public void setup(Wand wand){
			if (level < wand.buffedLvl()){
				this.level = wand.buffedLvl();
				this.wandJustApplied = wand;
			}
		}

		public boolean appliesTo(Wand wand) {
			return level > wand.buffedLvl(false);
		}

		@Override
		public void detach() {
			super.detach();
			updateQuickslot();
		}

		public int level(){
			return this.level;
		}

		//this is used briefly so that a wand of magic missile can't clear the buff it just applied
		public Wand wandJustApplied(){
			Wand result = this.wandJustApplied;
			this.wandJustApplied = null;
			return result;
		}

		@Override
		public int icon() {
			return BuffIndicator.UPGRADE;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.2f, 0.6f, 1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", level()-Dungeon.hero.getBonus(new WandOfMagicMissile()), dispTurns());
		}

		private static final String LEVEL = "level";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LEVEL, level);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			level = bundle.getInt(LEVEL);
		}
	}

}
