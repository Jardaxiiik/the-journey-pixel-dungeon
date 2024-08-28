package com.shatteredpixel.shatteredpixeldungeon.traits;

import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.traits.traitEffects.TraitEffect;

public class Action {
    public enum ActionType {
        GET_RANGE("RANGE"),
        ATTACK("ATTACK");

        private String name;
        ActionType(String name){
            this.name = name;
        }
    }
    public static void getAttackRange(Character a) {

    }
    public static void actionPerformed(Character a, ActionType type) {
        int i = 0;
        for(TraitEffect t : a.getAttackTraitEffects()) {
            t.doSomething();
        }
    }

    public static void actionAttack(Character a, Character target) {

    }

    public static void actionDefend(Character a, Character attacker) {

    }

    public static void actionWait(Character a) {

    }

    public static void actionWalk(Character a) {

    }

    public static void actionUseItem(Character a, Item itemUsed) {

    }

    public static void actionUseRelic(Character a) {

    }
}

