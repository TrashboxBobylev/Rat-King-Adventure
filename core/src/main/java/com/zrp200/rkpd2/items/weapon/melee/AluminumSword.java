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

package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AnkhInvulnerability;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.npcs.NPC;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.InWorldWeaponSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public class AluminumSword extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {

	{
		image = ItemSpriteSheet.ALUMINUM_SWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 6;
		DLY = 0.5f;
	}

	@Override
	public int max(int lvl) {
		return (int) (4f*(tier-1) + ((tier-1)*lvl)); //20 (+5)
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage += Buff.affect(attacker, Combo.class).hit(defender, damage);
		return super.proc(attacker, defender, damage);
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		Buff.affect(Dungeon.hero, AnkhInvulnerability.class, delayFactor(Dungeon.hero)*2);
		return super.warriorAttack(damage, enemy);
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return 10;
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return new Ballistica( user.pos, dst, Ballistica.FRIENDLY_MAGIC_BOLT ).collisionPos;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (target == null || hero.pos == target) {
			GLog.i( Messages.get(Wand.class, "self_target") );
			return;
		}

		hero.busy();

		beforeAbilityUsed(hero, null);
		hero.sprite.zap(target);
		Invisibility.dispel();

		final Ballistica shot = new Ballistica( curUser.pos, target, Ballistica.FRIENDLY_PROJECTILE,
				curUser.buff(ChampionEnemy.Projecting.class) != null && curUser.pointsInTalent(Talent.RK_PROJECT) == 3);

		Sample.INSTANCE.play( Assets.Sounds.ZAP );

		MagicMissile.boltFromChar( hero.sprite.parent,
				MagicMissile.FROGGIT,
				hero.sprite,
				shot.collisionPos,
				() -> {
					updateQuickslot();
					hero.spendAndNext(delayFactor(hero));
					afterAbilityUsed(hero);

					Char ch = Actor.findChar(shot.collisionPos);

					FloatingSword sword = new FloatingSword();
					sword.configure(15, AluminumSword.this);

					if (ch != null){
						int closest = -1;
						boolean[] passable = Dungeon.level.passable;

						for (int n : PathFinder.NEIGHBOURS9) {
							int c = shot.collisionPos + n;
							if (passable[c] && Actor.findChar( c ) == null
									&& (closest == -1 || (Dungeon.level.trueDistance(c, curUser.pos) < (Dungeon.level.trueDistance(closest, curUser.pos))))) {
								closest = c;
							}
						}

						if (closest == -1){
							GLog.n(Messages.get(CursedWand.class, "nothing"));
							return;
						} else {
							sword.pos = closest;
							GameScene.add(sword);
						}
					} else {
						sword.pos = shot.collisionPos;
						GameScene.add(sword);
					}

					ScrollOfTeleportation.appear( sword, sword.pos );
					sword.sprite.centerEmitter().burst(MagicMissile.WhiteParticle.FACTORY, 15);
				});
	}

	public static class Combo extends Buff {

		public int count = 0;

		@Override
		public int icon() {
			return BuffIndicator.COMBO;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0xa6b9c8);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc",((Hero)target).heroClass.title(), count, visualcooldown(), Math.round((count) / 3f * 100 + 100));
		}

		@Override
		public String iconTextDisplay() {
			return String.valueOf(visualcooldown());
		}

		public int hit(Char enemy, int damage ) {

			count++;

			if (count >= 2) {
				GLog.p(Messages.get(this, "combo"), count );
				postpone( 3f - count / 10f );
				return (int)(damage * (count - 1) / 3f);

			} else {

				postpone( 2f );
				return 0;

			}
		}

		@Override
		public boolean act() {
			detach();
			return true;
		}

	}

	public static class FloatingSword extends NPC {

		{
			alignment = Alignment.ALLY;

			spriteClass = InWorldWeaponSprite.class;

			properties.add(Property.IMMOVABLE);

			flying = true;
		}

		private float left;
		private AluminumSword weapon;

		public void configure(int timeLeft, AluminumSword weapon){
			left = timeLeft;
			this.weapon = weapon;
		}

		@Override
		protected boolean act() {
			if (properties().contains(Property.IMMOVABLE)){
				throwItems();
			}

			ArrayList<Char> affected = new ArrayList<>();

			PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 2 );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE) {
					if (Dungeon.level.heroFOV[i]) {
						Talent.SpellbladeForgeryWound.hit(i, 45, 0xCEDAE4);
						if (i % 3 == 0){
							weapon.hitSound(Random.Float(0.87f, 1.15f));
						}
					}
					Char ch = Actor.findChar(i);
					if (ch != null && ch.alignment != Alignment.ALLY){
						affected.add(ch);
					}
				}
			}

			for (Char ch: affected){
				int dmg = Math.round(weapon.damageRoll(this) / weapon.delayFactor(this));
				if (weapon.enchantment != null){
					dmg = weapon.enchantment.proc(weapon, this, ch, dmg);
				}
				ch.damage(dmg, this);
				if (Dungeon.level.heroFOV[ch.pos]){
					Sample.INSTANCE.play(Assets.Sounds.HIT);
				}
				if (Dungeon.hero != null){
					Dungeon.hero.buff(MeleeWeapon.Charger.class).gainCharge(0.25f);
				}
			}

			if (--left <= 0){
				die(null);
				if (Dungeon.level.heroFOV[pos]) {
					Sample.INSTANCE.play(Assets.Sounds.PUFF);
					Splash.at(DungeonTilemap.tileCenterToWorld(pos), -PointF.PI / 2, PointF.PI, 0xCEDAE4, 100, 0.003f);
				}
				spend(TICK);
				return true;
			}

			spend(TICK);

			return true;
		}

		//cannot damaged or influenced by buffs
		@Override
		public int defenseSkill( Char enemy ) {
			return INFINITE_EVASION;
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

		@Override
		public boolean interact(Char c) {
			return true;
		}

		private static final String LEFT = "left";
		private static final String WEAPON = "weapon";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LEFT, left);
			bundle.put(WEAPON, weapon);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			left = bundle.getFloat(LEFT);
			weapon = (AluminumSword) bundle.get(WEAPON);
		}

		@Override
		public String description() {
			return Messages.get(this, "desc", weapon.title(), (int)left);
		}
	}
}
