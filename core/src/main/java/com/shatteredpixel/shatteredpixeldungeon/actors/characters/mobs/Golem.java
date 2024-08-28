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
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GolemSprite;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Golem extends Mob {
	
	{
		spriteClass = GolemSprite.class;
		
		healthPoints = healthMax = 120;
		evasionSkill = 15;
		
		EXP = 12;
		maxLvl = 22;

		loot = Random.oneOf(ItemGenerator.Category.WEAPON, ItemGenerator.Category.ARMOR);
		lootChance = 0.125f; //initially, see lootChance()

		properties.add(Property.INORGANIC);
		properties.add(Property.LARGE);

		WANDERING = new Wandering();
		HUNTING = new Hunting();
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 25, 30 );
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 28;
	}
	
	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 12);
	}

	@Override
	public float getLootChance() {
		//each drop makes future drops 1/2 as likely
		// so loot chance looks like: 1/8, 1/16, 1/32, 1/64, etc.
		return super.getLootChance() * (float)Math.pow(1/2f, Dungeon.LimitedDrops.GOLEM_EQUIP.count);
	}

	@Override
	public void dropLoot() {
		Imp.Quest.process( this );
		super.dropLoot();
	}

	public Item getLootItem() {
		Dungeon.LimitedDrops.GOLEM_EQUIP.count++;
		//uses probability tables for demon halls
		if (loot == ItemGenerator.Category.WEAPON){
			return ItemGenerator.randomWeapon(5, true);
		} else {
			return ItemGenerator.randomArmor(5);
		}
	}

	private boolean teleporting = false;
	private int selfTeleCooldown = 0;
	private int enemyTeleCooldown = 0;

	private static final String TELEPORTING = "teleporting";
	private static final String SELF_COOLDOWN = "self_cooldown";
	private static final String ENEMY_COOLDOWN = "enemy_cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TELEPORTING, teleporting);
		bundle.put(SELF_COOLDOWN, selfTeleCooldown);
		bundle.put(ENEMY_COOLDOWN, enemyTeleCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		teleporting = bundle.getBoolean( TELEPORTING );
		selfTeleCooldown = bundle.getInt( SELF_COOLDOWN );
		enemyTeleCooldown = bundle.getInt( ENEMY_COOLDOWN );
	}

	@Override
	protected boolean playGameTurn() {
		selfTeleCooldown--;
		enemyTeleCooldown--;
		if (teleporting){
			((GolemSprite)sprite).teleParticles(false);
			if (getCharacterOnPosition(target) == null && Dungeon.level.openSpace[target]) {
				ScrollOfTeleportation.appear(this, target);
				selfTeleCooldown = 30;
			} else {
				target = Dungeon.level.randomDestination(this);
			}
			teleporting = false;
			spendTimeAdjusted(TICK);
			return true;
		}
		return super.playGameTurn();
	}

	public void onZapComplete(){
		teleportEnemy();
		next();
	}

	public void teleportEnemy(){
		spendTimeAdjusted(TICK);

		int bestPos = enemy.position;
		for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
			if (Dungeon.level.passable[position + i]
				&& getCharacterOnPosition(position +i) == null
				&& Dungeon.level.trueDistance(position +i, enemy.position) > Dungeon.level.trueDistance(bestPos, enemy.position)){
				bestPos = position +i;
			}
		}

		if (enemy.getBuff(MagicImmune.class) != null){
			bestPos = enemy.position;
		}

		if (bestPos != enemy.position){
			ScrollOfTeleportation.appear(enemy, bestPos);
			if (enemy instanceof Hero){
				((Hero) enemy).interruptHeroPlannedAction();
				Dungeon.observe();
				GameScene.updateFog();
			}
		}

		enemyTeleCooldown = 20;
	}

	private boolean canTele(int target){
		if (enemyTeleCooldown > 0) return false;
		PathFinder.buildDistanceMap(target, BArray.not(Dungeon.level.solid, null), Dungeon.level.distance(position, target)+1);
		//zaps can go around blocking terrain, but not through it
		if (PathFinder.distance[position] == Integer.MAX_VALUE){
			return false;
		}
		return true;
	}

	private class Wandering extends Mob.Wandering{

		@Override
		protected boolean continueWandering() {
			enemySeen = false;

			int oldPos = position;
			if (target != -1 && moveCloserToTarget( target )) {
				spendTimeAdjusted( 1 / getSpeed() );
				return moveSprite( oldPos, position);
			} else if (!Dungeon.bossLevel() && target != -1 && target != position && selfTeleCooldown <= 0) {
				((GolemSprite)sprite).teleParticles(true);
				teleporting = true;
				spendTimeAdjusted( 2*TICK );
			} else {
				target = randomDestination();
				spendTimeAdjusted( TICK );
			}

			return true;
		}
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

				if (getDistanceToOtherCharacter(enemy) >= 1 && Random.Int(100/ getDistanceToOtherCharacter(enemy)) == 0
						&& !hasProperty(enemy, Property.IMMOVABLE) && canTele(target)){
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.position);
						return false;
					} else {
						teleportEnemy();
						return true;
					}

				} else if (moveCloserToTarget( target )) {
					spendTimeAdjusted( 1 / getSpeed() );
					return moveSprite( oldPos, position);

				} else if (!hasProperty(enemy, Property.IMMOVABLE) && canTele(target)) {
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.position);
						return false;
					} else {
						teleportEnemy();
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
