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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.emitters.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.emitters.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PiranhaSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Piranha extends Mob {
	
	{
		spriteClass = PiranhaSprite.class;

		baseSpeed = 2f;
		
		EXP = 0;
		
		loot = MysteryMeat.class;
		lootChance = 1f;
		
		SLEEPING = new Sleeping();
		WANDERING = new Wandering();
		HUNTING = new Hunting();
		
		state = SLEEPING;

	}
	
	public Piranha() {
		super();
		
		healthPoints = healthMax = 10 + Dungeon.depth * 5;
		defenseSkill = 10 + Dungeon.depth * 2;
	}
	
	@Override
	protected boolean playGameTurn() {
		
		if (!Dungeon.level.water[position]) {
			dieOnLand();
			return true;
		} else {
			return super.playGameTurn();
		}
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( Dungeon.depth, 4 + Dungeon.depth * 2 );
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 20 + Dungeon.depth * 2;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, Dungeon.depth);
	}

	@Override
	public boolean isSurprisedBy(Character enemy, boolean attacking) {
		if (enemy == Dungeon.hero && (!attacking || ((Hero)enemy).canDoSurpriseAttack())){
			if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
				fieldOfView = new boolean[Dungeon.level.length()];
				Dungeon.level.updateFieldOfView( this, fieldOfView );
			}
			return state == SLEEPING || !fieldOfView[enemy.position] || enemy.invisible > 0;
		}
		return super.isSurprisedBy(enemy, attacking);
	}

	public void dieOnLand(){
		die( null );
	}

	@Override
	public void die( Object source) {
		super.die(source);
		
		Statistics.piranhasKilled++;
		Badges.validatePiranhasKilled();
	}

	@Override
	public float getSpawningWeight() {
		return 0;
	}

	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
		
		if (rooted) {
			return false;
		}
		
		int step = Dungeon.findStep( this, targetPosition, Dungeon.level.water, fieldOfView, true );
		if (step != -1) {
			moveToPosition( step );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected boolean moveAwayFromTarget(int targetPosition) {
		int step = Dungeon.flee( this, targetPosition, Dungeon.level.water, fieldOfView, true );
		if (step != -1) {
			moveToPosition( step );
			return true;
		} else {
			return false;
		}
	}
	
	{
		for (Class c : new BlobImmunity().immunities()){
			if (c != Electricity.class && c != Freezing.class){
				immunities.add(c);
			}
		}
		immunities.add( Burning.class );
	}
	
	//if there is not a path to the enemy, piranhas act as if they can't see them
	private class Sleeping extends Mob.Sleeping{
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV) {
				PathFinder.buildDistanceMap(enemy.position, Dungeon.level.water, viewDistance);
				enemyInFOV = PathFinder.distance[position] != Integer.MAX_VALUE;
			}
			
			return super.playGameTurn(enemyInFOV, justAlerted);
		}
	}
	
	private class Wandering extends Mob.Wandering{
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV) {
				PathFinder.buildDistanceMap(enemy.position, Dungeon.level.water, viewDistance);
				enemyInFOV = PathFinder.distance[position] != Integer.MAX_VALUE;
			}
			
			return super.playGameTurn(enemyInFOV, justAlerted);
		}
	}
	
	private class Hunting extends Mob.Hunting{
		
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV) {
				PathFinder.buildDistanceMap(enemy.position, Dungeon.level.water, viewDistance);
				enemyInFOV = PathFinder.distance[position] != Integer.MAX_VALUE;
			}
			
			return super.playGameTurn(enemyInFOV, justAlerted);
		}
	}

	public static Piranha random(){
		if (Random.Int(50) == 0){
			return new PhantomPiranha();
		} else {
			return new Piranha();
		}
	}
}
