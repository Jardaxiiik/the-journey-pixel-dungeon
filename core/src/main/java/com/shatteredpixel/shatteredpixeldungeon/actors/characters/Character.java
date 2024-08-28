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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LifeLink;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Speed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ShockingDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollRockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.traits.traitEffects.TraitEffect;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class Character extends Actor {
	
	public int position = 0;
	
	public CharSprite sprite;
	
	public int healthMax;
	public int healthPoints;
	
	protected float baseSpeed	= 1;
	protected PathFinder.Path path;

	public int paralysed	    = 0;
	public boolean rooted		= false;
	public boolean flying		= false;
	public int invisible		= 0;
	
	//these are relative to the hero
	public enum Alignment{
		ENEMY,
		NEUTRAL,
		ALLY
	}
	public Alignment alignment;
	
	public int viewDistance	= 8;
	
	public boolean[] fieldOfView = null;

	protected static ArrayList<TraitEffect> attackTraitEffects;

	private void PerkSetup(ArrayList<TraitEffect> perks) {
		this.attackTraitEffects = perks;
	}
	public ArrayList<TraitEffect> getAttackTraitEffects() {
		return attackTraitEffects;
	}
	
	private LinkedHashSet<Buff> buffs = new LinkedHashSet<>();
	
	@Override
	protected boolean playGameTurn() {
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		//throw any items that are on top of an immovable char
		if (getProperties().contains(Property.IMMOVABLE)){
			throwItems();
		}
		return false;
	}

	protected void throwItems(){
		Heap heap = Dungeon.level.heaps.get(position);
		if (heap != null && heap.type == Heap.Type.HEAP
				&& !(heap.peek() instanceof Tengu.BombAbility.BombItem)
				&& !(heap.peek() instanceof Tengu.ShockerAbility.ShockerItem)) {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.OFFSETS_NEIGHBOURS8){
				if (Dungeon.level.passable[position +n]){
					candidates.add(position +n);
				}
			}
			if (!candidates.isEmpty()){
				Dungeon.level.dropItemOnPosition( heap.pickUp(), Random.element(candidates) ).sprite.drop(position);
			}
		}
	}

	public String getName(){
		return Messages.get(this, "name");
	}

	public boolean canInteract(Character c){
		if (Dungeon.level.adjacent(position, c.position)){
			return true;
		} else if (c instanceof Hero
				&& alignment == Alignment.ALLY
				&& !hasProperty(this, Property.IMMOVABLE)
				&& Dungeon.level.distance(position, c.position) <= 2*Dungeon.hero.pointsInTalent(Talent.ALLY_WARP)){
			return true;
		} else {
			return false;
		}
	}
	
	//swaps places by default
	public boolean interact(Character c){

		//don't allow char to swap onto hazard unless they're flying
		//you can swap onto a hazard though, as you're not the one instigating the swap
		if (!Dungeon.level.passable[position] && !c.flying){
			return true;
		}

		//can't swap into a space without room
		if (getProperties().contains(Property.LARGE) && !Dungeon.level.openSpace[c.position]
			|| c.getProperties().contains(Property.LARGE) && !Dungeon.level.openSpace[position]){
			return true;
		}

		//we do a little raw position shuffling here so that the characters are never
		// on the same cell when logic such as occupyCell() is triggered
		int oldPos = position;
		int newPos = c.position;

		//can't swap or ally warp if either char is immovable
		if (hasProperty(this, Property.IMMOVABLE) || hasProperty(c, Property.IMMOVABLE)){
			return true;
		}

		//warp instantly with allies in this case
		if (c == Dungeon.hero && Dungeon.hero.hasTalent(Talent.ALLY_WARP)){
			PathFinder.buildDistanceMap(c.position, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
			if (PathFinder.distance[position] == Integer.MAX_VALUE){
				return true;
			}
			position = newPos;
			c.position = oldPos;
			ScrollOfTeleportation.appear(this, newPos);
			ScrollOfTeleportation.appear(c, oldPos);
			Dungeon.observe();
			GameScene.updateFog();
			return true;
		}

		//can't swap places if one char has restricted movement
		if (rooted || c.rooted || getBuff(Vertigo.class) != null || c.getBuff(Vertigo.class) != null){
			return true;
		}

		c.position = oldPos;
		moveSprite( oldPos, newPos );
		moveToPosition( newPos );

		c.position = newPos;
		c.sprite.move( newPos, oldPos );
		c.moveToPosition( oldPos );
		
		c.spendTimeAdjusted( 1 / c.getSpeed() );

		if (c == Dungeon.hero){
			if (Dungeon.hero.subClass == HeroSubClass.FREERUNNER){
				Buff.affect(Dungeon.hero, Momentum.class).gainStack();
			}

			Dungeon.hero.busy();
		}
		
		return true;
	}
	
	protected boolean moveSprite( int from, int to ) {
		
		if (sprite.isVisible() && sprite.parent != null && (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.turnTo(from, to);
			sprite.place( to );
			return true;
		}
	}

	public void playHitSound(float pitch ){
		Sample.INSTANCE.play(Assets.Sounds.HIT, 1, pitch);
	}

	public boolean blockSound( float pitch ) {
		return false;
	}
	
	protected static final String POS       = "pos";
	protected static final String TAG_HP    = "HP";
	protected static final String TAG_HT    = "HT";
	protected static final String TAG_SHLD  = "SHLD";
	protected static final String BUFFS	    = "buffs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		bundle.put( POS, position);
		bundle.put( TAG_HP, healthPoints);
		bundle.put( TAG_HT, healthMax);
		bundle.put( BUFFS, buffs );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		position = bundle.getInt( POS );
		healthPoints = bundle.getInt( TAG_HP );
		healthMax = bundle.getInt( TAG_HT );
		
		for (Bundlable b : bundle.getCollection( BUFFS )) {
			if (b != null) {
				((Buff)b).attachTo( this );
			}
		}
	}

	final public boolean attack( Character enemy ){
		return attack(enemy, 1f, 0f, 1f);
	}
	
	public boolean attack(Character enemy, float dmgMulti, float dmgBonus, float accMulti ) {

		if (enemy == null) return false;
		
		boolean visibleFight = Dungeon.level.heroFOV[position] || Dungeon.level.heroFOV[enemy.position];

		if (enemy.isInvulnerableToEffectType(getClass())) {

			if (visibleFight) {
				enemy.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "invulnerable") );

				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
			}

			return false;

		} else if (isTargetHitByAttack( this, enemy, accMulti, false )) {
			
			int armorPoints = Math.round(enemy.getArmorPointsRolled() * AscensionChallenge.statModifier(enemy));
			
			if (this instanceof Hero){
				Hero h = (Hero)this;
				if (h.belongings.attackingWeapon() instanceof MissileWeapon
						&& h.subClass == HeroSubClass.SNIPER
						&& !Dungeon.level.adjacent(h.position, enemy.position)){
					armorPoints = 0;
				}

				if (h.getBuff(MonkEnergy.MonkAbility.UnarmedAbilityTracker.class) != null){
					armorPoints = 0;
				} else if (h.subClass == HeroSubClass.MONK) {
					//3 turns with standard attack delay
					Buff.prolong(h, MonkEnergy.MonkAbility.JustHitTracker.class, 4f);
				}
			}

			//we use a float here briefly so that we don't have to constantly round while
			// potentially applying various multiplier effects
			float dmg;
			Preparation prep = getBuff(Preparation.class);
			if (prep != null){
				dmg = prep.damageRoll(this);
				if (this == Dungeon.hero && Dungeon.hero.hasTalent(Talent.BOUNTY_HUNTER)) {
					Buff.affect(Dungeon.hero, Talent.BountyHunterTracker.class, 0.0f);
				}
			} else {
				dmg = getDamageRoll();
			}

			dmg = Math.round(dmg*dmgMulti);

			Berserk berserk = getBuff(Berserk.class);
			if (berserk != null) dmg = berserk.damageFactor(dmg);

			if (getBuff( Fury.class ) != null) {
				dmg *= 1.5f;
			}

			for (ChampionEnemy buff : getBuffs(ChampionEnemy.class)){
				dmg *= buff.meleeDamageFactor();
			}

			dmg *= AscensionChallenge.statModifier(this);

			//flat damage bonus is applied after positive multipliers, but before negative ones
			dmg += dmgBonus;

			//friendly endure
			Endure.EndureTracker endure = getBuff(Endure.EndureTracker.class);
			if (endure != null) dmg = endure.damageFactor(dmg);

			//enemy endure
			endure = enemy.getBuff(Endure.EndureTracker.class);
			if (endure != null){
				dmg = endure.adjustDamageTaken(dmg);
			}

			if (enemy.getBuff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}

			if (enemy.getBuff(MonkEnergy.MonkAbility.Meditate.MeditateResistance.class) != null){
				dmg *= 0.2f;
			}

			if ( getBuff(Weakness.class) != null ){
				dmg *= 0.67f;
			}
			
			int finalDamage = enemy.getDamageReceivedFromEnemyReducedByDefense( this, Math.round(dmg) );
			//do not trigger on-hit logic if defenseProc returned a negative value
			if (finalDamage >= 0) {
				finalDamage = Math.max(finalDamage - armorPoints, 0);

				if (enemy.getBuff(Viscosity.ViscosityTracker.class) != null) {
					finalDamage = enemy.getBuff(Viscosity.ViscosityTracker.class).deferDamage(finalDamage);
					enemy.getBuff(Viscosity.ViscosityTracker.class).detach();
				}

				//vulnerable specifically applies after armor reductions
				if (enemy.getBuff(Vulnerable.class) != null) {
					finalDamage *= 1.33f;
				}

				finalDamage = attackProc(enemy, finalDamage);
			}
			if (visibleFight) {
				if (finalDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
					playHitSound(Random.Float(0.87f, 1.15f));
				}
			}

			// If the enemy is already dead, interrupt the attack.
			// This matters as defence procs can sometimes inflict self-damage, such as armor glyphs.
			if (!enemy.isAlive()){
				return true;
			}

			enemy.receiveDamageFromSource( finalDamage, this );

			if (getBuff(FireImbue.class) != null)  getBuff(FireImbue.class).proc(enemy);
			if (getBuff(FrostImbue.class) != null) getBuff(FrostImbue.class).proc(enemy);

			if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
				enemy.healthPoints = 0;
				if (!enemy.isAlive()) {
					enemy.die(this);
				} else {
					//helps with triggering any on-damage effects that need to activate
					enemy.receiveDamageFromSource(-1, this);
					DeathMark.processFearTheReaper(enemy);
				}
				if (enemy.sprite != null) {
					enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
				}
			}

			Talent.CombinedLethalityTriggerTracker combinedLethality = getBuff(Talent.CombinedLethalityTriggerTracker.class);
			if (combinedLethality != null){
				if ( enemy.isAlive() && enemy.alignment != alignment && !Character.hasProperty(enemy, Property.BOSS)
						&& !Character.hasProperty(enemy, Property.MINIBOSS) && this instanceof Hero &&
						(enemy.healthPoints /(float)enemy.healthMax) <= 0.4f*((Hero)this).pointsInTalent(Talent.COMBINED_LETHALITY)/3f) {
					enemy.healthPoints = 0;
					if (!enemy.isAlive()) {
						enemy.die(this);
					} else {
						//helps with triggering any on-damage effects that need to activate
						enemy.receiveDamageFromSource(-1, this);
						DeathMark.processFearTheReaper(enemy);
					}
					if (enemy.sprite != null) {
						enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Talent.CombinedLethalityTriggerTracker.class, "executed"));
					}
				}
				combinedLethality.detach();
			}

			if (enemy.sprite != null) {
				enemy.sprite.bloodBurstA(sprite.center(), finalDamage);
				enemy.sprite.flash();
			}

			if (!enemy.isAlive() && visibleFight) {
				if (enemy == Dungeon.hero) {
					
					if (this == Dungeon.hero) {
						return true;
					}

					if (this instanceof WandOfLivingEarth.EarthGuardian
							|| this instanceof MirrorImage || this instanceof PrismaticImage){
						Badges.validateDeathFromFriendlyMagic();
					}
					Dungeon.fail( this );
					GLog.n( Messages.capitalize(Messages.get(Character.class, "kill", getName())) );
					
				} else if (this == Dungeon.hero) {
					GLog.i( Messages.capitalize(Messages.get(Character.class, "defeat", enemy.getName())) );
				}
			}
			
			return true;
			
		} else {

			enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.getDefenseVerb() );
			if (visibleFight) {
				//TODO enemy.defenseSound? currently miss plays for monks/crab even when they parry
				Sample.INSTANCE.play(Assets.Sounds.MISS);
			}
			
			return false;
			
		}
	}

	public static int INFINITE_ACCURACY = 1_000_000;
	public static int INFINITE_EVASION = 1_000_000;

	final public static boolean isTargetHitByAttack(Character attacker, Character defender, boolean magic ) {
		return isTargetHitByAttack(attacker, defender, magic ? 2f : 1f, magic);
	}

	public static boolean isTargetHitByAttack(Character attacker, Character defender, float accMulti, boolean magic ) {
		float acuStat = attacker.getAccuracyAgainstTarget( defender );
		float defStat = defender.getEvasionAgainstAttacker( attacker );

		if (defender instanceof Hero && ((Hero) defender).isHeroPlannedActionInterruptable){
			((Hero) defender).interruptHeroPlannedAction();
		}

		//invisible chars always hit (for the hero this is surprise attacking)
		if (attacker.invisible > 0 && attacker.canDoSurpriseAttack()){
			acuStat = INFINITE_ACCURACY;
		}

		if (defender.getBuff(MonkEnergy.MonkAbility.Focus.FocusBuff.class) != null && !magic){
			defStat = INFINITE_EVASION;
			defender.getBuff(MonkEnergy.MonkAbility.Focus.FocusBuff.class).detach();
			Buff.affect(defender, MonkEnergy.MonkAbility.Focus.FocusActivation.class, 0);
		}

		//if accuracy or evasion are large enough, treat them as infinite.
		//note that infinite evasion beats infinite accuracy
		if (defStat >= INFINITE_EVASION){
			return false;
		} else if (acuStat >= INFINITE_ACCURACY){
			return true;
		}

		float acuRoll = Random.Float( acuStat );
		if (attacker.getBuff(Bless.class) != null) acuRoll *= 1.25f;
		if (attacker.getBuff(  Hex.class) != null) acuRoll *= 0.8f;
		if (attacker.getBuff( Daze.class) != null) acuRoll *= 0.5f;
		for (ChampionEnemy buff : attacker.getBuffs(ChampionEnemy.class)){
			acuRoll *= buff.evasionAndAccuracyFactor();
		}
		acuRoll *= AscensionChallenge.statModifier(attacker);
		
		float defRoll = Random.Float( defStat );
		if (defender.getBuff(Bless.class) != null) defRoll *= 1.25f;
		if (defender.getBuff(  Hex.class) != null) defRoll *= 0.8f;
		if (defender.getBuff( Daze.class) != null) defRoll *= 0.5f;
		for (ChampionEnemy buff : defender.getBuffs(ChampionEnemy.class)){
			defRoll *= buff.evasionAndAccuracyFactor();
		}
		defRoll *= AscensionChallenge.statModifier(defender);
		
		return (acuRoll * accMulti) >= defRoll;
	}
	
	public int getAccuracyAgainstTarget(Character target ) {
		return 0;
	}
	
	public int getEvasionAgainstAttacker(Character enemy ) {
		return 0;
	}
	
	public String getDefenseVerb() {
		return Messages.get(this, "def_verb");
	}
	
	public int getArmorPointsRolled() {
		int armorPoints = 0;

		armorPoints += Random.NormalIntRange( 0 , Barkskin.currentLevel(this) );

		return armorPoints;
	}
	
	public int getDamageRoll() {
		return 1;
	}
	
	//TODO it would be nice to have a pre-armor and post-armor proc.
	// atm attack is always post-armor and defence is already pre-armor
	
	public int attackProc(Character enemy, int damage ) {
		for (ChampionEnemy buff : getBuffs(ChampionEnemy.class)){
			buff.onAttackProc( enemy );
		}
		return damage;
	}
	
	public int getDamageReceivedFromEnemyReducedByDefense(Character enemy, int damage ) {

		Earthroot.Armor armor = getBuff( Earthroot.Armor.class );
		if (armor != null) {
			damage = armor.getDamageReducedByEarthroot( damage );
		}

		return damage;
	}
	
	public float getSpeed() {
		float speed = baseSpeed;
		if ( getBuff( Cripple.class ) != null ) speed /= 2f;
		if ( getBuff( Stamina.class ) != null) speed *= 1.5f;
		if ( getBuff( Adrenaline.class ) != null) speed *= 2f;
		if ( getBuff( Haste.class ) != null) speed *= 3f;
		if ( getBuff( Dread.class ) != null) speed *= 2f;
		return speed;
	}

	//currently only used by invisible chars, or by the hero
	public boolean canDoSurpriseAttack(){
		return true;
	}
	
	//used so that buffs(Shieldbuff.class) isn't called every time unnecessarily
	private int cachedShield = 0;
	public boolean needsShieldUpdate = true;
	
	public int getShielding(){
		if (!needsShieldUpdate){
			return cachedShield;
		}
		
		cachedShield = 0;
		for (ShieldBuff s : getBuffs(ShieldBuff.class)){
			cachedShield += s.shielding();
		}
		needsShieldUpdate = false;
		return cachedShield;
	}
	
	public void receiveDamageFromSource(int dmg, Object sourceOfDamage ) {
		
		if (!isAlive() || dmg < 0) {
			return;
		}

		if(isInvulnerableToEffectType(sourceOfDamage.getClass())){
			sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
			return;
		}

		for (ChampionEnemy buff : getBuffs(ChampionEnemy.class)){
			dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
		}

		if (!(sourceOfDamage instanceof LifeLink) && getBuff(LifeLink.class) != null){
			HashSet<LifeLink> links = getBuffs(LifeLink.class);
			for (LifeLink link : links.toArray(new LifeLink[0])){
				if (Actor.getById(link.object) == null){
					links.remove(link);
					link.detach();
				}
			}
			dmg = (int)Math.ceil(dmg / (float)(links.size()+1));
			for (LifeLink link : links){
				Character ch = (Character)Actor.getById(link.object);
				if (ch != null) {
					ch.receiveDamageFromSource(dmg, link);
					if (!ch.isAlive()) {
						link.detach();
					}
				}
			}
		}

		Terror t = getBuff(Terror.class);
		if (t != null){
			t.recover();
		}
		Dread d = getBuff(Dread.class);
		if (d != null){
			d.recover();
		}
		Charm c = getBuff(Charm.class);
		if (c != null){
			c.recover(sourceOfDamage);
		}
		if (this.getBuff(Frost.class) != null){
			Buff.detach( this, Frost.class );
		}
		if (this.getBuff(MagicalSleep.class) != null){
			Buff.detach(this, MagicalSleep.class);
		}
		if (this.getBuff(Doom.class) != null && !isImmuneToEffectType(Doom.class)){
			dmg *= 1.67f;
		}
		if (alignment != Alignment.ALLY && this.getBuff(DeathMark.DeathMarkTracker.class) != null){
			dmg *= 1.25f;
		}
		
		Class<?> srcClass = sourceOfDamage.getClass();
		if (isImmuneToEffectType( srcClass )) {
			dmg = 0;
		} else {
			dmg = Math.round( dmg * getResistanceMultiplierToEffectType( srcClass ));
		}
		
		//TODO improve this when I have proper damage source logic
		if (AntiMagic.RESISTS.contains(sourceOfDamage.getClass()) && getBuff(ArcaneArmor.class) != null){
			dmg -= Random.NormalIntRange(0, getBuff(ArcaneArmor.class).level());
			if (dmg < 0) dmg = 0;
		}

		if (getBuff(Sickle.HarvestBleedTracker.class) != null){
			if (isImmuneToEffectType(Bleeding.class)){
				sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(Messages.get(this, "immune")));
				getBuff(Sickle.HarvestBleedTracker.class).detach();
				return;
			}

			Bleeding b = getBuff(Bleeding.class);
			if (b == null){
				b = new Bleeding();
			}
			b.announced = false;
			b.set(dmg* getBuff(Sickle.HarvestBleedTracker.class).bleedFactor, Sickle.HarvestBleedTracker.class);
			b.attachTo(this);
			sprite.showStatus(CharSprite.WARNING, Messages.titleCase(b.name()) + " " + (int)b.level());
			getBuff(Sickle.HarvestBleedTracker.class).detach();
			return;
		}
		
		if (getBuff( Paralysis.class ) != null) {
			getBuff( Paralysis.class ).processDamage(dmg);
		}

		int shielded = dmg;
		//FIXME: when I add proper damage properties, should add an IGNORES_SHIELDS property to use here.
		if (!(sourceOfDamage instanceof Hunger)){
			for (ShieldBuff s : getBuffs(ShieldBuff.class)){
				dmg = s.absorbDamage(dmg);
				if (dmg == 0) break;
			}
		}
		shielded -= dmg;
		healthPoints -= dmg;

		if (healthPoints > 0 && getBuff(Grim.GrimTracker.class) != null){

			float finalChance = getBuff(Grim.GrimTracker.class).maxChance;
			finalChance *= (float)Math.pow( ((healthMax - healthPoints) / (float) healthMax), 2);

			if (Random.Float() < finalChance) {
				int extraDmg = Math.round(healthPoints * getResistanceMultiplierToEffectType(Grim.class));
				dmg += extraDmg;
				healthPoints -= extraDmg;

				sprite.emitter().burst( ShadowParticle.UP, 5 );
				if (!isAlive() && getBuff(Grim.GrimTracker.class).qualifiesForBadge){
					Badges.validateGrimWeapon();
				}
			}
		}

		if (healthPoints < 0 && sourceOfDamage instanceof Character && alignment == Alignment.ENEMY){
			if (((Character) sourceOfDamage).getBuff(Kinetic.KineticTracker.class) != null){
				int dmgToAdd = -healthPoints;
				dmgToAdd -= ((Character) sourceOfDamage).getBuff(Kinetic.KineticTracker.class).conservedDamage;
				dmgToAdd = Math.round(dmgToAdd * Weapon.Enchantment.genericProcChanceMultiplier((Character) sourceOfDamage));
				if (dmgToAdd > 0) {
					Buff.affect((Character) sourceOfDamage, Kinetic.ConservedDamage.class).setBonus(dmgToAdd);
				}
				((Character) sourceOfDamage).getBuff(Kinetic.KineticTracker.class).detach();
			}
		}
		
		if (sprite != null) {
			//defaults to normal damage icon if no other ones apply
			int                                                         icon = FloatingText.PHYS_DMG;
			if (NO_ARMOR_PHYSICAL_SOURCES.contains(sourceOfDamage.getClass()))     icon = FloatingText.PHYS_DMG_NO_BLOCK;
			if (AntiMagic.RESISTS.contains(sourceOfDamage.getClass()))             icon = FloatingText.MAGIC_DMG;
			if (sourceOfDamage instanceof Pickaxe)                                 icon = FloatingText.PICK_DMG;

			//special case for sniper when using ranged attacks
			if (sourceOfDamage == Dungeon.hero
					&& Dungeon.hero.subClass == HeroSubClass.SNIPER
					&& !Dungeon.level.adjacent(Dungeon.hero.position, position)
					&& Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon){
				icon = FloatingText.PHYS_DMG_NO_BLOCK;
			}

			if (sourceOfDamage instanceof Hunger)                                  icon = FloatingText.HUNGER;
			if (sourceOfDamage instanceof Burning)                                 icon = FloatingText.BURNING;
			if (sourceOfDamage instanceof Chill || sourceOfDamage instanceof Frost)        icon = FloatingText.FROST;
			if (sourceOfDamage instanceof GeyserTrap || sourceOfDamage instanceof StormCloud) icon = FloatingText.WATER;
			if (sourceOfDamage instanceof Burning)                                 icon = FloatingText.BURNING;
			if (sourceOfDamage instanceof Electricity)                             icon = FloatingText.SHOCKING;
			if (sourceOfDamage instanceof Bleeding)                                icon = FloatingText.BLEEDING;
			if (sourceOfDamage instanceof ToxicGas)                                icon = FloatingText.TOXIC;
			if (sourceOfDamage instanceof Corrosion)                               icon = FloatingText.CORROSION;
			if (sourceOfDamage instanceof Poison)                                  icon = FloatingText.POISON;
			if (sourceOfDamage instanceof Ooze)                                    icon = FloatingText.OOZE;
			if (sourceOfDamage instanceof Viscosity.DeferedDamage)                 icon = FloatingText.DEFERRED;
			if (sourceOfDamage instanceof Corruption)                              icon = FloatingText.CORRUPTION;
			if (sourceOfDamage instanceof AscensionChallenge)                      icon = FloatingText.AMULET;

			sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(dmg + shielded), icon);
		}

		if (healthPoints < 0) healthPoints = 0;

		if (!isAlive()) {
			die( sourceOfDamage );
		} else if (healthPoints == 0 && getBuff(DeathMark.DeathMarkTracker.class) != null){
			DeathMark.processFearTheReaper(this);
		}
	}

	//these are misc. sources of physical damage which do not apply armor, they get a different icon
	private static HashSet<Class> NO_ARMOR_PHYSICAL_SOURCES = new HashSet<>();
	{
		NO_ARMOR_PHYSICAL_SOURCES.add(CrystalSpire.SpireSpike.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.Boulder.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.GnollRockFall.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollRockfallTrap.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.KingDamager.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.Summoning.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Chasm.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(WandOfBlastWave.Knockback.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Heap.class); //damage from wraiths attempting to spawn from heaps
		NO_ARMOR_PHYSICAL_SOURCES.add(Necromancer.SummoningBlockDamage.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DriedRose.GhostHero.NoRoseDamage.class);
	}
	
	public void destroy() {
		healthPoints = 0;
		Actor.removeActor( this );

		for (Character ch : Actor.getCharacters().toArray(new Character[0])){
			if (ch.getBuff(Charm.class) != null && ch.getBuff(Charm.class).object == getId()){
				ch.getBuff(Charm.class).detach();
			}
			if (ch.getBuff(Dread.class) != null && ch.getBuff(Dread.class).object == getId()){
				ch.getBuff(Dread.class).detach();
			}
			if (ch.getBuff(Terror.class) != null && ch.getBuff(Terror.class).object == getId()){
				ch.getBuff(Terror.class).detach();
			}
			if (ch.getBuff(SnipersMark.class) != null && ch.getBuff(SnipersMark.class).object == getId()){
				ch.getBuff(SnipersMark.class).detach();
			}
			if (ch.getBuff(Talent.FollowupStrikeTracker.class) != null
					&& ch.getBuff(Talent.FollowupStrikeTracker.class).object == getId()){
				ch.getBuff(Talent.FollowupStrikeTracker.class).detach();
			}
			if (ch.getBuff(Talent.DeadlyFollowupTracker.class) != null
					&& ch.getBuff(Talent.DeadlyFollowupTracker.class).object == getId()){
				ch.getBuff(Talent.DeadlyFollowupTracker.class).detach();
			}
		}
	}
	
	public void die( Object source ) {
		destroy();
		if (source != Chasm.class) sprite.die();
	}

	//we cache this info to prevent having to call buff(...) in isAlive.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications
	public boolean deathMarked = false;
	
	public boolean isAlive() {
		return healthPoints > 0 || deathMarked;
	}

	public boolean isActive() {
		return isAlive();
	}

	@Override
	public void spendTime(float time) {
		TimekeepersHourglass.timeFreeze freeze = getBuff(TimekeepersHourglass.timeFreeze.class);
		if (freeze != null) {
			freeze.processTime(time);
			return;
		}

		Swiftthistle.TimeBubble bubble = getBuff(Swiftthistle.TimeBubble.class);
		if (bubble != null){
			bubble.processTime(time);
			return;
		}

		super.spendTime(time);
	}

	@Override
	protected void spendTimeAdjusted(float time ) {

		float timeScale = 1f;
		if (getBuff( Slow.class ) != null) {
			timeScale *= 0.5f;
			//slowed and chilled do not stack
		} else if (getBuff( Chill.class ) != null) {
			timeScale *= getBuff( Chill.class ).speedFactor();
		}
		if (getBuff( Speed.class ) != null) {
			timeScale *= 2.0f;
		}
		
		super.spendTimeAdjusted( time / timeScale );
	}
	
	public synchronized LinkedHashSet<Buff> getBuffs() {
		return new LinkedHashSet<>(buffs);
	}
	
	@SuppressWarnings("unchecked")
	//returns all buffs assignable from the given buff class
	public synchronized <T extends Buff> HashSet<T> getBuffs(Class<T> c ) {
		HashSet<T> filtered = new HashSet<>();
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				filtered.add( (T)b );
			}
		}
		return filtered;
	}

	@SuppressWarnings("unchecked")
	//returns an instance of the specific buff class, if it exists. Not just assignable
	public synchronized  <T extends Buff> T getBuff(Class<T> c ) {
		for (Buff b : buffs) {
			if (b.getClass() == c) {
				return (T)b;
			}
		}
		return null;
	}

	public synchronized boolean isCharmedBy( Character ch ) {
		int chID = ch.getId();
		for (Buff b : buffs) {
			if (b instanceof Charm && ((Charm)b).object == chID) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean addBuff(Buff buff ) {

		if (getBuff(PotionOfCleansing.Cleanse.class) != null) { //cleansing buff
			if (buff.type == Buff.buffType.NEGATIVE
					&& !(buff instanceof AllyBuff)
					&& !(buff instanceof LostInventory)){
				return false;
			}
		}

		if (sprite != null && getBuff(Challenge.SpectatorFreeze.class) != null){
			return false; //can't add buffs while frozen and game is loaded
		}

		buffs.add( buff );
		if (Actor.getCharacters().contains(this)) Actor.addActor( buff );

		if (sprite != null && buff.announced) {
			switch (buff.type) {
				case POSITIVE:
					sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(buff.name()));
					break;
				case NEGATIVE:
					sprite.showStatus(CharSprite.WARNING, Messages.titleCase(buff.name()));
					break;
				case NEUTRAL:
				default:
					sprite.showStatus(CharSprite.NEUTRAL, Messages.titleCase(buff.name()));
					break;
			}
		}

		return true;

	}
	
	public synchronized boolean removeBuff(Buff buff ) {
		
		buffs.remove( buff );
		Actor.removeActor( buff );

		return true;
	}
	
	public synchronized void removeBuff(Class<? extends Buff> buffClass ) {
		for (Buff buff : getBuffs( buffClass )) {
			removeBuff( buff );
		}
	}
	
	@Override
	protected synchronized void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
			buff.detach();
		}
	}
	
	public synchronized void updateSpriteState() {
		for (Buff buff:buffs) {
			buff.fx( true );
		}
	}
	
	public float getStealth() {
		return 0;
	}

	public final void moveToPosition(int step ) {
		moveToPosition( step, true );
	}

	//travelling may be false when a character is moving instantaneously, such as via teleportation
	public void moveToPosition(int newPosition, boolean travelling ) {

		if (travelling && Dungeon.level.adjacent(newPosition, position) && getBuff( Vertigo.class ) != null) {
			sprite.interruptMotion();
			int newPos = position + PathFinder.OFFSETS_NEIGHBOURS8[Random.Int( 8 )];
			if (!(Dungeon.level.passable[newPos] || Dungeon.level.avoid[newPos])
					|| (getProperties().contains(Property.LARGE) && !Dungeon.level.openSpace[newPos])
					|| Actor.getCharacterOnPosition( newPos ) != null)
				return;
			else {
				sprite.move(position, newPos);
				newPosition = newPos;
			}
		}

		if (Dungeon.level.map[position] == Terrain.OPEN_DOOR) {
			Door.leave(position);
		}

		position = newPosition;
		
		if (this != Dungeon.hero) {
			sprite.visible = Dungeon.level.heroFOV[position];
		}
		
		Dungeon.level.occupyCell(this );
	}
	
	public int getDistanceToOtherCharacter(Character other ) {
		return Dungeon.level.distance(position, other.position);
	}

	public boolean[] modifyPassable( boolean[] passable){
		//do nothing by default, but some chars can pass over terrain that others can't
		return passable;
	}
	
	public void onMotionComplete() {
		//Does nothing by default
		//The main actor thread already accounts for motion,
		// so calling next() here isn't necessary (see Actor.process)
	}
	
	public void onAttackComplete() {
		next();
	}
	
	public void onOperateComplete() {
		next();
	}
	
	protected final HashSet<Class> resistances = new HashSet<>();
	
	//returns percent effectiveness after resistances
	//TODO currently resistances reduce effectiveness by a static 50%, and do not stack.
	public float getResistanceMultiplierToEffectType(Class effect ){
		HashSet<Class> resists = new HashSet<>(resistances);
		for (Property p : getProperties()){
			resists.addAll(p.getResistances());
		}
		for (Buff b : getBuffs()){
			resists.addAll(b.resistances());
		}
		
		float result = 1f;
		for (Class c : resists){
			if (c.isAssignableFrom(effect)){
				result *= 0.5f;
			}
		}
		return result * RingOfElements.resist(this, effect);
	}
	
	protected final HashSet<Class> immunities = new HashSet<>();
	
	public boolean isImmuneToEffectType(Class effect ){
		HashSet<Class> immunes = new HashSet<>(immunities);
		for (Property p : getProperties()){
			immunes.addAll(p.getImmunities());
		}
		for (Buff b : getBuffs()){
			immunes.addAll(b.immunities());
		}
		
		for (Class c : immunes){
			if (c.isAssignableFrom(effect)){
				return true;
			}
		}
		return false;
	}

	//similar to isImmune, but only factors in damage.
	//Is used in AI decision-making
	public boolean isInvulnerableToEffectType(Class effect ){
		return getBuff(Challenge.SpectatorFreeze.class) != null;
	}

	protected HashSet<Property> properties = new HashSet<>();

	public HashSet<Property> getProperties() {
		HashSet<Property> props = new HashSet<>(properties);
		//TODO any more of these and we should make it a property of the buff, like with resistances/immunities
		if (getBuff(ChampionEnemy.Giant.class) != null) {
			props.add(Property.LARGE);
		}
		return props;
	}

	public enum Property{
		BOSS ( new HashSet<Class>( Arrays.asList(Grim.class, GrimTrap.class, ScrollOfRetribution.class, ScrollOfPsionicBlast.class)),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
		MINIBOSS ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
		BOSS_MINION,
		UNDEAD,
		DEMONIC,
		INORGANIC ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(Bleeding.class, ToxicGas.class, Poison.class) )),
		FIERY ( new HashSet<Class>( Arrays.asList(WandOfFireblast.class, Elemental.FireElemental.class)),
				new HashSet<Class>( Arrays.asList(Burning.class, Blazing.class))),
		ICY ( new HashSet<Class>( Arrays.asList(WandOfFrost.class, Elemental.FrostElemental.class)),
				new HashSet<Class>( Arrays.asList(Frost.class, Chill.class))),
		ACIDIC ( new HashSet<Class>( Arrays.asList(Corrosion.class)),
				new HashSet<Class>( Arrays.asList(Ooze.class))),
		ELECTRIC ( new HashSet<Class>( Arrays.asList(WandOfLightning.class, Shocking.class, Potential.class, Electricity.class, ShockingDart.class, Elemental.ShockElemental.class )),
				new HashSet<Class>()),
		LARGE,
		IMMOVABLE;
		
		private HashSet<Class> resistances;
		private HashSet<Class> immunities;
		
		Property(){
			this(new HashSet<Class>(), new HashSet<Class>());
		}
		
		Property( HashSet<Class> resistances, HashSet<Class> immunities){
			this.resistances = resistances;
			this.immunities = immunities;
		}
		
		public HashSet<Class> getResistances(){
			return new HashSet<>(resistances);
		}
		
		public HashSet<Class> getImmunities(){
			return new HashSet<>(immunities);
		}

	}

	public static boolean hasProperty(Character ch, Property p){
		return (ch != null && ch.getProperties().contains(p));
	}
}
