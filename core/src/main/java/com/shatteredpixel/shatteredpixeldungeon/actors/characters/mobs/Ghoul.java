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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhoulSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Ghoul extends Mob {
	
	{
		spriteClass = GhoulSprite.class;
		
		healthPoints = healthMax = 45;
		evasionSkill = 20;
		
		EXP = 5;
		maxLvl = 20;
		
		SLEEPING = new Sleeping();
		WANDERING = new Wandering();
		state = SLEEPING;

		loot = Gold.class;
		lootChance = 0.2f;
		
		properties.add(Property.UNDEAD);
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 16, 22 );
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 24;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	@Override
	public float getSpawningWeight() {
		return 0.5f;
	}

	private int timesDowned = 0;
	protected int partnerID = -1;

	private static final String PARTNER_ID = "partner_id";
	private static final String TIMES_DOWNED = "times_downed";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PARTNER_ID, partnerID );
		bundle.put( TIMES_DOWNED, timesDowned );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		partnerID = bundle.getInt( PARTNER_ID );
		timesDowned = bundle.getInt( TIMES_DOWNED );
	}
	
	@Override
	protected boolean playGameTurn() {
		//create a child
		if (partnerID == -1){
			
			ArrayList<Integer> candidates = new ArrayList<>();
			
			int[] neighbours = {position + 1, position - 1, position + Dungeon.level.width(), position - Dungeon.level.width()};
			for (int n : neighbours) {
				if (Dungeon.level.passable[n]
						&& getCharacterOnPosition( n ) == null
						&& (!hasProperty(this, Property.LARGE) || Dungeon.level.openSpace[n])) {
					candidates.add( n );
				}
			}
			
			if (!candidates.isEmpty()){
				Ghoul child = new Ghoul();
				child.partnerID = this.getId();
				this.partnerID = child.getId();
				if (state != SLEEPING) {
					child.state = child.WANDERING;
				}
				
				child.position = Random.element( candidates );

				GameScene.add( child );
				Dungeon.level.occupyCell(child);
				
				if (sprite.visible) {
					addActor( new Pushing( child, position, child.position) );
				}

				for (Buff b : getBuffs(ChampionEnemy.class)){
					Buff.affect( child, b.getClass());
				}

			}
			
		}
		return super.playGameTurn();
	}

	private boolean beingLifeLinked = false;

	@Override
	public void die(Object source) {
		if (source != Chasm.class && source != GhoulLifeLink.class && !Dungeon.level.pit[position]){
			Ghoul nearby = GhoulLifeLink.searchForHost(this);
			if (nearby != null){
				beingLifeLinked = true;
				timesDowned++;
				removeActor(this);
				Dungeon.level.mobs.remove( this );
				Buff.append(nearby, GhoulLifeLink.class).set(timesDowned*5, this);
				((GhoulSprite)sprite).crumple();
				return;
			}
		}

		super.die(source);
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() || beingLifeLinked;
	}

	@Override
	public boolean isActive() {
		return !beingLifeLinked && isAlive();
	}

	@Override
	protected synchronized void onRemove() {
		if (beingLifeLinked) {
			for (Buff buff : getBuffs()) {
				if (buff instanceof SacrificialFire.Marked){
					//don't remove and postpone so marked stays on
					Buff.prolong(this, SacrificialFire.Marked.class, timesDowned*5);
				} else if (buff instanceof AllyBuff
						|| buff instanceof ChampionEnemy
						|| buff instanceof DwarfKing.KingDamager) {
					//don't remove
				} else {
					buff.detach();
				}
			}
		} else {
			super.onRemove();
		}
	}

	private class Sleeping extends Mob.Sleeping {
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted ) {
			Ghoul partner = (Ghoul) getById( partnerID );
			if (partner != null && partner.state != partner.SLEEPING){
				state = WANDERING;
				target = partner.position;
				return true;
			} else {
				return super.playGameTurn( enemyInFOV, justAlerted );
			}
		}
	}
	
	private class Wandering extends Mob.Wandering {
		
		@Override
		protected boolean continueWandering() {
			enemySeen = false;
			
			Ghoul partner = (Ghoul) getById( partnerID );
			if (partner != null && (partner.state != partner.WANDERING || Dungeon.level.distance(position,  partner.target) > 1)){
				target = partner.position;
				int oldPos = position;
				if (moveCloserToTarget( target )){
					spendTimeAdjusted( 1 / getSpeed() );
					return moveSprite( oldPos, position);
				} else {
					spendTimeAdjusted( TICK );
					return true;
				}
			} else {
				return super.continueWandering();
			}
		}
	}

	public static class GhoulLifeLink extends Buff{

		private Ghoul ghoul;
		private int turnsToRevive;

		@Override
		public boolean playGameTurn() {
			if (target.alignment != ghoul.alignment){
				detach();
				return true;
			}

			if (target.fieldOfView == null){
				target.fieldOfView = new boolean[Dungeon.level.length()];
				Dungeon.level.updateFieldOfView( target, target.fieldOfView );
			}

			if (!target.fieldOfView[ghoul.position] && Dungeon.level.distance(ghoul.position, target.position) >= 4){
				detach();
				return true;
			}

			if (Dungeon.level.pit[ghoul.position]){
				super.detach();
				ghoul.beingLifeLinked = false;
				ghoul.die(this);
				return true;
			}

			//have to delay this manually here are a downed ghouls can't be directly frozen otherwise
			if (target.getBuff(Challenge.DuelParticipant.class) == null) {
				turnsToRevive--;
			}
			if (turnsToRevive <= 0){
				if (Actor.getCharacterOnPosition( ghoul.position) != null) {
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int n : PathFinder.OFFSETS_NEIGHBOURS8) {
						int cell = ghoul.position + n;
						if (Dungeon.level.passable[cell]
								&& Actor.getCharacterOnPosition( cell ) == null
								&& (!hasProperty(ghoul, Property.LARGE) || Dungeon.level.openSpace[cell])) {
							candidates.add( cell );
						}
					}
					if (candidates.size() > 0) {
						int newPos = Random.element( candidates );
						Actor.addActor( new Pushing( ghoul, ghoul.position, newPos ) );
						ghoul.position = newPos;

					} else {
						spendTimeAdjusted(TICK);
						return true;
					}
				}
				ghoul.healthPoints = Math.round(ghoul.healthMax /10f);
				ghoul.beingLifeLinked = false;
				Actor.addActor(ghoul);
				ghoul.timeToNow();
				Dungeon.level.mobs.add(ghoul);
				Dungeon.level.occupyCell( ghoul );
				ghoul.sprite.idle();
				ghoul.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(Math.round(ghoul.healthMax /10f)), FloatingText.HEALING);
				super.detach();
				return true;
			}

			spendTimeAdjusted(TICK);
			return true;
		}

		public void updateVisibility(){
			if (ghoul != null && ghoul.sprite != null){
				ghoul.sprite.visible = Dungeon.level.heroFOV[ghoul.position];
			}
		}

		public void set(int turns, Ghoul ghoul){
			this.ghoul = ghoul;
			turnsToRevive = turns;
		}

		@Override
		public void fx(boolean on) {
			if (on && ghoul != null && ghoul.sprite == null){
				GameScene.addSprite(ghoul);
				((GhoulSprite)ghoul.sprite).crumple();
			}
		}

		@Override
		public void detach() {
			super.detach();
			Ghoul newHost = searchForHost(ghoul);
			if (newHost != null){
				attachTo(newHost);
				timeToNow();
			} else {
				ghoul.beingLifeLinked = false;
				ghoul.die(this);
			}
		}

		private static final String GHOUL = "ghoul";
		private static final String LEFT  = "left";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(GHOUL, ghoul);
			bundle.put(LEFT, turnsToRevive);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			ghoul = (Ghoul) bundle.get(GHOUL);
			ghoul.beingLifeLinked = true;
			turnsToRevive = bundle.getInt(LEFT);
		}

		public static Ghoul searchForHost(Ghoul dieing){

			for (Character ch : Actor.getCharacters()){
				//don't count hero ally ghouls or duel frozen ghouls
				if (ch != dieing && ch instanceof Ghoul
						&& ch.alignment == dieing.alignment
						&& ch.getBuff(Challenge.SpectatorFreeze.class) == null){
					if (ch.fieldOfView == null){
						ch.fieldOfView = new boolean[Dungeon.level.length()];
						Dungeon.level.updateFieldOfView( ch, ch.fieldOfView );
					}
					if (ch.fieldOfView[dieing.position] || Dungeon.level.distance(ch.position, dieing.position) < 4){
						return (Ghoul) ch;
					}
				}
			}
			return null;
		}
	}
}
