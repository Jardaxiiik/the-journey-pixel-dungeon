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
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class DeathMark extends ArmorAbility {

	{
		baseChargeUse = 25f;
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
		if (hero.getBuff(DoubleMarkTracker.class) != null){
			//reduced charge use by 30%/50%/65%/75%
			chargeUse *= Math.pow(0.707, hero.pointsInTalent(Talent.DOUBLE_MARK));
		}
		return chargeUse;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		Character ch = DungeonCharactersHandler.getCharacterOnPosition(target);

		if (ch == null || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "no_target"));
			return;
		} else if (ch.alignment != CharacterAlignment.ENEMY){
			GLog.w(Messages.get(this, "ally_target"));
			return;
		}

		if (ch != null){
			Buff.affect(ch, DeathMarkTracker.class, DeathMarkTracker.DURATION).setInitialHP(ch.healthPoints);
		}

		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();
		hero.sprite.zap(target);

		DungeonTurnsHandler.nextActorToPlayHero(hero);();

		if (hero.getBuff(DoubleMarkTracker.class) != null){
			hero.getBuff(DoubleMarkTracker.class).detach();
		} else if (hero.hasTalent(Talent.DOUBLE_MARK)) {
			Buff.affect(hero, DoubleMarkTracker.class, 0.01f);
		}

	}

	public static void processFearTheReaper( Character ch ){
		if (ch.healthPoints > 0 || ch.getBuff(DeathMarkTracker.class) == null){
			return;
		}

		if (Dungeon.hero.hasTalent(Talent.FEAR_THE_REAPER)) {
			if (Dungeon.hero.pointsInTalent(Talent.FEAR_THE_REAPER) >= 2) {
				Buff.prolong(ch, Terror.class, 5f).object = Dungeon.hero.getId();
			}
			Buff.prolong(ch, Cripple.class, 5f);

			if (Dungeon.hero.pointsInTalent(Talent.FEAR_THE_REAPER) >= 3) {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				PathFinder.buildDistanceMap(ch.position, passable, 3);

				for (Character near : DungeonCharactersHandler.getCharacters()) {
					if (near != ch && near.alignment == CharacterAlignment.ENEMY
							&& PathFinder.distance[near.position] != Integer.MAX_VALUE) {
						if (Dungeon.hero.pointsInTalent(Talent.FEAR_THE_REAPER) == 4) {
							Buff.prolong(near, Terror.class, 5f).object = Dungeon.hero.getId();
						}
						Buff.prolong(near, Cripple.class, 5f);
					}
				}
			}
		}
	}

	public static class DoubleMarkTracker extends FlavourBuff{};

	@Override
	public int icon() {
		return HeroIcon.DEATH_MARK;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.FEAR_THE_REAPER, Talent.DEATHLY_DURABILITY, Talent.DOUBLE_MARK, Talent.HEROIC_ENERGY};
	}

	public static class DeathMarkTracker extends FlavourBuff {

		public static float DURATION = 5f;

		int initialHP = 0;

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		@Override
		public int icon() {
			return BuffIndicator.INVERT_MARK;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0.2f, 0.2f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		private void setInitialHP( int hp ){
			if (initialHP < hp){
				initialHP = hp;
			}
		}

		@Override
		public boolean attachTo(Character target) {
			if (super.attachTo(target)){
				target.deathMarked = true;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void detach() {
			super.detach();
			target.deathMarked = false;
			if (!target.ActionHealth.isAlive()){
				target.sprite.flash();
				target.sprite.bloodBurstA(target.sprite.center(), target.healthMax *2);
				Sample.INSTANCE.play(Assets.Sounds.HIT_STAB);
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				target.die(this);
				int shld = Math.round(initialHP * (0.125f*Dungeon.hero.pointsInTalent(Talent.DEATHLY_DURABILITY)));
				if (shld > 0 && target.alignment != CharacterAlignment.ALLY){
					Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shld), FloatingText.SHIELDING);
					Buff.affect(Dungeon.hero, Barrier.class).setShield(shld);
				}
			}
		}

		private static String INITIAL_HP = "initial_hp";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(INITIAL_HP, initialHP);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			initialHP = bundle.getInt(INITIAL_HP);
		}
	}

}
