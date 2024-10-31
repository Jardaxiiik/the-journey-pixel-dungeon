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
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GuardSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Guard extends Mob {

	//they can only use their chains once
	private boolean chainsUsed = false;

	{
		spriteClass = GuardSprite.class;

		healthPoints = healthMax = 40;
		evasionSkill = 10;

		EXP = 7;
		maxLvl = 14;

		loot = ItemGenerator.Category.ARMOR;
		lootChance = 0.2f; //by default, see lootChance()

		properties.add(Property.UNDEAD);
		
		HUNTING = new Hunting();
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange(4, 12);
	}

	private boolean chain(int target){
		if (chainsUsed || enemy.getProperties().contains(Property.IMMOVABLE))
			return false;

		Ballistica chain = new Ballistica(position, target, Ballistica.PROJECTILE);

		if (chain.collisionPos != enemy.position
				|| chain.path.size() < 2
				|| Dungeon.level.pit[chain.path.get(1)])
			return false;
		else {
			int newPos = -1;
			for (int i : chain.subPath(1, chain.dist)){
				if (!Dungeon.level.solid[i] && DungeonCharactersHandler.getCharacterOnPosition(i) == null){
					newPos = i;
					break;
				}
			}

			if (newPos == -1){
				return false;
			} else {
				final int newPosFinal = newPos;
				this.target = newPos;

				if (sprite.visible || enemy.sprite.visible) {
					yell(Messages.get(this, "scorpion"));
					new Item().throwSound();
					Sample.INSTANCE.play(Assets.Sounds.CHAINS);
					sprite.parent.add(new Chains(sprite.center(),
							enemy.sprite.destinationCenter(),
							Effects.Type.CHAIN,
							new Callback() {
						public void call() {
							DungeonActorsHandler.addActor(new Pushing(enemy, enemy.position, newPosFinal, new Callback() {
								public void call() {
									pullEnemy(enemy, newPosFinal);
								}
							}));
							DungeonTurnsHandler.nextActorToPlay(this);();
						}
					}));
				} else {
					pullEnemy(enemy, newPos);
				}
			}
		}
		chainsUsed = true;
		return true;
	}

	private void pullEnemy(Character enemy, int pullPos ){
		enemy.position = pullPos;
		enemy.sprite.place(pullPos);
		Dungeon.level.occupyCell(enemy);
		Cripple.prolong(enemy, Cripple.class, 4f);
		if (enemy == Dungeon.hero) {
			Dungeon.hero.interruptHeroPlannedAction();
			Dungeon.observe();
			GameScene.updateFog();
		}
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 12;
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 7);
	}

	@Override
	public float getLootChance() {
		//each drop makes future drops 1/2 as likely
		// so loot chance looks like: 1/5, 1/10, 1/20, 1/40, etc.
		return super.getLootChance() * (float)Math.pow(1/2f, Dungeon.LimitedDrops.GUARD_ARM.count);
	}

	@Override
	public Item getLootItem() {
		Dungeon.LimitedDrops.GUARD_ARM.count++;
		return super.getLootItem();
	}

	private final String CHAINSUSED = "chainsused";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHAINSUSED, chainsUsed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		chainsUsed = bundle.getBoolean(CHAINSUSED);
	}
	
	private class Hunting extends Mob.Hunting{
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			
			if (!chainsUsed
					&& enemyInFOV
					&& !ActionBuffs.isCharmedBy(this,enemy)
					&& !canAttackEnemy( enemy )
					&& Dungeon.level.distance(position, enemy.position) < 5

					
					&& chain(enemy.position)){
				return !(sprite.visible || enemy.sprite.visible);
			} else {
				return super.playGameTurn( enemyInFOV, justAlerted );
			}
			
		}
	}
}
