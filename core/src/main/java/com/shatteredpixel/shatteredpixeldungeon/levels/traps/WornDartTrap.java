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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.JourneyPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Character;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class WornDartTrap extends Trap {

	{
		color = GREY;
		shape = CROSSHAIR;
		
		canBeHidden = false;
		avoidsHallways = true;
	}

	@Override
	public void activate() {

		//we handle this inside of a separate actor as the trap may produce a visual effect we need to pause for
		Actor.addActor(new Actor() {

			{
				actPriority = VFX_PRIO;
			}

			@Override
			protected boolean playGameTurn() {
				Actor.removeActor(this);
				Character target = Actor.getCharacterOnPosition(pos);

				//find the closest char that can be aimed at
				if (target == null){
					float closestDist = Float.MAX_VALUE;
					for (Character ch : Actor.getCharacters()){
						if (!ch.isAlive()) continue;
						float curDist = Dungeon.level.trueDistance(pos, ch.position);
						if (ch.invisible > 0) curDist += 1000;
						Ballistica bolt = new Ballistica(pos, ch.position, Ballistica.PROJECTILE);
						if (bolt.collisionPos == ch.position && curDist < closestDist){
							target = ch;
							closestDist = curDist;
						}
					}
				}

				if (target != null) {
					final Character finalTarget = target;
					if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[target.position]) {
						((MissileSprite) JourneyPixelDungeon.scene().recycle(MissileSprite.class)).
								reset(pos, finalTarget.sprite, new Dart(), new Callback() {
									@Override
									public void call() {
										int dmg = Random.NormalIntRange(4, 8) - finalTarget.drRoll();
										finalTarget.receiveDamageFromSource(dmg, WornDartTrap.this);
										if (finalTarget == Dungeon.hero && !finalTarget.isAlive()){
											Dungeon.fail( WornDartTrap.this  );
										}
										Sample.INSTANCE.play(Assets.Sounds.HIT, 1, 1, Random.Float(0.8f, 1.25f));
										finalTarget.sprite.bloodBurstA(finalTarget.sprite.center(), dmg);
										finalTarget.sprite.flash();
										next();
									}
								});
						return false;
					} else {
						finalTarget.receiveDamageFromSource(Random.NormalIntRange(4, 8) - finalTarget.drRoll(), WornDartTrap.this);
						return true;
					}
				} else {
					return true;
				}
			}

		});
	}
}
