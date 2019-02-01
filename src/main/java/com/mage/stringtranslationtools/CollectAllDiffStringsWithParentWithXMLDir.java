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

import com.mage.stringtranslationtools.database.TranslationStringDatabase;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class CollectAllDiffStringsWithParentWithXMLDir {
    private static File xmlFileDir = new File("./translation/");

    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePath = "translated.xls";
        if (!EnviromentBuilder.isValidArgs(args)) {
            return;
        }
        String filePath = args[1];
        Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
        Map<String, Boolean> filterMap = EnviromentBuilder.scanFilterItems(filterFileName);
        List<String> valuesSet = EnviromentBuilder.scanValuesList(valuesConfigFileName);
        File xmlFile = new File(xmlFilePath);
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(xmlFile);
            String valueBase = valuesSet.get(0);
            List<String> valuesSetTemp = valuesSet.subList(1, valuesSet.size());
            for (String str : valuesSetTemp) {
                WritableSheet sheet = workbook.createSheet(str, 0);
                Label label = new Label(0, 0, "String Name");
                sheet.addCell(label);
                Label pLabel = new Label(1, 0, "App Path");
                sheet.addCell(pLabel);
                Label valueBaseLabel = new Label(2, 0, valueBase);
                sheet.addCell(valueBaseLabel);
                Label contentLabel = new Label(3, 0, str);
                sheet.addCell(contentLabel);
            }
            workbook.write();
            workbook.close();
            for (String resDir : resDirPathSet) {
                workbook = Workbook.createWorkbook(xmlFile, Workbook.getWorkbook(xmlFile));
                CollectAllDiffStringsWithParentWithXMLDir.collectAllString(filePath, resDir, valuesSet, workbook, filterMap);
                workbook.write();
                workbook.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void outputValuesSet(WritableWorkbook workbook, String valuesName, String key, String resDir, String labelValue, String labelValueTranslation) {
        WritableSheet sheet = workbook.getSheet(valuesName);
        int count = sheet.getRows();
        try {
            sheet.addCell(new Label(0, count, key));
            sheet.addCell(new Label(1, count, resDir));
            sheet.addCell(new Label(2, count, labelValue));
            sheet.addCell(new Label(3, count, labelValueTranslation));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableWorkbook workbook, Map<String, Boolean> filterMap) {
        TranslationStringDatabase translationStringDatabase = new TranslationStringDatabase(".." + File.separator + "database", resDir);
        ArrayList<String> keys = new ArrayList<String>();
        String valueBase = valuesSet.get(0);
        Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(String.valueOf(filePath) + resDir + File.separator + "res" + File.separator + valuesSet.get(0), keys);
        HashMap<String, Map<String, String>> valuesResourceMap = new HashMap<String, Map<String, String>>();
        valuesSet = valuesSet.subList(1, valuesSet.size());
        Iterator<String> iterator = valuesSet.iterator();
        while (iterator.hasNext()) {
            String key;
            String temp = key = iterator.next();
            HashMap<String, String> valuesResourceTemp = new HashMap<String, String>();
            int index = temp.indexOf("-");
            while (index != -1) {
                if ((index = key.indexOf("-", index + 1)) == -1) break;
                temp = key.substring(0, index);
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(String.valueOf(filePath) + resDir + File.separator + "res" + File.separator + temp, null));
            }
            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(String.valueOf(filePath) + resDir + File.separator + "res" + File.separator + key, null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }
        ArrayList<String> tempStringNames = new ArrayList<String>();
        String lastName = null;
        try {
            HashMap<String, ArrayList<Item>> itemsMap = new HashMap<String, ArrayList<Item>>();
            for (String str : valuesSet) {
                ArrayList itemsList = new ArrayList();
                itemsMap.put(str, itemsList);
            }
            for (String key : keys) {
                if (key.startsWith("A:") || key.startsWith("P:")) {
                    String itemName = key.substring(0, key.lastIndexOf(":"));
                    if (itemName.equals(lastName)) {
                        tempStringNames.add(key);
                        continue;
                    }
                    if (tempStringNames.size() != 0) {
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            CollectAllDiffStringsWithParentWithXMLDir.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                        }
                        tempStringNames.clear();
                    }
                    tempStringNames.add(key);
                    lastName = itemName;
                    continue;
                }
                if (tempStringNames.size() != 0) {
                    if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                        CollectAllDiffStringsWithParentWithXMLDir.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                    }
                    tempStringNames.clear();
                    lastName = null;
                }
                tempStringNames.add(key);
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    CollectAllDiffStringsWithParentWithXMLDir.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                }
                tempStringNames.clear();
                lastName = null;
            }
            if (tempStringNames.size() != 0) {
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    CollectAllDiffStringsWithParentWithXMLDir.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                }
                tempStringNames.clear();
                lastName = null;
            }
            for (String str : valuesSet) {
                CollectAllDiffStringsWithParentWithXMLDir.writeItemsToXML((List)itemsMap.get(str), str, xmlFileDir);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeItems(List<String> tempStringNames, String resDir, Map<String, String> valuesResource, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap, String valueBase, TranslationStringDatabase translationStringDatabase, WritableWorkbook workbook, Map<String, ArrayList<Item>> itemsMap) throws RowsExceededException, WriteException {
        for (String key : tempStringNames) {
            Map<String, String> notTranslatedMap = EnviromentBuilder.getNotTranslatedMap(key, valuesSet, valuesResourceMap);
            notTranslatedMap.put(valueBase, valuesResource.get(key));
            Utils.logout("KEY!:" + key);
            Utils.logout(notTranslatedMap.toString());
            HashMap<String, Map<String, String>> notTranslatedStringsMap = new HashMap<String, Map<String, String>>();
            notTranslatedStringsMap.put(key, notTranslatedMap);
            translationStringDatabase.getTranslatedStringsMap(notTranslatedStringsMap, valueBase);
            Utils.logout("KEY2:" + key);
            Utils.logout(notTranslatedMap.toString());
            for (String str : valuesSet) {
                String temp = "";
                if (valuesResourceMap.get(str) != null && valuesResourceMap.get(str).get(key) != null) {
                    temp = valuesResourceMap.get(str).get(key);
                }
                if (temp == null || temp.length() == 0) {
                    if (notTranslatedMap.get(str) != null && notTranslatedMap.get(str).length() != 0) {
                        CollectAllDiffStringsWithParentWithXMLDir.outputValuesSet(workbook, str, key, resDir, valuesResource.get(key), notTranslatedMap.get(str));
                        continue;
                    }
                    itemsMap.get(str).add(new Item(key, resDir, valuesResource.get(key), valuesResource.get(key)));
                    continue;
                }
                itemsMap.get(str).add(new Item(key, resDir, valuesResource.get(key), valuesResource.get(key)));
            }
        }
    }

    public static void writeItemsToXML(List<Item> items, String valuesDir, File fileDirBase) {
        if (items.size() == 0) {
            return;
        }
        try {
            String resPath = items.get(0).getPath();
            resPath = resPath.replace('/', File.separatorChar);
            String fileDir = String.valueOf(valuesDir) + File.separator + resPath + File.separator + "res" + File.separator + valuesDir;
            File file = new File(fileDirBase, fileDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedWriter fw = new BufferedWriter(new FileWriter(new File(file, "strings.xml")));
            fw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            fw.newLine();
            fw.write("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">");
            fw.newLine();
            ArrayList<Item> itemsTemp = null;
            String lastName = null;
            for (Item item : items) {
                String itemName;
                if (item.getName().startsWith("S:")) {
                    CollectAllDiffStringsWithParentWithXMLDir.writeItemToResources(itemsTemp, fw);
                    itemsTemp = new ArrayList<Item>();
                    lastName = item.getName();
                    itemsTemp.add(item);
                    continue;
                }
                if (item.getName().startsWith("P:")) {
                    itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                    if (itemName.equals(lastName)) {
                        lastName = itemName;
                        itemsTemp.add(item);
                        continue;
                    }
                    CollectAllDiffStringsWithParentWithXMLDir.writeItemToResources(itemsTemp, fw);
                    itemsTemp = new ArrayList();
                    lastName = itemName;
                    itemsTemp.add(item);
                    continue;
                }
                if (!item.getName().startsWith("A:")) continue;
                itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                if (itemName.equals(lastName)) {
                    lastName = itemName;
                    itemsTemp.add(item);
                    continue;
                }
                CollectAllDiffStringsWithParentWithXMLDir.writeItemToResources(itemsTemp, fw);
                itemsTemp = new ArrayList();
                lastName = itemName;
                itemsTemp.add(item);
            }
            CollectAllDiffStringsWithParentWithXMLDir.writeItemToResources(itemsTemp, fw);
            fw.write("</resources>");
            fw.newLine();
            fw.flush();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeItemToResources(List<Item> items, BufferedWriter bufferedWriter) throws IOException {
        String name;
        if (items == null) {
            return;
        }
        Item itemFirst = items.get(0);
        if (itemFirst.getName().startsWith("S:")) {
            bufferedWriter.write("    ");
            bufferedWriter.write("<string name=\"");
            bufferedWriter.write(itemFirst.getName().substring(itemFirst.getName().indexOf(":") + 1, itemFirst.getName().length()));
            bufferedWriter.write("\">");
            bufferedWriter.write("\"" + itemFirst.getStringTranslation() + "\"");
            bufferedWriter.write("</string>");
            bufferedWriter.newLine();
        }
        if (itemFirst.getName().startsWith("P:")) {
            name = itemFirst.getName();
            bufferedWriter.write("    ");
            bufferedWriter.write("<plurals name=\"");
            bufferedWriter.write(name.substring(name.indexOf(":") + 1, name.lastIndexOf(":")));
            bufferedWriter.write("\">");
            bufferedWriter.newLine();
            for (Item item : items) {
                String itemName = item.getName();
                String pluralsQuantity = itemName.substring(itemName.lastIndexOf(":") + 1, itemName.length());
                bufferedWriter.write("        ");
                bufferedWriter.write("<item quantity=\"");
                bufferedWriter.write(pluralsQuantity);
                bufferedWriter.write("\">");
                bufferedWriter.write("\"" + item.getStringTranslation() + "\"");
                bufferedWriter.write("</item>");
                bufferedWriter.newLine();
            }
            bufferedWriter.write("    ");
            bufferedWriter.write("</plurals>");
            bufferedWriter.newLine();
        }
        if (itemFirst.getName().startsWith("A:")) {
            name = itemFirst.getName();
            String stringArrayName = name.substring(name.indexOf(":") + 1, name.lastIndexOf(":"));
            bufferedWriter.write("    ");
            bufferedWriter.write("<string-array name=\"");
            bufferedWriter.write(stringArrayName);
            bufferedWriter.write("\">");
            bufferedWriter.newLine();
            for (Item item : items) {
                bufferedWriter.write("        ");
                bufferedWriter.write("<item>");
                bufferedWriter.write("\"" + item.getStringTranslation() + "\"");
                bufferedWriter.write("</item>");
                bufferedWriter.newLine();
            }
            bufferedWriter.write("    ");
            bufferedWriter.write("</string-array>");
            bufferedWriter.newLine();
        }
    }
}

