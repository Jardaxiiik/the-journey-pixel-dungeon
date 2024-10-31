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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Honeypot extends Item {
	
	public static final String AC_SHATTER	= "SHATTER";
	
	{
		image = ItemSpriteSheet.HONEYPOT;

		defaultAction = AC_THROW;
		usesTargeting = true;

		stackable = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_SHATTER );
		return actions;
	}
	
	@Override
	public void execute( final Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_SHATTER )) {
			
			hero.sprite.zap( hero.position);
			
			detach( hero.belongings.backpack );

			Item item = shatter( hero, hero.position);
			if (!item.collect()){
				Dungeon.level.dropItemOnPosition(item, hero.position);
				if (item instanceof ShatteredPot){
					((ShatteredPot) item).dropPot(hero, hero.position);
				}
			}

			DungeonTurnsHandler.nextActorToPlayHero(hero);();

		}
	}
	
	@Override
	protected void onThrow( int cell ) {
		if (Dungeon.level.pit[cell]) {
			super.onThrow( cell );
		} else {
			Dungeon.level.dropItemOnPosition(shatter( null, cell ), cell);
		}
	}
	
	public Item shatter(Character owner, int pos ) {
		
		if (Dungeon.level.heroFOV[pos]) {
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
			Splash.at( pos, 0xffd500, 5 );
		}
		
		int newPos = pos;
		if (DungeonCharactersHandler.getCharacterOnPosition( pos ) != null) {
			ArrayList<Integer> candidates = new ArrayList<>();
			
			for (int n : PathFinder.OFFSETS_NEIGHBOURS4) {
				int c = pos + n;
				if (!Dungeon.level.solid[c] && DungeonCharactersHandler.getCharacterOnPosition( c ) == null) {
					candidates.add( c );
				}
			}
	
			newPos = candidates.size() > 0 ? Random.element( candidates ) : -1;
		}
		
		if (newPos != -1) {
			Bee bee = new Bee();
			bee.spawn( Dungeon.scalingDepth() );
			bee.setPotInfo( pos, owner );
			bee.healthPoints = bee.healthMax;
			bee.position = newPos;
			
			GameScene.addMob( bee );
			if (newPos != pos) DungeonActorsHandler.addActor( new Pushing( bee, pos, newPos ) );

			bee.sprite.alpha( 0 );
			bee.sprite.parent.add( new AlphaTweener( bee.sprite, 1, 0.15f ) );
			
			Sample.INSTANCE.play( Assets.Sounds.BEE );
			return new ShatteredPot();
		} else {
			return this;
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int value() {
		return 30 * quantity;
	}

	//The bee's broken 'home', all this item does is let its bee know where it is, and who owns it (if anyone).
	public static class ShatteredPot extends Item {

		{
			image = ItemSpriteSheet.SHATTPOT;
			stackable = true;
		}

		@Override
		public boolean doPickUp(Hero hero, int pos) {
			if ( super.doPickUp(hero, pos) ){
				pickupPot( hero );
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void doDrop(Hero hero) {
			super.doDrop(hero);
			dropPot(hero, hero.position);
		}

		@Override
		protected void onThrow(int cell) {
			super.onThrow(cell);
			dropPot(curUser, cell);
		}

		public void pickupPot(Character holder){
			for (Bee bee : findBees(holder.position)){
				updateBee(bee, -1, holder);
			}
		}
		
		public void dropPot(Character holder, int dropPos ){
			for (Bee bee : findBees(holder)){
				updateBee(bee, dropPos, null);
			}
		}

		public void movePot( int oldpos, int movePos){
			for (Bee bee : findBees(oldpos)){
				updateBee(bee, movePos, null);
			}
		}

		public void destroyPot( int potPos ){
			for (Bee bee : findBees(potPos)){
				updateBee(bee, -1, null);
			}
		}

		private void updateBee( Bee bee, int cell, Character holder ){
			if (bee != null && bee.alignment == CharacterAlignment.ENEMY)
				bee.setPotInfo( cell, holder );
		}
		
		//returns up to quantity bees which match the current pot Pos
		private ArrayList<Bee> findBees( int potPos ){
			ArrayList<Bee> bees = new ArrayList<>();
			for (Character c : DungeonCharactersHandler.getCharacters()){
				if (c instanceof Bee && ((Bee) c).potPos() == potPos){
					bees.add((Bee) c);
					if (bees.size() >= quantity) {
						break;
					}
				}
			}
			
			return bees;
		}
		
		//returns up to quantity bees which match the current pot holder
		private ArrayList<Bee> findBees( Character potHolder ){
			ArrayList<Bee> bees = new ArrayList<>();
			for (Character c : DungeonCharactersHandler.getCharacters()){
				if (c instanceof Bee && ((Bee) c).potHolderID() == potHolder.getId()){
					bees.add((Bee) c);
					if (bees.size() >= quantity) {
						break;
					}
				}
			}
			
			return bees;
		}

		@Override
		public boolean isUpgradable() {
			return false;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}
		
		@Override
		public int value() {
			return 5 * quantity;
		}
	}
}
