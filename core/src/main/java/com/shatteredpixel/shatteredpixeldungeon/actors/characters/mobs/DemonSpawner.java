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
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpawnerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DemonSpawner extends Mob {

	{
		spriteClass = SpawnerSprite.class;

		healthPoints = healthMax = 120;
		evasionSkill = 0;

		EXP = 15;
		maxLvl = 29;

		state = PASSIVE;

		loot = PotionOfHealing.class;
		lootChance = 1f;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.MINIBOSS);
		properties.add(Property.DEMONIC);
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 12);
	}

	@Override
	public void travelToPosition(int cell) {
		//do nothing
	}

	@Override
	public boolean reset() {
		return true;
	}

	private float spawnCooldown = 0;

	public boolean spawnRecorded = false;

	@Override
    public boolean playGameTurn() {
		if (!spawnRecorded){
			Statistics.spawnersAlive++;
			spawnRecorded = true;
		}

		if (Dungeon.level.visited[position]){
			Notes.add( Notes.Landmark.DEMON_SPAWNER );
		}

		if (Dungeon.hero.getBuff(AscensionChallenge.class) != null && spawnCooldown > 20){
			spawnCooldown = 20;
		}

		spawnCooldown--;
		if (spawnCooldown <= 0){

			//we don't want spawners to store multiple ripper demons
			if (spawnCooldown < -20){
				spawnCooldown = -20;
			}

			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.OFFSETS_NEIGHBOURS8) {
				if (Dungeon.level.passable[position +n] && DungeonCharactersHandler.getCharacterOnPosition( position +n ) == null) {
					candidates.add( position +n );
				}
			}

			if (!candidates.isEmpty()) {
				RipperDemon spawn = new RipperDemon();

				spawn.position = Random.element( candidates );
				spawn.state = spawn.HUNTING;

				GameScene.addMob( spawn, 1 );
				Dungeon.level.occupyCell(spawn);

				if (sprite.visible) {
					DungeonActorsHandler.addActor(new Pushing(spawn, position, spawn.position));
				}

				spawnCooldown += 60;
				if (Dungeon.depth > 21){
					//60/53.33/46.67/40 turns to spawn on floor 21/22/23/24
					spawnCooldown -= Math.min(20, (Dungeon.depth-21)*6.67);
				}
			}
		}
		alerted = false;
		return super.playGameTurn();
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if (dmg >= 20){
			//takes 20/21/22/23/24/25/26/27/28/29/30 dmg
			// at   20/22/25/29/34/40/47/55/64/74/85 incoming dmg
			dmg = 19 + (int)(Math.sqrt(8*(dmg - 19) + 1) - 1)/2;
		}
		spawnCooldown -= dmg;
		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}

	@Override
	public void die(Object source) {
		if (spawnRecorded){
			Statistics.spawnersAlive--;
			Notes.remove(Notes.Landmark.DEMON_SPAWNER);
		}
		GLog.h(Messages.get(this, "on_death"));
		super.die(source);
	}

	public static final String SPAWN_COOLDOWN = "spawn_cooldown";
	public static final String SPAWN_RECORDED = "spawn_recorded";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPAWN_COOLDOWN, spawnCooldown);
		bundle.put(SPAWN_RECORDED, spawnRecorded);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spawnCooldown = bundle.getFloat(SPAWN_COOLDOWN);
		spawnRecorded = bundle.getBoolean(SPAWN_RECORDED);
	}

	{
		immunities.add( Paralysis.class );
		immunities.add( Amok.class );
		immunities.add( Sleep.class );
		immunities.add( Dread.class );
		immunities.add( Terror.class );
		immunities.add( Vertigo.class );
	}
}
