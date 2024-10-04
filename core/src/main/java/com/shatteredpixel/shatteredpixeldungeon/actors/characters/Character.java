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
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonTurnsHandler;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ShockingDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollRockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class Character extends Actor {

	// Movement
	// Health
	// VIEW
	// Items / Behaviors


	public int position = 0; // get / set
	public int healthMax;
	public int healthPoints;
	private int cachedShield = 0;
	protected float baseSpeed = 1;
	public int paralysed	    = 0;
	public int invisible		= 0;

	public boolean isRooted = false;
	public boolean isFlying = false;
	public boolean deathMarked = false;
	public boolean needsShieldUpdate = true;

	public HeroAlignment alignment;

	public CharSprite sprite; // move sprite + addBuff

	protected PathFinder.Path path;

	public int viewDistance	= 8;
	public boolean[] fieldOfView = null;

	private LinkedHashSet<Buff> buffs = new LinkedHashSet<>(); // they just are
	private LinkedHashSet<Buff> activeBuffs = new LinkedHashSet<>(); // they actively do something


	protected final HashSet<Class> resistances = new HashSet<>();
	protected final HashSet<Class> immunities = new HashSet<>();
	protected HashSet<Property> properties = new HashSet<>();

	public void setPosition_1(int position) {
		this.position = position;
	}

	public float getBaseSpeed() {
		return baseSpeed;
	}

	public void setFieldOfView(boolean[] fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

	public boolean[] getFieldOfView() {
		return fieldOfView;
	}

	public float getStealth() {
		return 0;
	}


	public String getName(){
		return Messages.get(this, "name");
	}

	public boolean moveSprite(int from, int to) {
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

	public boolean willBlockSound(float pitch ) {
		return false;
	}

	public String getDefenseVerb() {
		return Messages.get(this, "def_verb");
	}
	//currently only used by invisible chars, or by the hero

	public boolean canDoSurpriseAttack(){
		return true;
	}
	//used so that buffs(Shieldbuff.class) isn't called every time unnecessarily

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
	//these are misc. sources of physical damage which do not apply armor, they get a different icon

	public static HashSet<Class> NO_ARMOR_PHYSICAL_SOURCES = new HashSet<>();
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
		DungeonCharactersHandler.removeCharacter(this);

		for (Character ch : DungeonCharactersHandler.getCharacters().toArray(new Character[0])){
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
	//we cache this info to prevent having to call buff(...) in isAlive.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications

	public boolean isAlive() {
		return healthPoints > 0 || deathMarked;
	}

	public boolean isActive() {
		return isAlive();
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
		if (DungeonCharactersHandler.getCharacters().contains(this)) DungeonActorsHandler.addActor( buff );

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
		DungeonActorsHandler.removeActor( buff );

		return true;
	}

	public synchronized void removeBuff(Class<? extends Buff> buffClass ) {
		for (Buff buff : getBuffs( buffClass )) {
			removeBuff( buff );
		}
	}

	@Override
    public synchronized void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
			buff.detach();
		}
	}

	public synchronized void updateSpriteState() {
		for (Buff buff:buffs) {
			buff.fx( true );
		}
	}

	public void onMotionComplete() {
		//Does nothing by default
		//The main actor thread already accounts for motion,
		// so calling next() here isn't necessary (see Actor.process)
	}

	public void onAttackComplete() {
		DungeonTurnsHandler.nextActorToPlay(this);
	}

	public void onOperateComplete() {
		DungeonTurnsHandler.nextActorToPlay(this);
	}
	// RESISTANCES

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

	// PROPERTIES
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

	// BUNDLES
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
}
