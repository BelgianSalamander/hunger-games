package hungergames.game.event;

import hungergames.game.Game;

public interface GameEvent {
    double getWeight();
    String getName();

    boolean canRun(Game game);
    void run(Game game);
}
