package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;

/**
 * ActionSpeed handles the speed of actions.
 */
public class ActionSpeed {

    public static float getSpeed(Character character) {
        float speed = character.getBaseSpeed();
        if ( character.getBuff( Cripple.class ) != null ) speed /= 2f;
        if ( character.getBuff( Stamina.class ) != null) speed *= 1.5f;
        if ( character.getBuff( Adrenaline.class ) != null) speed *= 2f;
        if ( character.getBuff( Haste.class ) != null) speed *= 3f;
        if ( character.getBuff( Dread.class ) != null) speed *= 2f;
        return speed;
    }
}
