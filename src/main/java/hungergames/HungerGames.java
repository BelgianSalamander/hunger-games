package hungergames;

import hungergames.config.GameConfig;
import hungergames.game.Game;
import hungergames.game.Player;
import hungergames.lists.WordList;
import hungergames.out.MessageLevel;
import hungergames.out.Out;
import hungergames.trap.Trap;
import hungergames.weapon.Weapon;

import java.io.*;

public class HungerGames {
    public static void main(String[] args) {
        GameConfig config = loadConfig();

        if(config == null){
            Out.write("Could not load config file!", MessageLevel.FATAL);
            return;
        }

        seed(config.getSeed());

        Game game = new Game(config);
        game.startGame();
        game.runGame();
    }

    private static GameConfig loadConfig(){
        GameConfig config;
        try {
            config = new GameConfig("game.json");
        } catch (IOException e) {
            Out.write("Could not read config file", MessageLevel.FATAL);
            Out.write(e.getMessage(), MessageLevel.FATAL);
            return null;
        }
        return config;
    }

    private static void seed(long s){
        Out.write("Using seed " + s, MessageLevel.INFO);
        Player.seed(s);
        WordList.seed(s);
        Weapon.seed(s);
        Trap.setSeed(s);
    }
}
