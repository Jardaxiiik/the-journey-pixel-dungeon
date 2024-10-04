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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonTurnsHandler;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.BArray;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class ChampionEnemy extends Buff {

	{
		type = buffType.POSITIVE;
	}

	protected int color;

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(color);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.aura( color );
		else target.sprite.clearAura();
	}

	public void onAttackProc(Character enemy ){

	}

	public boolean canAttackWithExtraReach( Character enemy ){
		return false;
	}

	public float meleeDamageFactor(){
		return 1f;
	}

	public float damageTakenFactor(){
		return 1f;
	}

	public float evasionAndAccuracyFactor(){
		return 1f;
	}

	{
		immunities.add(AllyBuff.class);
	}

	public static void rollForChampion(Mob m){
		if (Dungeon.mobsToChampion <= 0) Dungeon.mobsToChampion = 8;

		Dungeon.mobsToChampion--;

		//we roll for a champion enemy even if we aren't spawning one to ensure that
		//mobsToChampion does not affect levelgen RNG (number of calls to Random.Int() is constant)
		Class<?extends ChampionEnemy> buffCls;
		switch (Random.Int(6)){
			case 0: default:    buffCls = Blazing.class;      break;
			case 1:             buffCls = Projecting.class;   break;
			case 2:             buffCls = AntiMagic.class;    break;
			case 3:             buffCls = Giant.class;        break;
			case 4:             buffCls = Blessed.class;      break;
			case 5:             buffCls = Growing.class;      break;
		}

		if (Dungeon.mobsToChampion <= 0 && Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)) {
			Buff.affect(m, buffCls);
			m.state = m.WANDERING;
		}
	}

	public static class Blazing extends ChampionEnemy {

		{
			color = 0xFF8800;
		}

		@Override
		public void onAttackProc(Character enemy) {
			if (!Dungeon.level.water[enemy.position]) {
				Buff.affect(enemy, Burning.class).reignite(enemy);
			}
		}

		@Override
		public void detach() {
			//don't trigger when killed by being knocked into a pit
			if (target.getCharacterMovement().isFlying() || !Dungeon.level.pit[target.position]) {
				for (int i : PathFinder.OFFSETS_NEIGHBOURS9) {
					if (!Dungeon.level.solid[target.position + i] && !Dungeon.level.water[target.position + i]) {
						GameScene.addMob(ActorLoop.seed(target.position + i, 2, Fire.class));
					}
				}
			}
			super.detach();
		}

		@Override
		public float meleeDamageFactor() {
			return 1.25f;
		}

		{
			immunities.add(Burning.class);
		}
	}

	public static class Projecting extends ChampionEnemy {

		{
			color = 0x8800FF;
		}

		@Override
		public float meleeDamageFactor() {
			return 1.25f;
		}

		@Override
		public boolean canAttackWithExtraReach(Character enemy) {
			if (Dungeon.level.distance( target.position, enemy.position) > 4){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Character ch : DungeonCharactersHandler.getCharacters()) {
					//our own tile is always passable
					passable[ch.position] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.position, passable, 4);

				return PathFinder.distance[target.position] <= 4;
			}
		}
	}

	public static class AntiMagic extends ChampionEnemy {

		{
			color = 0x00FF00;
		}

		@Override
		public float damageTakenFactor() {
			return 0.5f;
		}

		{
			immunities.addAll(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic.RESISTS);
		}

	}

	//Also makes target large, see Character.properties()
	public static class Giant extends ChampionEnemy {

		{
			color = 0x0088FF;
		}

		@Override
		public float damageTakenFactor() {
			return 0.2f;
		}

		@Override
		public boolean canAttackWithExtraReach(Character enemy) {
			if (Dungeon.level.distance( target.position, enemy.position) > 2){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Character ch : DungeonCharactersHandler.getCharacters()) {
					//our own tile is always passable
					passable[ch.position] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.position, passable, 2);

				return PathFinder.distance[target.position] <= 2;
			}
		}
	}

	public static class Blessed extends ChampionEnemy {

		{
			color = 0xFFFF00;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return 4f;
		}
	}

	public static class Growing extends ChampionEnemy {

		{
			color = 0xFF0000;
		}

		private float multiplier = 1.19f;

		@Override
		public boolean playGameTurn() {
			multiplier += 0.01f;
			spendTimeAdjusted(4* DungeonTurnsHandler.TICK);
			return true;
		}

		@Override
		public float meleeDamageFactor() {
			return multiplier;
		}

		@Override
		public float damageTakenFactor() {
			return 1f/multiplier;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return multiplier;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int)(100*(multiplier-1)), (int)(100*(1 - 1f/multiplier)));
		}

		private static final String MULTIPLIER = "multiplier";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(MULTIPLIER, multiplier);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			multiplier = bundle.getFloat(MULTIPLIER);
		}
	}

}
