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

package com.zrp200.rkpd2.windows;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.SPDSettings;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.ui.CheckBox;
import com.zrp200.rkpd2.ui.IconButton;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.ScrollPane;
import com.zrp200.rkpd2.ui.Window;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndChallenges extends Window {

	private final int WIDTH = 120;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;

	private boolean editable;
	private ArrayList<IconButton> infos = new ArrayList<>();
	private ArrayList<ConduitBox> boxes;

	public WndChallenges( int checked, boolean editable ) {

		super();

		this.editable = editable;
		OrderedMap<String, Integer> challenges = Challenges.availableChallenges();
		if (!this.editable){
			OrderedMap<String, Integer> activeChallenges = new OrderedMap<>();
			for (ObjectMap.Entry<String, Integer> chal : challenges.entries()){
				if ((checked & chal.value) != 0) activeChallenges.put(chal.key, chal.value);
			}
			challenges = new OrderedMap<>(activeChallenges);
		}
		int HEIGHT = Math.min((challenges.size + 1) * (BTN_HEIGHT + GAP),
				(int) (PixelScene.uiCamera.height * 0.9));
		resize(WIDTH, HEIGHT);

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		boxes = new ArrayList<>();

		float pos = 2;
		int i = 0;

		OrderedMap<String, Integer> finalChallenges = challenges;
		ScrollPane pane = new ScrollPane(new Component()) {
			@Override
			public void onClick(float x, float y) {
				int size = boxes.size();
				if (editable) {
					for (int i = 0; i < size; i++) {
						if (boxes.get(i).onClick(x, y)) break;
					}
				}
				size = infos.size();
				for (int i = 0; i < size; i++) {
					if (infos.get(i).inside(x, y)) {
						String challenge = finalChallenges.keys().toArray().get(i);

						ShatteredPixelDungeon.scene().add(
								new WndTitledMessage(Icons.get(Icons.CHALLENGE_ON),
										Messages.titleCase(Messages.get(Challenges.class, challenge)),
										Messages.get(Challenges.class, challenge+"_desc"))
						);

						break;
					}
				}
			}
		};
		add(pane);
		pane.setRect(0, title.bottom()+2, WIDTH, HEIGHT - title.bottom() - 2);
		Component content = pane.content();

		for (ObjectMap.Entry<String, Integer> chal : challenges.entries()) {

			final String challenge = chal.key;
			String chaltitle = Messages.titleCase(Messages.get(Challenges.class, challenge));
			if (!Challenges.defaultChals.keys().toArray().contains(challenge, false)){
				chaltitle = "_" + chaltitle + "_";
			}
			
			ConduitBox cb = new ConduitBox( chaltitle );
			cb.checked( (checked & chal.value) != 0 );
			cb.active = editable;
			if (chal.value > Challenges.EVIL_MODE){
				cb.textColor(0x5c5c5c);
			}
			if (chal.value == Challenges.EVIL_MODE){
				cb.textColor(0xe36e00);
			}
			if (chal.value >= Challenges.NO_TALENTS){
				cb.textColor(0x292b30);
			}

			if (++i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

			content.add( cb );
			boxes.add( cb );

			IconButton info = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void layout() {
					super.layout();
					hotArea.y = -5000;
				}
			};
			info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
			content.add(info);
			infos.add(info);
			
			pos = cb.bottom();
		}

		content.setSize(WIDTH, pos);
	}

	@Override
	public void onBackPressed() {

		if (editable) {
			int value = 0;
			for (int i=0; i < boxes.size(); i++) {
				if (boxes.get( i ).checked()) {
					value |= Challenges.availableChallenges().values().toArray().get(i);
				}
			}
			SPDSettings.challenges( value );
		}

		super.onBackPressed();
	}

	public class ConduitBox extends CheckBox{

		public ConduitBox(String label) {
			super(label);
		}

		@Override
		protected void onClick() {
			super.onClick();
		}

		protected boolean onClick(float x, float y) {
			if (!inside(x, y) || !editable) return false;
			Sample.INSTANCE.play(Assets.Sounds.CLICK);
			onClick();
			return true;
		}

		@Override
		protected void layout() {
			super.layout();
			hotArea.width = hotArea.height = 0;
		}
	}
}