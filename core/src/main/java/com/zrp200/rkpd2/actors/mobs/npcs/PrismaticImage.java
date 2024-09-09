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

package com.zrp200.rkpd2.actors.mobs.npcs;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.PrismaticGuard;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.KindOfWeapon;
import com.zrp200.rkpd2.items.armor.glyphs.AntiMagic;
import com.zrp200.rkpd2.items.armor.glyphs.Brimstone;
import com.zrp200.rkpd2.levels.features.Chasm;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.PrismaticSprite;

public class PrismaticImage extends AbstractMirrorImage {
	
	{
		spriteClass = PrismaticSprite.class;
		
		HP = HT = 10;

		intelligentAlly = true;
		
		WANDERING = new Wandering();
	}
	
	private int deathTimer = -1;
	
	@Override
	protected boolean act() {
		
		if (!isAlive()){
			if (hero != null && hero.pointsInTalent(Talent.HELPER_TO_HERO) > 2){
				PrismaticGuard prismaticGuard = Buff.affect(hero, PrismaticGuard.class);
				prismaticGuard.set( PrismaticGuard.maxHP(hero) / 2 );
				destroy();
				CellEmitter.get(pos).start( Speck.factory(Speck.LIGHT), 0.2f, 3 );
				sprite.die();
				Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
				Talent.Cooldown.affectHero(Talent.HelperToHeroReviveCooldown.class);
				return true;
			}
			if (hero != null && hero.pointsInTalent(Talent.HELPER_TO_HERO) < 2)
				deathTimer--;
			
			if (deathTimer > 0) {
				sprite.alpha((deathTimer + 3) / 8f);
				spend(TICK);
			} else {
				destroy();
				sprite.die();
			}
			return true;
		}
		
		if (deathTimer != -1){
			if (paralysed == 0) sprite.remove(CharSprite.State.PARALYSED);
			deathTimer = -1;
			sprite.resetColor();
		}
		return super.act();
	}
	
	@Override
	public void die(Object cause) {
		if (deathTimer == -1) {
			if (cause == Chasm.class){
				super.die( cause );
			} else {
				deathTimer = 5;
				sprite.add(CharSprite.State.PARALYSED);
			}
		}
	}

	@Override
	public boolean isActive() {
		return isAlive() || deathTimer > 0;
	}

	private static final String HEROID	= "hero_id";
	private static final String TIMER	= "timer";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( TIMER, deathTimer );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		deathTimer = bundle.getInt( TIMER );
	}
	
	public void duplicate( Hero hero, int HP ) {
		duplicate(hero);
		this.HP = HP;
		HT = PrismaticGuard.maxHP( hero );
	}
	
	@Override
	public int damageRoll() {
		int i;
		if (hero != null) {
			i = Random.NormalIntRange(2 + hero.lvl / 4, 4 + hero.lvl / 2);
		} else {
			i = Random.NormalIntRange(2, 4);
			if (hero.pointsInTalent(Talent.HELPER_TO_HERO) > 1 && hero.belongings.secondWep() != null){
				i += Math.round(hero.belongings.secondWep().damageRoll(hero) * (0.25f * hero.pointsInTalent(Talent.HELPER_TO_HERO)));
			}
		}
		i *= 1f + 0.2f*hero.pointsInTalent(Talent.SPECTRE_ALLIES);
		return i;
	}

	@Override
	protected int heroEvasion() {
		// armor boosts contribute to evasion
		return hero.belongings.armor() != null ?
				(int)hero.belongings.armor().evasionFactor(this, super.heroEvasion())
				: super.heroEvasion();
	}

	@Override
	public int drRoll() {
		int dr = super.drRoll();
		if (hero != null) dr += hero.drRoll();
		return dr;
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		int dmg = super.attackProc(enemy, damage);
		if (hero != null && hero.hasTalent(Talent.HELPER_TO_HERO) && hero.belongings.secondWep() != null){
			dmg = hero.belongings.secondWep().proc(this, enemy, dmg);
		}
		return dmg;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (hero != null && hero.belongings.armor() != null){
			damage = hero.belongings.armor().proc( enemy, this, damage );
		}
		return super.defenseProc(enemy, damage);
	}
	
	@Override
	public void damage(int dmg, Object src) {
		
		//TODO improve this when I have proper damage source logic
		if (hero != null && hero.belongings.armor() != null && hero.belongings.armor().hasGlyph(AntiMagic.class, this)
				&& AntiMagic.RESISTS.contains(src.getClass())){
			dmg -= AntiMagic.drRoll(hero, hero.belongings.armor(). glyphEffectLevel(hero));
			dmg = Math.max(dmg, 0);
		}

		
		super.damage(dmg, src);
	}
	
	@Override
	public float speed() {
		if (hero != null && hero.belongings.armor() != null){
			return hero.belongings.armor().speedFactor(this, super.speed());
		}
		return super.speed();
	}

	@Override
	public boolean canAttack(Char enemy) {
		return super.canAttack(enemy) ||
				(hero.belongings.weapon() != null && hero.belongings.weapon().canReach(this, enemy.pos)) ||
				(hero.hasTalent(Talent.SPECTRE_ALLIES) && KindOfWeapon.canReach(this, enemy.pos, 1 + hero.pointsInTalent(Talent.SPECTRE_ALLIES)));
	}
	@Override
	public boolean isImmune(Class effect) {
		if (effect == Burning.class
				&& hero != null
				&& hero.belongings.armor() != null
				&& hero.belongings.armor().hasGlyph(Brimstone.class, this)){
			return true;
		}
		return super.isImmune(effect);
	}

	private class Wandering extends Mob.Wandering{
		
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV){
				Buff.affect(hero, PrismaticGuard.class).set( HP );
				destroy();
				CellEmitter.get(pos).start( Speck.factory(Speck.LIGHT), 0.2f, 3 );
				sprite.die();
				Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
				return true;
			} else {
				return super.act(enemyInFOV, justAlerted);
			}
		}
		
	}
	
}
