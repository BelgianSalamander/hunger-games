package hungergames;

import hungergames.game.Player;
import hungergames.lists.WordList;
import hungergames.lists.WordLists;
import hungergames.weapon.Weapon;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* Parameters allowed:
 *   - attackerName
 *   - defenderName
 *   - weaponName
 *   - select({listName})
 */
public class FormattedMessage {
    private List<MessageComponent> components;

    public FormattedMessage(String template){
        components = new ArrayList<>();

        int startIndex = 0;
        int currentIndex = 0;

        while(currentIndex < template.length()){
            while (currentIndex < template.length()){
                if(template.charAt(currentIndex) == '{'){
                    break;
                }
                currentIndex++;
            }

            if(currentIndex > startIndex)
                components.add(new TextComponent(template.substring(startIndex, currentIndex)));
            startIndex = currentIndex;

            if(currentIndex >= template.length()) break;

            if(template.charAt(currentIndex) == '{'){
                while (currentIndex < template.length()){
                    if(template.charAt(currentIndex) == '}'){
                        break;
                    }
                    currentIndex++;
                }

                if(template.charAt(currentIndex) == '}'){
                    String value = template.substring(startIndex + 1, currentIndex); //Remove curly braces
                    if(value.startsWith("select(")){
                        components.add(new SelectorComponent(value.substring(7, value.length() - 1)));
                    }else{
                        components.add(new VariableComponent(value));
                    }
                }

                startIndex = ++currentIndex;
            }
        }
    }

    public String evaluate(Map<String, Object> data){
        String message = "";
        for(MessageComponent component : components){
            message += component.evaluate(data);
        }
        return message;
    }

    public String evaluate(){
        return evaluate(new HashMap<>());
    }

    private static interface MessageComponent{
        public String evaluate(Map<String, Object> data);
    }

    private static class TextComponent implements MessageComponent{
        private final String text;

        public TextComponent(String text){this.text = text;}

        @Override
        public String evaluate(Map<String, Object> data) {
            return text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Override
    public String toString() {
        return components.stream().map(MessageComponent::toString).collect(Collectors.joining());
    }

    private static class VariableComponent implements MessageComponent{
        private final String wanted;

        public VariableComponent(@NotNull String wantedVar){
            wanted = wantedVar;
        }

        @Override
        public String evaluate(Map<String, Object> data) {
            Object wantedInfo = data.get(wanted);
            if(wantedInfo == null){
                return "['" + wanted + "' not provided]";
            }
            return data.get(wanted).toString();
        }

        @Override
        public String toString() {
            return "[Variable " + wanted + "]";
        }
    }

    private static class SelectorComponent implements MessageComponent{
        WordList list;
        String wanted;

        public SelectorComponent(String listName){
            wanted = listName;
            list = WordLists.getList(listName);
        }

        @Override
        public String evaluate(Map<String, Object> data) {
            if(list == null)
                list = WordLists.getList(wanted);
            if(list == null){
                return "[Invalid list " + wanted + "]";
            }else{
                return list.select().evaluate(data);
            }
        }

        @Override
        public String toString() {
            return "[Selector " + wanted + "]";
        }
    }
}
