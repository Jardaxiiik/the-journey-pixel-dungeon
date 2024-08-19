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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.actorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElementalSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class Elemental extends Mob {

	{
		healthPoints = healthMax = 60;
		defenseSkill = 20;
		
		EXP = 10;
		maxLvl = 20;
		
		flying = true;
	}

	protected boolean summonedALly;
	
	@Override
	public int getDamageRoll() {
		if (!summonedALly) {
			return Random.NormalIntRange(20, 25);
		} else {
			int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
			return Random.NormalIntRange(5*regionScale, 5 + 5*regionScale);
		}
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		if (!summonedALly) {
			return 25;
		} else {
			int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
			return 5 + 5*regionScale;
		}
	}

	public void setSummonedALly(){
		summonedALly = true;
		//sewers are prison are equivalent, otherwise scales as normal (2/2/3/4/5)
		int regionScale = Math.max(2, (1 + Dungeon.scalingDepth()/5));
		defenseSkill = 5*regionScale;
		healthMax = 15*regionScale;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 5);
	}
	
	protected int rangedCooldown = Random.NormalIntRange( 3, 5 );
	
	@Override
	protected boolean playGameTurn() {
		if (state == HUNTING){
			rangedCooldown--;
		}
		
		return super.playGameTurn();
	}
	
	@Override
	protected boolean canAttackEnemy(Character enemy ) {
		if (super.canAttackEnemy(enemy)){
			return true;
		} else {
			return rangedCooldown < 0 && new Ballistica(position, enemy.position, Ballistica.MAGIC_BOLT ).collisionPos == enemy.position;
		}
	}
	
	protected boolean attackCharacter(Character targetCharacter) {
		
		if (Dungeon.level.adjacent(position, targetCharacter.position)
				|| rangedCooldown > 0
				|| new Ballistica(position, targetCharacter.position, Ballistica.MAGIC_BOLT ).collisionPos != targetCharacter.position) {
			
			return super.attackCharacter(targetCharacter);
			
		} else {
			
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
	public int attackProc(Character enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		meleeProc( enemy, damage );
		
		return damage;
	}
	
	protected void zap() {
		spendTimeAdjusted( 1f );

		Invisibility.dispel(this);
		Character enemy = this.enemy;
		if (isTargetHitByAttack( this, enemy, true )) {
			
			rangedProc( enemy );
			
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}

		rangedCooldown = Random.NormalIntRange( 3, 5 );
	}
	
	public void onZapComplete() {
		zap();
		next();
	}
	
	@Override
	public boolean addBuff(Buff buff ) {
		if (harmfulBuffs.contains( buff.getClass() )) {
			receiveDamageFromSource( Random.NormalIntRange( healthMax /2, healthMax * 3/5 ), buff );
			return false;
		} else {
			return super.addBuff( buff );
		}
	}
	
	protected abstract void meleeProc(Character enemy, int damage );
	protected abstract void rangedProc( Character enemy );
	
	protected ArrayList<Class<? extends Buff>> harmfulBuffs = new ArrayList<>();
	
	private static final String COOLDOWN = "cooldown";
	private static final String SUMMONED_ALLY = "summoned_ally";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( COOLDOWN, rangedCooldown );
		bundle.put( SUMMONED_ALLY, summonedALly);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( COOLDOWN )){
			rangedCooldown = bundle.getInt( COOLDOWN );
		}
		summonedALly = bundle.getBoolean( SUMMONED_ALLY );
		if (summonedALly){
			setSummonedALly();
		}
	}
	
	public static class FireElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Fire.class;
			
			loot = new PotionOfLiquidFlame();
			lootChance = 1/8f;
			
			properties.add( Property.FIERY );
			
			harmfulBuffs.add( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost.class );
			harmfulBuffs.add( Chill.class );
		}
		
		@Override
		protected void meleeProc(Character enemy, int damage ) {
			if (Random.Int( 2 ) == 0 && !Dungeon.level.water[enemy.position]) {
				Buff.affect( enemy, Burning.class ).reignite( enemy );
				if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
			}
		}
		
		@Override
		protected void rangedProc( Character enemy ) {
			if (!Dungeon.level.water[enemy.position]) {
				Buff.affect( enemy, Burning.class ).reignite( enemy, 4f );
			}
			if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
		}
	}
	
	//used in wandmaker quest, a fire elemental with lower ACC/EVA/DMG, no on-hit fire
	// and a unique 'fireball' style ranged attack, which can be dodged
	public static class NewbornFireElemental extends FireElemental {
		
		{
			spriteClass = ElementalSprite.NewbornFire.class;

			defenseSkill = 12;
			
			properties.add(Property.MINIBOSS);
		}

		private int targetingPos = -1;

		@Override
		protected boolean playGameTurn() {
			if (targetingPos != -1){
				if (sprite != null && (sprite.visible || Dungeon.level.heroFOV[targetingPos])) {
					sprite.zap( targetingPos );
					return false;
				} else {
					zap();
					return true;
				}
			} else {
				return super.playGameTurn();
			}
		}

		@Override
		protected boolean canAttackEnemy(Character enemy ) {
			if (super.canAttackEnemy(enemy)){
				return true;
			} else {
				return rangedCooldown < 0 && new Ballistica(position, enemy.position, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos == enemy.position;
			}
		}

		protected boolean attackCharacter(Character targetCharacter) {

			if (rangedCooldown > 0) {

				return super.attackCharacter(targetCharacter);

			} else if (new Ballistica(position, targetCharacter.position, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET ).collisionPos == targetCharacter.position) {

				//set up an attack for next turn
				ArrayList<Integer> candidates = new ArrayList<>();
				for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
					int target = targetCharacter.position + i;
					if (target != position && new Ballistica(position, target, Ballistica.STOP_SOLID | Ballistica.STOP_TARGET).collisionPos == target){
						candidates.add(target);
					}
				}

				if (!candidates.isEmpty()){
					targetingPos = Random.element(candidates);

					for (int i : PathFinder.OFFSETS_NEIGHBOURS9){
						if (!Dungeon.level.solid[targetingPos + i]) {
							sprite.parent.addToBack(new TargetedCell(targetingPos + i, 0xFF0000));
						}
					}

					GLog.n(Messages.get(this, "charging"));
					spendTimeAdjusted(GameMath.gate(getAttackDelay(), (int)Math.ceil(Dungeon.hero.cooldown()), 3* getAttackDelay()));
					Dungeon.hero.interrupt();
					return true;
				} else {
					rangedCooldown = 1;
					return super.attackCharacter(targetCharacter);
				}


			} else {
				rangedCooldown = 1;
				return super.attackCharacter(targetCharacter);
			}
		}

		@Override
		protected void zap() {
			if (targetingPos != -1) {
				spendTimeAdjusted(1f);

				Invisibility.dispel(this);

				for (int i : PathFinder.OFFSETS_NEIGHBOURS9) {
					if (!Dungeon.level.solid[targetingPos + i]) {
						CellEmitter.get(targetingPos + i).burst(ElmoParticle.FACTORY, 5);
						if (Dungeon.level.water[targetingPos + i]) {
							GameScene.add(actorLoop.seed(targetingPos + i, 2, Fire.class));
						} else {
							GameScene.add(actorLoop.seed(targetingPos + i, 8, Fire.class));
						}

						Character target = Actor.getCharacterOnPosition(targetingPos + i);
						if (target != null && target != this) {
							Buff.affect(target, Burning.class).reignite(target);
						}
					}
				}
				Sample.INSTANCE.play(Assets.Sounds.BURNING);
			}

			targetingPos = -1;
			rangedCooldown = Random.NormalIntRange( 3, 5 );
		}

		@Override
		public int getAccuracyAgainstTarget(Character target) {
			if (!summonedALly) {
				return 15;
			} else {
				return super.getAccuracyAgainstTarget(target);
			}
		}

		@Override
		public int getDamageRoll() {
			if (!summonedALly) {
				return Random.NormalIntRange(10, 12);
			} else {
				return super.getDamageRoll();
			}
		}

		@Override
		protected void meleeProc(Character enemy, int damage) {
			//no fiery on-hit unless it is an ally summon
			if (summonedALly) {
				super.meleeProc(enemy, damage);
			}
		}

		@Override
		public void die(Object source) {
			super.die(source);
			if (alignment == Alignment.ENEMY) {
				Dungeon.level.dropItemOnPosition( new Embers(), position).sprite.drop();
				Statistics.questScores[1] = 2000;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						Music.INSTANCE.fadeOut(1f, new Callback() {
							@Override
							public void call() {
								if (Dungeon.level != null) {
									Dungeon.level.playLevelMusic();
								}
							}
						});
					}
				});
			}
		}

		@Override
		public boolean reset() {
			return !summonedALly;
		}

		@Override
		public String getDescription() {
			String desc = super.getDescription();

			if (summonedALly){
				desc += " " + Messages.get(this, "desc_ally");
			} else {
				desc += " " + Messages.get(this, "desc_boss");
			}

			return desc;
		}

		private static final String TARGETING_POS = "targeting_pos";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TARGETING_POS, targetingPos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			targetingPos = bundle.getInt(TARGETING_POS);
		}
	}

	//not a miniboss, no ranged attack, otherwise a newborn elemental
	public static class AllyNewBornElemental extends NewbornFireElemental {

		{
			rangedCooldown = Integer.MAX_VALUE;

			properties.remove(Property.MINIBOSS);
		}

	}
	
	public static class FrostElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Frost.class;
			
			loot = new PotionOfFrost();
			lootChance = 1/8f;
			
			properties.add( Property.ICY );
			
			harmfulBuffs.add( Burning.class );
		}
		
		@Override
		protected void meleeProc(Character enemy, int damage ) {
			if (Random.Int( 3 ) == 0 || Dungeon.level.water[enemy.position]) {
				Freezing.freeze( enemy.position);
				if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
			}
		}
		
		@Override
		protected void rangedProc( Character enemy ) {
			Freezing.freeze( enemy.position);
			if (enemy.sprite.visible) Splash.at( enemy.sprite.center(), sprite.blood(), 5);
		}
	}
	
	public static class ShockElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Shock.class;
			
			loot = new ScrollOfRecharging();
			lootChance = 1/4f;
			
			properties.add( Property.ELECTRIC );
		}
		
		@Override
		protected void meleeProc(Character enemy, int damage ) {
			ArrayList<Character> affected = new ArrayList<>();
			ArrayList<Lightning.Arc> arcs = new ArrayList<>();
			Shocking.arc( this, enemy, 2, affected, arcs );
			
			if (!Dungeon.level.water[enemy.position]) {
				affected.remove( enemy );
			}
			
			for (Character ch : affected) {
				ch.receiveDamageFromSource( Math.round( damage * 0.4f ), Shocking.class );
				if (ch == Dungeon.hero && !ch.isAlive()){
					Dungeon.fail(this);
					GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", getName())) );
				}
			}

			boolean visible = sprite.visible || enemy.sprite.visible;
			for (Character ch : affected){
				if (ch.sprite.visible) visible = true;
			}

			if (visible) {
				sprite.parent.addToFront(new Lightning(arcs, null));
				Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
			}
		}
		
		@Override
		protected void rangedProc( Character enemy ) {
			Buff.affect( enemy, Blindness.class, Blindness.DURATION/2f );
			if (enemy == Dungeon.hero) {
				GameScene.flash(0x80FFFFFF);
			}
		}
	}
	
	public static class ChaosElemental extends Elemental {
		
		{
			spriteClass = ElementalSprite.Chaos.class;
			
			loot = new ScrollOfTransmutation();
			lootChance = 1f;
		}
		
		@Override
		protected void meleeProc(Character enemy, int damage ) {
			CursedWand.cursedEffect(null, this, enemy);
		}
		
		@Override
		protected void rangedProc( Character enemy ) {
			CursedWand.cursedEffect(null, this, enemy);
		}
	}
	
	public static Class<? extends Elemental> random(){
		if (Random.Int( 50 ) == 0){
			return ChaosElemental.class;
		}
		
		float roll = Random.Float();
		if (roll < 0.4f){
			return FireElemental.class;
		} else if (roll < 0.8f){
			return FrostElemental.class;
		} else {
			return ShockElemental.class;
		}
	}
}
