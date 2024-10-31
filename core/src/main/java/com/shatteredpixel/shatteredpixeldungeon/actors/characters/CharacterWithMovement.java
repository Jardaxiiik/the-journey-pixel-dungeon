package com.shatteredpixel.shatteredpixeldungeon.actors.characters;

import com.watabou.utils.PathFinder;

public abstract class CharacterWithMovement extends CharacterWithAppearance {
    public int position = 0;
    protected static final String BUNDLE_POS = "pos";

    public boolean isRooted = false;
    public boolean isFlying = false;
    public PathFinder.Path path;
    public float movementSpeed = 1;
    public float baseMovementSpeed = 1;
}
