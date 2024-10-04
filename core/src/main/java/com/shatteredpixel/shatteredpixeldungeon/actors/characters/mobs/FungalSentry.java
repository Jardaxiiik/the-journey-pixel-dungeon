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

import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FungalSentrySprite;
import com.watabou.utils.Random;

public class FungalSentry extends Mob {

	{
		spriteClass = FungalSentrySprite.class;

		healthPoints = healthMax = 200;
		evasionSkill = 12;

		EXP = 10;
		maxLvl = -2;

		state = WANDERING = new Waiting();

		properties.add(Property.IMMOVABLE);
		properties.add(Property.MINIBOSS);
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public float getSpawningWeight() {
		return 0;
	}

	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
		return false;
	}

	@Override
	protected boolean moveAwayFromTarget(int targetPosition) {
		return false;
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange(5, 10);
	}

	@Override
	//TODO attack is a little permissive atm?
	protected boolean canAttackEnemy(Character enemy ) {
		return super.canAttackEnemy(enemy)
				|| new Ballistica(position, enemy.position, Ballistica.MAGIC_BOLT).collisionPos == enemy.position;
	}

	//TODO if we want to allow them to be literally killed, probably should give them a heal if hero is out of FOV, or similar

	@Override
	public int attackProc_1(Character enemy, int damage) {
		Buff.affect(enemy, Poison.class).extend(6);
		return ActionAttack.attackProc(this,enemy, damage);
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 50;
	}

	{
		immunities.add( ToxicGas.class );
		immunities.add( Poison.class );
	}

	private class Waiting extends Mob.Wandering{

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted ) {
			//always notices the hero
			if (enemyInFOV) {

				return noticeEnemy();

			} else {

				return continueWandering();

			}
		}

		@Override
		protected boolean noticeEnemy() {
			spendTimeAdjusted(DungeonActors.TICK);
			return super.noticeEnemy();
		}
	}

}
