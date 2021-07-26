package hungergames.lists;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hungergames.FormattedMessage;
import hungergames.MyUtil;
import hungergames.out.MessageLevel;
import hungergames.out.Out;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WordLists {
    static Map<String, WordList> wordLists;

    public static WordList getList(String name){
        return wordLists.get(name);
    }

    public static FormattedMessage select(String name){
        WordList list = wordLists.get(name);
        if(list == null){Out.write(Color.RED + "Tried to get non-existent list '" + name + "'", MessageLevel.ERROR); return null;}
        return wordLists.get(name).select();
    }

    static{
        wordLists = new HashMap<>();

        String listsAsJsonString = MyUtil.getResource("lists.json");
        JsonNode lists = null;
        try {
            lists = new ObjectMapper().readTree(listsAsJsonString);
        } catch (IOException e) {
            Out.write("Could not read lists JSON file", MessageLevel.FATAL);
            Out.write(e.getMessage(), MessageLevel.FATAL);
        }
        for(Iterator<Map.Entry<String, JsonNode>> i =  lists.fields(); i.hasNext();){
            Map.Entry<String, JsonNode> entry = i.next();
            wordLists.put(entry.getKey(), new WordList(MyUtil.streamFromJsonNode(entry.getValue()).map(text -> new FormattedMessage(text.asText())).toArray(FormattedMessage[]::new)));
        }
    }
}
