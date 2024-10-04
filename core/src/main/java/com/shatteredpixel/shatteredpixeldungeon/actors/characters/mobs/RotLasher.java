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
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RotLasherSprite;
import com.watabou.utils.Random;

public class RotLasher extends Mob {

	{
		spriteClass = RotLasherSprite.class;

		healthPoints = healthMax = 80;
		evasionSkill = 0;

		EXP = 1;

		loot = ItemGenerator.Category.SEED;
		lootChance = 0.75f;

		state = WANDERING = new Waiting();
		viewDistance = 1;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.MINIBOSS);
	}

	@Override
    public boolean playGameTurn() {
		if (healthPoints < healthMax && (enemy == null || !Dungeon.level.adjacent(position, enemy.position))) {
			sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(Math.min(5, healthMax - healthPoints)), FloatingText.HEALING);
			healthPoints = Math.min(healthMax, healthPoints + 5);
		}
		return super.playGameTurn();
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if (sourceOfDamage instanceof Burning) {
			destroy();
			sprite.die();
		} else {
			super.receiveDamageFromSource(dmg, sourceOfDamage);
		}
	}

	@Override
	public int attackProc_1(Character enemy, int damage) {
		damage = ActionAttack.attackProc(this, enemy, damage );
		Buff.affect( enemy, Cripple.class, 2f );
		return ActionAttack.attackProc(this,enemy, damage);
	}

	@Override
	public boolean reset() {
		return true;
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
		return Random.NormalIntRange(10, 20);
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 25;
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 8);
	}
	
	{
		immunities.add( ToxicGas.class );
	}

	private class Waiting extends Mob.Wandering{

		@Override
		protected boolean noticeEnemy() {
			spendTimeAdjusted(DungeonActors.TICK);
			return super.noticeEnemy();
		}
	}
}
