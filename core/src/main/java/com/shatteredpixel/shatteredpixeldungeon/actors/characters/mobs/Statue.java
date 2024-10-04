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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Statue extends Mob {
	
	{
		spriteClass = StatueSprite.class;

		EXP = 0;
		state = PASSIVE;
		
		properties.add(Property.INORGANIC);
	}
	
	protected Weapon weapon;

	public boolean levelGenStatue = true;
	
	public Statue() {
		super();
		
		healthPoints = healthMax = 15 + Dungeon.depth * 5;
		evasionSkill = 4 + Dungeon.depth;
	}

	public void createWeapon( boolean useDecks ){
		if (useDecks) {
			weapon = (MeleeWeapon) ItemGenerator.random(ItemGenerator.Category.WEAPON);
		} else {
			weapon = (MeleeWeapon) ItemGenerator.randomUsingDefaults(ItemGenerator.Category.WEAPON);
		}
		levelGenStatue = useDecks;
		weapon.cursed = false;
		weapon.enchant( Enchantment.random() );
	}
	
	private static final String WEAPON	= "weapon";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( WEAPON, weapon );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		weapon = (Weapon)bundle.get( WEAPON );
	}
	
	@Override
    public boolean playGameTurn() {
		if (levelGenStatue && Dungeon.level.visited[position]) {
			Notes.add( Notes.Landmark.STATUE );
		}
		return super.playGameTurn();
	}
	
	@Override
	public int getDamageRoll() {
		return weapon.damageRoll(this);
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return (int)((9 + Dungeon.depth) * weapon.accuracyFactor( this, target ));
	}
	
	@Override
	public float getAttackDelay() {
		return super.getAttackDelay()*weapon.delayFactor( this );
	}

	@Override
	protected boolean canAttackEnemy(Character enemy) {
		return super.canAttackEnemy(enemy) || weapon.canReach(this, enemy.position);
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, Dungeon.depth + weapon.defenseFactor(this));
	}
	
	@Override
	public boolean addBuff(Buff buff) {
		if (super.addBuff(buff)) {
			if (state == PASSIVE && buff.type == Buff.buffType.NEGATIVE) {
				state = HUNTING;
			}
			return true;
		}
		return false;
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {

		if (state == PASSIVE) {
			state = HUNTING;
		}
		
		super.receiveDamageFromSource( dmg, sourceOfDamage);
	}
	
	@Override
	public int attackProc_1(Character enemy, int damage ) {
		damage = ActionAttack.attackProc(this, enemy, damage );
		damage = weapon.proc( this, enemy, damage );
		if (!enemy.isAlive() && enemy == Dungeon.hero){
			Dungeon.fail(this);
			GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", getName())) );
		}
		return damage;
	}
	
	@Override
	public void travelToPosition(int cell ) {
		// Do nothing
	}
	
	@Override
	public void die( Object source) {
		weapon.identify(false);
		Dungeon.level.dropItemOnPosition( weapon, position).sprite.drop();
		super.die(source);
	}
	
	@Override
	public void destroy() {
		if (levelGenStatue) {
			Notes.remove( Notes.Landmark.STATUE );
		}
		super.destroy();
	}

	@Override
	public float getSpawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		state = PASSIVE;
		return true;
	}

	@Override
	public String getDescription() {
		return Messages.get(this, "desc", weapon.name());
	}
	
	{
		resistances.add(Grim.class);
	}

	public static Statue random(){
		return random( true );
	}

	public static Statue random( boolean useDecks ){
		Statue statue;
		if (Random.Int(10) == 0){
			statue = new ArmoredStatue();
		} else {
			statue = new Statue();
		}
		statue.createWeapon(useDecks);
		return statue;
	}
	
}
