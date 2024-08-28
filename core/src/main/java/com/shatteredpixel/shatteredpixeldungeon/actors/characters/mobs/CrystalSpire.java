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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrystalSpireSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CrystalSpire extends Mob {

	{
		//this translates to roughly 33/27/23/20/18/16 pickaxe hits at +0/1/2/3/4/5
		healthPoints = healthMax = 300;
		spriteClass = CrystalSpireSprite.class;

		EXP = 20;

		//acts after other mobs, which makes baiting crystal guardians more consistent
		actPriority = MOB_PRIO-1;

		state = PASSIVE;

		alignment = Alignment.NEUTRAL;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.BOSS);
		properties.add(Property.INORGANIC);
	}

	private float abilityCooldown;
	private static final int ABILITY_CD = 15;

	private ArrayList<ArrayList<Integer>> targetedCells = new ArrayList<>();

	@Override
	protected boolean playGameTurn() {
		//char logic
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		throwItems();

		sprite.hideAlert();
		sprite.hideLost();

		//mob logic
		enemy = Dungeon.hero;

		//crystal can still track an invisible hero
		enemySeen = enemy.isAlive() && fieldOfView[enemy.position];
		//end of char/mob logic

		if (!targetedCells.isEmpty()){

			ArrayList<Integer> cellsToAttack = targetedCells.remove(0);

			for (int i : cellsToAttack){

				Character ch = getCharacterOnPosition(i);
				if (ch instanceof CrystalSpire){
					continue; //don't spawn crystals on itself
				}

				Level.set(i, Terrain.MINE_CRYSTAL);
				GameScene.updateMap(i);

				Splash.at(i, 0xFFFFFF, 5);
			}

			for (int i : cellsToAttack){
				Character ch = getCharacterOnPosition(i);

				if (ch != null && !(ch instanceof CrystalWisp || ch instanceof CrystalSpire)){
					int dmg = Random.NormalIntRange(6, 15);

					//guardians are hit harder by the attack
					if (ch instanceof CrystalGuardian) {
						dmg += 12; //18-27 damage
						Buff.prolong(ch, Cripple.class, 30f);
					}
					ch.receiveDamageFromSource(dmg, new SpireSpike());

					int movePos = i;
					//crystal guardians get knocked away from the hero, others get knocked away from the spire
					if (ch instanceof CrystalGuardian){
						for (int j : PathFinder.OFFSETS_NEIGHBOURS8){
							if (!Dungeon.level.solid[i+j] && getCharacterOnPosition(i+j) == null &&
									Dungeon.level.trueDistance(i+j, Dungeon.hero.position) > Dungeon.level.trueDistance(movePos, Dungeon.hero.position)){
								movePos = i+j;
							}
						}
					} else if (!hasProperty(ch, Property.IMMOVABLE)) {
						for (int j : PathFinder.OFFSETS_NEIGHBOURS8){
							if (!Dungeon.level.solid[i+j] && getCharacterOnPosition(i+j) == null &&
									Dungeon.level.trueDistance(i+j, position) > Dungeon.level.trueDistance(movePos, position)){
								movePos = i+j;
							}
						}
					}

					if (ch.isAlive()){
						if (movePos != i){
							addActor(new Pushing(ch, i, movePos));
							ch.position = movePos;
							Dungeon.level.occupyCell(ch);
						}
					} else if (ch == Dungeon.hero){
						GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", getName())) );
						Dungeon.fail(this);
					}
				}
			}

			PixelScene.shake( 1, 0.7f );
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );

			if (!targetedCells.isEmpty()){
				for (int i : targetedCells.get(0)){
					sprite.parent.add(new TargetedCell(i, 0xFF0000));
				}
			}

		}

		if (hits < 3 || !enemySeen){
			spendTimeAdjusted(TICK);
			return true;
		} else {

			if (abilityCooldown <= 0){

				if (Random.Int(2) == 0) {
					diamondAOEAttack();
				} else {
					lineAttack();
				}

				for (int i : targetedCells.get(0)){
					sprite.parent.add(new TargetedCell(i, 0xFF0000));
				}

				abilityCooldown += ABILITY_CD;

				spendTimeAdjusted(GameMath.gate(TICK, (int)Math.ceil(Dungeon.hero.cooldown()), 3*TICK));
				Dungeon.hero.interruptHeroPlannedAction();
			} else {
				abilityCooldown -= 1;
				spendTimeAdjusted(TICK);
			}

		}

		return true;
	}

	public static class SpireSpike{}

	private void diamondAOEAttack(){
		targetedCells.clear();

		ArrayList<Integer> aoeCells = new ArrayList<>();
		aoeCells.add(Dungeon.hero.position);
		aoeCells.addAll(spreadDiamondAOE(aoeCells));
		targetedCells.add(new ArrayList<>(aoeCells));

		if (healthPoints < 2* healthMax /3f){
			aoeCells.addAll(spreadDiamondAOE(aoeCells));
			targetedCells.add(new ArrayList<>(aoeCells));
			if (healthPoints < healthMax /3f) {
				aoeCells.addAll(spreadDiamondAOE(aoeCells));
				targetedCells.add(aoeCells);
			}
		}
	}

	private ArrayList<Integer> spreadDiamondAOE(ArrayList<Integer> currentCells){
		ArrayList<Integer> spreadCells = new ArrayList<>();
		for (int i : currentCells){
			for (int j : PathFinder.OFFSETS_NEIGHBOURS4){
				if ((!Dungeon.level.solid[i+j] || Dungeon.level.map[i+j] == Terrain.MINE_CRYSTAL)
						&& !spreadCells.contains(i+j) && !currentCells.contains(i+j)){
					spreadCells.add(i+j);
				}
			}
		}
		return spreadCells;
	}

	private void lineAttack(){
		targetedCells.clear();

		ArrayList<Integer> lineCells = new ArrayList<>();
		Ballistica aim = new Ballistica(position, Dungeon.hero.position, Ballistica.WONT_STOP);
		for (int i : aim.subPath(1, 7)){
			if (!Dungeon.level.solid[i] || Dungeon.level.map[i] == Terrain.MINE_CRYSTAL){
				lineCells.add(i);
			} else {
				break;
			}
		}

		targetedCells.add(new ArrayList<>(lineCells));
		if (healthPoints < 2* healthMax /3f){
			lineCells.addAll(spreadDiamondAOE(lineCells));
			targetedCells.add(new ArrayList<>(lineCells));
			if (healthPoints < healthMax /3f) {;
				lineCells.addAll(spreadDiamondAOE(lineCells));
				targetedCells.add(lineCells);
			}
		}
	}

	private ArrayList<Integer> spreadAOE(ArrayList<Integer> currentCells){
		ArrayList<Integer> spreadCells = new ArrayList<>();
		for (int i : currentCells){
			for (int j : PathFinder.OFFSETS_NEIGHBOURS8){
				if ((!Dungeon.level.solid[i+j] || Dungeon.level.map[i+j] == Terrain.MINE_CRYSTAL)
						&& !spreadCells.contains(i+j) && !currentCells.contains(i+j)){
					spreadCells.add(i+j);
				}
			}
		}
		return spreadCells;
	}

	@Override
	public void travelToPosition(int cell) {
		//do nothing
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {
		if (!(sourceOfDamage instanceof Pickaxe) ){
			dmg = 0;
		}
		super.receiveDamageFromSource(dmg, sourceOfDamage);
	}

	@Override
	public boolean isInvulnerableToEffectType(Class effect) {
		return super.isInvulnerableToEffectType(effect) || effect != Pickaxe.class;
	}

	@Override
	public boolean addBuff(Buff buff ) {
		return false; //immune to all buffs and debuffs
	}

	int hits = 0;

	@Override
	public boolean interact(Character c) {
		if (c == Dungeon.hero){
			final Pickaxe p = Dungeon.hero.belongings.getItem(Pickaxe.class);

			if (p == null){
				return true;
			}

			Dungeon.hero.sprite.attack(position, new Callback() {
				@Override
				public void call() {
					//does its own special damage calculation that's only influenced by pickaxe level and augment
					//we pretend the spire is the owner here so that properties like hero str or or other equipment do not factor in
					int dmg = p.damageRoll(CrystalSpire.this);

					receiveDamageFromSource(dmg, p);
					abilityCooldown -= dmg/10f;
					sprite.bloodBurstA(Dungeon.hero.sprite.center(), dmg);
					sprite.flash();

					BossHealthBar.bleed(healthPoints <= healthMax /3);

					if (isAlive()) {
						Sample.INSTANCE.play(Assets.Sounds.SHATTER, 1f, Random.Float(1.15f, 1.25f));
						((CrystalSpireSprite) sprite).updateIdle();
					} else {
						Sample.INSTANCE.play(Assets.Sounds.SHATTER);
						Sample.INSTANCE.playDelayed(Assets.Sounds.ROCKS, 0.1f);
						PixelScene.shake( 3, 0.7f );
						Blacksmith.Quest.beatBoss();

						if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
							fieldOfView = new boolean[Dungeon.level.length()];
							Dungeon.level.updateFieldOfView( CrystalSpire.this, fieldOfView );
						}

						for (int i = 0; i < Dungeon.level.length(); i++){
							if (fieldOfView[i] && Dungeon.level.map[i] == Terrain.MINE_CRYSTAL){
								Level.set(i, Terrain.EMPTY);
								GameScene.updateMap(i);
								Splash.at(i, 0xFFFFFF, 5);
							}
						}

						for (Character ch : getCharacters()){
							if (fieldOfView[ch.position]) {
								if (ch instanceof CrystalGuardian) {
									ch.receiveDamageFromSource(ch.healthMax, new SpireSpike());
								}
								if (ch instanceof CrystalWisp) {
									Buff.affect(ch, Blindness.class, 5f);
								}
							}
						}

					}

					hits++;

					if (hits == 1){
						GLog.w(Messages.get(CrystalSpire.class, "warning"));
						PixelScene.shake( 1, 0.7f );
						Sample.INSTANCE.play( Assets.Sounds.MINE );
					} else if (hits >= 3) {

						if (hits == 3){
							Sample.INSTANCE.play( Assets.Sounds.ROCKS );
							PixelScene.shake( 3, 0.7f );
							GLog.n(Messages.get(CrystalSpire.class, "alert"));
							BossHealthBar.assignBoss(CrystalSpire.this);

							abilityCooldown = 1; //dely first attack by 1 turn
						}

						boolean affectingGuardians = false;
						for (Character ch : getCharacters()) {
							if (ch instanceof CrystalWisp) {
								if (((CrystalWisp) ch).state != ((CrystalWisp)ch).HUNTING && ((CrystalWisp) ch).target != position) {
									((CrystalWisp) ch).travelToPosition(position);
								}
							} else if (ch instanceof CrystalGuardian) {
								if (((CrystalGuardian) ch).state != ((CrystalGuardian)ch).HUNTING && ((CrystalGuardian) ch).target != position) {
									affectingGuardians = true;
								}
							}
						}

						//build a pathfind route to the guardians
						// cripple close sleeping guardians to give more time
						// haste far awake guardians to punish waking them
						if (affectingGuardians){
							boolean[] passable = Dungeon.level.passable.clone();
							for (int i = 0; i < Dungeon.level.length(); i++){
								if (Dungeon.level.map[i] == Terrain.MINE_CRYSTAL){
									passable[i] = true;
								}
							}
							PathFinder.buildDistanceMap(position, passable);

							for (Character ch : getCharacters()) {
								if (ch instanceof CrystalGuardian){
									if (((CrystalGuardian) ch).state == ((CrystalGuardian) ch).SLEEPING) {

										((CrystalGuardian) ch).startHunting(Dungeon.hero);
										((CrystalGuardian) ch).travelToPosition(position);

										//delays sleeping guardians that happen to be near to the crystal
										if (PathFinder.distance[ch.position] < 20){
											Buff.affect(ch, Paralysis.class, 20-PathFinder.distance[ch.position]);
										}

									} else if (((CrystalGuardian) ch).state != ((CrystalGuardian) ch).HUNTING && ((CrystalGuardian) ch).target != position){
										((CrystalGuardian) ch).travelToPosition(position);
										if (((CrystalGuardian) ch).state != HUNTING) {
											((CrystalGuardian) ch).startHunting(Dungeon.hero);
										}

										//speeds up already woken guardians that aren't very close
										if (PathFinder.distance[ch.position] > 8){
											Buff.affect(ch, Haste.class, Math.round((PathFinder.distance[ch.position]-8)/2f));
										}
									}
								}
							}
						}
					}

					Invisibility.dispel(Dungeon.hero);
					Dungeon.hero.spendTimeAdjustedAndNext(p.delayFactor(CrystalSpire.this));
				}
			});
			return false;

		}
		return true;
	}

	public CrystalSpire(){
		super();
		switch (Random.Int(3)){
			case 0: default:
				spriteClass = CrystalSpireSprite.Blue.class;
				break;
			case 1:
				spriteClass = CrystalSpireSprite.Green.class;
				break;
			case 2:
				spriteClass = CrystalSpireSprite.Red.class;
				break;
		}
	}

	@Override
	public float getSpawningWeight() {
		return 0;
	}

	public static final String SPRITE = "sprite";
	public static final String HITS = "hits";

	public static final String ABILITY_COOLDOWN = "ability_cooldown";
	public static final String TARGETED_CELLS = "targeted_cells";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPRITE, spriteClass);
		bundle.put(HITS, hits);

		bundle.put(ABILITY_COOLDOWN, abilityCooldown);
		for (int i = 0; i < targetedCells.size(); i++){
			int[] bundleArr = new int[targetedCells.get(i).size()];
			for (int j = 0; j < targetedCells.get(i).size(); j++){
				bundleArr[j] = targetedCells.get(i).get(j);
			}
			bundle.put(TARGETED_CELLS+i, bundleArr);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spriteClass = bundle.getClass(SPRITE);
		hits = bundle.getInt(HITS);

		if (hits >= 3){
			BossHealthBar.assignBoss(this);
		}

		abilityCooldown = bundle.getFloat(ABILITY_COOLDOWN);
		targetedCells.clear();
		int i = 0;
		while (bundle.contains(TARGETED_CELLS+i)){
			ArrayList<Integer> targets = new ArrayList<>();
			for (int j : bundle.getIntArray(TARGETED_CELLS+i)){
				targets.add(j);
			}
			targetedCells.add(targets);
			i++;
		}
	}

	{
		immunities.add( Blindness.class );

		immunities.add( Paralysis.class );
		immunities.add( Amok.class );
		immunities.add( Sleep.class );
		immunities.add( Terror.class );
		immunities.add( Dread.class );
		immunities.add( Vertigo.class );
	}

}
