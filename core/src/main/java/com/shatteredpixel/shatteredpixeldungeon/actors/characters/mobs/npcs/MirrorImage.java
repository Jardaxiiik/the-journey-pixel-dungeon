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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actions.ActionHit;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MirrorSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class MirrorImage extends NPC {
	
	{
		spriteClass = MirrorSprite.class;
		
		healthPoints = healthMax = 1;
		evasionSkill = 1;
		
		alignment = Alignment.ALLY;
		state = HUNTING;
		
		//before other mobs
		actPriority = MOB_PRIO + 1;
	}
	
	private Hero hero;
	private int heroID;
	public int armTier;
	
	@Override
    public boolean playGameTurn() {
		
		if ( hero == null ){
			hero = (Hero)DungeonActors.getById(heroID);
			if ( hero == null ){
				die(null);
				sprite.killAndErase();
				return true;
			}
		}
		
		if (hero.tier() != armTier){
			armTier = hero.tier();
			((MirrorSprite)sprite).updateArmor( armTier );
		}
		
		return super.playGameTurn();
	}
	
	private static final String HEROID	= "hero_id";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( HEROID, heroID );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		heroID = bundle.getInt( HEROID );
	}
	
	public void duplicate( Hero hero ) {
		this.hero = hero;
		heroID = this.hero.getId();
		Buff.affect(this, MirrorInvis.class, Short.MAX_VALUE);
	}
	
	@Override
	public int getDamageRoll() {
		int damage;
		if (hero.belongings.weapon() != null){
			damage = hero.belongings.weapon().damageRoll(this);
		} else {
			damage = hero.getDamageRoll(); //handles ring of force
		}
		return (damage+1)/2; //half hero damage, rounded up
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		//same base attack skill as hero, benefits from accuracy ring and weapon
		int attackSkill = 9 + hero.lvl;
		attackSkill *= RingOfAccuracy.accuracyMultiplier(hero);
		if (hero.belongings.attackingWeapon() != null){
			attackSkill *= hero.belongings.attackingWeapon().accuracyFactor(this, target);
		}
		return attackSkill;
	}
	
	@Override
	public int getEvasionAgainstAttacker(Character enemy) {
		if (hero != null) {
			int baseEvasion = 4 + hero.lvl;
			int heroEvasion = (int)((4 + hero.lvl) * RingOfEvasion.evasionMultiplier( hero ));
			
			//if the hero has more/less evasion, 50% of it is applied
			//includes ring of evasion boost
			return ActionHit.getEvasionAgainstAttacker(this,enemy) * (baseEvasion + heroEvasion) / 2;
		} else {
			return 0;
		}
	}
	
	@Override
	public float getAttackDelay() {
		return hero.attackDelay(); //handles ring of furor
	}
	
	@Override
	protected boolean canAttackEnemy(Character enemy) {
		return super.canAttackEnemy(enemy) || (hero.belongings.weapon() != null && hero.belongings.weapon().canReach(this, enemy.position));
	}
	
	@Override
	public int getArmorPointsRolled() {
		int armorPoints = super.getArmorPointsRolled();
		if (hero != null && hero.belongings.weapon() != null){
			return armorPoints + Random.NormalIntRange(0, hero.belongings.weapon().defenseFactor(this)/2);
		} else {
			return armorPoints;
		}
	}
	
	@Override
	public int attackProc_1(Character enemy, int damage ) {
		damage = ActionAttack.attackProc(this, enemy, damage );
		
		MirrorInvis buff = getBuff(MirrorInvis.class);
		if (buff != null){
			buff.detach();
		}
		
		if (enemy instanceof Mob) {
			((Mob)enemy).startHunting( this );
		}
		if (hero.belongings.weapon() != null){
			damage = hero.belongings.weapon().proc( this, enemy, damage );
			if (!ActionHealth.isAlive(enemy) && enemy == Dungeon.hero){
				Dungeon.fail(this);
				GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", ActionAppearance.getName(this))) );
			}
			return damage;
		} else {
			return damage;
		}
	}
	
	@Override
	public CharSprite sprite() {
		CharSprite s = super.sprite();
		
		hero = (Hero)DungeonActors.getById(heroID);
		if (hero != null) {
			armTier = hero.tier();
		}
		((MirrorSprite)s).updateArmor( armTier );
		return s;
	}
	
	{
		immunities.add( ToxicGas.class );
		immunities.add( CorrosiveGas.class );
		immunities.add( Burning.class );
		immunities.add( AllyBuff.class );
	}
	
	public static class MirrorInvis extends Invisibility {
		
		{
			announced = false;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}
}