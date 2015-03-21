/*
 * Copyright 2012 CREADOR GRANOESTE<granoete@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jejer.hipda.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Key and Value Array Adapter
 *
 * @param <T>
 */
public class KeyValueArrayAdapter extends ArrayAdapter<KeyValueArrayAdapter.KeyValue> {

    /**
     * Key and Value
     */
    public class KeyValue {
        public String key;
        public String value;

        /**
         * @param key
         * @param value
         */
        public KeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

    }

    /**
     * @param context
     * @param resource
     * @param textViewResourceId
     * @param objects
     */
    public KeyValueArrayAdapter(final Context context, final int resource,
                                final int textViewResourceId,
                                final KeyValue[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    /**
     * @param context
     * @param resource
     * @param textViewResourceId
     * @param objects
     */
    public KeyValueArrayAdapter(final Context context, final int resource,
                                final int textViewResourceId,
                                final List<KeyValue> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    /**
     * @param context
     * @param resource
     * @param textViewResourceId
     */
    public KeyValueArrayAdapter(final Context context, final int resource,
                                final int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public KeyValueArrayAdapter(final Context context, final int textViewResourceId,
                                final KeyValue[] objects) {
        super(context, textViewResourceId, objects);
    }

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public KeyValueArrayAdapter(final Context context, final int textViewResourceId,
                                final List<KeyValue> objects) {
        super(context, textViewResourceId, objects);
    }

    /**
     * @param context
     * @param textViewResourceId
     */
    public KeyValueArrayAdapter(final Context context, final int textViewResourceId) {
        super(context, textViewResourceId);
    }

    /**
     * Change the string value of the TextView with the value of the KeyValue.
     */
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final TextView view = (TextView) super.getView(position, convertView, parent);

        view.setText(getItem(position).value);
        return view;
    }

    /**
     * Change the string value of the TextView with the value of the KeyValue.
     */
    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        final TextView view = (TextView) super.getDropDownView(position, convertView, parent);

        view.setText(getItem(position).value);
        return view;
    }

    /**
     * Set the specified Collection at the array.
     *
     * @param keys
     * @param vaules
     */
    public void setKeyValue(final String[] keys, final String[] vaules) {
        if (keys.length != vaules.length) {
            throw new RuntimeException("The length of keys and values is not in agreement.");
        }

        final int N = keys.length;
        for (int i = 0; i < N; i++) {
            add(new KeyValue(keys[i], vaules[i]));
        }
    }

    /**
     * Set the specified Collection at the array.
     *
     * @param keysVaules
     */
    public void setKeyValue(final String[][] keysVaules) {
        final int N = keysVaules.length;
        for (int i = 0; i < N; i++) {
            add(new KeyValue(keysVaules[i][0], keysVaules[i][1]));
        }
    }

    private String[] entries;
    private String[] entryValues;

    /**
     * Set the specified Collection at the array.
     *
     * @param entries
     */
    public void setEntries(final String[] entries) {
        this.entries = entries;
        if (entryValues != null) {
            setKeyValue(entryValues, entries);
        }
    }

    /**
     * Set the specified Collection at the array.
     *
     * @param entryValues
     */
    public void setEntryValues(final String[] entryValues) {
        this.entryValues = entryValues;
        if (entries != null) {
            setKeyValue(entryValues, entries);
        }
    }

    /**
     * Get the value of the KeyValue with the specified position in the data set.
     *
     * @param position
     * @return
     */
    public String getValue(final int position) {
        return getItem(position).value;
    }

    /**
     * Get the key of the KeyValue with the specified position in the data set.
     *
     * @param position
     * @return
     */
    public String getKey(final int position) {
        return getItem(position).key;
    }

    /**
     * Get the entry of the KeyValue with the specified position in the data set.
     *
     * @param position
     * @return
     */
    public String getEntry(final int position) {
        return getValue(position);
    }

    /**
     * Get the entry value of the KeyValue with the specified position in the data set.
     *
     * @param position
     * @return
     */
    public String getEntryValue(final int position) {
        return getKey(position);
    }

}