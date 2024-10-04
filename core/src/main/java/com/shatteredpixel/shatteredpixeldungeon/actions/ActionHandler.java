package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;

/**
 * ActionHandler handles Actors turn.
 * Characters are handled using Actions system (see ActionHandler.playGameTurnCharacter)
 */
public class ActionHandler {
    public static boolean playGameTurn(Actor actor) {
        if (actor instanceof Character) {
            return playGameTurnCharacter((Character) actor);
        } else if (actor instanceof Buff) {
            return playGameTurnBuff((Buff) actor);
        } else if (actor instanceof ActorLoop) {
            return playGameTurnActorLoop((ActorLoop) actor);
        }
        return false;
    }

    public static boolean playGameTurnCharacter(Character character) {
        if (character.getFieldOfView() == null || character.getFieldOfView().length != Dungeon.level.length()){
            character.setFieldOfView(new boolean[Dungeon.level.length()]);
        }
        Dungeon.level.updateFieldOfView( character, character.getFieldOfView() );

        //throw any items that are on top of an immovable char
        if (character.getProperties().contains(Character.Property.IMMOVABLE)){
            ActionThrowItems.throwItems(character);
        }
        return false;
    }

    public static boolean playGameTurnBuff(Actor actor) {

        return false;
    }

    public static boolean playGameTurnActorLoop(Actor actor) {

        return false;
    }

    public static boolean playGameTurnMob(Actor actor) {

        return false;
    }

    public static boolean playGameTurnHero(Actor actor) {

        return false;
    }
}
