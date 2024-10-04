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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs;

import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Regrowth;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FungalSpinnerSprite;
import com.watabou.utils.PathFinder;

public class FungalSpinner extends Spinner {

	{
		spriteClass = FungalSpinnerSprite.class;

		healthPoints = healthMax = 40;
		evasionSkill = 16;

		EXP = 7;
		maxLvl = -2;
	}

	@Override
	protected void applyWebToCell(int cell) {
		GameScene.addMob(ActorLoop.seed(cell, 40, Regrowth.class));
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		int grassCells = 0;
		for (int i : PathFinder.OFFSETS_NEIGHBOURS9) {
			if (Dungeon.level.map[position +i] == Terrain.FURROWED_GRASS
					|| Dungeon.level.map[position +i] == Terrain.HIGH_GRASS){
				grassCells++;
			}
		}
		//first adjacent grass cell reduces damage taken by 30%, each one after reduces by another 10%
		if (grassCells > 0) dmg = Math.round(dmg * (8-grassCells)/10f);

		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}

	@Override
	public int attackProc_1(Character enemy, int damage) {
		return damage; //does not apply poison
	}

	{
		immunities.add(Regrowth.class);
	}

}
