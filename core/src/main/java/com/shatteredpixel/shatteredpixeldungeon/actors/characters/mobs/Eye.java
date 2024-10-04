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
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionHit;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EyeSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Eye extends Mob {
	
	{
		spriteClass = EyeSprite.class;
		
		healthPoints = healthMax = 100;
		evasionSkill = 20;
		viewDistance = Light.DISTANCE;
		
		EXP = 13;
		maxLvl = 26;

		getCharacterMovement().setFlying(true);

		HUNTING = new Hunting();
		
		loot = new Dewdrop();
		lootChance = 1f;

		properties.add(Property.DEMONIC);
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange(20, 30);
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 30;
	}
	
	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 10);
	}
	
	private Ballistica beam;
	private int beamTarget = -1;
	private int beamCooldown;
	public boolean beamCharged;

	@Override
	protected boolean canAttackEnemy(Character enemy ) {

		if (beamCooldown == 0) {
			Ballistica aim = new Ballistica(position, enemy.position, Ballistica.STOP_SOLID);

			if (enemy.invisible == 0 && !isCharmedBy(enemy) && fieldOfView[enemy.position]
					&& (super.canAttackEnemy(enemy) || aim.subPath(1, aim.dist).contains(enemy.position))){
				beam = aim;
				beamTarget = enemy.position;
				return true;
			} else {
				//if the beam is charged, it has to attack, will aim at previous location of target.
				return beamCharged;
			}
		} else {
			return super.canAttackEnemy(enemy);
		}
	}

	@Override
    public boolean playGameTurn() {
		if (beamCharged && state != HUNTING){
			beamCharged = false;
			sprite.idle();
		}
		if (beam == null && beamTarget != -1) {
			beam = new Ballistica(position, beamTarget, Ballistica.STOP_SOLID);
			sprite.turnTo(position, beamTarget);
		}
		if (beamCooldown > 0)
			beamCooldown--;
		return super.playGameTurn();
	}

	@Override
	protected boolean attackCharacter(Character targetCharacter) {

		beam = new Ballistica(position, beamTarget, Ballistica.STOP_SOLID);
		if (beamCooldown > 0 || (!beamCharged && !beam.subPath(1, beam.dist).contains(targetCharacter.position))) {
			return super.attackCharacter(targetCharacter);
		} else if (!beamCharged){
			((EyeSprite)sprite).charge( targetCharacter.position);
			spendTimeAdjusted( getAttackDelay()*2f );
			beamCharged = true;
			return true;
		} else {

			spendTimeAdjusted( getAttackDelay() );
			
			if (Dungeon.level.heroFOV[position] || Dungeon.level.heroFOV[beam.collisionPos] ) {
				sprite.zap( beam.collisionPos );
				return false;
			} else {
				sprite.idle();
				deathGaze();
				return true;
			}
		}

	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if (beamCharged) dmg /= 4;
		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}
	
	//used so resistances can differentiate between melee and magical attacks
	public static class DeathGaze{}

	public void deathGaze(){
		if (!beamCharged || beamCooldown > 0 || beam == null)
			return;

		beamCharged = false;
		beamCooldown = Random.IntRange(4, 6);

		boolean terrainAffected = false;

		Invisibility.dispel(this);
		for (int pos : beam.subPath(1, beam.dist)) {

			if (Dungeon.level.flamable[pos]) {

				Dungeon.level.destroy( pos );
				GameScene.updateMap( pos );
				terrainAffected = true;

			}

			Character ch = DungeonCharactersHandler.getCharacterOnPosition( pos );
			if (ch == null) {
				continue;
			}

			if (ActionHit.isTargetHitByAttack( this, ch, true )) {
				int dmg = Random.NormalIntRange( 30, 50 );
				dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
				ch.receiveDamageFromSource( dmg, new DeathGaze() );

				if (Dungeon.level.heroFOV[pos]) {
					ch.sprite.flash();
					CellEmitter.center( pos ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
				}

				if (!ch.isAlive() && ch == Dungeon.hero) {
					Badges.validateDeathFromEnemyMagic();
					Dungeon.fail( this );
					GLog.n( Messages.get(this, "deathgaze_kill") );
				}
			} else {
				ch.sprite.showStatus( CharSprite.NEUTRAL,  ch.getDefenseVerb() );
			}
		}

		if (terrainAffected) {
			Dungeon.observe();
		}

		beam = null;
		beamTarget = -1;
	}

	//generates an average of 1 dew, 0.25 seeds, and 0.25 stones
	@Override
	public Item getLootItem() {
		Item loot;
		switch(Random.Int(4)){
			case 0: case 1: default:
				loot = new Dewdrop();
				int ofs;
				do {
					ofs = PathFinder.OFFSETS_NEIGHBOURS8[Random.Int(8)];
				} while (Dungeon.level.solid[position + ofs] && !Dungeon.level.passable[position + ofs]);
				if (Dungeon.level.heaps.get(position +ofs) == null) {
					Dungeon.level.dropItemOnPosition(new Dewdrop(), position + ofs).sprite.drop(position);
				} else {
					Dungeon.level.dropItemOnPosition(new Dewdrop(), position + ofs).sprite.drop(position + ofs);
				}
				break;
			case 2:
				loot = ItemGenerator.randomUsingDefaults(ItemGenerator.Category.SEED);
				break;
			case 3:
				loot = ItemGenerator.randomUsingDefaults(ItemGenerator.Category.STONE);
				break;
		}
		return loot;
	}

	private static final String BEAM_TARGET     = "beamTarget";
	private static final String BEAM_COOLDOWN   = "beamCooldown";
	private static final String BEAM_CHARGED    = "beamCharged";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( BEAM_TARGET, beamTarget);
		bundle.put( BEAM_COOLDOWN, beamCooldown );
		bundle.put( BEAM_CHARGED, beamCharged );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(BEAM_TARGET))
			beamTarget = bundle.getInt(BEAM_TARGET);
		beamCooldown = bundle.getInt(BEAM_COOLDOWN);
		beamCharged = bundle.getBoolean(BEAM_CHARGED);
	}

	{
		resistances.add( WandOfDisintegration.class );
		resistances.add( DeathGaze.class );
		resistances.add( DisintegrationTrap.class );
	}

	private class Hunting extends Mob.Hunting{
		@Override
		public boolean playGameTurn(boolean enemyInFOV, boolean justAlerted) {
			//even if enemy isn't seen, attack them if the beam is charged
			if (beamCharged && enemy != null && canAttackEnemy(enemy)) {
				enemySeen = enemyInFOV;
				return attackCharacter(enemy);
			}
			return super.playGameTurn(enemyInFOV, justAlerted);
		}
	}
}
