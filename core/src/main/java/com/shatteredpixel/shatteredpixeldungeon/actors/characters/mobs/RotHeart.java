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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RotHeartSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class RotHeart extends Mob {

	{
		spriteClass = RotHeartSprite.class;

		healthPoints = healthMax = 80;
		evasionSkill = 0;

		EXP = 4;

		state = PASSIVE;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.MINIBOSS);
	}

	@Override
	protected boolean playGameTurn() {
		alerted = false;
		return super.playGameTurn();
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		//TODO: when effect properties are done, change this to FIRE
		if (sourceOfDamage instanceof Burning) {
			destroy();
			sprite.die();
		} else {
			super.receiveDamageFromSource(dmg, sourceOfDamage);
		}
	}

	@Override
	public int getDamageReceivedFromEnemyReducedByDefense(Character enemy, int damage) {
		//rot heart spreads less gas in enclosed spaces
		int openNearby = 0;
		for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
			if (!Dungeon.level.solid[position +i]){
				openNearby++;
			}
		}

		GameScene.add(ActorLoop.seed(position, 5 + 3*openNearby, ToxicGas.class));

		return super.getDamageReceivedFromEnemyReducedByDefense(enemy, damage);
	}

	@Override
	public void travelToPosition(int cell) {
		//do nothing
	}

	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
		return false;
	}

	@Override
	public void destroy() {
		super.destroy();
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[Dungeon.level.mobs.size()])){
			if (mob instanceof RotLasher){
				mob.die(null);
			}
		}
	}

	@Override
	public void die(Object source) {
		super.die(source);
		Dungeon.level.dropItemOnPosition( new Rotberry.Seed(), position).sprite.drop();
		Statistics.questScores[1] = 2000;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public int getDamageRoll() {
		return 0;
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 0;
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 5);
	}
	
	{
		immunities.add( Paralysis.class );
		immunities.add( Amok.class );
		immunities.add( Sleep.class );
		immunities.add( ToxicGas.class );
		immunities.add( Terror.class );
		immunities.add( Dread.class );
		immunities.add( Vertigo.class );
	}

}
