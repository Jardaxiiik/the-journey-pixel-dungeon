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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Necromancer extends Mob {
	
	{
		spriteClass = NecromancerSprite.class;
		
		healthPoints = healthMax = 40;
		evasionSkill = 14;
		
		EXP = 7;
		maxLvl = 14;
		
		loot = new PotionOfHealing();
		lootChance = 0.2f; //see lootChance()
		
		properties.add(Property.UNDEAD);
		
		HUNTING = new Hunting();
	}
	
	public boolean summoning = false;
	public int summoningPos = -1;
	
	protected boolean firstSummon = true;
	
	private NecroSkeleton mySkeleton;
	private int storedSkeletonID = -1;

	@Override
	protected boolean playGameTurn() {
		if (summoning && state != HUNTING){
			summoning = false;
			if (sprite instanceof NecromancerSprite) ((NecromancerSprite) sprite).cancelSummoning();
		}
		return super.playGameTurn();
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 5);
	}
	
	@Override
	public float getLootChance() {
		return super.getLootChance() * ((6f - Dungeon.LimitedDrops.NECRO_HP.count) / 6f);
	}
	
	@Override
	public Item getLootItem(){
		Dungeon.LimitedDrops.NECRO_HP.count++;
		return super.getLootItem();
	}
	
	@Override
	public void die(Object source) {
		if (storedSkeletonID != -1){
			Actor ch = getById(storedSkeletonID);
			storedSkeletonID = -1;
			if (ch instanceof NecroSkeleton){
				mySkeleton = (NecroSkeleton) ch;
			}
		}
		
		if (mySkeleton != null && mySkeleton.isAlive()){
			mySkeleton.die(null);
		}
		
		super.die(source);
	}

	@Override
	protected boolean canAttackEnemy(Character enemy) {
		return false;
	}

	private static final String SUMMONING = "summoning";
	private static final String FIRST_SUMMON = "first_summon";
	private static final String SUMMONING_POS = "summoning_pos";
	private static final String MY_SKELETON = "my_skeleton";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( SUMMONING, summoning );
		bundle.put( FIRST_SUMMON, firstSummon );
		if (summoning){
			bundle.put( SUMMONING_POS, summoningPos);
		}
		if (mySkeleton != null){
			bundle.put( MY_SKELETON, mySkeleton.getId() );
		} else if (storedSkeletonID != -1){
			bundle.put( MY_SKELETON, storedSkeletonID );
		}
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		summoning = bundle.getBoolean( SUMMONING );
		if (bundle.contains(FIRST_SUMMON)) firstSummon = bundle.getBoolean(FIRST_SUMMON);
		if (summoning){
			summoningPos = bundle.getInt( SUMMONING_POS );
		}
		if (bundle.contains( MY_SKELETON )){
			storedSkeletonID = bundle.getInt( MY_SKELETON );
		}
	}
	
	public void onZapComplete(){
		if (mySkeleton == null || mySkeleton.sprite == null || !mySkeleton.isAlive()){
			return;
		}
		
		//heal skeleton first
		if (mySkeleton.healthPoints < mySkeleton.healthMax){

			if (sprite.visible || mySkeleton.sprite.visible) {
				sprite.parent.add(new Beam.HealthRay(sprite.center(), mySkeleton.sprite.center()));
			}
			
			mySkeleton.healthPoints = Math.min(mySkeleton.healthPoints + mySkeleton.healthMax /5, mySkeleton.healthMax);
			if (mySkeleton.sprite.visible) {
				mySkeleton.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( mySkeleton.healthMax /5 ), FloatingText.HEALING );
			}
			
		//otherwise give it adrenaline
		} else if (mySkeleton.getBuff(Adrenaline.class) == null) {

			if (sprite.visible || mySkeleton.sprite.visible) {
				sprite.parent.add(new Beam.HealthRay(sprite.center(), mySkeleton.sprite.center()));
			}
			
			Buff.affect(mySkeleton, Adrenaline.class, 3f);
		}
		
		next();
	}

	public void summonMinion(){
		if (getCharacterOnPosition(summoningPos) != null) {

			//cancel if character cannot be moved
			if (hasProperty(getCharacterOnPosition(summoningPos), Property.IMMOVABLE)){
				summoning = false;
				((NecromancerSprite)sprite).finishSummoning();
				spendTimeAdjusted(TICK);
				return;
			}

			int pushPos = position;
			for (int c : PathFinder.OFFSETS_NEIGHBOURS8) {
				if (getCharacterOnPosition(summoningPos + c) == null
						&& Dungeon.level.passable[summoningPos + c]
						&& (Dungeon.level.openSpace[summoningPos + c] || !hasProperty(getCharacterOnPosition(summoningPos), Property.LARGE))
						&& Dungeon.level.trueDistance(position, summoningPos + c) > Dungeon.level.trueDistance(position, pushPos)) {
					pushPos = summoningPos + c;
				}
			}

			//push enemy, or wait a turn if there is no valid pushing position
			if (pushPos != position) {
				Character ch = getCharacterOnPosition(summoningPos);
				addActor( new Pushing( ch, ch.position, pushPos ) );

				ch.position = pushPos;
				Dungeon.level.occupyCell(ch );

			} else {

				Character blocker = getCharacterOnPosition(summoningPos);
				if (blocker.alignment != alignment){
					blocker.receiveDamageFromSource( Random.NormalIntRange(2, 10), new SummoningBlockDamage() );
					if (blocker == Dungeon.hero && !blocker.isAlive()){
						Badges.validateDeathFromEnemyMagic();
						Dungeon.fail(this);
						GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", getName())) );
					}
				}

				spendTimeAdjusted(TICK);
				return;
			}
		}

		summoning = firstSummon = false;

		mySkeleton = new NecroSkeleton();
		mySkeleton.position = summoningPos;
		GameScene.add( mySkeleton );
		Dungeon.level.occupyCell( mySkeleton );
		((NecromancerSprite)sprite).finishSummoning();

		for (Buff b : getBuffs(AllyBuff.class)){
			Buff.affect(mySkeleton, b.getClass());
		}
		for (Buff b : getBuffs(ChampionEnemy.class)){
			Buff.affect( mySkeleton, b.getClass());
		}
	}

	public static class SummoningBlockDamage{}
	
	private class Hunting extends Mob.Hunting{
		
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			enemySeen = enemyInFOV;

			if (enemySeen){
				target = enemy.position;
			}
			
			if (storedSkeletonID != -1){
				Actor ch = getById(storedSkeletonID);
				storedSkeletonID = -1;
				if (ch instanceof NecroSkeleton){
					mySkeleton = (NecroSkeleton) ch;
				}
			}
			
			if (summoning){
				summonMinion();
				return true;
			}
			
			if (mySkeleton != null &&
					(!mySkeleton.isAlive()
					|| !Dungeon.level.mobs.contains(mySkeleton)
					|| mySkeleton.alignment != alignment)){
				mySkeleton = null;
			}
			
			//if enemy is seen, and enemy is within range, and we have no skeleton, summon a skeleton!
			if (enemySeen && Dungeon.level.distance(position, enemy.position) <= 4 && mySkeleton == null){
				
				summoningPos = -1;

				//we can summon around blocking terrain, but not through it
				PathFinder.buildDistanceMap(position, BArray.not(Dungeon.level.solid, null), Dungeon.level.distance(position, enemy.position)+3);

				for (int c : PathFinder.OFFSETS_NEIGHBOURS8){
					if (getCharacterOnPosition(enemy.position +c) == null
							&& PathFinder.distance[enemy.position +c] != Integer.MAX_VALUE
							&& Dungeon.level.passable[enemy.position +c]
							&& (!hasProperty(Necromancer.this, Property.LARGE) || Dungeon.level.openSpace[enemy.position +c])
							&& fieldOfView[enemy.position +c]
							&& Dungeon.level.trueDistance(position, enemy.position +c) < Dungeon.level.trueDistance(position, summoningPos)){
						summoningPos = enemy.position +c;
					}
				}
				
				if (summoningPos != -1){
					
					summoning = true;
					sprite.zap( summoningPos );
					
					spendTimeAdjusted( firstSummon ? TICK : 2*TICK );
				} else {
					//wait for a turn
					spendTimeAdjusted(TICK);
				}
				
				return true;
			//otherwise, if enemy is seen, and we have a skeleton...
			} else if (enemySeen && mySkeleton != null){
				
				spendTimeAdjusted(TICK);
				
				if (!fieldOfView[mySkeleton.position]){
					
					//if the skeleton is not next to the enemy
					//teleport them to the closest spot next to the enemy that can be seen
					if (!Dungeon.level.adjacent(mySkeleton.position, enemy.position)){
						int telePos = -1;
						for (int c : PathFinder.OFFSETS_NEIGHBOURS8){
							if (getCharacterOnPosition(enemy.position +c) == null
									&& Dungeon.level.passable[enemy.position +c]
									&& fieldOfView[enemy.position +c]
									&& (Dungeon.level.openSpace[enemy.position +c] || !hasProperty(mySkeleton, Property.LARGE))
									&& Dungeon.level.trueDistance(position, enemy.position +c) < Dungeon.level.trueDistance(position, telePos)){
								telePos = enemy.position +c;
							}
						}
						
						if (telePos != -1){
							
							ScrollOfTeleportation.appear(mySkeleton, telePos);
							mySkeleton.teleportSpend();
							
							if (sprite != null && sprite.visible){
								sprite.zap(telePos);
								return false;
							} else {
								onZapComplete();
							}
						}
					}
					
					return true;
					
				} else {
					
					//zap skeleton
					if (mySkeleton.healthPoints < mySkeleton.healthMax || mySkeleton.getBuff(Adrenaline.class) == null) {
						if (sprite != null && sprite.visible){
							sprite.zap(mySkeleton.position);
							return false;
						} else {
							onZapComplete();
						}
					}
					
				}
				
				return true;
				
			//otherwise, default to regular hunting behaviour
			} else {
				return super.playGameTurn(enemyInFOV, justAlerted);
			}
		}
	}
	
	public static class NecroSkeleton extends Skeleton {
		
		{
			state = WANDERING;
			
			spriteClass = NecroSkeletonSprite.class;
			
			//no loot or exp
			maxLvl = -5;
			
			//20/25 health to start
			healthPoints = 20;
		}

		@Override
		public float getSpawningWeight() {
			return 0;
		}

		private void teleportSpend(){
			spendTimeAdjusted(TICK);
		}
		
		public static class NecroSkeletonSprite extends SkeletonSprite{
			
			public NecroSkeletonSprite(){
				super();
				brightness(0.75f);
			}
			
			@Override
			public void resetColor() {
				super.resetColor();
				brightness(0.75f);
			}
		}
		
	}
}
