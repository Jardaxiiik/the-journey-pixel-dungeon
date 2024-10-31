package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

/**
 * ActionAttack is action which handles damage dealing to target based on weapon's / item's attackTraits.
 * It is the only way to deal damage.
 */
public class ActionAttack {
    public static boolean attack(Character caster, Character target, float dmgMulti, float dmgBonus, float accMulti ) {

        if (target == null) return false;

        boolean visibleFight = Dungeon.level.heroFOV[caster.position] || Dungeon.level.heroFOV[target.position];

        if (target.isInvulnerableToEffectType(caster.getClass())) {

            if (visibleFight) {
                target.sprite.showStatus( CharSprite.POSITIVE, Messages.get(caster, "invulnerable") );

                Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
            }

            return false;

        } else if (ActionHit.isTargetHitByAttack( caster, target, accMulti, false )) {

            int armorPoints = Math.round(ActionDefense.getArmorPointsRolled(target) * AscensionChallenge.statModifier(target));

            if (caster instanceof Hero){
                Hero h = (Hero)caster;
                if (h.belongings.attackingWeapon() instanceof MissileWeapon
                        && h.subClass == HeroSubClass.SNIPER
                        && !Dungeon.level.adjacent(h.position, target.position)){
                    armorPoints = 0;
                }

                if (h.getBuff(MonkEnergy.MonkAbility.UnarmedAbilityTracker.class) != null){
                    armorPoints = 0;
                } else if (h.subClass == HeroSubClass.MONK) {
                    //3 turns with standard attack delay
                    Buff.prolong(h, MonkEnergy.MonkAbility.JustHitTracker.class, 4f);
                }
            }

            //we use a float here briefly so that we don't have to constantly round while
            // potentially applying various multiplier effects
            float dmg;
            Preparation prep = caster.getBuff(Preparation.class);
            if (prep != null){
                dmg = prep.damageRoll(caster);
                if (caster == Dungeon.hero && Dungeon.hero.hasTalent(Talent.BOUNTY_HUNTER)) {
                    Buff.affect(Dungeon.hero, Talent.BountyHunterTracker.class, 0.0f);
                }
            } else {
                dmg = ActionAttack.getDamageRoll(caster);
            }

            dmg = Math.round(dmg*dmgMulti);

            Berserk berserk = caster.getBuff(Berserk.class);
            if (berserk != null) dmg = berserk.damageFactor(dmg);

            if (caster.getBuff( Fury.class ) != null) {
                dmg *= 1.5f;
            }

            for (ChampionEnemy buff : caster.getBuffs(ChampionEnemy.class)){
                dmg *= buff.meleeDamageFactor();
            }

            dmg *= AscensionChallenge.statModifier(caster);

            //flat damage bonus is applied after positive multipliers, but before negative ones
            dmg += dmgBonus;

            //friendly endure
            Endure.EndureTracker endure = caster.getBuff(Endure.EndureTracker.class);
            if (endure != null) dmg = endure.damageFactor(dmg);

            //enemy endure
            endure = target.getBuff(Endure.EndureTracker.class);
            if (endure != null){
                dmg = endure.adjustDamageTaken(dmg);
            }

            if (target.getBuff(ScrollOfChallenge.ChallengeArena.class) != null){
                dmg *= 0.67f;
            }

            if (target.getBuff(MonkEnergy.MonkAbility.Meditate.MeditateResistance.class) != null){
                dmg *= 0.2f;
            }

            if ( caster.getBuff(Weakness.class) != null ){
                dmg *= 0.67f;
            }

            int finalDamage = ActionDefense.getDamageReceivedFromEnemyReducedByDefense( caster,target, Math.round(dmg) );
            //do not trigger on-hit logic if defenseProc returned a negative value
            if (finalDamage >= 0) {
                finalDamage = Math.max(finalDamage - armorPoints, 0);

                if (target.getBuff(Viscosity.ViscosityTracker.class) != null) {
                    finalDamage = target.getBuff(Viscosity.ViscosityTracker.class).deferDamage(finalDamage);
                    target.getBuff(Viscosity.ViscosityTracker.class).detach();
                }

                //vulnerable specifically applies after armor reductions
                if (target.getBuff(Vulnerable.class) != null) {
                    finalDamage *= 1.33f;
                }

                finalDamage = ActionAttack.attackProc(caster,target, finalDamage);
            }
            if (visibleFight) {
                if (finalDamage > 0 || !ActionSound.willBlockSound(target,Random.Float(0.96f, 1.05f))) {
                    ActionSound.playHitSound(caster,Random.Float(0.87f, 1.15f));
                }
            }

            // If the enemy is already dead, interrupt the attack.
            // This matters as defence procs can sometimes inflict self-damage, such as armor glyphs.
            if (!ActionHealth.isAlive(target)){
                return true;
            }

            ActionHealth.receiveDamageFromSource(target, finalDamage, caster );

            if (caster.getBuff(FireImbue.class) != null)  caster.getBuff(FireImbue.class).proc(target);
            if (caster.getBuff(FrostImbue.class) != null) caster.getBuff(FrostImbue.class).proc(target);

            if (ActionHealth.isAlive(target) && target.alignment != caster.alignment && prep != null && prep.canKO(target)){
                target.healthPoints = 0;
                if (!ActionHealth.isAlive(target)) {
                    ActionDeath.die(target,caster);
                } else {
                    //helps with triggering any on-damage effects that need to activate
                    ActionHealth.receiveDamageFromSource(target,-1, caster);
                    DeathMark.processFearTheReaper(target);
                }
                if (target.sprite != null) {
                    target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
                }
            }

            Talent.CombinedLethalityTriggerTracker combinedLethality = caster.getBuff(Talent.CombinedLethalityTriggerTracker.class);
            if (combinedLethality != null){
                if ( ActionHealth.isAlive(target) && target.alignment != caster.alignment && !Character.hasProperty(target, Character.Property.BOSS)
                        && !Character.hasProperty(target, Character.Property.MINIBOSS) && caster instanceof Hero &&
                        (target.healthPoints /(float)target.healthMax) <= 0.4f*((Hero)caster).pointsInTalent(Talent.COMBINED_LETHALITY)/3f) {
                    target.healthPoints = 0;
                    if (!ActionHealth.isAlive(target)) {
                        ActionDeath.die(target,caster);
                    } else {
                        //helps with triggering any on-damage effects that need to activate
                        ActionHealth.receiveDamageFromSource(target,-1, caster);
                        DeathMark.processFearTheReaper(target);
                    }
                    if (target.sprite != null) {
                        target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Talent.CombinedLethalityTriggerTracker.class, "executed"));
                    }
                }
                combinedLethality.detach();
            }

            if (target.sprite != null) {
                target.sprite.bloodBurstA(caster.sprite.center(), finalDamage);
                target.sprite.flash();
            }

            if (!ActionHealth.isAlive(target) && visibleFight) {
                if (target == Dungeon.hero) {

                    if (caster == Dungeon.hero) {
                        return true;
                    }

                    if (caster instanceof WandOfLivingEarth.EarthGuardian
                            || caster instanceof MirrorImage || caster instanceof PrismaticImage){
                        Badges.validateDeathFromFriendlyMagic();
                    }
                    Dungeon.fail( caster );
                    GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", ActionAppearance.getName(caster))) );

                } else if (caster == Dungeon.hero) {
                    GLog.i( Messages.capitalize(Messages.get(Character.class, "defeat", ActionAppearance.getName(target))) );
                }
            }

            return true;

        } else {

            target.sprite.showStatus( CharSprite.NEUTRAL, ActionDefense.getDefenseVerb(target) );
            if (visibleFight) {
                //TODO enemy.defenseSound? currently miss plays for monks/crab even when they parry
                Sample.INSTANCE.play(Assets.Sounds.MISS);
            }

            return false;

        }
    }

    public static int attackProc(Character victim,Character enemy, int damage ) {
        for (ChampionEnemy buff : victim.getBuffs(ChampionEnemy.class)){
            buff.onAttackProc( enemy );
        }
        return damage;
    }

    public static int getDamageRoll(Character character) {
        return 1;
    }

    public static boolean canDoSurpriseAttack(Character character){
        return true;
    }

}
