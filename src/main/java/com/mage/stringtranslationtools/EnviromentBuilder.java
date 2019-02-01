/*
 *  Copyright (C) 2019.  mamh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/*
 * Decompiled with CFR 0.139.
 */
package com.mage.stringtranslationtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnviromentBuilder {
    public static String FILEPATH_RES = String.valueOf(File.separator) + "res" + File.separator + "values";
    public static FilenameFilter mStringXMLFileFilter = new FilenameFilter(){

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(".xml");
        }
    };

    public static boolean isValidArgs(String[] args) {
        if (args.length < 2) {
            Utils.logerr("Please give three file path to check.");
            return false;
        }
        return true;
    }

    public static boolean isValidArgsThree(String[] args) {
        if (args.length < 3) {
            Utils.logerr("Please give three file path to check.");
            return false;
        }
        return true;
    }

    public static Map<String, String> readStringValueFromDir(String dirPath, List<String> keys) {
        Utils.logout("readStringValueFromDir:" + dirPath);
        File[] files = new File(dirPath).listFiles(mStringXMLFileFilter);
        HashMap<String, String> xmlContentMap = new HashMap<String, String>();
        Dom4jParser parser = new Dom4jParser();
        if (files != null) {
            for (File file : files) {
                Map<String, String> temp = parser.parseValidStringNames(file, keys);
                xmlContentMap.putAll(temp);
            }
        }
        return xmlContentMap;
    }

    public static Set<String> scanResDirPathList(String configFileName) {
        HashSet<String> set;
        set = new HashSet<String>();
        try {
            FileInputStream f = new FileInputStream(new File(configFileName));
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(f));
            try {
                String file;
                while ((file = fileReader.readLine()) != null) {
                    if ((file = file.replace("/", File.separator)).indexOf(FILEPATH_RES) != -1) {
                        file = file.substring(1, file.indexOf(FILEPATH_RES));
                    } else if (file.indexOf(String.valueOf(File.separator) + "res1" + File.separator + "values") != -1) {
                        file = file.substring(1, file.indexOf(String.valueOf(File.separator) + "res1" + File.separator + "values"));
                    }
                    Utils.logout("scanResDirPathList:" + file);
                    set.add(file);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return set;
    }

    public static Map<String, Boolean> scanFilterItems(String configFileName) {
        HashMap<String, Boolean> map;
        map = new HashMap<String, Boolean>();
        try {
            FileInputStream f = new FileInputStream(new File(configFileName));
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(f));
            try {
                String file;
                while ((file = fileReader.readLine()) != null) {
                    Utils.logout("scanFilterItems:" + file);
                    map.put(file, true);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static List<String> scanValuesList(String fileName) {
        ArrayList<String> set;
        set = new ArrayList<String>();
        try {
            FileInputStream f = new FileInputStream(new File(fileName));
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(f));
            try {
                String file;
                while ((file = fileReader.readLine()) != null) {
                    set.add(file);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return set;
    }

    public static boolean isValidString(String key, String value) {
        if (key.startsWith("A:") || key.startsWith("P:")) {
            return true;
        }
        if (value.isEmpty()) {
            return false;
        }
        if (value.startsWith("@string/")) {
            return false;
        }
        if (value.startsWith("array/")) {
            return false;
        }
        if (value.startsWith("drawable/")) {
            return false;
        }
        if (value.startsWith("string/")) {
            return false;
        }
        if (value.startsWith("@*android:string/")) {
            return false;
        }
        if (value.startsWith("@drawable/")) {
            return false;
        }
        if (value.trim().length() < 1) {
            return false;
        }
        try {
            Float.parseFloat(value);
            return false;
        }
        catch (Exception exception) {
            return true;
        }
    }

    public static boolean isValidKey(String key, Map<String, Boolean> filterMap) {
        if (filterMap.get(key) != null && filterMap.get(key).booleanValue()) {
            Utils.logout("isValidKey = false : " + key);
            return false;
        }
        return true;
    }

    public static boolean isNotTranslated(String key, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap) {
        for (String str : valuesSet) {
            String temp = null;
            if (valuesResourceMap.get(str) != null && valuesResourceMap.get(str).get(key) != null) {
                temp = valuesResourceMap.get(str).get(key);
            }
            if (temp == null) {
                return true;
            }
            if (temp.startsWith("\"")) {
                temp = temp.substring(1, temp.length());
            }
            if (temp.endsWith("\"")) {
                temp = temp.substring(0, temp.length() - 1);
            }
            if (!temp.isEmpty()) continue;
            return true;
        }
        return false;
    }

    public static boolean isArrayNotTranslated(List<String> tempStringNames, Map<String, String> valuesResource, String resDir, Map<String, Boolean> filterMap, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap) {
        for (String tempKey : tempStringNames) {
            String tempvalue = valuesResource.get(tempKey);
            if (tempvalue.startsWith("\"")) {
                tempvalue = tempvalue.substring(1, tempvalue.length());
            }
            if (tempvalue.endsWith("\"")) {
                tempvalue = tempvalue.substring(0, tempvalue.length() - 1);
            }
            if (!EnviromentBuilder.isValidString("", tempvalue) || !EnviromentBuilder.isValidKey(String.valueOf(tempKey) + "==" + resDir, filterMap) || !EnviromentBuilder.isNotTranslated(tempKey, valuesSet, valuesResourceMap)) continue;
            return true;
        }
        return false;
    }

    public static Map<String, String> getNotTranslatedMap(String key, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String str : valuesSet) {
            String temp = null;
            if (valuesResourceMap.get(str) != null && valuesResourceMap.get(str).get(key) != null) {
                temp = valuesResourceMap.get(str).get(key);
            }
            if (temp == null) {
                map.put(str, null);
                continue;
            }
            if (temp.startsWith("\"")) {
                temp = temp.substring(1, temp.length());
            }
            if (temp.endsWith("\"")) {
                temp = temp.substring(0, temp.length() - 1);
            }
            if (!temp.isEmpty()) continue;
            map.put(str, null);
        }
        return map;
    }

}

