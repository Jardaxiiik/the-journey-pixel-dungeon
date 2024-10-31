package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Speed;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;

/**
 * ActionSpendTime handles Character's time while performing actions.
 */
public class ActionSpendTime {
    public static void makeCharacterSpendTime(Character character, float time) {
        ActionSpendTime.spendTime(character,time);
        for (Buff b : character.buffs){
            b.spendTime(time);
        }
    }

    public static void spendTime(Character character, float time) {
        TimekeepersHourglass.timeFreeze freeze = character.getBuff(TimekeepersHourglass.timeFreeze.class);
        if (freeze != null) {
            freeze.processTime(time);
            return;
        }

        Swiftthistle.TimeBubble bubble = character.getBuff(Swiftthistle.TimeBubble.class);
        if (bubble != null){
            bubble.processTime(time);
            return;
        }

        ActionSpendTime.spendTime(character,time);
    }

    protected static void spendTimeAdjusted(Character character, float time ) {
        float timeScale = 1f;
        if (character.getBuff( Slow.class ) != null) {
            timeScale *= 0.5f;
            //slowed and chilled do not stack
        } else if (character.getBuff( Chill.class ) != null) {
            timeScale *= character.getBuff( Chill.class ).speedFactor();
        }
        if (character.getBuff( Speed.class ) != null) {
            timeScale *= 2.0f;
        }

        ActionSpendTime.spendTimeAdjusted(character, time / timeScale );
    }

    public static boolean isActive(Character character) {
        return ActionHealth.isAlive(character);
    }

}
