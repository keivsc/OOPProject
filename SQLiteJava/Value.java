package com.keivsc.SQLiteJava;

import java.util.*;

/**
 * Value Class
 */
public class Value {

    /**
     * Create a new Value
     */
    public Value(){

    }

    /**
     * The name of the columns in the value
     */
    public final List<String> keys = new ArrayList<>();
    /**
     * The entire row of data
     */
    public final Map<String, Object> data = new HashMap<>();

    /**
     * Builds from SQL items
     * @param items | List of items
     */
    public void ValuesInit(List<Map<String, Object>> items){
        for (Map<String, Object> item : items) {
            for (Map.Entry<String, Object> entry : item.entrySet()) {
                Object value = entry.getValue();
                String name = entry.getKey();
                this.data.put(name, value);
            }
        }
    };

    /**
     * Add item to the current value, this also overrides existing data with the same keys
     * @param name String | Name of the column
     * @param value Object | Value of the column, can be any type
     */
    public void addItem(String name, Object value){
        this.data.put(name, value);
        this.keys.add(name);
    }

    /**
     * Remove an item from the current value
     * @param key String | The name of the column
     */
    public void removeItem(String key){
        this.data.remove(key);
    }

    /**
     * Get the Value of a column
     * @param key String | Name of the column
     * @return {@code Object}
     */
    public Object get(String key){
        return this.data.get(key);
    }

    /**
     * Returns the number of items in the row.  If the
     * row contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return {@code int} Number of items in the row
     */
    public int size(){
        return this.data.size();
    }

    /**
     * Returns the entrySet of the value
     * @return <code>Set&lt;Map.Entry&lt;String, Object&gt;&gt;</code>
     */
    public Set<Map.Entry<String, Object>> entrySet(){
        return this.data.entrySet();
    }

    /**
     * Get the ClassType of the value with {@code Key}
     * @param key String | The key of the item
     * @return <code> Class&lt;?&gt;</code>
     */
    public Class<?> getType(String key){
        return this.data.get(key).getClass();
    }

}
