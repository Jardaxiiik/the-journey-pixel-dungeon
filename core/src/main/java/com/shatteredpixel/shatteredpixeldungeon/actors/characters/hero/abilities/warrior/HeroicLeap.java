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

package com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.warrior;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class HeroicLeap extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public float chargeUse( Hero hero ) {
		float chargeUse = super.chargeUse(hero);
		if (hero.getBuff(DoubleJumpTracker.class) != null){
			//reduced charge use by 16%/30%/41%/50%
			chargeUse *= Math.pow(0.84, hero.pointsInTalent(Talent.DOUBLE_JUMP));
		}
		return chargeUse;
	}

	@Override
	public void activate( ClassArmor armor, Hero hero, Integer target ) {
		if (target != null) {

			if (hero.rooted){
				PixelScene.shake( 1, 1f );
				return;
			}

			Ballistica route = new Ballistica(hero.position, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
			int cell = route.collisionPos;

			//can't occupy the same cell as another char, so move back one.
			int backTrace = route.dist-1;
			while (Actor.getCharacterOnPosition( cell ) != null && cell != hero.position) {
				cell = route.path.get(backTrace);
				backTrace--;
			}

			armor.charge -= chargeUse( hero );
			armor.updateQuickslot();

			final int dest = cell;
			hero.busy();
			hero.sprite.jump(hero.position, cell, new Callback() {
				@Override
				public void call() {
					hero.moveToPosition(dest);
					Dungeon.level.occupyCell(hero);
					Dungeon.observe();
					GameScene.updateFog();

					for (int i : PathFinder.OFFSETS_NEIGHBOURS8) {
						Character mob = Actor.getCharacterOnPosition(hero.position + i);
						if (mob != null && mob != hero && mob.alignment != Character.Alignment.ALLY) {
							if (hero.hasTalent(Talent.BODY_SLAM)){
								int damage = Random.NormalIntRange(hero.pointsInTalent(Talent.BODY_SLAM), 4*hero.pointsInTalent(Talent.BODY_SLAM));
								damage += Math.round(hero.drRoll()*0.25f*hero.pointsInTalent(Talent.BODY_SLAM));
								damage -= mob.drRoll();
								mob.receiveDamageFromSource(damage, hero);
							}
							if (mob.position == hero.position + i && hero.hasTalent(Talent.IMPACT_WAVE)){
								Ballistica trajectory = new Ballistica(mob.position, mob.position + i, Ballistica.MAGIC_BOLT);
								int strength = 1+hero.pointsInTalent(Talent.IMPACT_WAVE);
								WandOfBlastWave.throwChar(mob, trajectory, strength, true, true, HeroicLeap.this);
								if (Random.Int(4) < hero.pointsInTalent(Talent.IMPACT_WAVE)){
									Buff.prolong(mob, Vulnerable.class, 5f);
								}
							}
						}
					}

					WandOfBlastWave.BlastWave.blast(dest);
					PixelScene.shake(2, 0.5f);

					Invisibility.dispel();
					hero.spendAndNext(Actor.TICK);

					if (hero.getBuff(DoubleJumpTracker.class) != null){
						hero.getBuff(DoubleJumpTracker.class).detach();
					} else {
						if (hero.hasTalent(Talent.DOUBLE_JUMP)) {
							Buff.affect(hero, DoubleJumpTracker.class, 3);
						}
					}
				}
			});
		}
	}

	public static class DoubleJumpTracker extends FlavourBuff{};

	@Override
	public int icon() {
		return HeroIcon.HEROIC_LEAP;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BODY_SLAM, Talent.IMPACT_WAVE, Talent.DOUBLE_JUMP, Talent.HEROIC_ENERGY};
	}
}
