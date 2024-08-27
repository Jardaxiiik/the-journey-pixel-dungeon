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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSapperSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class GnollSapper extends Mob {

	{
		//always acts after guards, makes it easier to kite them into attacks
		actPriority = MOB_PRIO -1;

		spriteClass = GnollSapperSprite.class;

		healthPoints = healthMax = 45;
		evasionSkill = 15;

		EXP = 10;
		maxLvl = -2;

		properties.add(Property.MINIBOSS);

		HUNTING = new Hunting();
		WANDERING = new Wandering();
		state = SLEEPING;
	}

	public int spawnPos;
	private int partnerID = -1;

	private int abilityCooldown = Random.NormalIntRange(4, 6);
	private boolean lastAbilityWasRockfall = false;

	public int throwingRockFromPos = -1;
	public int throwingRockToPos = -1;

	public void linkPartner(Character c){
		losePartner();
		partnerID = c.getId();
		if (c instanceof GnollGuard) {
			((GnollGuard) c).linkSapper(this);
		} else if (c instanceof GnollGeomancer){
			((GnollGeomancer) c).linkSapper(this);
		}
	}

	public void losePartner(){
		if (partnerID != -1){
			if (getById(partnerID) instanceof GnollGuard) {
				((GnollGuard) getById(partnerID)).loseSapper();
			} else if (getById(partnerID) instanceof GnollGeomancer) {
				((GnollGeomancer) getById(partnerID)).loseSapper();
			}
			partnerID = -1;
		}
	}

	public Actor getPartner(){
		return getById(partnerID);
	}

	@Override
	public void die(Object source) {
		super.die(source);
		losePartner();
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 1, 6 );
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 18;
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		super.receiveDamageFromSource(dmg, sourceOfDamage);
		abilityCooldown -= dmg/10f;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 6);
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
	protected boolean playGameTurn() {
		if (throwingRockFromPos != -1){

			boolean attacked = Dungeon.level.map[throwingRockFromPos] == Terrain.MINE_BOULDER;

			if (attacked) {
				GnollGeomancer.doRockThrowAttack(this, throwingRockFromPos, throwingRockToPos);
			}

			throwingRockFromPos = -1;
			throwingRockToPos = -1;

			spendTimeAdjusted(TICK);
			return !attacked;
		} else {
			return super.playGameTurn();
		}

	}

	public class Hunting extends Mob.Hunting {
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV) {
				if (Dungeon.level.distance(spawnPos, target) > 3){
					//don't chase something more than a few tiles out of spawning position
					target = position;
				}
				return super.playGameTurn(enemyInFOV, justAlerted);
			} else {
				enemySeen = true;

				if (getById(partnerID) != null
						&& Dungeon.level.distance(position, enemy.position) <= 3){
					Mob partner = (Mob) getById(partnerID);
					if (partner.state == partner.SLEEPING){
						partner.notice();
					}
					if (enemy != partner) {
						partner.target = enemy.position;
						partner.startHunting(enemy);
					}
				}

				if (abilityCooldown-- <= 0){
					boolean targetNextToBarricade = false;
					for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
						if (Dungeon.level.map[enemy.position +i] == Terrain.BARRICADE
							|| Dungeon.level.map[enemy.position +i] == Terrain.ENTRANCE){
							targetNextToBarricade = true;
							break;
						}
					}

					// 50/50 to either throw a rock or do rockfall, but never do rockfall twice
					// unless target is next to a barricade, then always try to throw
					// unless nothing to throw, then always rockfall
					Ballistica aim = GnollGeomancer.prepRockThrowAttack(enemy, GnollSapper.this);
					if (aim != null && (targetNextToBarricade || lastAbilityWasRockfall || Random.Int(2) == 0)) {

						lastAbilityWasRockfall = false;
						throwingRockFromPos = aim.sourcePos;
						throwingRockToPos = aim.collisionPos;

						Ballistica warnPath = new Ballistica(aim.sourcePos, aim.collisionPos, Ballistica.STOP_SOLID);
						for (int i : warnPath.subPath(0, warnPath.dist)){
							sprite.parent.add(new TargetedCell(i, 0xFF0000));
						}

						Dungeon.hero.interrupt();
						abilityCooldown = Random.NormalIntRange(4, 6);
						spendTimeAdjusted(GameMath.gate(TICK, (int)Math.ceil(enemy.cooldown()), 3*TICK));
						return true;
					} else if (GnollGeomancer.prepRockFallAttack(enemy, GnollSapper.this, 2, true)) {
						lastAbilityWasRockfall = true;
						Dungeon.hero.interrupt();
						spendTimeAdjusted(GameMath.gate(TICK, (int)Math.ceil(enemy.cooldown()), 3*TICK));
						abilityCooldown = Random.NormalIntRange(4, 6);
						return true;
					}
				}

				//does not approach an enemy it can see, but does melee if in range
				if (canAttackEnemy(enemy)){
					return super.playGameTurn(enemyInFOV, justAlerted);
				} else {
					spendTimeAdjusted(TICK);
					return true;
				}
			}
		}
	}

	public class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination() {
			return spawnPos;
		}
	}

	private static final String SPAWN_POS = "spawn_pos";
	private static final String PARTNER_ID = "partner_id";

	private static final String ABILITY_COOLDOWN = "ability_cooldown";
	private static final String LAST_ABILITY_WAS_ROCKFALL = "last_ability_was_rockfall";

	private static final String ROCK_FROM_POS = "rock_from_pos";
	private static final String ROCK_TO_POS = "rock_to_pos";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PARTNER_ID, partnerID);
		bundle.put(SPAWN_POS, spawnPos);

		bundle.put(ABILITY_COOLDOWN, abilityCooldown);
		bundle.put(LAST_ABILITY_WAS_ROCKFALL, lastAbilityWasRockfall);

		bundle.put(ROCK_FROM_POS, throwingRockFromPos);
		bundle.put(ROCK_TO_POS, throwingRockToPos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		partnerID = bundle.getInt(PARTNER_ID);
		spawnPos = bundle.getInt(SPAWN_POS);

		abilityCooldown = bundle.getInt(ABILITY_COOLDOWN);
		lastAbilityWasRockfall = bundle.getBoolean(LAST_ABILITY_WAS_ROCKFALL);

		throwingRockFromPos = bundle.getInt(ROCK_FROM_POS);
		throwingRockToPos = bundle.getInt(ROCK_TO_POS);
	}
}
