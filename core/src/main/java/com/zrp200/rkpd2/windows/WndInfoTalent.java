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

import com.watabou.utils.Callback;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.RedButton;
import com.zrp200.rkpd2.ui.TalentIcon;

import static com.zrp200.rkpd2.Dungeon.hero;

public class WndInfoTalent extends WndTitledMessage {

	public WndInfoTalent(Talent talent, int points, TalentButtonCallback buttonCallback){
		super(
		    new TalentIcon( talent ),
		    Messages.titleCase(talent.title() + (points > 0 ? " +" + points: "")),
		    talent.desc((buttonCallback != null && buttonCallback.metamorphDesc()) ||
					hero != null && (hero.metamorphedTalents.containsValue(talent)
							|| hero.heroClass.is(HeroClass.RAT_KING)
								// Rat King currently doesn't have duelist implemented
								// and metamorph effects of his classes do not apply to hit
								&& !talent.isClassTalent(HeroClass.DUELIST)
								&& !talent.isClassTalent(HeroClass.RAT_KING))),
            WIDTH_MIN );

		if (buttonCallback != null) {
			addToBottom(new RedButton( buttonCallback.prompt() ) {
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					buttonCallback.call();
				}
				{
					icon(Icons.get(Icons.TALENT));
					setHeight(18);
				}
			}, 2*GAP, 1);
		}

	}

	public interface TalentButtonCallback extends Callback {
		String prompt();

		default boolean metamorphDesc(){
		    return false;
		}
	}

}
