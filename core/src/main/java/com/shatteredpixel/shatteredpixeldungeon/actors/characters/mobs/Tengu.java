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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TenguSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class Tengu extends Mob {
	
	{
		spriteClass = TenguSprite.class;
		
		healthPoints = healthMax = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 250 : 200;
		EXP = 20;
		evasionSkill = 15;
		
		HUNTING = new Hunting();
		
		flying = true; //doesn't literally fly, but he is fleet-of-foot enough to avoid hazards
		
		properties.add(Property.BOSS);
		
		viewDistance = 12;
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 6, 12 );
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		if (Dungeon.level.adjacent(position, target.position)){
			return 10;
		} else {
			return 20;
		}
	}
	
	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 5);
	}

	boolean loading = false;

	//Tengu is immune to debuffs and damage when removed from the level
	@Override
	public boolean addBuff(Buff buff) {
		if (getCharacters().contains(this) || buff instanceof Doom || loading){
			return super.addBuff(buff);
		}
		return false;
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if (!Dungeon.level.mobs.contains(this)){
			return;
		}

		PrisonBossLevel.State state = ((PrisonBossLevel)Dungeon.level).state();
		
		int hpBracket = healthMax / 8;

		int curbracket = healthPoints / hpBracket;

		int beforeHitHP = healthPoints;
		super.receiveDamageFromSource(dmg, sourceOfDamage);

		//cannot be hit through multiple brackets at a time
		if (healthPoints <= (curbracket-1)*hpBracket){
			healthPoints = (curbracket-1)*hpBracket + 1;
		}

		int newBracket =  healthPoints / hpBracket;
		dmg = beforeHitHP - healthPoints;

		LockedFloor lock = Dungeon.hero.getBuff(LockedFloor.class);
		if (lock != null) {
			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(2*dmg/3f);
			else                                                    lock.addTime(dmg);
		}

		//phase 2 of the fight is over
		if (healthPoints == 0 && state == PrisonBossLevel.State.FIGHT_ARENA) {
			//let full attack action complete first
			addActor(new Actor() {

				{
					actPriority = VFX_PRIO;
				}

				@Override
				protected boolean playGameTurn() {
					Actor.removeActor(this);
					((PrisonBossLevel)Dungeon.level).progress();
					return true;
				}
			});
			return;
		}

		//phase 1 of the fight is over
		if (state == PrisonBossLevel.State.FIGHT_START && healthPoints <= healthMax /2){
			healthPoints = (healthMax /2);
			yell(Messages.get(this, "interesting"));
			((PrisonBossLevel)Dungeon.level).progress();
			BossHealthBar.bleed(true);

		//if tengu has lost a certain amount of hp, jump
		} else if (newBracket != curbracket) {
			//let full attack action complete first
			addActor(new Actor() {

				{
					actPriority = VFX_PRIO;
				}

				@Override
				protected boolean playGameTurn() {
					Actor.removeActor(this);
					jump();
					return true;
				}
			});
			return;
		}
	}
	
	@Override
	public boolean isAlive() {
		return super.isAlive() || Dungeon.level.mobs.contains(this); //Tengu has special death rules, see prisonbosslevel.progress()
	}

	@Override
	public void die( Object source) {
		
		if (Dungeon.hero.subClass == HeroSubClass.NONE) {
			Dungeon.level.dropItemOnPosition( new TengusMask(), position).sprite.drop();
		}
		
		GameScene.bossSlain();
		super.die(source);
		
		Badges.validateBossSlain();
		if (Statistics.qualifiedForBossChallengeBadge){
			Badges.validateBossChallengeCompleted();
		}
		Statistics.bossScores[1] += 2000;
		
		LloydsBeacon beacon = Dungeon.hero.belongings.getItem(LloydsBeacon.class);
		if (beacon != null) {
			beacon.upgrade();
		}
		
		yell( Messages.get(this, "defeated") );
	}
	
	@Override
	protected boolean canAttackEnemy(Character enemy ) {
		return new Ballistica(position, enemy.position, Ballistica.PROJECTILE).collisionPos == enemy.position;
	}
	
	private void jump() {
		
		//in case tengu hasn't had a chance to act yet
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
			Dungeon.level.updateFieldOfView( this, fieldOfView );
		}
		
		if (enemy == null) enemy = chooseEnemy();
		if (enemy == null) return;
		
		int newPos;
		if (Dungeon.level instanceof PrisonBossLevel){
			PrisonBossLevel level = (PrisonBossLevel) Dungeon.level;
			
			//if we're in phase 1, want to warp around within the room
			if (level.state() == PrisonBossLevel.State.FIGHT_START) {
				
				level.cleanTenguCell();

				int tries = 100;
				do {
					newPos = ((PrisonBossLevel)Dungeon.level).randomTenguCellPos();
					tries--;
				} while ( tries > 0 && (level.trueDistance(newPos, enemy.position) <= 3.5f
						|| level.trueDistance(newPos, Dungeon.hero.position) <= 3.5f
						|| getCharacterOnPosition(newPos) != null));

				if (tries <= 0) newPos = position;

				if (level.heroFOV[position]) CellEmitter.get(position).burst( Speck.factory( Speck.WOOL ), 6 );
				
				sprite.move(position, newPos );
				moveToPosition( newPos );
				
				if (level.heroFOV[newPos]) CellEmitter.get( newPos ).burst( Speck.factory( Speck.WOOL ), 6 );
				Sample.INSTANCE.play( Assets.Sounds.PUFF );

				float fill = 0.9f - 0.5f*((healthPoints -(healthMax /2f))/(healthMax /2f));
				level.placeTrapsInTenguCell(fill);
				
			//otherwise, jump in a larger possible area, as the room is bigger
			} else {

				int tries = 100;
				do {
					newPos = Random.Int(level.length());
					tries--;
				} while (  tries > 0 &&
						(level.solid[newPos] ||
								level.distance(newPos, enemy.position) < 5 ||
								level.distance(newPos, enemy.position) > 7 ||
								level.distance(newPos, Dungeon.hero.position) < 5 ||
								level.distance(newPos, Dungeon.hero.position) > 7 ||
								level.distance(newPos, position) < 5 ||
								getCharacterOnPosition(newPos) != null ||
								Dungeon.level.heaps.get(newPos) != null));

				if (tries <= 0) newPos = position;

				if (level.heroFOV[position]) CellEmitter.get(position).burst( Speck.factory( Speck.WOOL ), 6 );
				
				sprite.move(position, newPos );
				moveToPosition( newPos );
				
				if (arenaJumps < 4) arenaJumps++;
				
				if (level.heroFOV[newPos]) CellEmitter.get( newPos ).burst( Speck.factory( Speck.WOOL ), 6 );
				Sample.INSTANCE.play( Assets.Sounds.PUFF );
				
			}
			
		//if we're on another type of level
		} else {
			Level level = Dungeon.level;
			
			newPos = level.randomRespawnCell( this );
			
			if (level.heroFOV[position]) CellEmitter.get(position).burst( Speck.factory( Speck.WOOL ), 6 );
			
			sprite.move(position, newPos );
			moveToPosition( newPos );
			
			if (level.heroFOV[newPos]) CellEmitter.get( newPos ).burst( Speck.factory( Speck.WOOL ), 6 );
			Sample.INSTANCE.play( Assets.Sounds.PUFF );
			
		}
		
	}
	
	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()) {
			BossHealthBar.assignBoss(this);
			if (healthPoints <= healthMax /2) BossHealthBar.bleed(true);
			if (healthPoints == healthMax) {
				yell(Messages.get(this, "notice_gotcha", Dungeon.hero.getName()));
				for (Character ch : getCharacters()){
					if (ch instanceof DriedRose.GhostHero){
						((DriedRose.GhostHero) ch).sayBoss();
					}
				}
			} else {
				yell(Messages.get(this, "notice_have", Dungeon.hero.getName()));
			}
		}
	}
	
	{
		immunities.add( Blindness.class );
		immunities.add( Dread.class );
		immunities.add( Terror.class );
	}
	
	private static final String LAST_ABILITY     = "last_ability";
	private static final String ABILITIES_USED   = "abilities_used";
	private static final String ARENA_JUMPS      = "arena_jumps";
	private static final String ABILITY_COOLDOWN = "ability_cooldown";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( LAST_ABILITY, lastAbility );
		bundle.put( ABILITIES_USED, abilitiesUsed );
		bundle.put( ARENA_JUMPS, arenaJumps );
		bundle.put( ABILITY_COOLDOWN, abilityCooldown );
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		loading = true;
		super.restoreFromBundle(bundle);
		loading = false;
		lastAbility = bundle.getInt( LAST_ABILITY );
		abilitiesUsed = bundle.getInt( ABILITIES_USED );
		arenaJumps = bundle.getInt( ARENA_JUMPS );
		abilityCooldown = bundle.getInt( ABILITY_COOLDOWN );
		
		BossHealthBar.assignBoss(this);
		if (healthPoints <= healthMax /2) BossHealthBar.bleed(true);
	}
	
	//don't bother bundling this, as its purely cosmetic
	private boolean yelledCoward = false;
	
	//tengu is always hunting
	private class Hunting extends Mob.Hunting{
		
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			
			enemySeen = enemyInFOV;
			if (enemyInFOV && !isCharmedBy( enemy ) && canAttackEnemy( enemy )) {
				
				if (canUseAbility()){
					return useAbility();
				}
				
				return attackCharacter( enemy );
				
			} else {
				
				if (enemyInFOV) {
					target = enemy.position;
				} else {
					chooseEnemy();
					if (enemy == null){
						//if nothing else can be targeted, target hero
						enemy = Dungeon.hero;
					}
					target = enemy.position;
				}
				
				//if not charmed, attempt to use an ability, even if the enemy can't be seen
				if (canUseAbility()){
					return useAbility();
				}
				
				spendTimeAdjusted( TICK );
				return true;
				
			}
		}
	}
	
	//*****************************************************************************************
	//***** Tengu abilities. These are expressed in game logic as buffs, blobs, and items *****
	//*****************************************************************************************
	
	//so that mobs can also use this
	private static Character throwingCharacter;
	
	private int lastAbility = -1;
	private int abilitiesUsed = 0;
	private int arenaJumps = 0;
	
	//starts at 2, so one turn and then first ability
	private int abilityCooldown = 2;
	
	private static final int BOMB_ABILITY    = 0;
	private static final int FIRE_ABILITY    = 1;
	private static final int SHOCKER_ABILITY = 2;
	
	//expects to be called once per turn;
	public boolean canUseAbility(){
		
		if (healthPoints > healthMax /2) return false;
		
		if (abilitiesUsed >= targetAbilityUses()){
			return false;
		} else {
			
			abilityCooldown--;
			
			if (targetAbilityUses() - abilitiesUsed >= 4 && !Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
				//Very behind in ability uses, use one right away!
				//but not on bosses challenge, we already cast quickly then
				abilityCooldown = 0;
				
			} else if (targetAbilityUses() - abilitiesUsed >= 3){
				//moderately behind in uses, use one every other action.
				if (abilityCooldown == -1 || abilityCooldown > 1) abilityCooldown = 1;
				
			} else {
				//standard delay before ability use, 1-4 turns
				if (abilityCooldown == -1) abilityCooldown = Random.IntRange(1, 4);
			}
			
			if (abilityCooldown == 0){
				return true;
			} else {
				return false;
			}
		}
	}
	
	private int targetAbilityUses(){
		//1 base ability use, plus 2 uses per jump
		int targetAbilityUses = 1 + 2*arenaJumps;
		
		//and ane extra 2 use for jumps 3 and 4
		targetAbilityUses += Math.max(0, arenaJumps-2);
		
		return targetAbilityUses;
	}
	
	public boolean useAbility(){
		boolean abilityUsed = false;
		int abilityToUse = -1;
		
		while (!abilityUsed){
			
			if (abilitiesUsed == 0){
				abilityToUse = BOMB_ABILITY;
			} else if (abilitiesUsed == 1){
				abilityToUse = SHOCKER_ABILITY;
			} else if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
				abilityToUse = Random.Int(2)*2; //0 or 2, can't roll fire ability with challenge
			} else {
				abilityToUse = Random.Int(3);
			}
			
			//If we roll the same ability as last time, 9/10 chance to reroll
			if (abilityToUse != lastAbility || Random.Int(10) == 0){
				switch (abilityToUse){
					case BOMB_ABILITY : default:
						abilityUsed = throwBomb(Tengu.this, enemy);
						//if Tengu cannot use his bomb ability first, use fire instead.
						if (abilitiesUsed == 0 && !abilityUsed){
							abilityToUse = FIRE_ABILITY;
							abilityUsed = throwFire(Tengu.this, enemy);
						}
						break;
					case FIRE_ABILITY:
						abilityUsed = throwFire(Tengu.this, enemy);
						break;
					case SHOCKER_ABILITY:
						abilityUsed = throwShocker(Tengu.this, enemy);
						//if Tengu cannot use his shocker ability second, use fire instead.
						if (abilitiesUsed == 1 && !abilityUsed){
							abilityToUse = FIRE_ABILITY;
							abilityUsed = throwFire(Tengu.this, enemy);
						}
						break;
				}
				//always use the fire ability with the bosses challenge
				if (abilityUsed && abilityToUse != FIRE_ABILITY && Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
					throwFire(Tengu.this, enemy);
				}
			}
			
		}
		
		//spend 1 less turn if seriously behind on ability uses
		if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
			if (targetAbilityUses() - abilitiesUsed >= 4) {
				//spend no time
			} else {
				spendTimeAdjusted(TICK);
			}
		} else {
			if (targetAbilityUses() - abilitiesUsed >= 4) {
				spendTimeAdjusted(TICK);
			} else {
				spendTimeAdjusted(2 * TICK);
			}
		}
		
		lastAbility = abilityToUse;
		abilitiesUsed++;
		return lastAbility == FIRE_ABILITY;
	}
	
	//******************
	//***Bomb Ability***
	//******************
	
	//returns true if bomb was thrown
	public static boolean throwBomb(final Character thrower, final Character target){
		
		int targetCell = -1;
		
		//Targets closest cell which is adjacent to target and has no existing bombs
		for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
			int cell = target.position + i;
			boolean bombHere = false;
			for (BombAbility b : thrower.getBuffs(BombAbility.class)){
				if (b.bombPos == cell){
					bombHere = true;
				}
			}
			if (!bombHere && !Dungeon.level.solid[cell] &&
					(targetCell == -1 || Dungeon.level.trueDistance(cell, thrower.position) < Dungeon.level.trueDistance(targetCell, thrower.position))){
				targetCell = cell;
			}
		}
		
		if (targetCell == -1){
			return false;
		}
		
		final int finalTargetCell = targetCell;
		throwingCharacter = thrower;
		final BombAbility.BombItem item = new BombAbility.BombItem();
		thrower.sprite.zap(finalTargetCell);
		((MissileSprite) thrower.sprite.parent.recycle(MissileSprite.class)).
				reset(thrower.sprite,
						finalTargetCell,
						item,
						new Callback() {
							@Override
							public void call() {
								item.onThrow(finalTargetCell);
								thrower.next();
							}
						});
		return true;
	}
	
	public static class BombAbility extends Buff {
		
		public int bombPos = -1;
		private int timer = 3;

		private ArrayList<com.watabou.noosa.particles.Emitter> smokeEmitters = new ArrayList<>();
		
		@Override
		public boolean playGameTurn() {

			if (smokeEmitters.isEmpty()){
				fx(true);
			}
			
			PointF p = DungeonTilemap.raisedTileCenterToWorld(bombPos);
			if (timer == 3) {
				FloatingText.show(p.x, p.y, bombPos, "3...", CharSprite.WARNING);
			} else if (timer == 2){
				FloatingText.show(p.x, p.y, bombPos, "2...", CharSprite.WARNING);
			} else if (timer == 1){
				FloatingText.show(p.x, p.y, bombPos, "1...", CharSprite.WARNING);
			} else {
				PathFinder.buildDistanceMap( bombPos, BArray.not( Dungeon.level.solid, null ), 2 );
				for (int cell = 0; cell < PathFinder.distance.length; cell++) {

					if (PathFinder.distance[cell] < Integer.MAX_VALUE) {
						Character ch = Actor.getCharacterOnPosition(cell);
						if (ch != null && !(ch instanceof Tengu)) {
							int dmg = Random.NormalIntRange(5 + Dungeon.scalingDepth(), 10 + Dungeon.scalingDepth() * 2);
							dmg -= ch.getArmorPointsRolled();

							if (dmg > 0) {
								ch.receiveDamageFromSource(dmg, Bomb.class);
							}

							if (ch == Dungeon.hero){
								Statistics.qualifiedForBossChallengeBadge = false;
								Statistics.bossScores[1] -= 100;

								if (!ch.isAlive()) {
									Dungeon.fail(Tengu.class);
								}
							}
						}
					}

				}

				Heap h = Dungeon.level.heaps.get(bombPos);
				if (h != null) {
					for (Item i : h.items.toArray(new Item[0])) {
						if (i instanceof BombItem) {
							h.remove(i);
						}
					}
				}
				Sample.INSTANCE.play(Assets.Sounds.BLAST);
				detach();
				return true;
			}
			
			timer--;
			spendTimeAdjusted(TICK);
			return true;
		}

		@Override
		public void fx(boolean on) {
			if (on && bombPos != -1){
				PathFinder.buildDistanceMap( bombPos, BArray.not( Dungeon.level.solid, null ), 2 );
				for (int i = 0; i < PathFinder.distance.length; i++) {
					if (PathFinder.distance[i] < Integer.MAX_VALUE) {
						com.watabou.noosa.particles.Emitter e = CellEmitter.get(i);
						e.pour( SmokeParticle.FACTORY, 0.25f );
						smokeEmitters.add(e);
					}
				}
			} else if (!on) {
				for (com.watabou.noosa.particles.Emitter e : smokeEmitters){
					e.burst(BlastParticle.FACTORY, 2);
				}
			}
		}

		private static final String BOMB_POS = "bomb_pos";
		private static final String TIMER = "timer";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( BOMB_POS, bombPos );
			bundle.put( TIMER, timer );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			bombPos = bundle.getInt( BOMB_POS );
			timer = bundle.getInt( TIMER );
		}
		
		public static class BombItem extends Item {
			
			{
				dropsDownHeap = true;
				unique = true;
				
				image = ItemSpriteSheet.TENGU_BOMB;
			}
			
			@Override
			public boolean doPickUp(Hero hero, int pos) {
				GLog.w( Messages.get(this, "cant_pickup") );
				return false;
			}
			
			@Override
			protected void onThrow(int cell) {
				super.onThrow(cell);
				if (throwingCharacter != null){
					Buff.append(throwingCharacter, BombAbility.class).bombPos = cell;
					throwingCharacter = null;
				} else {
					Buff.append(curUser, BombAbility.class).bombPos = cell;
				}
			}
			
			@Override
			public com.watabou.noosa.particles.Emitter emitter() {
				com.watabou.noosa.particles.Emitter emitter = new com.watabou.noosa.particles.Emitter();
				emitter.pos(7.5f, 3.5f);
				emitter.fillTarget = false;
				emitter.pour(SmokeParticle.SPEW, 0.05f);
				return emitter;
			}
		}
	}
	
	//******************
	//***Fire Ability***
	//******************
	
	public static boolean throwFire(final Character thrower, final Character target){
		
		Ballistica aim = new Ballistica(thrower.position, target.position, Ballistica.WONT_STOP);
		
		for (int i = 0; i < PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE.length; i++){
			if (aim.sourcePos+PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[i] == aim.path.get(1)){
				thrower.sprite.zap(target.position);
				Buff.append(thrower, Tengu.FireAbility.class).direction = i;
				
				thrower.sprite.emitter().start(Speck.factory(Speck.STEAM), .03f, 10);
				return true;
			}
		}
		
		return false;
	}
	
	public static class FireAbility extends Buff {
		
		public int direction;
		private int[] curCells;
		
		HashSet<Integer> toCells = new HashSet<>();
		
		@Override
		public boolean playGameTurn() {

			toCells.clear();

			if (curCells == null){
				curCells = new int[1];
				curCells[0] = target.position;
				spreadFromCell( curCells[0] );

			} else {
				for (Integer c : curCells) {
					if (FireActorLoop.volumeAt(c, FireActorLoop.class) > 0) spreadFromCell(c);
				}
			}
			
			for (Integer c : curCells){
				toCells.remove(c);
			}
			
			if (toCells.isEmpty()){
				detach();
			} else {
				curCells = new int[toCells.size()];
				int i = 0;
				for (Integer c : toCells){
					GameScene.add(ActorLoop.seed(c, 2, FireActorLoop.class));
					curCells[i] = c;
					i++;
				}
			}
			
			spendTimeAdjusted(TICK);
			return true;
		}
		
		private void spreadFromCell( int cell ){
			if (!Dungeon.level.solid[cell + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[left(direction)]]){
				toCells.add(cell + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[left(direction)]);
			}
			if (!Dungeon.level.solid[cell + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[direction]]){
				toCells.add(cell + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[direction]);
			}
			if (!Dungeon.level.solid[cell + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[right(direction)]]){
				toCells.add(cell + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[right(direction)]);
			}
		}
		
		private int left(int direction){
			return direction == 0 ? 7 : direction-1;
		}
		
		private int right(int direction){
			return direction == 7 ? 0 : direction+1;
		}
		
		private static final String DIRECTION = "direction";
		private static final String CUR_CELLS = "cur_cells";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( DIRECTION, direction );
			if (curCells != null) bundle.put( CUR_CELLS, curCells );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			direction = bundle.getInt( DIRECTION );
			if (bundle.contains( CUR_CELLS )) curCells = bundle.getIntArray( CUR_CELLS );
		}
		
		public static class FireActorLoop extends ActorLoop {
			
			{
				actPriority = BUFF_PRIO - 1;
				alwaysVisible = true;
			}
			
			@Override
			protected void evolve() {
				
				boolean observe = false;
				boolean burned = false;
				
				int cell;
				for (int i = area.left; i < area.right; i++){
					for (int j = area.top; j < area.bottom; j++){
						cell = i + j* Dungeon.level.width();
						off[cell] = (int)GameMath.gate(0, cur[cell] - 1, 1);
						
						if (off[cell] > 0) {
							volume += off[cell];
						}
						
						if (cur[cell] > 0 && off[cell] == 0){

							//similar to fire.burn(), but Tengu is immune, and hero loses score
							Character ch = Actor.getCharacterOnPosition( cell );
							if (ch != null && !ch.isImmuneToEffectType(Fire.class) && !(ch instanceof Tengu)) {
								Buff.affect( ch, Burning.class ).reignite( ch );
							}
							if (ch == Dungeon.hero){
								Statistics.qualifiedForBossChallengeBadge = false;
								Statistics.bossScores[1] -= 100;
							}

							Heap heap = Dungeon.level.heaps.get( cell );
							if (heap != null) {
								heap.burn();
							}

							Plant plant = Dungeon.level.plants.get( cell );
							if (plant != null){
								plant.wither();
							}
							
							if (Dungeon.level.flamable[cell]){
								Dungeon.level.destroy( cell );
								
								observe = true;
								GameScene.updateMap( cell );
							}
							
							burned = true;
							CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.03f, 10);
						}
					}
				}
				
				if (observe) {
					Dungeon.observe();
				}
				
				if (burned){
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
				}
			}
			
			@Override
			public void use(BlobEmitter emitter) {
				super.use(emitter);
				
				emitter.pour( Speck.factory( Speck.STEAM ), 0.2f );
			}
			
			@Override
			public String tileDesc() {
				return Messages.get(this, "desc");
			}
		}
	}
	
	//*********************
	//***Shocker Ability***
	//*********************
	
	//returns true if shocker was thrown
	public static boolean throwShocker(final Character thrower, final Character target){
		
		int targetCell = -1;
		
		//Targets closest cell which is adjacent to target, and not adjacent to thrower or another shocker
		for (int i : PathFinder.OFFSETS_NEIGHBOURS8){
			int cell = target.position + i;
			if (Dungeon.level.distance(cell, thrower.position) >= 2 && !Dungeon.level.solid[cell]){
				boolean validTarget = true;
				for (ShockerAbility s : thrower.getBuffs(ShockerAbility.class)){
					if (Dungeon.level.distance(cell, s.shockerPos) < 2){
						validTarget = false;
						break;
					}
				}
				if (validTarget && Dungeon.level.trueDistance(cell, thrower.position) < Dungeon.level.trueDistance(targetCell, thrower.position)){
					targetCell = cell;
				}
			}
		}
		
		if (targetCell == -1){
			return false;
		}
		
		final int finalTargetCell = targetCell;
		throwingCharacter = thrower;
		final ShockerAbility.ShockerItem item = new ShockerAbility.ShockerItem();
		thrower.sprite.zap(finalTargetCell);
		((MissileSprite) thrower.sprite.parent.recycle(MissileSprite.class)).
				reset(thrower.sprite,
						finalTargetCell,
						item,
						new Callback() {
							@Override
							public void call() {
								item.onThrow(finalTargetCell);
								thrower.next();
							}
						});
		return true;
	}
	
	public static class ShockerAbility extends Buff {
	
		public int shockerPos;
		private Boolean shockingOrdinals = null;
		
		@Override
		public boolean playGameTurn() {
			
			if (shockingOrdinals == null){
				shockingOrdinals = Random.Int(2) == 1;
				
				spreadblob();
			} else if (shockingOrdinals){
				
				target.sprite.parent.add(new Lightning(shockerPos - 1 - Dungeon.level.width(), shockerPos + 1 + Dungeon.level.width(), null));
				target.sprite.parent.add(new Lightning(shockerPos - 1 + Dungeon.level.width(), shockerPos + 1 - Dungeon.level.width(), null));
				
				if (Dungeon.level.distance(Dungeon.hero.position, shockerPos) <= 1){
					Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				}
				
				shockingOrdinals = false;
				spreadblob();
			} else {
				
				target.sprite.parent.add(new Lightning(shockerPos - Dungeon.level.width(), shockerPos + Dungeon.level.width(), null));
				target.sprite.parent.add(new Lightning(shockerPos - 1, shockerPos + 1, null));
				
				if (Dungeon.level.distance(Dungeon.hero.position, shockerPos) <= 1){
					Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				}
				
				shockingOrdinals = true;
				spreadblob();
			}
			
			spendTimeAdjusted(TICK);
			return true;
		}
		
		private void spreadblob(){
			GameScene.add(ActorLoop.seed(shockerPos, 1, ShockerActorLoop.class));
			for (int i = shockingOrdinals ? 0 : 1; i < PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE.length; i += 2){
				if (!Dungeon.level.solid[shockerPos+PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[i]]) {
					GameScene.add(ActorLoop.seed(shockerPos + PathFinder.OFFSETS_NEIGHBOURS8_CLOCKWISE[i], 2, ShockerActorLoop.class));
				}
			}
		}
		
		private static final String SHOCKER_POS = "shocker_pos";
		private static final String SHOCKING_ORDINALS = "shocking_ordinals";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( SHOCKER_POS, shockerPos );
			if (shockingOrdinals != null) bundle.put( SHOCKING_ORDINALS, shockingOrdinals );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			shockerPos = bundle.getInt( SHOCKER_POS );
			if (bundle.contains(SHOCKING_ORDINALS)) shockingOrdinals = bundle.getBoolean( SHOCKING_ORDINALS );
		}
		
		public static class ShockerActorLoop extends ActorLoop {
			
			{
				actPriority = BUFF_PRIO - 1;
				alwaysVisible = true;
			}
			
			@Override
			protected void evolve() {

				boolean shocked = false;
				
				int cell;
				for (int i = area.left; i < area.right; i++){
					for (int j = area.top; j < area.bottom; j++){
						cell = i + j* Dungeon.level.width();
						off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;
						
						if (off[cell] > 0) {
							volume += off[cell];
						}
						
						if (cur[cell] > 0 && off[cell] == 0){

							shocked = true;
							
							Character ch = Actor.getCharacterOnPosition(cell);
							if (ch != null && !(ch instanceof Tengu)){
								ch.receiveDamageFromSource(2 + Dungeon.scalingDepth(), new Electricity());
								
								if (ch == Dungeon.hero){
									Statistics.qualifiedForBossChallengeBadge = false;
									Statistics.bossScores[1] -= 100;
									if (!ch.isAlive()) {
										Dungeon.fail(Tengu.class);
										GLog.n(Messages.get(Electricity.class, "ondeath"));
									}
								}
							}
							
						}
					}
				}

				if (shocked) Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				
			}
			
			@Override
			public void use(BlobEmitter emitter) {
				super.use(emitter);
				
				emitter.pour( SparkParticle.STATIC, 0.10f );
			}
			
			@Override
			public String tileDesc() {
				return Messages.get(this, "desc");
			}
		}
		
		public static class ShockerItem extends Item {
			
			{
				dropsDownHeap = true;
				unique = true;
				
				image = ItemSpriteSheet.TENGU_SHOCKER;
			}
			
			@Override
			public boolean doPickUp(Hero hero, int pos) {
				GLog.w( Messages.get(this, "cant_pickup") );
				return false;
			}
			
			@Override
			protected void onThrow(int cell) {
				super.onThrow(cell);
				if (throwingCharacter != null){
					Buff.append(throwingCharacter, ShockerAbility.class).shockerPos = cell;
					throwingCharacter = null;
				} else {
					Buff.append(curUser, ShockerAbility.class).shockerPos = cell;
				}
			}
			
			@Override
			public com.watabou.noosa.particles.Emitter emitter() {
				com.watabou.noosa.particles.Emitter emitter = new com.watabou.noosa.particles.Emitter();
				emitter.pos(5, 5);
				emitter.fillTarget = false;
				emitter.pour(SparkParticle.FACTORY, 0.1f);
				return emitter;
			}
		}
		
	}
}
