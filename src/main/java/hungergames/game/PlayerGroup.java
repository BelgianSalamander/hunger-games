package hungergames.game;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class PlayerGroup{
    final Set<Integer> people;
    private final Game game;

    public PlayerGroup(Game game){
        people = new TreeSet<>();

        this.game = game;
    }

    public PlayerGroup(Game game, Player firstMember){
        people = new TreeSet<>();

        this.game = game;

        addPlayer(firstMember);
    }

    public void addPlayer(Player player){
        if(player.getGroup() != null){
            player.getGroup().removePlayer(player);
        }

        player.setGroup(this);
        people.add(player.index);
    }

    public void removePlayer(Player player){
        player.setGroup(null);
        people.remove(player.index);

        if(people.size() == 0){
            game.removeGroup(this);
        }
    }

    public int getRandomPlayer(){
        int i = Game.rand.nextInt(people.size());
        int j = 0;
        for(Integer index : people){
            if(j == i){
                return index;
            }
            j++;
        }
        return -1;
    }

    public int getRandomPlayer(Collection<Integer> removed){
        TreeSet<Integer> temp = new TreeSet<>(people);
        temp.removeAll(removed);
        int i = Game.rand.nextInt(temp.size());
        int j = 0;
        for(Integer index : temp){
            if(j == i){
                return index;
            }
            j++;
        }
        return -1;
    }

    @Override
    public String toString() {
        return game.formatPlayersFromIndices(people);
    }


}
