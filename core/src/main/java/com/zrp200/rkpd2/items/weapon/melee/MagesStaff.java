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

package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.ArtifactRecharge;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.particles.ElmoParticle;
import com.zrp200.rkpd2.items.ArcaneResin;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.items.bags.MagicalHolster;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.wands.WandOfCorrosion;
import com.zrp200.rkpd2.items.wands.WandOfCorruption;
import com.zrp200.rkpd2.items.wands.WandOfDisintegration;
import com.zrp200.rkpd2.items.wands.WandOfLivingEarth;
import com.zrp200.rkpd2.items.wands.WandOfRegrowth;
import com.zrp200.rkpd2.items.wands.WandOfUnstable2;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndBag;
import com.zrp200.rkpd2.windows.WndOptions;
import com.zrp200.rkpd2.windows.WndUseItem;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.*;

public class MagesStaff extends MeleeWeapon {

	protected Wand wand;
	public Wand wand() { return wand; }

	public static final String AC_IMBUE = "IMBUE";
	public static final String AC_ZAP	= "ZAP";

	private static final float STAFF_SCALE_FACTOR = 0.75f;

	{
		image = ItemSpriteSheet.MAGES_STAFF;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.1f;

		tier = 1;

		defaultAction = AC_ZAP;
		usesTargeting = true;

		unique = true;
		bones = false;
	}

	@Override
	protected boolean hasAbility() {
		return false;
	}

	public MagesStaff() {
		wand = null;
	}

	@Override
	public int max(int lvl) {
		return  Math.round(3f*(tier+1)) +   //6 base damage, down from 10
				lvl*(tier+1);               //scaling unaffected
	}

	public MagesStaff(Wand wand){
		this();
		wand.identify();
		wand.cursed = false;
		this.wand = wand;
		updateWand(false);
		wand.curCharges = wand.maxCharges;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		actions.add(AC_IMBUE);
		if (wand!= null && curCharges() > 0) {
			actions.add( AC_ZAP );
		}
		return actions;
	}

	@Override
	public String defaultAction() {
		return AC_ZAP;
	}

	@Override
	public void activate( Char ch ) {
		super.activate(ch);
		applyWandChargeBuff(ch);
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		if (wand != null) {
			return wand.targetingPos(user, dst);
		} else {
			return super.targetingPos(user, dst);
		}
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_IMBUE)) {

			curUser = hero;
			GameScene.selectItem(itemSelector);

		} else if (action.equals(AC_ZAP)){

			if (wand == null) {
				GameScene.show(new WndUseItem(null, this));
				return;
			}

			if (cursed || hasCurseEnchant()) wand.cursed = true;
			else                             wand.cursed = false;
			if (this instanceof KromerStaff)
				Warp.inflict(2.5f, 1.5f);
			wand.execute(hero, AC_ZAP);
		}
	}

	@Override
	public int buffedVisiblyUpgraded() {
		if (wand != null){
			return Math.max(super.buffedVisiblyUpgraded(), wand.buffedVisiblyUpgraded());
		} else {
			return super.buffedVisiblyUpgraded();
		}
	}

	public void procBM() {
		int points = hero.shiftedPoints(Talent.MYSTICAL_CHARGE, Talent.RK_BATTLEMAGE);
		if (points > 0){
			ArtifactRecharge.chargeArtifacts(hero, points/2f);
		}

		if (curCharges() < wand.maxCharges) gainCharge(0.5f);
		ScrollOfRecharging.charge(Dungeon.hero);
	}

	public int curCharges() {
		return wand.curCharges();
	}

	public void procWand(Char defender, int damage) {
		wand.onHit(this, hero,defender,damage);
	}

	@Override
	public int reachFactor(Char owner) {
		int reach = super.reachFactor(owner);
		if (owner instanceof Hero
				&& wand instanceof WandOfDisintegration
				&& ((Hero)owner).hasTalent(Talent.RK_BATTLEMAGE)) {
			reach += Math.round(Wand.procChanceMultiplier(owner));
		}
		return reach;
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect(container)) {
			if (container.owner != null) {
				applyWandChargeBuff(container.owner);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDetach( ) {
		if (wand != null) wand.stopCharging();
	}

	protected int wastedUpgrades; // amount of upgrades wasted on initial imbue.
	public Item imbueWand(Wand wand, Char owner){

		int oldStaffcharges = this.wand != null ? this.wand.curCharges : 0;

		if (owner == hero) {
			boolean preserve = false;
			if(hero.hasTalent(Talent.WAND_PRESERVATION, Talent.POWER_WITHIN)) {
				int max = hero.pointsInTalent(Talent.WAND_PRESERVATION) < 2 ? 5 : 8;
				Talent.WandPreservationCounter counter = Buff.affect(hero, Talent.WandPreservationCounter.class);
				if (counter.count() < max && (hero.hasTalent(Talent.WAND_PRESERVATION) || hero.pointsInTalent(Talent.POWER_WITHIN) == 2 || Random.Int(3) > 0)){
					counter.countUp(1);
					preserve = true;
				};
			}
			// mage has an intrinsic 2/3 chance to preserve a wand anyway.
			if(preserve || hero.heroClass.isExact(HeroClass.MAGE) && Random.Int(3) > 0) {
				this.wand.level(wastedUpgrades);
				if (!this.wand.collect()) {
					level.drop(this.wand, owner.pos);
				}
				else {
					GameScene.pickUp( this, owner.pos );
					Sample.INSTANCE.play( Assets.Sounds.ITEM );
				}
				GLog.newLine();
				GLog.p(Messages.get(this, "preserved"));
			} else {
				ArcaneResin resin = new ArcaneResin();
				if (!resin.collect()) {
					level.drop(resin, owner.pos);
				}
				GLog.newLine();
				GLog.p(Messages.get(this, "preserved_resin"));
			}
		}

		this.wand = null;

		wand.resinBonus = 0;
		wand.updateLevel();

		//syncs the level of the two items.
		int targetLevel = Math.max(this.trueLevel(), wand.trueLevel());

		//if the staff's level is being overridden by the wand, preserve 1 upgrade
		if (wand.trueLevel() >= this.trueLevel() && this.trueLevel() > 0) targetLevel++;

		// determine how many levels were wasted
		wastedUpgrades = this.level() + wand.level() - targetLevel;
		// update to the target level.
		level(targetLevel);
		this.wand = wand;
		updateWand(false);
		wand.curCharges = Math.min(wand.maxCharges, wand.curCharges+oldStaffcharges);
		if (owner != null){
			applyWandChargeBuff(owner);
 		} else if (hero.belongings.contains(this)){
			applyWandChargeBuff(hero);
		}

		//This is necessary to reset any particles.
		//FIXME this is gross, should implement a better way to fully reset quickslot visuals
		int slot = quickslot.getSlot(this);
		if (slot != -1){
			quickslot.clearSlot(slot);
			updateQuickslot();
			quickslot.setSlot( slot, this );
			updateQuickslot();
		}
		
		Badges.validateItemLevelAquired(this);

		return this;
	}

	public void gainCharge( float amt ){
		gainCharge(amt, false);
	}

	public void gainCharge( float amt, boolean overcharge ){
		if (wand != null){
			wand.gainCharge(amt, overcharge);
		}
	}

	public void applyWandChargeBuff(Char owner){
		if (wand != null){
			wand.charge(owner, STAFF_SCALE_FACTOR);
		}
	}

	public Class<?extends Wand> wandClass(){
		return wand != null ? wand.getClass() : null;
	}

	public static Class<?extends Wand> getWandClass() {
		MagesStaff staff = hero.belongings.getItem(MagesStaff.class);
		return staff != null ? staff.wandClass() : null;
	}

	@Override
	public Item upgrade(boolean enchant) {
		super.upgrade( enchant );

		updateWand(true);

		return this;
	}

	@Override
	public Item degrade() {
		super.degrade();

		updateWand(false);

		return this;
	}
	
	public void updateWand(boolean levelled){
		if (wand != null) {
			int curCharges = wand.curCharges;
			wand.level(level());
			//gives the wand one additional max charge
			wand.maxCharges = Math.min(wand.maxCharges + 1, 10);
			wand.curCharges = Math.min(curCharges + (levelled ? 1 : 0), wand.maxCharges);
			updateQuickslot();
		}
	}

	@Override
	public String status() {
		if (wand == null) return super.status();
		else return wand.status();
	}

	@Override
	public String name() {
		if (wand == null) {
			return super.name();
		} else {
			String name = Messages.get(wand, "staff_name");
			if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
				return ShatteredPixelDungeon.turnIntoRrrr(name);
			}
			return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.name( name ) : name;
		}
	}

	@Override
	public String info() {
		String info = super.info();

		if (wand == null){
			//FIXME this is removed because of journal stuff, and is generally unused.
			//perhaps reword to fit in journal better
			//info += "\n\n" + Messages.get(this, "no_wand");
		} else {
			info += "\n\n" + Messages.get(this, "has_wand", Messages.get(wand, "name"));
			if ((!cursed && !hasCurseEnchant()) || !cursedKnown)    info += " " + wand.statsDesc();
			else                                                    info += " " + Messages.get(this, "cursed_wand");

			if (hero.subClass.is(HeroSubClass.BATTLEMAGE)){
				info += "\n\n" + Messages.get(wand, "bmage_desc", Messages.titleCase(hero.subClass.title()));
			}
		}

		return info;
	}

	@Override
	public Emitter emitter() {
		if (wand == null) return null;
		Emitter emitter = new Emitter();
		emitter.pos(12.5f, 3);
		emitter.fillTarget = false;
		emitter.pour(StaffParticleFactory, 0.1f);
		return emitter;
	}

	private static final String WAND = "wand";
	private static final String WAST = "wastedUpgrades";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(WAND, wand);
		bundle.put(WAST, wastedUpgrades);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		wand = (Wand) bundle.get(WAND);
		if (wand != null) {
			wand.maxCharges = Math.min(wand.maxCharges + 1, 10);
		}
		wastedUpgrades = bundle.getInt(WAST);
	}

	@Override
	public int value() {
		return 0;
	}
	
	@Override
	public Weapon enchant(Enchantment ench) {
		if (curseInfusionBonus && (ench == null || !ench.curse())){
			curseInfusionBonus = false;
			updateWand(false);
		}
		return super.enchant(ench);
	}
	
	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(MagesStaff.class, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return MagicalHolster.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Wand;
		}

		@Override
		public void onSelect( final Item item ) {
			if (item != null) {

				if (!item.isIdentified()) {
					GLog.w(Messages.get(MagesStaff.class, "id_first"));
					return;
				} else if (item.cursed){
					GLog.w(Messages.get(MagesStaff.class, "cursed"));
					return;
				}

				if (wand == null){
					applyWand((Wand)item);
				} else {
					int newLevel;
					int itemLevel = item.trueLevel();
					if (itemLevel >= trueLevel()){
						if (trueLevel() > 0)    newLevel = itemLevel + 1;
						else                    newLevel = itemLevel;
					} else {
						newLevel = trueLevel();
					}

					String bodyText = Messages.get(MagesStaff.class, "imbue_desc", newLevel);
					int preservesLeft = hero.pointsInTalent(Talent.WAND_PRESERVATION) == 2 ? 8 : hero.pointsInTalent(Talent.WAND_PRESERVATION) == 1 || hero.hasTalent(Talent.POWER_WITHIN) ? 5 : 0;
					if (hero.buff(Talent.WandPreservationCounter.class) != null){
						preservesLeft -= hero.buff(Talent.WandPreservationCounter.class).count();
					}
					if (preservesLeft > 0){
						int preserveChance = hero.pointsInTalent(Talent.POWER_WITHIN) == 1 ? 67 : 100;
						bodyText += "\n\n" + Messages.get(MagesStaff.class, "imbue_talent", preserveChance, wastedUpgrades, preservesLeft);
					} else {
						bodyText += "\n\n" + Messages.get(MagesStaff.class, "imbue_lost");
					}

					GameScene.show(
							new WndOptions(new ItemSprite(item),
									Messages.titleCase(item.name()),
									bodyText,
									Messages.get(MagesStaff.class, "yes"),
									Messages.get(MagesStaff.class, "no")) {
								@Override
								protected void onSelect(int index) {
									if (index == 0) {
										applyWand((Wand)item);
									}
								}
							}
					);
				}
			}
		}

		private void applyWand(Wand wand){
			Sample.INSTANCE.play(Assets.Sounds.BURNING);
			curUser.sprite.emitter().burst( ElmoParticle.FACTORY, 12 );
			evoke(curUser);

			quickslot.clearItem(wand);

			wand.detach(curUser.belongings.backpack);

			GLog.p( Messages.get(MagesStaff.class, "imbue", wand.name()));
			imbueWand( wand, curUser );

			updateQuickslot();
		}
	};

	private final Emitter.Factory StaffParticleFactory = new Emitter.Factory() {
		@Override
		//reimplementing this is needed as instance creation of new staff particles must be within this class.
		public void emit( Emitter emitter, int index, float x, float y ) {
			StaffParticle c = (StaffParticle)emitter.getFirstAvailable(StaffParticle.class);
			if (c == null) {
				c = new StaffParticle();
				emitter.add(c);
			}
			c.reset(x, y);
		}

		@Override
		//some particles need light mode, others don't
		public boolean lightMode() {
			return !((wand instanceof WandOfDisintegration)
					|| (wand instanceof WandOfCorruption)
					|| (wand instanceof WandOfCorrosion)
					|| (wand instanceof WandOfRegrowth)
					|| (wand instanceof WandOfLivingEarth)
					|| (wand instanceof WandOfUnstable2));
		}
	};

	//determines particle effects to use based on wand the staff owns.
	public class StaffParticle extends PixelParticle {

		private float minSize;
		private float maxSize;
		public float sizeJitter = 0;

		public StaffParticle(){
			super();
		}

		public void reset( float x, float y ) {
			revive();

			speed.set(0);

			this.x = x;
			this.y = y;

			if (wand != null)
				wand.staffFx( this );

		}

		public void setSize( float minSize, float maxSize ){
			this.minSize = minSize;
			this.maxSize = maxSize;
		}

		public void setLifespan( float life ){
			lifespan = left = life;
		}

		public void shuffleXY(float amt){
			x += Random.Float(-amt, amt);
			y += Random.Float(-amt, amt);
		}

		public void radiateXY(float amt){
			float hypot = (float)Math.hypot(speed.x, speed.y);
			this.x += speed.x/hypot*amt;
			this.y += speed.y/hypot*amt;
		}

		@Override
		public void update() {
			super.update();
			size(minSize + (left / lifespan)*(maxSize-minSize) + Random.Float(sizeJitter));
		}
	}
}
