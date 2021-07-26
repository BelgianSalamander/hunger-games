package hungergames.game.event;

import hungergames.game.Game;
import hungergames.out.MessageLevel;
import hungergames.out.Out;

public class BasicEvent implements GameEvent{
    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canRun(Game game) {
        boolean can = condition.canRun(game);
        if(!can){
            Out.write("Cannot run event '" + name + "'", MessageLevel.INFO);
        }
        return can;
    }

    @Override
    public void run(Game game) {
        method.run();
    }

    @FunctionalInterface
    public interface EventMethod{
        public void run();
    }

    private final EventMethod method;
    private final double weight;
    private final String name;
    private final RunningCondition condition;

    public BasicEvent(String name, double weight, EventMethod method, RunningCondition condition){
        this.name = name;
        this.weight = weight;
        this.method = method;
        this.condition = condition;
    }


}
