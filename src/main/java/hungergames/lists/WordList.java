package hungergames.lists;

import hungergames.FormattedMessage;

import java.util.Random;

public class WordList {
    private final FormattedMessage[] messages;
    static Random rand = new Random();

    public WordList(FormattedMessage[] messages) {
        this.messages = messages;
    }

    public FormattedMessage select(){
        return messages[rand.nextInt(messages.length)];
    }

    static public void seed(long seed){rand.setSeed(seed);}
}
