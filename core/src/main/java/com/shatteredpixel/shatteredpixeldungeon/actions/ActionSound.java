package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.watabou.noosa.audio.Sample;

public class ActionSound {
    public static void playHitSound(Character character, float pitch ){
        Sample.INSTANCE.play(Assets.Sounds.HIT, 1, pitch);
    }

    public static boolean willBlockSound(Character character,   float pitch ) {
        return false;
    }

}
