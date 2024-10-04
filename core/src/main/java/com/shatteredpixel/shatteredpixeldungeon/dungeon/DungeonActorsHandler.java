package com.shatteredpixel.shatteredpixeldungeon.dungeon;

import com.shatteredpixel.shatteredpixeldungeon.actions.ActionSpendTime;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Mob;
import com.watabou.utils.SparseArray;

import java.util.HashSet;

/**
 * represents the current Dungeon level our Hero is in.
 */
public class DungeonActorsHandler {
    private static HashSet<Actor> all = new HashSet<>();
    private static SparseArray<Actor> ids = new SparseArray<>(); // ids > 0 (actors have default 0, only when needed ids are assigned)

    public static synchronized void clear() {
        DungeonTurnsHandler.clear();
        all.clear();
        ids.clear();
    }

    public static void init() {
        addActor( Dungeon.hero );

        for (Mob mob : Dungeon.level.mobs) {
            addActor( mob );
        }

        //mobs need to remember their targets after every actor is added
        for (Mob mob : Dungeon.level.mobs) {
            mob.restoreEnemy();
        }

        for (ActorLoop actorLoop : Dungeon.level.blobs.values()) {
            addActor(actorLoop);
        }
    }

    public static void addActor(Actor actor ) {
        addActor( actor, DungeonTurnsHandler.getNow() );
    }

    public static void addActorWithDelay(Actor actor, float delay ) {
        addActor( actor, DungeonTurnsHandler.getNow() + Math.max(delay, 0) );
    }

    private static synchronized void addActor(Actor actor, float time ) {

        if (all.contains( actor )) {
            return;
        }

        ids.put( actor.getId(),  actor );

        all.add( actor );
        actor.addTime(time);
        actor.onAdd();
    }

    public static synchronized void removeActor(Actor actor ) {

        if (actor != null) {
            all.remove( actor );
            actor.onRemove();

            if (actor.getId() > 0) {
                ids.remove( actor.getId() );
            }
        }
    }

    //'freezes' a character in time for a specified amount of time
    //USE CAREFULLY! Manipulating time like this is useful for some gameplay effects but is tricky
    public static void makeCharacterSpendTime(Character ch, float time ){
        ActionSpendTime.spendTime(ch,time);
        for (Buff b : ch.getBuffs()){
            b.spendTime(time);
        }
    }

    public static synchronized Actor getById(int id ) {
        return ids.get( id );
    }

    public static synchronized HashSet<Actor> getAll() {
        return new HashSet<>(ids.valueList());
    }

}
