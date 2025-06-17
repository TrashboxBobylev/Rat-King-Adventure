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

package com.zrp200.rkpd2.ui.changelist;

import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.ChangesScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.BuffIcon;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.TalentIcon;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import static com.zrp200.rkpd2.sprites.HeroSprite.avatar;

//not actually a button, but functions as one.
public class ChangeButton extends Component {

	protected Image icon;
	protected String title;
	protected String[] messages;

	public ChangeButton( Image icon, String title, String... messages){
		super();

		this.icon = icon;
		add(this.icon);

		this.title = Messages.titleCase(title);

		for (int i=0; i < messages.length; i++) messages[i] = messages[i].trim();
		this.messages = messages;

		layout();
	}

	public ChangeButton(Item item, String message ){
		this( new ItemSprite(item), item.name(), message);
	}

	public ChangeButton(HeroClass heroClass, String message) {
		this( avatar(heroClass, 6), heroClass.title(), message );
	}

	public ChangeButton(Talent talent, String title, String message) {
		this( new Image( new TalentIcon(talent) ), title, message);
	}
	public ChangeButton(Talent talent, String message) {
		this( talent, talent.title(), message);
	}

	public ChangeButton(HeroSubClass subClass, String message) {
		this( new Image( new HeroIcon(subClass) ), subClass.title(), message);
	}

	public ChangeButton(ArmorAbility ability, String message) {
		this(ability, ability.name(), message);
	}
	public ChangeButton(ArmorAbility ability, String title, String message) {
		this( new Image( new HeroIcon(ability) ), title, message);
	}

	public ChangeButton(Buff buff, String message) {
		this( new Image( new BuffIcon(buff,true) ), buff.toString(), message);
	}

	protected void onClick() {
		Image image = Reflection.newInstance(icon.getClass());
		image.copy(icon);
		ChangesScene.showChangeInfo(image, title, messages);
	}

	@Override
	protected void layout() {
		super.layout();

		icon.x = x + (width - icon.width()) / 2f;
		icon.y = y + (height - icon.height()) / 2f;
		PixelScene.align(icon);
	}
}
