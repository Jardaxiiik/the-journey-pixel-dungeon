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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SheepSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Sheep extends NPC {

	private static final String[] LINE_KEYS = {"Baa!", "Baa?", "Baa.", "Baa..."};

	{
		spriteClass = SheepSprite.class;
	}

	public float lifespan;

	private boolean initialized = false;

	@Override
    public boolean playGameTurn() {
		if (initialized) {
			healthPoints = 0;

			destroy();
			sprite.die();

		} else {
			initialized = true;
			spendTimeAdjusted( lifespan + Random.Float(-2, 2) );
		}
		return true;
	}

	@Override
	public int getEvasionAgainstAttacker(Character enemy) {
		return INFINITE_EVASION;
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		//do nothing
	}

	@Override
	public boolean addBuff(Buff buff ) {
		return false;
	}

	@Override
	public boolean interact(Character c) {
		sprite.showStatus( CharSprite.NEUTRAL, Messages.get(this, Random.element( LINE_KEYS )) );
		if (c == Dungeon.hero) {
			Dungeon.hero.spendTimeAdjustedAndNext(1f);
			Sample.INSTANCE.play(Assets.Sounds.SHEEP, 1, Random.Float(0.91f, 1.1f));
			//sheep summoned by woolly bomb can be dispelled by interacting
			if (lifespan >= 20){
				spendTimeAdjusted(-cooldown());
			}
		}
		return true;
	}

	private static final String LIFESPAN = "lifespan";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LIFESPAN, lifespan);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lifespan = bundle.getInt(LIFESPAN);
	}
}