package hungergames.game;

import hungergames.config.PlayerConfig;
import hungergames.out.MessageLevel;
import hungergames.out.Out;
import hungergames.trap.Trap;
import hungergames.weapon.Weapon;
import hungergames.FormattedMessage;
import hungergames.weapon.WeaponMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {
    private final String name;

    private boolean alive = true;

    private double strength = 1.0;
    private double meleeSkill;
    private double rangedSkill;
    private double intelligence;

    private List<Weapon> weapons;
    private List<Trap> traps;

    private static int instances = 0;
    private static Random random = new Random();
    private static final int maxWeapons = 5;

    private static final double defaultMelee = 0.5;
    private static final FormattedMessage[] defaultHitMessages = new FormattedMessage[]{
            new FormattedMessage("{attackerName} {select(hands)} {victimName} to death"),
            new FormattedMessage("{victimName} got {select(hands)} by {attackerName} and died")
    };
    private static final FormattedMessage[] defaultMissMessages = new FormattedMessage[]{
            new FormattedMessage("{attackerName} tried {select(hands_infinitive)} {victimName}")
    };
    private static final Weapon defaultWeapon = new Weapon("fists");

    public void multiplyStrength(double factor){
        strength *= factor;
    }

    public double getMeleeSkill() {
        return meleeSkill;
    }

    public double getRangedSkill() {
        return rangedSkill;
    }

    public double getIntelligence() {
        return intelligence;
    }

    public int getIndex() {
        return index;
    }

    //Game Specific Data
    int index = -1;

    public PlayerGroup getGroup() {
        return group;
    }

    public void setGroup(PlayerGroup group) {
        this.group = group;
    }

    private PlayerGroup group = null;

    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean b){alive = b;}

    public boolean hasTraps(){
        return traps.size() > 0;
    }

    public String getName() {
        return name;
    }

    public Player(PlayerConfig config){
        instances++;

        name = config.getName() == null ? getDefaultName() : config.getName();
        meleeSkill = config.getMeleeSkill() == null ? getDefaultMeleeSkill() : config.getMeleeSkill();
        rangedSkill = config.getRangedSkill() == null ? getDefaultRangedSkill() : config.getRangedSkill();
        intelligence = config.getIntelligence() == null ? getDefaultIntelligence() : config.getIntelligence();

        weapons = new ArrayList<>();
        traps = new ArrayList<>();
    }

    public double getCombatStrength(){
        double melee = defaultMelee;
        double ranged = 0;
        double intelligence = 0;

        for(Weapon weapon : weapons){
            melee += weapon.getMeleeModifier();
            ranged += weapon.getRangedModifier();
            intelligence += weapon.getIntelligenceModifier();
        }

        melee = Math.sqrt(melee * meleeSkill);
        ranged = Math.sqrt(ranged * rangedSkill);
        intelligence = Math.sqrt(intelligence * this.intelligence);

        return (melee + ranged + intelligence) * strength;
    }

    private double getTheoreticalStrength(int exclude, Weapon newWeapon){
        double melee = newWeapon.getMeleeModifier() + defaultMelee;
        double ranged = newWeapon.getRangedModifier();
        double intelligence = newWeapon.getIntelligenceModifier();

        for(int i = 0; i < weapons.size(); i++){
            if(i == exclude) continue;
            melee += weapons.get(i).getMeleeModifier();
            ranged += weapons.get(i).getRangedModifier();
            intelligence += weapons.get(i).getIntelligenceModifier();
        }

        melee = Math.sqrt(melee * meleeSkill);
        ranged = Math.sqrt(ranged * rangedSkill);
        intelligence = Math.sqrt(intelligence * this.intelligence);

        return (melee + ranged + intelligence) * strength;
    }

    public void addWeapon(Weapon weapon){
        if(weapons.size() < maxWeapons){
            weapons.add(weapon);
        }else{
            double currentCombatStrength = getCombatStrength();

            double bestStrength = 0.0;
            int bestIndex = -1;
            for(int i = 0; i < maxWeapons; i++){
                double strengthWithout = getTheoreticalStrength(i, weapon);
                if(strengthWithout > bestStrength){
                    bestIndex = i;
                    bestStrength = strengthWithout;
                }
            }

            if(currentCombatStrength > bestStrength){
                Out.write(name + " could not pick up " + weapon.getName() + ", he already had " + maxWeapons + " weapons!", MessageLevel.MAIN);
            }else{
                Weapon removedWeapon = weapons.get(bestIndex);
                weapons.remove(bestIndex);
                Out.write(name + " dropped their " + removedWeapon.getName() + " to pick up a " + weapon.getName(), MessageLevel.MAIN);
            }
        }
    }

    public void addTrap(Trap trap){
        traps.add(trap);
    }

    public Trap getTrap(){
        assert traps.size() > 0;
        int trapIndex = random.nextInt(traps.size());
        Trap trap = traps.get(trapIndex);
        traps.remove(trapIndex);
        return trap;
    }

    public WeaponMessage getHitMessage(){
        if (weapons.size() == 0) {
            return new WeaponMessage(this, defaultWeapon, defaultHitMessages[random.nextInt(defaultHitMessages.length)]);
        }else{
            Weapon chosenWeapon = weapons.get(random.nextInt(weapons.size()));
            return new WeaponMessage(this, chosenWeapon, chosenWeapon.getHitMessage());
        }
    }
    public WeaponMessage getMissMessage(){
        if(weapons.size() == 0){
            return new WeaponMessage(this, defaultWeapon, defaultMissMessages[random.nextInt(defaultMissMessages.length)]);
        }else{
            Weapon chosenWeapon = weapons.get(random.nextInt(weapons.size()));
            return new WeaponMessage(this, chosenWeapon, chosenWeapon.getMissMessage());
        }
    }

    static private String getDefaultName(){return "Player " + instances;}
    static private double getDefaultMeleeSkill(){return getDefaultSkill();}
    static private double getDefaultRangedSkill(){return getDefaultSkill();}
    static private double getDefaultIntelligence(){return getDefaultSkill();}

    static private double getDefaultSkill(){
        return Math.max(0.1, random.nextGaussian() * 0.4 + 1);
    }

    static public void seed(long seed){random.setSeed(seed);}

    @Override
    public String toString() {
        return name;
    }
}
