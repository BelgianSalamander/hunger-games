package hungergames.game.event;

import hungergames.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventRegistry {
    private final List<GameEvent> events;
    private final Game game;
    private final Random rand;

    public EventRegistry(Game game) {
        events = new ArrayList<>();
        this.game = game;

        rand = new Random(game.getSeed());
    }

    public void addEvent(GameEvent event){
        events.add(event);
    }

    public GameEvent getRandomEvent(){
        double totalWeight = 0;
        List<GameEvent> runnableEvents = new ArrayList<>();

        for(GameEvent event : events){
            if(event.canRun(game)){
                runnableEvents.add(event);
                totalWeight += event.getWeight();
            }
        }

        if(totalWeight == 0 || runnableEvents.size() == 0)
            return null;

        double n = rand.nextDouble() * totalWeight;
        for(GameEvent event : runnableEvents){
            n -= event.getWeight();
            if(n <= 0){
                return event;
            }
        }

        return null;
    }
}
