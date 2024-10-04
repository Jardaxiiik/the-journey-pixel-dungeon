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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.duelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionSpendTime;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonTurnsHandler;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class Challenge extends ArmorAbility {

	{
		baseChargeUse = 35;
	}

	@Override
	public int icon() {
		return HeroIcon.CHALLENGE;
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
	public float chargeUse( Hero hero ) {
		float chargeUse = super.chargeUse(hero);
		if (hero.getBuff(EliminationMatchTracker.class) != null){
			//reduced charge use by 16%/30%/41%/50%
			chargeUse *= Math.pow(0.84, hero.pointsInTalent(Talent.ELIMINATION_MATCH));
		}
		return chargeUse;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		Character targetCh = DungeonCharactersHandler.getCharacterOnPosition(target);
		if (targetCh == null || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		if (hero.getBuff(DuelParticipant.class) != null){
			GLog.w(Messages.get(this, "already_dueling"));
			return;
		}

		if (targetCh.alignment == hero.alignment){
			GLog.w(Messages.get(this, "ally_target"));
			return;
		}

		boolean[] passable = BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null);
		for (Character c : DungeonCharactersHandler.getCharacters()) {
			if (c != hero) passable[c.position] = false;
		}
		PathFinder.buildDistanceMap(targetCh.position, passable);
		int[] reachable = PathFinder.distance.clone();

		int blinkpos = hero.position;
		if (hero.hasTalent(Talent.CLOSE_THE_GAP) && !hero.getCharacterMovement().isRooted()){

			int blinkrange = 1 + hero.pointsInTalent(Talent.CLOSE_THE_GAP);
			PathFinder.buildDistanceMap(hero.position, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null), blinkrange);

			for (int i = 0; i < PathFinder.distance.length; i++){
				if (PathFinder.distance[i] == Integer.MAX_VALUE
						|| reachable[i] == Integer.MAX_VALUE
						|| (!Dungeon.level.passable[i] && !(hero.getCharacterMovement().isFlying() && Dungeon.level.avoid[i]))
						|| i == targetCh.position){
					continue;
				}

				if (Dungeon.level.distance(i, targetCh.position) < Dungeon.level.distance(blinkpos, targetCh.position)){
					blinkpos = i;
				} else if (Dungeon.level.distance(i, targetCh.position) == Dungeon.level.distance(blinkpos, targetCh.position)){
					if (Dungeon.level.trueDistance(i, hero.position) < Dungeon.level.trueDistance(blinkpos, hero.position)){
						blinkpos = i;
					}
				}
			}
		}

		if (reachable[blinkpos] == Integer.MAX_VALUE){
			GLog.w(Messages.get(this, "unreachable_target"));
			if (hero.getCharacterMovement().isRooted()) PixelScene.shake( 1, 1f );
			return;
		}

		if (Dungeon.level.distance(blinkpos, targetCh.position) > 5){
			GLog.w(Messages.get(this, "distant_target"));
			if (hero.getCharacterMovement().isRooted()) PixelScene.shake( 1, 1f );
			return;
		}

		if (blinkpos != hero.position){
			Dungeon.hero.position = blinkpos;
			Dungeon.level.occupyCell(Dungeon.hero);
			//prevents the hero from being interrupted by seeing new enemies
			Dungeon.observe();
			GameScene.updateFog();
			Dungeon.hero.checkVisibleMobs();

			Dungeon.hero.sprite.place( Dungeon.hero.position);
			CellEmitter.get( Dungeon.hero.position).burst( Speck.factory( Speck.WOOL ), 6 );
			Sample.INSTANCE.play( Assets.Sounds.PUFF );
		}

		boolean bossTarget = Character.hasProperty(targetCh, Character.Property.BOSS);
		for (Character characterToFreeze : DungeonCharactersHandler.getCharacters()){
			if (characterToFreeze != targetCh && characterToFreeze.alignment != Character.Alignment.ALLY && !(characterToFreeze instanceof NPC)
				&& (!bossTarget || !(Character.hasProperty(targetCh, Character.Property.BOSS) || Character.hasProperty(targetCh, Character.Property.BOSS_MINION)))) {
				ActionSpendTime.makeCharacterSpendTime(characterToFreeze,DuelParticipant.DURATION);
				Buff.affect(characterToFreeze, SpectatorFreeze.class, DuelParticipant.DURATION);
			}
		}

		Buff.affect(targetCh, DuelParticipant.class);
		Buff.affect(hero, DuelParticipant.class);
		if (targetCh instanceof Mob){
			((Mob) targetCh).startHunting(hero);
		}

		GameScene.flash(0x80FFFFFF);
		Sample.INSTANCE.play(Assets.Sounds.DESCEND);

		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		Invisibility.dispel();
		hero.sprite.zap(target);

		DungeonTurnsHandler.nextActorToPlayHero(hero);();

		if (hero.getBuff(EliminationMatchTracker.class) != null){
			hero.getBuff(EliminationMatchTracker.class).detach();
		}
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.CLOSE_THE_GAP, Talent.INVIGORATING_VICTORY, Talent.ELIMINATION_MATCH, Talent.HEROIC_ENERGY};
	}

	public static class EliminationMatchTracker extends FlavourBuff{};

	public static class DuelParticipant extends Buff {

		public static float DURATION = 10f;

		private int left = (int)DURATION;
		private int takenDmg = 0;

		@Override
		public int icon() {
			return BuffIndicator.CHALLENGE;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - left) / DURATION);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}

		public void addDamage(int dmg){
			takenDmg += dmg;
		}

		@Override
		public boolean playGameTurn() {

			left--;
			if (left == 0) {
				detach();
			} else {
				Character other = null;
				for (Character ch : DungeonCharactersHandler.getCharacters()){
					if (ch != target && ch.getBuff(DuelParticipant.class) != null){
						other = ch;
					}
				}

				if (other == null
					|| target.alignment == other.alignment
					|| Dungeon.level.distance(target.position, other.position) > 5) {
					detach();
				}
			}

			spendTimeAdjusted(DungeonTurnsHandler.TICK);
			return true;
		}

		@Override
		public void detach() {
			super.detach();
			if (target != Dungeon.hero){
				if (!target.isAlive() || target.alignment == Dungeon.hero.alignment){
					Sample.INSTANCE.play(Assets.Sounds.BOSS);

					if (Dungeon.hero.hasTalent(Talent.INVIGORATING_VICTORY)){
						DuelParticipant heroBuff = Dungeon.hero.getBuff(DuelParticipant.class);

						int hpToHeal = 0;
						if (heroBuff != null){
							hpToHeal = heroBuff.takenDmg;
						}

						//heals for 30%/50%/65%/75% of taken damage plus 5/10/15/20 bonus, based on talent points
						hpToHeal = (int)Math.round(hpToHeal * (1f - Math.pow(0.707f, Dungeon.hero.pointsInTalent(Talent.INVIGORATING_VICTORY))));
						hpToHeal += 5*Dungeon.hero.pointsInTalent(Talent.INVIGORATING_VICTORY);
						hpToHeal = Math.min(hpToHeal, Dungeon.hero.healthMax - Dungeon.hero.healthPoints);
						if (hpToHeal > 0){
							Dungeon.hero.healthPoints += hpToHeal;
							Dungeon.hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.33f, 6 );
							Dungeon.hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(hpToHeal), FloatingText.HEALING );
						}
					}
				}

				for (Character ch : DungeonCharactersHandler.getCharacters()) {
					if (ch.getBuff(SpectatorFreeze.class) != null) {
						ch.getBuff(SpectatorFreeze.class).detach();
					}
					if (ch.getBuff(DuelParticipant.class) != null && ch != target) {
						ch.getBuff(DuelParticipant.class).detach();
					}
				}
			} else {
				if (Dungeon.hero.isAlive()) {
					GameScene.flash(0x80FFFFFF);

					if (Dungeon.hero.hasTalent(Talent.ELIMINATION_MATCH)){
						Buff.affect(target, EliminationMatchTracker.class, 3);
					}
				}
			}
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", left);
		}

		private static final String LEFT = "left";
		private static final String TAKEN_DMG = "taken_dmg";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LEFT, left);
			bundle.put(TAKEN_DMG, takenDmg);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			left = bundle.getInt(LEFT);
			takenDmg = bundle.getInt(TAKEN_DMG);
		}
	}

	public static class SpectatorFreeze extends FlavourBuff {

		@Override
		public void fx(boolean on) {
			if (on) {
				target.sprite.add(CharSprite.State.DARKENED);
				target.sprite.add(CharSprite.State.PARALYSED);
			} else {
				//allies can't be spectator frozen, so just check doom
				if (target.getBuff(Doom.class) == null) target.sprite.remove(CharSprite.State.DARKENED);
				if (target.paralysed == 0) target.sprite.remove(CharSprite.State.PARALYSED);
			}
		}

		@Override
		public void detach(){
			super.detach();
			if (cooldown() > 0) {
				ActionSpendTime.makeCharacterSpendTime(target,-cooldown());
			}
		}

		{
			immunities.addAll(new BlobImmunity().immunities());
		}

	}
}
