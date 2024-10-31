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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalWispSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class CrystalWisp extends Mob{

	{
		spriteClass = CrystalWispSprite.class;

		healthPoints = healthMax = 30;
		evasionSkill = 16;

		EXP = 7;
		maxLvl = -2;

		getCharacterMovement().setFlying(true);

		properties.add(Property.INORGANIC);
	}

	public CrystalWisp(){
		super();
		switch (Random.Int(3)){
			case 0: default:
				spriteClass = CrystalWispSprite.Blue.class;
				break;
			case 1:
				spriteClass = CrystalWispSprite.Green.class;
				break;
			case 2:
				spriteClass = CrystalWispSprite.Red.class;
				break;
		}
	}

	@Override
	public boolean[] modifyPassable(boolean[] passable) {
		for (int i = 0; i < Dungeon.level.length(); i++){
			passable[i] = passable[i] || Dungeon.level.map[i] == Terrain.MINE_CRYSTAL;
		}
		return passable;
	}

	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 5, 10 );
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 18;
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 5);
	}

	@Override
	protected boolean canAttackEnemy(Character enemy ) {
		return super.canAttackEnemy(enemy)
				|| new Ballistica(position, enemy.position, Ballistica.MAGIC_BOLT).collisionPos == enemy.position;
	}

	protected boolean attackCharacter(Character targetCharacter) {

		if (Dungeon.level.adjacent(position, targetCharacter.position)
				|| new Ballistica(position, targetCharacter.position, Ballistica.MAGIC_BOLT).collisionPos != targetCharacter.position) {

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

	//used so resistances can differentiate between melee and magical attacks
	public static class LightBeam {}

	private void zap() {
		spendTimeAdjusted( 1f );

		Invisibility.dispel(this);
		Character enemy = this.enemy;
		if (ActionHit.isTargetHitByAttack( this, enemy, true )) {

			int dmg = Random.NormalIntRange( 5, 10 );
			enemy.receiveDamageFromSource( dmg, new LightBeam() );

			if (!ActionHealth.isAlive(enemy) && enemy == Dungeon.hero) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "beam_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  ActionDefense.getDefenseVerb(enemy) );
		}
	}

	public void onZapComplete() {
		zap();
		DungeonTurnsHandler.nextActorToPlay(this);();
	}

	public static final String SPRITE = "sprite";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPRITE, spriteClass);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spriteClass = bundle.getClass(SPRITE);
	}
}
