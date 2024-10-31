package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.watabou.utils.Random;

/**
 * ActionHit handles the chances of Action happening. Mostly used for ActionAttack and ActionDefense.
 * AccuracyTrait of the attack and DefensiveTrait of the defender's defense are compared to each other and the higher one has higher chance of winning.
 */
public class ActionHit {
    public static int INFINITE_ACCURACY = 1_000_000;
    public static int INFINITE_EVASION = 1_000_000;

    final public static boolean isTargetHitByAttack(Character attacker, Character defender, boolean magic ) {
        return isTargetHitByAttack(attacker, defender, magic ? 2f : 1f, magic);
    }

    public static boolean isTargetHitByAttack(Character attacker, Character defender, float accMulti, boolean magic ) {
        float acuStat = ActionHit.getAccuracyAgainstTarget( attacker, defender );
        float defStat = ActionHit.getEvasionAgainstAttacker( defender, attacker );

        if (defender instanceof Hero && ((Hero) defender).isHeroPlannedActionInterruptable){
            ((Hero) defender).interruptHeroPlannedAction();
        }

        //invisible chars always hit (for the hero this is surprise attacking)
        if (attacker.invisible > 0 && ActionAttack.canDoSurpriseAttack(attacker)){
            acuStat = INFINITE_ACCURACY;
        }

        if (defender.getBuff(MonkEnergy.MonkAbility.Focus.FocusBuff.class) != null && !magic){
            defStat = INFINITE_EVASION;
            defender.getBuff(MonkEnergy.MonkAbility.Focus.FocusBuff.class).detach();
            Buff.affect(defender, MonkEnergy.MonkAbility.Focus.FocusActivation.class, 0);
        }

        //if accuracy or evasion are large enough, treat them as infinite.
        //note that infinite evasion beats infinite accuracy
        if (defStat >= INFINITE_EVASION){
            return false;
        } else if (acuStat >= INFINITE_ACCURACY){
            return true;
        }

        float acuRoll = Random.Float( acuStat );
        if (attacker.getBuff(Bless.class) != null) acuRoll *= 1.25f;
        if (attacker.getBuff(  Hex.class) != null) acuRoll *= 0.8f;
        if (attacker.getBuff( Daze.class) != null) acuRoll *= 0.5f;
        for (ChampionEnemy buff : attacker.getBuffs(ChampionEnemy.class)){
            acuRoll *= buff.evasionAndAccuracyFactor();
        }
        acuRoll *= AscensionChallenge.statModifier(attacker);

        float defRoll = Random.Float( defStat );
        if (defender.getBuff(Bless.class) != null) defRoll *= 1.25f;
        if (defender.getBuff(  Hex.class) != null) defRoll *= 0.8f;
        if (defender.getBuff( Daze.class) != null) defRoll *= 0.5f;
        for (ChampionEnemy buff : defender.getBuffs(ChampionEnemy.class)){
            defRoll *= buff.evasionAndAccuracyFactor();
        }
        defRoll *= AscensionChallenge.statModifier(defender);

        return (acuRoll * accMulti) >= defRoll;
    }

    public static int getAccuracyAgainstTarget(Character caster, Character target ) {
        return 0;
    }

    public static int getEvasionAgainstAttacker(Character caster, Character target ) {
        return 0;
    }


}
