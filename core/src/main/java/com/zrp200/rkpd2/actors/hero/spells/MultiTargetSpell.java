/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * RKPD2
 * Copyright (C) 2020-2025 Zachary Perlmutter
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

import static com.zrp200.rkpd2.Dungeon.level;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;

public abstract class MultiTargetSpell extends TargetedClericSpell {

    protected int castsLeft, totalCasts;

    protected boolean announced = false;

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        if (isMultiTarget()) {
            ArrayList<Integer> targets = new ArrayList<>();
            for (Char ch : level.mobs) {
                if (level.heroFOV[ch.pos] && ch.alignment == Char.Alignment.ENEMY) {
                    int aim = QuickSlotButton.autoAim(ch, this);
                    if (aim == ch.pos) {
                        targets.add(aim);
                    }
                }
            }
            if (targets.isEmpty()) {
                GLog.w(Messages.get(this, "no_targets"));
            } else {
                totalCasts = castsLeft = targets.size();
                if (announced) hero.sprite.showStatus(CharSprite.POSITIVE, name());
                for (int target : targets) onTargetSelected(tome, hero, target);
            }
            return;
        }
        super.onCast(tome, hero);
    }

    @Override
    public boolean usesTargeting() { return !isMultiTarget() && super.usesTargeting(); }

    @Override
    public int targetingFlags() {
        int targetingFlags = baseTargetingFlags();
        if (isMultiTarget()) {
            // projects through characters
            targetingFlags |= Ballistica.STOP_TARGET;
            targetingFlags &= ~Ballistica.STOP_CHARS;
        }
        return targetingFlags;
    }

    protected boolean isMultiTarget() { return totalCasts > 0 || SpellEmpower.isActive(); }

    protected int baseTargetingFlags() { return super.targetingFlags(); }

    @Override
    public synchronized void onSpellCast(HolyTome tome, Hero hero) {
        if (--castsLeft > 0) return;
        super.onSpellCast(tome, hero);
        onSpellComplete(tome, hero);
        totalCasts = castsLeft = 0;
    }

    public void onSpellComplete(HolyTome tome, Hero hero) {/* do nothing */}
}
