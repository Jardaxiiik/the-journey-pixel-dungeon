package com.shatteredpixel.shatteredpixeldungeon.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.Character;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * ActionThrowItems handles throwing items from heaps.
 */
public class ActionThrowItems {

    public static void throwItems(Character character){
        Heap heap = Dungeon.level.heaps.get(character.getCharacterMovement().getPosition()());
        if (heap != null && heap.type == Heap.Type.HEAP
                && !(heap.peek() instanceof Tengu.BombAbility.BombItem)
                && !(heap.peek() instanceof Tengu.ShockerAbility.ShockerItem)) {
            ArrayList<Integer> candidates = new ArrayList<>();
            for (int n : PathFinder.OFFSETS_NEIGHBOURS8){
                if (Dungeon.level.passable[character.getCharacterMovement().getPosition()() +n]){
                    candidates.add(character.getCharacterMovement().getPosition()() +n);
                }
            }
            if (!candidates.isEmpty()){
                Dungeon.level.dropItemOnPosition( heap.pickUp(), Random.element(candidates) ).sprite.drop(character.getCharacterMovement().getPosition()());
            }
        }
    }
}
