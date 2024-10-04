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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Corruption extends AllyBuff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	private float buildToDamage = 0f;

	//corrupted enemies are usually fully healed and cleansed of most debuffs
	public static void corruptionHeal(Character target){
		target.getCharacterHealth().setHealthPoints(target.getCharacterHealth().getHealthMax());
		target.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(target.healthMax), FloatingText.HEALING);
		for (Buff buff : target.getBuffs()) {
			if (buff.type == Buff.buffType.NEGATIVE
					&& !(buff instanceof SoulMark)) {
				buff.detach();
			}
		}
	}
	
	@Override
	public boolean playGameTurn() {
		buildToDamage += target.healthMax /100f;

		int damage = (int)buildToDamage;
		buildToDamage -= damage;

		if (damage > 0)
			target.receiveDamageFromSource(damage, this);

		spendTimeAdjusted(DungeonActors.TICK);

		return true;
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add( CharSprite.State.DARKENED );
		else if (target.invisible == 0) target.sprite.remove( CharSprite.State.DARKENED );
	}

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

}
