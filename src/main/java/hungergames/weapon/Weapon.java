package hungergames.weapon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hungergames.MyUtil;
import hungergames.FormattedMessage;
import hungergames.out.MessageLevel;
import hungergames.out.Out;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Weapon {
    private final String name;
    private final double meleeModifier;
    private final double rangedModifier;
    private final double intelligenceModifier;

    private final List<FormattedMessage> missMessages;
    private final List<FormattedMessage> hitMessages;

    private Weapon(String name, double meleeModifier, double rangedModifier, double intelligenceModifier, List<FormattedMessage> missMessages, List<FormattedMessage> hitMessages) {
        this.name = name;
        this.meleeModifier = meleeModifier;
        this.rangedModifier = rangedModifier;
        this.intelligenceModifier = intelligenceModifier;
        this.missMessages = missMessages;
        this.hitMessages = hitMessages;
    }

    public Weapon(String name){
        this.name = name;

        meleeModifier = 0.0;
        rangedModifier = 0.0;
        intelligenceModifier = 0.0;
        missMessages = null;
        hitMessages = null;
    }

    public static Weapon createRandom(){
        double n = rand.nextDouble() * totalWeight;
        for(WeaponTemplate template : weaponTemplates){
            n -= template.getWeight();
            if(n <= 0){
                return template.generateWeapon();
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public double getMeleeModifier() {
        return meleeModifier;
    }

    public double getRangedModifier() {
        return rangedModifier;
    }

    public double getIntelligenceModifier() {
        return intelligenceModifier;
    }

    public FormattedMessage getHitMessage(){
        return hitMessages.get(rand.nextInt(hitMessages.size()));
    }

    public FormattedMessage getMissMessage(){
        return missMessages.get(rand.nextInt(missMessages.size()));
    }

    public static void seed(long seed){
        rand.setSeed(seed * 4536 ^ 5464274);
    }

    private static List<WeaponTemplate> weaponTemplates;
    private static double totalWeight = 0;
    private static Random rand = new Random();

    private static class WeaponTemplate {
        private final String[] names;
        private final double meleeModifier;
        private final double rangedModifier;
        private final double intelligenceModifier;
        private final List<FormattedMessage> missMessages;
        private final List<FormattedMessage> hitMessages;
        private final double weight;

        public WeaponTemplate(String[] names, double weight, double meleeModifier, double rangedModifier, double intelligenceModifier, List<FormattedMessage> missMessages, List<FormattedMessage> hitMessages) {
            this.names = names;
            this.meleeModifier = meleeModifier;
            this.rangedModifier = rangedModifier;
            this.intelligenceModifier = intelligenceModifier;
            this.missMessages = missMessages;
            this.hitMessages = hitMessages;
            this.weight = weight;

            totalWeight += weight;
        }

        public WeaponTemplate(JsonNode json){
            names = MyUtil.streamFromJsonNode(json.get("names")).map(JsonNode::asText).toArray(String[]::new);
            meleeModifier = json.get("melee").asDouble();
            rangedModifier = json.get("ranged").asDouble();
            intelligenceModifier = json.get("intelligence").asDouble();
            weight = json.get("weight").asDouble();
            missMessages = MyUtil.streamFromJsonNode(json.get("miss")).map(text -> new FormattedMessage(text.asText())).collect(Collectors.toList());
            hitMessages = MyUtil.streamFromJsonNode(json.get("hit")).map(text -> new FormattedMessage(text.asText())).collect(Collectors.toList());

            totalWeight += weight;
        }

        Weapon generateWeapon(){
            String name = names[rand.nextInt(names.length)];
            return new Weapon(name, meleeModifier, rangedModifier, intelligenceModifier, missMessages, hitMessages);
        }

        public double getWeight(){return weight;}
    }

    static {
        String weaponsAsString = MyUtil.getResource("weapons.json");
        JsonNode root = null;
        try{
            root = new ObjectMapper().readTree(weaponsAsString);
        }
        catch (IOException e){
            Out.write(Color.RED + "Could not load weapons!", MessageLevel.FATAL);
            e.printStackTrace();
        }

        weaponTemplates = new ArrayList<>();
        double total = 0;
        for(JsonNode obj : root){
            weaponTemplates.add(new WeaponTemplate(obj));
        }

        totalWeight = total;
    }
}
