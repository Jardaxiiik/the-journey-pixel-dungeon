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

import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GnollGuard extends Mob {

	{
		spriteClass = GnollGuardSprite.class;

		healthPoints = healthMax = 35;
		evasionSkill = 15;

		EXP = 7;
		maxLvl = -2;

		loot = Spear.class;
		lootChance = 0.1f;

		WANDERING = new Wandering();
	}

	private int sapperID = -1;

	public void linkSapper( GnollSapper sapper){
		this.sapperID = sapper.getId();
		if (sprite instanceof GnollGuardSprite){
			((GnollGuardSprite) sprite).setupArmor();
		}
	}

	public boolean hasSapper(){
		return sapperID != -1
				&& DungeonActors.getById(sapperID) instanceof GnollSapper
				&& ((GnollSapper) DungeonActors.getById(sapperID)).ActionHealth.isAlive();
	}

	public void loseSapper(){
		if (sapperID != -1){
			sapperID = -1;
			if (sprite instanceof GnollGuardSprite){
				((GnollGuardSprite) sprite).loseArmor();
			}
		}
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if (hasSapper()) dmg /= 4;
		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}

	@Override
	public int getDamageRoll() {
		if (enemy != null && !Dungeon.level.adjacent(position, enemy.position)){
			return Random.NormalIntRange( 16, 22 );
		} else {
			return Random.NormalIntRange( 6, 12 );
		}
	}

	@Override
	public int attackProc_1(Character enemy, int damage) {
		int dmg = ActionAttack.attackProc(this,enemy, damage);
		if (enemy == Dungeon.hero && !Dungeon.level.adjacent(position, enemy.position) && dmg > 12){
			GLog.n(Messages.get(this, "spear_warn"));
		}
		return dmg;
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 20;
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 6);
	}

	@Override
	protected boolean canAttackEnemy(Character enemy ) {
		//cannot 'curve' spear hits like the hero, requires fairly open space to hit at a distance
		return Dungeon.level.distance(enemy.position, position) <= 2
				&& new Ballistica(position, enemy.position, Ballistica.PROJECTILE).collisionPos == enemy.position
				&& new Ballistica( enemy.position, position, Ballistica.PROJECTILE).collisionPos == position;
	}

	@Override
	public String getDescription() {
		if (hasSapper()){
			return super.getDescription() + "\n\n" + Messages.get(this, "desc_armor");
		} else {
			return super.getDescription();
		}
	}

	private static final String SAPPER_ID = "sapper_id";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SAPPER_ID, sapperID);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		sapperID = bundle.getInt(SAPPER_ID);
	}

	public class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination() {
			if (hasSapper()){
				return ((GnollSapper) DungeonActors.getById(sapperID)).position;
			} else {
				return super.randomDestination();
			}
		}
	}

}
