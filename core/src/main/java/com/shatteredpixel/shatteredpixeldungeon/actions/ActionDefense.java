package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;

/**
 * ActionDefense handles defense reactions to attacks. (They may ignore the damage dealt)
 */
public class ActionDefense {
    public static int getArmorPointsRolled(Character character) {
        int armorPoints = 0;

        armorPoints += Random.NormalIntRange( 0 , Barkskin.currentLevel(character) );

        return armorPoints;
    }
    public static int getDamageReceivedFromEnemyReducedByDefense(Character attacker, Character target, int damage ) {

        Earthroot.Armor armor = target.getBuff( Earthroot.Armor.class );
        if (armor != null) {
            damage = armor.getDamageReducedByEarthroot( damage );
        }

        return damage;
    }

    public static int getShielding(Character character){
        if (!character.needsShieldUpdate){
            return character.cachedShield;
        }

        character.cachedShield = 0;
        for (ShieldBuff s : character.getBuffs(ShieldBuff.class)){
            character.cachedShield += s.shielding();
        }
        character.needsShieldUpdate = false;
        return character.cachedShield;
    }

    public static String getDefenseVerb(Character character) {
        return Messages.get(character, "def_verb");
    }
}
