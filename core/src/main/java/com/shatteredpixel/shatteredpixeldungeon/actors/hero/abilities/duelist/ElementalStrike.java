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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ActorLoop;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Annoying;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Dazzling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Displacing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Explosive;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Friendly;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Polarized;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blooming;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Corrupting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Unstable;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vampiric;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ElementalStrike extends ArmorAbility {

	//TODO a few duplicates here (curse duplicates are fine)
	private static final HashMap<Class<?extends Weapon.Enchantment>, Integer> effectTypes = new HashMap<>();
	static {
		effectTypes.put(Blazing.class,      MagicMissile.FIRE_CONE);
		effectTypes.put(Chilling.class,     MagicMissile.FROST_CONE);
		effectTypes.put(Kinetic.class,      MagicMissile.FORCE_CONE);
		effectTypes.put(Shocking.class,     MagicMissile.SPARK_CONE);
		effectTypes.put(Blocking.class,     MagicMissile.WARD_CONE);
		effectTypes.put(Blooming.class,     MagicMissile.FOLIAGE_CONE);
		effectTypes.put(Elastic.class,      MagicMissile.FORCE_CONE);
		effectTypes.put(Lucky.class,        MagicMissile.RAINBOW_CONE);
		effectTypes.put(Projecting.class,   MagicMissile.PURPLE_CONE);
		effectTypes.put(Unstable.class,     MagicMissile.RAINBOW_CONE);
		effectTypes.put(Corrupting.class,   MagicMissile.SHADOW_CONE);
		effectTypes.put(Grim.class,         MagicMissile.SHADOW_CONE);
		effectTypes.put(Vampiric.class,     MagicMissile.BLOOD_CONE);

		effectTypes.put(Annoying.class,     MagicMissile.SHADOW_CONE);
		effectTypes.put(Displacing.class,   MagicMissile.SHADOW_CONE);
		effectTypes.put(Dazzling.class,     MagicMissile.SHADOW_CONE);
		effectTypes.put(Explosive.class,    MagicMissile.SHADOW_CONE);
		effectTypes.put(Sacrificial.class,  MagicMissile.SHADOW_CONE);
		effectTypes.put(Wayward.class,      MagicMissile.SHADOW_CONE);
		effectTypes.put(Polarized.class,    MagicMissile.SHADOW_CONE);
		effectTypes.put(Friendly.class,     MagicMissile.SHADOW_CONE);

		effectTypes.put(null,               MagicMissile.MAGIC_MISS_CONE);
	}

	{
		baseChargeUse = 25;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos(Character user, int dst) {
		return dst;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		armor.charge -= chargeUse(hero);
		Item.updateQuickslot();

		Ballistica aim = new Ballistica(hero.position, target, Ballistica.WONT_STOP);

		int maxDist = 4 + hero.pointsInTalent(Talent.ELEMENTAL_REACH);
		int dist = Math.min(aim.dist, maxDist);

		ConeAOE cone = new ConeAOE(aim,
				dist,
				65 + 10*hero.pointsInTalent(Talent.ELEMENTAL_REACH),
				Ballistica.STOP_SOLID | Ballistica.STOP_TARGET);

		KindOfWeapon w = hero.belongings.weapon();
		Weapon.Enchantment enchantment = null;
		if (w instanceof MeleeWeapon) {
			enchantment = ((MeleeWeapon) w).enchantment;
		}
		Class<?extends Weapon.Enchantment> enchCls = null;
		if (enchantment != null){
			enchCls = enchantment.getClass();
		}

		//cast to cells at the tip, rather than all cells, better performance.
		for (Ballistica ray : cone.outerRays){
			((MagicMissile)hero.sprite.parent.recycle( MagicMissile.class )).reset(
					effectTypes.get(enchCls),
					hero.sprite,
					ray.path.get(ray.dist),
					null
			);
		}

		Weapon.Enchantment finalEnchantment = enchantment;
		hero.sprite.attack(target, new Callback() {
			@Override
			public void call() {

				Character enemy = Actor.getCharacterOnPosition(target);

				if (enemy != null) {
					if (hero.isCharmedBy(enemy)) {
						enemy = null;
					} else if (enemy.alignment == hero.alignment) {
						enemy = null;
					} else if (!hero.canAttack(enemy)) {
						enemy = null;
					}
				}

				preAttackEffect(cone, hero, finalEnchantment);

				if (enemy != null){
					AttackIndicator.target(enemy);
					oldEnemyPos = enemy.position;
					if (hero.attack(enemy, 1, 0, Character.INFINITE_ACCURACY)) {
						Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
					}
				}

				perCellEffect(cone, finalEnchantment);

				perCharEffect(cone, hero, enemy, finalEnchantment);

				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
			}
		});

		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
		hero.busy();

	}

	//effects that trigger before the attack
	private void preAttackEffect(ConeAOE cone, Hero hero, Weapon.Enchantment ench){

		int targetsHit = 0;
		for (Character ch : Actor.getCharacters()){
			if (ch.alignment == Character.Alignment.ENEMY && cone.cells.contains(ch.position)){
				targetsHit++;
			}
		}

		if (hero.hasTalent(Talent.DIRECTED_POWER)){
			float enchBoost = 0.30f * targetsHit * hero.pointsInTalent(Talent.DIRECTED_POWER);
			Buff.affect(hero, DirectedPowerTracker.class, 0f).enchBoost = enchBoost;
		}

		float powerMulti = 1f + 0.30f*Dungeon.hero.pointsInTalent(Talent.STRIKING_FORCE);

		//*** Kinetic ***
		if (ench instanceof Kinetic){
			if (hero.getBuff(Kinetic.ConservedDamage.class) != null) {
				storedKineticDamage = hero.getBuff(Kinetic.ConservedDamage.class).damageBonus();
			}

		//*** Blocking ***
		} else if (ench instanceof Blocking){
			if (targetsHit > 0){
				int shield = Math.round(Math.round(6f*targetsHit*powerMulti));
				Buff.affect(hero, Barrier.class).setShield(Math.round(6f*targetsHit*powerMulti));
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
			}

		//*** Vampiric ***
		} else if (ench instanceof Vampiric){
			if (targetsHit > 0){
				int heal = Math.round(2.5f*targetsHit*powerMulti);
				heal = Math.min( heal, hero.healthMax - hero.healthPoints);
				if (heal > 0){
					hero.healthPoints += heal;
					hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( heal ), FloatingText.HEALING );
				}
			}

		//*** Sacrificial ***
		} else if (ench instanceof Sacrificial){
			Buff.affect(hero, Bleeding.class).set(10 * powerMulti);
		}

	}

	public static class DirectedPowerTracker extends FlavourBuff{
		public float enchBoost = 0f;
	}

	public static class ElementalStrikeLuckyTracker extends Buff{};

	private int storedKineticDamage = 0;

	public static class ElementalStrikeFurrowCounter extends CounterBuff{{revivePersists = true;}};

	//effects that affect the cells of the environment themselves
	private void perCellEffect(ConeAOE cone, Weapon.Enchantment ench){

		int targetsHit = 0;
		for (Character ch : Actor.getCharacters()){
			if (ch.alignment == Character.Alignment.ENEMY && cone.cells.contains(ch.position)){
				targetsHit++;
			}
		}

		float powerMulti = 1f + 0.30f*Dungeon.hero.pointsInTalent(Talent.STRIKING_FORCE);

		//*** Blazing ***
		if (ench instanceof Blazing){
			for (int cell : cone.cells) {
				GameScene.add(ActorLoop.seed(cell, Math.round(8 * powerMulti), Fire.class));
			}

		//*** Chilling ***
		} else if (ench instanceof Chilling){
			for (int cell : cone.cells) {
				GameScene.add(ActorLoop.seed(cell, Math.round(8 * powerMulti), Freezing.class));
			}

		//*** Shocking ***
		} else if (ench instanceof Shocking){
			for (int cell : cone.cells) {
				GameScene.add(ActorLoop.seed(cell, Math.round(8 * powerMulti), Electricity.class));
			}

		//*** Blooming ***
		} else if (ench instanceof Blooming){
			ArrayList<Integer> cells = new ArrayList<>(cone.cells);
			Random.shuffle(cells);
			int grassToPlace = Math.round(8*powerMulti);

			//start spawning furrowed grass if exp is not being gained
			// each hero level is worth 20 normal uses, but just 5 if no enemies are present
			// cap of 40/10 uses
			int highGrassType = Terrain.HIGH_GRASS;
			if (Buff.affect(Dungeon.hero, ElementalStrikeFurrowCounter.class).count() >= 40){
				highGrassType = Terrain.FURROWED_GRASS;
			} else {
				if (Dungeon.hero.visibleEnemies() == 0 && targetsHit == 0) {
					Buff.count(Dungeon.hero, ElementalStrikeFurrowCounter.class, 4f);
				} else {
					Buff.count(Dungeon.hero, ElementalStrikeFurrowCounter.class, 1f);
				}
			}

			for (int cell : cells) {
				int terr = Dungeon.level.map[cell];
				if (terr == Terrain.EMPTY || terr == Terrain.EMBERS || terr == Terrain.EMPTY_DECO ||
						terr == Terrain.GRASS) {
					if (grassToPlace > 0
							&& !Character.hasProperty(Actor.getCharacterOnPosition(cell), Character.Property.IMMOVABLE)
							&& Dungeon.level.plants.get(cell) == null){
						Level.set(cell, highGrassType);
						grassToPlace--;
					} else {
						Level.set(cell, Terrain.GRASS);
					}
					GameScene.updateMap( cell );
				}
			}
			Dungeon.observe();
		}
	}

	private int oldEnemyPos;

	//effects that affect the characters within the cone AOE
	private void perCharEffect(ConeAOE cone, Hero hero, Character primaryTarget, Weapon.Enchantment ench) {

		float powerMulti = 1f + 0.30f * Dungeon.hero.pointsInTalent(Talent.STRIKING_FORCE);

		ArrayList<Character> affected = new ArrayList<>();

		for (Character ch : Actor.getCharacters()) {
			if (ch.alignment != Character.Alignment.ALLY && cone.cells.contains(ch.position)) {
				affected.add(ch);
			}
		}

		//*** no enchantment ***
		if (ench == null) {
			for (Character ch : affected){
				ch.receiveDamageFromSource(Math.round(powerMulti*Random.NormalIntRange(6, 12)), ElementalStrike.this);
			}

		//*** Kinetic ***
		} else if (ench instanceof Kinetic){
			if (storedKineticDamage > 0) {
				for (Character ch : affected) {
					if (ch != primaryTarget) {
						ch.receiveDamageFromSource(Math.round(storedKineticDamage * 0.4f * powerMulti), ench);
					}
				}
				storedKineticDamage = 0;
			}
			//clear stored damage if there was no primary target
			if (primaryTarget == null && hero.getBuff(Kinetic.ConservedDamage.class) != null){
				hero.getBuff(Kinetic.ConservedDamage.class).detach();
			}

		//*** Blooming ***
		} else if (ench instanceof Blooming){
			for (Character ch : affected){
				Buff.affect(ch, Roots.class, Math.round(6f*powerMulti));
			}

		//*** Elastic ***
		} else if (ench instanceof Elastic){

			//sorts affected from furthest to closest
			Collections.sort(affected, new Comparator<Character>() {
				@Override
				public int compare(Character a, Character b) {
					return Dungeon.level.distance(hero.position, a.position) - Dungeon.level.distance(hero.position, b.position);
				}
			});

			for (Character ch : affected){
				if (ch == primaryTarget && oldEnemyPos != primaryTarget.position) continue;

				Ballistica aim = new Ballistica(hero.position, ch.position, Ballistica.WONT_STOP);
				int knockback = Math.round(5*powerMulti);
				WandOfBlastWave.throwChar(ch,
						new Ballistica(ch.position, aim.collisionPos, Ballistica.MAGIC_BOLT),
						knockback,
						true,
						true,
						ElementalStrike.this);
			}

		//*** Lucky ***
		} else if (ench instanceof Lucky){
			for (Character ch : affected){
				if (ch.alignment == Character.Alignment.ENEMY
						&& Random.Float() < 0.125f*powerMulti
						&& ch.getBuff(ElementalStrikeLuckyTracker.class) == null) {
					Dungeon.level.dropItemOnPosition(Lucky.genLoot(), ch.position).sprite.drop();
					Lucky.showFlare(ch.sprite);
					Buff.affect(ch, ElementalStrikeLuckyTracker.class);
				}
			}

		//*** Projecting ***
		} else if (ench instanceof Projecting){
			for (Character ch : affected){
				if (ch != primaryTarget) {
					ch.receiveDamageFromSource(Math.round(hero.getDamageRoll() * 0.3f * powerMulti), ench);
				}
			}

		//*** Unstable ***
		} else if (ench instanceof Unstable){
			KindOfWeapon w = hero.belongings.weapon();
			if (w instanceof Weapon) {
				for (Character ch : affected){
					if (ch != primaryTarget) {
						ench.proc((Weapon) w, hero, ch, w.damageRoll(hero));
					}
				}
			}

		//*** Corrupting ***
		} else if (ench instanceof Corrupting){
			for (Character ch : affected){
				if (ch != primaryTarget
						&& !ch.isImmuneToEffectType(Corruption.class)
						&& ch.getBuff(Corruption.class) == null
						&& ch instanceof Mob
						&& ch.isAlive()) {
					float hpMissing = 1f - (ch.healthPoints / (float)ch.healthMax);
					float chance = 0.05f + 0.2f*hpMissing; //5-25%
					if (Random.Float() < chance*powerMulti){
						Corruption.corruptionHeal(ch);
						AllyBuff.affectAndLoot((Mob) ch, hero, Corruption.class);
					}
				}
			}

		//*** Grim ***
		} else if (ench instanceof Grim){
			for (Character ch : affected){
				if (ch != primaryTarget) {
					float hpMissing = 1f - (ch.healthPoints / (float)ch.healthMax);
					float chance = 0.06f + 0.24f*hpMissing; //6-30%
					if (Random.Float() < chance*powerMulti){
						ch.receiveDamageFromSource( ch.healthPoints, Grim.class );
						ch.sprite.emitter().burst( ShadowParticle.UP, 5 );
					}
				}
			}

		//*** Annoying ***
		} else if (ench instanceof Annoying){
			for (Character ch : affected){
				if (Random.Float() < 0.2f*powerMulti){
					//TODO totally should add a bit of dialogue here
					Buff.affect(ch, Amok.class, 6f);
				}
			}

		//*** Displacing ***
		} else if (ench instanceof Displacing){
			for (Character ch : affected){
				if (Random.Float() < 0.5f*powerMulti){
					int oldpos = ch.position;
					if (ScrollOfTeleportation.teleportChar(ch)){
						if (Dungeon.level.heroFOV[oldpos]) {
							CellEmitter.get( oldpos ).start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
						}

						if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).HUNTING){
							((Mob) ch).state = ((Mob) ch).WANDERING;
						}
					}
				}
			}

		//*** Dazzling ***
		} else if (ench instanceof Dazzling){
			for (Character ch : affected){
				if (Random.Float() < 0.5f*powerMulti){
					Buff.affect(ch, Blindness.class, 6f);
				}
			}

		//*** Explosive ***
		} else if (ench instanceof Explosive){
			if (Random.Float() < 0.5f*powerMulti){
				Character exploding = Random.element(affected);
				if (exploding != null) new Bomb.ConjuredBomb().explode(exploding.position);
			}

		//*** Sacrificial ***
		} else if (ench instanceof Sacrificial){
			for (Character ch : affected){
				Buff.affect(ch, Bleeding.class).set(12f*powerMulti);
			}

		//*** Wayward ***
		} else if (ench instanceof Wayward){
			for (Character ch : affected){
				if (Random.Float() < 0.5f*powerMulti){
					Buff.affect(ch, Hex.class, 6f);
				}
			}

		//*** Polarized ***
		} else if (ench instanceof Polarized){
			for (Character ch : affected){
				if (Random.Float() < 0.5f*powerMulti){
					ch.receiveDamageFromSource(Random.NormalIntRange(24, 36), ElementalStrike.this);
				}
			}

		//*** Friendly ***
		} else if (ench instanceof Friendly){
			for (Character ch : affected){
				if (Random.Float() < 0.5f*powerMulti){
					Buff.affect(ch, Charm.class, 6f).object = hero.getId();
				}
			}
		}

	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		if (Game.scene() instanceof GameScene){
			KindOfWeapon w = Dungeon.hero.belongings.weapon();
			if (w instanceof MeleeWeapon && ((MeleeWeapon) w).enchantment != null){
				desc += "\n\n" + Messages.get(((MeleeWeapon) w).enchantment, "elestrike_desc");
			} else {
				desc += "\n\n" + Messages.get(this, "generic_desc");
			}
		} else {
			desc += "\n\n" + Messages.get(this, "generic_desc");
		}
		desc += "\n\n" + Messages.get(this, "cost", (int)baseChargeUse);
		return desc;
	}

	@Override
	public int icon() {
		return HeroIcon.ELEMENTAL_STRIKE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.ELEMENTAL_REACH, Talent.STRIKING_FORCE, Talent.DIRECTED_POWER, Talent.HEROIC_ENERGY};
	}

}
