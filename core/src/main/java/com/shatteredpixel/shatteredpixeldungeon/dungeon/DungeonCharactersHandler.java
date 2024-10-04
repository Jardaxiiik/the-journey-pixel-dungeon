package com.shatteredpixel.shatteredpixeldungeon.dungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;

import java.util.HashSet;

public class DungeonCharactersHandler {
    private static HashSet<Character> characters = new HashSet<>();

    public static synchronized Character getCharacterOnPosition(int pos ) {
        for (Character ch : characters){
            if (ch.position == pos)
                return ch;
        }
        return null;
    }

    public static synchronized HashSet<Character> getCharacters() {
        return new HashSet<>(characters);
    }

    public static synchronized void addCharacter(Character ch, float time) {
        characters.add( ch );
        for (Buff buff : ch.getBuffs()) {
            DungeonActorsHandler.addActor(buff);
        }
    }

    public static synchronized void removeCharacter(Character character ) {
        characters.remove( character );
        DungeonActorsHandler.removeActor(character);
    }

    public static synchronized void clear() {
        characters.clear();
    }

    public static int getDistanceToOtherCharacter(Character one, Character other ) {
        return Dungeon.level.distance(one.position, other.position);
    }
}
