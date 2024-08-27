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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalGuardianSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class CrystalGuardian extends Mob{

	{
		spriteClass = CrystalGuardianSprite.class;

		healthPoints = healthMax = 100;
		evasionSkill = 14;

		EXP = 10;
		maxLvl = -2;

		SLEEPING = new Sleeping();
		state = SLEEPING;

		properties.add(Property.INORGANIC);
		properties.add(Property.MINIBOSS);
	}

	private boolean recovering = false;

	public boolean recovering(){
		return recovering;
	}

	@Override
	protected boolean playGameTurn() {
		if (recovering){
			throwItems();
			healthPoints = Math.min(healthMax, healthPoints +5);
			if (Dungeon.level.heroFOV[position]) {
				sprite.showStatusWithIcon(CharSprite.POSITIVE, "5", FloatingText.HEALING);
			}
			if (healthPoints == healthMax){
				recovering = false;
				if (sprite instanceof CrystalGuardianSprite) ((CrystalGuardianSprite) sprite).endCrumple();
			}
			spendTimeAdjusted(TICK);
			return true;
		}
		return super.playGameTurn();
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 10, 16 );
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 20;
	}

	@Override
	public int getEvasionAgainstAttacker(Character enemy) {
		if (recovering) return 0;
		else            return super.getEvasionAgainstAttacker(enemy);
	}

	@Override
	public boolean isSurprisedBy(Character enemy, boolean attacking) {
		if (recovering) return false;
		else            return super.isSurprisedBy(enemy, attacking);
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 10);
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public int getDamageReceivedFromEnemyReducedByDefense(Character enemy, int damage) {
		if (recovering){
			sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(damage));
			healthPoints = Math.max(1, healthPoints -damage);
			damage = -1;
		}

		return super.getDamageReceivedFromEnemyReducedByDefense(enemy, damage);
	}

	@Override
	public boolean isAlive() {
		if (healthPoints <= 0){
			healthPoints = 1;

			for (Buff b : getBuffs()){
				if (!(b instanceof Doom || b instanceof Cripple)) {
					b.detach();
				}
			}

			if (!recovering) {
				recovering = true;
				if (sprite != null) ((CrystalGuardianSprite) sprite).crumple();
			}
		}
		return super.isAlive();
	}

	@Override
	public boolean isInvulnerableToEffectType(Class effect) {
		if (recovering){
			//while recovering, immune to chars that aren't the hero or spire
			// this is sort of a hack to prevent allies from attacking downed guardians
			return super.isInvulnerableToEffectType(effect) || (Character.class.isAssignableFrom(effect) && !Hero.class.isAssignableFrom(effect) && !CrystalSpire.class.isAssignableFrom(effect));
		}
		return super.isInvulnerableToEffectType(effect);
	}

	public CrystalGuardian(){
		super();
		switch (Random.Int(3)){
			case 0: default:
				spriteClass = CrystalGuardianSprite.Blue.class;
				break;
			case 1:
				spriteClass = CrystalGuardianSprite.Green.class;
				break;
			case 2:
				spriteClass = CrystalGuardianSprite.Red.class;
				break;
		}
	}

	@Override
	public float getSpawningWeight() {
		return 0;
	}

	@Override
	public float getSpeed() {
		//crystal guardians take up to 4 turns when moving through an enclosed space
		if (!Dungeon.level.openSpace[position]) {
			return Math.max(0.25f, super.getSpeed() / 4f);
		}
		return super.getSpeed();
	}

	@Override
	public void moveToPosition(int newPosition, boolean travelling) {
		super.moveToPosition(newPosition, travelling);
		if (Dungeon.level.map[position] == Terrain.MINE_CRYSTAL){
			Level.set(position, Terrain.EMPTY);
			GameScene.updateMap(position);
			if (Dungeon.level.heroFOV[position]){
				Splash.at(position, 0xFFFFFF, 5);
				Sample.INSTANCE.play( Assets.Sounds.SHATTER );
			}
			//breaking a crystal costs an extra move, not affected by enclosed spaces though
			spendTimeAdjusted(1/super.getSpeed());
		}
	}

	@Override
	public boolean[] modifyPassable(boolean[] passable) {
		//if we are hunting, we can stomp through crystals, but prefer not to
		if (state == HUNTING && target != -1){
			PathFinder.buildDistanceMap(target, passable);

			if (PathFinder.distance[position] > 2*Dungeon.level.distance(position, target)) {
				for (int i = 0; i < Dungeon.level.length(); i++) {
					passable[i] = passable[i] || Dungeon.level.map[i] == Terrain.MINE_CRYSTAL;
				}
			}
		}
		return passable;
	}

	@Override
	public void beckon(int cell) {
		if (state == SLEEPING){
			//do nothing
		} else {
			super.beckon(cell);
		}
	}

	protected class Sleeping extends Mob.Sleeping{

		@Override
		protected void awaken(boolean enemyInFOV) {
			if (enemyInFOV){
				//do not wake up if we see an enemy we can't actually reach
				PathFinder.buildDistanceMap(enemy.position, Dungeon.level.passable);
				if (PathFinder.distance[position] == Integer.MAX_VALUE){
					return;
				}
			}
			super.awaken(enemyInFOV);
		}
	}

	public static final String SPRITE = "sprite";
	public static final String RECOVERING = "recovering";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPRITE, spriteClass);
		bundle.put(RECOVERING, recovering);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spriteClass = bundle.getClass(SPRITE);
		recovering = bundle.getBoolean(RECOVERING);
	}
}
