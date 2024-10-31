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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actions.ActionBuffs;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.CharacterAlignment;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class Whip extends MeleeWeapon {

	{
		image = ItemSpriteSheet.WHIP;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1.1f;

		tier = 3;
		RCH = 3;    //lots of extra reach
	}

	@Override
	public int max(int lvl) {
		return  5*(tier) +      //15 base, down from 20
				lvl*(tier);     //+3 per level, down from +4
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {

		ArrayList<Character> targets = new ArrayList<>();
		Character closest = null;

		hero.belongings.abilityWeapon = this;
		for (Character ch : DungeonCharactersHandler.getCharacters()){
			if (ch.alignment == CharacterAlignment.ENEMY
					&& !ActionBuffs.isCharmedBy(hero,ch)
					&& Dungeon.level.heroFOV[ch.position]
					&& hero.canAttack(ch)){
				targets.add(ch);
				if (closest == null || Dungeon.level.trueDistance(hero.position, closest.position) > Dungeon.level.trueDistance(hero.position, ch.position)){
					closest = ch;
				}
			}
		}
		hero.belongings.abilityWeapon = null;

		if (targets.isEmpty()) {
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		throwSound();
		Character finalClosest = closest;
		hero.sprite.attack(hero.position, new Callback() {
			@Override
			public void call() {
				beforeAbilityUsed(hero, finalClosest);
				for (Character ch : targets) {
					hero.attack(ch, 1, 0, ch == finalClosest ? Character.INFINITE_ACCURACY : 1);
					if (!ActionHealth.isAlive(ch)){
						onAbilityKill(hero, ch);
					}
				}
				Invisibility.dispel();
				hero.spendTimeAdjustedAndNext(hero.attackDelay());
				afterAbilityUsed(hero);
			}
		});
	}

}
