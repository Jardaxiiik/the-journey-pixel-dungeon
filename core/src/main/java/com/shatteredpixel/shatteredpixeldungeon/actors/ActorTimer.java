package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonTurnsHandler;

/**
 * This one is about time
 */
public abstract class ActorTimer {

    //default priority values for general actor categories
    //note that some specific actors pick more specific values
    //e.g. a buff acting after all normal buffs might have priority BUFF_PRIO + 1
    protected static final int VFX_PRIORITY = 100;   //visual effects take priority
    protected static final int HERO_PRIORITY = 0;     //positive is before hero, negative after
    protected static final int BLOB_PRIO   = -10;   //blobs act after hero, before mobs
    protected static final int MOB_PRIO    = -20;   //mobs act between buffs and blobs
    protected static final int BUFF_PRIO   = -30;   //buffs act last in a turn
    public static final int   DEFAULT     = -100;  //if no priority is given, act after all else

    protected float time;

    //used to determine what order actors act in if their time is equal. Higher values act earlier.
    protected int actPriority = DEFAULT;

    public int getActPriority() {
        return actPriority;
    }

    public float getTime() {
        return time;
    }

    public void addTime(float time) {
        this.time += time;
    }

    //Always spends exactly the specified amount of time, regardless of time-influencing factors
    public void spendTime(float time ){
        this.time += time;
        //if time is very close to a whole number, round to a whole number to fix errors
        float ex = Math.abs(this.time % 1f);
        if (ex < .001f){
            this.time = Math.round(this.time);
        }
    }

    //sends time, but the amount can be influenced
    protected void spendTimeAdjusted(float time ) {
        spendTime( time );
    }

    public void spentTimeRoundUp(){
        time = (float)Math.ceil(time);
    }

    protected void postpone( float time ) {
        if (this.time < DungeonTurnsHandler.getNow() + time) {
            this.time = DungeonTurnsHandler.getNow() + time;
            //if time is very close to a whole number, round to a whole number to fix errors
            float ex = Math.abs(this.time % 1f);
            if (ex < .001f){
                this.time = Math.round(this.time);
            }
        }
    }

    public float cooldown() {
        return time - DungeonTurnsHandler.getNow();
    }

    public void clearTime() {
        time = 0;
    }

    public void timeToNow() {
        time = DungeonTurnsHandler.getNow();
    }

    protected void deactivate() {
        time = Float.MAX_VALUE;
    }

}
