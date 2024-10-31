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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters;

import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonTurnsHandler;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public abstract class Character extends CharacterWithActions {

	public CharacterAlignment alignment;

	public CharSprite sprite;

	public boolean moveSprite(int from, int to) {
		if (sprite.isVisible() && sprite.parent != null && (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.turnTo(from, to);
			sprite.place( to );
			return true;
		}
	}

	public void destroy() {
		healthPoints = 0;
		DungeonCharactersHandler.removeCharacter(this);

		for (Character ch : DungeonCharactersHandler.getCharacters().toArray(new Character[0])){
			if (ch.getBuff(Charm.class) != null && ch.getBuff(Charm.class).object == getId()){
				ch.getBuff(Charm.class).detach();
			}
			if (ch.getBuff(Dread.class) != null && ch.getBuff(Dread.class).object == getId()){
				ch.getBuff(Dread.class).detach();
			}
			if (ch.getBuff(Terror.class) != null && ch.getBuff(Terror.class).object == getId()){
				ch.getBuff(Terror.class).detach();
			}
			if (ch.getBuff(SnipersMark.class) != null && ch.getBuff(SnipersMark.class).object == getId()){
				ch.getBuff(SnipersMark.class).detach();
			}
			if (ch.getBuff(Talent.FollowupStrikeTracker.class) != null
					&& ch.getBuff(Talent.FollowupStrikeTracker.class).object == getId()){
				ch.getBuff(Talent.FollowupStrikeTracker.class).detach();
			}
			if (ch.getBuff(Talent.DeadlyFollowupTracker.class) != null
					&& ch.getBuff(Talent.DeadlyFollowupTracker.class).object == getId()){
				ch.getBuff(Talent.DeadlyFollowupTracker.class).detach();
			}
		}
	}


	public synchronized void updateSpriteState() {
		for (Buff buff:buffs) {
			buff.fx( true );
		}
	}

	public void onMotionComplete() {
		//Does nothing by default
		//The main actor thread already accounts for motion,
		// so calling next() here isn't necessary (see Actor.process)
	}

	public void onAttackComplete() { // when things are shooting we must wait till they finish it
		DungeonTurnsHandler.nextActorToPlay(this);
	}

	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );

		bundle.put(BUNDLE_POS, position);
		bundle.put(BUNDLE_TAG_HP, healthPoints);
		bundle.put(BUNDLE_TAG_HT, healthMax);
		bundle.put(BUNDLE_BUFFS, buffs );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );

		position = bundle.getInt(BUNDLE_POS);
		healthPoints = bundle.getInt(BUNDLE_TAG_HP);
		healthMax = bundle.getInt(BUNDLE_TAG_HT);

		for (Bundlable b : bundle.getCollection(BUNDLE_BUFFS)) {
			if (b != null) {
				((Buff)b).attachTo( this );
			}
		}
	}
}
