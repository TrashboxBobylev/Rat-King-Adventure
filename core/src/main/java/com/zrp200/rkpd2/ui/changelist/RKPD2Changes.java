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

package com.zrp200.rkpd2.ui.changelist;

import com.watabou.noosa.Image;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges.Badge;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.Wrath;
import com.zrp200.rkpd2.effects.BadgeBanner;
import com.zrp200.rkpd2.items.armor.RatKingArmor;
import com.zrp200.rkpd2.items.armor.WarriorArmor;
import com.zrp200.rkpd2.items.bags.VelvetPouch;
import com.zrp200.rkpd2.items.quest.FlexTape;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.quest.NerfGun;
import com.zrp200.rkpd2.items.wands.WandOfFirebolt;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.enchantments.Explosive;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.ChangesScene;
import com.zrp200.rkpd2.sprites.BlacksmithSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.KingSprite;
import com.zrp200.rkpd2.sprites.RatKingBossSprite;
import com.zrp200.rkpd2.sprites.ShopkeeperSprite;
import com.zrp200.rkpd2.sprites.TormentedSpiritSprite;
import com.zrp200.rkpd2.sprites.WraithSprite;
import com.zrp200.rkpd2.ui.Icons;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Assets.Interfaces.TALENT_ICONS;
import static com.zrp200.rkpd2.actors.hero.HeroClass.*;
import static com.zrp200.rkpd2.actors.hero.HeroSubClass.*;
import static com.zrp200.rkpd2.actors.hero.Talent.*;
import static com.zrp200.rkpd2.messages.Messages.get;
import static com.zrp200.rkpd2.sprites.CharSprite.*;
import static com.zrp200.rkpd2.sprites.HeroSprite.avatar;
import static com.zrp200.rkpd2.sprites.ItemSpriteSheet.*;
import static com.zrp200.rkpd2.ui.Icons.DEPTH;
import static com.zrp200.rkpd2.ui.Icons.DISPLAY_LAND;
import static com.zrp200.rkpd2.ui.Icons.INFO;
import static com.zrp200.rkpd2.ui.Icons.PREFS;
import static com.zrp200.rkpd2.ui.Icons.TALENT;
import static com.zrp200.rkpd2.ui.Icons.TARGET;
import static com.zrp200.rkpd2.ui.Icons.TRASHBOXBOBYLEV;
import static com.zrp200.rkpd2.ui.Icons.get;
import static com.zrp200.rkpd2.ui.Window.SHPX_COLOR;
import static com.zrp200.rkpd2.ui.Window.TITLE_COLOR;
import static java.util.Arrays.asList;

// TODO should I have a separate section for shattered changes?
interface ChangeLog {
    // yet another attempt at formatting changes. not sure if this is any better than the last one, but one thing is certain: I HATE the way it's done in Shattered.
    // in this case I made it so you could add buttons in the ChangeInfo constructor; this is 'lustrous' style

    ChangeInfo[][] getChanges();
}

public enum RKPD2Changes {
    v1(() -> new ChangeInfo[][]{
        {
            new ChangeInfo("Coming Soon", true, TITLE_COLOR,
                    new ChangeButton(Icons.get(Icons.SHPX), "Future Shattered Updates",
                            "I plan on implementing future Shattered updates, to prevent this mod from becoming outdated."),
                    new ChangeButton(DUELIST, "The monk is currently missing an extra t4 talent, and Rat King does not have her implemented at all."),
                    bugFixes("Critical bug fixes are planned. For what, you ask? *chuckle*")
            )
        },
        {
            new ChangeInfo("v2.0.0", true, TITLE_COLOR,
                    info(Messages.get(RKPD2Changes.class, "200")),
                    new ChangeButton(DUELIST, "Added duelist!"
                            +"\n\nNotable changes from SHPD:" + list(2,
                            "Maximum ability charges increased by 50%",
                            "Some weapon abilities have been buffed" + list(
                                    "Lunge now works at a distance of 2, letting it be used for spacing."
                            ),
                            "Most T1-3 talents buffed heavily. T4 talents are unchanged.",
                            "Added an additional talent to Champion that allows equipping of thrown weapons, which can be used to extend most abilities' effective range, or a third weapon. _Monk ability is WIP._"
                    ) + "\n\n_Rat King currently does not have access to Duelist's mechanics_"),
                    new ChangeButton(BERSERKER,
                            // TODO fix this
                            "Reworked Berserker in response to 1.4.0:" +
                                    list(2,
                                            "Berserk is now manually activated, but it has a much lower cooldown.",
                                            "Endless Rage and Inevitability now grant a bonus to berserk duration and cooldown when above 100% rage.") + list(
                                            "Berserking Stamina replaced with Deathless Fury, which lets berserking automatically trigger just like before, but require exp like before" + list("The cooldown now matches Shattered's growth instead of being greatly increased", "Rage weighting is now moved to this talent instead of being spread across several different ones.", "Berserking Stamina's perk of allowing rage gain while recovering is transfered to Deathless Fury.")
                                    )
                    ),
                    new ChangeButton(Icons.get(TALENT), "Talents",
                            "_Armsmaster's Intuition_:" + list(
                                    "Renamed to Veteran's Intuition to match Shattered v2.0.0",
                                    "+0 effect identifies weapons 1.75x faster, down from 2x, but identifies armors 2.5x faster, up from 2x.",
                                    "Weapons are no longer identified on equip at +1, instead identified 2.5x faster. They are still identified on equip at +2.",
                                    "+1 can now identify armors on pickup with a 30% chance, and the +2 chance to do so is increased to 60% from 50%.",
                                    "Chance to identify weapons on pickup reduced to 0/30% from 0/50% at +1/+2"
                            ) + "Note that Duelist's _Adventurer's Intuition_ is the exact reverse of this talent.",
                            "_Assassin's Lethal Momentum_ has been reworked to be even more distinct from the standard Lethal Momentum:" + list(
                                    "Now preserves a slightly random proportion of Preparation on kill in addition to nullifying the attack delay of the killing blow.",
                                    "I'd also say that it no longer works on successive kills without preparation, but the mechanic was bugged to not work anyway.")
                                    + "That said:\n_Warrior's Lethal Momentum_ now correctly works on follow-up attacks as well!"
                            + '\n' + list("Rogue's Foresight buffed to 66%/99% detection, and grants additional chances to notice hidden stuff.")
                    ),
                    new ChangeButton(new ItemSprite(CROWN),
                            "Talent and Armor Ability Changes",
                            list("_Smoke Bomb_ range down to 10 from 12", "Smoke Bomb Shadow Step reduction now 20%/36%/50%/60% down from 24%/42%/56%/67")
                                    + list("_Wrath_ range reduced to 6 from 10", "_Smoke and Mirrors_ charge cost reduction down to 16/30/41/50%, from 20/36/50/60%")
                                    + list("_Heroic Energy_ unchanged at 16/30/40/50 instead of being nerfed")
                                    + list("_Shrug it Off_ now just directly boosts the damage resistance from endure to 60/68/74/80%, instead of reducing max damage taken")
                                    + list("_Wild Magic_ now spends 0.5 wand charges at base. Conserved magic can reduce this to 0.1")
                                    + list("_Double Jump_ charge reduction nerfed to 16%/30%/41%/50% from 20%/36%/50%/60%/68%")
                    ),
                    bugFixes(list(
                            "Fixed an exploit allowing Rogue to gain infinite healing from talents",
                            "Rat King sometimes healing beyond his max hp when eating."
                    ))
            ),
            new ChangeInfo("v2.0.4-v2.0.6",false,TITLE_COLOR,
                misc("_2.0.6_" + list("Added an Elemental Strike effect for Chaotic curse. It's rather...chaotic, if you ask me.")),
                bugFixes(
                    "_2.0.6_:" + list(
                            "Barkskin talent giving more barkskin than intended",
                            "Omniability getting stuck on duelist abilities",
                            "Rat King always gaining combo when attacking")
                    + "_2.0.5_:" + list(
                        "Crash when interacting with the pickaxe under certain scenarios",
                        "Glaive's spike not doing damage",
                        "Backup barrier always visually activating",
                        "Crash handler not activating :("
                    ),
                    "_2.0.4_:" + list(
                        "detaching the seal degrading armor",
                        "elite dexterity not working as intended/described",
                        "elite dexterity switch prompt not always showing the correct weapons",
                        "backup barrier not giving correct amount of shielding",
                        "Blacksmith quest not correctly indicating quest completion"
                    )
            )),
            new ChangeInfo("v2.0.1-3", false, TITLE_COLOR,
                    new ChangeButton(INSCRIBED_POWER, "Inscribed Power, Desperate Power, and their equivalents",
                            "It turns out that these talents didn't actually work! Well, now they do, with some changes:" +
                                    "\n\n_Desperate Power_:"
                                    + list("Now boosts levels by what its description says, instead of the +1/+2/+3 that was accidentally implemented.",
                                        "Rat King's Desperate Power now actually works, and does +1/+2/+3")
                                    + "\n_Inscribed Power_:" + list(
                                        "Removed outdated description; it now just has its old +2 effect at +1, and its old +3 effect at +2",
                                        "Amount of wand zaps given increased by 1",
                                        "This probably makes it silly, but this mod is silly anyway."
                                    )
                                    + "\n_Inscribed Stealth_:"
                                    + list("Buffed to be 6/9 turns of stealth, instead of having no buff at all.", "Removed outdated description, and added a comment")
                    ),
                    new ChangeButton(ELITE_DEXTERITY, "Talent Icons", "Changed the talent icons for Elite Dexterity, Assassin's Lethal Momentum, and Lethal Rush to better match similar talents"),
                    bugFixes("Caused by v2.0.0:" + list(
                            "Lethal Rush always proccing",
                            "Outdated description for Iron Stomach",
                            "Bad highlighting for Law of Consumption",
                            "Elite Dexterity only recharging when upgraded to +3",
                            "Crash whenever Bright Fist attacks",
                            "Eating full healing uncondiditionally",
                            "On id talents sometimes not working",
                            "Minor text errors",
                            "Bugged rat king portrait",
                            "Weird lunge behavior"
                    ) + "Existed Prior:" + list("Staff not always showing level"))
            ),
            new ChangeInfo("From SHPD v2.2-3", false, SHPX_COLOR,
                    new ChangeButton(new BlacksmithSprite(), "Quests rework", "Implemented the blacksmith quest with changes:" + list(
                            "The mining area is a bit smaller, and only requires 30 gold",
                            "You get 2000 favor for free!",
                            "Updated blacksmith quotes to be more appropriate to speaking with royalty."
                    ) + "\n\n"
                            + "_Implemented Wandmaker quest changes:_"
                            + "\n\n_Corpse Dust Quest:_ The mass grave room now always spawns at least a bit away from the entrance, and wraith spawning is more consistent."
                            + "\n\n_Elemental Embers Quest:_"
                            + list(
                            "HP up to 60 from 30, attacking power reduced, no longer ignites on-hit",
                            "Now shoots an avoidable fireball in a 3x3 area",
                            "The summon elemental spell is unchanged")
                            + "\n_Rotberry Quest:_" + list(
                            "Rot lashers are now much stronger, but take 1 turn to notice an adjacent enemy before attacking",
                            "Room layout is now much more chaotic, with more grass and crumbling walls",
                            "There is now a guaranteed safe path to the rot heart")),
                    new ChangeButton(Icons.get(Icons.AUDIO), "Audio", "Added additional audio tracks."),
                    new ChangeButton(new ItemSprite(ItemSpriteSheet.REMAINS), "Heroes Remains Items", "Implemented heroes remains items, and added an additional one, for....reasons."),
                    new ChangeButton(new ItemSprite(CHOC_AMULET), "Added holiday items", "You can enjoy holiday items in RKPD2 as well now! Enjoy your free amulet!"),
                    new ChangeButton(Icons.get(Icons.BUFFS), "Floating Text Icons", "Implemented floating text icons, replacing text in a lot of cases."),
                    misc(list(
                            "Updated to the latest version of Shattered's game library (libGDX), which has a few benefits:\n" + list("Improved vibration on modern iOS devices", "Improved changing audio device behavior", "Misc. stability & compatibility improvements"),
                            "Magical fire is now cleared by frost next to it, in addition to on top of it",
                            "Tengu's fire wall attack now ignites items\n",
                            "Improved music transitions in main menu when game was just won\n",
                            "Added support for controller vibration",
                            "Added a vibration toggle in the settings\n",
                            "Updated translators and translator credits\n",
                            "Improved the sprites for Armored Brutes and DM-201s", "Random scroll and potion drops are now more consistent throughout a run", "DM-300's rockfall attack now uses positional danger indicators", "Improved visual clarity of sparks in the DM-300 fight", "Removed unnecessary game log entries when DM-300 uses abilities\n", "Added a warning when trying to steal from shops with less than 100% success chance", "Curse infusion now preserves an existing curse on items that don't have the curse infusion bonus yet", "long pressing on the ghost equip window now shows the stats of equipped items", "Ghosts and Rogue's shadow clone are now considered inorganic (immune to bleed, toxic, poison)", "Corrupted allies no longer attack passive enemies", "Spirit hawk now interrupts the hero when it expires\n", "Surface scene now shows night later in the evening as well as after midnight", "Did a consistency pass on heal over time effects interrupting the hero resting", "Long-press to assign quickslot now works in the full UI inventory pane, just like the mobile inventory window", "Added support for themed icons on Android 13+", "Removed support for saves prior to v1.4.3 --- this means prior to rkpd2 1.0.0 :(")),
                    // lmao
                    bugFixes(list("Enemies continuing to fight each other after amok expires in many cases", "Rounding errors causing tipped darts to last longer than intended in some cases", "Corpse dust quest tracking all wraiths instead of just the ones it spawned", "Final boss fight not properly interacting with the into darkness challenge", "Tengu behaving slightly incorrectly when taking massive damage", "Mimics not dropping their loot if corrupted while hiding", "Rare errors in DM-201 target selection", "Rotberry seed being deleted in rare cases", "Rare cases where the game would freeze after reviving via unblessed ankh", "Some bombs and explosion-spawning effects incorrectly dealing magic damage", "Foresight effects not triggering after level transition", "Projecting missile weapons not working on enemies inside solid terrain", "Cursed wand of warding having different targeting properties than other wands", "Thrown potions not clearing fire/ooze if they shattered out of view", "Retribution and psionic blast not applying to all visible characters in very rare cases", "Degrade debuff not applying to thrown weapons", "Cloak of shadows not losing charge if it is dispelled as it is activated", "Items being assignable to non-visible quickslots in specific cases", "Rare quickslot errors when bags which already contain items are collected", "Damage from Body Slam talent ignoring armor", "Cases where prismatic images could keep appearing and then disappearing", "Hero not being able to self-trample plants when standing on stairs", "Berserker being able to rage without his seal equipped in some cases", "Allies rarely spawning on hazards after ankh revive", "Ally warp working on corrupted DM-201s", "Various rare crash and freeze bugs", "Various minor visual and textual errors", "Tutorial becoming stuck in rare cases", "Beta updates setting not working as intended", "Music fading not working in rare cases", "Scrolling pane in journal window freezing in rare cases", "Some items being incorrectly consumed when the game is closed while they are being used", "Mage's Staff not being affected by the degrade debuff", "Further characters sometimes rendering on top of closer large characters", "Dwarf King's Crown rarely triggering the effect of the runic transference talent", "Exploit where multiplicity curse could be used to skip some of Dwarf King's second phase", "Various errors with class armor conversion and Warrior's broken seal", "Ring of Force incorrectly displaying +99.99% when at +7, instead of +100%", "Living Earth and Transfusion wands granting their self-buffs when shooting NPCs", "Several obscure issues with noisemakers", "Trap effects from reclaim trap spell not scaling with ascension challenge", "Horn of Plenty occasionally having the wrong visuals for its charge state", "Damage caused to Yog's fists not correctly adding time to boss regen limit", "Ambitious Imp sometimes calling out to the hero when not visible", "Phantom Piranhas rapidly teleporting when corrupted", "DM-300 fight sometimes not having a safe route to a power pylon", "Mimics not dropping loot when they are killed via chasm while hiding", "Tengu very rarely throwing bombs ontop of each other", "Piranhas throwing themselves off chasms in very rare cases", "The Rogue's body replacement talent not triggering effects like chasms and traps", "Various rare crash issues", "Various minor visual and textual errors", "Various rare cases where levelgen could differ between two runs on the same seed", "Into Darkness and Barren Land challenges affecting levelgen", "Items in Imp's shop not being affected by dungeon seed", "Gold ore appearing on the back face of walls in regular caves levels", "Starflower plant VFX triggering even when out of the hero's FOV", "Storm Clouds not correctly clearing fire or harming fiery enemies", "Cases where pushing effects could cause pitfalls to trigger early", "Combining diagonal direction keys on desktop causing rare errors", "Cases where default keybindings could override custom ones", "Crashes caused by text input windows for controller users", "System gestures in iOS sometimes registering as taps within the game", "Endure ability not working properly when used twice quickly", "Melee damage of Mage's Staff benefiting from talents that boost wand levels", "Various blink effects allowing movement over magical fire", "Some game actions being possible while meditating", "Various minor visual and textual errors"))),
            new ChangeInfo("From SHPD v2.0-1", false, SHPX_COLOR,
                new ChangeButton(new ItemSprite(ItemSpriteSheet.WAR_SCYTHE), "New Weapons!",
                        "Three new weapons have been added to the game!\n" +
                                "\n" +
                                "_The Katana_ is a tier-4 defensive weapon that was designed to respond to the common feedback point of players wanting a higher tier weapon with the rapier's ability. Just like with her rapier, the Duelist can _lunge_ at enemies with a katana, dealing bonus damage.\n" +
                                "\n" +
                                "_The Sickle_ and _War Scythe_ are T2 and T5 weapons that trade in some accuracy for increased base damage. The Duelist can use the _harvest_ ability with these weapons, which deals a large amount of bleeding instead of direct damage, but costs 2 charges."),
                new ChangeButton(new Image(new TormentedSpiritSprite()), "New Exotic Enemies",
                        "An exotic variant has been added for wraiths and piranhas!\n" +
                                "\n" +
                                "_Tormented Spirits_ replace 1 in every 100 wraiths, and have higher stats along with a unique interaction. Using a scroll of remove curse on these spirits will save them from their curse, peacefully defeating them and giving you an uncursed equipment reward!\n" +
                                "\n" +
                                "_Phantom Piranhas_ replace 1 in every 50 piranhas, and can teleport either to attack or retreat whenever they take damage. They're harder to kill, but give valuable phantom meat as a drop, instead of regular mystery meat. They're also a reference to the phantom fish quest from the original Pixel Dungeon!"),
                new ChangeButton(new Image(new ShopkeeperSprite()), "Shop Interface Improvements",
                        "_A new UI has been added when interacting with shopkeepers._ This UI lets you talk with them and buyback the 3 most recently sold items!"),
                new ChangeButton(Icons.get(Icons.STAIRS), "Ascension",
                        "_-_ Enemies to kill per floor reduced to 2 from 2.5. Thresholds for all amulet debuff effects adjusted to compensate\n"
                        + "\n"
                        + "_- Ripper demon_ spawn rate increased if player is ascending\n"
                        + "\n"
                        + "_- Monk & Warlock_ stat boost up to 1.5x from 1.33x\n"
                        + "_- Elemental & Ghoul_ stat boost up to 1.67x from 1.5x\n"
                        + "\n" +
                        "_- Crab & Slime_ stat boost up to 8x from 6x\n"
                        + "_- Swarm_ stat boost up to 8.5x from 6.5x\n"
                        + "_- Gnoll & Snake_ stat boost up to 9x from 7x\n"
                        + "_- Rat_ stat boost up to 10x from 8x"),
                    new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_TOPAZ),"Item Changes",
                        "_Buffs_:\n"
                                + list("_Pickaxe_ can now benefit from upgrades enchantments and augmentation, if you feel like using it for fun.")
                                + list("_Ring of Energy_ now also applies a recharging boost to hero armor abilities. All the boosts it gives are now standardized to +15%.", "_Ring of Arcana_ enchantment boost up to +17.5% per level, from +15%")
                                + list("_Glyph of Repulsion_ now only knocks back enemies who are adjacent to the hero. This should make it slightly better versus ranged enemies.", "_Glyph of Flow_ now grants +50% movespeed in water per level, up from +25%")
                                + list("_Horn of Plenty_ now gains 2 levels from being fed a pasty, up from 1.5", "_Horn of Plenty_ now gains 4 levels from being fed a meat pie, up from 3"),
                            "_Major Nerfs_:"
                                    + "\n"
                                    + list("_Round Shield_ blocking per level reduced to 0-1 from 0-2, base damage increased to 3-12 from 3-10", "_Greatshield_ blocking per level reduced to 0-2 from 0-3, base damage increased to 5-18 from 5-15")
                                    + "\n"
                                    + "_Wand of Corruption:_" + list("Corrupted enemies now die over 100 turns, down from 200", "Doomed bosses now take +67% damage, down from +100%",  "Battlemage corruption on-hit base proc rate reduced to 1/6 from 1/4")
                                    + "\n"
                                    + "_Wand of Regrowth:_" + list("Base charge limit increased to 20 from 8","Charge limit scaling substantially reduced at wand levels 4 to 9.")
                                    + "\n"
                                    + "_Chalice of Blood:_" + list("Prick damage increased by 5 at all levels","Now grants 1.15x-5x healing, down from 0x-10x")
                                    + list("_Ethereal chains_ charge from gaining exp reduced by 40%"),
                            "_Minor Nerfs_:"
                                    + "\n"
                                    + list(
                                    "_Ring of Furor_ attack speed boost per level down to 9.05% from 10.5%", "_Ring of Evasion_ dodge boost per level down to 12.5% from 15%", "_Blocking_ enchantment now grants 2+item level shielding, down from max HP/10", "_Timekeeper's Hourglass_ sand bag cost up to 30 from 20", "_Alchemist's Toolkit_ now requires 6 energy per level, up from 5", "_Wand of Fireblast_ base damage reduced to 1-2 from 1-6 when spending 1 charge, and 2-8 from 2-12 when spending 2 charges. This is to offset the relatively high amount of DOT the wand deals at low levels.")),
                    misc(
                            "_Highlights:_" + list("The game now remembers if the player removes the waterskin from their quickslot right after starting a run", "The damage warning vfx now always interrupts the hero, regardless of any other factors", "The deadly misstep badge can now also be unlocked with disintegration traps", "Added metamorphosis effects to the two remaining talents that previously couldn't be gained by metamorphosis", "Desktop users can now toggle fullscreen with right-alt + enter", "Added a setting to enable/disable playing music in background on desktop", "Added a 5th games in progress slot for the Duelist","The changes screen now supports more text for a single entry. On mobile UI the changes window can now have multiple tabs, on full UI the changes pane on the right is now scrollable."),
                            "_Effects:_" + list("Backup barrier now triggers before wand zaps fully resolve", "The chasm warning screen now also appears when levitation is about to end.", "Levitation now prevents damage from floor electricity during the DM-300 fight"),
                            "_Hero, Allies, & Enemies:_" + list("The hero can now also self-trample plants, in addition to grass", "Ripper demons will now try to leap even if their destination is blocked by terrain", "Red Sentry attacks can now be dodged or blocked, but are very accurate.", "Knockback effects now round up after being halved vs. bosses"),
                            "_Levelgen:_" + list("Adjusted the layout of sacrifice rooms to provide some cover from ranged enemies", "Secret rooms now never affect the generation of items in other rooms", "Items and Enemies can no longer spawn on the Wandmaker quest ritual marker."),
                            "_Items:_" + list("Several artifacts now cancel invisibility when used", "Items no longer spawn on pitfall traps", "Ritual candles now light if they are placed correctly", "Item selectors now always open the main backpack if their preferred bag isn't present", "Quickslot contents are now automatically swapped if a newly equipped item that is not quickslotted replaces an item that was quickslotted. This should make weapon swapping gameplay smoother."),
                            "_Misc:_" + list("Updated the icons for several talents", "Healing no longer interrupts resting when HP is already full", "Updated various code libraries", "Attacking an enemy now properly sets them as the auto-targeting target in all cases")),
                    bugFixes(
                            "_Highlights:_" + list("Various rare crash and freeze errors", "Softlocks caused by the warden using fadeleaf just as they start a boss fight", "Particle effects failing to appear in a bunch of rare cases", "AOE from gladiator's crush move invalidating Dwarf King's 'no weapons' badge", "Magic resistance being extremely effective against Grim traps at low HP", "Allies spawned by some armor abilities getting boosted stats during ascension", "One upgrade being lost when transferring class armor with a warrior's seal attached", "Transmuting a dried rose deleting any items held by the ghost", "Rare cases of hero stacking onto enemies when trying to swap positions with an ally", "Directable allies being easily distracted after being told to move", "Several on-kill effects incorrectly triggering when ghouls get downed"),
                            "_Effects:_" + list("Lethal momentum not triggering on kills made via enchantment",
                                    "Teleportation effects not being blocked by magic immunity",
                                    "Barkskin not reducing damage from things like bombs or the chalice of blood",
                                    "Some armor abilities not checking if targets are out of vision",
                                    "Magical fire not clearing regular fire if they are on the same tile",
                                    "Gladiator being able to riposte enemies who charmed him",
                                    "Iron Stomach talent cancelling fall damage in rare cases",
                                    "Time freeze causing various odd behaviour when triggering plants and traps",
                                    "Rare cases of earthroot armor and hold fast working after movement",
                                    "Volley ability not triggering lethal momentum"),
                            "_Items:_" + list("Darts being lost in rare cases when tipped darts have bonus durability",  "Alchemist's Toolkit not triggering the enhanced rings talent", "Wand of fireblast rarely shooting great distances", "Wand of lightning rarely taking credit for hero deaths not caused by it", "Horn of plenty benefiting from artifact recharging much more than intended", "Shurikens still getting an instant attack after waiting", "Transmutation not turning artifacts into rings if all artifacts have been spawned", "Magic immunity not blocking use of shield battery, cursed artifact effects, or wand recharging", "Cursed items still blocking equipment slots when lost via ankh revive", "Antimagic not reducing damage from enchantments", "Rare cases where cloak of shadows wouldn't spend a charge on activation", "Disarming traps rarely teleporting weapons into chests or graves", "Blacksmith failing to take his pickaxe back in rare cases", "Various rare errors with blacksmith reforging and resin boosted wands"),
                            "_Allies & Enemies:_" + list("DM-300 not using abilities in its first phase in specific situations", "DM-201s rarely lobbing grenades when they shouldn't", "DM-300's rockfall attack very rarely having no delay", "Tengu rarely throwing bombs into walls", "Soiled fist being able to see through shrouding fog", "Rare cases where the Imp's shop could appear without completing his quest", "Gladiator not gaining combo from attacking hiding mimics", "Demon spawners rapidly spawning ripper demons in very specific cases", "Fly swarms often not splitting during ascension challenge", "Rare cases where enemies couldn't be surprise attacked when in combat with allies", "Various rare errors with shock elemental electricity damage", "Evil eyes only resisting some disintegration effects", "Several rare issues with spinner web shooting", "Very rare cases where surprise attacks on mimics would fail", "Very rare pathfinding bugs with flying enemies"),
                            "_UI/VFX:_" + list("Various minor audiovisual errors", "Various minor textual errors", "Items rarely disappearing when hotkeys are used to close the inventory", "Number display errors when device language is set to Arabic", "'i' being incorrectly uppercased to 'I' in Turkish", "Auras from champion enemies being visible in the fog of war for one frame", "Very rare cases where Goo attack particles behaved incorrectly", "VFX rarely not appearing on characters as they are spawned by multiplicity", "Damage warn vfx not accounting for hunger ignoring shielding", "Cases where very fast heroes would cause landmarks to not be recorded", "No error message being given when the mage uses elemental blast without a staff"),
                            "_v2.0.1:_" + list("Various UI bugs caused by pressing multiple buttons simultaneously", "Noisemakers being visually defusable after trigger but not exploding", "Noisemakers being collectable in some cases after triggering","Damage/Stun from blastwave knockback applying to downed ghouls","Even more cases of particle effects sometimes failing to appear", "Projecting champions with ranged attacks refusing to melee from a distance in some cases","Life Link sometimes persisting for longer than intended during Dwarf King fight", "Various rare UI bugs")
                    )
            ),
            new ChangeInfo("From SHPD v1.4", false, SHPX_COLOR,
                    new ChangeButton(new ItemSprite(ARTIFACT_SANDALS), "Equipment",
                        "_Footwear of Nature_:"+list("Footwear of Nature has been redesigned to to use the effect of the most recently fed seed", "Amount of extra seeds and dew reduced", "One additional seed is required per level")
                        +"\n_Enchantments and Curses_:" + list(
                                "Blocking has been redesigned to grant a large amount of shield with a small proc chance, as opposed to always giving a small amount of shield",
                                "Annoying curse buffed, now has additional lines")
                        +"\n_Ring of Wealth_:" + list("Now gives a rare drop every 0-20 kills, up from 0-25", "Now gives an equipment drop every 5-10 rare drops, down from every 4-8", "Equipment drops are now guaranteed to be at least level 1/2/3/4/5/6 at ring level 1/3/5/7/9/11, up from 1/3/6/10/15/21", "Level for equipment drops is now based on the most upgraded equipped wealth ring, and a second one can only boost the level by another +1 at most.")
                        + "\n_Misc_:"+list("Wand of Transfusion damage vs undead scaling doubled (1-2, up from 0.5-1)","Telekinetic grab now grabs all items stuck at a location or stuck to an enemy, rather than just the first one","Scroll of Antimagic now suppresses the positive effects of rings and artifacts while it is applied to the hero")
                    ),
                    misc(
                            "_Highlights_:" + list( "Desktop and mobile landscape users will now see a new hero select screen that makes better use of screen real-estate","Daily runs can now be replayed", "Buff bar now condenses itself if many buffs are visible at once. This raises the limit of on-screen buffs to 15."),
                            "_Hero Actions_:" + list("Waiting always takes exactly 1 turn regardless of hero speed", "Grass the hero is standing on can now be trampled by selecting the hero's position", "Hero now pauses before using stairs if enemies are nearby"),
                            "_Items_:" + list("Wand of Disintegration no longer harms undiscovered neutral characters", "Blooming enchant now attempts to avoid placing grass on the hero", "The scroll holder now can hold arcane resin", "Rotberry plant now gives a small puff of toxic gas when trampled", "Plants now trigger after time freeze ends, just like traps"),
                            "_Allies and Enemies_:" + list("Improved behavior of ally AI when told to hold a position", "Goo's pump up attack now always gives the hero at least one action to react", "DM-300 now knocks back during rockfall even if hero is 1 tile away", "Slightly adjusted enemy stats on ascension to smooth out difficulty"),
                            "_UI/VFX_:" + list("Throwing weapons now show their quantity in orange when one is about to break", "Item boosts from potion of mastery or curse infusion now change the color of text in that item's item slot", "Various minor UI improvements to hero select and rankings", "Added sacrificial fire and demon spawners to the landmarks page", "Added some ascension dialogue for the ghost hero", "Slightly improved the marsupial rat attacking animation", "Improved chains VFX, especially for prison guards", "Added lullaby vfx to sthe stone of deep sleep"),
                            "_Rankings_:" + list("Clarified description for boss score in rankings", "Yog's laser now deducts score even if the hero dodges it", "Goo no longer deducts score by healing in water"),
                            "_Levelgen_:" + list("Items and enemies can no longer spawn in aquarium room water", "Improved room merging logic in a few specific situations"),
                            "_Controls_:" + list("Added a copy and paste button to text input windows", "Adjusted default controller bindings slightly", "The 'switch enemy' keybind also switches tabs on tabbed windows","On desktop, the game now attempts to keep mouse and controller pointer actions in sync", "Added a setting to adjust camera follow intensity", "The controller pointer can now pan the game camera"/*, "Heroes can now be renamed individually"*/)
                    ),
                    // fixme for whatever reason this doesn't work with the tabbed form. The breaking part is the items section. I've instead used the old scrolling style here but it could pop up elsewhere.
                    bugFixes(
                            "_Highlights:_" + list("Victory and Champion badges not being awarded in some cases", "Various rare crash and hang bugs", "Various minor visual/textual errors")
                                    +"\n"+
                                    "_Allies & Enemies:_" + list("Characters rarely managing to enter eternal fire", "Summons from guardian traps counting as regular statues in some cases", "Rare cases where ranged allies would refuse to target nearby enemies", "Various rare cases where characters might stack on each other", "Albino Rats causing bleed when hitting for 0 damage", "Necromancers being able to summon through crystal doors", "Giant necromancers summoning skeletons into doorways", "Goo immediately using pump up attack if previous pump up attack was interrupted by sleep")
                                    + "\n" +
                            "_Items:_" + list("Honeypots not reacting correctly to being teleported", "Rare cases where lost inventory and items on stairs could softlock the game", "Hero armor transferring rarely deleting the warrior's broken seal", "Scrolls of Mirror Image not identifying in rare cases", "Various incorrect interactions between kinetic/viscosity and damage mitigating effects", "Wand of Fireblast sometimes not igniting adjacent items or barricades", "Ring of Furor not affecting Sniper specials", "Cursed rings of force still heavily buffing melee attacks", "Armband not breaking invisibility", "Various quirks with charge spending on timekeeper's hourglass", "Stones of Aggression working more effectively than intended", "Chalice of Blood benefiting from recharging when the hero is starving", "Cases where explosive curse would create explosions at the wrong location", "Cases where spellbook could generate scrolls of lullaby", "Heavy boomerangs getting an accuracy penalty when returning", "Rare consistency errors in potion of might buff description", "Death to aqua blast counting as death to geyser trap", "Reading spellbook not spending a turn if the scroll was cancelled", "Screen orientation changes cancelling scroll of enchantment", "Magical infusion incorrectly clearing curses on wands and rings", "Multiplicity glyph rarely duplicating NPCs", "Rare cases where potion of healing-related talents wouldn't trigger", "Cursed horn of plenty affecting non-food items", "Being able to self-target with cursed wands in rare cases", "Some thrown weapons triggering traps as Tengu jumps", "Magic resistance not applying to some cursed wand effects")
                                    +"\n"+
                            "_Effects:_" + list("Invisibility effects not working when applied to enemies", "Rare cases where giant enemies couldn't attack", "Ratmogrify incorrectly clearing enemy champion buffs", "Exploits where gladiator could build combo on ally characters", "Cases where piranhas could live for a turn on land", "Errors with wild magic and flurry with regard to knockback effects", "Magical sight not making the hero immune to blindness", "Knockback effects paralyzing dead characters", "Caves boss area not displacing all items on the tile that caves in", "Recharging effects sometimes getting an extra action on game load", "Exploits during ascension challenge that let players still use shops", "Elastic and battlemage blast wave on-hit conflicting with each other")
                                    +"\n"+
                            "_Misc:_" + list("Dailies using seeds that are also user-enterable", "Confusing text when a weapon or armor is partly uncursed", "'No Weapons in His Presence' badge not stating that the ring of force counts as a weapon", "Various cases where the friendly fire badge was not being awarded", "Controller axis mapping issues on Android", "Various rare fog of war errors when the hero is knocked a high distance", "Rare cases where items would not correctly appear in the rankings scene", "Prizes from sacrifice rooms not always being the same with the same dungeon seed", "Rare crashes with the radial inventory selector", "Boss health bar not appearing in rare cases", "Buff icons sometimes going outside of character info windows", "Death by necromancer summoning damage not producing a record in rankings", "Some users seeing rankings dates in local formats, instead of international")
                    ))
        },
            {new ChangeInfo("RKA-1.5.3",true,TITLE_COLOR,
                    new ChangeButton(Icons.get(TRASHBOXBOBYLEV), "Developer Commentary",
                            "When this RKPD2 update is going to release..."),
                    new ChangeButton(new Warp(), list(
                            "Warp frequency timer is now set at 25 turns as base and is capped to 3 turns",
                            "Increased Scroll of Infinity Wealth's warp decay speed from 20 to 15",
                            "Increased nerf gun mode swap's warp decay speed from 3 to 1.5",
                            "Decreased nerf gun reload's warp amount from 15 to 10 and decreased decay speed from 2 to 3",
                            "Increased scamming spell's warp decay speed from 3 to 2.5",
                            "Increased wand of kromer's warp decay speed from 8 to 3 and usage warp amount from 20 to 30"
                    )),
                    new ChangeButton(new Kromer(), list(
                            "Buffed wand of kromer's level boost from 3x to 4x",
                            "Kromer Crown and Kromer Mask can no longer be used infinitely via using exploit handler functionality"
                    )),
                    new ChangeButton(new ItemSprite(SEED_SWIFTTHISTLE), "Special seeds", list(
                            "Refactored how special seed info is stored in-game to allow for multiple seed effects to be active at the same time",
                            "Added some new seeds:\n" +
                                    "_--_ AMA-GEN-ERF\n" +
                                    "_--_ BOB-PAL-KIA\n" +
                                    "_--_ BUF-HUN-TRS\n" +
                                    "_--_ COL-LOS-EUM\n" +
                                    "_--_ ROT-INM-IND\n" +
                                    "_--_ RRR-RRR-RRR\n" +
                                    "_--_ VBA-LAN-CED\n" +
                                    "_--_ WHO-LEP-ACK",
                            "_BES-TFR-END:_ mobs are now enthralled with 33% chance instead of 50%",
                            "_EXP-ANS-IVE:_ decreased room count modifier from 4x to 2x",
                            "_NOW-ALL-SHE:_ walls now turn into embers, bookshelves turn into chasms and doors turn into chasms after being closed",
                            "_RAT-RAT-RAT:_ rat enemies now scale their EXP values and max level with game's progression"
                    )),
                    misc(list(
                            "Summon Elemental's spell elementals now follow player between floors",
                            "Blooming Pick no longer can mine blocks on boss floors",
                            "Increased Rat King boss's battlemage mode damage by 25%",
                            "Increased the chance of tier 6 weapons in city from 2% to 3% and in halls from 6% to 7%",
                            "Removed bonus initial charges from wand of His Ratiness and wand of Kromer, but they now charge 33% faster",
                            "Enthralled enemies now have 25% increased accuracy, evasion and armor"
                    )),
                    bugFixes(list(
                            "Fixed nerf gun's necromantic imbue triggering on bosses and minibosses"
                    ))
            )
            },
            {new ChangeInfo("RKA-1.5.2",true,TITLE_COLOR,
                    new ChangeButton(Icons.get(TRASHBOXBOBYLEV), "Developer Commentary",
                            "I wanted to wait until RKPD2 actually updates, but it is been almost 1.5 years since that, and players really want some updates..."),
                    new ChangeButton(new Warp(), list(
                            "Massively buffed the rate at which warp effects appear",
                            "Reduced the preparation delay for warp sources",
                            "Reduced the effect of decay averaging out from multiple warp sources",
                            "Doubled the warp decay rate of flex tape's both abilities"
                    )),
                    new ChangeButton(new Kromer(), list(
                            "Added _Kromerized Bomb_, which combines most of other Shattered bombs in one explosion",
                            "Added _Pipisfusion_ spell, which doubles the damage of selected melee weapon, but makes it inflict warp from each hit",
                            "Changed _Kromer-infused Staff_: now does slightly more melee damage, but inflicts warp from zapping wands",
                            "Changed _Kromer-infused Cloak_: now is compatible with every Cloak of Shadows interaction, but inflicts more warp and its decay rate depends on how long each charge lasts",
                            "Nerfed _Kromer-infused Bow_: now inflicts massively more warp with each shot",
                            "Changed _Kromer-infused Slingshot_: now does 16% more damage, but inflicts much more warp with each shot",
                            "Nerfed _Wand of Kromer_: now inflicts massively more warp with each zap",
                            "Buffed _Scam Spell_: now inflict Scam for longer, increased the yield per craft to 11, but increased energy cost to 22"
                    )),
                    misc(list(
                            "Nerfed cryogenic imbue of nerf gun by making its freezing last shorter and making Rat King boss immune to frost",
                            "Nerfed dreamful imbue of nerf gun by making bosses resist it like grim enchantment",
                            "Nerfed slightly max charge of normal and fast modes for nerf gun",
                            "Improved Spirit Caller's tracker's visuals",
                            "Buffed Like a Bullet talent: increased starting power from +39%/+46%/+55% to +45%/+58%/+71% and increased distance multiplier from +17%/+21%/+25% to +18%/+23%/+28%",
                            "Made Chaotic curse be considered a curse instead of enchantment",
                            "Made Paladin champion effect apply to more ranged zombies",
                            "Added action names to RKA's abilities that didn't have them",
                            "Animosity Mode's Goo now spawns only one slime per hit, not 4-5 (also fixes the freeze connected to too many slimes spawning)",
                            "Updated Android target version to API 33"
                    )),
                    bugFixes(list(
                            "Fixed Energizing Upgrade not activating for various reasons",
                            "Fixed Spirit Caller's tracker consuming all of charge when summoning a wraith",
                            "Fixed a crash from Abyssal Nightmare trying to break walls close to edge of the level",
                            "Fixed a crash with some allies trying to use hero's enchantments",
                            "Fixed most of Kromer Crown's and Kromer Mask's broken interactions",
                            "Fixed rare crash with Evil Eyes splitting",
                            "Fixed Kromer Mask's missing strings",
                            "Fixed Degrading Health's tracker crashing the game on reloading",
                            "Fixed Kromer Economy's 180 turns preparation delay disappearing on reloading the game",
                            "Fixed melee weapons being possible to quickslot outside of Brawler's subclass",
                            "Fixed some cases of Unenchanted's no upgrades effect not applying to drops when it should"
                    ))
            ),
                    new ChangeInfo("RKA-1.5.1",true,TITLE_COLOR,
                            new ChangeButton(Icons.get(TRASHBOXBOBYLEV), "Developer Commentary",
                                    "The unexpected events (such as university and my PC being broken for 2 months) made this update arrive very-very late, sorry about that."),
                            new ChangeButton(Icons.get(TALENT), "Talent and Armor Ability Changes", list(
                                    "_Greenfields_ buffed by empowering regeneration boost and removing satiety boost (that was working incorrectly anyways)",
                                    "_Archery's Mark_ fixed by tweaking the cause for softlock",
                                    "_Ectotoughness_ reworked into _Auxiliary Charge_ and rewards player with charge on using wands"
                            )),
                            new ChangeButton(new WraithSprite(), "Spirit Caller Changes", list(
                                    "Ectotoughness's part is now innate _+33%_ evasion boost",
                                    "Allied Wraiths now have 1.5x attack speed",
                                    "Tweaked the charge necessary for summoning from 40 to 45 but allowed to accumulate it to max of 90 charge"
                            )),
                            new ChangeButton(new NerfGun(),
                                    "Significantly nerfed nerf gun:\n" + list(
                                            "Reduced minimal and maximal damage",
                                            "Increased amount of warp from each reload and imbuing",
                                            "Also tweaked the lack of level indicator"
                                    )
                            ),
                            new ChangeButton(new Warp(), "Reevaluated some aspects of warp effect: \n" + list(
                                    "Incoming sources of warp now average out decay rate with previous source of warp instead of overriding it, to prevent the common strategy of mitigating it",
                                    "Warped enemy's retaliation damage now depends on damage it have been taking instead of its maximum HP",
                                    "Increased the frequency of warp effects on higher levels of warp",
                                    "Spawning effect no longer softlocks the game when on empty boss floor"
                            )),
                            misc(list(
                                    "Added the tips on how to unlock new challenges",
                                    "Restored the ability to metamorph talents granted by kromer",
                                    "Abyss now plays the music of halls instead of what played before",
                                    "Ring of Force's description is a bit more clear on its custom effects",
                                    "Restored UCE-Handler functionality"
                            )),
                            bugFixes(list(
                                    "Fixed the lack of kromer-granted subclasses and classes in rankings",
                                    "Fixed the occasional consumption of Phantom Spear when thrown",
                                    "Fixed Crowd Diversity-unaffected monsters still getting buffs",
                                    "Fixed duping of aqua blasts in Domain of Hell runs"
                            ))
                    ),
                    new ChangeInfo("RKA-1.5.0",true,TITLE_COLOR,
                            new ChangeButton(Icons.get(TRASHBOXBOBYLEV), "Developer Commentary", "This is long-awaited update to Rat King Adventure.\n\n" +
                                    "Not everything was caught in playtesting and such and therefore here are plans for future patches:\n" +
                                    "_-_ Add more special seeds\n" +
                                    "_-_ Watch over balance of newer talents\n" +
                                    "_-_ Adjust the values of warp and warp effects\n" +
                                    "_-_ Add more kromer-related content\n" +
                                    "_-_ Fix feature regressions"),
                            new ChangeButton(new Warp(), "This new status effect replaces buffs and debuffs inflicted by Kromer-created items and will be used for related items and content in the future.\n" +
                                    "#Warp# can be obtained by interacting with #otherworldy items# that do not belong in usual Pixel Dungeon setting. When inflicted, it will start decaying, while triggering various negative effects on the way.\n\n" +
                                    "Warp effects depend on how much warp you have collected, but the max you can carry is 150.\n\n" +
                                    "Don't accumulate too much warp or else you will have to face the consequences!"),
                            new ChangeButton(new ItemSprite(SEED_SWIFTTHISTLE), "Special seeds", "When entering specific codes into custom seed input, you will get random seed playthrough but with some twist along the way.\n\n" +
                                    "Those seeds include (in current patch):\n" +
                                    "_-_ RAT-RAT-RAT\n" +
                                    "_-_ ROG-UEB-UFF\n" +
                                    "_-_ REV-ERS-EED\n" +
                                    "_-_ NOW-ALL-SHE\n" +
                                    "_-_ RNG-ITE-MSS\n" +
                                    "_-_ ITE-MCH-EST\n" +
                                    "_-_ EXP-ANS-IVE\n" +
                                    "_-_ BES-TFR-END\n" +
                                    "_-_ ECH-ECH-ECH"),
                            new ChangeButton(Icons.get(TALENT), "Talent and Armor Ability Changes", list(
                                    "_Combo Meal_ buffed by giving 1 more combo.",
                                    "_Bravery_ buffed by giving 25% more rage at all points",
                                    "_Cockatrocious_ buffed by extending the duration of petrification",
                                    "_Big Time_ fixed working in more champion titles than it should",
                                    "_Adapt and Overcome_ fixed debug message for +3 effect",
                                    "_Archery Mark_ no longer targets invulnerable enemies"
                            )),
                            new ChangeButton(new ItemSprite(MASK), "Subclass Changes", list(
                                    "_Spirit Caller_ buffed by increasing the ability charge rate",
                                    "_Shadowflare_ nerfed by reducing vehicle's speed from 3x to 2x",
                                    "_Omnibattler_'s Paladin form now only provides 75% damage reduction, down from 100%",
                                    "_Omnibattler_ buffed by decreasing title change cooldown"
                            )),
                            new ChangeButton(new NerfGun(),
                                    "This new weapon can be found in crystal chests and is capable of shooting three different kinds of ammo.\n" +
                                            "It requires reloading once ammunition is exhausted.\n\n" +
                                            "Nerf Gun also can use seeds to get unique enchantments that persist until the gun is reloaded.\n" +
                                            "And finally, this weapon grows with your progression in the dungeon, using experience."
                            ),
                            new ChangeButton(new FlexTape(),
                                    "This curious item can be bought in shops or be found in Shattered v1.2 new rooms and has various uses, including instantly killing enemies and healing allies.\n" +
                                            "Unfortunately, it is quite cursed and will give warp on using."
                            ),
                            new ChangeButton(new ItemSprite(ALUMINUM_SWORD), "Weaponry Changes", list(
                                    "_Aluminum Sword_ reworked to use vanilla Gladiator perk, with reduced damage",
                                    "_Runic Blade MK2_ buffed to recharge its magic faster, deal more damage and inflict magic dispel with magical swords",
                                    "_Construction Wand_ buffed with increased durability of its summons",
                                    "_Luminious Cutlass_ nerfed with reduced damage and weaker lightning effect",
                                    "_Blooming Pick_ buffed with significant buff to its damage and mining delay",
                                    "_Elemental Dirk_ buffed with significant buff to its damage, its debuffs are also no longer depend on chance",
                                    "_Exo Knife_ reworked to deal increased damage in 5x5 instead of recursive hits",
                                    "_Homing Boomerang_ nerfed to lose damage after each bounce and no longer being able to hit highly evasive or invulnerable enemies",
                                    "_Phantom Spear_ fixed to remove durability-related issues",
                                    "_Terminus Blade_ nerfed with reduced damage, attack speed, but buffed ability's charge rate"
                            )),
                            new ChangeButton(new Kromer(), list(
                                    "All kromer items inflict various amount of warp",
                                    "Infinity Wealth adjusted to only give 50% EXP from spawned mobs, but is now able to upgrade thrown weapons and preserve enchantments",
                                    "Infinity Manpower buffed to grant more strength and no longer damage the player",
                                    "No Death Elixir buffed to last 250 turns",
                                    "Kromer Crown buffed to be usable with Kromer Mask"
                            )),
                            misc(list(
                                    "Exploding champions will explode into arcane bomb instead of using several bombs",
                                    "Paladin champions give 75% damage reduction to their allies instead of full invulnerability",
                                    "Increased effect cap on certain rings",
                                    "Chaotic curse now uses same visual effect as kromer items",
                                    "Reworked Chemical Barrier challenge to replicate older alchemy style",
                                    "Buffed some of Animosity Mode bosses",
                                    "Significantly buffed Soul of Yendor's charge efficiency and implemented new Armband for it",
                                    "Phantoms no longer inflict DoT debuffs with their attacks",
                                    "Increased Rat King boss HP by 50%"
                            )),
                            bugFixes(list(
                                    "Fixed the crash with using brawler with bare hands",
                                    "Fixed DK being faster outside of Animosity",
                                    "Fixed abyss trappers being able to override exit stairs",
                                    "Fixed possible crash with trap classes in Abyss",
                                    "Fixed Rat King not getting points from kromer talents",
                                    "Fixed the increased HP of voodoo champion's offspring",
                                    "Fixed the bug with Aqua Blasts being given on each level transition"
                            ))
                    )
            },
        { // v1.0.0
            new ChangeInfo("v1.0.0",true,TITLE_COLOR,
                    info(Messages.get(RKPD2Changes.class, "100")),
                    new ChangeButton(new ItemSprite(ItemSpriteSheet.SHORTSWORD, new ItemSprite.Glowing(0x000000)), "New Curse!", "In addition to the new curses added in Shattered v1.3, I've added an RKPD2-exclusive curse:\n\n_Chaotic_ weapons will usually roll a random weapon curse on hit, but every once in a while it'll roll a wand curse!"),
                    new ChangeButton(new ItemSprite(CLEANSING_DART), "Vetoed Sleep Dart Removal",
                            "In Shattered v1.2, Evan came up with the brilliant idea of nerfing _sleep darts_ into the ground. To avoid suspicion, he also made _dreamfoil_ unable to inflict magical sleep, so as to appear to be fair."
                                    + "\n\nIn Shattered v1.3, it became clear that this was a horrible mistake---dreamfoil not being able to inflict sleep proved incredibly confusing to players, and the 'nerfed' sleep darts---now cleansing darts---were almost completely useless.\n\nHowever, Evan was unwilling to go back, and instead retconned Dreamfoil, renaming it to Mageroyal without changing its description at all. He also buffed what were now cleansing darts to have certain 'sleep dart-like' properties, while keeping them almost completely useless for what we really wanted to use them for.\n\nFEAR NOT, HOWEVER!\n\nNot only does dreamfoil still inflict magical sleep in this game blessed by the King himself, RAT KING in his infinite wisdom has decided to enhance the sleep dart into Dream™ darts!"),
                    new ChangeButton(Icons.get(TALENT), "Talent and Armor Ability Changes",
                            "Several talents that were previously exempt from being chosen by the scroll of metamorphosis now have alternative effects that let them be used by any hero:"+list("Light Cloak", "Noble Cause", "Restored Willpower", "Energizing Upgrade","Mystical Upgrade","Restoration","Seer Shot","Light Cloak")
                                    +"\nBuffs from Shattered v1.2:"+list("_Energizing Upgrade_/_Restoration_ charge boost up to 4/6, from 3/5", "_Power Within_ wand preserve chance at +1 reduced to 50%, but now grants 1 arcane resin if it fails to preserve. Max uses increased to 5.", "_Rat Magic_'s empowering scrolls now gives +3 on the next 1/2/3 wand zaps", "_Timeless Running_ light cloak charge rate boosted to 25/50/75%, from 17/33/50%.")
                                    +"\nNerfs from Shattered v1.2:"+list("_Restoration_ Shield Battery nerfed to 4%/6%", "_Shield Battery_ nerfed to 6/9%", "_Inevitability_ Enraged Catalyst proc boost now 15/30/45%, standard enraged catalyst unchanged.", "_Inevitability_ max rage boost now +10%/+20%/+30%") + list("_Wand Preservation_ chance to preserve at +1 reverted to 67% from 50%, still grants 1 arcane resin if it fails to preserve")),
                    new ChangeButton(new ItemSprite(CROWN), "Armor Abilities",
                            list("_Endure_ damage bonus increased to 1/2 of damage taken from 1/3")
                                    +list("_Wild Magic_ base wand boost and max boost increased by 1","_Fire Everything_ now has a 25% chance per point to let a wand be usable 3 times","_Conserved Magic_ no longer lets a wand be usable 3 times, now grants a chance for wild magic to take 0 turns instead")
                                    +list("_Elemental power_ boost per point up to 33%, from 25%, to match Shattered's buff from 20% to 25%","_Reactive Barrier_ shielding per point up to 2.5, from 2, and max targets now increases by 1 per point.")
                                    +list("_Shadow Clone_ now costs 35 energy, down from 50. Initial HP down to 80 from 100","_Shadow Blade_ damage per point up to 8%, from 7.5%","_Cloned Armor_ armor per point down to 16%, from 20%, to match Shattered's nerf from 15% to 12%.")
                                    +list("_Eagle Eye_ now grants 9 and 10 vision range at 3 and 4 points", "_Go for the Eyes_ now cripples at ranks 3 and 4","_Swift Spirit_ now grants 2/4/6/8 dodges, up from 2/3/4/5")
                                    +list("_Heroic Leap_ energy cost up to 35 from 25", "_Body Slam_ now adds 1-4 base damage per point in talent", "_Impact Wave_ now applies vulnerable for 5 turns, up from 3", "_Double Jump_ energy cost reduction increased by 20%")
                                    +list("_Smoke Bomb_ energy cost up to 50 from 35, but max range up to 12 from 8 (Shattered buffed it from 6 to 10)")
                                    + "\nThis has led me to adjust _Rat King's Wrath:_"
                                    +list("Energy cost up to 70 from 60", "Range up to 10 from 6", "_Rat Blast_ boost per point up to 25% from 20%", "_Rat Blast_ shielding per point up to 2.5, from 2, and max targets now increases by 1 per point.")
                                    +list("_Ratforcements_ stat-scaling adjusted to take into account ascension challenge")
                    ),
                    misc(list("Ability to run challenges is now tied to unlocking rat king.")
                            + "\n\nFrom Shattered v1.3:"
                            // buff and spell icons
                            +list("All buffs now have a unique image, even if it is just a recolor.", "A few new overhead spell icons have been added as well")
                            +list("Characters with guarenteed dodges (e.g.) spirit hawk) can now evade Yog's laser")
                            +list("Minor visual improvements to the amulet scene")
                            // misc
                            +list("Updated various code dependencies", "Made major internal changes in prep for quest improvements in v1.4", "Added a slight delay to chasm jump confirmation window, to prevent mistaps", "Progress is now shown for badges that need to be unlocked with multiple heroes", " Multiple unlocked badges can now be shown at once", "Various minor tweaks to item and level generation to support seeded runs", "Keys now appear on top of other items in pit rooms", "Large floors now spawn two torches with the 'into darkness' challenge enabled", "Blazing champions no longer explode if they are killed by chasms", "Red sentries no longer fire on players with lost inventories")
                            + "\n\nFrom v1.2:"
                            + list("Improved blinking behavior of journal", "Improved depth display to include level feelings", "Added challenge indicator", "Secrets level feeling less extreme in hiding things", "Improved save system resilience")
                    ),
                    bugFixes(
                            // rkpd2
                            list("Dwarf King's health being handled incorrectly in Badder Bosses",
                                    "Thief's Intuition having incorrect mechanics; now has 33% chance to id curse at +1, rather than 50% chance at +0.", "Text at the very top and very bottom of scrollpanes being cut off.")
                            //v1.2
                            +list("Very rare cases where dried rose is unusable", "Corruption affecting smoke bomb decoy", "Character mind vision persisting after a character dies", "Dwarf King not being targeted by wands or thrown weapons while on his throne", "Floor 5 entrance rooms sometimes being smaller than intended", "Exploits involving Corruption and Ratmogrify", "Rare cases where lullaby scrolls were generated by the Unstable Spellbook", "Red flash effects stacking on each other in some cases", "Game forgetting previous window size when maximized and minimized", "Various rare cases of save corruption on Android", "Various minor textual and visual errors", "Unidentified wands being usable in alchemy", "Various rare cases where the hero could perform two actions at once", "Pharmacophobia challenge incorrectly blocking some alchemy recipes", "Various rare cases where giant enemies could enter enclosed spaces", "Rare cases where the freerunner could gain momentum while freerunning", "On-hit effects still triggering when the great crab blocks", "Various bugs with the potion of dragon's breath", "Assassinate killing enemies right after they were corrupted by a corrupting weapon", "Layout issues with the loot indicator", "Artifact recharging not charging the horn of plenty in some cases when it should", "Some items rarely not being consumed when they should be", "Fog of War not properly updating when warp beacon is used")
                            //v1.3
                            +list("Various minor textual and visual bugs", "Final boss's summons being slightly weaker than intended when badder bosses is enabled", "Great crab not blocking right after loading a save", "Exploits that could force DM-300 to dig outside of its arena", "Various 'cause of death' badges not being awarded if death occurred with an ankh.", "Wraiths from spectral necromancers not always dying when the necromancer dies", "The mystical charge talent giving more charge than intended", "Ring of might HP bonus not applying in specific cases", "Stones of blink disappearing if they fail to teleport", "Beacon of returning not working at all in boss arenas", "Earthen guardian not being immune to poison, gas, and bleed", "Transmogrified enemies awarding exp when the effect ends", "Gateway traps being able to teleport containers")
                    )
            ),
            new ChangeInfo("From SHPD v1.2,v1.3", false, SHPX_COLOR,
                    new ChangeButton(Icons.get(Icons.SEED), "Seeded Runs!",
                            "_It's now possible to enter a custom seed when starting a new game!_\n\n" +
                                    "Seeds are used to determine dungeon generation, and two runs with the same seed and game version will produce the exact same dungeon to play though.\n\n" +
                                    "If you don't enter a custom seed, the game will use a random one to generate a random dungeon, just like it did prior to this update.\n\n" +
                                    "Note that only players who have won at least once can enter custom seeds, and games with custom seeds are not eligible to appear in rankings."
                                    + "\n\n"
                                    + "_Daily runs_ have also been implemented. Each day there is a specific seeded run that is available to all players, making it easy to compete against others."
                    ) {{ icon.hardlight(1f, 1.5f, 0.67f); }},
                    new ChangeButton(BadgeBanner.image( Badge.HIGH_SCORE_2.image ), "Ascension and New Score System!",
                            "_The game's scoring system has been overhauled to go along with seeded runs and dailies!_\n\n"
                                    + "The score system now factors in a bunch of new criteria like exploration, performance during boss fights, quest completions, and enabled challenges. This should make score a much better measure of player performance.\n\n"
                                    + "A score breakdown page has also been added to the rankings screen. This page even works for old games, and retroactively applies the challenge bonus!"
                                    +"\n\n" +
                                    "_A bunch of adjustments have been made to the ascension route to make it a proper challenge!_\n\n"
                                    + "Enemies will get much stronger as you ascend, and it's impossible to teleport back up or flee and avoid all combat. Expect to have to work a little bit more for an ascension win!"),
                    new ChangeButton(Icons.get(DISPLAY_LAND), "UI/UX improvements", ""
                            +list("new main UI for larger displays", "Full controller support, better keyboard controls, better mouse support", "Two additional quickslots")
                            +list("Boss health bars have been expanded to show current health and active buffs/debuffs", "Changes scene expanded on large enough displays.")
                            +list("Boss music implemented", "Badge changes implemented from Shattered")
                            +"\n"
                            +list("The settings menu has been adjusted with a few new and rearranged options.","Added radial menus for controller users, and redid default controller bindings.","Keyboard and controller key bindings now have separate windows", "Added a few new key/button bindings actions", "Default 'Next Special Ability' keybind is now F")),

                    // this is the merge of 3 different shpd stuff
                    new ChangeButton(BadgeBanner.image( Badge.BOSS_CHALLENGE_5.image ), "Implemented Badge Changes",
                            "_Badges now have names, and 21 new badges have been added!_"
                                + "\n"
                                + list("8 of these new badges are mostly part of the existing series badges (e.g. defeat X enemies), and exist around the gold badge level.","Five of these badges are 'high score' badges, meant to tie into the new score system.", "Another five of these badges are 'boss challenge' badges, which each require you to defeat a boss in a particular way", "Four new 'cause of death' badges have also been added, which should be a little trickier than the existing ones.")
                                + "\n"
                                + "Several of these badges are on the harder end, in particular the final high score and boss challenge badge should be a real challenge, even for veteran players."
                                + "\n\n"
                                + "The 'games played' badges have also been adjusted to unlock either on a large number of games played, or a smaller number of games won."
                    )
            ),
            new ChangeInfo("From SHPD v1.2,v1.3", false, SHPX_COLOR,
                new ChangeButton(new Image(Assets.Environment.TILES_SEWERS, 48, 80, 16, 16), "Levelgen and Enemies", "Implemented new special rooms from Shattered v1.2:" + list("sacrifical fire room", "crystal path rooms", "crystal choice rooms", "sentry room", "magical fire room", "toxic gas room")
                        +"\nFloor 16's spawn rates have been adjusted to smooth over a difficulty spike on that floor" + list("Ghouls up to 60% from 40%","Elementals down to 20% from 40%")
                        + "\nOther changes:"+list("_Soiled Fist_ is now immune to burning, but the grass it generates still burns", "_Burning Fist_ is now immune to freezing, but it can still be chilled", "_Rotting and Rusted Fists_ now take less damage from retribution, grim, and psionic blast")
                ),
                new ChangeButton(new ItemSprite(ARTIFACT_ARMBAND), "Armband", "Armband reworked, now lets you steal from enemies as well as shops."
                ),
                new ChangeButton(new ItemSprite(ItemSpriteSheet.SHORTSWORD, new ItemSprite.Glowing(0x000000)), "Curse Redesigns",
                        "Weapon and Armor curses have been redesigned:"
                                +list("_Fragile_ has been replaced by _explosive,_ which builds power and then explodes!","_Wayward_ has been redesigned to sometimes apply an accuracy reducing debuff, instead of always reducing accuracy.","_Exhausting_ has been replaced by _dazzling,_ which can blind both the attacker and defender.")
                                +list("_Anti-Entropy_ now spreads less fire to the player, and freezes all adjacent tiles instead of just the enemy.", "_Sacrifical_ now more heavily scales on current HP, bleeding for a bit more at high health, and very little at medium to low health.")),
                new ChangeButton(new ItemSprite(ItemSpriteSheet.CLEANSING_DART), "Alchemy", "Buffs from v1.3:"
                        + list("_Woolly Bombs_ now summon sheep for 200 turns, or 20 turns during boss fights, up from 12-16 turns. However, sheep no longer totally prevent bosses from summoning minions.")
                        +list("_Rot Dart_ uses increased to 5 from 1","_Shocking Dart_ damage now slightly scales with depth", "_Poison Dart_ damage scaling increased", "_Displacing Dart_ now more consistently teleports enemies away", "_Holy Dart_ now heavily damages undead or demonic enemies, instead of blessing them", "_Adrenaline Dart_ now cripples enemies for 5 turns, instead of giving them adrenaline")
                        +"\nBuffs from v1.2:"
                        +list("_ Bomb Recipe_ energy costs down across the board\n", "_ Infernal, Blizzard, and Caustic Brew_ energy costs down by 1\n", "_ Telekinetic Grab_ energy cost down to 2 from 4, liquid metal cost reduced to 10 from 15", "_ Phase Shift_ energy cost down to 4 from 6", "_ Wild Energy_ energy cost down to 4 from 6", "_ Beacon of Returning_ energy cost down to 6 from 8", "_ Summon Elemental_ energy cost down to 6 from 8", "_ Alchemize_ energy cost down to 2 from 3")
                        + list("_ Scroll of Foresight_ duration up to 400 from 250", "_ Scroll of Dread_ now grants 1/2 exp for defeated enemies", "_ Potion of Shrouding Fog_ gas quantity increased bt 50%\n", "Items and effects which create water now douse fire")
                        +list("_Caustic Brew_ damage per turn increased by 1", "_ Infernal and Blizzard Brew_ now start their gas in a 3x3 AOE", "_ Shocking Brew_ AOE up to 7x7 from 5x5\n", "_ Phase Shift_ now stuns whatever it teleports", "_ Summon Elemental_ quantity up to 5 from 3, elemental's stats now scale with depth, and elementals can be re-summoned", "_ Aqua Blast_ now acts like a geyser trap, quantity down to 8 from 12", "_ Reclaim Trap_ quantity up to 4 from 3", "_Curse Infusion_ now boosts highly levelled gear by more than +1, quantity up to 4 from 3.", "_Recycle_ quantity up to 12 from 8, cost up to 8 from 6")
                        +"\nNerfs from v1.2:"
                        +list("_Magical Infusion_ energy cost up to 4 from 3", "_Holy Bomb_ bonus damage reduced to 50% from 67%", "_Goo Blob and Metal Shard_ energy value reduced to 3", "_Alchemize_ quantity in shops reduced by 1")
                )
            )
        },
    }),
    v0(() -> new ChangeInfo[][]{
        {
                new ChangeInfo("1.4.14", true, TITLE_COLOR, "Renamed from DLC into Rat King Adventure (RKA)",
                        bugFixes(list(
                                "Fixed Homing Boomerang hitting invulnerable targets.",
                                "Fixed the depth 25 duplication and item duplication glitches connected it.",
                                "Fixed Domain of Hell bugs, added Aqua Blasts for each depth.",
                                "Fixed Rat King's Battlemage form crashes.",
                                "Fixed glitches connected to RK boss badge.",
                                "Fixed Scout's Barrier having old Nature's Aid effect.",
                                "Fixed targeted cell VFX."))
                ),
                new ChangeInfo("DLC-1.4.1X", true, TITLE_COLOR, "",
                        new ChangeButton(new ItemSprite(ARMOR_RAT_KING), "RKPD Changes",
                                list(2,
                                        "Successfully ported RKPD2 0.3.0. Have fun with metamorph!")),
                        new ChangeButton(new ItemSprite(KROMER_CROWN), "Second subclass",
                                "_-_ You can sacrifice DK's crown to get a second subclass instead of armor ability.\n\n" +
                                        "_-_ Some subclasses are blacklisted and 3 are randomly chosen."),
                        new ChangeButton(new ItemSprite(KROMER_MASK), "Second class",
                                "_-_ You can use Tengu's mask with kromer to get a second class instead of subclass.\n\n" +
                                        "_-_ 2 are randomly chosen."),
                        misc(list(
                                "Arcane Resin has no limit for level.",
                                "Nerfed Protein Infusion by 33%.",
                                "Nerfed Trapper's Mastery: no longer reclaims traps, decreased cooldown to compensate.",
                                "Kromer follows the same rules as Scroll of Metamorphosis.",
                                "Doubled Big Rush's bonus damage, but made it roll from 0 to value.",
                                "t4 hero-specific talents are listed ahead of armor ability talents and can be metamorphed.",
                                "Buffed Durable Projectiles by a 25%.",
                                "Added brawler special to Spirit and Kromer bows.",
                                "Shadowflare has unique sprites for each class.",
                                "Added anticheese to Kromer Crown.")),
                        bugFixes(list( "Fixed Abyssal Nightmare crashes.",
                                "Fixed Domain of Hell crashes.",
                                "Fixed certain special action crashes.",
                                "Fixed Shadow Clone crashes.",
                                "Fixed crash related to In My Memories",
                                "Fixed crash with Shadowflare",
                                "Fixed crash with Terminus Blade's transmutation.",
                                "Fixed crash with Studded Gloves's brawler special.",
                                "Fixed missing items on Unenchanted.",
                                "Fixed 0 HP enemies on Crowd Diversity.",
                                "Fixed Archery Mark crashes.",
                                "Fixed old toolkit limitation on enemy presence for Soul of Yendor."))
                ),
        },
        {// v0.3
            new ChangeInfo("v0.3",true, TITLE_COLOR),
            NewContent(
                info(Messages.get(RKPD2Changes.class, "030")), // trying something different with this.
                new ChangeButton(new ItemSprite(ARMOR_RAT_KING), "New Rat King Armor Ability!", "_Omni-Ability_ is an armor ability generator. Every time you use it, it will generate a new armor ability for you to use out of the existing ones. It's currently a tad unwieldy (especially with abilities that summon things) and more than a little bit confusing, but it should finally give Rat King what many have been waiting for: access to every armor ability without exception in one slot!")
            ),
            new ChangeInfo("From SHPD v1.1", false, SHPX_COLOR, "",
                new ChangeButton(new ItemSprite(ARTIFACT_TOOLKIT), "Alchemy and Artifacts", "Implemented SHPD's alchemy rework:" + list("Energy is now a resource the player carries around.", "Less energy is provided naturally, but consumables can be converted into energy") + "\nChanges to Exotics:" + list("Exotics now require energy instead of seeds or stones","Potion of Holy Furor is now _Potion of Divine Inspiration_, which gives bonus talent points.", "Potion of Adrenaline Surge is now _Potion of Mastery_, which reduces the strength requirement of one item by 2."/*\n*/, "Scroll of Petrification is now _Scroll of Dread_, which causes enemies to flee the dungeon entirely.", "Scroll of Affection is now _Scroll of Siren's Song_, which permanently makes an enemy into an ally.", "Scroll of Confusion is now _Scroll of Challenge_, which attracts enemies but creates an arena where you take reduced damage.", "Scroll of Polymorph is now _Scroll of Metamorphosis_, which lets you swap out a talent to one from another class (including Rat King, though he is somewhat less common).") + "\nSpells:" + list("Added _Summon Elemental_ and _Telekinetic Grab_.", "_Alchemize_ reworked, replaces Merchant's Beacon and can also convert consumables to energy.", "Removed _Magical Porter_") + "\nExotic Buffs:" + list("_Potions of Storm Clouds, Shrouding Fog, and Corrosion_ initial gas AOE up to 3x3 from 1x1","_Potion of Shrouding Fog_ now only blocks enemy vision","_Potion of Corrosion_ starting damage increased by 1","_Potion of Magical Sight_ vision range up to 12 from 8","_Potion of Cleansing_ now applies debuff immunity for 5 turns\n","_Scroll of Foresight_ now increases detection range to 8 (from 2), but lasts 250 turns (from 600)","_Scroll of Prismatic Image_ hp +2 and damage +20%") + "\n\n_Artifact Changes_:" + list("Energy required to level up _Alchemist's Toolkit_ halved, kit can now be levelled and used anywhere", "Toolkit warmup is now based on time, and gets faster as it levels up\n", "The _Horn of Plenty_  now has a 'snack' option that always consumes 1 charge.", "To counterbalance this, the total number of charges and charge speed have been halved, but each charge is worth twice as much as before.\n", "_Dried Rose_: Ghost HP regen doubled, to match the rose's recharge speed (500 turns to full HP)")),
                misc(list("Added Shattered's new music tracks.") + list("Item drops and special room spawns are now more consistent, and getting loads of the same item is now much less likely.", "Items present on boss floors are now preserved if the hero is revived via unblessed ankh.", "Teleport mechanics now work on boss levels.","Traps that teleport no longer work on items in chests or similar containers", "Rewards from piranha and trap rooms now always appear in chests") + list("Tipped darts can now be transmuted and recycled", "Thrown weapons no longer stick to allies", "Liquid metal production from upgraded thrown weapons now caps at +3") + list("Updated game icons on Android and Desktop platforms to match Shattered's new ones.","Tabs in rankings and hero info windows now use icons, not text" ,"'potions cooked' badge and stats are now 'items crafted'") + list("Newborn elementals no longer have a ranged attack")),
                bugFixes(list("Specific cases where guidebook windows could be stacked.", "Remove curse stating nothing was cleansed when it removed the degrade debuff","Various minor/rare visual and textual errors","Cases where pausing/resuming the game at precise moments would cancel animations or attacks","Endure damage reduction applying after some specific other damage-reducing effects","Unblessed ankh resurrection windows disappearing in some cases","Lucky enchantment rarely not trigger in some cases","Artifacts spawning upgraded from golden mimics", "Unblessed ankh revival cancelling corpse dust curse","Unstable spellbook letting the player select unidentified scrolls", "Desktop version not working correctly with FreeBSD","Liquid metal being usable on darts","Teleportation working on immovable characters in some cases","Various quirks with thrown weapon durability","Rare cases where ghouls would get many extra turns when reviving", "Magical infusion not preserving curses on armor","Vertigo and teleportation effects rarely interfering","Layout issues in the hero info window with long buff names","Cursed wands being usable to create arcane resin","Unblessed ankh revival rarely causing crashes or placing the player on hazards","Some glyphs not working for armored statues or the ghost hero","Various oddities with inferno gas logic","Spirit bow having negative damage values in rare cases","Artifact recharging buff working on cursed artifacts","Scrolls of upgrade revealing whether unidentified rings/wands were cursed","Ring of Might not updating hero health total in rare cases","Specific cases where darts would not recognize an equipped crossbow","Cap on regrowth wand being affect by level boosts","Some on-hit effects not triggering on ghost or armored statues", "Rare errors when gateway traps teleported multiple things at once","Various rare errors when multiple inputs were given in the same frame", "Fog of War errors in Tengu's arena","Rare errors with sheep spawning items and traps")),
                new ChangeButton(new ItemSprite(MAGES_STAFF), "Heroes","Note that both Rat King and the corresponding class are affected by SHPD buffs unless indicated otherwise.\n" + list("Rage now starts expiring after not taking damage for 2 turns, rather than instantly. This should make it easier to hold onto rage during combat.") + list("Staff damage reduced to 1-6, from 1-7.", "Preparation bonus damage reduced, is now 10/20/35/50 instead of 15/30/45/60.") + "\n" + list("_Wild Magic_ Charge cost reduced to 25, from 35.", "_Spirit Hawk_ Duration up to 100 turns, from 60.\n", "_Empowering Scrolls_ (and the variant provided by _Rat Magic_) now lasts for 2 wand zaps, up from 1.", "_Timeless Running_'s Light Cloak aspect now grants 16.6% charge speed per rank, up from 13.3%", "_Shrug it Off_ now caps damage taken at 20% at +4, up from 25%.") + list("_Double Jump_ nerfed, now requires the user to jump within 3 turns rather than 5 and has a charge reduction of 16%/30%/41%/50/59%, from 20%/36%/50%/60%/75% in a compromise between existing mechanics and SHPD's changes"))),
            Changes(
                new ChangeButton(HUNTRESS, list(2,"Replaced huntress's +durability perk with a perk that passively boosts the damage of thrown weapons (+1 level).", "_Durable Projectiles_ has been buffed to compensate (now gives +100%/+150% durability, up from +50/+75%), though the amount of durability is slightly nerfed overall.", "_Seer Shot_ reworked. Instead of being available really fast, now applies to an increasingly large area with upgrades (3x3/5x5/7x7 at +1/+2/+3), though cooldown is also increased with upgrades.", "_Point Blank_ partially reworked. It's no longer level-shifted and gives SHPD point blank effects, but it also increases ranged accuracy now.")),
                new ChangeButton(Random.Int(2) == 0 ? LETHAL_MOMENTUM : LETHAL_MOMENTUM_2, "Made Lethal Momentum more distinct from its SHPD counterpart (and Pursuit):" + list(2, "Lethal Momentum now procs when your blow would have killed, regardless of enemy mechanics (such as those of brutes or ghouls) or other factors (enchantments like grim or blazing) that would otherwise stop SHPD Lethal Momentum.","Lethal Momentum (T2) now gives double accuracy on the turn that it procs, allowing for easier chaining.","Reverted T2 Lethal Momentum's +1 proc chance to be 67%, down from 75%.", "Lethal Momentum (Assassin) now requires preparation to trigger the first time, but follow-up kills do not have this limitation.")),
                misc(list("Modified Dwarf King's phase skip mechanics to produce greater overall stability and fix all reported issues with this mechanic.", "It is now possible to use multihit attacks such as Fury on Dwarf King and successfully skip his second phase, but doing this incurs damage penalties that make it less consistent at completely skipping than just doing a single large attack.","Changed Dwarf King FX for skipping slightly.") + list("You no longer have to equip class armor to use the armor ability, though it will no longer charge naturally.") + list("Changed and added descriptions and comments to talents. Some talents have unique comments when metamorphed.")),
                bugFixes("Added Trashbox Bobylev's crash message handler for easier bug reporting on Android." + "\n"
                        + list("Nature Power not giving its distinctive vfx","Death mark doing +67% damage instead of +33%")
                        + list("Rare cases where looking at class descriptions crashes the game.")
                        + list("Crash when selecting an empty tile with a free-targeted sniper's mark.","Rare cases where free-target would become unusable until the game is reloaded.")
                        + list("Smart targeting not prompting when no targets are around.", "crash when attempting to target a character with sniper's mark when no possible valid targets are present")
                        + list("Dwarf King having an incorrect amount of health for phase 3 in Badder Bosses.", "Dwarf King not saying lines in Badder Bosses when partially skipped.", "Dwarf King sometimes saying lines out of order.")
                        // TODO
                        + list(Messages.NO_TEXT_FOUND + " when looking at Elemental Blast description of firebolt", Messages.NO_TEXT_FOUND + " when targeting yourself with shadowstepping Wrath.")
                        + list("Rat statues not being given the respect they deserve."))
            ),
            Nerfs(
                new ChangeButton(BARKSKIN, "I'm testing something out, and Barkskin ended up being the guinea pig. Expect a buff to Warden mechanics in the future."
                    + "\n" + list("Barkskin granted by the talent now degrades every 1 turn, down from every two turns (current behavior of SHPD/Natural Dominance)", "Barkskin amounts adjusted, now 40/80/160/240% instead of 0/100/150/200% at +0/1/2/3. (+0 effect was bugged).","For reference, SHPD's Barkskin is 0/50/100/150% at +0/1/2/3.")
                    + "\nThere really no reason to have it be multi-turn when you can refresh the duration so easily by planting a seed or via rejuvenating steps.\nRelatedly, Warden now automatically gets Rejuvenating Steps (10 turn cooldown) if the subclass is chosen without any points in Rejuvenating Steps, so it is now impossible for a warden to be completely without access to grass."),
                new ChangeButton(WARLOCKS_TOUCH, "Removed Warlock's Touch's instant-proc chance."), new ChangeButton(PERFECT_COPY, "Removed +0 effect of instant swapping.\n\nThere will be another pass over armor abilities in the future so they work more predictably with Omni-Ability.")),
                new ChangeInfo("DLC-1.4", true, TITLE_COLOR, "",
                        new ChangeButton(WARRIOR,
                                list(2,
                                        "Added _Bearing Paw_ as t3 talent: gives extra range, bleeding and lifesteal on low HP.",
                                        "Added _Combo Meal_ as gladiator talent: gives combo time and combo hits on eating.",
                                        "Added _Bravery_ as berserker talent: gives extra rage when hero is unarmored against damage.",
                                        "Added _Pride of Steel_ as brawler talent: armor piercing, extra items and aggro control with specials.",
                                        "Reworked _Heroic Endurance_ into _Heroic Enchanting_: allows to use glyphs as enchantments while attacking.")),
                        new ChangeButton(MAGE,
                                list(2,
                                        "Added _Cryonic Spell_ as t3 talent: makes fire-related wands more frosty and buffs frost wand.",
                                        "Added _Spectre Allies_ as battlemage talent: buffs mirror images to use battlemage's perks.",
                                        "Added _Banished_ as warlock talent: allows to instakill soul-marked enemies on cooldown.",
                                        "Added _Mind Break_ as spirit caller talent: wraiths summoned by hero can possess enemies they attack.")),
                        new ChangeButton(ROGUE,
                                list(2,
                                        "Added _Trapper's Mastery_ as t3 talent: reclaims traps hero steps on with cooldown.",
                                        "Added _Energizing Steps_ as assassin talent: assassinations give artifact recharge.",
                                        "Added _Olympic Stats_ as freerunner talent: more freerun speed at cost of extra exhaustion.",
                                        "Added _Mechanical Power_ as Shadowflare talent: buffs melee attacks in robot form.",
                                        "Assasination-related talents can proc with 40% chance even if kill did not proc assassination.",
                                        "Innate Lethality have been buffed to +3 (_6%, 20%, 40%, 100%_)")),
                        new ChangeButton(HUNTRESS,
                                list(2,
                                        "Added _Auto-Reload_ as t3 talent: repairs throwning weapons with liquid metal on go.",
                                        "Buffed _Point Blank_, now provides the chance to knockback in melee range.",
                                        "Added _Archery's Mark_ as sniper talent: arrows bounce for extra damage to nearby enemy.",
                                        "Added _Indirect Benefits_ as warden talent: warden's plant effects can trigger when mob steps on plant.",
                                        "Moved Super-Shot damage boosts to _Heroic Archery_." +
                                                "Added an actual effect to _Heroic Archery_: thrown weapons have a chance for not consuming their durability.")),
                        new ChangeButton(new Kromer(),
                                list(2,
                                        "Added brand new material, coming from otherworldy darkness: kromer!",
                                        "Kromer can be obtained by completing quests or by buying them in shops (beware of scam!)",
                                        "Usages of kromer include giving you extra talents, significantly upgrading starting items, crafting ultimate versions of PoS and SoU and even potion that allows you to not die!",
                                        "_This seems a little too fishy to be true..._")),
                        new ChangeButton(new ItemSprite(TERMINUS, new ItemSprite.Glowing()), "New weapons!",
                                list(2,
                                        "Added _Terminus Blade_, which is made with chaosstones and kromer. It has exceptional damage and ability to instakill.",
                                        "Added a clone of _dagger_ that makes you stunlocked after using it.",
                                        "Added an upgraded version of Wand of His Ratiness.")),
                        new ChangeButton(new ItemSprite(ARTIFACT_OMNI, new ItemSprite.Glowing()), "Soul of Yendor",
                                "_-_ Added the artifact, that is 8 other artifacts at once.\n\n" +
                                        "_-_ This item combines properties of Horn of Plenty, Alchemical Toolkit, Ethereal Chains, Chalice of Blood, Sandals of Nature, Master Thieves\' Armband, Timekeeper\'s Hourglass and Unstable Spellbook.\n\n" +
                                        "_-_ To make it, use cursed wand in some way to combine all artifacts together.\n\n" +
                                        "_-_ Alternatively, use kromer, chaosstone, Amulet of Yendor and 101 energy."),
                        new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "New challenges",
                                list(2,
                                        "Added 21 new challenges that are accessible from beating challenges!",
                                        "Each new challenge is connected to one that unlocks it but has separate effects.",
                                        "First 9 challenges are unlocked from regular challenges, another nine are from beating previous 9 challenges, and last three are from beating 6/12/18 challenges at once.",
                                        "Have fun for trying all of them!")),
                        misc("_-_ Abyss stages grow in size up to depth 75 with 2x size of depth 27.\n\n" +
                                "_-_ Heroes can level up beyond level 30 but more XP is required for each new level; each 3rd level up beyond 30 will also give talent point to every tier.\n\n" +
                                "_-_ Abyssal enemies give 40%-50% less XP.\n\n" +
                                "_-_ Much more items can be put into alchemy interface.\n\n" +
                                "_-_ Ring of Force can be enchanted by curse enchantments with Curse Infusion.\n\n" +
                                "_-_ Dried Rose's ghost heals 2.5x faster and has more HP and unarmed damage if hero is beyond level 30.\n\n" +
                                "_-_ Buffed Final Froggit's HP from 90 to 180.\n\n" +
                                "_-_ Nerfed Abyssal Nightmare's ability to split by making it only work for initial mob, also nerfed HP regen from 10 to 7.\n\n" +
                                "_-_ Changed UI to better differentate from regular RKPD2."),
                        bugFixes("_-_ Fixed Frostburn not slowing down characters.\n\n" +
                                "_-_ Fixed Molten Strife's brawler special damaging hero.\n\n" +
                                "_-_ Fixed Ring of Force being \"augmentable\".\n\n" +
                                "_-_ Fixed crash for In My Memories if player has no broken seal.\n\n" +
                                "_-_ Fixed crash after examining weak floor pit in higher levels of Abyss.\n\n" +
                                "_-_ Fixed sprite of Spectral Necromancer.\n\n" +
                                "_-_ Fixed Rat King's boss depth always putting you at stairs on loading the level.\n\n" +
                                "_-_ Fixed Wand of Firebolt's elemental blast description.\n\n" +
                                "_-_ Fixed Thinking with Portals consuming all of cloak's charge independant on distance.")
                ),
                new ChangeInfo("DLC-1.3", true, TITLE_COLOR, ""),
                NewContent(
                        new ChangeButton(new ItemSprite(MASK), "Secret Subs",
                                list(2,
                                        "Added secret subclasses for each hero! They can be accessed in same way as huntress's one.",
                                        "You can permanently unlock secret subclass by winning with it or defeating Rat King boss as any subclass.")),
                        new ChangeButton(FUN, "Rat King's Wrath",
                                "_-_ Added new talent for Wrath, which makes ability more fun.\n\n" +
                                        "_-_ Old Wrath have been renamed to _Divine Fury_."),
                        new ChangeButton(new ItemSprite(RING_RUBY), "Ring of Force rework",
                                "_-_ Can hold enchantments and proc them.\n\n" +
                                        "_-_ Reduced direct unarmed damage, but armed damage pierces armor and is buffed by 66%."),
                        new ChangeButton(ASSASSIN,
                                list(2,
                                        "All 3 Shattered's assassin talents got replaced by new ones.",
                                        "_Bloodbath_ allows for AoE damage from assassinations.",
                                        "_Thinking with Portals_ gives Assassin an ability to teleport and gain preparation instantly.",
                                        "_Adapt and Overcome_ patches Assassin's weaknesses such as no synergy with wands, bad damage rolls and unsuccessful attacks.",
                                        "Old Enhanced Lethality and Assassin's Reach are now innate abilities."))
                ),
                Changes(
                        new ChangeButton(new RatKingBossSprite(), "Rat King Boss",
                                "_-_ RK boss will teleport if he is stuck.\n\n" +
                                        "_-_ RK boss will move in emperor phase 3 state.\n\n" +
                                        "_-_ Reduced amount of statues and water on level 0.\n\n" +
                                        "_-_ Fixed issues with Tengu's tricks used by RK.\n\n" +
                                        "_-_ RK boss is resistant to corrosion.\n\n" +
                                        "_-_ Any rat will attack you during the fight.\n\n" +
                                        "_-_ RK boss has far better accuracy against wraiths and kills all of them while switching to new phase.\n\n" +
                                        "_-_ Rat King (class) can fight Rat King (boss) now. Throw something very important on the ground.\n\n" +
                                        "_-_ Reduced amount of monster spam."),
                        new ChangeButton(BERSERKER, "_-_ Buffed rage damage boost to up +100% on 90% rage."),
                        new ChangeButton(KING, "Reintoduced certain perks for Rat King subclass:\n\n" +
                                "_-_ Berserker's rage\n" +
                                "_-_ Warlock's soulmark\n" +
                                "_-_ Assassin's preparation\n" +
                                "_-_ Sniper's armor penetration\n" +
                                "_-_ Warden's seed effects"),
                        new ChangeButton(SOUL_SIPHON, "_-_ Added unique icons for Huntress's secret subclass."),
                        new ChangeButton(HEROIC_WIZARDRY, "_-_ Changed Heroic Wizardry to properly act as additional max charges for every possible occasion."),
                        new ChangeButton(LIKE_A_BULLET, "Previously was Seer Shot.\n\n" +
                                "_-_ Removed and replaced by _Like a Bullet_. Point Blank's Super-shot buffs were moved here, with very mild nerf.\n" +
                                "_-_ Seer Shot is now Huntress's innate ability.\n" +
                                "_-_ Natural Dominance still has Seer Shot part."),
                        new ChangeButton(new ItemSprite(RING_AMETHYST), "Ring of Wealth change",
                                "_-_ Upgrades affect drop rates, at +16 it maxes out at 1 item per kill."),
                        bugFixes("_-_ Fixed Elemental Blast doing not as much damage as intended.\n" +
                                "_-_ Fixed Heroic Endurance not working at all.\n" +
                                "_-_ Fixed Burning and Red Burning conflicting between each other.\n" +
                                "_-_ Fixed Big Rush killing quest givers and shopkeepers.\n" +
                                "_-_ Fixed Eldritch Blessing not working properly.\n" +
                                "_-_ Fixed DK getting stuck if his progression damage is affected by some modifier.\n\n" +
                                "RKPD2 bugs:\n\n" +
                                "_-_ Fixed talent window description having extra whitespace in title.\n" +
                                "_-_ Fixed miscolored icons for heroes and talents.")
                ),
        },{ // v0.2
            new ChangeInfo("v0.2", true, TITLE_COLOR, ""),
            NewContent(
                    info("As of v0.2.1, I shifted to Shattered's new major.minor.patch versioning system. As such v0.2.1 was really the equivalent of v0.2.0a."),
                    new ChangeButton(new Wrath(), "Rat King's Wrath Redesign!", "I've finally gotten around to updating Rat King's Wrath to reflect v0.9.3 reworks to armor abilities!"
                            + "\n\nWhile the previous Wrath was a combination of all armor abilities, the prospect of combining 13 different abilities into one isn't possible under the Wrath design, so I have instead decided to adapt the ones that have similar functionality to each part of the previous Wrath: _Smoke Bomb, Shockwave, Elemental Blast, and Spectral Blades._"
                            + "\n\nNote, however, that Wrath is not a perfect mirror of these abilities, though all their mechanics are there in some form." + "\n"
                            + list("Energy cost increased to 60 from 35.") + list("Added four new talents to Wrath.", "Each new talent corresponds as closely as possible to the talents of the respective armor ability.", "Wrath does not have Heroic Energy.") + list("Smoke Bomb no longer grants invisibility, mechanic instead moved to corresponding talent.", "Range is reduced to 6, from 8.") + list("Molten Earth effect replaced with Elemental Blast.") + list("Wrath's leap no longer stuns adjacent foes, instead sends out a 360 degree AOE Shockwave that covers a 3x3 area.", "Aftershock's Striking Wave and Shock Force are less effective than the real thing.", "Stun inflicted through through Aftershock cannot be broken during Wrath itself.") + list("Spectral Blades retains the ability to hit all targets in sight (removing the need to target it).", "Spectral Blades instead has damage and proc penalties when attacking multiple targets, though upgrading its respective talent lowers the degree to which this occurs.")
                            + "\nWrath should be much more powerful now, but also much less cheesy; the consistent stun and root are gone, and it's much more bound to set ranges than before, but upgrading its talents can hugely increase Wrath's power output and flexibility."),
                    new ChangeButton(HUNTRESS, list(2,
                            "Added a _secret subclass_ to Huntress, accessible by a secret interaction while choosing a subclass.",
                            "_Restored Nature_ root duration reverted to 2/3, down from 4/6, but it now also causes health potions and related alchemy products to be used instantly.")
                            + "_Multi-Shot:_" + list("Now uses multiple buffs to show that more than one target is marked.",
                            "Allows stacking of free-targeted marks instead of overriding them when a new target is marked.",
                            "Has changed free-targeting logic (thanks to smart-targeting) to make these new interactions smoother; enemies that are already targeted will be highlighted while manually targeting.")
                            + "\nMulti-shot should now be more complex, but in exchange it should (somewhat ironically) be easier to use and understand. It's also much more flexible with its free-targeted sniper special functionality."),
                    new ChangeButton(KINGS_WISDOM, "New Talent Icons!", "Most of my added talents now have unique icons! Many of these are personally done, but some credit to _Trashbox Bobylev_ is needed.\n\nAlso, the new music and UI changes from SHPD v1.0.0 have been implemented into the game.")),
            new ChangeInfo("From SHPD v1.0.3", false, SHPX_COLOR, "",
                    // alchemy stuff once it's added in.
                    new ChangeButton(new ItemSprite(CROWN), "Armor Ability Changes", ""
                        + "_Buffs:_\n"
                        + list("_Endure_ bonus damage conversion rate up to 1/3 from 1/4.")
                        + list("_Striking Wave_ effectiveness increased by 20%."/*,"_Shock Force_ now actually adds 20% damage per level as stated. Previously it only added 15%."*/, "Relatedly, Striking Force is no longer level shifted with regards to damage, but its boost is now +25/+50/+75/+100% damage.")
                        + list("_Wild Magic_ now boosts wand levels, instead of overriding them.","_Conserved Magic_ now has a chance to give each wand a 3rd shot.","_Conserved Magic_ charge cost reduction down to 33/55/70/80% from 44/69/82/90%.")
                        + list("_Elemental Blast_ base damage increased to 15-25 from 10-20.", "Elemental Power scaling increased to 20%/40%/60%/80% (Rat Blast) and 25/50/75/100 (Elemental Blast).")
                        + list("_Remote Beacon_ range per level increased to 4, from 3.")
                        + list("_Shadow Clone_ now follows the hero at 2x speed.","_Shadow Blade_ unshifted, damage per level increased to 7.5% from 6.25%.","_Cloned Armor_ unshifted, armor per level increased to 15% from 12.5%.")
                        + list("_Spirit Hawk_ evasion, accuracy, and duration increased by 20%.","_Swift Spirit_ now gives 2/3/4/5 dodges, up from 1/2/3/4.","_Go for the Eyes_ now gives 2/4/6/8 turns of blind, up from 2/3/4/5.")
                        + list("_Spirit Blades_ effectiveness increased by 20%.")
                        + "\n\nNerfs:\n"
                        + list("_Double Jump_ charge cost reduction down to 20/36/50/60%, from 24/42/56/67%.")
                        + list("_Telefrag_ self damage increased to a flat 5 per level.")
                        + "\nSmoke bomb nerfs are only applied to Wrath. Standard smoke bomb is left intact."
                        + list("_Smoke Bomb_ max range reduced to 6 tiles from 8.", "_Body Replacement_ armor reduced to 1-3 per level, from 1-5.", "_Hasty Retreat_ turns of haste/invis reduced to 1/2/3/4 from 2/3/4/5","_Shadow Step_ charge cost reduction down to 20/36/50/60%, from 24/42/56/67%.")
                        + list("_Double Mark_ balance changed in response to SHPD changes; charge cost reduction down to 16/40/58/70/79% (which is still up from shpd's 30/50/65/70), from 33/55/70/80%/87.")
                        + list("_13th armor ability_ now only lasts for 6 turns, but also no longer prevents EXP or item drops.",
                        "_resistance talent_ damage reduction, in a compromise, reduced to 15/28/39/48%, which is still well above shattered levels.")),
                    new ChangeButton(Icons.get(DEPTH), "SHPD Additions and Changes", "Implemented:"
                        + list("New music",
                            "Geyser and Gateway traps",
                            "Spectral Necromancers",
                            "Liquid Metal and Arcane Resin alchemy recipes",
                            "Unblessed Ankh rework",
                            "Blessed Ankhs now give 3 turns of invulnerability",
                            "Guidebook rework")
                        +"\nRunestone buffs:"+list("All Scrolls now produce 2 runestones.","_Stone of Intuition_ can now be used a second time if the guess was correct.","_Stone of Flock_ AOE up to 5x5 from 3x3, sheep duration increased slightly.","_Stone of Deepened Sleep_ is now stone of deep sleep, instantly puts one enemy into magical sleep.","_Stone of Clairvoyance_ AOE up to 20x20, from 12x12.","_Stone of Aggression_ duration against enemies up 5, now works on bosses, and always forces attacking.","_Stone of Affection_ is now stone of fear, it fears one target for 20 turns.")),
                    misc("Implemented:\n"
                            + list("Various tech and stability improvements.", "Increased the minimum supported Android version to 4.0, from 2.3.", "Game versions that use github for update checking can now opt-in to beta updates within the game.")
                            + list("Various minor UI improvements to the intro, welcome and about scenes.","Adjusted settings windows, removed some unnecessary elements.","Armor with the warrior's seal on it now states max shielding.","Bonus strength is now shown separately from base strength.", "Added info buttons to the scroll of enchantment window.")
                            + list("'Improved' the exit visuals on floor 10.","Becoming magic immune now also cleanses existing magical buffs and debuffs.","Traps that spawn visible or that never deactivate can no longer appear in enclosed spaces")
                            + list("Added info buttons to the scroll of enchantment window.")),
                    bugFixes(""
                        + list("Various rare crash bugs", "Various minor visual and text errors", "damage warn triggering when hero gains HP from being hit", "various rare bugs involving pitfall traps")
                        + list("statues not becoming aggressive when debuffed", "swapping places with allies reducing momentum", "DK minions dropping imp quest tokens", "giant succubi teleporting into enclosed spaces", "spectral blades being blocked by allies", "Spirit Hawk and Shadow Clone being corruptible")
                        + list("wands losing max charge on save/load in rare cases", "magical infusion clearing curses", "dewdrops stacking on each other in rare cases", "exploding skeletons not being blocked by transfusion shield in rare cases", "rare incorrect interactions between swiftthistle and golden lotus")
                        + list("various minor errors with electricity effects", "soul mark not working properly on low HP enemies with shielding", "various rare errors with shadows buff", "errors with time freeze and inter-floor teleportation mechanics", "rooted characters not being immune to knockback effects")
                        + list("gladiator combos dealing much more damage than intended in certain cases", "magical charge and scroll empower interacting incorrectly", "magical sight not working with farsight talent", "perfect copy talent giving very slightly more HP than intended", "wild magic using cursed wands as if they're normal") + list("Disarming traps opening chests.", "Body replacement ally being vulnerable to various AI-related debuffs.") + list("Disarming traps opening chests", "Body replacement ally being vulnerable to various AI-related debuffs", "Some ranged enemies becoming frozen if they were attacked from out of their vision", "Time stasis sometimes not preventing harmful effects in its last turn."))),
            Changes(
                new ChangeButton(WARLOCKS_TOUCH, "Warlock's Touch is currently extremely situational and often requires giving up warlock's other gimmicks to work at its best. At the same time, when exploited it's incredibly overpowered. These changes are intended to instead generalize its use, increasing its versatility and amount of situations in which it is applicable."
                    + list(2,
                        "Proc chance on melee attacks is now a fixed 15/25/35% chance at +1/+2/+3 respectively.",
                        "Proc chance on thrown weapons is now a fixed 25/40/55% chance at +1/+2/+3.",
                        "Allies can now inflict soul mark via Warlock's Touch using melee mark chances.",
                        "Proc duration of mark is now a fixed 6 turns, instead of being 10 + weapon level",
                        "Chance for proccing soul mark with the attack that inflicts it is 20/30/40%, down from 25/50/75%, but now applies to wands if Soul Siphon is upgraded.")),
                new ChangeButton(new ItemSprite(STONE_ENCHANT), "Enchanting Logic",
                        list("The chance for rare weapon enchantments to appear has been increased by ~50%.")
                                + "_\n\nSpirit Bow only:_" + list(2,
                                "Stones of Enchantment can no longer roll Lucky or Blocking.",
                                "Explosive Enchantment can now be rolled by Stones of Enchantment.",
                                "Explosive is now exactly as common as a standard uncommon enchantment in Shattered Pixel Dungeon. "
                                    + "Other uncommon enchants are now slightly more common to compensate.",
                                "Grim no longer has specifically boosted chances to appear.")),
                misc(list(2,
                        //"TODO _Energizing Meal I_ now adds new recharging buffs instead of stacking on existing ones.",
                        "Magic Missile Elemental Blast now adds new recharge buffs rather than extending recharging to prevent exploits.",
                        "Talents that identify curses now declare whether an item is cursed when activated.",
                        // ui
                        "Most windows now scroll if they would not fit on the screen.",
                        "Changed commentary on Rat King's tier 3 talents.",
                        "Changed capitalization logic, hyphenated titles now have both words capitalized.")),
                bugFixes(list(2,
                        "Lethal Momentum not working with Death Mark.",
                        "bm staff on-hit effects not responding to proc chance modifiers like Enraged Catalyst and Spirit Blades.",
                        "Rat King's light cloak now is 13/27/40 instead of 10/20/30",
                        "Fixed a mistake when updating Ranger to Multi-Shot talent.",
                        "Typo in Restoration description",
                        "Rat King's Wrath sometimes freezing Rat King's sprite after use.",
                        "fixed soul eater working incorrectly and sometimes yielding NaN hunger.", "Fixed rare cases of incorrect character-specific text.", "Fixed color of Rat King's eyes in his subclass icon."))),
            Nerfs(
                new ChangeButton(ROGUE, ""
                        + "_Mending Shadows_ turned out to have exploits, so it's (very unfortunately) being largely scrapped, though the name remains for now:"
                        + list("Now provides shielding every 2/1 turns, up to 3/5 max shielding (Shattered Protective Shadows).",
                        "Healing rate reduced to every 4/2 turns, and it no longer works while starving.")
                        + "\nIn addition, I'm making these changes based on player feedback:"
                        + list("_Cached Rations_ now gives 3/5 rations, down from 4/6.", "_Light Cloak_ effectiveness now 20%/40%/60%, down from 25/50/75.")),
                new ChangeButton(RAT_KING, list(2,
                        "The recent Strongman buff has turned Tactics into a monster, while Imperial Wrath is still rather niche in comparison. Thus, Imperial Wrath now has Strongman instead of Tactics. I've also taken the liberty of renaming Imperial Wrath to be Inevitability to commemorate this change.",
                        "Royal Intuition's +1 effect is now additive with the SHPD Survialist's Intuition rather than multiplicative. It is now 2.75x/3.75x id speed, down from 3.5x/5.25x.",
                        "Rat King is also affected by the v1.0.0 staff nerf.")),
                new ChangeButton(RATFORCEMENTS, "I've successfully made Ratforcements the best talent Ratmogrify has. That said, it's so powerful now that it's making the other aspects of Ratmogrify much less useful." + list(2, "Ratforcements stats reduced by ~20% across the board.") + "Don't be fooled though into thinking it's bad now, it is still VERY superior to Shattered's Ratforcements."))
        },

            {
                    new ChangeInfo("DLC (Abyssal 1.2)", true, TITLE_COLOR, "From this update, the mod is called RKPD2 DLC.",
                            new ChangeButton(avatar(RAT_KING,6), "Another attempt at Rat King Nerfs (rat king nerfs)",
                                    "_-_ Reverted HP nerf.\n\n" +
                                            "_-_ Subclass powers are acquired via talents instead of being given by default.\n\n" +
                                            "_-_ Restoration works with drinking from waterskin."),
                            new ChangeButton(new RatKingBossSprite(), "Rat King Boss",
                                    "Added new extremely hard boss, which can be fought, when you try to talk with RK while having an amulet.\n\nHis attacks are based on hero classes and some bosses, and Rat King will frequently switch between them.\n\n_Beware of his power while doing challenge run._")
                    ),
                    new ChangeInfo("Talents", false, 0x44d1d3, "",
                            new ChangeButton(NOBLE_CAUSE, "RK",
                                    "_-_ Changed every Rat King talent to have unique sprite.\n" +
                                            "_-_ Imperial Wrath now get both _Hold Fast_ and _Strongman_, to make it more relevant.\n" +
                                            "_-_ Added _Advanced Education_ talent as additional T4: allows to get bonus points for previous tiers.\n" +
                                            "_-_ Ratmogrify talents are level-shifted for Rat King.\n"+
                                            "_-_ Added _Drratedon_ talent for Ratmogrify: gives buffs to allied ratmogrified enemies and summoned rats.\n" +
                                            "_-_ Revamped Wrath's talents to have more rounded distribution of powers.\n" +
                                            "_-_ Added _Avalon Power-Up_ talent for Wrath: enhances wrath with powers related to Ratmogrify, Heroic Leap, Elemental Blast.\n\n" +
                                            "_-_ Added _Mus Rex Ira_ armor ability, combining the powers of _Spirit Hawk_, _Shadow Clone_, _Remote Beacon_, _Wild Magic_, _Endure_ and _Nature's Power_.\n" +
                                            "_-_ It has four talents:\n" +
                                            "_-_ _Bloodflare Skin_ buffs enduring and enhances clone and hawk's survivability.\n" +
                                            "_-_ _Astral Charge_ buffs beacon and wild magic.\n" +
                                            "_-_ _Shadowspec Blade_ buffs clone and hawk's offensive capabilities.\n" +
                                            "_-_ _Silva Range_ buffs nature's power and gives ranged attack to clone."
                            ),
                            new ChangeButton(IRON_WILL, "Warrior",
                                    "_-_ Changed One Man Army and Skill to have unique sprite.\n" +
                                            "_-_ Replaced _Restored Willpower_ with _Willpower of Injured_: makes seal's shielding regenerate faster on low HP.\n" +
                                            "_-_ Added _Weapon Mastery_ talent as 5th T1: increases the minimum amount of weapons.\n"+
                                            "_-_ Added _Big Rush_ talent as 6th T2: causes warrior to ram enemies instead of going around them, with bonus damage from seal's shielding.\n" +
                                            "_-_ Changed _Hold Fast_: now gives minimal armor value.\n" +
                                            "_-_ Reworked _Endless Rage_: now gives ability to gain rage from DoT, magic spells and Viscosity.\n" +
                                            "_-_ Changed _Berserkering Stamina_: now also makes shielding decay slower, but gives slighty less of it; level-shifting for bonus shielding is removed.\n" +
                                            "_-_ Gladiator gets 25 turns of combo after killing an enemy, from 15.\n" +
                                            "_-_ Replaced _Cleave_ with _Battle Tendency_: causes combo to decay instead of resetting.\n" +
                                            "_-_ Added _Heroic Endurance_ talent as additional T4: adds bonus upgrades to any armor.\n" +
                                            "_-_ Added _Alice Gambit_ to Heroic Leap: causes enemies around landing place to shrink after leap.\n" +
                                            "_-_ Reworked _Double Jump_: gives second jump for free at +2 and adjusts cost for third jump starting from +3.\n" +
                                            "_-_ Added _Cockatriocious_ to Shockwave: turns enemies caught in shockwave into stone.\n" +
                                            "_-_ Added _Demonshader_ to Endure: imbues endure-empowered attacks with fire damage and ability to generate rage and combo."),
                            new ChangeButton(SORCERY, "Mage",
                                    "_-_ Changed Sorcery and Warlock's Touch to have unique sprite.\n" +
                                            "_-_ Added _Arcane Boost_ talent as 5th T1: slightly increases recharge speed on wands not in staff.\n" +
                                            "_-_ Nerfed _Shield Battery_ by 17.3%.\n" +
                                            "_-_ Added _Pyromaniac_ talent as 6th T2: increases damage from all fire in the game.\n" +
                                            "_-_ Nerfed _Excess Charge_: 50% reduced shielding.\n" +
                                            "_-_ Added _Heroic Wizardry_ talent as additional T4: allows to use wand charges beyond 0.\n" +
                                            "_-_ Buffed _Elemental Blast_ to have its base damage based on staff's melee damage.\n" +
                                            "_-_ Added _Empowered Strike II_ to Elemental Blast: doubles the damage and effects of ability for 2x charge cost.\n" +
                                            "_-_ Added _Eldritch Blessing_ to Wild Magic: makes all zaps cursed, cursed effects are safer and stronger while using the ability.\n" +
                                            "_-_ Added _Chrono Screw_ to Remote Beacon: gives time bubble after successful teleportation."),
                            new ChangeButton(DUAL_WIELDING, "Rogue",
                                    "_-_ Changed Lethal Momentum and Marathon Runner to have unique sprite.\n" +
                                            "_-_ Added _Faraday Cage_ talent as 5th T1: protects hero from electricity.\n"+
                                            "_-_ Replaced _Rogue's Foresight_ with _Protein Infusion_: gives bonus speed and evasion depending on satiety.\n" +
                                            "_-_ Added _Efficient Shadows_ talent as 6th T2: removes cloak's charge speed boost for more invisibility time per charge.\n" +
                                            "_-_ Fixed _Dual Wielding_: no longer causes softlocks.\n" +
                                            "_-_ Added _Heroic Stamina_ talent as additional T4: gives movespeed boost for using artifacts.\n" +
                                            "_-_ Added _Frigid Touch_ to Smoke Bomb: sets the hero's FOV on frost fire after blinking.\n" +
                                            "_-_ Reworked _Double Mark_: gives second use for free at +2 and adjusts cost for third use starting from +3.\n" +
                                            "_-_ Added _Cataclysmic Energy_ to Death Mark: increases the duration of death mark and removes damage boost, but marked enemies at 0 HP will combust each turn.\n" +
                                            "_-_ Added _Dar Magic_ to Shadow Clone: gives various ranged capabilities to shadow clone."),
                            new ChangeButton(NATURES_AID, "Huntress",
                                    "_-_ Changed Nature's Better Aid to have unique sprite.\n" +
                                            "_-_ Added _Greenfields_ talent as 5th T1: increases regeneration while in furrowed grass.\n"+
                                            "_-_ Replaced _Nature's Aid_ with _Scout's Barrier_: gives shielding for successful Super-Shots.\n" +
                                            "_-_ Added _Scout's Agility_ talent as 6th T2: makes heroine dodge ranged attacks better.\n" +
                                            "_-_ Armored Cloak is preserved when using DK's crown.\n" +
                                            "_-_ Added _Heroic Archery_ talent as additional T4: adds bonus upgrades to thrown weapons.\n" +
                                            "_-_ Added _Spectral Shot_ to Spectral Blades: makes blades behave like throwing weapons and summon arrows from her bow.\n" +
                                            "_-_ Added _Primal Awakening_ to Nature's Power: with small chance turns enemies into gnoll trickster while buff is active.\n" +
                                            "_-_ Added _Beak of Power_ to Spirit Hawk: increases utility power of hawk and gives it ranged attack at +4.")
                    ),
                    NewContent(new ChangeButton(new ItemSprite(ROYAL_SWORD), "Tier 6 weapons",
                                    "Added 10 melee and 4 thrown exceptionally rare tier 6 weapons.\n\n" +
                                            "_-_ They appear in ebony chest and have many special abilities.\n" +
                                            "_-_ T6 weapons also omni-melee weapon and omni-thrown weapon, called _Royal Brand_ and _Steel Axe_, respectively."),
                            new ChangeButton(new ItemSprite(WAND_UNSTABLE), "New wand",
                                    "Added wand of His Ratiness, which will create random wand zaps and use random wand effects for Battlemage and Elemental Blast."),
                            new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "New champions",
                                    "Added a lot of champion types, increasing their number to 15.")),
                    new ChangeInfo("", false, 0x000000, "",
                            bugFixes("_-_ Fixed light cloak charge speed being wrong (was higher than intended for Rogue and lower for RK)\n\n" +
                                    "_-_ Fixed Wealth and Might using wrong percentages in their descriptions.\n" +
                                    "_-_ Fixed Shrinking being only cosmetic effect.\n" +
                                    "_-_ Fixed shop appearing on D21."),
                            misc("_-_ Added some additional lore and sprite edits for dungeon monsters.\n\n" +
                                    "_-_ Added missing loot into abyssal shops.")
                    )
            },
            // v0.1.0
        {
            new ChangeInfo("v0.1.0", true, TITLE_COLOR, ""),
            NewContent(
                new ChangeButton(Icons.get(INFO), "Developer Commentary",
                    "I regret the relatively long wait for a v0.9.3 implementation, but RKPD2 is now updated to Shattered v0.9.3c! Enjoy smaller levels, some quality of life improvements, and even more buffs to your favorite heroes."
                    + "\n\nI'm currently messing with a new method with which to buff talents and heroes: _point shifting_. Point shifting means that I make +1 effects +0 effects, +2 effects +1 effects, and so on. This results in an immediate power bump when the particular talents are obtained, though it can be slightly confusing on the first time (or with tier 2 talents)."
                    + "\n\nThere's probably going to be a v0.1.1 that will add more thematic changes that I can make building off of this update."
                ),
                new ChangeButton(new ItemSprite(CROWN), "T4 Talents Implemented!", ""
                    +"Now bringing you a RKPD2 with FULL talents!!!"
                    +list(2,
                        "Epic armors and T4 talents have been directly implemented.",
                        "The 12 points for t3 talents remain, causing the hero to uniquely gain two talent points per level for levels 21-24.",
                        "You can still supercharge your armor if you have too low charge, don't worry, though I doubt it'll be as notable as before with the charging mechanic changes and Heroic Energy.",
                        "Rat King has two \"special\" abilities to choose from, currently, and one more will hopefully be added in future updates.")
                    + "_Ability Buffs_:" + list("Heroic Energy, Elemental Power, Growing Power, and Reactive Barrier are now more potent.", "Nature's Wrath is now 10%/20%/30%/40% prc.", "Ratforcements spawns scaled rats that are much stronger than standard rats. It can also summon scaled loyal albinos now.", "Ratlomancy now gives 50% more adrenaline for 50% more fun.", "Death mark now gives +33% damage, up from +25%.","Blast Radius, Long Range Warp, and Telefrag have been level-shifted", "Projecting blades penetration is now 1/3/5/7/9, down from 2/4/6/8/10, and its accuracy boost is 0/33/67/100/133 instead of 25/50/75/100/125.", "Fan of blades now affects 1/2-3/4/5-6/7 targets in a 30/45/90/135/180 degree AOE cone.")),
                new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "New Challenge!", "Badder bosses has been implemented into RKPD2, enjoy teaching those bosses that no matter what they do, they will still lose."),
                new ChangeButton(Icons.get(TARGET), "Special Action Targeting", "Added smart targeting for Combo, Preparation, and Sniper's Mark's free shot via Multishot."
                    + list(2,
                        "When there is only one target for the action, the action will skip the prompt and simply execute.", "When there is more than one possible target (or no targets), the valid targets will be highlighted.")
                    + "This should make using these abilities much more smooth when fighting a single enemy as well as making them more intuitive in general.")),

            Changes(
                new ChangeButton(RAT_KING, ""
                        +"Shattered balance changes have been directly implemented to Rat King's mechanics, for better or for worse:"
                        +list(2,
                            "Noble Cause nerfed, gives less shielding when the staff runs out of charge.",
                            "Imperial Wrath buffed, now gives more shielding on berserk (yay?)",
                            "Tactics buffed, its strongman now uses the v0.9.3 version and its description has been updated to indicate that cleave makes combo last for 15/30/45 turns. This talent is probably the biggest winner of this update.",
                            "Ranged Terror buffed, now gives a greater damage boost to specials when using thrown weapons.",
                            "Royal Presence changed, now has an increased chance to spawn wraiths but decreased indirect damage soul mark effectiveness and ally warp range",
                            "Natural Dominance nerfed, now gives 50/100/150% Rat King's level in barkskin that fades every turn instead of previous barkskin effect.")),
                new ChangeButton(Icons.get(DEPTH), "Levelgen", "Implementing SHPD v0.9.3's levelgen changes has resulted in an even smaller RKPD2!"
                        +list(2,
                            "Levels should be slightly smaller than before in terms of room amounts",
                            "There also should be less tunnels, which should significantly cut down further on level sizes. Up until now RKPD2 was plagued by very long connection room generation. No longer!",
                            "Entrance and Exits can no longer connect to each other.",
                            "Pit room size minimum increased.",
                            "Hordes are somewhat less likely to spawn on floor 1 despite the smaller size.")
                ),
                misc(list(2,
                        "Implemented virtually all misc changes up to SHPD v0.9.3c, including the addition of quick-use bags, stone behavior, etc.",
                        "Sorcery talent moved from 3rd slot to 6th slot for consistency with other RKPD2-exclusive talents.",
                        "Updated some talent descriptions to be clearer or otherwise add commentary.",
                        "Added unique dialogue for Rat King-Wandmaker interactions",
                        "Dwarf King has a new snide comment for you in badder bosses!",
                        "Dwarf King now bellows on certain quotes",
                        "Added differing descriptions for loyal rats and hostile rats, might wanna look at their descriptions when you get the chance.")
                ),
                bugFixes(list("Bugfixes up to SHPD v0.9.3c have been implemented.",
                        "King's Vision now correctly updates Rat King's field of view on the turn it is upgraded.",
                        "Fixed some talent description typos.")
                        // v0.1.0a
                        + list("Rogue's cloak boost to weapons being incorrectly displayed in some cases", "Missing text for certain Rat King's Wrath interactions", "String formatting failure in Enhanced Scrolls", "Various typos in talent and subclass descriptions.")
                        // v0.1.0b
                        + list("Sorcery not giving BM effects on non-staff melee attacks", "Berserking Stamina not level-shifted in terms of berserk recovery speed.", "Confusing typo in Berserking Stamina description", "Spectral blades being able to attack NPCs (actually a shattered bug)", "Fan of Blades dealing more damage to additional targets than intended.", "Projecting Blades giving more wall penetration for the initial target than additional targets."))),

            Buffs(
                new ChangeButton(HUNTRESS, "I'm leaning harder on giving subclasses access to talents without upgrading them, and _Warden_ is an excellent place to start:"
                    +list("Tipped Darts shifted, now gives 2x/3x/4x/5x durability.",
                        "v0.9.3 Barkskin implemented, but Barkskin now degrades every two turns instead of one and gets barkskin equal to 50/100/150/200% of her level at +0/1/2/3 respectively. Thus instead of nerfing it, I have buffed it ;)")
                    +"\nIt's also clear that _Sniper needs buffs._"
                    +list("Farsight shifted, now gives 25/50/75/100% bonus sight.",
                        "Shared Upgrades shifted (now 10/20/30/40), and the increasing duration aspect is given at +0.",
                        "New Sniper perk: +33 accuracy.")
                    //+"\n"
                    //+"\n_DEFERRED_ Multishot shifted, +3 now is 4x duration+4x target"
                    // TODO see if I can make the new targeting make this a smooth addition for future
                    //+"\n_TODO_ Multishot free shot can stack with standard marking."
                    //+"\n"
                ),
                new ChangeButton(WARRIOR, list(2,
                        "Cleave buffed from 0/10/20/30 (was bugged) to 15/30/45/60.",
                        "Endless Rage buffed from +15%/30%/45% to +20%/40%/60%",
                        "Enraged Catalyst buffed from 17%/33%/50% to 20%/40%/60%.",
                        "Berserking Stamina level-shifted.",
                        "Improvised projectiles is not nerfed.")),
                new ChangeButton(BATTLEMAGE, "Battlemage is currently a bit 'weaker' than warlock, and thus it's getting a power spike after subclassing."
                        + list(2,
                            "Mystical Charge recharging now .5/1/1.5/2 instead of 0/.75/1.5/2.25 at +0/1/2/3 respectively.",
                            "Excess Charge proc chance is now 20/40/60/80 at +0/1/2/3, up from 0/25/50/75.")
                ),
                misc(list(2,
                        "Assassin's Enhanced Lethality is buffed to be in line with SHPD",
                        "Studded gloves damage now 1-6, up from 1-5.",
                        "Empowering scrolls talent no longer is time-limited, boost increased to +2/+4/+6.",
                        "Shops sell upgraded items for cheaper (again)."
                ))
            ),
            Nerfs(
                new ChangeButton(IRON_WILL,
                    "I've decided to take the opportunity to yank Iron Will's bonus upgrade mechanics. It's really unfun to use and it kinda warps the game to a large extent."
                            +list(2,
                                "Iron Will no longer gives bonus upgrades to the seal.",
                                "Iron Will now increases the seal's maximum shield by 1/3/5 and reduces the time it takes to fully recharge all shield by 30/60/90 turns."
                            )
                            +"This change makes iron will recharge the seal super fast, even when not upgraded. At the very start of the game, the time to fully recharge two shield is halved!"
                ),
                new ChangeButton(STRONGMAN, "I've implemented and (obviously) buffed Shattered's Strongman (13%/21%/29% up from 8%/13%/18%), now that Evan has finally gotten his act together and realized that I was right all along."
                        +"\n\n_+3:_"+list(":14-:17 strength -> +4 -> 18/19/20/21 down from 19/20/21/22",
                            "12-13 strength -> +3 -> 15/16 down from 17/18",
                            "10-11 strength -> +2 -> 12/13 down from 15/16",
                            "It is effectively around the same starting around 15 strength (16 if you were using a greataxe), but is increasingly worse prior to that."
                        ) + "\n_+2_:"+list("Gives a worse boost (2 down from 3) before the hero naturally attains 15 strength.",
                            "Gives a better boost once the hero has natural 20 strength"
                        )
                        +"\n_+1_:"+list("Gives +2 strength, up from its effective previous value of +1, once the hero naturally attains 16 strength."
                        )
                        +"\nOverall Strongman is a bit worse at +3 and a bit better at +2 and +1. Its ability to be exploited is down due to now being reliant on having strength, but in return it also gives true strength bonuses (thus opening up synergies with rings of force and might...)"
                ),
                new ChangeButton(BACKUP_BARRIER, "Backup barrier now generates 5/8 shield, down from 6/9, to reflect the Shattered nerf to Backup Barrier."))
        },

            {
                    new ChangeInfo("Abyssal", true, TITLE_COLOR, "",
                            new ChangeButton(Icons.get(Icons.DEPTH),"New chapter!",
                                    "Added endless abyss chapter from Summoning PD, excluding exclusive traps"),
                            new ChangeButton(avatar(RAT_KING,6), "Rat King Nerfs (rat king nerfs)",
                                    "Rat King's incomprehensible power bugged me during development of abyss too much.\n\n" +
                                            "_-_ Reduced HP by 60%"),
                            new ChangeButton(new Image(Assets.Sprites.HUNTRESS, 0, 15, 12, 15), "Huntress changes",
                                    "The Huntress recieved brand new starting armor to make her more stylish and strong.\n\n" +
                                            "_-_ With new armor, she can use _Super-Shots_ to shoot stronger arrow which does more damage with distance.\n\n" +
                                            "_-_ This ability has 35 turns cooldown.\n\n" +
                                            "_-_ The armor works as +1 cloak armor, but still can be viable in endgame."),
                            new ChangeButton(new ItemSprite(new RatKingArmor()), "Rat King's Wrath",
                                    "Reworked Rat King's Wrath into full armor ability with 3 exclusive talents:\n\n" +
                                            "_-_ _Auric Tesla Ability_ twists Wrath's components to have properties of 0.9.3 Shattered armor abilities.\n\n" +
                                            "_-_ _Quantum Positioning_ significantly extends Wrath's range.\n\n" +
                                            "_-_ _Rat Age Origins_ turns Smoke Bomb component into gas spreader which can freeze, confuse and damage enemies.\n\n" +
                                            "Molten Earth damage, Heroic Leap paralysis, Smoke Bomb invis and Spectral Blades range have been signficantly adjusted, cost increased from _35_ to _100_."),
                            new ChangeButton(new ItemSprite(RING_AGATE), "Ring caps",
                                    "Most of the rings have been capped at 2x of their effect (2.5x for Furor, 60% damage reduction for defense rings and no cap for SS and Wealth)."),

                            new ChangeButton(new Image(TALENT_ICONS, 16*5,16,16,16), "Talent changes",
                                    "_-_ Reworked _Energized Upgrade_ to recharge auxiliary wands when they are fully used.\n\n" +
                                            "_-_ Replaced _Mystical Upgrade_ with _Dual Wielding_, allowing player to use wands and missiles at same time.\n\n" +
                                            "_-_ Healing potion talents work on drinking from waterskin.\n\n" +
                                            "_-_ Replaced _Restored Nature_ with _Ivylash_, which augments super-shots with Rooting.\n\n" +
                                            "_-_ Huntress T3 and T4 talents boost the damage potential of Super-Shots."
                            )
                    )
            },
            // v0.0.1
        {
            new ChangeInfo("v0.0.1", true, TITLE_COLOR, "") {{
                addButton(new ChangeButton(Icons.get(Icons.INFO), "Developer Commentary", "This update is mostly just bugfixes and balance adjustments. More substantial changes should come when SHPD v0.9.3 is released.\n\nDo note that while things are intended to be broken, I'm aiming for a state where things are 'evenly' overpowered such that you can play any class or do any build and be like 'that's really damn good' for everything, rather than resetting (or just choosing talents!) for that same broken build every time."));
                addButton(new ChangeButton(WARRIOR, "This is intended to make Warrior (and Berserker) a little more balanced powerwise compared to other stuff in the game."
                    + "\n\nGeneral:" + list("Implemented buffed runic transference.",
                        "Nerfed Iron Stomach to be in line with SHPD.",
                        "Removed Iron Will charge speed increase and bonus shielding.",
                        "Strongman buffed to 1/3/5 from 1/2/3. It's good now I swear!")
                    + "\nBerserker:" + list("Fixed a bug that made it MUCH easier to get rage while recovering than intended.",
                        "Berserking Stamina recovery nerfed from 1.5/1/.5 to 1.67/1.33/1. Shielding unchanged.",
                        "Enraged Catalyst was bugged to be at shpd levels (17/33/50), but now that's intended.",
                        "One Man Army now only gives boost if you're in combat, rather than simply relying on line of sight.",
                        "Berserker no longer gets rage from non-melee sources. (this was an unintentional addition)",
                        "Current HP weighting now refers to actual current HP, instead of current HP after damage is dealt.")
                ));
                addButton(new ChangeButton(ROGUE, "Of all the classes, Rogue has the most exploits and uneven gameplay. These changes are intended to make him seem less repetitive/unbalanced.\n"
                    + list("Removed Mystical Meal-Horn of Plenty exploit, it was WAY more potent than I'd thought it would be.",
                        "Sucker punch now only works on the first surprise attack, numbers unchanged.",
                        "Marathon runner potency halved (is now 17%/33%/50%).",
                        "Mystical Upgrade is now more potent, gives instant 8/12 recharge to non-cloak artifacts, up from 5/8.",
                        "Enhanced Rings now stacks.")
                ));
                addButton(new ChangeButton(avatar(HUNTRESS, 6), "Multishot and Warden", ""
                        + "\nMultishot:" + list("now has a unique icon.",
                        "levels for targets are now evaluated independently for the purposes of shared upgrades.",
                        "the total duration of sniper's mark is now the sum of the durations of each marked target.",
                        "free shot is now kept for 1/2/3x duration at +1/2/3.",
                        "Improved target handling between floors and when some targets cannot be shot.")
                        + "\nWarden's power seems to currently be concentrated in Nature's Better Aid:"
                        + list("NBA seed boost now +17/33/50% down from +33/66/100%",
                        "NBA dew boost now +8/17/25% down from +33/66/100%",
                        "Shielding Dew now also gives upfront shielding equal to 25%/50%/75% of the heal when not at full HP.")));
                addButton(new ChangeButton(Icons.get(Icons.TALENT), "General Talents", ""
                        + list("Scholar's Intuition +2 now has a 50% chance to identify consumables when picking them up, down from 2/3",
                        "Scholar's Intuition +1 now identifies wand levels on the first zap.")
                        + list("Restored Nature now roots for 4/6 turns, up from 3/5")
                        + list("Seer Shot cooldown reduced from 20 to 5. (It was bugged to be 20 instead of 10)",
                        "Natural Dominance's Seer Shot cooldown increased to 20 from 10")
                        + "\n_-_ Fixed an oversight that caused Soul Siphon to cause wands to trigger soul mark at 200/300/400% effectiveness instead of 20/30/40%"
                ));
                addButton(new ChangeButton(new ItemSprite(KIT), "Epic Armor", ""
                    + list(2,
                        "Overcharging is now standardized to +100% per overcharge, down from 200% for everyone but Rat King.",
                        "Rogue Garb now has no limit on the distance it can jump.",
                        "Huntress Spectral Blades now has no distance limit.")
                    + "Rat King's Wrath:" + list("jump takes an extra turn.",
                        "Changed effects to make them more distinct (and fixed a couple omitted visual effects)",
                        "Changed the priority of the invisibility to actually cover the last turn of delay.")
                ));
                addButton(misc(list(2,
                        "Changed Rat King's hero description in Hero Select and the descriptions of many of his t3 talents.", "Added a note about how to toggle special actions for Rat King's subclass, and added a hotkey for toggling special action for desktop users.", "Added unique dialog for Ambitious Imp.")
                        + list("Huntress spirit bow now scales to +10, down from +12.", "Empowering Scrolls talent's effect now lasts for 40 turns, up from 30.", "Shopkeepers now offer reasonable deals for both buying and selling...", "ID VFX is now used for on-equip identification.", "Adjusted VFX for Hearty Meal and Test Subject.", "Added a shatter vfx effect when 'skipping' DK.", "Added a sfx for fully recovering, since apparently it's needed.",
                    "Troll Blacksmith now transitions directly into the reforge window after initially talking to him.")
                        + list("The level at which enemies stop giving exp has been increased again by 1 (now +2 compared to shattered)", "The level at which enemies stop dropping stuff has been decreased by 1 to match this new value. (now same as shattered)").trim() + list("explosive arrows can now proc on miss regardless of where they are shot.", "Adrenaline and Sniper's Mark visual fades now match their initial durations", "Dwarf King now gloats if he manages to kill you.")));
                addButton(bugFixes("Fixed many bugs involving soul marking that were directly impacting the power of soul mark:"
                    + list("Warlock's Touch can now actually provide its benefit immediately.",
                        "Fixed melee attacks proccing soul mark twice.",
                        "Fixed melee attacks coming from the hero being considered indirect damage and thus requiring Soul Siphon or Presence to work with soul mark at all.",
                        "Rare crashes when zapping the shopkeeper with a wand that can soul-mark.")
                    + "\nOther fixes:" + list("Huntress and Rat King getting more berries at +2 from their respective talents than intended.", "Being able to apply sniper's mark to multiple foes when it shouldn't be possible.",
                        "Typos")
                        + list("natural dominance incorrectly giving ally swap instead of king's presence", "Rat King getting Assassin's reach buffs instead of rogue", "Rogue not actually recieving innate 2x ring id speed.", "display bugs with rings, especially might and cursed rings.", "Multishot not checking if a given character is already marked when marking an enemy with sniper's mark", "'Hang' bug when attempting to target an out-of-range character with free shot from multi-shot", "Shattered bug where dk shows his alert during the whole second phase.")));
            }}
        },
            // v0.0.0
        {
            new ChangeInfo("v0.0.0",true, TITLE_COLOR, "This is a list of all the stuff I've changed from SHPD, for those who are curious."),
            new ChangeInfo("Classes",false,"") {{
                    addButton(new ChangeButton(avatar(RAT_KING,6), "New Class!",
                            "Added a new class! Rat King is supposed to be a sort of 'omniclass', with the perks of all the shattered classes combined.\n\n"
                                    + "He also has his subclass implemented, which, in line with the above, is of course all subclasses in one. I'm pretty proud of this one, give it a shot!"));
                    addButton(new ChangeButton(WARRIOR,""
                            + list("Iron Will now increases the amount of upgrades the seal can store.",
                                "All t1 talents buffed.")
                            + "\n_Berserker_:" + list("Rage gain now scales with HP as talents are upgraded",
                                "Added a new talent that makes him attack faster with more enemies around")
                            + "\n_Gladiator_:" + list("All finishers get an extra damage roll check for increased consistency via Skill talent")));
                    addButton(new ChangeButton(MAGE, list(2,
                            "Mage now has intrinsic +2 level to all wands for purposes of power and recharge",
                            "Battlemage now has a new talent that lets him spread his staff effects to all his attacks",
                            "Battlemage gets +2 effect on his staff.",
                            "Warlock can now soul mark with weapons and all damage can now trigger soul mark through new Warlock's Touch talent",
                            "Most talents buffed.",
                            "Empowering meal has been removed (for mage at least) and replaced with Energizing Meal, t2 meal replaced with something else.")));
                    addButton(new ChangeButton(ROGUE, list(2,
                            "Now gets an invisible +1 to weapons when cloak is active",
                            "Subclasses get more invisible upgrades to various item types.",
                            "Subclasses have their t3s buffed.",
                            "Cloak recharges faster",
                            "Talents buffed",
                            "Protective Shadows replaced by mending shadows.")));
                    addButton(new ChangeButton(HUNTRESS,
                        "Huntress has also recieved 'tweaks'!" + list(2,
                                "Spirit Bow now scales more.",
                                "The Spirit bow can be enchanted simply by using a scroll of upgrade on it.",
                                "Huntress has her innate heightened vision and missile durability back!",
                                "Huntress talents have been buffed across the board by ~50%.",
                                "Replaced invigorating meal with adrenalizing meal. ;)",
                                "Added new talents to both subclasses")));
                    addButton(new ChangeButton(new ItemSprite(new VelvetPouch()), "Bags",
                            "All heroes start with all bags. This should reduce the stress of inventory management throughout the game and also make gold more plentiful."));
                }},
            new ChangeInfo("Misc", false, "") {{
                    addButton(new ChangeButton(new WandOfFirebolt(), "Added wand of firebolt."
                            + '\n' + list("Does 0-9 damage with +1/+6 scaling instead of its vanilla/early shattered 1-8 with exponential scaling.",
                                "Is otherwise unchanged.")
                            + "\nThis should make it more consistently overpowered instead of requiring 10+ upgrades to reach actually astounding performance. I can probably call this my greatest mistake of all time. Oh well..."));
                    addButton(new ChangeButton(Icons.get(Icons.DEPTH),"Level gen changes", list(2,
                            "Amount of standard rooms reduced by 30%-50% in honor of precedent set by Rat King Dungeon.",
                            "Gold yield from Rat King's room increased by ~10x, general gold yield increased by 50%",
                            "Hero takes 1/3 damage from Grim traps.",
                            "Rat King room has a sign in it if playing as rat king as a homage to RKPD",
                            "Enemy level caps increased by 1")));
                    addButton(new ChangeButton(new KingSprite(),"Boss Changes","Certain bosses have recieved adjustments:" +
                            list(2,"Dwarf King fight start altered.",
                                "Dwarf King's phases can now be skipped with enough burst damage.",
                                "Dwarf King and DM-300 have altered quotes for Rat King",
                                "DM-300 fight starts directly upon moving initially, instead of when you see a pylon. Better know what you are doing!")));
                    addButton(new ChangeButton(new ItemSprite(new SpiritBow().enchant(new Explosive())),"Spirit Bow enchantments",
                            "The idea here is to lower the chance of 'misfiring' when enchanting the spirit bow dramatically."
                                    + list(2,
                                        "Added a new spirit-bow exclusive enchantment.",
                                        "It, along with grim, will replace blocking and lucky when using a scroll of enchantment on the bow (or a scroll of upgrade in the case of huntress).")));
                    addButton(new ChangeButton(new ItemSprite(new WarriorArmor()),"Class Armor","Who liked limits anyway? You can trade current HP for a full charge."));
                    addButton(new ChangeButton(Icons.get(Icons.LANGS),"Text Changes",
                            "I've changed some of the text in the game, including:"
                            + list("Class descriptions both ingame and in the select screen",
                                "Some enemy descriptions",
                                "Some boss quotes",
                                "This changelog")
                            + "\nHowever, since I only know English and do not have an active translation project, I've set the language to English. Sorry!"));
                }}
        },
    });

    private final ChangeLog changes;
    RKPD2Changes(ChangeLog changes) { this.changes = changes; }

    public void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        for (ChangeInfo[] section : changes.getChanges()) changeInfos.addAll(asList(section));
    }

    // utility
    private static ChangeButton bugFixes(String... messages) {
        return new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), get(ChangesScene.class, "bugfixes"), messages);
    }

    private static ChangeButton misc(String... messages) {
        return new ChangeButton(get(PREFS), get(ChangesScene.class, "misc"), messages);
    }

    // section types
    private static ChangeInfo NewContent(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "new"),
                false, TITLE_COLOR,
                "",
                buttons);
    }

    private static ChangeInfo Buffs(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "buffs"),
                false, POSITIVE,
                "",
                buttons);
    }

    private static ChangeInfo Changes(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "changes"),
                false, WARNING, "",
                buttons);
    }

    private static ChangeInfo Nerfs(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "nerfs"),
                false, NEGATIVE,
                "",
                buttons);
    }

    private static ChangeButton info(String message) {
        return new ChangeButton(get(INFO), "Developer Commentary", message);
    }

    // more utils

    /**
     * makes a list in the standard PD style.
     * [lineSpace] determines the number of spaces between each list item.
     * If you want to append extra spaces, you should do it at the end of the previous item, rather than at the start of that item.
     */
    private static String list(String... items) {
        return list(1, items);
    }

    private static String list(int lineSpace, String... items) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < lineSpace; j++) builder.append('\n');
        for (String item : items) {
            builder.append("_-_ ").append(item);
            for (int j = 0; j < lineSpace; j++) builder.append('\n');
        }
        return builder.toString();
    }
}