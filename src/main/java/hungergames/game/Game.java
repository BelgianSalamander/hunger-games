package hungergames.game;

import hungergames.MyUtil;
import hungergames.config.GameConfig;
import hungergames.game.event.BasicEvent;
import hungergames.game.event.EventRegistry;
import hungergames.game.event.GameEvent;
import hungergames.game.event.RunningCondition;
import hungergames.lists.WordLists;
import hungergames.out.MessageLevel;
import hungergames.out.Out;
import hungergames.trap.Trap;
import hungergames.weapon.Weapon;
import hungergames.weapon.WeaponMessage;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final long seed;

    private final String name;
    private final Player[] players;
    private final Set<Integer> alivePeople;
    private final List<PlayerGroup> groups;
    private final List<Trap> activeTraps = new ArrayList<>();

    private final EventRegistry dayEvents = new EventRegistry(this);
    private final EventRegistry nightEvents = new EventRegistry(this);

    static Random rand = new Random();

    public int getNumPlayers(){return players.length;}
    public int getNumAlivePlayers(){return alivePeople.size();}

    public Player getPlayer(int index){return players[index];}
    public List<PlayerGroup> getGroups(){return groups;}

    public long getSeed() {
        return seed;
    }

    public Game(GameConfig config){
        seed = config.getSeed();

        rand = new Random(seed);
        name = config.getGameName();

        addDayEvent(new BasicEvent("Group Fight", 1.0, this::teamCombat, RunningCondition.minimumGroups(2)));
        addDayEvent(new BasicEvent("Trap Placement", 0.4, this::trapPlacement, game -> {
            if(game.getNumAlivePlayers() <= 0) return false;
            if(game.alivePeople.stream().filter(player -> getPlayer(player).hasTraps()).findFirst().isEmpty()) return false;
            return true;
        }));
        addDayEvent(new BasicEvent("Trap Activation", 0.3, this::trapActivation, game -> game.activeTraps.size() != 0 && game.alivePeople.size() >= 1));

        addNightEvent(new BasicEvent("Dream", 1.0, this::dream, RunningCondition.minimumPlayers(2)));

        players = new Player[config.getPlayerConfig().size()];
        alivePeople = new TreeSet<>();
        groups = new ArrayList<>();

        for(int i = 0; i < config.getPlayerConfig().size(); i++){
            players[i] = new Player(config.getPlayerConfig().get(i));
            players[i].index = i;
            alivePeople.add(i);
        }
    }

    public void startGame(){
        createTeams();
        giveWeapons();
        listTeams();
    }

    private void dayEvent(){
        GameEvent event = dayEvents.getRandomEvent();
        if(event == null){
            Out.write("No day event could run", MessageLevel.ERROR);
            return;
        }else{
            Out.write("Running event '" + event.getName() + "'", MessageLevel.DEBUG);
        }
        event.run(this);
    }

    private void nightEvent(){
        GameEvent event = nightEvents.getRandomEvent();
        if(event == null){
            Out.write("No night event could run", MessageLevel.ERROR);
            return;
        }else{
            Out.write("Running event '" + event.getName() + "'", MessageLevel.DEBUG);
        }
        event.run(this);
    }

    private void createTeams(){
        for (Player player : players) {
            boolean foundGroup = false;
            for (PlayerGroup group : groups) {
                if (rand.nextInt((int) (players.length * 0.15 + group.people.size() * 2.0) + 2) == 0) {
                    Out.write(player.getName() + " teams up with " + group, MessageLevel.MAIN);
                    group.addPlayer(player);
                    foundGroup = true;
                    break;
                }
            }

            if (!foundGroup) {
                groups.add(new PlayerGroup(this, player));
            }
        }
    }

    private void giveWeapons(){
        int numWeaponTries = Math.min(players.length * 2, Math.max(players.length, (int) (rand.nextGaussian() * 0.2 + players.length * 1.5)));
        for(int i = 0; i < numWeaponTries; i++){
            if(rand.nextInt(5) >= 1) { //Give weapon
                Weapon weaponToAdd = Weapon.createRandom();
                Player playerToReceive = getRandomAlivePlayer();
                Out.write(playerToReceive.getName() + " found a " + weaponToAdd.getName(), MessageLevel.MAIN);
                playerToReceive.addWeapon(weaponToAdd);
            }else{ //Give trap
                Player playerToReceive = getRandomAlivePlayer();
                Trap trapToAdd = Trap.createRandom(playerToReceive);
                playerToReceive.addTrap(trapToAdd);
                Out.write(playerToReceive.getName() + " found a " + trapToAdd.getName(), MessageLevel.MAIN);
            }
        }
    }

    private void listTeams(){
        for(PlayerGroup group : groups){
            List<Integer> members = new ArrayList<Integer>(group.people);
            if(members.size() == 1){
                Out.write(players[members.get(0)] + " is alone!", MessageLevel.MAIN);
            }else if(members.size() >= 2) {
                Out.write(players[members.get(0)] + " teamed up with " + formatPlayersFromIndices(members.subList(1, members.size())), MessageLevel.MAIN);
            }else{
                Out.write("[Critical] empty team found!", MessageLevel.MAIN);
            }
        }
    }

    private void teamCombat(){
        int indexOne = rand.nextInt(groups.size());
        int indexTwo = rand.nextInt(groups.size() - 1);
        if(indexOne == indexTwo) indexTwo++;

        teamCombat(groups.get(indexOne), groups.get(indexTwo));
    }

    private void trapPlacement(){
        List<Integer> playersWithTraps = alivePeople.stream().filter(player -> getPlayer(player).hasTraps()).collect(Collectors.toUnmodifiableList());
        if(playersWithTraps.size() == 0){
            Out.write("Ran event that shouldn't have run", MessageLevel.ERROR);
        }
        Player player = getPlayer(playersWithTraps.get(rand.nextInt(playersWithTraps.size())));
        Trap trap = player.getTrap();
        activeTraps.add(trap);
        Out.write(player.getName() + " placed and armed their " + trap);
    }

    private void trapActivation(){
        int trapIndex = rand.nextInt(activeTraps.size());
        Trap activatedTrap = activeTraps.get(trapIndex);

        Player victim = null;

        double index = rand.nextDouble() * (alivePeople.size() - (alivePeople.contains(activatedTrap.getSetter().getIndex()) ? 0.75 : 0.0));
        for(Integer playerIndex : alivePeople){
            if(playerIndex == activatedTrap.getSetter().getIndex()){
                index -= 0.25;
            }else{
                index -= 1.0;
            }

            if(index <= 0){
                victim = getPlayer(playerIndex);
            }
        }

        if(rand.nextDouble() < (activatedTrap.getChanceOfKilling() / victim.getIntelligence())){//Trap has killed victim
            killPlayer(victim);
            Out.write(activatedTrap.getKillMessage().evaluate(victim), MessageLevel.MAIN);
        }else if(rand.nextDouble() < activatedTrap.getChanceOfDamaging() / victim.getIntelligence()) {//Trap has damaged victim
            victim.multiplyStrength(activatedTrap.getDamage());
            Out.write(activatedTrap.getDamageMessage().evaluate(victim), MessageLevel.MAIN);
        }else{//Player escaped unscathed
            Out.write(activatedTrap.getMissMessage().evaluate(victim), MessageLevel.MAIN);
        }
    }

    private void dream(){
        int indexOne = rand.nextInt(alivePeople.size());
        int indexTwo = rand.nextInt(alivePeople.size() - 1);

        Player dreamer = null;
        Player inDream = null;

        int i = 0;
        for(int playerIndex : alivePeople){
            if(i == indexOne)
                dreamer = getPlayer(playerIndex);

            if(i == indexTwo)
                inDream = getPlayer(playerIndex);

            i++;
        }

        if(indexOne == indexTwo) indexTwo++;

        Map<String, Object> dreamData = new HashMap<>();
        dreamData.put("dreamer", dreamer);
        dreamData.put("inDream", inDream);

        Out.write(WordLists.getList("dream").select().evaluate(dreamData), MessageLevel.MAIN);
    }

    private void teamCombat(PlayerGroup teamOne, PlayerGroup teamTwo){
        Set<Integer> fled = new TreeSet<>();

        Out.write(teamOne + " enters in combat with " + teamTwo, MessageLevel.MAIN);

        while(isTeamStillThere(teamOne, fled) && isTeamStillThere(teamTwo, fled)){
            if(rand.nextDouble() < 0.4){ //Member flees
                if(rand.nextBoolean()){
                    int fleeingMember = teamOne.getRandomPlayer(fled);
                    Player fleeing = players[fleeingMember];
                    Out.write(fleeing + " tries to run away from combat!", MessageLevel.MAIN);
                    fled.add(fleeingMember);
                    playerAttack(players[teamTwo.getRandomPlayer()], fleeing, 0.0);
                }else{
                    int fleeingMember = teamTwo.getRandomPlayer(fled);
                    Player fleeing = players[fleeingMember];
                    Out.write(fleeing + " tries to run away from combat!", MessageLevel.MAIN);
                    fled.add(fleeingMember);
                    playerAttack(players[teamOne.getRandomPlayer()], fleeing, 0.0);
                }
            }else{
                doCombatBetween(players[teamOne.getRandomPlayer(fled)], players[teamTwo.getRandomPlayer(fled)]);
            }
        }
    }



    private boolean isTeamStillThere(PlayerGroup team, Set<Integer> fled){
        return !fled.containsAll(team.people);
    }

    private void doCombatBetween(Player playerOne, Player playerTwo){
        double playerOneStrength = playerOne.getCombatStrength();
        double playerTwoStrength = playerTwo.getCombatStrength();

        double n = rand.nextDouble() * (playerOneStrength + playerTwoStrength + 2);

        if(n < playerTwoStrength){
            killPlayer(playerOne);
            WeaponMessage message = playerTwo.getHitMessage();
            Out.write(message.evaluate(playerOne), MessageLevel.MAIN);
        }else if(n < playerOneStrength + playerTwoStrength){
            killPlayer(playerTwo);
            WeaponMessage message = playerOne.getHitMessage();
            Out.write(message.evaluate(playerTwo), MessageLevel.MAIN);
        }else{
            if(rand.nextBoolean()){
                WeaponMessage message = playerOne.getMissMessage();
                Out.write(message.evaluate(playerTwo), MessageLevel.MAIN);
            }else{
                WeaponMessage message = playerTwo.getMissMessage();
                Out.write(message.evaluate(playerOne), MessageLevel.MAIN);
            }
        }
    }

    private void playerAttack(Player attacker, Player victim, double victimAdvantage){
        double attackerStrength = attacker.getCombatStrength();
        double victimStrength = victim.getCombatStrength() + victimAdvantage;

        double n = rand.nextDouble() * (attackerStrength + victimStrength);
        if(n < attackerStrength){
            killPlayer(victim);
            WeaponMessage message = attacker.getHitMessage();
            Out.write(message.evaluate(victim), MessageLevel.MAIN);
        }else{
            WeaponMessage message = attacker.getMissMessage();
            Out.write(message.evaluate(victim), MessageLevel.MAIN);
        }
    }

    private void killPlayer(Player player){
        player.getGroup().removePlayer(player);
        alivePeople.remove(player.index);
        player.setAlive(false);
    }

    private Player getRandomAlivePlayer(){
        int index = rand.nextInt(alivePeople.size());
        int i = 0;
        for(Integer playerIndex : alivePeople){
            if(index == i){
                return players[playerIndex];
            }
            i++;
        }
        return null;
    }

    public void printPlayers(){
        for(Player player : players){
            Out.write(MyUtil.getInstanceInfo(player), MessageLevel.MAIN);
        }
    }

    public String formatPlayers(Collection<Player> players){
        if(players.size() == 0){
            return "no one";
        }else if(players.size() == 1){
            return players.iterator().next().toString();
        }else if(players.size() == 2){
            Iterator<Player> iter = players.iterator();
            return iter.next() + " and " + iter.next();
        }else{
            Iterator<Player> iter = players.iterator();
            StringBuilder data = new StringBuilder();
            for(int i = 0; i < players.size() - 2; i++){
                data.append(iter.next()).append(", ");
            }
            data.append(iter.next()).append(" and ").append(iter.next());
            return data.toString();
        }
    }

    public String formatPlayersFromIndices(Collection<Integer> indices){
        if(indices.size() == 0){
            return "no one";
        }else if(indices.size() == 1){
            return getPlayer(indices.iterator().next()).toString();
        }else if(indices.size() == 2){
            Iterator<Integer> iter = indices.iterator();
            return getPlayer(iter.next()) + " and " + getPlayer(iter.next());
        }else{
            Iterator<Integer> iter = indices.iterator();
            String data = "";
            for(int i = 0; i < indices.size() - 2; i++){
                data += getPlayer(iter.next())+ ", ";
            }
            data += getPlayer(iter.next()) + " and " + getPlayer(iter.next());
            return data;
        }
    }

    void removeGroup(PlayerGroup group){
        groups.remove(group);
    }

    public void addDayEvent(GameEvent event){
        dayEvents.addEvent(event);

        Out.write("Registered day event '" + event.getName() + "'", MessageLevel.DEBUG);
    }

    public void addNightEvent(GameEvent event){
        nightEvents.addEvent(event);

        Out.write("Registered night event '" + event.getName() + "'", MessageLevel.DEBUG);
    }

    public void runGame(){
        int day = 1;
        while(!gameEnded()){
            if(day == 100){
                Out.write("Reached 100th day", MessageLevel.WARNING);
            }

            int numDayEvents = (int) Math.max(3, rand.nextGaussian() * 0.4 + alivePeople.size() * 0.4 + 1);
            Out.write("--------Day " + day + "--------");
            for(int i = 0; i < numDayEvents; i++){
                dayEvent();
                if(gameEnded()) break;
                splitTeamsIfNeeded();
            }

            if(gameEnded()) break;

            Out.write("--------Night " + day + "--------");
            int numNightEvents = (int) Math.max(3, rand.nextGaussian() * 0.1 + alivePeople.size() + 1);
            for(int i = 0; i < numNightEvents; i++){
                nightEvent();
                if(gameEnded()) break;
                splitTeamsIfNeeded();
            }

            day++;
        }

        Out.write(formatPlayersFromIndices(alivePeople) + " wins the " + name);
    }

    private void splitTeamsIfNeeded(){
        if(!gameEnded() && groups.size() == 1){
            PlayerGroup onlyGroup = groups.get(0);
            PlayerGroup newGroup = new PlayerGroup(this);
            groups.add(newGroup);

            List<Integer> playersMoving = new ArrayList<>();

            int i = -1;
            for(int playerIndex : onlyGroup.people){
                i++;

                if(i == 0) continue;
                if(i == 1){
                    playersMoving.add(playerIndex);
                    continue;
                }

                if(rand.nextBoolean()){
                    playersMoving.add(playerIndex);
                }
            }

            for(int movingPlayerIndex : playersMoving){
                newGroup.addPlayer(getPlayer(movingPlayerIndex));
            }

            Out.write("Split teams", MessageLevel.DEBUG);
            for(PlayerGroup group : groups){
                Out.write(group.people, MessageLevel.DEBUG);
            }
        }
    }

    private boolean gameEnded() {
        return alivePeople.size() <= 1;
    }
}
