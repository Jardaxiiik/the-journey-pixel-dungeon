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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PhantomPiranhaSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class PhantomPiranha extends Piranha {

	{
		spriteClass = PhantomPiranhaSprite.class;

		loot = PhantomMeat.class;
		lootChance = 1f;
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		Character dmgSource = null;
		if (sourceOfDamage instanceof Character) dmgSource = (Character) sourceOfDamage;
		if (sourceOfDamage instanceof Wand) dmgSource = Dungeon.hero;

		if (dmgSource == null || !Dungeon.level.adjacent(position, dmgSource.position)){
			dmg = Math.round(dmg/2f); //halve damage taken if we are going to teleport
		}
		super.receiveDamageFromSource(dmg, sourceOfDamage);

		if (isAlive() && !(sourceOfDamage instanceof Corruption)) {
			if (dmgSource != null) {
				if (!Dungeon.level.adjacent(position, dmgSource.position)) {
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int i : PathFinder.OFFSETS_NEIGHBOURS8) {
						if (Dungeon.level.water[dmgSource.position + i] && getCharacterOnPosition(dmgSource.position + i) == null) {
							candidates.add(dmgSource.position + i);
						}
					}
					if (!candidates.isEmpty()) {
						ScrollOfTeleportation.appear(this, Random.element(candidates));
						startHunting(dmgSource);
					} else {
						teleportAway();
					}
				}
			} else {
				teleportAway();
			}
		}
	}

	@Override
	public int getDamageReceivedFromEnemyReducedByDefense(Character enemy, int damage) {
		return super.getDamageReceivedFromEnemyReducedByDefense(enemy, damage);
	}

	@Override
	public void dieOnLand() {
		if (!teleportAway()){
			super.dieOnLand();
		}
	}

	private boolean teleportAway(){

		ArrayList<Integer> inFOVCandidates = new ArrayList<>();
		ArrayList<Integer> outFOVCandidates = new ArrayList<>();
		for (int i = 0; i < Dungeon.level.length(); i++){
			if (Dungeon.level.water[i] && getCharacterOnPosition(i) == null){
				if (Dungeon.level.heroFOV[i]){
					inFOVCandidates.add(i);
				} else {
					outFOVCandidates.add(i);
				}
			}
		}

		if (!outFOVCandidates.isEmpty()){
			if (Dungeon.level.heroFOV[position]) GLog.i(Messages.get(this, "teleport_away"));
			ScrollOfTeleportation.appear(this, Random.element(outFOVCandidates));
			return true;
		} else if (!inFOVCandidates.isEmpty()){
			ScrollOfTeleportation.appear(this, Random.element(inFOVCandidates));
			return true;
		}

		return false;

	}
}
