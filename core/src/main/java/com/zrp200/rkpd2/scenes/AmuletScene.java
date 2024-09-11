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

package com.zrp200.rkpd2.scenes;

import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Chrome;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.GamesInProgress;
import com.zrp200.rkpd2.effects.BadgeBanner;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.Amulet;
import com.zrp200.rkpd2.items.quest.Chaosstone;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.StyledButton;

public class AmuletScene extends PixelScene {

	private static final int WIDTH			= 120;
	private static final int BTN_HEIGHT		= 20;
	private static final float SMALL_GAP	= 2;
	private static final float LARGE_GAP	= 8;

	public static boolean noText = false;
	public static boolean gauntlet = false;

	private Image amulet;

	{
		inGameScene = true;
	}
	StyledButton btnExit = null;
	StyledButton btnStay = null;

	@Override
	public void create() {
		super.create();

		RenderedTextBlock text = null;
		if (!noText) {
			text = renderTextBlock( Messages.get(this, "text"), 8 );
			text.maxWidth( PixelScene.landscape() ? 2*WIDTH-4 : WIDTH);
		}

		amulet = new Image( Assets.Sprites.AMULET );
		add( amulet );

		btnExit = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "exit") ) {
			@Override
			protected void onClick() {
				Class clazz = Amulet.class;
				if (Dungeon.hero.belongings.getSimilar(new Chaosstone()) != null) {
					clazz = Chaosstone.class;
				}
				Dungeon.win(clazz);
				Dungeon.deleteGame( GamesInProgress.curSlot, true );
				btnExit.enable(false);
				btnStay.enable(false);

				AmuletScene.this.add(new Delayer(0.1f){
					@Override
					protected void onComplete() {
						if (BadgeBanner.isShowingBadges()){
							AmuletScene.this.add(new Delayer(3f){
								@Override
								protected void onComplete() {
									Game.switchScene( RankingsScene.class );
								}
							});
						} else {
							Game.switchScene( RankingsScene.class );
						}
					}
				});
				Music.INSTANCE.playTracks(
						new String[]{Assets.Music.THEME_2, Assets.Music.THEME_1},
						new float[]{1, 1},
						false);
			}
		};
		btnExit.icon(new ItemSprite(ItemSpriteSheet.AMULET));
		btnExit.setSize( WIDTH, BTN_HEIGHT );
		add( btnExit );

		btnStay = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "stay") ) {
			@Override
			protected void onClick() {
				onBackPressed();
				btnExit.enable(false);
				btnStay.enable(false);
			}
		};
		btnStay.icon(Icons.CLOSE.get());
		btnStay.setSize( WIDTH, BTN_HEIGHT );
		add( btnStay );

		StyledButton btnMastery = new StyledButton(Chrome.Type.GREY_BUTTON_TR,  Messages.get(this, "mastery") ) {
			@Override
			protected void onClick() {
				InterlevelScene.mode = InterlevelScene.Mode.ABYSS;
				Game.switchScene(InterlevelScene.class);
			}
		};
		btnMastery.icon(Icons.CHALLENGE_ON.get());
		btnMastery.setSize( WIDTH, BTN_HEIGHT );
		add( btnMastery );

		float height;
		if (noText) {
			height = amulet.height + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height();

			amulet.x = (Camera.main.width - amulet.width) / 2;
			amulet.y = (Camera.main.height - height) / 2;
			align(amulet);

			btnExit.setPos( (Camera.main.width - btnExit.width()) / 2, amulet.y + amulet.height + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
			btnMastery.setPos(btnExit.left(), btnStay.bottom() + SMALL_GAP);

		} else {
			height = amulet.height + LARGE_GAP + text.height() + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height();

			amulet.x = (Camera.main.width - amulet.width) / 2;
			amulet.y = (Camera.main.height - height) / 2;
			align(amulet);

			text.setPos((Camera.main.width - text.width()) / 2, amulet.y + amulet.height + LARGE_GAP);
			align(text);
add(text);

			btnExit.setPos( (Camera.main.width - btnExit.width()) / 2, text.top() + text.height() + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
			btnMastery.setPos(btnExit.left(), btnStay.bottom() + SMALL_GAP);
		}

		new Flare( 8, 48 ).color( 0xFFDDBB, true ).show( amulet, 0 ).angularSpeed = +30;

		fadeIn();
	}

	@Override
	protected void onBackPressed() {
		if (btnExit.isActive()) {
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
		}
	}

	private float timer = 0;

	@Override
	public void update() {
		super.update();

		if ((timer -= Game.elapsed) < 0) {
			timer = Random.Float( 0.5f, 5f );

			Speck star = (Speck)recycle( Speck.class );
			star.reset( 0, amulet.x + 10.5f, amulet.y + 5.5f, Speck.DISCOVER );
			add( star );
		}
	}
}
