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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM200Sprite;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class DM200 extends Mob {

	{
		spriteClass = DM200Sprite.class;

		healthPoints = healthMax = 80;
		defenseSkill = 12;

		EXP = 9;
		maxLvl = 17;

		loot = Random.oneOf(ItemGenerator.Category.WEAPON, ItemGenerator.Category.ARMOR);
		lootChance = 0.125f; //initially, see lootChance()

		properties.add(Property.INORGANIC);
		properties.add(Property.LARGE);

		HUNTING = new Hunting();
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 10, 25 );
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 20;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 8);
	}

	@Override
	public float getLootChance(){
		//each drop makes future drops 1/2 as likely
		// so loot chance looks like: 1/8, 1/16, 1/32, 1/64, etc.
		return super.getLootChance() * (float)Math.pow(1/2f, Dungeon.LimitedDrops.DM200_EQUIP.count);
	}

	public Item getLootItem() {
		Dungeon.LimitedDrops.DM200_EQUIP.count++;
		//uses probability tables for dwarf city
		if (loot == ItemGenerator.Category.WEAPON){
			return ItemGenerator.randomWeapon(4, true);
		} else {
			return ItemGenerator.randomArmor(4);
		}
	}

	private int ventCooldown = 0;

	private static final String VENT_COOLDOWN = "vent_cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VENT_COOLDOWN, ventCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		ventCooldown = bundle.getInt( VENT_COOLDOWN );
	}

	@Override
	protected boolean playGameTurn() {
		ventCooldown--;
		return super.playGameTurn();
	}

	public void onZapComplete(){
		zap();
		next();
	}

	private void zap( ){
		spendTimeAdjusted( TICK );
		ventCooldown = 30;

		Ballistica trajectory = new Ballistica(position, enemy.position, Ballistica.STOP_TARGET);

		for (int i : trajectory.subPath(0, trajectory.dist)){
			GameScene.add(ActorLoop.seed(i, 20, ToxicGas.class));
		}
		GameScene.add(ActorLoop.seed(trajectory.collisionPos, 100, ToxicGas.class));

	}

	protected boolean canVent(int target){
		if (ventCooldown > 0) return false;
		PathFinder.buildDistanceMap(target, BArray.not(Dungeon.level.solid, null), Dungeon.level.distance(position, target)+1);
		//vent can go around blocking terrain, but not through it
		if (PathFinder.distance[position] == Integer.MAX_VALUE){
			return false;
		}
		return true;
	}

	private class Hunting extends Mob.Hunting{

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV || canAttackEnemy(enemy)) {
				return super.playGameTurn(enemyInFOV, justAlerted);
			} else {
				enemySeen = true;
				target = enemy.position;

				int oldPos = position;

				if (getDistanceToOtherCharacter(enemy) >= 1 && Random.Int(100/ getDistanceToOtherCharacter(enemy)) == 0 && canVent(target)){
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.position);
						return false;
					} else {
						zap();
						return true;
					}

				} else if (moveCloserToTarget( target )) {
					spendTimeAdjusted( 1 / getSpeed() );
					return moveSprite( oldPos, position);

				} else if (canVent(target)) {
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.position);
						return false;
					} else {
						zap();
						return true;
					}

				} else {
					spendTimeAdjusted( TICK );
					return true;
				}

			}
		}
	}

}
