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
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FistSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class YogFist extends Mob {

	{
		healthPoints = healthMax = 300;
		evasionSkill = 20;

		viewDistance = Light.DISTANCE;

		//for doomed resistance
		EXP = 25;
		maxLvl = -2;

		state = HUNTING;

		properties.add(Property.BOSS);
		properties.add(Property.DEMONIC);
	}

	private float rangedCooldown;
	protected boolean canRangedInMelee = true;

	protected void incrementRangedCooldown(){
		rangedCooldown += Random.NormalFloat(8, 12);
	}

	@Override
	protected boolean playGameTurn() {
		if (paralysed <= 0 && rangedCooldown > 0) rangedCooldown--;

		if (Dungeon.hero.invisible <= 0 && state == WANDERING){
			travelToPosition(Dungeon.hero.position);
			state = HUNTING;
			enemy = Dungeon.hero;
		}

		return super.playGameTurn();
	}

	@Override
	protected boolean canAttackEnemy(Character enemy) {
		if (rangedCooldown <= 0){
			return new Ballistica(position, enemy.position, Ballistica.MAGIC_BOLT).collisionPos == enemy.position;
		} else {
			return super.canAttackEnemy(enemy);
		}
	}

	private boolean invulnWarned = false;

	protected boolean isNearYog(){
		int yogPos = Dungeon.level.exit() + 3*Dungeon.level.width();
		return Dungeon.level.distance(position, yogPos) <= 4;
	}

	@Override
	public boolean isInvulnerableToEffectType(Class effect) {
		if (isNearYog() && !invulnWarned){
			invulnWarned = true;
			GLog.w(Messages.get(this, "invuln_warn"));
		}
		return isNearYog() || super.isInvulnerableToEffectType(effect);
	}

	@Override
	protected boolean attackCharacter(Character targetCharacter) {

		if (Dungeon.level.adjacent(position, targetCharacter.position) && (!canRangedInMelee || rangedCooldown > 0)) {

			return super.attackCharacter(targetCharacter);

		} else {

			incrementRangedCooldown();
			if (sprite != null && (sprite.visible || targetCharacter.sprite.visible)) {
				sprite.zap( targetCharacter.position);
				return false;
			} else {
				zap();
				return true;
			}
		}
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		int preHP = healthPoints;
		super.receiveDamageFromSource(dmg, sourceOfDamage);
		int dmgTaken = preHP - healthPoints;

		LockedFloor lock = Dungeon.hero.getBuff(LockedFloor.class);
		if (dmgTaken > 0 && lock != null){
			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(dmgTaken/4f);
			else                                                    lock.addTime(dmgTaken/2f);
		}
	}

	protected abstract void zap();

	public void onZapComplete(){
		zap();
		next();
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 36;
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 18, 36 );
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 15);
	}

	{
		immunities.add( Sleep.class );
	}

	@Override
	public String getDescription() {
		return Messages.get(YogFist.class, "desc") + "\n\n" + Messages.get(this, "desc");
	}

	public static final String RANGED_COOLDOWN = "ranged_cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(RANGED_COOLDOWN, rangedCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		rangedCooldown = bundle.getFloat(RANGED_COOLDOWN);
	}

	public static class BurningFist extends YogFist {

		{
			spriteClass = FistSprite.Burning.class;

			properties.add(Property.FIERY);
		}

		@Override
		public boolean playGameTurn() {

			boolean result = super.playGameTurn();

			if (Dungeon.level.map[position] == Terrain.WATER){
				Level.set(position, Terrain.EMPTY);
				GameScene.updateMap(position);
				CellEmitter.get(position).burst( Speck.factory( Speck.STEAM ), 10 );
			}

			//1.67 evaporated tiles on average
			int evaporatedTiles = Random.chances(new float[]{0, 1, 2});

			for (int i = 0; i < evaporatedTiles; i++) {
				int cell = position + PathFinder.OFFSETS_NEIGHBOURS8[Random.Int(8)];
				if (Dungeon.level.map[cell] == Terrain.WATER){
					Level.set( cell, Terrain.EMPTY);
					GameScene.updateMap( cell );
					CellEmitter.get( cell ).burst( Speck.factory( Speck.STEAM ), 10 );
				}
			}

			for (int i : PathFinder.OFFSETS_NEIGHBOURS9) {
				int vol = Fire.volumeAt(position +i, Fire.class);
				if (vol < 4 && !Dungeon.level.water[position + i] && !Dungeon.level.solid[position + i]){
					GameScene.add( ActorLoop.seed( position + i, 4 - vol, Fire.class ) );
				}
			}

			return result;
		}

		@Override
		protected void zap() {
			spendTimeAdjusted( 1f );

			if (Dungeon.level.map[enemy.position] == Terrain.WATER){
				Level.set( enemy.position, Terrain.EMPTY);
				GameScene.updateMap( enemy.position);
				CellEmitter.get( enemy.position).burst( Speck.factory( Speck.STEAM ), 10 );
			} else {
				Buff.affect( enemy, Burning.class ).reignite( enemy );
			}

			for (int i : PathFinder.OFFSETS_NEIGHBOURS9){
				if (!Dungeon.level.water[enemy.position +i] && !Dungeon.level.solid[enemy.position +i]){
					int vol = Fire.volumeAt(enemy.position +i, Fire.class);
					if (vol < 4){
						GameScene.add( ActorLoop.seed( enemy.position + i, 4 - vol, Fire.class ) );
					}
				}
			}

		}

		{
			immunities.add(Frost.class);

			resistances.add(StormCloud.class);
			resistances.add(GeyserTrap.class);
		}

	}

	public static class SoiledFist extends YogFist {

		{
			spriteClass = FistSprite.Soiled.class;
		}

		@Override
		public boolean playGameTurn() {

			boolean result = super.playGameTurn();

			//1.33 grass tiles on average
			int furrowedTiles = Random.chances(new float[]{0, 2, 1});

			for (int i = 0; i < furrowedTiles; i++) {
				int cell = position + PathFinder.OFFSETS_NEIGHBOURS9[Random.Int(9)];
				if (Dungeon.level.map[cell] == Terrain.GRASS) {
					Level.set(cell, Terrain.FURROWED_GRASS);
					GameScene.updateMap(cell);
					CellEmitter.get(cell).burst(LeafParticle.GENERAL, 10);
				}
			}

			Dungeon.observe();

			for (int i : PathFinder.OFFSETS_NEIGHBOURS9) {
				int cell = position + i;
				if (canSpreadGrass(cell)){
					Level.set(position +i, Terrain.GRASS);
					GameScene.updateMap( position + i );
				}
			}

			return result;
		}

		@Override
		public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
			int grassCells = 0;
			for (int i : PathFinder.OFFSETS_NEIGHBOURS9) {
				if (Dungeon.level.map[position +i] == Terrain.FURROWED_GRASS
				|| Dungeon.level.map[position +i] == Terrain.HIGH_GRASS){
					grassCells++;
				}
			}
			if (grassCells > 0) dmg = Math.round(dmg * (6-grassCells)/6f);

			//can be ignited, but takes no damage from burning
			if (sourceOfDamage.getClass() == Burning.class){
				return;
			}

			super.receiveDamageFromSource(dmg, sourceOfDamage);
		}

		@Override
		protected void zap() {
			spendTimeAdjusted( 1f );

			Invisibility.dispel(this);
			Character enemy = this.enemy;
			if (isTargetHitByAttack( this, enemy, true )) {

				Buff.affect( enemy, Roots.class, 3f );

			} else {

				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.getDefenseVerb() );
			}

			for (int i : PathFinder.OFFSETS_NEIGHBOURS9){
				int cell = enemy.position + i;
				if (canSpreadGrass(cell)){
					if (Random.Int(5) == 0){
						Level.set(cell, Terrain.FURROWED_GRASS);
						GameScene.updateMap( cell );
					} else {
						Level.set(cell, Terrain.GRASS);
						GameScene.updateMap( cell );
					}
					CellEmitter.get( cell ).burst( LeafParticle.GENERAL, 10 );
				}
			}
			Dungeon.observe();

		}

		private boolean canSpreadGrass(int cell){
			int yogPos = Dungeon.level.exit() + Dungeon.level.width()*3;
			return Dungeon.level.distance(cell, yogPos) > 4 && !Dungeon.level.solid[cell]
					&& !(Dungeon.level.map[cell] == Terrain.FURROWED_GRASS || Dungeon.level.map[cell] == Terrain.HIGH_GRASS);
		}

	}

	public static class RottingFist extends YogFist {

		{
			spriteClass = FistSprite.Rotting.class;

			properties.add(Property.ACIDIC);
		}

		@Override
		protected boolean playGameTurn() {
			//ensures toxic gas acts at the appropriate time when added
			GameScene.add(ActorLoop.seed(position, 0, ToxicGas.class));

			if (Dungeon.level.water[position] && healthPoints < healthMax) {
				sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healthMax /50), FloatingText.HEALING);
				healthPoints = Math.min(healthMax, healthPoints + healthMax /50);
			}

			return super.playGameTurn();
		}

		@Override
		public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
			if (!isInvulnerableToEffectType(sourceOfDamage.getClass())
					&& !(sourceOfDamage instanceof Bleeding)
					&& getBuff(Sickle.HarvestBleedTracker.class) == null){
				dmg = Math.round( dmg * getResistanceMultiplierToEffectType( sourceOfDamage.getClass() ));
				if (dmg < 0){
					return;
				}
				Bleeding b = getBuff(Bleeding.class);
				if (b == null){
					b = new Bleeding();
				}
				b.announced = false;
				b.set(dmg*.6f);
				b.attachTo(this);
				sprite.showStatus(CharSprite.WARNING, Messages.titleCase(b.name()) + " " + (int)b.level());
			} else{
				super.receiveDamageFromSource(dmg, sourceOfDamage);
			}
		}

		@Override
		protected void zap() {
			spendTimeAdjusted( 1f );
			GameScene.add(ActorLoop.seed(enemy.position, 100, ToxicGas.class));
		}

		@Override
		public int attackProc(Character enemy, int damage ) {
			damage = super.attackProc( enemy, damage );

			if (Random.Int( 2 ) == 0) {
				Buff.affect( enemy, Ooze.class ).set( Ooze.DURATION );
				enemy.sprite.burst( 0xFF000000, 5 );
			}

			return damage;
		}

		{
			immunities.add(ToxicGas.class);
		}

	}

	public static class RustedFist extends YogFist {

		{
			spriteClass = FistSprite.Rusted.class;

			properties.add(Property.LARGE);
			properties.add(Property.INORGANIC);
		}

		@Override
		public int getDamageRoll() {
			return Random.NormalIntRange( 22, 44 );
		}

		@Override
		public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
			if (!isInvulnerableToEffectType(sourceOfDamage.getClass()) && !(sourceOfDamage instanceof Viscosity.DeferedDamage)){
				dmg = Math.round( dmg * getResistanceMultiplierToEffectType( sourceOfDamage.getClass() ));
				if (dmg >= 0) {
					Buff.affect(this, Viscosity.DeferedDamage.class).prolong(dmg);
					sprite.showStatus(CharSprite.WARNING, Messages.get(Viscosity.class, "deferred", dmg));
				}
			} else{
				super.receiveDamageFromSource(dmg, sourceOfDamage);
			}
		}

		@Override
		protected void zap() {
			spendTimeAdjusted( 1f );
			Buff.affect(enemy, Cripple.class, 4f);
		}

	}

	public static class BrightFist extends YogFist {

		{
			spriteClass = FistSprite.Bright.class;

			properties.add(Property.ELECTRIC);

			canRangedInMelee = false;
		}

		@Override
		protected void incrementRangedCooldown() {
			//ranged attack has no cooldown
		}

		//used so resistances can differentiate between melee and magical attacks
		public static class LightBeam{}

		@Override
		protected void zap() {
			spendTimeAdjusted( 1f );

			Invisibility.dispel(this);
			Character enemy = this.enemy;
			if (isTargetHitByAttack( this, enemy, true )) {

				enemy.receiveDamageFromSource( Random.NormalIntRange(10, 20), new LightBeam() );
				Buff.prolong( enemy, Blindness.class, Blindness.DURATION/2f );

				if (!enemy.isAlive() && enemy == Dungeon.hero) {
					Badges.validateDeathFromEnemyMagic();
					Dungeon.fail( this );
					GLog.n( Messages.get(Character.class, "kill", getName()) );
				}

			} else {

				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.getDefenseVerb() );
			}

		}

		@Override
		public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
			int beforeHP = healthPoints;
			super.receiveDamageFromSource(dmg, sourceOfDamage);
			if (isAlive() && beforeHP > healthMax /2 && healthPoints < healthMax /2){
				healthPoints = healthMax /2;
				Buff.prolong( Dungeon.hero, Blindness.class, Blindness.DURATION*1.5f );
				int i;
				do {
					i = Random.Int(Dungeon.level.length());
				} while (Dungeon.level.heroFOV[i]
						|| Dungeon.level.solid[i]
						|| Actor.getCharacterOnPosition(i) != null
						|| PathFinder.getStep(i, Dungeon.level.exit(), Dungeon.level.passable) == -1);
				ScrollOfTeleportation.appear(this, i);
				state = WANDERING;
				GameScene.flash(0x80FFFFFF);
				GLog.w( Messages.get( this, "teleport" ));
			} else if (!isAlive()){
				Buff.prolong( Dungeon.hero, Blindness.class, Blindness.DURATION*3f );
				GameScene.flash(0x80FFFFFF);
			}
		}

	}

	public static class DarkFist extends YogFist {

		{
			spriteClass = FistSprite.Dark.class;

			canRangedInMelee = false;
		}

		@Override
		protected void incrementRangedCooldown() {
			//ranged attack has no cooldown
		}

		//used so resistances can differentiate between melee and magical attacks
		public static class DarkBolt{}

		@Override
		protected void zap() {
			spendTimeAdjusted( 1f );

			Invisibility.dispel(this);
			Character enemy = this.enemy;
			if (isTargetHitByAttack( this, enemy, true )) {

				enemy.receiveDamageFromSource( Random.NormalIntRange(10, 20), new DarkBolt() );

				Light l = enemy.getBuff(Light.class);
				if (l != null){
					l.weaken(50);
				}

				if (!enemy.isAlive() && enemy == Dungeon.hero) {
					Badges.validateDeathFromEnemyMagic();
					Dungeon.fail( this );
					GLog.n( Messages.get(Character.class, "kill", getName()) );
				}

			} else {

				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.getDefenseVerb() );
			}

		}

		@Override
		public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
			int beforeHP = healthPoints;
			super.receiveDamageFromSource(dmg, sourceOfDamage);
			if (isAlive() && beforeHP > healthMax /2 && healthPoints < healthMax /2){
				healthPoints = healthMax /2;
				Light l = Dungeon.hero.getBuff(Light.class);
				if (l != null){
					l.detach();
				}
				int i;
				do {
					i = Random.Int(Dungeon.level.length());
				} while (Dungeon.level.heroFOV[i]
						|| Dungeon.level.solid[i]
						|| Actor.getCharacterOnPosition(i) != null
						|| PathFinder.getStep(i, Dungeon.level.exit(), Dungeon.level.passable) == -1);
				ScrollOfTeleportation.appear(this, i);
				state = WANDERING;
				GameScene.flash(0, false);
				GLog.w( Messages.get( this, "teleport" ));
			} else if (!isAlive()){
				Light l = Dungeon.hero.getBuff(Light.class);
				if (l != null){
					l.detach();
				}
				GameScene.flash(0, false);
			}
		}

	}

}
