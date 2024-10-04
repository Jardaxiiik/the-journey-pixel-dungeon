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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SlimeSprite;
import com.watabou.utils.Random;

public class Slime extends Mob {
	
	{
		spriteClass = SlimeSprite.class;
		
		healthPoints = healthMax = 20;
		evasionSkill = 5;
		
		EXP = 4;
		maxLvl = 9;
		
		lootChance = 0.2f; //by default, see lootChance()
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 2, 5 );
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 12;
	}
	
	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		float scaleFactor = AscensionChallenge.statModifier(this);
		int scaledDmg = Math.round(dmg/scaleFactor);
		if (scaledDmg >= 5){
			//takes 5/6/7/8/9/10 dmg at 5/7/10/14/19/25 incoming dmg
			scaledDmg = 4 + (int)(Math.sqrt(8*(scaledDmg - 4) + 1) - 1)/2;
		}
		dmg = (int)(scaledDmg*AscensionChallenge.statModifier(this));
		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}

	@Override
	public float getLootChance(){
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/5, 1/15, 1/45, 1/135, etc.
		return super.getLootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.SLIME_WEP.count);
	}
	
	@Override
	public Item getLootItem() {
		Dungeon.LimitedDrops.SLIME_WEP.count++;
		ItemGenerator.Category c = ItemGenerator.Category.WEP_T2;
		MeleeWeapon w = (MeleeWeapon) ItemGenerator.randomUsingDefaults(ItemGenerator.Category.WEP_T2);
		w.level(0);
		return w;
	}
}
