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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Mob;
import com.watabou.utils.Bundle;

public class DirectableAlly extends NPC {

	{
		alignment = Character.Alignment.ALLY;
		intelligentAlly = true;
		WANDERING = new Wandering();
		HUNTING = new Hunting();
		state = WANDERING;

		//before other mobs
		actPriority = MOB_PRIO + 1;

	}

	protected boolean attacksAutomatically = true;

	protected int defendingPos = -1;
	protected boolean movingToDefendPos = false;

	public void defendPos( int cell ){
		defendingPos = cell;
		movingToDefendPos = true;
		startHunting(null);
		state = WANDERING;
	}

	public void clearDefensingPos(){
		defendingPos = -1;
		movingToDefendPos = false;
	}

	public void followHero(){
		defendingPos = -1;
		movingToDefendPos = false;
		startHunting(null);
		state = WANDERING;
	}

	public void targetChar( Character ch ){
		defendingPos = -1;
		movingToDefendPos = false;
		startHunting(ch);
		target = ch.position;
	}

	@Override
	public void startHunting(Character ch) {
		enemy = ch;
		if (!movingToDefendPos && state != PASSIVE){
			state = HUNTING;
		}
	}

	public void directTocell( int cell ){
		if (!Dungeon.level.heroFOV[cell]
				|| DungeonCharactersHandler.getCharacterOnPosition(cell) == null
				|| (DungeonCharactersHandler.getCharacterOnPosition(cell) != Dungeon.hero && DungeonCharactersHandler.getCharacterOnPosition(cell).alignment != Character.Alignment.ENEMY)){
			defendPos( cell );
			return;
		}

		if (DungeonCharactersHandler.getCharacterOnPosition(cell) == Dungeon.hero){
			followHero();

		} else if (DungeonCharactersHandler.getCharacterOnPosition(cell).alignment == Character.Alignment.ENEMY){
			targetChar(DungeonCharactersHandler.getCharacterOnPosition(cell));

		}
	}

	private static final String DEFEND_POS = "defend_pos";
	private static final String MOVING_TO_DEFEND = "moving_to_defend";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DEFEND_POS, defendingPos);
		bundle.put(MOVING_TO_DEFEND, movingToDefendPos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(DEFEND_POS)) defendingPos = bundle.getInt(DEFEND_POS);
		movingToDefendPos = bundle.getBoolean(MOVING_TO_DEFEND);
	}

	private class Wandering extends Mob.Wandering {

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted ) {
			if ( enemyInFOV
					&& attacksAutomatically
					&& !movingToDefendPos
					&& (defendingPos == -1 || !Dungeon.level.heroFOV[defendingPos] || canAttackEnemy(enemy))) {

				enemySeen = true;

				notice();
				alerted = true;
				state = HUNTING;
				target = enemy.position;

			} else {

				enemySeen = false;

				int oldPos = position;
				target = defendingPos != -1 ? defendingPos : Dungeon.hero.position;
				//always move towards the hero when wandering
				if (moveCloserToTarget( target )) {
					spendTimeAdjusted( 1 / getSpeed() );
					if (position == defendingPos) movingToDefendPos = false;
					return moveSprite( oldPos, position);
				} else {
					//if it can't move closer to defending pos, then give up and defend current position
					if (movingToDefendPos){
						defendingPos = position;
						movingToDefendPos = false;
					}
					spendTimeAdjusted( DungeonActors.TICK );
				}

			}
			return true;
		}

	}

	private class Hunting extends Mob.Hunting {

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV && defendingPos != -1 && Dungeon.level.heroFOV[defendingPos] && !canAttackEnemy(enemy)){
				target = defendingPos;
				state = WANDERING;
				return true;
			}
			return super.playGameTurn(enemyInFOV, justAlerted);
		}

	}

}
