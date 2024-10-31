package com.shatteredpixel.shatteredpixeldungeon.actions;

import static com.shatteredpixel.shatteredpixeldungeon.actions.ActionMove.moveToPosition;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.CharacterAlignment;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

/**
 * ActionInteract handles the interaction between two characters.
 * (Mainly Hero and other ally Character swapping positions)
 */
public class ActionInteract {

    public static boolean canInteract(Character caster, Character target){
        if (Dungeon.level.adjacent(caster.position, target.position)){
            return true;
        } else if (target instanceof Hero
                && caster.alignment == CharacterAlignment.ALLY
                && !caster.hasProperty(caster, Character.Property.IMMOVABLE)
                && Dungeon.level.distance(caster.position, target.position) <= 2*Dungeon.hero.pointsInTalent(Talent.ALLY_WARP)){
            return true;
        } else {
            return false;
        }
    }

    public static boolean interact(Character caster, Character target){

        //don't allow char to swap onto hazard unless they're flying
        //you can swap onto a hazard though, as you're not the one instigating the swap
        if (!Dungeon.level.passable[caster.position] && !target.isFlying){
            return true;
        }

        //can't swap into a space without room
        if (caster.getProperties().contains(Character.Property.LARGE) && !Dungeon.level.openSpace[target.position]
                || target.getProperties().contains(Character.Property.LARGE) && !Dungeon.level.openSpace[caster.position]){
            return true;
        }

        //we do a little raw position shuffling here so that the characters are never
        // on the same cell when logic such as occupyCell() is triggered
        int oldPos = caster.position;
        int newPos = target.position;

        //can't swap or ally warp if either char is immovable
        if (caster.hasProperty(caster, Character.Property.IMMOVABLE) || caster.hasProperty(target, Character.Property.IMMOVABLE)){
            return true;
        }

        //warp instantly with allies in this case
        if (target == Dungeon.hero && Dungeon.hero.hasTalent(Talent.ALLY_WARP)){
            PathFinder.buildDistanceMap(target.position, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
            if (PathFinder.distance[caster.position] == Integer.MAX_VALUE){
                return true;
            }
            caster.position = newPos;
            target.position = oldPos;
            ScrollOfTeleportation.appear(caster, newPos);
            ScrollOfTeleportation.appear(target, oldPos);
            Dungeon.observe();
            GameScene.updateFog();
            return true;
        }

        //can't swap places if one char has restricted movement
        if (caster.isRooted || target.isRooted || caster.getBuff(Vertigo.class) != null || target.getBuff(Vertigo.class) != null){
            return true;
        }

        target.position = oldPos;
        caster.moveSprite( oldPos, newPos );
        ActionMove.moveToPosition( caster, newPos );

        target.position = newPos;
        target.sprite.move( newPos, oldPos );
        ActionMove.moveToPosition( target, oldPos );

        ActionSpendTime.spendTimeAdjusted( target,1 / ActionSpeed.getSpeed(target));

        if (target == Dungeon.hero){
            if (Dungeon.hero.subClass == HeroSubClass.FREERUNNER){
                Buff.affect(Dungeon.hero, Momentum.class).gainStack();
            }

            Dungeon.hero.busy();
        }

        return true;
    }
}
