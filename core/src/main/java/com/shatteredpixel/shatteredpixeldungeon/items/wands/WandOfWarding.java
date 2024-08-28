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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WardSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfWarding extends Wand {

	{
		image = ItemSpriteSheet.WAND_WARDING;
	}

	@Override
	public int collisionProperties(int target) {
		if (cursed)                                 return super.collisionProperties(target);
		else if (!Dungeon.level.heroFOV[target])    return Ballistica.PROJECTILE;
		else                                        return Ballistica.STOP_TARGET;
	}

	private boolean wardAvailable = true;
	
	@Override
	public boolean tryToZap(Hero owner, int target) {
		
		int currentWardEnergy = 0;
		for (Character ch : Actor.getCharacters()){
			if (ch instanceof Ward){
				currentWardEnergy += ((Ward) ch).tier;
			}
		}
		
		int maxWardEnergy = 0;
		for (Buff buff : curUser.getBuffs()){
			if (buff instanceof Wand.Charger){
				if (((Charger) buff).wand() instanceof WandOfWarding){
					maxWardEnergy += 2 + ((Charger) buff).wand().level();
				}
			}
		}
		
		wardAvailable = (currentWardEnergy < maxWardEnergy);
		
		Character ch = Actor.getCharacterOnPosition(target);
		if (ch instanceof Ward){
			if (!wardAvailable && ((Ward) ch).tier <= 3){
				GLog.w( Messages.get(this, "no_more_wards"));
				return false;
			}
		} else {
			if ((currentWardEnergy + 1) > maxWardEnergy){
				GLog.w( Messages.get(this, "no_more_wards"));
				return false;
			}
		}
		
		return super.tryToZap(owner, target);
	}
	
	@Override
	public void onZap(Ballistica bolt) {

		int target = bolt.collisionPos;
		Character ch = Actor.getCharacterOnPosition(target);
		if (ch != null && !(ch instanceof Ward)){
			if (bolt.dist > 1) target = bolt.path.get(bolt.dist-1);

			ch = Actor.getCharacterOnPosition(target);
			if (ch != null && !(ch instanceof Ward)){
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(bolt.collisionPos);
				return;
			}
		}

		if (!Dungeon.level.passable[target]){
			GLog.w( Messages.get(this, "bad_location"));
			Dungeon.level.pressCell(target);
			
		} else if (ch != null){
			if (ch instanceof Ward){
				if (wardAvailable) {
					((Ward) ch).upgrade( buffedLvl() );
				} else {
					((Ward) ch).wandHeal( buffedLvl() );
				}
				ch.sprite.emitter().burst(MagicMissile.WardParticle.UP, ((Ward) ch).tier);
			} else {
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(target);
			}
			
		} else {
			Ward ward = new Ward();
			ward.position = target;
			ward.wandLevel = buffedLvl();
			GameScene.add(ward, 1f);
			Dungeon.level.occupyCell(ward);
			ward.sprite.emitter().burst(MagicMissile.WardParticle.UP, ward.tier);
			Dungeon.level.pressCell(target);

		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile m = MagicMissile.boltFromChar(curUser.sprite.parent,
				MagicMissile.WARD,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		
		if (bolt.dist > 10){
			m.setSpeed(bolt.dist*20);
		}
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(MagesStaff staff, Character attacker, Character defender, int damage) {
		int level = Math.max( 0, staff.buffedLvl() );

		// lvl 0 - 20%
		// lvl 1 - 33%
		// lvl 2 - 43%
		float procChance = (level+1f)/(level+5f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {

			float powerMulti = Math.max(1f, procChance);

			for (Character ch : Actor.getCharacters()){
				if (ch instanceof Ward){
					((Ward) ch).wandHeal(staff.buffedLvl(), powerMulti);
					ch.sprite.emitter().burst(MagicMissile.WardParticle.UP, ((Ward) ch).tier);
				}
			}
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0x8822FF );
		particle.am = 0.3f;
		particle.setLifespan(3f);
		particle.speed.polar(Random.Float(PointF.PI2), 0.3f);
		particle.setSize( 1f, 2f);
		particle.radiateXY(2.5f);
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", level()+2);
		else
			return Messages.get(this, "stats_desc", 2);
	}

	public static class Ward extends NPC {

		public int tier = 1;
		private int wandLevel = 1;

		public int totalZaps = 0;

		{
			spriteClass = WardSprite.class;

			alignment = Alignment.ALLY;

			properties.add(Property.IMMOVABLE);
			properties.add(Property.INORGANIC);

			viewDistance = 4;
			state = WANDERING;
		}

		@Override
		public String getName() {
			return Messages.get(this, "name_" + tier );
		}

		public void upgrade(int wandLevel ){
			if (this.wandLevel < wandLevel){
				this.wandLevel = wandLevel;
			}

			switch (tier){
				case 1: case 2: default:
					break; //do nothing
				case 3:
					healthMax = 35;
					healthPoints = 15 + (5-totalZaps)*4;
					break;
				case 4:
					healthMax = 54;
					healthPoints += 19;
					break;
				case 5:
					healthMax = 84;
					healthPoints += 30;
					break;
				case 6:
					wandHeal(wandLevel);
					break;
			}

			if (tier < 6){
				tier++;
				viewDistance++;
				if (sprite != null){
					((WardSprite)sprite).updateTier(tier);
					sprite.place(position);
				}
				GameScene.updateFog(position, viewDistance+1);
			}

		}

		public void wandHeal( int wandLevel ){
			wandHeal( wandLevel, 1f );
		}

		public void wandHeal( int wandLevel, float healFactor ){
			if (this.wandLevel < wandLevel){
				this.wandLevel = wandLevel;
			}

			int heal;
			switch(tier){
				default:
					return;
				case 4:
					heal = Math.round(9 * healFactor);
					break;
				case 5:
					heal = Math.round(12 * healFactor);
					break;
				case 6:
					heal = Math.round(16 * healFactor);
					break;
			}

			healthPoints = Math.min(healthMax, healthPoints +heal);
			if (sprite != null) sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);

		}

		@Override
		public int getEvasionAgainstAttacker(Character enemy) {
			if (tier > 3){
				evasionSkill = 4 + Dungeon.scalingDepth();
			}
			return super.getEvasionAgainstAttacker(enemy);
		}

		@Override
		public int getArmorPointsRolled() {
			int armorPoints = super.getArmorPointsRolled();
			if (tier > 3){
				return armorPoints + Math.round(Random.NormalIntRange(0, 3 + Dungeon.scalingDepth()/2) / (7f - tier));
			} else {
				return armorPoints;
			}
		}

		@Override
		protected boolean canAttackEnemy(Character enemy ) {
			return new Ballistica(position, enemy.position, Ballistica.MAGIC_BOLT).collisionPos == enemy.position;
		}

		@Override
		protected boolean attackCharacter(Character targetCharacter) {
			boolean visible = fieldOfView[position] || fieldOfView[targetCharacter.position];
			if (visible) {
				sprite.zap( targetCharacter.position);
			} else {
				zap();
			}

			return !visible;
		}

		private void zap() {
			spendTimeAdjusted( 1f );

			//always hits
			int dmg = Random.NormalIntRange( 2 + wandLevel, 8 + 4*wandLevel );
			Character enemy = this.enemy;
			enemy.receiveDamageFromSource( dmg, this );
			if (enemy.isAlive()){
				Wand.wandProc(enemy, wandLevel, 1);
			}

			if (!enemy.isAlive() && enemy == Dungeon.hero) {
				Badges.validateDeathFromFriendlyMagic();
				GLog.n(Messages.capitalize(Messages.get( this, "kill", getName() )));
				Dungeon.fail( WandOfWarding.class );
			}

			totalZaps++;
			switch(tier){
				case 1: case 2: case 3: default:
					if (totalZaps >= (2*tier-1)){
						die(this);
					}
					break;
				case 4:
					receiveDamageFromSource(5, this);
					break;
				case 5:
					receiveDamageFromSource(6, this);
					break;
				case 6:
					receiveDamageFromSource(7, this);
					break;
			}
		}

		public void onZapComplete() {
			zap();
			next();
		}

		@Override
		protected boolean moveCloserToTarget(int targetPosition) {
			return false;
		}

		@Override
		protected boolean moveAwayFromTarget(int targetPosition) {
			return false;
		}

		@Override
		public CharSprite sprite() {
			WardSprite sprite = (WardSprite) super.sprite();
			sprite.linkVisuals(this);
			return sprite;
		}

		@Override
		public void updateSpriteState() {
			super.updateSpriteState();
			((WardSprite)sprite).updateTier(tier);
			sprite.place(position);
		}
		
		@Override
		public void destroy() {
			super.destroy();
			Dungeon.observe();
			GameScene.updateFog(position, viewDistance+1);
		}
		
		@Override
		public boolean canInteract(Character c) {
			return true;
		}

		@Override
		public boolean interact( Character c ) {
			if (c != Dungeon.hero){
				return true;
			}
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions( sprite(),
							Messages.get(Ward.this, "dismiss_title"),
							Messages.get(Ward.this, "dismiss_body"),
							Messages.get(Ward.this, "dismiss_confirm"),
							Messages.get(Ward.this, "dismiss_cancel") ){
						@Override
						protected void onSelect(int index) {
							if (index == 0){
								die(null);
							}
						}
					});
				}
			});
			return true;
		}

		@Override
		public String getDescription() {
			return Messages.get(this, "desc_" + tier, 2+wandLevel, 8 + 4*wandLevel, tier );
		}
		
		{
			immunities.add( Sleep.class );
			immunities.add( Terror.class );
			immunities.add( Dread.class );
			immunities.add( Vertigo.class );
			immunities.add( AllyBuff.class );
		}

		private static final String TIER = "tier";
		private static final String WAND_LEVEL = "wand_level";
		private static final String TOTAL_ZAPS = "total_zaps";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TIER, tier);
			bundle.put(WAND_LEVEL, wandLevel);
			bundle.put(TOTAL_ZAPS, totalZaps);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			tier = bundle.getInt(TIER);
			viewDistance = 3 + tier;
			wandLevel = bundle.getInt(WAND_LEVEL);
			totalZaps = bundle.getInt(TOTAL_ZAPS);
		}
	}
}
