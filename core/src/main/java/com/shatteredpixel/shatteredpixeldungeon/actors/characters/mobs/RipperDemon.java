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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class RipperDemon extends Mob {

	{
		spriteClass = RipperSprite.class;

		healthPoints = healthMax = 60;
		evasionSkill = 22;
		viewDistance = Light.DISTANCE;

		EXP = 9; //for corrupting
		maxLvl = -2;

		HUNTING = new Hunting();

		baseSpeed = 1f;

		properties.add(Property.DEMONIC);
		properties.add(Property.UNDEAD);
	}

	@Override
	public float getSpawningWeight() {
		return 0;
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 15, 25 );
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 30;
	}

	@Override
	public float getAttackDelay() {
		return super.getAttackDelay()*0.5f;
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 4);
	}

	private static final String LAST_ENEMY_POS = "last_enemy_pos";
	private static final String LEAP_POS = "leap_pos";
	private static final String LEAP_CD = "leap_cd";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LAST_ENEMY_POS, lastEnemyPos);
		bundle.put(LEAP_POS, leapPos);
		bundle.put(LEAP_CD, leapCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lastEnemyPos = bundle.getInt(LAST_ENEMY_POS);
		leapPos = bundle.getInt(LEAP_POS);
		leapCooldown = bundle.getFloat(LEAP_CD);
	}

	private int lastEnemyPos = -1;

	@Override
	protected boolean playGameTurn() {
		if (state == WANDERING){
			leapPos = -1;
		}

		AiState lastState = state;
		boolean result = super.playGameTurn();
		if (paralysed <= 0) leapCooldown --;

		//if state changed from wandering to hunting, we haven't acted yet, don't update.
		if (!(lastState == WANDERING && state == HUNTING)) {
			if (enemy != null) {
				lastEnemyPos = enemy.position;
			} else {
				lastEnemyPos = Dungeon.hero.position;
			}
		}

		return result;
	}

	private int leapPos = -1;
	private float leapCooldown = 0;

	public class Hunting extends Mob.Hunting {

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted ) {

			if (leapPos != -1){

				leapCooldown = Random.NormalIntRange(2, 4);

				if (rooted){
					leapPos = -1;
					return true;
				}

				Ballistica b = new Ballistica(position, leapPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
				leapPos = b.collisionPos;

				final Character leapVictim = getCharacterOnPosition(leapPos);
				final int endPos;

				//ensure there is somewhere to land after leaping
				if (leapVictim != null){
					int bouncepos = -1;
					for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
						if ((bouncepos == -1 || Dungeon.level.trueDistance(position, leapPos+i) < Dungeon.level.trueDistance(position, bouncepos))
								&& getCharacterOnPosition(leapPos+i) == null && Dungeon.level.passable[leapPos+i]){
							bouncepos = leapPos+i;
						}
					}
					if (bouncepos == -1) {
						leapPos = -1;
						return true;
					} else {
						endPos = bouncepos;
					}
				} else {
					endPos = leapPos;
				}

				//do leap
				sprite.visible = Dungeon.level.heroFOV[position] || Dungeon.level.heroFOV[leapPos] || Dungeon.level.heroFOV[endPos];
				sprite.jump(position, leapPos, new Callback() {
					@Override
					public void call() {

						if (leapVictim != null && alignment != leapVictim.alignment){
							if (isTargetHitByAttack(RipperDemon.this, leapVictim, INFINITE_ACCURACY, false)) {
								Buff.affect(leapVictim, Bleeding.class).set(0.75f * getDamageRoll());
								leapVictim.sprite.flash();
								Sample.INSTANCE.play(Assets.Sounds.HIT);
							} else {
								enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.getDefenseVerb() );
								Sample.INSTANCE.play(Assets.Sounds.MISS);
							}
						}

						if (endPos != leapPos){
							addActor(new Pushing(RipperDemon.this, leapPos, endPos));
						}

						position = endPos;
						leapPos = -1;
						sprite.idle();
						Dungeon.level.occupyCell(RipperDemon.this);
						next();
					}
				});
				return false;
			}

			enemySeen = enemyInFOV;
			if (enemyInFOV && !isCharmedBy( enemy ) && canAttackEnemy( enemy )) {

				return attackCharacter( enemy );

			} else {

				if (enemyInFOV) {
					target = enemy.position;
				} else if (enemy == null) {
					state = WANDERING;
					target = Dungeon.level.randomDestination( RipperDemon.this );
					return true;
				}

				if (leapCooldown <= 0 && enemyInFOV && !rooted
						&& Dungeon.level.distance(position, enemy.position) >= 3) {

					int targetPos = enemy.position;
					if (lastEnemyPos != enemy.position){
						int closestIdx = 0;
						for (int i = 1; i < PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE.length; i++){
							if (Dungeon.level.trueDistance(lastEnemyPos, enemy.position +PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[i])
									< Dungeon.level.trueDistance(lastEnemyPos, enemy.position +PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[closestIdx])){
								closestIdx = i;
							}
						}
						targetPos = enemy.position + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[(closestIdx+4)%8];
					}

					Ballistica b = new Ballistica(position, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
					//try aiming directly at hero if aiming near them doesn't work
					if (b.collisionPos != targetPos && targetPos != enemy.position){
						targetPos = enemy.position;
						b = new Ballistica(position, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
					}
					if (b.collisionPos == targetPos){
						//get ready to leap
						leapPos = targetPos;
						//don't want to overly punish players with slow move or attack speed
						spendTimeAdjusted(GameMath.gate(getAttackDelay(), (int)Math.ceil(enemy.cooldown()), 3* getAttackDelay()));
						if (Dungeon.level.heroFOV[position] || Dungeon.level.heroFOV[leapPos]){
							GLog.w(Messages.get(RipperDemon.this, "leap"));
							sprite.parent.addToBack(new TargetedCell(leapPos, 0xFF0000));
							((RipperSprite)sprite).leapPrep( leapPos );
							Dungeon.hero.interruptHeroPlannedAction();
						}
						return true;
					}
				}

				int oldPos = position;
				if (target != -1 && moveCloserToTarget( target )) {

					spendTimeAdjusted( 1 / getSpeed() );
					return moveSprite( oldPos, position);

				} else {
					spendTimeAdjusted( TICK );
					if (!enemyInFOV) {
						sprite.showLost();
						state = WANDERING;
						target = Dungeon.level.randomDestination( RipperDemon.this );
					}
					return true;
				}
			}
		}

	}

}
