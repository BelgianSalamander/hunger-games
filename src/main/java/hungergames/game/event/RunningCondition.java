package hungergames.game.event;

import hungergames.game.Game;

@FunctionalInterface
public interface RunningCondition {
    public boolean canRun(Game game);

    static RunningCondition minimumPlayers(int minimum){
        assert minimum >= 0;
        return new MinimumPlayersCondition(minimum);
    }

    static RunningCondition minimumGroups(int minimum){
        assert minimum >= 0;
        return new MinimumGroupsCondition(minimum);
    }
}

class MinimumPlayersCondition implements RunningCondition{
    private final int minimum;

    MinimumPlayersCondition(int minimum){
        this.minimum = minimum;
    }

    @Override
    public boolean canRun(Game game) {
        return game.getNumAlivePlayers() >= minimum;
    }
}

class MinimumGroupsCondition implements RunningCondition{
    private final int minimum;

    MinimumGroupsCondition(int minimum){
        this.minimum = minimum;
    }

    @Override
    public boolean canRun(Game game) {
        return game.getGroups().size() >= minimum;
    }
}

class Unconditional implements RunningCondition{
    @Override
    public boolean canRun(Game game){return true;}
}
