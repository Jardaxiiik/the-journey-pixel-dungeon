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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.rogue;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionInteract;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ShadowClone extends ArmorAbility {

	@Override
	public String targetingPrompt() {
		if (getShadowAlly() == null) {
			return super.targetingPrompt();
		} else {
			return Messages.get(this, "prompt");
		}
	}

	@Override
	public boolean useTargeting(){
		return false;
	}

	{
		baseChargeUse = 35f;
	}

	@Override
	public float chargeUse(Hero hero) {
		if (getShadowAlly() == null) {
			return super.chargeUse(hero);
		} else {
			return 0;
		}
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		ShadowAlly ally = getShadowAlly();

		if (ally != null){
			if (target == null){
				return;
			} else {
				ally.directTocell(target);
			}
		} else {
			ArrayList<Integer> spawnPoints = new ArrayList<>();
			for (int i = 0; i < PathFinder.OFFSETS_NEIGHBOURS8.length; i++) {
				int p = hero.position + PathFinder.OFFSETS_NEIGHBOURS8[i];
				if (DungeonCharactersHandler.getCharacterOnPosition(p) == null && Dungeon.level.passable[p]) {
					spawnPoints.add(p);
				}
			}

			if (!spawnPoints.isEmpty()){
				armor.charge -= chargeUse(hero);
				armor.updateQuickslot();

				ally = new ShadowAlly(hero.lvl);
				ally.position = Random.element(spawnPoints);
				GameScene.addMob(ally);

				ShadowAlly.appear(ally, ally.position);

				Invisibility.dispel();
				hero.spendTimeAdjustedAndNext(DungeonTurnsHandler.TICK);

			} else {
				GLog.w(Messages.get(SpiritHawk.class, "no_space"));
			}
		}

	}

	@Override
	public int icon() {
		return HeroIcon.SHADOW_CLONE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.SHADOW_BLADE, Talent.CLONED_ARMOR, Talent.PERFECT_COPY, Talent.HEROIC_ENERGY};
	}

	private static ShadowAlly getShadowAlly(){
		for (Character ch : DungeonCharactersHandler.getCharacters()){
			if (ch instanceof ShadowAlly){
				return (ShadowAlly) ch;
			}
		}
		return null;
	}

	public static class ShadowAlly extends DirectableAlly {

		{
			spriteClass = ShadowSprite.class;

			healthPoints = healthMax = 80;

			immunities.add(AllyBuff.class);

			properties.add(Property.INORGANIC);
		}

		public ShadowAlly(){
			super();
		}

		public ShadowAlly( int heroLevel ){
			super();
			int hpBonus = 15 + 5*heroLevel;
			hpBonus = Math.round(0.1f * Dungeon.hero.pointsInTalent(Talent.PERFECT_COPY) * hpBonus);
			if (hpBonus > 0){
				healthMax += hpBonus;
				healthPoints += hpBonus;
			}
			evasionSkill = heroLevel + 5; //equal to base hero defense skill
		}

		@Override
        public boolean playGameTurn() {
			int oldPos = position;
			boolean result = super.playGameTurn();
			//partially simulates how the hero switches to idle animation
			if ((position == target || oldPos == position) && sprite.looping()){
				sprite.idle();
			}
			return result;
		}

		@Override
		public void defendPos(int cell) {
			GLog.i(Messages.get(this, "direct_defend"));
			super.defendPos(cell);
		}

		@Override
		public void followHero() {
			GLog.i(Messages.get(this, "direct_follow"));
			super.followHero();
		}

		@Override
		public void targetChar(Character ch) {
			GLog.i(Messages.get(this, "direct_attack"));
			super.targetChar(ch);
		}

		@Override
		public int getAccuracyAgainstTarget(Character target) {
			return evasionSkill +5; //equal to base hero attack skill
		}

		@Override
		public int getDamageRoll() {
			int damage = Random.NormalIntRange(10, 20);
			int heroDamage = Dungeon.hero.getDamageRoll();
			heroDamage /= Dungeon.hero.attackDelay(); //normalize hero damage based on atk speed
			heroDamage = Math.round(0.08f * Dungeon.hero.pointsInTalent(Talent.SHADOW_BLADE) * heroDamage);
			if (heroDamage > 0){
				damage += heroDamage;
			}
			return damage;
		}

		@Override
		public int attackProc_1(Character enemy, int damage ) {
			damage = ActionAttack(this, enemy, damage );
			if (Random.Int(4) < Dungeon.hero.pointsInTalent(Talent.SHADOW_BLADE)
					&& Dungeon.hero.belongings.weapon() != null){
				return Dungeon.hero.belongings.weapon().proc( this, enemy, damage );
			} else {
				return damage;
			}
		}

		@Override
		public int getArmorPointsRolled() {
			int armorPoints = super.getArmorPointsRolled();
			int heroRoll = Dungeon.hero.getArmorPointsRolled();
			heroRoll = Math.round(0.12f * Dungeon.hero.pointsInTalent(Talent.CLONED_ARMOR) * heroRoll);
			if (heroRoll > 0){
				armorPoints += heroRoll;
			}
			return armorPoints;
		}

		@Override
		public boolean isImmuneToEffectType(Class effect) {
			if (effect == Burning.class
					&& Random.Int(4) < Dungeon.hero.pointsInTalent(Talent.CLONED_ARMOR)
					&& Dungeon.hero.belongings.armor() != null
					&& Dungeon.hero.belongings.armor().hasGlyph(Brimstone.class, this)){
				return true;
			}
			return super.isImmuneToEffectType(effect);
		}

		@Override
		public int getDamageReceivedFromEnemyReducedByDefense(Character enemy, int damage) {
			damage = super.getDamageReceivedFromEnemyReducedByDefense(enemy, damage);
			if (Random.Int(4) < Dungeon.hero.pointsInTalent(Talent.CLONED_ARMOR)
					&& Dungeon.hero.belongings.armor() != null){
				return Dungeon.hero.belongings.armor().proc( enemy, this, damage );
			} else {
				return damage;
			}
		}

		@Override
		public void receiveDamageFromSource(int dmg, Object sourceOfDamage) {

			//TODO improve this when I have proper damage source logic
			if (Random.Int(4) < Dungeon.hero.pointsInTalent(Talent.CLONED_ARMOR)
					&& Dungeon.hero.belongings.armor() != null
					&& Dungeon.hero.belongings.armor().hasGlyph(AntiMagic.class, this)
					&& AntiMagic.RESISTS.contains(sourceOfDamage.getClass())){
				dmg -= AntiMagic.drRoll(Dungeon.hero, Dungeon.hero.belongings.armor().buffedLvl());
				dmg = Math.max(dmg, 0);
			}

			super.receiveDamageFromSource(dmg, sourceOfDamage);
		}

		@Override
		public float getSpeed() {
			float speed = super.getSpeed();

			//moves 2 tiles at a time when returning to the hero
			if (state == WANDERING
					&& defendingPos == -1
					&& Dungeon.level.distance(position, Dungeon.hero.position) > 1){
				speed *= 2;
			}

			return speed;
		}

		@Override
		public boolean canInteract(Character c) {
			if (ActionInteract.canInteract(this,c)){
				return true;
			} else if (Dungeon.level.distance(position, c.position) <= Dungeon.hero.pointsInTalent(Talent.PERFECT_COPY)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean interact(Character c) {
			if (!Dungeon.hero.hasTalent(Talent.PERFECT_COPY)){
				return super.interact(c);
			}

			//some checks from super.interact
			if (!Dungeon.level.passable[position] && !c.getCharacterMovement().isFlying()){
				return true;
			}

			if (getProperties().contains(Property.LARGE) && !Dungeon.level.openSpace[c.position]
					|| c.getProperties().contains(Property.LARGE) && !Dungeon.level.openSpace[position]){
				return true;
			}

			int curPos = position;

			//warp instantly with the clone
			PathFinder.buildDistanceMap(c.position, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
			if (PathFinder.distance[position] == Integer.MAX_VALUE){
				return true;
			}
			appear(this, Dungeon.hero.position);
			appear(Dungeon.hero, curPos);
			Dungeon.observe();
			GameScene.updateFog();
			return true;
		}

		private static void appear(Character ch, int pos ) {

			ch.sprite.interruptMotion();

			if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[ch.position]){
				Sample.INSTANCE.play(Assets.Sounds.PUFF);
			}

			ch.moveToPosition( pos );
			if (ch.position == pos) ch.sprite.place( pos );

			if (Dungeon.level.heroFOV[pos] || ch == Dungeon.hero ) {
				ch.sprite.emitter().burst(SmokeParticle.FACTORY, 10);
			}
		}

		private static final String DEF_SKILL = "def_skill";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(DEF_SKILL, evasionSkill);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			evasionSkill = bundle.getInt(DEF_SKILL);
		}
	}

	public static class ShadowSprite extends MobSprite {

		private Emitter smoke;

		public ShadowSprite() {
			super();

			texture( Dungeon.hero.heroClass.spritesheet() );

			TextureFilm film = new TextureFilm( HeroSprite.tiers(), 6, 12, 15 );

			idle = new Animation( 1, true );
			idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

			run = new Animation( 20, true );
			run.frames( film, 2, 3, 4, 5, 6, 7 );

			die = new Animation( 20, false );
			die.frames( film, 0 );

			attack = new Animation( 15, false );
			attack.frames( film, 13, 14, 15, 0 );

			idle();
			resetColor();
		}

		@Override
		public void onComplete(Tweener tweener) {
			super.onComplete(tweener);
		}

		@Override
		public void resetColor() {
			super.resetColor();
			alpha(0.8f);
			brightness(0.0f);
		}

		@Override
		public void link( Character ch ) {
			super.link( ch );
			renderShadow = false;

			if (smoke == null) {
				smoke = emitter();
				smoke.pour( CityLevel.Smoke.factory, 0.2f );
			}
		}

		@Override
		public void update() {

			super.update();

			if (smoke != null) {
				smoke.visible = visible;
			}
		}

		@Override
		public void kill() {
			super.kill();

			if (smoke != null) {
				smoke.on = false;
			}
		}
	}
}
