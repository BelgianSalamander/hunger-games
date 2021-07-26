package hungergames.trap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hungergames.FormattedMessage;
import hungergames.MyUtil;
import hungergames.game.Player;
import hungergames.out.MessageLevel;
import hungergames.out.Out;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Trap {
    private final Player setter;
    private final String name;
    private final double deathProb;
    private final double damageProb;
    private final double minimumDamage;
    private final double damageRange;
    private final FormattedMessage[] killMessages, damageMessages, missMessages;

    private static final Random random = new Random();

    public Player getSetter() {
        return setter;
    }

    public String getName() {
        return name;
    }

    public double getDeathProb() {
        return deathProb;
    }

    public double getDamageProb() {
        return damageProb;
    }

    public double getMinimumDamage() {
        return minimumDamage;
    }

    public double getDamageRange() {
        return damageRange;
    }

    private Trap(Player setter, String name, double deathProb, double damageProb, double minimumDamage, double damageRange, FormattedMessage[] killMessages, FormattedMessage[] damageMessages, FormattedMessage[] missMessages) {
        this.setter = setter;
        this.name = name;
        this.deathProb = deathProb;
        this.damageProb = damageProb;
        this.minimumDamage = minimumDamage;
        this.damageRange = damageRange;
        this.killMessages = killMessages;
        this.damageMessages = damageMessages;
        this.missMessages = missMessages;
    }

    public static Trap createRandom(Player setter){
        double n = random.nextDouble() * totalWeight;
        for(TrapTemplate template : trapTemplates){
            n -= template.getWeight();
            if(n <= 0){
                return template.generate(setter);
            }
        }
        return null;
    }

    private static class TrapTemplate{
        private final String[] names;
        private final double deathProb;
        private final double damageProb;
        private final double minimumDamage;
        private final double damageRange;
        private final FormattedMessage[] killMessages, damageMessages, missMessages;

        private final double weight;

        public TrapTemplate(String[] names, double deathProb, double damageProb, double minimumDamage, double damageRange, double weight, FormattedMessage[] killMessages, FormattedMessage[] damageMessages, FormattedMessage[] missMessages) {
            this.names = names;
            this.deathProb = deathProb;
            this.damageProb = damageProb;
            this.minimumDamage = minimumDamage;
            this.damageRange = damageRange;
            this.weight = weight;
            this.killMessages = killMessages;
            this.damageMessages = damageMessages;
            this.missMessages = missMessages;
        }

        public TrapTemplate(JsonNode json){
            String[] names = MyUtil.streamFromJsonNode(json).map(Object::toString).toArray(String[]::new);

            minimumDamage = json.get("damage_range").get(0).asDouble();
            double maximumDamage = json.get("damage_range").get(1).asDouble();

            this.names = names;
            deathProb = json.get("death_prob").asDouble();
            damageProb = json.get("damage_prob").asDouble();
            this.damageRange = maximumDamage - minimumDamage;
            this.weight = json.get("weight").asDouble();

            this.killMessages = MyUtil.streamFromJsonNode(json.get("kill")).map(s -> new FormattedMessage(s.asText())).toArray(FormattedMessage[]::new);
            this.damageMessages = MyUtil.streamFromJsonNode(json.get("damage")).map(s -> new FormattedMessage(s.asText())).toArray(FormattedMessage[]::new);
            this.missMessages = MyUtil.streamFromJsonNode(json.get("miss")).map(s -> new FormattedMessage(s.asText())).toArray(FormattedMessage[]::new);
        }

        public double getWeight(){return weight;}

        public Trap generate(Player setter){
            return new Trap(setter, names[random.nextInt(names.length)], deathProb, damageProb, minimumDamage, damageRange, killMessages, damageMessages, missMessages);
        }
    }

    static public void setSeed(long seed){
        random.setSeed(seed);
    }

    private static final List<TrapTemplate> trapTemplates;
    private static final double totalWeight;

    static {
        String trapsAsString = MyUtil.getResource("traps.json");
        JsonNode root = null;
        try {
            root = new ObjectMapper().readTree(trapsAsString);
        } catch (IOException e) {
            Out.write("Could not parse traps file", MessageLevel.FATAL);
            Out.write(e.getMessage(), MessageLevel.FATAL);
        }

        trapTemplates = (List<TrapTemplate>)
                MyUtil.streamFromJsonNode(root)
                        .map(obj -> new TrapTemplate(obj))
                        .collect(Collectors.toList());
        totalWeight = trapTemplates.stream().mapToDouble(TrapTemplate::getWeight).sum();
    }

    public double getChanceOfKilling(){
        return setter.getIntelligence() * deathProb;
    }

    public double getChanceOfDamaging(){
        return setter.getIntelligence() * damageProb;
    }

    public double getDamage(){
        return 1 - Math.min(random.nextDouble() * damageRange + minimumDamage, 1);
    }

    public TrapMessage getKillMessage(){
        return new TrapMessage(killMessages[random.nextInt(killMessages.length)], setter, this);
    }

    public TrapMessage getDamageMessage(){
        return new TrapMessage(damageMessages[random.nextInt(damageMessages.length)], setter, this);
    }

    public TrapMessage getMissMessage(){
        return new TrapMessage(missMessages[random.nextInt(missMessages.length)], setter, this);
    }

    @Override
    public String toString() {
        return name;
    }
}