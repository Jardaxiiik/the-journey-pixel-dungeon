package com.shatteredpixel.shatteredpixeldungeon.actors.characters;

import com.shatteredpixel.shatteredpixeldungeon.actions.ActionBuffs;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.actorLoop.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.characters.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonActorsHandler;
import com.shatteredpixel.shatteredpixeldungeon.dungeon.DungeonCharactersHandler;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ShockingDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class CharacterWithStates extends CharacterWithStats{
    // BUFFS
    public LinkedHashSet<Buff> buffs = new LinkedHashSet<>(); // they just are
    public LinkedHashSet<Buff> activeBuffs = new LinkedHashSet<>(); // they actively do something
    public final HashSet<Class> resistances = new HashSet<>();
    public final HashSet<Class> immunities = new HashSet<>();
    public HashSet<Character.Property> properties = new HashSet<>();

    protected static final String BUNDLE_BUFFS = "buffs";
    @Override
    public synchronized void onRemove() {
        for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
            buff.detach();
        }
    }

    @SuppressWarnings("unchecked")
    //returns all buffs assignable from the given buff class
    public synchronized <T extends Buff> HashSet<T> getBuffs(Class<T> c ) {
        HashSet<T> filtered = new HashSet<>();
        for (Buff b : buffs) {
            if (c.isInstance( b )) {
                filtered.add( (T)b );
            }
        }
        return filtered;
    }

    @SuppressWarnings("unchecked")
    //returns an instance of the specific buff class, if it exists. Not just assignable
    public synchronized  <T extends Buff> T getBuff(Class<T> c ) {
        for (Buff b : buffs) {
            if (b.getClass() == c) {
                return (T)b;
            }
        }
        return null;
    }

    //returns percent effectiveness after resistances
    //TODO currently resistances reduce effectiveness by a static 50%, and do not stack.
    public float getResistanceMultiplierToEffectType(Class effect ){
        HashSet<Class> resists = new HashSet<>(resistances);
        for (Property p : getProperties()){
            resists.addAll(p.getResistances());
        }
        for (Buff b : buffs){
            resists.addAll(b.resistances());
        }

        float result = 1f;
        for (Class c : resists){
            if (c.isAssignableFrom(effect)){
                result *= 0.5f;
            }
        }
        return result * RingOfElements.resist(this, effect);
    }

    public boolean isImmuneToEffectType(Class effect ){
        HashSet<Class> immunes = new HashSet<>(immunities);
        for (Property p : getProperties()){
            immunes.addAll(p.getImmunities());
        }
        for (Buff b : buffs){
            immunes.addAll(b.immunities());
        }

        for (Class c : immunes){
            if (c.isAssignableFrom(effect)){
                return true;
            }
        }
        return false;
    }
    //similar to isImmune, but only factors in damage.
    //Is used in AI decision-making

    public boolean isInvulnerableToEffectType(Class effect ){
        return getBuff(Challenge.SpectatorFreeze.class) != null;
    }

    // PROPERTIES
    public HashSet<Property> getProperties() {
        HashSet<Property> props = new HashSet<>(properties);
        //TODO any more of these and we should make it a property of the buff, like with resistances/immunities
        if (getBuff(ChampionEnemy.Giant.class) != null) {
            props.add(Property.LARGE);
        }
        return props;
    }

    public enum Property{
        BOSS ( new HashSet<Class>( Arrays.asList(Grim.class, GrimTrap.class, ScrollOfRetribution.class, ScrollOfPsionicBlast.class)),
                new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
        MINIBOSS ( new HashSet<Class>(),
                new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
        BOSS_MINION,
        UNDEAD,
        DEMONIC,
        INORGANIC ( new HashSet<Class>(),
                new HashSet<Class>( Arrays.asList(Bleeding.class, ToxicGas.class, Poison.class) )),
        FIERY ( new HashSet<Class>( Arrays.asList(WandOfFireblast.class, Elemental.FireElemental.class)),
                new HashSet<Class>( Arrays.asList(Burning.class, Blazing.class))),
        ICY ( new HashSet<Class>( Arrays.asList(WandOfFrost.class, Elemental.FrostElemental.class)),
                new HashSet<Class>( Arrays.asList(Frost.class, Chill.class))),
        ACIDIC ( new HashSet<Class>( Arrays.asList(Corrosion.class)),
                new HashSet<Class>( Arrays.asList(Ooze.class))),
        ELECTRIC ( new HashSet<Class>( Arrays.asList(WandOfLightning.class, Shocking.class, Potential.class, Electricity.class, ShockingDart.class, Elemental.ShockElemental.class )),
                new HashSet<Class>()),
        LARGE,
        IMMOVABLE;

        private HashSet<Class> resistances;

        private HashSet<Class> immunities;
        Property(){
            this(new HashSet<Class>(), new HashSet<Class>());
        }

        Property( HashSet<Class> resistances, HashSet<Class> immunities){
            this.resistances = resistances;
            this.immunities = immunities;
        }

        public HashSet<Class> getResistances(){
            return new HashSet<>(resistances);
        }

        public HashSet<Class> getImmunities(){
            return new HashSet<>(immunities);
        }


    }
    public static boolean hasProperty(Character ch, Property p){
        return (ch != null && ch.getProperties().contains(p));
    }

}
