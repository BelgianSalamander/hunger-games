package hungergames.weapon;

import hungergames.FormattedMessage;
import hungergames.game.Player;

import java.util.HashMap;
import java.util.Map;

public class WeaponMessage {
    private final Map<String, Object> names;
    private final FormattedMessage message;

    public WeaponMessage(Player attacker, Weapon weapon, FormattedMessage message) {
        names = new HashMap<>();
        names.put("attackerName", attacker.getName());
        names.put("weaponName", weapon.getName());

        this.message = message;
    }

    public String evaluate(Player victim){
        names.put("victimName", victim.getName());
        return message.evaluate(names);
    }
}
