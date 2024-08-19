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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SwarmSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Swarm extends Mob {

	{
		spriteClass = SwarmSprite.class;
		
		healthPoints = healthMax = 50;
		defenseSkill = 5;

		EXP = 3;
		maxLvl = 9;
		
		flying = true;

		loot = new PotionOfHealing();
		lootChance = 0.1667f; //by default, see lootChance()
	}
	
	private static final float SPLIT_DELAY	= 1f;
	
	int generation	= 0;
	
	private static final String GENERATION	= "generation";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GENERATION, generation );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		generation = bundle.getInt( GENERATION );
		if (generation > 0) EXP = 0;
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 1, 4 );
	}
	
	@Override
	public int getDamageReceivedFromEnemyReducedByDefense(Character enemy, int damage ) {

		if (healthPoints >= damage + 2) {
			ArrayList<Integer> candidates = new ArrayList<>();
			
			int[] neighbours = {position + 1, position - 1, position + Dungeon.level.width(), position - Dungeon.level.width()};
			for (int n : neighbours) {
				if (!Dungeon.level.solid[n]
						&& Actor.getCharacterOnPosition( n ) == null
						&& (Dungeon.level.passable[n] || Dungeon.level.avoid[n])
						&& (!getProperties().contains(Property.LARGE) || Dungeon.level.openSpace[n])) {
					candidates.add( n );
				}
			}
	
			if (candidates.size() > 0) {
				
				Swarm clone = split();
				clone.position = Random.element( candidates );
				clone.state = clone.HUNTING;
				GameScene.add( clone, SPLIT_DELAY ); //we add before assigning HP due to ascension

				clone.healthPoints = (healthPoints - damage) / 2;
				Actor.addActor( new Pushing( clone, position, clone.position) );

				Dungeon.level.occupyCell(clone);
				
				healthPoints -= clone.healthPoints;
			}
		}
		
		return super.getDamageReceivedFromEnemyReducedByDefense(enemy, damage);
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 10;
	}
	
	private Swarm split() {
		Swarm clone = new Swarm();
		clone.generation = generation + 1;
		clone.EXP = 0;
		if (getBuff( Burning.class ) != null) {
			Buff.affect( clone, Burning.class ).reignite( clone );
		}
		if (getBuff( Poison.class ) != null) {
			Buff.affect( clone, Poison.class ).set(2);
		}
		for (Buff b : getBuffs(AllyBuff.class)){
			Buff.affect( clone, b.getClass());
		}
		for (Buff b : getBuffs(ChampionEnemy.class)){
			Buff.affect( clone, b.getClass());
		}
		return clone;
	}

	@Override
	public float getLootChance() {
		lootChance = 1f/(6 * (generation+1) );
		return super.getLootChance() * (5f - Dungeon.LimitedDrops.SWARM_HP.count) / 5f;
	}
	
	@Override
	public Item getLootItem(){
		Dungeon.LimitedDrops.SWARM_HP.count++;
		return super.getLootItem();
	}
}
