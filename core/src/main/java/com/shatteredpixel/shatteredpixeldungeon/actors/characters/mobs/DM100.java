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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM100Sprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class DM100 extends Mob implements Callback {

	private static final float TIME_TO_ZAP	= 1f;
	
	{
		spriteClass = DM100Sprite.class;
		
		healthPoints = healthMax = 20;
		evasionSkill = 8;
		
		EXP = 6;
		maxLvl = 13;
		
		loot = ItemGenerator.Category.SCROLL;
		lootChance = 0.25f;
		
		properties.add(Property.ELECTRIC);
		properties.add(Property.INORGANIC);
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 2, 8 );
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 11;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	@Override
	protected boolean canAttackEnemy(Character enemy ) {
		return super.canAttackEnemy(enemy)
				|| new Ballistica(position, enemy.position, Ballistica.MAGIC_BOLT).collisionPos == enemy.position;
	}
	
	//used so resistances can differentiate between melee and magical attacks
	public static class LightningBolt{}
	
	@Override
	protected boolean attackCharacter(Character targetCharacter) {

		if (Dungeon.level.adjacent(position, targetCharacter.position)
				|| new Ballistica(position, targetCharacter.position, Ballistica.MAGIC_BOLT).collisionPos != targetCharacter.position) {
			
			return super.attackCharacter(targetCharacter);
			
		} else {
			
			spendTimeAdjusted( TIME_TO_ZAP );

			Invisibility.dispel(this);
			if (isTargetHitByAttack( this, targetCharacter, true )) {
				int dmg = Random.NormalIntRange(3, 10);
				dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
				targetCharacter.receiveDamageFromSource( dmg, new LightningBolt() );

				if (targetCharacter.sprite.visible) {
					targetCharacter.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
					targetCharacter.sprite.flash();
				}
				
				if (targetCharacter == Dungeon.hero) {
					
					PixelScene.shake( 2, 0.3f );
					
					if (!targetCharacter.isAlive()) {
						Badges.validateDeathFromEnemyMagic();
						Dungeon.fail( this );
						GLog.n( Messages.get(this, "zap_kill") );
					}
				}
			} else {
				targetCharacter.sprite.showStatus( CharSprite.NEUTRAL,  targetCharacter.defenseVerb() );
			}
			
			if (sprite != null && (sprite.visible || targetCharacter.sprite.visible)) {
				sprite.zap( targetCharacter.position);
				return false;
			} else {
				return true;
			}
		}
	}
	
	@Override
	public void call() {
		next();
	}
	
}
