package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;

/**
 * ActionPassable modifies where can Character go. (through walls and such).
 */
public class ActionPassable {
    public static boolean[] modifyPassable(Character character, boolean[] passable){
        //do nothing by default, but some chars can pass over terrain that others can't
        return passable;
    }
}
