package hungergames.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameConfig {
    private final String gameName;
    private final long seed;

    public String getGameName() {
        return gameName;
    }

    public long getSeed() {
        return seed;
    }

    public List<PlayerConfig> getPlayerConfig() {
        return playerConfig;
    }

    private List<PlayerConfig> playerConfig = new ArrayList<>();

    public GameConfig(String path) throws IOException {
        String gameData;
        gameData = Files.readString(Path.of(path));

        System.out.println(gameData);

        JsonNode root = new ObjectMapper().readTree(gameData);
        root.getNodeType();

        if(root.has("name")) gameName = root.get("name").asText();
        else gameName = "Untitled Name";

        if(root.has("seed")) seed = root.get("seed").asLong();
        else{
            Random rand = new Random();
            seed = rand.nextLong();
        }

        if(root.has("players")){
            for(JsonNode playerData : root.get("players")){
                if(playerData.getNodeType().equals(JsonNodeType.STRING)){
                    playerConfig.add(new PlayerConfig(playerData.asText(), null, null, null));
                }else {
                    String name = playerData.get("name").asText();
                    Double melee = asDouble(playerData.get("melee"));
                    Double ranged = asDouble(playerData.get("ranged"));
                    Double intelligence = asDouble(playerData.get("intelligence"));

                    playerConfig.add(new PlayerConfig(name, melee, ranged, intelligence));
                }
            }
        }else{
            for(int i = 0; i < 12; i++){
                playerConfig.add(new PlayerConfig(null, null, null, null));
            }
        }
    }

    private static Double asDouble(JsonNode node){
        if(node == null){return null;}
        return node.asDouble();
    }
}
