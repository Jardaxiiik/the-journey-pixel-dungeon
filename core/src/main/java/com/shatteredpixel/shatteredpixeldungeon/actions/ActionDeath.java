package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;

/**
 * ActionDeath handles death of Character.
 */
public class ActionDeath {
    public static void die(Character character, Object source ) {
        character.destroy();
        if (source != Chasm.class) character.sprite.die();
    }
}
