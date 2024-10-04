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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpectralNecromancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpectralNecromancer extends Necromancer {

	{
		spriteClass = SpectralNecromancerSprite.class;
	}

	private ArrayList<Integer> wraithIDs = new ArrayList<>();

	@Override
    public boolean playGameTurn() {
		if (summoning && state != HUNTING){
			summoning = false;
			if (sprite instanceof SpectralNecromancerSprite) {
				((SpectralNecromancerSprite) sprite).cancelSummoning();
			}
		}
		return super.playGameTurn();
	}

	@Override
	public void dropLoot() {
		if (Dungeon.hero.lvl > maxLvl + 2) return;

		super.dropLoot();

		int ofs;
		do {
			ofs = PathFinder.OFFSETS_NEIGHBOURS8[Random.Int(8)];
		} while (Dungeon.level.solid[position + ofs] && !Dungeon.level.passable[position + ofs]);
		Dungeon.level.dropItemOnPosition( new ScrollOfRemoveCurse(), position + ofs ).sprite.drop(position);
	}

	@Override
	public void die(Object source) {
		for (int ID : wraithIDs){
			Actor a = DungeonActorsHandler.getById(ID);
			if (a instanceof Wraith){
				((Wraith) a).die(null);
			}
		}

		super.die(source);
	}

	private static final String WRAITH_IDS = "wraith_ids";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		int[] wraithIDArr = new int[wraithIDs.size()];
		int i = 0; for (Integer val : wraithIDs){ wraithIDArr[i] = val; i++; }
		bundle.put(WRAITH_IDS, wraithIDArr);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		wraithIDs.clear();
		for (int i : bundle.getIntArray(WRAITH_IDS)){
			wraithIDs.add(i);
		}
	}

	@Override
	public void summonMinion() {
		if (DungeonCharactersHandler.getCharacterOnPosition(summoningPos) != null) {

			//cancel if character cannot be moved
			if (hasProperty(DungeonCharactersHandler.getCharacterOnPosition(summoningPos), Property.IMMOVABLE)){
				summoning = false;
				((SpectralNecromancerSprite)sprite).finishSummoning();
				spendTimeAdjusted(DungeonTurnsHandler.TICK);
				return;
			}

			int pushPos = position;
			for (int c : PathFinder.OFFSETS_NEIGHBOURS8) {
				if (DungeonCharactersHandler.getCharacterOnPosition(summoningPos + c) == null
						&& Dungeon.level.passable[summoningPos + c]
						&& (Dungeon.level.openSpace[summoningPos + c] || !hasProperty(DungeonCharactersHandler.getCharacterOnPosition(summoningPos), Property.LARGE))
						&& Dungeon.level.trueDistance(position, summoningPos + c) > Dungeon.level.trueDistance(position, pushPos)) {
					pushPos = summoningPos + c;
				}
			}

			//push enemy, or wait a turn if there is no valid pushing position
			if (pushPos != position) {
				Character ch = DungeonCharactersHandler.getCharacterOnPosition(summoningPos);
				DungeonActorsHandler.addActor( new Pushing( ch, ch.position, pushPos ) );

				ch.position = pushPos;
				Dungeon.level.occupyCell(ch );

			} else {

				Character blocker = DungeonCharactersHandler.getCharacterOnPosition(summoningPos);
				if (blocker.alignment != alignment){
					blocker.receiveDamageFromSource( Random.NormalIntRange(2, 10), new SummoningBlockDamage() );
					if (blocker == Dungeon.hero && !blocker.isAlive()){
						Badges.validateDeathFromEnemyMagic();
						Dungeon.fail(this);
						GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", getName())) );
					}
				}

				spendTimeAdjusted(DungeonTurnsHandler.TICK);
				return;
			}
		}

		summoning = firstSummon = false;

		Wraith wraith = Wraith.spawnAt(summoningPos, Wraith.class);
		wraith.adjustStats(0);
		Dungeon.level.occupyCell( wraith );
		((SpectralNecromancerSprite)sprite).finishSummoning();

		for (Buff b : getBuffs(AllyBuff.class)){
			Buff.affect( wraith, b.getClass());
		}
		for (Buff b : getBuffs(ChampionEnemy.class)){
			Buff.affect( wraith, b.getClass());
		}
		wraithIDs.add(wraith.getId());
	}
}
