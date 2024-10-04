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
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM201Sprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class DM201 extends DM200 {

	{
		spriteClass = DM201Sprite.class;

		healthPoints = healthMax = 120;

		properties.add(Property.IMMOVABLE);

		HUNTING = new Hunting();
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 15, 25 );
	}

	private boolean threatened = false;

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if ((sourceOfDamage instanceof Character && !Dungeon.level.adjacent(position, ((Character) sourceOfDamage).position))
				|| enemy == null || !Dungeon.level.adjacent(position, enemy.position)){
			threatened = true;
		}
		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}

	public void onZapComplete(){
		zap();
		DungeonTurnsHandler.nextActorToPlay(this);();
	}

	private void zap( ){
		threatened = false;
		spendTimeAdjusted(DungeonActors.TICK);

		GameScene.addMob(ActorLoop.seed(enemy.position, 15, CorrosiveGas.class).setStrength(8));
		for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
			if (!Dungeon.level.solid[enemy.position +i]) {
				GameScene.addMob(ActorLoop.seed(enemy.position + i, 5, CorrosiveGas.class).setStrength(8));
			}
		}

	}

	@Override
	protected boolean canVent(int target) {
		return false;
	}

	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
		return false;
	}

	@Override
	protected boolean moveAwayFromTarget(int targetPosition) {
		return false;
	}

	@Override
	public void dropLoot() {
		if (Dungeon.hero.lvl > maxLvl + 2) return;

		super.dropLoot();

		int ofs;
		do {
			ofs = PathFinder.OFFSETS_NEIGHBOURS8[Random.Int(8)];
		} while (Dungeon.level.solid[position + ofs] && !Dungeon.level.passable[position + ofs]);
		Dungeon.level.dropItemOnPosition( new MetalShard(), position + ofs ).sprite.drop(position);
	}

	private class Hunting extends Mob.Hunting {

		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted ) {

			if (threatened && enemyInFOV){
				if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
					sprite.zap( enemy.position);
					return false;
				} else {
					zap();
					return true;
				}
			} else {
				return super.playGameTurn( enemyInFOV, justAlerted );
			}

		}

	}

}
