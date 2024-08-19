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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SuccubusSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Succubus extends Mob {

	private int blinkCooldown = 0;
	
	{
		spriteClass = SuccubusSprite.class;
		
		healthPoints = healthMax = 80;
		defenseSkill = 25;
		viewDistance = Light.DISTANCE;
		
		EXP = 12;
		maxLvl = 25;
		
		loot = ItemGenerator.Category.SCROLL;
		lootChance = 0.33f;

		properties.add(Property.DEMONIC);
	}
	
	@Override
	public int getDamageRoll() {
		return Random.NormalIntRange( 25, 30 );
	}
	
	@Override
	public int attackProc(Character enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		
		if (enemy.getBuff(Charm.class) != null ){
			int shield = (healthPoints - healthMax) + (5 + damage);
			if (shield > 0){
				healthPoints = healthMax;
				if (shield < 5){
					sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(5-shield), FloatingText.HEALING);
				}

				Buff.affect(this, Barrier.class).setShield(shield);
				sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
			} else {
				healthPoints += 5 + damage;
				sprite.showStatusWithIcon(CharSprite.POSITIVE, "5", FloatingText.HEALING);
			}
			if (Dungeon.level.heroFOV[position]) {
				Sample.INSTANCE.play( Assets.Sounds.CHARMS );
			}
		} else if (Random.Int( 3 ) == 0) {
			Charm c = Buff.affect( enemy, Charm.class, Charm.DURATION/2f );
			c.object = getId();
			c.ignoreNextHit = true; //so that the -5 duration from succubus hit is ignored
			if (Dungeon.level.heroFOV[enemy.position]) {
				enemy.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
				Sample.INSTANCE.play(Assets.Sounds.CHARMS);
			}
		}
		
		return damage;
	}
	
	@Override
	protected boolean moveCloserToTarget(int targetPosition) {
		if (fieldOfView[targetPosition] && Dungeon.level.distance(position, targetPosition) > 2 && blinkCooldown <= 0 && !rooted) {
			
			if (blink(targetPosition)) {
				spendTimeAdjusted(-1 / getSpeed());
				return true;
			} else {
				return false;
			}
			
		} else {

			blinkCooldown--;
			return super.moveCloserToTarget(targetPosition);
			
		}
	}
	
	private boolean blink( int target ) {
		
		Ballistica route = new Ballistica(position, target, Ballistica.PROJECTILE);
		int cell = route.collisionPos;

		//can't occupy the same cell as another char, so move back one.
		if (Actor.getCharacterOnPosition( cell ) != null && cell != this.position)
			cell = route.path.get(route.dist-1);

		if (Dungeon.level.avoid[ cell ] || (getProperties().contains(Property.LARGE) && !Dungeon.level.openSpace[cell])){
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.OFFSETS_NEIGHBOURS8) {
				cell = route.collisionPos + n;
				if (Dungeon.level.passable[cell]
						&& Actor.getCharacterOnPosition( cell ) == null
						&& (!getProperties().contains(Property.LARGE) || Dungeon.level.openSpace[cell])) {
					candidates.add( cell );
				}
			}
			if (candidates.size() > 0)
				cell = Random.element(candidates);
			else {
				blinkCooldown = Random.IntRange(4, 6);
				return false;
			}
		}
		
		ScrollOfTeleportation.appear( this, cell );

		blinkCooldown = Random.IntRange(4, 6);
		return true;
	}
	
	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		return 40;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 10);
	}

	@Override
	public Item getLootItem() {
		Class<?extends Scroll> loot;
		do{
			loot = (Class<? extends Scroll>) Random.oneOf(ItemGenerator.Category.SCROLL.classes);
		} while (loot == ScrollOfIdentify.class || loot == ScrollOfUpgrade.class);

		return Reflection.newInstance(loot);
	}

	{
		immunities.add( Charm.class );
	}

	private static final String BLINK_CD = "blink_cd";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BLINK_CD, blinkCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		blinkCooldown = bundle.getInt(BLINK_CD);
	}
}
