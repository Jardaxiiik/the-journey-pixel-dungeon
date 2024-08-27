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
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollTricksterSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class GnollTrickster extends Gnoll {

	{
		spriteClass = GnollTricksterSprite.class;

		healthPoints = healthMax = 20;
		evasionSkill = 5;

		EXP = 5;

		WANDERING = new Wandering();
		state = WANDERING;

		//at half quantity, see createLoot()
		//loot = ItemGenerator.Category.MISSILE;
		//lootChance = 1f;

		properties.add(Property.MINIBOSS);
	}

	private int combo = 0;

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 16;
	}

	@Override
	protected boolean canAttackEnemy(Character enemy ) {
		return !Dungeon.level.adjacent(position, enemy.position)
				&& (super.canAttackEnemy(enemy) || new Ballistica(position, enemy.position, Ballistica.PROJECTILE).collisionPos == enemy.position);
	}

	@Override
	public int attackProc(Character enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		//The gnoll's attacks get more severe the more the player lets it hit them
		combo++;
		int effect = Random.Int(4)+combo;

		if (effect > 2) {

			if (effect >=6 && enemy.getBuff(Burning.class) == null){

				if (Dungeon.level.flamable[enemy.position])
					GameScene.add(ActorLoop.seed(enemy.position, 4, Fire.class));
				Buff.affect(enemy, Burning.class).reignite( enemy );

			} else
				Buff.affect( enemy, Poison.class).set((effect-2) );

		}
		return damage;
	}

	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
		combo = 0; //if he's moving, he isn't attacking, reset combo.
		if (state == HUNTING) {
			return enemySeen && moveAwayFromTarget(targetPosition);
		} else {
			return super.moveCloserToTarget(targetPosition);
		}
	}

	@Override
	public void startHunting(Character ch) {
		//cannot be aggroed to something it can't see
		if (ch == null || fieldOfView == null || fieldOfView[ch.position]) {
			super.startHunting(ch);
		}
	}
	
	@Override
	public Item getLootItem() {
		MissileWeapon drop = (MissileWeapon)super.getLootItem();
		//half quantity, rounded up
		drop.quantity((drop.quantity()+1)/2);
		return drop;
	}
	
	@Override
	public void die( Object source) {
		super.die(source);

		Ghost.Quest.process();
	}

	protected class Wandering extends Mob.Wandering{
		@Override
		protected int randomDestination() {
			//of two potential wander positions, picks the one closest to the hero
			int pos1 = super.randomDestination();
			int pos2 = super.randomDestination();
			PathFinder.buildDistanceMap(Dungeon.hero.position, Dungeon.level.passable);
			if (PathFinder.distance[pos2] < PathFinder.distance[pos1]){
				return pos2;
			} else {
				return pos1;
			}
		}
	}

	private static final String COMBO = "combo";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(COMBO, combo);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		combo = bundle.getInt( COMBO );
	}

}
