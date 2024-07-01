package com.keivsc.SQLiteJava;

import org.json.JSONObject;
import org.w3c.dom.ls.LSOutput;

import java.util.Map;

/**
 * JSONObject
 */
public class JSONObj extends JSONObject {

    /**
     * Create a new JSONObject
     */
    public JSONObj() {
        super();
    }

    /**
     * Create a new JSONObject from a Map
     * @param map Map&lt;?, ?&gt;
     */
    public JSONObj(Map<?, ?> map) {
        super(map);
    }
}
