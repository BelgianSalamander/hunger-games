package hungergames;

import com.fasterxml.jackson.databind.JsonNode;
import hungergames.out.MessageLevel;
import hungergames.out.Out;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MyUtil {

    static public <T> String getInstanceInfo(T obj){
        Class<? extends Object> type = obj.getClass();

        String info = type.getSimpleName() + "[ ";
        List<Field> fields = Arrays.stream(type.getDeclaredFields()).filter((Field field) -> {return !Modifier.isStatic(field.getModifiers()); }).collect(Collectors.toList());
        for(int i = 0; i < fields.size(); i++){
            Field field = fields.get(i);
            field.setAccessible(true);
            try {
                info = info + field.getName() + " = " + field.get(obj);
                if(i != fields.size() - 1) info = info + ", ";
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
            if(i == fields.size() - 1){
                info = info + " ]";
            }
        }

        return info;
    }

    static public String getResource(String filename){
        InputStream is = HungerGames.class.getClassLoader().getResourceAsStream(filename);
        try {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Out.write(Color.RED + "IOException when loading resource", MessageLevel.FATAL);
            e.printStackTrace();
            return null;
        }
    }

    static public Stream<JsonNode> streamFromJsonNode(JsonNode node){
        return StreamSupport.stream(node.spliterator(), false);
    }
}
