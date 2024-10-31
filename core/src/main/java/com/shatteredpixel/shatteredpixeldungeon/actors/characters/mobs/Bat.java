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

import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

public class Bat extends Mob {

	{
		spriteClass = BatSprite.class;
		
		healthPoints = healthMax = 30;
		evasionSkill = 15;
		baseMovementSpeed = 2f;
		
		EXP = 7;
		maxLvl = 15;

		getCharacterMovement().setFlying(true);
		
		loot = new PotionOfHealing();
		lootChance = 0.1667f; //by default, see lootChance()
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 5, 18 );
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 16;
	}
	
	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 4);
	}
	
	@Override
	public int attackProc_1(Character enemy, int damage ) {
		damage = ActionAttack.attackProc(this, enemy, damage );
		int reg = Math.min( damage - 4, healthMax - healthPoints);
		
		if (reg > 0) {
			healthPoints += reg;
			sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(reg), FloatingText.HEALING);
		}
		
		return damage;
	}
	
	@Override
	public float getLootChance(){
		return super.getLootChance() * ((7f - Dungeon.LimitedDrops.BAT_HP.count) / 7f);
	}
	
	@Override
	public Item getLootItem(){
		Dungeon.LimitedDrops.BAT_HP.count++;
		return super.getLootItem();
	}
	
}
