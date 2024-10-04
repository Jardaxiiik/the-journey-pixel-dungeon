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

import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonTurnsHandler;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public abstract class Actor extends ActorTimer implements Bundlable {

	public abstract boolean playGameTurn();

	// ---
	// DungeonActorsHandler - addActor, removeActor
	// ---
	public void onAdd() {}

	public void onRemove() {}

	//---
	// ID AND BUNDLES LOGIC
	// ---
	private int id = 0;
	private static int nextID = 1;
	private static final String TIME    = "time";
	private static final String ID      = "id";
	private static final String NEXTID = "nextid";

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( TIME, time );
		bundle.put( ID, id );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		time = bundle.getFloat( TIME );
		int incomingID = bundle.getInt( ID );
		if (DungeonActorsHandler.getById(incomingID) == null){
			id = incomingID;
		} else {
			id = nextID++;
		}
	}

	public static void storeNextID( Bundle bundle){
		bundle.put( NEXTID, nextID );
	}

	public static void restoreNextID( Bundle bundle){
		nextID = bundle.getInt( NEXTID );
	}

	public static void resetNextID(){
		nextID = 1;
	}

	public int getId() {
		if (id > 0) {
			return id;
		} else {
			return (id = nextID++);
		}
	}
}
