package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * ActionMove handles Character movement.
 */
public class ActionMove {
    public static void moveToPosition(Character character, int step ) {
        moveToPosition( character, step, true );
    }

    //travelling may be false when a character is moving instantaneously, such as via teleportation
    public static void moveToPosition(Character character, int newPosition, boolean travelling ) {
        if (travelling && Dungeon.level.adjacent(newPosition, character.position) && character.getBuff( Vertigo.class ) != null) {
            character.sprite.interruptMotion();
            int newPos = character.position + PathFinder.OFFSETS_NEIGHBOURS8[Random.Int( 8 )];
            if (!(Dungeon.level.passable[newPos] || Dungeon.level.avoid[newPos])
                    || (character.getProperties().contains(Character.Property.LARGE) && !Dungeon.level.openSpace[newPos])
                    || DungeonCharactersHandler.getCharacterOnPosition( newPos ) != null)
                return;
            else {
                character.sprite.move(character.position, newPos);
                newPosition = newPos;
            }
        }

        if (Dungeon.level.map[character.position] == Terrain.OPEN_DOOR) {
            Door.leave(character.position);
        }

        character.position = newPosition;

        if (character != Dungeon.hero) {
            character.sprite.visible = Dungeon.level.heroFOV[character.position];
        }

        Dungeon.level.occupyCell(character );
    }


}
