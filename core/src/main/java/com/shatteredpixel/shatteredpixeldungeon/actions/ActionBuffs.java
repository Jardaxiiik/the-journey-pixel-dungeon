package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

public class ActionBuffs {

    public static boolean addBuff(Character character, Buff buff) {
        if (character.getBuff(PotionOfCleansing.Cleanse.class) != null) { //cleansing buff
            if (buff.type == Buff.buffType.NEGATIVE
                    && !(buff instanceof AllyBuff)
                    && !(buff instanceof LostInventory)){
                return false;
            }
        }

        if (character.sprite != null && character.getBuff(Challenge.SpectatorFreeze.class) != null){
            return false; //can't add buffs while frozen and game is loaded
        }

        character.buffs.add( buff );
        if (DungeonCharactersHandler.getCharacters().contains(character)) DungeonActorsHandler.addActor( buff );

        if (character.sprite != null && buff.announced) {
            switch (buff.type) {
                case POSITIVE:
                    character.sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(buff.name()));
                    break;
                case NEGATIVE:
                    character.sprite.showStatus(CharSprite.WARNING, Messages.titleCase(buff.name()));
                    break;
                case NEUTRAL:
                default:
                    character.sprite.showStatus(CharSprite.NEUTRAL, Messages.titleCase(buff.name()));
                    break;
            }
        }

        return true;

    }

    public static boolean removeBuff(Character character, Buff buff ) {
        character.buffs.remove( buff );
        DungeonActorsHandler.removeActor( buff );

        return true;
    }

    public static void removeBuff(Character character, Class<? extends Buff> buffClass ) {
        for (Buff buff : character.getBuffs( buffClass )) {
            removeBuff( character, buff );
        }
    }

    public static void onRemove(Character character) {
        for (Buff buff : character.buffs.toArray(new Buff[character.buffs.size()])) {
            buff.detach();
        }
    }

    public static synchronized boolean isCharmedBy( Character target, Character source ) {
        int chID = source.getId();
        for (Buff b : target.buffs) {
            if (b instanceof Charm && ((Charm)b).object == chID) {
                return true;
            }
        }
        return false;
    }

}
