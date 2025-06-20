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

import com.watabou.noosa.Image;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.BlobEmitter;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.SelectableCell;
import com.zrp200.rkpd2.effects.TargetedCell;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class WallOfLight extends TargetedClericSpell {

	//
	int toPlace = -1;

	public static WallOfLight INSTANCE = new WallOfLight();

	@Override
	public int icon() {
		return HeroIcon.WALL_OF_LIGHT;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo make icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	public String desc() {
		if (SpellEmpower.isActive()) {
			toPlace = 1;
			try {
				return super.desc();
			} finally {
				toPlace = 0;
			}

		}
		return super.desc();
	}

	@Override
	protected List<Object> getDescArgs() {
		return Collections.singletonList(1 + 2*Dungeon.hero.pointsInTalent(Talent.WALL_OF_LIGHT));
	}

	@Override
	public int targetingFlags(){
		return -1; //auto-targeting behaviour is often wrong, so we don't use it
	}

	@Override
	public float chargeUse(Hero hero) {
		if (toPlace > 0) {
			// 1/3 / 1/5 / 1/7
			return toPlace/(float)(1+2*hero.pointsInTalent(Talent.WALL_OF_LIGHT));
		}
		if (Dungeon.level.blobs.get(LightWall.class) != null
			&& Dungeon.level.blobs.get(LightWall.class).volume > 0){
			return 0f;
		}
		return 3f;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.shiftedPoints(Talent.WALL_OF_LIGHT) > (SpellEmpower.isActive() ? 0 : 1)
				// allow for clean-up of existing walls
				|| Dungeon.level.blobs.containsKey(LightWall.class)
				&& Dungeon.level.blobs.get(LightWall.class).volume > 0;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		if (SpellEmpower.isActive()) {
			GameScene.selectCell(new CellSelector.Listener() {
				{
					readyOnSelect = false;
				}
				// draw your own wall

				Image targetedCell = null;
				int originPos;

				void setTargetedCell(Integer cell) {
					if (targetedCell != null) {
						targetedCell.killAndErase();
					}
					if (cell != null) {
						targetedCell = new Image(Icons.get(Icons.TARGET));
						targetedCell.point(DungeonTilemap.tileToWorld( cell ));
						GameScene.scene.add(targetedCell);
						originPos = cell;
					} else {
						targetedCell = null;
					}
				}

				boolean wallPlaced = false;

				void destroy() {
					setTargetedCell(null);
					toPlace = 0;
					if (wallPlaced) hero.next();
					GameScene.clearCellSelector(true);
					GameScene.ready();
				}

				@Override
				public void onSelect(Integer cell) {
					if (cell == null || cell < 0) {
						destroy();
						return;
					}
					LightWall existing = (LightWall) Dungeon.level.blobs.get(LightWall.class);
					if (targetedCell == null) {
						if(existing != null && existing.clearConnected(cell)) {
							GLog.i(Messages.get(WallOfLight.this, "early_end"));
							destroy();
							return;
						}
						if (Dungeon.level.solid[cell]
								|| !Dungeon.level.heroFOV[cell]) {
							GLog.w(Messages.get(WallOfLight.this, "invalid_target"));
							destroy();
							return;
						}
						if (existing != null && existing.volume > 0) for (int dir : PathFinder.NEIGHBOURS4) {
							if (existing.cur[cell+dir] > 0) {
								GLog.w(Messages.get(WallOfLight.this, "existing"));
								destroy();
								return;
							}
						}
						setTargetedCell(cell);
					}
					else if (originPos == cell) {
						setTargetedCell(null);
					}
					else {
						Ballistica b = new Ballistica(originPos, cell, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
						if (!cell.equals(b.collisionPos) || !Dungeon.level.heroFOV[cell]) {
							GLog.w(Messages.get(WallOfLight.this, "invalid_target"));
							return;
						}

						List<Integer> walls = new ArrayList<>(b.subPath(0, b.dist));
						for (int i = 1; i < walls.size(); i++) {
							// fill in gaps in the wall
							Point p0 = Dungeon.level.cellToPoint(walls.get(i-1));
							Point p1 = Dungeon.level.cellToPoint(walls.get(i));
							if (p0.x != p1.x && p0.y != p1.y) {
								int toAdd = -1;
								float distance = 0;
								for (Point p : new Point[]{new Point(p0.x, p1.y), new Point(p1.x, p0.y)}) {
									int c = Dungeon.level.pointToCell(p);
									float dist = Dungeon.level.trueDistance(hero.pos, c);
									if (dist > distance || (dist == distance && Random.Int(2) == 0)) {
										distance = dist;
										toAdd = c;
									}
								}
								if (toAdd > -1 && !Dungeon.level.solid[toAdd]) {
									walls.add(i, toAdd);
								}
							}
						}

						if (existing != null && existing.cur != null) for (int wall : walls) {
							if (wall == originPos && existing.cur[originPos] > 0) continue;
							for (int dir : PathFinder.NEIGHBOURS4) {
								int c = wall+dir;
								if (c != originPos && existing.cur[wall+dir] > 0) {
									GLog.w(Messages.get(WallOfLight.this, "existing"));
									destroy();
									return;
								}
							}
						}


						// charge cost scales on walls placed. However, the charge cost is VERY decreased.
						toPlace = 0;
						for (int c : walls) if (!Dungeon.level.solid[c]) toPlace++;
						onSpellCast(tome, hero);
						toPlace = 0;

						hero.sprite.zap(Dungeon.level.pointToCell(
								// midpoint
								Dungeon.level.cellToPoint(cell)
										.offset(Dungeon.level.cellToPoint(originPos))
										.scale(0.5f)
						));

						Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
						if (!wallPlaced) {
							wallPlaced = true;
							hero.spend(1);
						}

						HashSet<Integer> placed = new HashSet<>();
						for (int wallPos : walls) {
							// fixme this isn't correct at all. need to find a solution that's consistent with the base implementation.
							Ballistica tangent = new Ballistica(hero.pos, wallPos, Ballistica.STOP_TARGET);
							int ideal = tangent.path.get(tangent.dist + 1);
							int finalDir = 0;
							float best = 0;
							for (int dir : PathFinder.NEIGHBOURS8) {
								int pos = dir + wallPos;
								if (!Dungeon.level.passable[pos] || Actor.findChar(pos) != null || placed.contains(pos)) continue;
								if (pos == ideal) {
									finalDir = dir;
									break;
								}
								float d = Dungeon.level.trueDistance(hero.pos, pos);
								if (d > best) {
									finalDir = dir;
									best = d;
								}
							}
							if (finalDir != 0 && Actor.findChar(wallPos+finalDir) != null) {
								placed.add(wallPos+finalDir);
							}
							placeWall(wallPos, tangent.path.get(tangent.dist + 1) - wallPos);
						}

						if (!SpellEmpower.isActive()) {
							destroy();
						} else {
							setTargetedCell(cell);
						}
					}
				}

				@Override
				public String prompt() {
					String chargeUse;
					try {
						toPlace = 1;
						chargeUse = Messages.decimalFormat("#.##", chargeUse(Dungeon.hero));
					} finally {
						toPlace = 0;
					}
					return "Select two cells to draw a wall between them for _" + chargeUse + "_ charges per tile placed." +
							"\nNote you cannot place two walls adjacent to each other." +
							"\nSelect a wall to dispel it, or targeted cell to cancel targeting.";
				}
			});
			return;
		}
		if (Dungeon.level.blobs.get(LightWall.class) != null
				&& Dungeon.level.blobs.get(LightWall.class).volume > 0){
			Dungeon.level.blobs.get(LightWall.class).fullyClear();
			GLog.i(Messages.get(this, "early_end"));
			return;
		}
		super.onCast(tome, hero);
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		if (target == hero.pos){
			GLog.w(Messages.get(this, "invalid_target"));
			return;
		}

		int leapPos = hero.pos;
		final boolean force;
		if (!Dungeon.level.adjacent(target, leapPos) && hero.hasTalent(Talent.TRIAGE)) {
			force = true;
			leapPos = Combo.Leap.findLeapPos(hero, target, hero.pointsInTalent(Talent.TRIAGE));
			if (leapPos == -1) {
				Combo.Leap.onInvalid(Messages.get(this, "invalid_target"));
				return;
			}
		} else {
			force = false;
		}
		Combo.Leap.execute(hero, leapPos, () -> doCast(tome, hero, target, force));
	}

	private void doCast(HolyTome tome, Hero hero, int target, boolean force) {
		int closest = hero.pos;
		int closestIdx = -1;

		for (int i = 0; i < PathFinder.CIRCLE8.length; i++){
			int ofs = PathFinder.CIRCLE8[i];
			if (Dungeon.level.trueDistance(target, hero.pos+ofs) < Dungeon.level.trueDistance(target, closest)){
				closest = hero.pos+ofs;
				closestIdx = i;
			}
		}

		int leftDirX = 0;
		int leftDirY = 0;

		int rightDirX = 0;
		int rightDirY = 0;

		int steps = Dungeon.hero.pointsInTalent(Talent.WALL_OF_LIGHT);

		switch (closestIdx){
			case 0: //top left
				leftDirX = -1;
				leftDirY = 1;
				rightDirX = 1;
				rightDirY = -1;
				break;
			case 1: //top
				leftDirX = -1;
				rightDirX = 1;
				leftDirY = rightDirY = 0;
				break;
			case 2: //top right (left and right DIR are purposefully inverted)
				leftDirX = 1;
				leftDirY = 1;
				rightDirX = -1;
				rightDirY = -1;
				break;
			case 3: //right
				leftDirY = -1;
				rightDirY = 1;
				leftDirX = rightDirX = 0;
				break;
			case 4: //bottom right (left and right DIR are purposefully inverted)
				leftDirX = 1;
				leftDirY = -1;
				rightDirX = -1;
				rightDirY = 1;
				break;
			case 5: //bottom
				leftDirX = 1;
				rightDirX = -1;
				leftDirY = rightDirY = 0;
				break;
			case 6: //bottom left
				leftDirX = -1;
				leftDirY = -1;
				rightDirX = 1;
				rightDirY = 1;
				break;
			case 7: //left
				leftDirY = -1;
				rightDirY = 1;
				leftDirX = rightDirX = 0;
				break;
		}

		if (Dungeon.level.blobs.get(LightWall.class) != null){
			Dungeon.level.blobs.get(LightWall.class).fullyClear();
		}

		boolean placedWall = false;

		int knockBackDir = PathFinder.CIRCLE8[closestIdx];

		//if all 3 tiles infront of Paladin are blocked, assume cast was in error and cancel
		if (!force && Dungeon.level.solid[closest]
				&& Dungeon.level.solid[hero.pos + PathFinder.CIRCLE8[(closestIdx+1)%8]]
				&& Dungeon.level.solid[hero.pos + PathFinder.CIRCLE8[(closestIdx+7)%8]]){
			GLog.w(Messages.get(this, "invalid_target"));
			return;
		}

		//process early so that cost is calculated before walls are added
		onSpellCast(tome, hero);

		placeWall(closest, knockBackDir);

		int leftPos = closest;
		int rightPos = closest;

		//iterate to the left and right, placing walls as we go
		for (int i = 0; i < steps; i++) {
			if (leftDirY != 0) {
				leftPos += leftDirY * Dungeon.level.width();
				if (!Dungeon.level.insideMap(leftPos)){
					break;
				}
				placeWall(leftPos, knockBackDir);
			}
			if (leftDirX != 0) {
				leftPos += leftDirX;
				if (!Dungeon.level.insideMap(leftPos)){
					break;
				}
				placeWall(leftPos, knockBackDir);
			}
		}
		for (int i = 0; i < steps; i++) {
			if (rightDirX != 0) {
				rightPos += rightDirX;
				if (!Dungeon.level.insideMap(rightPos)){
					break;
				}
				placeWall(rightPos, knockBackDir);
			}
			if (rightDirY != 0) {
				rightPos += rightDirY * Dungeon.level.width();
				if (!Dungeon.level.insideMap(rightPos)){
					break;
				}
				placeWall(rightPos, knockBackDir);
			}
		}

		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);

		hero.sprite.zap(closest);
		Dungeon.hero.spendAndNext(1f);
	}

	private void placeWall( int pos, int knockbackDIR){
		if (!Dungeon.level.solid[pos]) {
			GameScene.add(Blob.seed(pos, 20, LightWall.class));

			Char ch = Actor.findChar(pos);
			if (ch != null && ch.alignment == Char.Alignment.ENEMY){
				WandOfBlastWave.throwChar(ch, new Ballistica(pos, pos+knockbackDIR, Ballistica.PROJECTILE), 1, false, false, WallOfLight.INSTANCE);
				Buff.affect(ch, Paralysis.class, ch.cooldown());
			}
		}
	}

	public static class LightWall extends Blob {

		@Override
		protected void evolve() {

			int cell;

			Level l = Dungeon.level;
			for (int i = area.left; i < area.right; i++){
				for (int j = area.top; j < area.bottom; j++){
					cell = i + j*l.width();
					off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;

					volume += off[cell];

					l.solid[cell] = off[cell] > 0 || (Terrain.flags[l.map[cell]] & Terrain.SOLID) != 0;
					l.passable[cell] = off[cell] == 0 && (Terrain.flags[l.map[cell]] & Terrain.PASSABLE) != 0;
					l.avoid[cell] = off[cell] == 0 && (Terrain.flags[l.map[cell]] & Terrain.AVOID) != 0;
				}
			}
		}

		@Override
		public void seed(Level level, int cell, int amount) {
			super.seed(level, cell, amount);
			level.solid[cell] = cur[cell] > 0 || (Terrain.flags[level.map[cell]] & Terrain.SOLID) != 0;
			level.passable[cell] = cur[cell] == 0 && (Terrain.flags[level.map[cell]] & Terrain.PASSABLE) != 0;
			level.avoid[cell] = cur[cell] == 0 && (Terrain.flags[level.map[cell]] & Terrain.AVOID) != 0;
		}

		public boolean clearConnected(int cell) {
			if (volume == 0 || cur[cell] == 0) return false;
			clear(cell);
			for (int i : PathFinder.NEIGHBOURS8) clearConnected(cell + i);
			return true;
		}

		@Override
		public void clear(int cell) {
			super.clear(cell);
			if (cur == null) return;
			Level l = Dungeon.level;
			l.solid[cell] = cur[cell] > 0 || (Terrain.flags[l.map[cell]] & Terrain.SOLID) != 0;
			l.passable[cell] = cur[cell] == 0 && (Terrain.flags[l.map[cell]] & Terrain.PASSABLE) != 0;
			l.avoid[cell] = cur[cell] == 0 && (Terrain.flags[l.map[cell]] & Terrain.AVOID) != 0;
		}

		@Override
		public void fullyClear() {
			super.fullyClear();
			Dungeon.level.buildFlagMaps();
		}

		@Override
		public void onBuildFlagMaps(Level l) {
			if (volume > 0){
				for (int i=0; i < l.length(); i++) {
					l.solid[i] = l.solid[i] || cur[i] > 0;
					l.passable[i] = l.passable[i] && cur[i] == 0;
					l.avoid[i] = l.avoid[i] && cur[i] == 0;
				}
			}
		}

		@Override
		public void use(BlobEmitter emitter) {
			super.use( emitter );
			emitter.pour( MagicMissile.WhiteParticle.WALL, 0.02f );
		}

		@Override
		public String tileDesc() {
			return Messages.get(this, "desc");
		}

	}

}
