/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.SparseArray;

import java.util.HashSet;

public abstract class Actor implements Bundlable {
	
	public static final float TICK	= 1f;

	private float time;

	private int id = 0;

	//default priority values for general actor categories
	//note that some specific actors pick more specific values
	//e.g. a buff acting after all normal buffs might have priority BUFF_PRIO + 1
	protected static final int VFX_PRIO    = 100;   //visual effects take priority
	protected static final int HERO_PRIO   = 0;     //positive is before hero, negative after
	protected static final int BLOB_PRIO   = -10;   //blobs act after hero, before mobs
	protected static final int MOB_PRIO    = -20;   //mobs act between buffs and blobs
	protected static final int BUFF_PRIO   = -30;   //buffs act last in a turn
	private static final int   DEFAULT     = -100;  //if no priority is given, act after all else

	//used to determine what order actors act in if their time is equal. Higher values act earlier.
	protected int actPriority = DEFAULT;

	protected abstract boolean playGameTurn();

	//Always spends exactly the specified amount of time, regardless of time-influencing factors
	protected void spendTime(float time ){
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
		if (this.time < now + time) {
			this.time = now + time;
			//if time is very close to a whole number, round to a whole number to fix errors
			float ex = Math.abs(this.time % 1f);
			if (ex < .001f){
				this.time = Math.round(this.time);
			}
		}
	}
	
	public float cooldown() {
		return time - now;
	}

	public void clearTime() {
		time = 0;
	}

	public void timeToNow() {
		time = now;
	}
	
	protected void diactivate() {
		time = Float.MAX_VALUE;
	}
	
	protected void onAdd() {}
	
	protected void onRemove() {}

	private static final String TIME    = "time";
	private static final String ID      = "id";

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( TIME, time );
		bundle.put( ID, id );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		time = bundle.getFloat( TIME );
		int incomingID = bundle.getInt( ID );
		if (Actor.getById(incomingID) == null){
			id = incomingID;
		} else {
			id = nextID++;
		}
	}

	public int getId() {
		if (id > 0) {
			return id;
		} else {
			return (id = nextID++);
		}
	}

	// **********************
	// *** Static members ***
	// **********************
	
	private static HashSet<Actor> all = new HashSet<>();
	private static HashSet<Character> characters = new HashSet<>();
	private static volatile Actor current;

	private static SparseArray<Actor> ids = new SparseArray<>();
	private static int nextID = 1;

	private static float now = 0;
	
	public static float now(){
		return now;
	}
	
	public static synchronized void clear() {
		
		now = 0;

		all.clear();
		characters.clear();

		ids.clear();
	}

	public static synchronized void fixTime() {
		
		if (all.isEmpty()) return;
		
		float min = Float.MAX_VALUE;
		for (Actor a : all) {
			if (a.time < min) {
				min = a.time;
			}
		}

		//Only pull everything back by whole numbers
		//So that turns always align with a whole number
		min = (int)min;
		for (Actor a : all) {
			a.time -= min;
		}

		if (Dungeon.hero != null && all.contains( Dungeon.hero )) {
			Statistics.duration += min;
		}
		now -= min;
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
		
		current = null;
	}

	private static final String NEXTID = "nextid";

	public static void storeNextID( Bundle bundle){
		bundle.put( NEXTID, nextID );
	}

	public static void restoreNextID( Bundle bundle){
		nextID = bundle.getInt( NEXTID );
	}

	public static void resetNextID(){
		nextID = 1;
	}

	/*protected*/public void next() {
		if (current == this) {
			current = null;
		}
	}

	public static boolean processing(){
		return current != null;
	}

	public static int getCurrentActorPriority() {
		return current != null ? current.actPriority : DEFAULT;
	}
	
	public static boolean keepActorThreadAlive = true;
	
	public static void process() {
		
		boolean doNext;
		boolean interrupted = false;

		do {
			
			current = null;
			if (!interrupted) {
				float earliest = Float.MAX_VALUE;

				for (Actor actor : all) {
					
					//some actors will always go before others if time is equal.
					if (actor.time < earliest ||
							actor.time == earliest && (current == null || actor.actPriority > current.actPriority)) {
						earliest = actor.time;
						current = actor;
					}
					
				}
			}

			if  (current != null) {

				now = current.time;
				Actor acting = current;

				if (acting instanceof Character && ((Character) acting).sprite != null) {
					// If it's character's turn to act, but its sprite
					// is moving, wait till the movement is over
					try {
						synchronized (((Character)acting).sprite) {
							if (((Character)acting).sprite.isMoving) {
								((Character) acting).sprite.wait();
							}
						}
					} catch (InterruptedException e) {
						interrupted = true;
					}
				}
				
				interrupted = interrupted || Thread.interrupted();
				
				if (interrupted){
					doNext = false;
					current = null;
				} else {
					doNext = acting.playGameTurn();
					if (doNext && (Dungeon.hero == null || !Dungeon.hero.isAlive())) {
						doNext = false;
						current = null;
					}
				}
			} else {
				doNext = false;
			}

			if (!doNext){
				synchronized (Thread.currentThread()) {
					
					interrupted = interrupted || Thread.interrupted();
					
					if (interrupted){
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
	
	public static void addActor(Actor actor ) {
		addActor( actor, now );
	}
	
	public static void addActorWithDelay(Actor actor, float delay ) {
		addActor( actor, now + Math.max(delay, 0) );
	}
	
	private static synchronized void addActor(Actor actor, float time ) {
		
		if (all.contains( actor )) {
			return;
		}

		ids.put( actor.getId(),  actor );

		all.add( actor );
		actor.time += time;
		actor.onAdd();
		
		if (actor instanceof Character) {
			Character ch = (Character)actor;
			characters.add( ch );
			for (Buff buff : ch.getBuffs()) {
				addActor(buff);
			}
		}
	}
	
	public static synchronized void removeActor(Actor actor ) {
		
		if (actor != null) {
			all.remove( actor );
			characters.remove( actor );
			actor.onRemove();

			if (actor.id > 0) {
				ids.remove( actor.id );
			}
		}
	}

	//'freezes' a character in time for a specified amount of time
	//USE CAREFULLY! Manipulating time like this is useful for some gameplay effects but is tricky
	public static void makeCharacterSpendTime(Character ch, float time ){
		ch.spendTime(time);
		for (Buff b : ch.getBuffs()){
			b.spendTime(time);
		}
	}
	
	public static synchronized Character getCharacterOnPosition(int pos ) {
		for (Character ch : characters){
			if (ch.position == pos)
				return ch;
		}
		return null;
	}

	public static synchronized Actor getById(int id ) {
		return ids.get( id );
	}

	public static synchronized HashSet<Actor> getAll() {
		return new HashSet<>(all);
	}

	public static synchronized HashSet<Character> getCharacters() { return new HashSet<>(characters); }
}
