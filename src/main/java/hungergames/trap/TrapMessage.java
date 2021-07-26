package hungergames.trap;

import hungergames.FormattedMessage;
import hungergames.game.Player;

import java.util.HashMap;
import java.util.Map;

public class TrapMessage {
    private final Player trapSetter;
    private final Trap trap;
    private final FormattedMessage message;

    public TrapMessage(FormattedMessage message, Player trapSetter, Trap trap) {
        this.trapSetter = trapSetter;
        this.trap = trap;
        this.message = message;
    }

    public String evaluate(Player victim){
        Map<String, Object> data = new HashMap<>();
        data.put("victimName", victim.getName());
        data.put("setterName", trapSetter.getName());
        data.put("trapName", trap.getName());

        return message.evaluate(data);
    }
}
