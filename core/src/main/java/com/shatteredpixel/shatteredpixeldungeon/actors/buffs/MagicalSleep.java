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
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.HeroAlignment;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class MagicalSleep extends Buff {

	private static final float STEP = 1f;

	@Override
	public boolean attachTo( Character target ) {
		if (!target.isImmuneToEffectType(Sleep.class) && super.attachTo( target )) {
			
			target.paralysed++;
			
			if (target.alignment == Character.Alignment.ALLY) {
				if (target.healthPoints == target.healthMax) {
					if (target instanceof  Hero) GLog.i(Messages.get(this, "toohealthy"));
					detach();
				} else {
					if (target instanceof  Hero) GLog.i(Messages.get(this, "fallasleep"));
				}
			}

			if (target instanceof Mob) {
				((Mob) target).state = ((Mob) target).SLEEPING;
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean playGameTurn(){
		if (target instanceof Mob && ((Mob) target).state != ((Mob) target).SLEEPING){
			detach();
			return true;
		}
		if (target.alignment == HeroAlignment.ALLY) {
			target.getCharacterHealth().setHealthPoints(Math.min(target.getCharacterHealth().getHealthPoints() +1, target.getCharacterHealth().getHealthMax()));
			if (target instanceof  Hero) ((Hero) target).resting = true;
			if (target.getCharacterHealth().getHealthPoints() == target.getCharacterHealth().getHealthMax()) {
				if (target instanceof  Hero) GLog.p(Messages.get(this, "wakeup"));
				detach();
			}
		}
		spendTimeAdjusted( STEP );
		return true;
	}

	@Override
	public void detach() {
		if (target.paralysed > 0)
			target.paralysed--;
		if (target instanceof Hero)
			((Hero) target).resting = false;
		super.detach();
	}

	@Override
	public int icon() {
		return BuffIndicator.MAGIC_SLEEP;
	}

	@Override
	public void fx(boolean on) {
		if (!on && (target.paralysed <= 1) ) {
			//in case the character has visual paralysis from another source
			target.sprite.remove(CharSprite.State.PARALYSED);
		}
	}
}