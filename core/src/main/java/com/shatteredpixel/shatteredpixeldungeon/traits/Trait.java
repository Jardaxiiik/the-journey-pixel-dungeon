package com.shatteredpixel.shatteredpixeldungeon.traits;

import com.shatteredpixel.shatteredpixeldungeon.traits.traitEffects.TraitEffect;

public class Trait {
    public Class<Action> action;
    public Class<TraitEffect> effect;
    public int priority;
}
