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

package com.zrp200.rkpd2.items;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRemoveCurse;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

abstract public class KindOfWeapon extends EquipableItem {

	protected String hitSound = Assets.Sounds.HIT;
	protected float hitSoundPitch = 1f;

	@Override
	public void execute(Hero hero, String action) {
		if (hero.subClass.is(HeroSubClass.CHAMPION) && action.equals(AC_EQUIP)){
			usesTargeting = false;
			// standard equip = replace one of the two
			// missile with 1 = replace one of the two, equip to slot three.
			// missile with 3, non-thrown in slot three, replace one of the three
			int points = hero.pointsInTalent(Talent.ELITE_DEXTERITY);
			slotOfUnequipped = -1;

			final int THROWN_SLOT = 2;

			// missile weapon with one slot open should just auto-add.
			if (this instanceof MissileWeapon) {
				if(points > 1 || hero.belongings.secondWep == null) {
					KindOfWeapon third = hero.belongings.thirdWep;
					if (third == null || third instanceof MissileWeapon) {
						doEquip(hero, THROWN_SLOT);
						return;
					}
				}
			}

			// equipping thrown and melee at the same time only works at +2
			// equipping melee to thrown slot only works at +3
			String[] names = new String[points < (KindOfWeapon.this instanceof MissileWeapon ? 2 : 3) ? 2 : 3];
			final String[] name_key = {"primary", "secondary", "tertiary"};
			KindOfWeapon[] weapons = hero.belongings.weapons();
			if(points == 1 && weapons[THROWN_SLOT] != null) {
				// prefer second slot over first slot
				for (int i = THROWN_SLOT - 1; i >= 0; i--) {
					// one of these should always be empty, but avoid making this assumption
					if (weapons[i] == null) {
						// pretend thrown weapon is in this slot
						weapons[i] = weapons[THROWN_SLOT];
						break;
					}
				}
			}
			for (int i = 0; i < names.length; i++) {
				KindOfWeapon weapon = weapons[i];
				names[i] = Messages.titleCase(weapon != null ? weapon.trueName() : Messages.get(KindOfWeapon.class, "empty"));
				if (names[i].length() > 18) names[i] = names[i].substring(0, 15) + "...";
				names[i] = Messages.get(KindOfWeapon.class, "which_equip_" + name_key[i], names[i]);
			}
			GameScene.show(new WndOptions(
					new ItemSprite(this),
					Messages.titleCase(name()),
					Messages.get(KindOfWeapon.class, "which_equip_msg"),
					names
			){
				@Override
				protected void onSelect(int index) {
					super.onSelect(index);
					if (index < 0 || index >= names.length) return;
					KindOfWeapon replaced = weapons[index];
					// src is slot to clear, dst is slot to equip to
					int dst = index, src = index;
					// index is the "pretend" slot
					if (points < 3 && replaced instanceof MissileWeapon ^ KindOfWeapon.this instanceof MissileWeapon) {
						if (replaced instanceof MissileWeapon) {
							// equipping melee to thrown slot
							// clear the thrown slot but put the weapon in the selected slot
							src = THROWN_SLOT;
						} else if (points < 2) {
							// equipping thrown to melee slot
							// clear the melee slot and put the thrown weapon into the third slot
							dst = THROWN_SLOT;
						}
					}
					if (dst != src) {
						// we want to avoid equipping a missile weapon to the wrong slot, so move it to the correct slot before equipping
						hero.belongings.setWeapon(dst, replaced);
						hero.belongings.setWeapon(src, null);
						if (!doEquip(hero, dst)) {
							hero.belongings.setWeapon(src, replaced);
						}
					} else {
						doEquip(hero, index);
					}
				}
			});
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		if (hero != null) for (KindOfWeapon weapon : hero.belongings.weapons()) if (weapon == this) return true;
		return false;
	}

    private static boolean isSwiftEquipping = false;

    protected float timeToEquip( Hero hero ) {
        return isSwiftEquipping ? 0f : super.timeToEquip(hero);
    }

	@Override
	final public boolean doEquip( Hero hero ) { return doEquip(hero, 0); }
	public boolean doEquip( Hero hero, int index ) {

		isSwiftEquipping = false;
		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)){
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()){
				isSwiftEquipping = true;
			}
		}

		// 30/50% chance
		if (!hero.heroClass.is(HeroClass.CLERIC) && hero.canHaveTalent(Talent.HOLY_INTUITION)
				&& cursed
				&& Random.Int(10) < 1 + 2*hero.pointsInTalent(Talent.HOLY_INTUITION)){
			ScrollOfRemoveCurse.doEffect(hero, this);
		}

		if (!(this instanceof MissileWeapon)) {
			// hopefully this doesn't cause weirdness
			detachAll( hero.belongings.backpack );
		}
		KindOfWeapon equipped = hero.belongings.weapon(index);

		if (equipped == null || equipped.doUnequip( hero, true )) {

			hero.belongings.setWeapon(index, this);
			activate( hero );
			Talent.onItemEquipped(hero, this);
			Badges.validateDuelistUnlock();
			updateQuickslot();

			int slot = Dungeon.quickslot.getSlot(this);
			if (slot != -1) {
				Dungeon.quickslot.setSlot(slot, this);
			} else if (slotOfUnequipped != -1 && defaultAction() != null) {
				//if this item wasn't quickslotted, but the item it is replacing as equipped was
				//then also have the item occupy the unequipped item's quickslot
				Dungeon.quickslot.setSlot( slotOfUnequipped, this );
				slotOfUnequipped = -1;
			}

			updateQuickslot();

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(KindOfWeapon.class, "equip_cursed") );
			}

			hero.spendAndNext( timeToEquip(hero) );
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null) {
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;

		} else {
			isSwiftEquipping = false;
			collect( hero.belongings.backpack );
			return false;
		}
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		int index = hero.belongings.findWeapon(this);

		if (index > 0){
			//do this first so that the item can go to a full inventory
			hero.belongings.setWeapon(index, null);
		}

		if (super.doUnequip( hero, collect, single )) {

			if (index == 0){
				hero.belongings.weapon = null;
			}
			return true;

		} else {

			hero.belongings.setWeapon(index, this);
			return false;

		}
	}

	public int min(){
		return min(buffedLvl()) + (Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.WEAPON_MASTERY) ?
				(2 + Dungeon.hero.pointsInTalent(Talent.WEAPON_MASTERY)) : 0);
	}

	public int max(){
		return max(buffedLvl());
	}

	abstract public int min(int lvl);
	abstract public int max(int lvl);

	public int damageRoll( Char owner ) {
		if (owner instanceof Hero){
			return Hero.heroDamageIntRange(min(), max());
		} else {
			return Random.NormalIntRange(min(), max());
		}
	}

	public float accuracyFactor( Char owner, Char target ) {
		return 1f;
	}

	public float delayFactor( Char owner ) {
		return 1f;
	}

	public int reachFactor( Char owner ){
		return 1;
	}

	public boolean canReach( Char owner, int target){
		return canReach(owner, target, reachFactor(owner));
	}

	public static boolean canReach( Char owner, int target, int reachFactor){
		int reach = reachFactor;
		if (Dungeon.level.distance( owner.pos, target ) > reach){
			return false;
		} else {
			boolean[] passable = BArray.not(Dungeon.level.solid, null);
			for (Char ch : Actor.chars()) {
				if (ch != owner) passable[ch.pos] = false;
			}

			PathFinder.buildDistanceMap(target, passable, reach);

			return PathFinder.distance[owner.pos] <= reach;
		}
	}

	public int defenseFactor( Char owner ) {
		return 0;
	}

	public int proc( Char attacker, Char defender, int damage ) {
		return damage;
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(hitSound, 1, pitch * hitSoundPitch);
	}

}
