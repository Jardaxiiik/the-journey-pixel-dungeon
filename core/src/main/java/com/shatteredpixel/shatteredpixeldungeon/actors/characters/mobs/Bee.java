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

import com.shatteredpixel.shatteredpixeldungeon.actions.ActionBuffs;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BeeSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

//FIXME the AI for these things is becoming a complete mess, should refactor
public class Bee extends Mob {
	
	{
		spriteClass = BeeSprite.class;
		
		viewDistance = 4;

		EXP = 0;

		getCharacterMovement().setFlying(true);
		state = WANDERING;
		
		//only applicable when the bee is charmed with elixir of honeyed healing
		intelligentAlly = true;
	}

	private int level;

	//-1 refers to a pot that has gone missing.
	private int potPos;
	//-1 for no owner
	private int potHolder;
	
	private static final String LEVEL	    = "level";
	private static final String POTPOS	    = "potpos";
	private static final String POTHOLDER	= "potholder";
	private static final String ALIGMNENT   = "alignment";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
		bundle.put( POTPOS, potPos );
		bundle.put( POTHOLDER, potHolder );
		bundle.put( ALIGMNENT, alignment);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		spawn( bundle.getInt( LEVEL ) );
		potPos = bundle.getInt( POTPOS );
		potHolder = bundle.getInt( POTHOLDER );
		if (bundle.contains(ALIGMNENT)) alignment = bundle.getEnum( ALIGMNENT, Alignment.class);
	}
	
	public void spawn( int level ) {
		this.level = level;
		
		healthMax = (2 + level) * 4;
		evasionSkill = 9 + level;
	}

	public void setPotInfo(int potPos, Character potHolder){
		this.potPos = potPos;
		if (potHolder == null)
			this.potHolder = -1;
		else
			this.potHolder = potHolder.getId();
	}
	
	public int potPos(){
		return potPos;
	}
	
	public int potHolderID(){
		return potHolder;
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return evasionSkill;
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( healthMax / 10, healthMax / 4 );
	}
	
	@Override
	public int attackProc_1(Character enemy, int damage ) {
		damage = ActionAttack.attackProc(this, enemy, damage );
		if (enemy instanceof Mob) {
			((Mob)enemy).startHunting( this );
		}
		return damage;
	}

	@Override
	public boolean addBuff(Buff buff) {
		if (ActionBuffs.addBuff(this,buff)) {
			//TODO maybe handle honeyed bees with their own ally buff?
			if (buff instanceof AllyBuff) {
				intelligentAlly = false;
				setPotInfo(-1, null);
			}
			return true;
		}
		return false;
	}

	@Override
	protected Character chooseEnemy() {
		//if the pot is no longer present, default to regular AI behaviour
		if (alignment == Alignment.ALLY || (potHolder == -1 && potPos == -1)){
			return super.chooseEnemy();
		
		//if something is holding the pot, target that
		}else if (DungeonActors.getById(potHolder) != null){
			return (Character) DungeonActors.getById(potHolder);
			
		//if the pot is on the ground
		}else {
			
			//try to find a new enemy in these circumstances
			if (enemy == null || !ActionHealth.isAlive(enemy) || !DungeonCharactersHandler.getCharacters().contains(enemy) || state == WANDERING
					|| Dungeon.level.distance(enemy.position, potPos) > 3
					|| (alignment == Alignment.ALLY && enemy.alignment == Alignment.ALLY)
					|| (getBuff( Amok.class ) == null && enemy.isInvulnerableToEffectType(getClass()))){
				
				//find all mobs near the pot
				HashSet<Character> enemies = new HashSet<>();
				for (Mob mob : Dungeon.level.mobs) {
					if (!(mob == this)
							&& Dungeon.level.distance(mob.position, potPos) <= 3
							&& mob.alignment != Alignment.NEUTRAL
							&& !mob.isInvulnerableToEffectType(getClass())
							&& !(alignment == Alignment.ALLY && mob.alignment == Alignment.ALLY)) {
						enemies.add(mob);
					}
				}
				
				if (!enemies.isEmpty()){
					return Random.element(enemies);
				} else {
					if (alignment != Alignment.ALLY && Dungeon.level.distance(Dungeon.hero.position, potPos) <= 3){
						return Dungeon.hero;
					} else {
						return null;
					}
				}
				
			} else {
				return enemy;
			}

			
		}
	}

	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
		if (alignment == Alignment.ALLY && enemy == null && getBuffs(AllyBuff.class).isEmpty()){
			targetPosition = Dungeon.hero.position;
		} else if (enemy != null && DungeonActors.getById(potHolder) == enemy) {
			targetPosition = enemy.position;
		} else if (potPos != -1 && (state == WANDERING || Dungeon.level.distance(targetPosition, potPos) > 3))
			this.target = targetPosition = potPos;
		return super.moveCloserToTarget(targetPosition);
	}
	
	@Override
	public String getDescription() {
		if (alignment == Alignment.ALLY && getBuffs(AllyBuff.class).isEmpty()){
			return Messages.get(this, "desc_honey");
		} else {
			return super.getDescription();
		}
	}
}