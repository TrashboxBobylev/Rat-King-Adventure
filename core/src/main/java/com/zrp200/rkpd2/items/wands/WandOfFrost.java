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

package com.zrp200.rkpd2.items.wands;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Chill;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.levels.rooms.special.MagicalFireRoom;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfFrost extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_FROST;
	}

	public int min(int lvl){
		return 2+lvl;
	}

	public int max(int lvl){
		return 8+5*lvl;
	}

	@Override
	public void onZap(Ballistica bolt) {

		Integer collisionPos = bolt.collisionPos;
		if (curUser.pointsInTalent(Talent.CRYONIC_SPELL) > 2){
			for (int i : PathFinder.NEIGHBOURS8){
				effect(collisionPos + i);
			}
		}
		effect(collisionPos);

		MagicalFireRoom.EternalFire eternalFire = (MagicalFireRoom.EternalFire)Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
		if (eternalFire != null && eternalFire.volume > 0) {
			eternalFire.clear( bolt.collisionPos );
			//bolt ends 1 tile short of fire, so check next tile too
			if (bolt.path.size() > bolt.dist+1){
				eternalFire.clear( bolt.path.get(bolt.dist+1) );
			}

		}
	}

	public void effect(int collisionPos) {
		Heap heap = Dungeon.level.heaps.get(collisionPos);
		if (heap != null) {
			heap.freeze();
		}

		Fire fire = (Fire) Dungeon.level.blobs.get(Fire.class);
		if (fire != null && fire.volume > 0) {
			fire.clear( collisionPos );
		}

		Char ch = Actor.findChar(collisionPos);
		if (ch != null){

			int damage = damageRoll();

			if (ch.buff(Frost.class) != null){
				return; //do nothing, can't affect a frozen target
			}
			if (ch.buff(Chill.class) != null){
				//6.67% less damage per turn of chill remaining, to a max of 10 turns (50% dmg)
				float chillturns = Math.min(10, ch.buff(Chill.class).cooldown());
				damage = (int)Math.round(damage * Math.pow(0.9333f, chillturns));
			} else {
				ch.sprite.burst( 0xFF99CCFF, buffedLvl() / 2 + 2 );
			}

			wandProc(ch, chargesPerCast(), damage);
			ch.damage(damage, this);
			procKO(ch);
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );

			if (ch.isAlive()){
				if (curUser.pointsInTalent(Talent.CRYONIC_SPELL) > 1 && Random.Int(2) == 0){
					if (Dungeon.level.water[ch.pos])
						Buff.affect(ch, Frost.class, 4+buffedLvl());
					else
						Buff.affect(ch, Frost.class, 2+buffedLvl());
				} else {
					if (Dungeon.level.water[ch.pos])
						Buff.affect(ch, Chill.class, 4 + buffedLvl());
					else
						Buff.affect(ch, Chill.class, 2 + buffedLvl());
				}
			}
		} else {
			Dungeon.level.pressCell(collisionPos);
		}
	}

	@Override
	public String upgradeStat2(int level) {
		return Integer.toString(2 + level);
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(curUser.sprite.parent,
				MagicMissile.FROST,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(Weapon staff, Char attacker, Char defender, int damage) {
		Chill chill = defender.buff(Chill.class);

		if (chill != null) {

			//1/9 at 2 turns of chill, scaling to 9/9 at 10 turns
			float procChance = ((int)Math.floor(chill.cooldown()) - 1)/9f;
			procChance *= procChanceMultiplier(attacker);

			if (Random.Float() < procChance) {

				float powerMulti = Math.max(1f, procChance);

				//need to delay this through an actor so that the freezing isn't broken by taking damage from the staff hit.
				new FlavourBuff() {
					{
						actPriority = VFX_PRIO;
					}

					public boolean act() {
						Buff.affect(target, Frost.class, Math.round(Frost.DURATION * powerMulti));
						return super.act();
					}
				}.attachTo(defender);
			}
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0x88CCFF);
		particle.am = 0.6f;
		particle.setLifespan(2f);
		float angle = Random.Float(PointF.PI2);
		particle.speed.polar( angle, 2f);
		particle.acc.set( 0f, 1f);
		particle.setSize( 0f, 1.5f);
		particle.radiateXY(Random.Float(1f));
	}

}
