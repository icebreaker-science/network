package chen.chaoran.data_manager.core;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * This version bases on the org.json package.
 */
public class JsonAccessHelper1 {

    private final List<Object> current;


    public JsonAccessHelper1(JSONObject jsonObject) {
        this.current = new ArrayList<>();
        this.current.add(jsonObject);
    }


    public JsonAccessHelper1(JSONArray jsonArray) {
        this.current = new ArrayList<>();
        this.current.addAll(flatArray(jsonArray));
    }


    private JsonAccessHelper1(List<Object> current) {
        this.current = current;
    }


    /**
     * @return A new instance of JsonAccessHelper1
     */
    public JsonAccessHelper1 go(String property) {
        List<Object> newCurrent = new ArrayList<>();

        for (Object jsonValue : current) {
            if (jsonValue instanceof JSONObject) {
                JSONObject x = (JSONObject) jsonValue;
                if (x.has(property)) {
                    Object newValue = x.get(property);
                    if (newValue instanceof JSONArray) {
                        newCurrent.addAll(flatArray((JSONArray) newValue));
                    } else {
                        newCurrent.add(newValue);
                    }
                }
            }
        }

        return new JsonAccessHelper1(newCurrent);
    }


    public List<Boolean> getBooleans() {
        List<Boolean> booleans = new ArrayList<>();
        for (Object jsonValue : current) {
            if (jsonValue instanceof Boolean) {
                booleans.add((Boolean) jsonValue);
            }
        }
        return booleans;
    }


    public List<Integer> getIntegers() {
        List<Integer> integers = new ArrayList<>();
        for (Object jsonValue : current) {
            if (jsonValue instanceof Integer) {
                integers.add((Integer) jsonValue);
            }
        }
        return integers;
    }


    public List<Long> getLongs() {
        List<Long> longs = new ArrayList<>();
        for (Object jsonValue : current) {
            if (jsonValue instanceof Long) {
                longs.add((Long) jsonValue);
            }
        }
        return longs;
    }


    public List<String> getStrings() {
        List<String> strings = new ArrayList<>();
        for (Object jsonValue : current) {
            if (jsonValue instanceof String) {
                strings.add((String) jsonValue);
            }
        }
        return strings;
    }


    public List<JSONObject> getJsonObjects() {
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (Object jsonValue : current) {
            if (jsonValue instanceof JSONObject) {
                jsonObjects.add((JSONObject) jsonValue);
            }
        }
        return jsonObjects;
    }


    public List<Object> getAll() {
        return new ArrayList<>(current);
    }


    public Boolean getSingleBoolean() {
        List<Boolean> booleans = getBooleans();
        if (booleans.isEmpty()) {
            return null;
        }
        return booleans.get(0);
    }


    public Integer getSingleInteger() {
        List<Integer> integers = getIntegers();
        if (integers.isEmpty()) {
            return null;
        }
        return integers.get(0);
    }


    public Long getSingleLong() {
        List<Long> longs = getLongs();
        if (longs.isEmpty()) {
            return null;
        }
        return longs.get(0);
    }


    public String getSingleString() {
        List<String> strings = getStrings();
        if (strings.isEmpty()) {
            return null;
        }
        return strings.get(0);
    }


    public JSONObject getSingleJsonObject() {
        List<JSONObject> jsonObjects = getJsonObjects();
        if (jsonObjects.isEmpty()) {
            return null;
        }
        return jsonObjects.get(0);
    }


    public Object getSingleObject() {
        List<Object> objects = getAll();
        if (objects.isEmpty()) {
            return null;
        }
        return objects.get(0);
    }


    private List<Object> flatArray(JSONArray jsonArray) {
        List<Object> content = new ArrayList<>();
        for (Object o : jsonArray) {
            if (o instanceof JSONArray) {
                content.addAll(flatArray((JSONArray) o));
            } else {
                content.add(o);
            }
        }
        return content;
    }
}
