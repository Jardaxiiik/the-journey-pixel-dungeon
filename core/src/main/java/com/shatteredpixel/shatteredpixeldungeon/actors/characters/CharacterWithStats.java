package com.shatteredpixel.shatteredpixeldungeon.actors.characters;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;

public abstract class CharacterWithStats extends Actor {
    public boolean deathMarked = false;

    public int healthMax;
    public int healthPoints;
    public int cachedShield = 0;
    public boolean needsShieldUpdate = true;

    protected static final String BUNDLE_TAG_HP = "HP";
    protected static final String BUNDLE_TAG_HT = "HT";
    protected static final String BUNDLE_TAG_SHLD = "SHLD";
}
