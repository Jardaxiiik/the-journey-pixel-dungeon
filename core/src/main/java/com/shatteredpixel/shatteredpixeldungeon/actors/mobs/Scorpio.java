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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ScorpioSprite;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Scorpio extends Mob {
	
	{
		spriteClass = ScorpioSprite.class;
		
		healthPoints = healthMax = 110;
		defenseSkill = 24;
		viewDistance = Light.DISTANCE;
		
		EXP = 14;
		maxLvl = 27;
		
		loot = ItemGenerator.Category.POTION;
		lootChance = 0.5f;

		properties.add(Property.DEMONIC);
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 30, 40 );
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 36;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 16);
	}
	
	@Override
	protected boolean canAttackEnemy(Character enemy ) {
		return !Dungeon.level.adjacent(position, enemy.position)
				&& (super.canAttackEnemy(enemy) || new Ballistica(position, enemy.position, Ballistica.PROJECTILE).collisionPos == enemy.position);
	}
	
	@Override
	public int attackProc(Character enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (Random.Int( 2 ) == 0) {
			Buff.prolong( enemy, Cripple.class, Cripple.DURATION );
		}
		
		return damage;
	}
	
	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
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
		Class<?extends Potion> loot;
		do{
			loot = (Class<? extends Potion>) Random.oneOf(ItemGenerator.Category.POTION.classes);
		} while (loot == PotionOfHealing.class || loot == PotionOfStrength.class);

		return Reflection.newInstance(loot);
	}
	
}
