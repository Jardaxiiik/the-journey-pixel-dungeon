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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionAppearance;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionBuffs;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionHit;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonTurnsHandler;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class Ratmogrify extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	//this is sort of hacky, but we need it to know when to use alternate name/icon for heroic energy
	public static boolean useRatroicEnergy = false;

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

		Character ch = DungeonCharactersHandler.getCharacterOnPosition(target);

		if (ch == null || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(this, "no_target"));
			return;
		} else if (ch == hero){
			if (!hero.hasTalent(Talent.RATFORCEMENTS)){
				GLog.w(Messages.get(this, "self_target"));
				return;
			} else {
				ArrayList<Integer> spawnPoints = new ArrayList<>();

				for (int i = 0; i < PathFinder.OFFSETS_NEIGHBOURS8.length; i++) {
					int p = hero.position + PathFinder.OFFSETS_NEIGHBOURS8[i];
					if (DungeonCharactersHandler.getCharacterOnPosition( p ) == null && Dungeon.level.passable[p]) {
						spawnPoints.add( p );
					}
				}

				int ratsToSpawn = hero.pointsInTalent(Talent.RATFORCEMENTS);

				while (ratsToSpawn > 0 && spawnPoints.size() > 0) {
					int index = Random.index( spawnPoints );

					Rat rat = new Rat();
					rat.alignment = CharacterAlignment.ALLY;
					rat.state = rat.HUNTING;
					Buff.affect(rat, AscensionChallenge.AscensionBuffBlocker.class);
					GameScene.addMob( rat );
					ScrollOfTeleportation.appear( rat, spawnPoints.get( index ) );

					spawnPoints.remove( index );
					ratsToSpawn--;
				}

			}
		} else if (ch.alignment != CharacterAlignment.ENEMY || !(ch instanceof Mob) || ch instanceof Rat){
			GLog.w(Messages.get(this, "cant_transform"));
			return;
		} else if (ch instanceof TransmogRat){
			if (((TransmogRat) ch).allied || !hero.hasTalent(Talent.RATLOMACY)){
				GLog.w(Messages.get(this, "cant_transform"));
				return;
			} else {
				((TransmogRat) ch).makeAlly();
				ch.sprite.emitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				if (hero.pointsInTalent(Talent.RATLOMACY) > 1){
					Buff.affect(ch, Adrenaline.class, 2*(hero.pointsInTalent(Talent.RATLOMACY)-1));
				}
			}
		} else if (Character.hasProperty(ch, Character.Property.MINIBOSS) || Character.hasProperty(ch, Character.Property.BOSS)){
			GLog.w(Messages.get(this, "too_strong"));
			return;
		} else {
			TransmogRat rat = new TransmogRat();
			rat.setup((Mob)ch);
			rat.position = ch.position;

			//preserve champion enemy buffs
			HashSet<ChampionEnemy> champBuffs = ch.getBuffs(ChampionEnemy.class);
			for (ChampionEnemy champ : champBuffs){
				if (ActionBuffs.removeBuff(ch,champ)) {
					ch.sprite.clearAura();
				}
			}

			DungeonCharactersHandler.removeCharacter(ch);
			ch.sprite.killAndErase();
			Dungeon.level.mobs.remove(ch);

			for (ChampionEnemy champ : champBuffs){
				ActionBuffs.addBuff(ch,champ);
			}

			GameScene.addMob(rat);

			TargetHealthIndicator.instance.target(null);
			CellEmitter.get(rat.position).burst(Speck.factory(Speck.WOOL), 4);
			Sample.INSTANCE.play(Assets.Sounds.PUFF);

			Dungeon.level.occupyCell(rat);

			//for rare cases where a buff was keeping a mob alive (e.g. gnoll brutes)
			if (!rat.ActionHealth.isAlive()){
				rat.die(this);
			}
		}

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Invisibility.dispel();
		hero.spendTimeAdjustedAndNext(DungeonTurnsHandler.TICK);

	}

	@Override
	public int icon() {
		return HeroIcon.RATMOGRIFY;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.RATSISTANCE, Talent.RATLOMACY, Talent.RATFORCEMENTS, Talent.HEROIC_ENERGY};
	}

	public static class TransmogRat extends Mob {

		{
			spriteClass = RatSprite.class;

			//always false, as we derive stats from what we are transmogging from (which was already added)
			firstAdded = false;
		}

		private Mob original;
		private boolean allied;

		public void setup(Mob original) {
			this.original = original;

			healthPoints = original.healthPoints;
			healthMax = original.healthMax;

			evasionSkill = original.evasionSkill;

			EXP = original.EXP;
			maxLvl = original.maxLvl;

			if (original.state == original.SLEEPING) {
				state = SLEEPING;
			} else if (original.state == original.HUNTING) {
				state = HUNTING;
			} else {
				state = WANDERING;
			}

		}

		public Mob getOriginal(){
			if (original != null) {
				original.healthPoints = healthPoints;
				original.position = position;
			}
			return original;
		}

		private float timeLeft = 6f;

		@Override
        public boolean playGameTurn() {
			if (timeLeft <= 0){
				Mob original = getOriginal();
				this.original = null;
				original.clearTime();
				GameScene.addMob(original);

				EXP = 0;
				destroy();
				sprite.killAndErase();
				CellEmitter.get(original.position).burst(Speck.factory(Speck.WOOL), 4);
				Sample.INSTANCE.play(Assets.Sounds.PUFF);
				return true;
			} else {
				return super.playGameTurn();
			}
		}

		@Override
		protected void spendTimeAdjusted(float time) {
			if (!allied) timeLeft -= time;
			super.spendTimeAdjusted(time);
		}

		public void makeAlly() {
			allied = true;
			alignment = Alignment.ALLY;
			timeLeft = Float.POSITIVE_INFINITY;
		}

		public int getAccuracyAgainstTarget(Character target) {
			return ActionHit.getAccuracyAgainstTarget(original,target);
		}

		public int getArmorPointsRolled() {
			return original.getArmorPointsRolled();
		}

		@Override
		public int getDamageRoll() {
			int damage = original.getDamageRoll();
			if (!allied && Dungeon.hero.hasTalent(Talent.RATSISTANCE)){
				damage *= Math.pow(0.9f, Dungeon.hero.pointsInTalent(Talent.RATSISTANCE));
			}
			return damage;
		}

		@Override
		public float getAttackDelay() {
			return original.getAttackDelay();
		}

		@Override
		public void dropLoot() {
			original.position = position;
			original.dropLoot();
		}

		@Override
		public String getName() {
			return Messages.get(this, "name", ActionAppearance.getName(original));
		}

		{
			immunities.add(AllyBuff.class);
		}

		private static final String ORIGINAL = "original";
		private static final String ALLIED = "allied";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(ORIGINAL, original);
			bundle.put(ALLIED, allied);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);

			original = (Mob) bundle.get(ORIGINAL);
			evasionSkill = original.evasionSkill;
			EXP = original.EXP;

			allied = bundle.getBoolean(ALLIED);
			if (allied) alignment = Alignment.ALLY;
		}
	}
}
