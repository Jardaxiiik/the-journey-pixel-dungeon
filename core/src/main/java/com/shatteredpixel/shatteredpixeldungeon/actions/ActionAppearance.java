package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class ActionAppearance {

    private static int getCharacterStealth(Character character) {
        return character.stealth;
    }

    public static String getName(Character character){
        return Messages.get(character, "name");
    }

}
