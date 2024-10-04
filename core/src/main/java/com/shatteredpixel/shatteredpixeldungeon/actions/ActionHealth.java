package com.shatteredpixel.shatteredpixeldungeon.actions;

import static com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character.NO_ARMOR_PHYSICAL_SOURCES;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LifeLink;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

/**
 * ActionHealth handles damage received to the Character.
 * ActionDefense handles the damage before is dealt. Here we handle actions after the damage is dealt (meaning damage is always greater than 0).
 */
public class ActionHealth {
    public static void receiveDamageFromSource(Character target,int dmg, Object sourceOfDamage ) {

        if (!target.isAlive() || dmg < 0) {
            return;
        }

        if(target.isInvulnerableToEffectType(sourceOfDamage.getClass())){
            target.sprite.showStatus(CharSprite.POSITIVE, Messages.get(target, "invulnerable"));
            return;
        }

        for (ChampionEnemy buff : target.getBuffs(ChampionEnemy.class)){
            dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
        }

        if (!(sourceOfDamage instanceof LifeLink) && target.getBuff(LifeLink.class) != null){
            HashSet<LifeLink> links = target.getBuffs(LifeLink.class);
            for (LifeLink link : links.toArray(new LifeLink[0])){
                if (DungeonActorsHandler.getById(link.object) == null){
                    links.remove(link);
                    link.detach();
                }
            }
            dmg = (int)Math.ceil(dmg / (float)(links.size()+1));
            for (LifeLink link : links){
                Character ch = (Character) DungeonActorsHandler.getById(link.object);
                if (ch != null) {
                    ActionHealth.receiveDamageFromSource(ch,dmg, link);
                    if (!ch.isAlive()) {
                        link.detach();
                    }
                }
            }
        }

        Terror t = target.getBuff(Terror.class);
        if (t != null){
            t.recover();
        }
        Dread d = target.getBuff(Dread.class);
        if (d != null){
            d.recover();
        }
        Charm c = target.getBuff(Charm.class);
        if (c != null){
            c.recover(sourceOfDamage);
        }
        if (target.getBuff(Frost.class) != null){
            Buff.detach( target, Frost.class );
        }
        if (target.getBuff(MagicalSleep.class) != null){
            Buff.detach(target, MagicalSleep.class);
        }
        if (target.getBuff(Doom.class) != null && !target.isImmuneToEffectType(Doom.class)){
            dmg *= 1.67f;
        }
        if (target.alignment != Character.Alignment.ALLY && target.getBuff(DeathMark.DeathMarkTracker.class) != null){
            dmg *= 1.25f;
        }

        Class<?> srcClass = sourceOfDamage.getClass();
        if (target.isImmuneToEffectType( srcClass )) {
            dmg = 0;
        } else {
            dmg = Math.round( dmg * target.getResistanceMultiplierToEffectType( srcClass ));
        }

        //TODO improve this when I have proper damage source logic
        if (AntiMagic.RESISTS.contains(sourceOfDamage.getClass()) && target.getBuff(ArcaneArmor.class) != null){
            dmg -= Random.NormalIntRange(0, target.getBuff(ArcaneArmor.class).level());
            if (dmg < 0) dmg = 0;
        }

        if (target.getBuff(Sickle.HarvestBleedTracker.class) != null){
            if (target.isImmuneToEffectType(Bleeding.class)){
                target.sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(Messages.get(target, "immune")));
                target.getBuff(Sickle.HarvestBleedTracker.class).detach();
                return;
            }

            Bleeding b = target.getBuff(Bleeding.class);
            if (b == null){
                b = new Bleeding();
            }
            b.announced = false;
            b.set(dmg* target.getBuff(Sickle.HarvestBleedTracker.class).bleedFactor, Sickle.HarvestBleedTracker.class);
            b.attachTo(target);
            target.sprite.showStatus(CharSprite.WARNING, Messages.titleCase(b.name()) + " " + (int)b.level());
            target.getBuff(Sickle.HarvestBleedTracker.class).detach();
            return;
        }

        if (target.getBuff( Paralysis.class ) != null) {
            target.getBuff( Paralysis.class ).processDamage(dmg);
        }

        int shielded = dmg;
        //FIXME: when I add proper damage properties, should add an IGNORES_SHIELDS property to use here.
        if (!(sourceOfDamage instanceof Hunger)){
            for (ShieldBuff s : target.getBuffs(ShieldBuff.class)){
                dmg = s.absorbDamage(dmg);
                if (dmg == 0) break;
            }
        }
        shielded -= dmg;
        target.getCharacterHealth().addHealthPoints(-dmg);

        if (target.healthPoints > 0 && target.getBuff(Grim.GrimTracker.class) != null){

            float finalChance = target.getBuff(Grim.GrimTracker.class).maxChance;
            finalChance *= (float)Math.pow( ((target.healthMax - target.healthPoints) / (float) target.healthMax), 2);

            if (Random.Float() < finalChance) {
                int extraDmg = Math.round(target.healthPoints * target.getResistanceMultiplierToEffectType(Grim.class));
                dmg += extraDmg;
                target.getCharacterHealth().addHealthPoints(-extraDmg);

                target.sprite.emitter().burst( ShadowParticle.UP, 5 );
                if (!target.isAlive() && target.getBuff(Grim.GrimTracker.class).qualifiesForBadge){
                    Badges.validateGrimWeapon();
                }
            }
        }

        if (target.healthPoints < 0 && sourceOfDamage instanceof Character && target.alignment == Character.Alignment.ENEMY){
            if (((Character) sourceOfDamage).getBuff(Kinetic.KineticTracker.class) != null){
                int dmgToAdd = -target.healthPoints;
                dmgToAdd -= ((Character) sourceOfDamage).getBuff(Kinetic.KineticTracker.class).conservedDamage;
                dmgToAdd = Math.round(dmgToAdd * Weapon.Enchantment.genericProcChanceMultiplier((Character) sourceOfDamage));
                if (dmgToAdd > 0) {
                    Buff.affect((Character) sourceOfDamage, Kinetic.ConservedDamage.class).setBonus(dmgToAdd);
                }
                ((Character) sourceOfDamage).getBuff(Kinetic.KineticTracker.class).detach();
            }
        }

        if (target.sprite != null) {
            //defaults to normal damage icon if no other ones apply
            int                                                         icon = FloatingText.PHYS_DMG;
            if (Character.NO_ARMOR_PHYSICAL_SOURCES.contains(sourceOfDamage.getClass()))     icon = FloatingText.PHYS_DMG_NO_BLOCK;
            if (AntiMagic.RESISTS.contains(sourceOfDamage.getClass()))             icon = FloatingText.MAGIC_DMG;
            if (sourceOfDamage instanceof Pickaxe)                                 icon = FloatingText.PICK_DMG;

            //special case for sniper when using ranged attacks
            if (sourceOfDamage == Dungeon.hero
                    && Dungeon.hero.subClass == HeroSubClass.SNIPER
                    && !Dungeon.level.adjacent(Dungeon.hero.position, target.position)
                    && Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon){
                icon = FloatingText.PHYS_DMG_NO_BLOCK;
            }

            if (sourceOfDamage instanceof Hunger)                                  icon = FloatingText.HUNGER;
            if (sourceOfDamage instanceof Burning)                                 icon = FloatingText.BURNING;
            if (sourceOfDamage instanceof Chill || sourceOfDamage instanceof Frost)        icon = FloatingText.FROST;
            if (sourceOfDamage instanceof GeyserTrap || sourceOfDamage instanceof StormCloud) icon = FloatingText.WATER;
            if (sourceOfDamage instanceof Burning)                                 icon = FloatingText.BURNING;
            if (sourceOfDamage instanceof Electricity)                             icon = FloatingText.SHOCKING;
            if (sourceOfDamage instanceof Bleeding)                                icon = FloatingText.BLEEDING;
            if (sourceOfDamage instanceof ToxicGas)                                icon = FloatingText.TOXIC;
            if (sourceOfDamage instanceof Corrosion)                               icon = FloatingText.CORROSION;
            if (sourceOfDamage instanceof Poison)                                  icon = FloatingText.POISON;
            if (sourceOfDamage instanceof Ooze)                                    icon = FloatingText.OOZE;
            if (sourceOfDamage instanceof Viscosity.DeferedDamage)                 icon = FloatingText.DEFERRED;
            if (sourceOfDamage instanceof Corruption)                              icon = FloatingText.CORRUPTION;
            if (sourceOfDamage instanceof AscensionChallenge)                      icon = FloatingText.AMULET;

            target.sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(dmg + shielded), icon);
        }

        if (target.healthPoints < 0) target.getCharacterHealth().setHealthPoints(0);

        if (!target.isAlive()) {
            ActionDeath.die(target, sourceOfDamage );
        } else if (target.healthPoints == 0 && target.getBuff(DeathMark.DeathMarkTracker.class) != null){
            DeathMark.processFearTheReaper(target);
        }
    }

}
