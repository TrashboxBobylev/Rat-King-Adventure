    /*
     * Pixel Dungeon
     * Copyright (C) 2012-2015 Oleg Dolya
     *
     * Shattered Pixel Dungeon
     * Copyright (C) 2014-2023 Evan Debenham
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
package com.zrp200.rkpd2.actors.mobs.npcs;

    import com.watabou.utils.Bundle;
    import com.watabou.utils.Random;
    import com.zrp200.rkpd2.actors.Actor;
    import com.zrp200.rkpd2.actors.Char;
    import com.zrp200.rkpd2.actors.blobs.CorrosiveGas;
    import com.zrp200.rkpd2.actors.blobs.ToxicGas;
    import com.zrp200.rkpd2.actors.buffs.AllyBuff;
    import com.zrp200.rkpd2.actors.buffs.Burning;
    import com.zrp200.rkpd2.actors.hero.Hero;
    import com.zrp200.rkpd2.actors.hero.Talent;
    import com.zrp200.rkpd2.actors.mobs.Mob;
    import com.zrp200.rkpd2.items.rings.RingOfAccuracy;
    import com.zrp200.rkpd2.items.rings.RingOfEvasion;
    import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
    import com.zrp200.rkpd2.sprites.CharSprite;
    import com.zrp200.rkpd2.sprites.MirrorSprite;

    public abstract class AbstractMirrorImage extends NPC {
    {
        spriteClass = MirrorSprite.class;

        defenseSkill = 1;

        alignment = Char.Alignment.ALLY;
        state = HUNTING;

        //before other mobs
        actPriority = MOB_PRIO + 1;
    }

    protected Hero hero;
    private int heroID;
    public int armTier;

    @Override
    protected boolean act() {

        if ( hero == null ){
            hero = (Hero) Actor.findById(heroID);
            if ( hero == null ){
                die(null);
                sprite.killAndErase();
                return true;
            }
        }

        if (hero.tier() != armTier){
            armTier = hero.tier();
            ((MirrorSprite)sprite).updateArmor( armTier );
        }

        return super.act();
    }

    private static final String HEROID	= "hero_id";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( HEROID, heroID );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        heroID = bundle.getInt( HEROID );
    }

    @Override
    public abstract int damageRoll();

    public void duplicate(Hero hero ) {
        this.hero = hero;
        heroID = this.hero.id();
    }

    @Override
    public int attackSkill( Char target ) {
        // same base attack skill as hero, benefits from accuracy ring
        return hero != null ? (int)((9 + hero.lvl) * RingOfAccuracy.accuracyMultiplier(hero))
        : super.attackSkill(target);
    }

    protected int heroEvasion() {
        return (int)((4 + hero.lvl) * RingOfEvasion.evasionMultiplier( hero ));
    }

    @Override
    public int defenseSkill(Char enemy) {
        if (hero != null) {
            int baseEvasion = 4 + hero.lvl;

            //if the hero has more/less evasion, 50% of it is applied
            //includes ring of evasion boost
            return super.defenseSkill(enemy) * (baseEvasion + heroEvasion()) / 2;
        } else {
            return 0;
        }
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );

        if (enemy instanceof Mob) {
            ((Mob)enemy).aggro( this );
        }
        if (hero.belongings.weapon() instanceof MagesStaff){
            if (hero.pointsInTalent(Talent.SPECTRE_ALLIES) == 2) {
                ((MagesStaff) hero.belongings.weapon()).procWand(enemy, damage);
            }
            if (hero.pointsInTalent(Talent.SPECTRE_ALLIES) == 3) {
                MagesStaff staff = (MagesStaff) hero.belongings.weapon();
                if ((hero.hasTalent(Talent.SORCERY))) {
                    if (Random.Int(5) < hero.pointsInTalent(Talent.SORCERY)) {
                        staff.procBM();
                    }
                    if (Random.Int(3) < hero.pointsInTalent(Talent.SORCERY))
                        if (buff(Talent.EmpoweredStrikeTracker.class) != null) {
                            buff(Talent.EmpoweredStrikeTracker.class).detach();
                            damage = Math.round(damage * (1f + hero.byTalent(
                                    Talent.EMPOWERED_STRIKE, 1/4f,
                                    Talent.RK_BATTLEMAGE, 1/6f)
                            ));
                        }
                }
                staff.procWand(enemy, damage);
            }
        }
        return damage;
    }

    @Override
    public CharSprite sprite() {
        CharSprite s = super.sprite();

        hero = (Hero)Actor.findById(heroID);
        if (hero != null) {
            armTier = hero.tier();
        }
        ((MirrorSprite)s).updateArmor( armTier );
        return s;
    }

    {
        immunities.add( ToxicGas.class );
        immunities.add( CorrosiveGas.class );
        immunities.add( Burning.class );
        immunities.add( AllyBuff.class );
    }
}
