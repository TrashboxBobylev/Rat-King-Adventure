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
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.GodSlayerFire;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.particles.FlameParticle;
import com.zrp200.rkpd2.journal.Catalog;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Torch extends Item {

	public static final String AC_LIGHT	= "LIGHT";
	
	public static final float TIME_TO_LIGHT = 1;
	
	{
		image = ItemSpriteSheet.TORCH;
		
		stackable = true;
		
		defaultAction = AC_LIGHT;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_LIGHT );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );
		
		if (action.equals( AC_LIGHT )) {
			
			hero.spend( TIME_TO_LIGHT );
			hero.busy();
			
			hero.sprite.operate( hero.pos );
			
			detach( hero.belongings.backpack );
			Catalog.countUse(getClass());

			Buff.affect(hero, Light.class, Light.DURATION);
			Sample.INSTANCE.play(Assets.Sounds.BURNING);
			
			Emitter emitter = hero.sprite.centerEmitter();
			emitter.start( FlameParticle.FACTORY, 0.2f, 3 );
			
		}
	}

	@Override
	protected void onThrow(int cell) {
		if (MonkEnergy.isFeelingEmpowered(Level.Feeling.DARK)){
			for (int i : PathFinder.NEIGHBOURS4){
				GameScene.add(Blob.seed(cell + i, 2, GodSlayerFire.class));
			}
			GameScene.add(Blob.seed(cell, 3, GodSlayerFire.class));
			Sample.INSTANCE.play( Assets.Sounds.BURNING );
		} else {
			super.onThrow(cell);
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int value() {
		return 8 * quantity;
	}

}
