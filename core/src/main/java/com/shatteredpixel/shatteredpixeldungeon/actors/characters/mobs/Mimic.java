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

import static javax.swing.text.StyleConstants.Alignment;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionAppearance;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionBuffs;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.CharacterAlignment;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemGenerator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MimicSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Mimic extends Mob {
	
	private int level;
	
	{
		spriteClass = MimicSprite.class;

		properties.add(Property.DEMONIC);

		EXP = 0;
		
		//mimics are neutral when hidden
		alignment = Alignment.NEUTRAL;
		state = PASSIVE;
	}
	
	public ArrayList<Item> items;
	
	private static final String LEVEL	= "level";
	private static final String ITEMS	= "items";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (items != null) bundle.put( ITEMS, items );
		bundle.put( LEVEL, level );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		if (bundle.contains( ITEMS )) {
			items = new ArrayList<>((Collection<Item>) ((Collection<?>) bundle.getCollection(ITEMS)));
		}
		level = bundle.getInt( LEVEL );
		adjustStats(level);
		super.restoreFromBundle(bundle);
		if (state != PASSIVE && alignment == Alignment.NEUTRAL){
			alignment = Alignment.ENEMY;
		}
	}

	@Override
	public boolean addBuff(Buff buff) {
		if (ActionBuffs.addBuff(this,buff)) {
			if (buff.type == Buff.buffType.NEGATIVE && alignment == CharacterAlignment.NEUTRAL) {
				alignment = CharacterAlignment.ENEMY;
				stopHiding();
				if (sprite != null) sprite.idle();
			}
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		if (alignment == Alignment.NEUTRAL){
			return Messages.get(Heap.class, "chest");
		} else {
			return ActionAppearance.getName(this);
		}
	}

	@Override
	public String getDescription() {
		if (alignment == Alignment.NEUTRAL){
			return Messages.get(Heap.class, "chest_desc") + "\n\n" + Messages.get(this, "hidden_hint");
		} else {
			return super.getDescription();
		}
	}

	@Override
    public boolean playGameTurn() {
		if (alignment == Alignment.NEUTRAL && state != PASSIVE){
			alignment = Alignment.ENEMY;
			if (sprite != null) sprite.idle();
			if (Dungeon.level.heroFOV[position]) {
				GLog.w(Messages.get(this, "reveal") );
				CellEmitter.get(position).burst(Speck.factory(Speck.STAR), 10);
				Sample.INSTANCE.play(Assets.Sounds.MIMIC);
			}
		}
		return super.playGameTurn();
	}

	@Override
	public CharSprite sprite() {
		MimicSprite sprite = (MimicSprite) super.sprite();
		if (alignment == Alignment.NEUTRAL) sprite.hideMimic();
		return sprite;
	}

	@Override
	public boolean interact(Character c) {
		if (alignment != Alignment.NEUTRAL || c != Dungeon.hero){
			return super.interact(c);
		}
		stopHiding();

		Dungeon.hero.busy();
		Dungeon.hero.sprite.operate(position);
		if (Dungeon.hero.invisible <= 0
				&& Dungeon.hero.getBuff(Swiftthistle.TimeBubble.class) == null
				&& Dungeon.hero.getBuff(TimekeepersHourglass.timeFreeze.class) == null){
			return attackCharacter(Dungeon.hero);
		} else {
			sprite.idle();
			alignment = Alignment.ENEMY;
			Dungeon.hero.spendTimeAdjustedAndNext(1f);
			return true;
		}
	}

	@Override
	public void onAttackComplete() {
		super.onAttackComplete();
		if (alignment == CharacterAlignment.NEUTRAL){
			alignment = CharacterAlignment.ENEMY;
			Dungeon.hero.spendTimeAdjustedAndNext(1f);
		}
	}

	@Override
	public int getDamageReceivedFromEnemyReducedByDefense(Character enemy, int damage) {
		if (state == PASSIVE){
			alignment = Alignment.ENEMY;
			stopHiding();
		}
		return super.getDamageReceivedFromEnemyReducedByDefense(enemy, damage);
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if (state == PASSIVE){
			alignment = Alignment.ENEMY;
			stopHiding();
		}
		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}

	@Override
	public void die(Object source) {
		if (state == PASSIVE){
			alignment = Alignment.ENEMY;
			stopHiding();
		}
		super.die(source);
	}

	public void stopHiding(){
		state = HUNTING;
		if (sprite != null) sprite.idle();
		if (DungeonCharactersHandler.getCharacters().contains(this) && Dungeon.level.heroFOV[position]) {
			enemy = Dungeon.hero;
			target = Dungeon.hero.position;
			GLog.w(Messages.get(this, "reveal") );
			CellEmitter.get(position).burst(Speck.factory(Speck.STAR), 10);
			Sample.INSTANCE.play(Assets.Sounds.MIMIC);
		}
	}

	@Override
	public int getDamageRoll() {
		if (alignment == Alignment.NEUTRAL){
			return Random.NormalIntRange( 2 + 2*level, 2 + 2*level);
		} else {
			return Random.NormalIntRange( 1 + level, 2 + 2*level);
		}
	}

	@Override
	public int getArmorPointsRolled() {
		return super.getArmorPointsRolled() + Random.NormalIntRange(0, 1 + level/2);
	}

	@Override
	public void travelToPosition(int cell ) {
		// Do nothing
	}

	@Override
	public int getAccuracyAgainstTarget(Character target ) {
		if (target != null && alignment == Alignment.NEUTRAL && target.invisible <= 0){
			return INFINITE_ACCURACY;
		} else {
			return 6 + level;
		}
	}

	public void setLevel( int level ){
		this.level = level;
		adjustStats(level);
	}
	
	public void adjustStats( int level ) {
		healthPoints = healthMax = (1 + level) * 6;
		evasionSkill = 2 + level/2;
		
		enemySeen = true;
	}
	
	@Override
	public void dropLoot(){
		
		if (items != null) {
			for (Item item : items) {
				Dungeon.level.dropItemOnPosition( item, position).sprite.drop();
			}
			items = null;
		}
		super.dropLoot();
	}

	@Override
	public float getSpawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		if (state != PASSIVE) state = WANDERING;
		return true;
	}

	public static Mimic spawnAt( int pos, Item... items){
		return spawnAt(pos, Mimic.class, items);
	}

	public static Mimic spawnAt( int pos, Class mimicType, Item... items){
		return spawnAt(pos, mimicType, true, items);
	}

	public static Mimic spawnAt( int pos, boolean useDecks, Item... items){
		return spawnAt(pos, Mimic.class, useDecks, items);
	}

	public static Mimic spawnAt( int pos, Class mimicType, boolean useDecks, Item... items){
		Mimic m;
		if (mimicType == GoldenMimic.class){
			m = new GoldenMimic();
		} else if (mimicType == CrystalMimic.class) {
			m = new CrystalMimic();
		} else {
			m = new Mimic();
		}

		m.items = new ArrayList<>( Arrays.asList(items) );
		m.setLevel( Dungeon.depth );
		m.position = pos;

		//generate an extra reward for killing the mimic
		m.generatePrize(useDecks);

		return m;
	}

	protected void generatePrize( boolean useDecks ){
		Item reward = null;
		do {
			switch (Random.Int(5)) {
				case 0:
					reward = new Gold().random();
					break;
				case 1:
					reward = ItemGenerator.randomMissile(!useDecks);
					break;
				case 2:
					reward = ItemGenerator.randomArmor();
					break;
				case 3:
					reward = ItemGenerator.randomWeapon(!useDecks);
					break;
				case 4:
					reward = useDecks ? ItemGenerator.random(ItemGenerator.Category.RING) : ItemGenerator.randomUsingDefaults(ItemGenerator.Category.RING);
					break;
			}
		} while (reward == null || Challenges.isItemBlocked(reward));
		items.add(reward);
	}

}
