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

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.items.wands.WandOfRegrowth;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.hero;

public class BloomingPick extends MeleeWeapon {

	public static final String AC_MINE	= "MINE";

	public static final float TIME_TO_MINE = 3f;
	
	{
		image = ItemSpriteSheet.BLOOMING_PICK;
		hitSound = Assets.Sounds.EVOKE;
		hitSoundPitch = 1.2f;
		defaultAction = AC_MINE;
		
		tier = 6;
	}
	
	@Override
	public int max(int lvl) {
		return  Math.round(4*(tier+1)) +     //24 base, down from 30
				lvl*Math.round((tier+1));  //+7 per level
	}

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_MINE );
		return actions;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		int healAmt = Math.round(damage*0.666f);
		healAmt = Math.min( healAmt, attacker.HT - attacker.HP );

		if (healAmt > 0 && attacker.isAlive()) {

			attacker.HP += healAmt;
			attacker.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 1 );
			attacker.sprite.showStatus( CharSprite.POSITIVE, Integer.toString( healAmt ) );

		}
		return super.proc(attacker, defender, damage);
	}

	@Override
	public void execute( final Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_MINE)) {

			if (Dungeon.bossLevel()){
				GLog.w(Messages.get(this, "no_boss"));
			} else {
				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {

					final int pos = hero.pos + PathFinder.NEIGHBOURS8[i];
					if (Dungeon.level.insideMap(pos) && (Dungeon.level.map[pos] == Terrain.WALL || Dungeon.level.map[pos] == Terrain.DOOR)) {

						hero.spend(TIME_TO_MINE);
						hero.busy();

						hero.sprite.attack(pos, new Callback() {

							@Override
							public void call() {

								CellEmitter.center(pos).burst(Speck.factory(Speck.STAR), 7);
								Sample.INSTANCE.play(Assets.Sounds.EVOKE);
								Sample.INSTANCE.play(Assets.Sounds.ROCKS);

								Level.set(pos, Terrain.EMBERS);
								GameScene.updateMap(pos);

								hero.onOperateComplete();
							}
						});

						return;
					}
				}

				GLog.w(Messages.get(this, "no_vein"));
			}

		}
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		WandOfRegrowth regrowth = new WandOfRegrowth();
		regrowth.upgrade(level());
		regrowth.fx(new Ballistica(hero.pos, enemy.pos, Ballistica.MAGIC_BOLT), () -> {
			regrowth.onZap(new Ballistica(hero.pos, enemy.pos, Ballistica.MAGIC_BOLT));
		});
		return super.warriorAttack(damage, enemy);
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected DuelistAbility duelistAbility() {
		return new VineStalling();
	}

	public static class VineStalling extends MeleeAbility {

		@Override
		public float dmgMulti(Char enemy) {
			return 0f;
		}

		@Override
		protected void playSFX() {
			Sample.INSTANCE.play(Sample.INSTANCE.play(Assets.Sounds.PLANT, 2f, 0.8f));
		}

		@Override
		public void afterHit(Char enemy, boolean hit) {
			if (enemy.isAlive()) {
				Buff.affect(enemy, VineCovered.class);
				float vineTime = (enemy.properties().contains(Char.Property.BOSS) || enemy.properties().contains(Char.Property.MINIBOSS)) ? 5f : 100_000_000f;
				enemy.spendConstant( vineTime );
				((Mob) enemy).clearEnemy();
				for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
					if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos] && Dungeon.level.distance(mob.pos, enemy.pos) <= 3) {
						mob.aggro(enemy);
					}
				}
				enemy.sprite.centerEmitter().burst( LeafParticle.LEVEL_SPECIFIC, 15 );
				for (Buff buff : enemy.buffs()) {
					if (buff.type == Buff.buffType.NEGATIVE && !(buff instanceof VineCovered)) {
						buff.detach();
					}
				}
			}
		}
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		return 3;
	}

	public static class VineCovered extends AllyBuff {

		{
			type = buffType.NEGATIVE;
			announced = true;

			properties.add(Char.Property.IMMOVABLE);
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add(CharSprite.State.VINECOVERED);
			else    target.sprite.remove(CharSprite.State.VINECOVERED);
		}

		@Override
		public int icon() {
			return BuffIndicator.HERB_HEALING;
		}

		@Override
		public void detach() {
			super.detach();
			target.alignment = Char.Alignment.ENEMY;
			AllyBuff.affectAndLoot((Mob) target, hero, PlaceVineHolder.class);
		}
	}

	public static class PlaceVineHolder extends AllyBuff {}
}
