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

import static com.zrp200.rkpd2.Dungeon.hero;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.CounterBuff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Regeneration;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.BlobEmitter;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.effects.particles.ShaftParticle;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HallowedGround extends TargetedClericSpell {

	public static final HallowedGround INSTANCE = new HallowedGround();

	@Override
	public int icon() {
		return HeroIcon.HALLOWED_GROUND;
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
	public boolean usesTargeting() {
		return !SpellEmpower.isActive();
	}

	@Override
	public int targetingFlags() {
		return Ballistica.STOP_TARGET;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.shiftedPoints(Talent.HALLOWED_GROUND) > (SpellEmpower.isActive() ? 0 : 1);
	}

    private final ArrayList<Char> affected = new ArrayList<>();
    private boolean[] canSeed;


	@Override
	public void onCast(HolyTome tome, Hero hero) {
		affected.clear();
		canSeed = BArray.not(Dungeon.level.solid, null);
		if (SpellEmpower.isActive()) {
			// flood the hero's field of view with hallowed ground
			// doing it in a loop causes it to extend a bit out of the hero's field of view
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.heroFOV[i] && !Dungeon.level.solid[i]) {
					onTargetSelected(tome, hero, i);
				}
			}
			hero.sprite.operate(hero.pos);
			onSpellCast(tome, hero);
		} else {
			super.onCast(tome, hero);
		}
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {

		if (target == null){
			return;
		}

		if (Dungeon.level.solid[target] || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "invalid_target"));
			return;
		}

		PathFinder.buildDistanceMap(target, canSeed, Math.max(1, hero.pointsInTalent(Talent.HALLOWED_GROUND)));
		for (int i = 0; i < Dungeon.level.length(); i++){
			if (canSeed[i] && PathFinder.distance[i] != Integer.MAX_VALUE){
				canSeed[i] = false;

				int quantity = 0;
				int c = Dungeon.level.map[i];
				if (c == Terrain.EMPTY || c == Terrain.EMBERS || c == Terrain.EMPTY_DECO) {
					Level.set( i, Terrain.GRASS);
					quantity += 2;
				}
				if (SpellEmpower.isActive()) {
					int points = hero.pointsInTalent(Talent.HALLOWED_GROUND);
					if (
							// 13/25/38/50% chance to immediately give grass
							Random.Int(quantity > 0 ? 8 : 4) <= points
					) {
						Level.set(i, isStale() ? Terrain.FURROWED_GRASS : Terrain.HIGH_GRASS);
						quantity += 5;
					}
				}
				if (quantity > 0) {
					GameScene.updateMap(i);
					CellEmitter.get(i).burst(LeafParticle.LEVEL_SPECIFIC, quantity);
				}

				HallowedTerrain terrain = Blob.seed(i, 20, HallowedTerrain.class);
				GameScene.add(terrain);
				CellEmitter.get(i).burst(ShaftParticle.FACTORY, 2);
				Char ch = Actor.findChar(i);
				if (ch != null){
					affected.add(ch);
				}
			}
		}
		if (SpellEmpower.isActive()) return;
		hero.sprite.zap(target);
		onSpellCast(tome, hero);
	}

	@Override
	public void onSpellCast(HolyTome tome, Hero hero) {
		Char ally = PowerOfMany.getPoweredAlly();
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
			if (affected.contains(hero) && !affected.contains(ally)){
				affected.add(ally);
			} else if (!affected.contains(hero) && affected.contains(ally)){
				affected.add(hero);
			}
		}

		for (Char ch : affected){
			affectChar(ch);
		}

		//5 casts per hero level before furrowing
		Buff.affect(hero, HallowedFurrowTracker.class).countUp(1);

		Sample.INSTANCE.play(Assets.Sounds.MELD);
		hero.spendAndNext( 1f );
		super.onSpellCast(tome, hero);
	}

	private int getHeal() { return 15 + 10 * (SpellEmpower.isActive() ? hero.pointsInTalent(Talent.HALLOWED_GROUND) : 0); }

	private void affectChar( Char ch ){
		if (ch.alignment == Char.Alignment.ALLY){
			int heal = getHeal();
			if (ch == Dungeon.hero || ch.HP == ch.HT){
				Buff.affect(ch, Barrier.class).incShield(heal);
			} else {
				int barrier = heal - (ch.HT - ch.HP);
				barrier = Math.max(barrier, 0);
				ch.HP += heal -= barrier;
				ch.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING );
				if (barrier > 0){
					Buff.affect(ch, Barrier.class).incShield(barrier);
				}
			}
		} else if (!ch.flying) {
			// spell empower increases the duration slightly
			Buff.affect(ch, Roots.class, 1 + (SpellEmpower.isActive() ? hero.shiftedPoints(Talent.HALLOWED_GROUND) : 1));
		}
	}

	@Override
	protected List<Object> getDescArgs() {
		int area = 1 + 2*Dungeon.hero.pointsInTalent(Talent.HALLOWED_GROUND);
		int heal = 15 + 10 * (SpellEmpower.isActive() ? hero.pointsInTalent(Talent.HALLOWED_GROUND) : 0);
		int radius = hero.pointsInTalent(Talent.HALLOWED_GROUND);
		int root = 1 + hero.shiftedPoints(Talent.HALLOWED_GROUND);
		return Arrays.asList(area, heal, radius, root);
	}

	public static class HallowedTerrain extends Blob {

		@Override
		protected void evolve() {

			int cell;

			Fire fire = (Fire)Dungeon.level.blobs.get( Fire.class );

			ArrayList<Char> affected = new ArrayList<>();

			// on avg, hallowed ground produces 9/17/25 tiles of grass, 100/67/50% of total tiles
			int chance = 10 + 10*Dungeon.hero.pointsInTalent(Talent.HALLOWED_GROUND);

			for (int i = area.left-1; i <= area.right; i++) {
				for (int j = area.top-1; j <= area.bottom; j++) {
					cell = i + j*Dungeon.level.width();
					if (cur[cell] > 0) {

						//fire destroys hallowed terrain
						if (fire != null && fire.volume > 0 && fire.cur[cell] > 0){
							off[cell] = cur[cell] = 0;
							continue;
						}

						int c = Dungeon.level.map[cell];
						if (c == Terrain.GRASS && Dungeon.level.plants.get(c) == null) {
							if (Random.Int(chance) == 0) {
								if (isStale()){
									Level.set(cell, Terrain.FURROWED_GRASS);
								} else {
									Level.set(cell, Terrain.HIGH_GRASS);
								}
								GameScene.updateMap(cell);
								CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC, 5);
							}
						} else if (c == Terrain.EMPTY || c == Terrain.EMBERS || c == Terrain.EMPTY_DECO) {
							Level.set(cell, Terrain.GRASS);
							GameScene.updateMap(cell);
							CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC, 2);
						}

						Char ch = Actor.findChar(cell);
						if (ch != null){
							affected.add(ch);
						}

						off[cell] = cur[cell] - 1;
						volume += off[cell];
					} else {
						off[cell] = 0;
					}
				}
			}

			Char ally = PowerOfMany.getPoweredAlly();
			if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
				if (affected.contains(Dungeon.hero) && !affected.contains(ally)){
					affected.add(ally);
				} else if (!affected.contains(Dungeon.hero) && affected.contains(ally)){
					affected.add(Dungeon.hero);
				}
			}
			for (Char ch : affected){
				affectChar(ch);
			}

		}

		private void affectChar( Char ch ){
			if (ch.alignment == Char.Alignment.ALLY){
				if (ch.HP == ch.HT || ch == Dungeon.hero && isStale()){
					Buff.affect(ch, Barrier.class).incShield(1);
				} else {
					ch.HP++;
					ch.sprite.showStatusWithIcon( CharSprite.POSITIVE, "1", FloatingText.HEALING );
				}
			} else if (!ch.flying && ch.buff(Roots.class) == null){
				Buff.prolong(ch, Cripple.class, 1f);
			}
		}

		@Override
		public void use(BlobEmitter emitter) {
			super.use( emitter );
			emitter.pour( ShaftParticle.FACTORY, 1f );
		}

		@Override
		public String tileDesc() {
			return Messages.get(this, "desc");
		}
	}

	private static boolean isStale() {
		return !Regeneration.regenOn() ||
				Dungeon.hero.buff(HallowedFurrowTracker.class) != null
				&& Dungeon.hero.buff(HallowedFurrowTracker.class).count() > 5;
	}

	public static class HallowedFurrowTracker extends CounterBuff{{revivePersists = true;}}

}
