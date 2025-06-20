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

import com.zrp200.rkpd2.Chrome;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.SPDSettings;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.spells.ClericSpell;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.IconButton;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.RightClickMenu;
import com.zrp200.rkpd2.ui.Window;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PointF;

import java.util.ArrayList;

public class WndClericSpells extends Window {

	protected static final int WIDTH    = 120;

	public static int BTN_SIZE = 20;

	private static final int SPELLS_PER_ROW = 5;

	public WndClericSpells(HolyTome tome, Hero cleric, boolean info){

		IconTitle title;
		if (!info){
			title = new IconTitle(new ItemSprite(tome), Messages.titleCase(Messages.get(this, "cast_title")));
		} else {
			title = new IconTitle(Icons.INFO.get(), Messages.titleCase(Messages.get(this, "info_title")));
		}

		title.setRect(0, 0, WIDTH, 0);
		add(title);

		IconButton btnInfo = new IconButton(info ? new ItemSprite(tome) : Icons.INFO.get()){
			@Override
			protected void onClick() {
				GameScene.show(new WndClericSpells(tome, cleric, !info));
				hide();
			}
		};
		btnInfo.setRect(WIDTH-16, 0, 16, 16);
		add(btnInfo);

		RenderedTextBlock msg;
		if (info){
			msg = PixelScene.renderTextBlock( Messages.get( this, "info_desc"), 6);
		} else if (DeviceCompat.isDesktop()){
			msg = PixelScene.renderTextBlock( Messages.get( this, "cast_desc_desktop"), 6);
		} else {
			msg = PixelScene.renderTextBlock( Messages.get( this, "cast_desc_mobile"), 6);
		}
		msg.maxWidth(WIDTH);
		msg.setPos(0, title.bottom()+4);
		add(msg);

		int top = (int)msg.bottom()+4;

		for (int i = 1; i <= Talent.MAX_TALENT_TIERS; i++) {

			ArrayList<ClericSpell> spells = ClericSpell.getSpellList(cleric, i);

			if (!spells.isEmpty() && i != 1){
				top += BTN_SIZE + 2;
				ColorBlock sep = new ColorBlock(WIDTH, 1, 0xFF000000);
				sep.y = top;
				add(sep);
				top += 3;
			}

			ArrayList<IconButton> spellBtns = new ArrayList<>();

			for (ClericSpell spell : spells) {
				IconButton spellBtn = new SpellButton(spell, tome, info);
				add(spellBtn);
				spellBtns.add(spellBtn);
			}

			int left = 0;

			boolean firstRow = true;
			int leftThisRow = 1;
			int remaining = spellBtns.size();
			for (IconButton btn : spellBtns) {
				if (--leftThisRow == 0) {
					// don't bother splitting them up, just put the new one below
					remaining -= leftThisRow = Math.min(SPELLS_PER_ROW, remaining);
					left = 2 + (WIDTH - leftThisRow * (BTN_SIZE + 4)) / 2;
					if (firstRow) firstRow = false; else top += BTN_SIZE + 2;
				}
				btn.setRect(left, top, BTN_SIZE, BTN_SIZE);
				left += BTN_SIZE + 4;
			}

		}

		resize(WIDTH, top + BTN_SIZE);

		//if we are on mobile, offset the window down to just above the toolbar
		if (SPDSettings.interfaceSize() != 2){
			offset(0, (int) (GameScene.uiCamera.height/2 - 30 - height/2));
		}

	}

	public class SpellButton extends IconButton {

		ClericSpell spell;
		HolyTome tome;
		boolean info;

		NinePatch bg;

		public SpellButton(ClericSpell spell, HolyTome tome, boolean info){
			super(new HeroIcon(spell));

			this.spell = spell;
			this.tome = tome;
			this.info = info;

			if (!tome.canCast(Dungeon.hero, spell)){
				icon.alpha( 0.3f );
			}

			bg = Chrome.get(Chrome.Type.TOAST);
			addToBack(bg);
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			if (!tome.canCast(Dungeon.hero, spell)){
				icon.alpha( 0.3f );
			}
		}

		@Override
		protected void layout() {
			super.layout();

			if (bg != null) {
				bg.size(width, height);
				bg.x = x;
				bg.y = y;
			}
		}

		@Override
		protected void onClick() {
			if (info){
				GameScene.show(new WndTitledMessage(new HeroIcon(spell), Messages.titleCase(spell.name()), spell.desc()));
			} else {
				hide();


				if(!tome.canCast(Dungeon.hero, spell)){
					GLog.w(Messages.get(HolyTome.class, "no_spell"));
				} else {
					spell.onCast(tome, Dungeon.hero);

					if (spell.usesTargeting() && Dungeon.quickslot.contains(tome)){
						tome.targetingSpell = spell;
						QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(tome));
					}
				}

			}
		}

		@Override
		protected boolean onLongClick() {
			hide();
			tome.setQuickSpell(spell);
			return true;
		}

		@Override
		protected void onRightClick() {
			super.onRightClick();
			RightClickMenu r = new RightClickMenu(new Image(icon),
					Messages.titleCase(spell.name()),
					Messages.get(WndClericSpells.class, "cast"),
					Messages.get(WndClericSpells.class, "info"),
					Messages.get(WndClericSpells.class, "quick_cast")){
				@Override
				public void onSelect(int index) {
					switch (index){
						default:
							//do nothing
							break;
						case 0:
							hide();
							if(!tome.canCast(Dungeon.hero, spell)){
								GLog.w(Messages.get(HolyTome.class, "no_spell"));
							} else {
								spell.onCast(tome, Dungeon.hero);

								if (spell.usesTargeting() && Dungeon.quickslot.contains(tome)){
									tome.targetingSpell = spell;
									QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(tome));
								}
							}
							break;
						case 1:
							GameScene.show(new WndTitledMessage(new HeroIcon(spell), Messages.titleCase(spell.name()), spell.desc()));
							break;
						case 2:
							hide();
							tome.setQuickSpell(spell);
							break;
					}
				}
			};
			parent.addToFront(r);
			r.camera = camera();
			PointF mousePos = PointerEvent.currentHoverPos();
			mousePos = camera.screenToCamera((int)mousePos.x, (int)mousePos.y);
			r.setPos(mousePos.x-3, mousePos.y-3);
		}

		@Override
		protected String hoverText() {
			return "_" + Messages.titleCase(spell.name()) + "_\n" + spell.shortDesc();
		}
	}

}
