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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Skeleton extends Mob {
	
	{
		spriteClass = SkeletonSprite.class;
		
		healthPoints = healthMax = 25;
		evasionSkill = 9;
		
		EXP = 5;
		maxLvl = 10;

		loot = ItemGenerator.Category.WEAPON;
		lootChance = 0.1667f; //by default, see lootChance()

		properties.add(Property.UNDEAD);
		properties.add(Property.INORGANIC);
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 2, 10 );
	}
	
	@Override
	public void die( Object source) {
		
		super.die(source);
		
		if (source == Chasm.class) return;
		
		boolean heroKilled = false;
		for (int i = 0; i < PathFinder.OFFSETS_NEIGHBOURS8.length; i++) {
			Character ch = DungeonCharactersHandler.getCharacterOnPosition( position + PathFinder.OFFSETS_NEIGHBOURS8[i] );
			if (ch != null && ch.isAlive()) {
				int damage = Math.round(Random.NormalIntRange(6, 12));
				damage = Math.round( damage * AscensionChallenge.statModifier(this));
				//armor is 2x effective against bone explosion
				damage = Math.max( 0,  damage - (ch.getArmorPointsRolled() + ch.getArmorPointsRolled()) );
				ch.receiveDamageFromSource( damage, this );
				if (ch == Dungeon.hero && !ch.isAlive()) {
					heroKilled = true;
				}
			}
		}
		
		if (Dungeon.level.heroFOV[position]) {
			Sample.INSTANCE.play( Assets.Sounds.BONES );
		}
		
		if (heroKilled) {
			Dungeon.fail( this );
			GLog.n( Messages.get(this, "explo_kill") );
		}
	}

	@Override
	public float getLootChance() {
		//each drop makes future drops 1/2 as likely
		// so loot chance looks like: 1/6, 1/12, 1/24, 1/48, etc.
		return super.getLootChance() * (float)Math.pow(1/2f, Dungeon.LimitedDrops.SKELE_WEP.count);
	}

	@Override
	public Item getLootItem() {
		Dungeon.LimitedDrops.SKELE_WEP.count++;
		return super.getLootItem();
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 12;
	}
	
	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 5);
	}

}
