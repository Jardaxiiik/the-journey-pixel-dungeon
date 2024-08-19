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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class PitRoom extends SpecialRoom {

	@Override //increase min size slightly to prevent tiny 3x3 wraith fights
	public int minWidth() { return 6; }
	public int minHeight() { return 6; }

	@Override //reduce max size to ensure well is visible in normal circumstances
	public int maxWidth() { return 9; }
	public int maxHeight() { return 9; }

	public void paint( Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		
		Door entrance = entrance();
		entrance.set( Door.Type.CRYSTAL );
		
		Point well = null;
		if (entrance.x == left) {
			well = new Point( right-1, Random.Int( 2 ) == 0 ? top + 1 : bottom - 1 );
		} else if (entrance.x == right) {
			well = new Point( left+1, Random.Int( 2 ) == 0 ? top + 1 : bottom - 1 );
		} else if (entrance.y == top) {
			well = new Point( Random.Int( 2 ) == 0 ? left + 1 : right - 1, bottom-1 );
		} else if (entrance.y == bottom) {
			well = new Point( Random.Int( 2 ) == 0 ? left + 1 : right - 1, top+1 );
		}
		Painter.set( level, well, Terrain.EMPTY_WELL );
		
		int remains = level.pointToCell(center());
		
		Item mainLoot = null;
		do {
			switch (Random.Int(3)){
				case 0:
					mainLoot = ItemGenerator.random(ItemGenerator.Category.RING);
					break;
				case 1:
					mainLoot = ItemGenerator.random(ItemGenerator.Category.ARTIFACT);
					break;
				case 2:
					mainLoot = ItemGenerator.random(Random.oneOf(
							ItemGenerator.Category.WEAPON,
							ItemGenerator.Category.ARMOR));
					break;
			}
		} while ( mainLoot == null || Challenges.isItemBlocked(mainLoot));
		level.dropItemOnPosition(mainLoot, remains).setHauntedIfCursed().type = Heap.Type.SKELETON;
		
		int n = Random.IntRange( 1, 2 );
		for (int i=0; i < n; i++) {
			level.dropItemOnPosition( prize( level ), remains ).setHauntedIfCursed();
		}

		level.dropItemOnPosition( new CrystalKey( Dungeon.depth ), remains );
	}
	
	private static Item prize( Level level ) {
		return ItemGenerator.random( Random.oneOf(
			ItemGenerator.Category.POTION,
			ItemGenerator.Category.SCROLL,
			ItemGenerator.Category.FOOD,
			ItemGenerator.Category.GOLD
		) );
	}
	
	@Override
	public boolean canPlaceTrap(Point p) {
		//the player is already weak after landing, and will likely need to kite the ghost.
		//having traps here just seems unfair
		return false;
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false; //We want the player to be able to see the well through the door
	}
}
