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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

public class BlobEmitter extends com.watabou.noosa.particles.Emitter {
	
	private ActorLoop actorLoop;
	
	public BlobEmitter( ActorLoop actorLoop) {
		
		super();
		
		this.actorLoop = actorLoop;
		actorLoop.use( this );
	}

	public RectF bound = new RectF(0, 0, 1, 1);
	
	@Override
	protected void emit( int index ) {
		
		if (actorLoop.volume <= 0) {
			return;
		}

		if (actorLoop.area.isEmpty())
			actorLoop.setupArea();
		
		int[] map = actorLoop.cur;
		float size = DungeonTilemap.SIZE;

		int cell;
		for (int i = actorLoop.area.left; i < actorLoop.area.right; i++) {
			for (int j = actorLoop.area.top; j < actorLoop.area.bottom; j++) {
				cell = i + j*Dungeon.level.width();
				if (cell < Dungeon.level.heroFOV.length
						&& (Dungeon.level.heroFOV[cell] || actorLoop.alwaysVisible)
						&& map[cell] > 0) {
					float x = (i + Random.Float(bound.left, bound.right)) * size;
					float y = (j + Random.Float(bound.top, bound.bottom)) * size;
					factory.emit(this, index, x, y);
				}
			}
		}
	}
}
