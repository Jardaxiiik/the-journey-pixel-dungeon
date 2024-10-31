package com.shatteredpixel.shatteredpixeldungeon.actors.characters;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;

public abstract class CharacterWithVision extends CharacterWithStates {
    public int viewDistance	= 8;
    public boolean[] fieldOfView = null;

}
