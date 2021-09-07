package com.zrp200.rkpd2.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.hero.Talent;

public class TalentIcon extends Image {

	private static TextureFilm film;
	private static final int SIZE = 16;

	public TalentIcon(Talent talent){
		this(talent.icon());
	}

	public TalentIcon(int icon){
		super( Assets.Interfaces.TALENT_ICONS );

		if (film == null) film = new TextureFilm(texture, SIZE, SIZE);

		frame(film.get(icon));
	}

}
