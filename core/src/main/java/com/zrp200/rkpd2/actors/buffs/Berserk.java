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

package com.zrp200.rkpd2.actors.buffs;

import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.items.BrokenSeal.WarriorShield;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;

public class Berserk extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
	}

	private enum State{
		NORMAL, BERSERK, RECOVERING
	}
	private State state = State.NORMAL;

	private static final float LEVEL_RECOVER_START = 4f;
	private float getLevelRecoverStart() {
		return LEVEL_RECOVER_START -
				// -1 level for berserker proper.
				((Hero)target).shiftedPoints(Talent.DEATHLESS_FURY,Talent.RK_BERSERKER);
	}
	private float levelRecovery;

	private static final int TURN_RECOVERY_START = 100;
	private int turnRecovery;

	public int powerLossBuffer = 0;
	private float power = 0;

	private static final String STATE = "state";
	private static final String LEVEL_RECOVERY = "levelrecovery";
	private static final String TURN_RECOVERY = "turn_recovery";
	private static final String POWER = "power";
	private static final String POWER_BUFFER = "power_buffer";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STATE, state);
		bundle.put(POWER, power);
		bundle.put(POWER_BUFFER, powerLossBuffer);
		bundle.put(LEVEL_RECOVERY, levelRecovery);
		bundle.put(TURN_RECOVERY, turnRecovery);
	}

	@Override
	public boolean usable() {
		return power >= 1f && state == State.NORMAL;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		state = bundle.getEnum(STATE, State.class);
		power = bundle.getFloat(POWER);
		powerLossBuffer = bundle.getInt(POWER_BUFFER);
		levelRecovery = bundle.getFloat(LEVEL_RECOVERY);
		turnRecovery = bundle.getInt(TURN_RECOVERY);

		ActionIndicator.setAction(this);
	}

	// this basically covers all of berserker's "buffed" talents.
	private boolean berserker() {
		return target instanceof Hero && ((Hero)target).subClass.isExact(HeroSubClass.BERSERKER);
	}

	@Override
	public boolean act() {
		if (state == State.BERSERK){
			ShieldBuff buff = target.buff(WarriorShield.class);
			if (target.shielding() > 0) {
				//lose 2.5% of shielding per turn, but no less than 1
				int dmg = (int)Math.ceil(target.shielding() * 0.025f);
				if (buff != null && buff.shielding() > 0) {
					dmg = buff.absorbDamage(dmg);
				}

				if (dmg > 0){
					//if there is leftover damage, then try to remove from other shielding buffs
					for (ShieldBuff s : target.buffs(ShieldBuff.class)){
						dmg = s.absorbDamage(dmg);
						if (dmg == 0) break;
					}
				}

				if (target.shielding() <= 0){
					state = State.RECOVERING;
					power = 0f;
					BuffIndicator.refreshHero();
					if (!target.isAlive()){
						target.die(this);
						if (!target.isAlive()) Dungeon.fail(this);
					}
				}

			} else {
				state = State.RECOVERING;
				if (buff != null) buff.absorbDamage(buff.shielding());
				power = 0f;
				if (!target.isAlive()){
					target.die(this);
					if (!target.isAlive()) Dungeon.fail(this);
				}

			}
		} else {
			// essentially while recovering your max rage is actually capped for basically all purposes.
			if (powerLossBuffer > 0){
				powerLossBuffer--;
			} else {
				if (state == State.RECOVERING && levelRecovery == 0
						&& Regeneration.regenOn()) {
					turnRecovery--;
					if (turnRecovery <= 0) {
						turnRecovery = 0;
						state = State.NORMAL;
					}
				}

				power -= GameMath.gate(recovered()/10f, power, recovered()) * (recovered() * 0.067f) * Math.pow((target.HP/(float)target.HT), 2);

				if (power <= 0 && state != State.RECOVERING) detach();
				else if (power < 1f) {
					power = Math.max(0, power);
				}
				ActionIndicator.refresh();
			}
		}
		spend(TICK);
		return true;
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	public float enchantFactor(float chance, boolean glyph){
		return chance + Math.min(1f,power)*((Hero)target).byTalent(
				Talent.ENRAGED_CATALYST, 1/5f,
				Talent.RK_BERSERKER, glyph ? .15f : 0f
		);
	}
	public float enchantFactor(float chance) { return enchantFactor(chance, false); }

	public int damageFactor(int dmg){
		return Math.round(dmg * damageMult());
	}
	public float damageMult() {
		return Math.min(1.5f, 1f + (power / 2f));
	}

	public boolean berserking(){
		if (target.HP == 0
				&& state == State.NORMAL
				&& power >= 1f
				&& target.buff(WarriorShield.class) != null
				&& ((Hero)target).hasTalent(Talent.DEATHLESS_FURY,Talent.RK_BERSERKER)){
			startBerserking();
			ActionIndicator.clearAction(this);
		}

		return state == State.BERSERK && target.shielding() > 0;
	}

	private void startBerserking(){
		state = State.BERSERK;

		SpellSprite.show(target, SpellSprite.BERSERK);
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
		GameScene.flash(0xFF0000);

		if (target.HP > 0) {
			turnRecovery = TURN_RECOVERY_START;
			levelRecovery = 0;
		} else {
			levelRecovery = getLevelRecoverStart();
			turnRecovery = 0;
		}

		//base multiplier scales at 2/3/4/5/6x at 100/37/20/9/0% HP
		float shieldMultiplier = 2f + 4*(float)Math.pow((1f-(target.HP/(float)target.HT)), 3);

		//Endless rage effect on shield and cooldown
		if (power > 1f){
			shieldMultiplier *= power;
			levelRecovery *= 2f - power;
			turnRecovery *= 2f - power;
		}

		WarriorShield shield = target.buff(WarriorShield.class);
		int shieldAmount = Math.round(shield.maxShield() * shieldMultiplier);
		shield.supercharge(shieldAmount);
		target.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(shieldAmount), FloatingText.SHIELDING );

		BuffIndicator.refreshHero();
	}

	private float rageFactor(int damage) {
		Hero hero = Dungeon.hero;
		float factor = 1 + hero.pointsInTalent(Talent.BERSERKING_STAMINA)
				// scales with hp like tenacity does
				* (float)Math.pow(hero.HP/(float)hero.HT, 2)
				// 4x / 7x / 10x. Even at +0 this should be absurd.
				* 3;
		return factor * damage / hero.HT / 3f * recovered();
	}

	float maxPower() {
		float endlessBoost = ((Hero)target).byTalent(
				Talent.ENDLESS_RAGE, 1/4f, // 125/150/175
				Talent.RK_BERSERKER, 1/6f
		);
		if(recovered() < 1) {
			// endless weights it towards 1.
			return recovered() * (1-endlessBoost) + endlessBoost;
		} else return 1 + endlessBoost;
	}

	public void damage(int damage){
		if (state == State.RECOVERING && !berserker()) return;
		power = Math.min(maxPower(), power + rageFactor(damage));
		BuffIndicator.refreshHero(); //show new power immediately
		powerLossBuffer = 3; //2 turns until rage starts dropping
		if (power >= 1f){
			ActionIndicator.setAction(this);
		}
	}

	public final float recovered() {
		if (levelRecovery > 0) {
			if(berserker()) levelRecovery = Math.min(levelRecovery, getLevelRecoverStart());
			return 1f - levelRecovery/getLevelRecoverStart();
		} else if(turnRecovery > 0){
			return 1f - turnRecovery/(float)TURN_RECOVERY_START;
		} else return 1;
	}

	public void recover(float percent){
		if (state == State.RECOVERING && levelRecovery > 0){
			levelRecovery -= percent;
			if (levelRecovery <= 0) {
				levelRecovery = 0;
				if (turnRecovery == 0){
					state = State.NORMAL;
					if(berserker()) {
						GLog.p("You have fully recovered!"); // because by this point it should look almost exactly like the regular anyway.
						Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
					}
				}
			}
		}
	}

	@Override
	public int actionIcon() {
		return HeroIcon.BERSERK;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text((int) (power * 100) + "%");
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x660000;
	}

	@Override
	public void doAction() {
		WarriorShield shield = target.buff(WarriorShield.class);
		if (shield != null && shield.maxShield() > 0) {
			startBerserking();
			ActionIndicator.clearAction(this);
		} else {
			GLog.w(Messages.get(this, "no_seal"));
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.BERSERK;
	}

	@Override
	public void tintIcon(Image icon) {
		float r,g,b;
		switch (state){
			case NORMAL: default:
				r = 1;
				g = power < 1f ? .5f : 0f;
				b = 0;
				break;
			case RECOVERING: // it's supposed to look more like the above as you get closer.
				r = berserker() ? .75f*recovered() : 0;
				g = .5f*r;
				b = 1-r;
				break;
		}
		icon.hardlight(r,g,b);
	}
	
	@Override
	public float iconFadePercent() {
		switch (state){
			case RECOVERING:
				if(!berserker() || recovered() == 0) return recovered();
				// fall through to NORMAL
			case NORMAL: default:
				return 1 - power/maxPower();
			case BERSERK:
				return 0f;
		}
	}

	public String iconTextDisplay(){
		// todo decide if/how to show berserker rage %
		switch (state){
			case NORMAL: case BERSERK: default:
				return (int)(power*100) + "%";
			case RECOVERING:
				if (levelRecovery > 0) {
					return Messages.decimalFormat("#.##", levelRecovery);
				} else {
					return Integer.toString(turnRecovery);
				}
		}
	}

	@Override
	public String name() {
		switch (state){
			case NORMAL: default:
				return Messages.get(this, "angered");
			case BERSERK:
				return Messages.get(this, "berserk");
			case RECOVERING:
				return Messages.get(this, "recovering");
		}
	}

	private String getCurrentRageDesc() {
		return Messages.get(this,"current_rage",Math.floor(power*100),Math.floor(100*(damageMult()-1)));
	}
	@Override
	public String desc() {
		String cls = Messages.titleCase(Dungeon.hero.subClass.title());
		StringBuilder desc = new StringBuilder();
		switch (state){
			case NORMAL: default:
				desc.append(Messages.get(this, "angered_desc",cls))
						.append("\n\n")
						.append(getCurrentRageDesc());
				break;
			case BERSERK:
				desc.append( Messages.get(this, "berserk_desc",cls,damageMult()));
				break;
			case RECOVERING:
				desc.append(Messages.get(this, "recovering_desc", cls, Messages.get(
						this,"recovering_penalty_" + (berserker() ? "berserk" : "default"), cls)));
				String key = "recovering_desc_";
				desc.append("\n\n").append(
						levelRecovery > 0 ? Messages.get(this, key + "levels", levelRecovery) :
								Messages.get(this, key + "turns", turnRecovery)
				);
				if( berserker() ) {
					desc.append("\n\n%Recovered: ").append((int)(recovered() * 100));
					if (power > 0) desc.append("\n").append(getCurrentRageDesc());
				}
				break;
		}
		return desc.toString();
	}
}
