package com.shatteredpixel.shatteredpixeldungeon.dungeon;

import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionHandler;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;

/**
 * represents the current Dungeon level our Hero is in.
 */
public class DungeonTurnsHandler {
    // ---
    // TIME HANDLER
    public static final float TICK = 1f;
    private static float now = 0;

    public static float getNow() {
        return now;
    }

    public static synchronized void clear() {
        now = 0;
    }

    public static synchronized void fixTime() {
        if (DungeonActorsHandler.getAll().isEmpty()) return;

        float min = Float.MAX_VALUE;
        for (Actor a : DungeonActorsHandler.getAll()) {
            if (a.getTime() < min) {
                min = a.getTime();
            }
        }

        //Only pull everything back by whole numbers
        //So that turns always align with a whole number
        min = (int) min;
        for (Actor a : DungeonActorsHandler.getAll()) {
            a.addTime(-min);
        }

        if (Dungeon.hero != null && DungeonActorsHandler.getAll().contains(Dungeon.hero)) {
            Statistics.duration += min;
        }
        now -= min;
    }

    // ---
    // TURN HANDLER
    private static volatile Actor current;
    public static boolean keepActorThreadAlive = true;
    public static void init() {
        current = null;
    }

    public static void nextActorToPlay(Actor actor) {
        if (current == actor) {
            current = null;
        }
    }
    public static void nextActorToPlayHero(Character hero) {
        if(hero.isAlive() && current == hero) {
            current = null;
        }
    }

    public static boolean processing() {
        return current != null;
    }

    public static int getCurrentActorPriority() {
        return current != null ? current.getActPriority() : Actor.DEFAULT;
    }

    public static void processActors() {
        boolean doNext;
        boolean interrupted = false;
        do {
            current = null;
            if (!interrupted) {
                float earliest = Float.MAX_VALUE;
                for (Actor actor : DungeonActorsHandler.getAll()) {
                    //some actors will always go before others if time is equal.
                    if (actor.getTime() < earliest ||
                            actor.getTime() == earliest && (current == null || actor.getActPriority() > current.getActPriority())) {
                        earliest = actor.getTime();
                        current = actor;
                    }

                }
            }

            if (current != null) {
                now = current.getTime();
                Actor acting = current;
                if (acting instanceof Character && ((Character) acting).sprite != null) {
                    // If it's character's turn to act, but its sprite
                    // is moving, wait till the movement is over
                    try {
                        synchronized (((Character) acting).sprite) {
                            if (((Character) acting).sprite.isMoving) {
                                ((Character) acting).sprite.wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }

                interrupted = interrupted || Thread.interrupted();

                if (interrupted) {
                    doNext = false;
                    current = null;
                } else {
                    doNext = ActionHandler.playGameTurn(acting);
                    if (doNext && (Dungeon.hero == null || !Dungeon.hero.isAlive())) {
                        doNext = false;
                        current = null;
                    }
                }
            } else {
                doNext = false;
            }

            if (!doNext) {
                synchronized (Thread.currentThread()) {

                    interrupted = interrupted || Thread.interrupted();

                    if (interrupted) {
                        current = null;
                        interrupted = false;
                    }

                    //signals to the gamescene that actor processing is finished for now
                    Thread.currentThread().notify();

                    try {
                        Thread.currentThread().wait();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }

        } while (keepActorThreadAlive);
    }

}
