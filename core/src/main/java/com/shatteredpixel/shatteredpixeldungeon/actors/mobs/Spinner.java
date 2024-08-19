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
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpinnerSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Spinner extends Mob {

	{
		spriteClass = SpinnerSprite.class;

		healthPoints = healthMax = 50;
		defenseSkill = 17;

		EXP = 9;
		maxLvl = 17;

		loot = new MysteryMeat();
		lootChance = 0.125f;

		HUNTING = new Hunting();
		FLEEING = new Fleeing();
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange(10, 20);
	}

	@Override
	public int getAccuracyAgainstTarget(Character target) {
		return 22;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 6);
	}

	private int webCoolDown = 0;
	private int lastEnemyPos = -1;

	private static final String WEB_COOLDOWN = "web_cooldown";
	private static final String LAST_ENEMY_POS = "last_enemy_pos";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(WEB_COOLDOWN, webCoolDown);
		bundle.put(LAST_ENEMY_POS, lastEnemyPos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		webCoolDown = bundle.getInt( WEB_COOLDOWN );
		lastEnemyPos = bundle.getInt( LAST_ENEMY_POS );
	}
	
	@Override
	protected boolean playGameTurn() {
		if (state == HUNTING || state == FLEEING){
			webCoolDown--;
		}

		AiState lastState = state;
		boolean result = super.playGameTurn();

		//We only want to update target position once per turn, so if switched from wandering, wait for a moment
		//Also want to avoid updating when we visually shot a web this turn (don't want to change the position)
		if (!(lastState == WANDERING && state == HUNTING)) {
			if (!shotWebVisually){
				if (enemy != null && enemySeen) {
					lastEnemyPos = enemy.position;
				} else {
					lastEnemyPos = Dungeon.hero.position;
				}
			}
			shotWebVisually = false;
		}
		
		return result;
	}

	@Override
	public int attackProc(Character enemy, int damage) {
		damage = super.attackProc( enemy, damage );
		if (Random.Int(2) == 0) {
			int duration = Random.IntRange(7, 8);
			//we only use half the ascension modifier here as total poison dmg doesn't scale linearly
			duration = Math.round(duration * (AscensionChallenge.statModifier(this)/2f + 0.5f));
			Buff.affect(enemy, Poison.class).set(duration);
			webCoolDown = 0;
			state = FLEEING;
		}

		return damage;
	}
	
	private boolean shotWebVisually = false;

	public int webPos(){

		Character enemy = this.enemy;
		if (enemy == null) return -1;

		//don't web a non-moving enemy that we're going to attack
		if (state != FLEEING && enemy.position == lastEnemyPos && canAttackEnemy(enemy)){
			return -1;
		}
		
		Ballistica b;
		//aims web in direction enemy is moving, or between self and enemy if they aren't moving
		if (lastEnemyPos == enemy.position){
			b = new Ballistica( enemy.position, position, Ballistica.WONT_STOP );
		} else {
			b = new Ballistica( lastEnemyPos, enemy.position, Ballistica.WONT_STOP );
		}
		
		int collisionIndex = 0;
		for (int i = 0; i < b.path.size(); i++){
			if (b.path.get(i) == enemy.position){
				collisionIndex = i;
				break;
			}
		}

		//in case target is at the edge of the map and there are no more cells in the path
		if (b.path.size() <= collisionIndex+1){
			return -1;
		}

		int webPos = b.path.get( collisionIndex+1 );

		//ensure we aren't shooting the web through walls
		int projectilePos = new Ballistica(position, webPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).collisionPos;
		
		if (webPos != enemy.position && projectilePos == webPos && Dungeon.level.passable[webPos]){
			return webPos;
		} else {
			return -1;
		}
		
	}
	
	public void shootWeb(){
		int webPos = webPos();
		if (webPos != -1){
			int i;
			for (i = 0; i < PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE.length; i++){
				if ((enemy.position + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[i]) == webPos){
					break;
				}
			}
			
			//spread to the tile hero was moving towards and the two adjacent ones
			int leftPos = enemy.position + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[left(i)];
			int rightPos = enemy.position + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[right(i)];
			
			if (Dungeon.level.passable[leftPos]) applyWebToCell(leftPos);
			if (Dungeon.level.passable[webPos])  applyWebToCell(webPos);
			if (Dungeon.level.passable[rightPos])applyWebToCell(rightPos);
			
			webCoolDown = 10;

			if (Dungeon.level.heroFOV[enemy.position]){
				Dungeon.hero.interrupt();
			}
		}
		next();
	}

	protected void applyWebToCell(int cell){
		GameScene.add(ActorLoop.seed(cell, 20, Web.class));
	}
	
	private int left(int direction){
		return direction == 0 ? 7 : direction-1;
	}
	
	private int right(int direction){
		return direction == 7 ? 0 : direction+1;
	}

	{
		resistances.add(Poison.class);
	}
	
	{
		immunities.add(Web.class);
	}

	private class Hunting extends Mob.Hunting {

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV && webCoolDown <= 0 && lastEnemyPos != -1){
				if (webPos() != -1){
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( webPos() );
						shotWebVisually = true;
						return false;
					} else {
						shootWeb();
						return true;
					}
				}
			}

			return super.playGameTurn(enemyInFOV, justAlerted);
		}
	}

	private class Fleeing extends Mob.Fleeing {

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (getBuff( Terror.class ) == null && getBuff( Dread.class ) == null &&
					enemyInFOV && enemy.getBuff( Poison.class ) == null){
				state = HUNTING;
				return true;
			}

			if (enemyInFOV && webCoolDown <= 0 && lastEnemyPos != -1){
				if (webPos() != -1){
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( webPos() );
						shotWebVisually = true;
						return false;
					} else {
						shootWeb();
						return true;
					}
				}
			}
			return super.playGameTurn(enemyInFOV, justAlerted);
		}

	}
}
