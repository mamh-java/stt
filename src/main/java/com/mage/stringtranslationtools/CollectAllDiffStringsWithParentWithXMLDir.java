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

package com.mage.stringtranslationtools;


import com.mage.stringtranslationtools.database.TranslationStringDatabase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class CollectAllDiffStringsWithParentWithXMLDir {
    private static File xmlFileDir = new File("./translation/");

    public CollectAllDiffStringsWithParentWithXMLDir() {
    }

    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePath = "translated.xls";

        if (EnviromentBuilder.isValidArgsTwo(args)) {
            return;
        }

        String filePath = args[1];
        Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
        Map<String, Boolean> filterMap = EnviromentBuilder.scanFilterItems(filterFileName);
        List<String> valuesSet = EnviromentBuilder.scanValuesList(valuesConfigFileName);
        File xmlFile = new File(xmlFilePath);

        try {
            WritableWorkbook workbook = Workbook.createWorkbook(xmlFile);
            String valueBase = (String) valuesSet.get(0);
            List<String> valuesSetTemp = valuesSet.subList(1, valuesSet.size());

            for (String resDir : valuesSetTemp) {
                WritableSheet sheet = workbook.createSheet(resDir, 0);
                Label label = new Label(0, 0, "String Name");
                sheet.addCell(label);
                Label pLabel = new Label(1, 0, "App Path");
                sheet.addCell(pLabel);
                Label valueBaseLabel = new Label(2, 0, valueBase);
                sheet.addCell(valueBaseLabel);
                Label contentLabel = new Label(3, 0, resDir);
                sheet.addCell(contentLabel);
            }

            workbook.write();
            workbook.close();

            for (String resDir : resDirPathSet) {
                workbook = Workbook.createWorkbook(xmlFile, Workbook.getWorkbook(xmlFile));
                collectAllString(filePath, resDir, valuesSet, workbook, filterMap);
                workbook.write();
                workbook.close();
            }
        } catch (Exception e) {
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
        } catch (Exception var9) {
            var9.printStackTrace();
        }

    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableWorkbook workbook, Map<String, Boolean> filterMap) {
        TranslationStringDatabase database =
                new TranslationStringDatabase(".." + File.separator + "database", resDir);
        List<String> keys = new ArrayList<>();
        String valueBase = valuesSet.get(0);
        Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + (String) valuesSet.get(0), keys);

        Map<String, Map<String, String>> valuesResourceMap = new HashMap<>();

        valuesSet = valuesSet.subList(1, valuesSet.size());

        for (String key : valuesSet) {
            String temp = key;
            Map<String, String> valuesResourceTemp = new HashMap<>();

            int index = temp.indexOf("-");
            while (index != -1) {
                index = key.indexOf("-", index + 1);
                if (index == -1) {
                    break;
                }
                temp = key.substring(0, index);
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + temp, null));
            }

            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }

        List<String> tempStringNames = new ArrayList<>();
        String lastName = null;
        try {
            Map<String, ArrayList<Item>> itemsMap = new HashMap<>();
            for (String str : valuesSet) {
                ArrayList<Item> itemsList = new ArrayList<>();
                itemsMap.put(str, itemsList);
            }
            for (String key : keys) {
                if ((key.startsWith("A:")) || (key.startsWith("P:"))) {
                    String itemName = key.substring(0, key.lastIndexOf(":"));
                    if (itemName.equals(lastName)) {
                        tempStringNames.add(key);
                    } else {
                        if (tempStringNames.size() != 0) {
                            if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                                writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, workbook, itemsMap);
                            }
                            tempStringNames.clear();
                        }
                        tempStringNames.add(key);
                        lastName = itemName;
                    }
                } else {
                    if (tempStringNames.size() != 0) {
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, workbook, itemsMap);
                        }
                        tempStringNames.clear();
                        lastName = null;
                    }
                    tempStringNames.add(key);
                    if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                        writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, workbook, itemsMap);
                    }
                    tempStringNames.clear();
                    lastName = null;
                }
            }

            if (tempStringNames.size() != 0) {
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, workbook, itemsMap);
                }
                tempStringNames.clear();
                lastName = null;
            }
            for (String str : valuesSet) {
                writeItemsToXML((List) itemsMap.get(str), str, xmlFileDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeItems(List<String> tempStringNames, String resDir, Map<String, String> valuesResource, List<String> valuesSet,
                                   Map<String, Map<String, String>> valuesResourceMap, String valueBase, TranslationStringDatabase database,
                                   WritableWorkbook workbook, Map<String, ArrayList<Item>> itemsMap) {
        for (String key : tempStringNames) {
            Map<String, String> notTranslatedMap = EnviromentBuilder.getNotTranslatedMap(key, valuesSet, valuesResourceMap);
            notTranslatedMap.put(valueBase, valuesResource.get(key));
            Utils.logout("KEY!:" + key);
            Utils.logout(notTranslatedMap.toString());
            HashMap<String, Map<String, String>> notTranslatedStringsMap = new HashMap<String, Map<String, String>>();
            notTranslatedStringsMap.put(key, notTranslatedMap);
            database.getTranslatedStringsMap(notTranslatedStringsMap, valueBase);
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
        if (items.size() != 0) {
            try {
                String resPath = items.get(0).getPath();
                resPath = resPath.replace('/', File.separatorChar);
                String fileDir = valuesDir + File.separator + resPath + File.separator + "res" + File.separator + valuesDir;
                File file = new File(fileDirBase, fileDir);
                if (!file.exists()) {
                    boolean b = file.mkdirs();
                }

                BufferedWriter fw = new BufferedWriter(new FileWriter(new File(file, "strings.xml")));
                fw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                fw.newLine();
                fw.write("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">");
                fw.newLine();

                List<Item> itemsTemp = null;
                String lastName = null;

                for (Item item : items) {
                    if (item.getName().startsWith("S:")) {
                        writeItemToResources(itemsTemp, fw);
                        itemsTemp = new ArrayList<>();
                        lastName = item.getName();
                        itemsTemp.add(item);
                    } else if (item.getName().startsWith("P:")) {
                        String itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                        if (itemName.equals(lastName)) {
                            lastName = itemName;
                            itemsTemp.add(item);
                        } else {
                            writeItemToResources(itemsTemp, fw);
                            itemsTemp = new ArrayList<>();
                            lastName = itemName;
                            itemsTemp.add(item);
                        }
                    } else if (item.getName().startsWith("A:")) {
                        String itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                        if (itemName.equals(lastName)) {
                            lastName = itemName;
                            itemsTemp.add(item);
                        } else {
                            writeItemToResources(itemsTemp, fw);
                            itemsTemp = new ArrayList<>();
                            lastName = itemName;
                            itemsTemp.add(item);
                        }
                    }

                }

                writeItemToResources(itemsTemp, fw);
                fw.write("</resources>");
                fw.newLine();
                fw.flush();
                fw.close();
            } catch (IOException var12) {
                var12.printStackTrace();
            }

        }
    }

    public static void writeItemToResources(List<Item> items, BufferedWriter bufferedWriter) throws IOException {
        if (items == null) {
            return;
        }

        String name = items.get(0).getName(); //一般这个不会是null
        if (name.startsWith("S:")) {
            writeString(items, bufferedWriter, name);
        } else if (name.startsWith("P:")) {
            writePlurals(items, bufferedWriter, name);
        } else if (name.startsWith("A:")) {
            writeArray(items, bufferedWriter, name);
        }
    }

    private static void writeArray(List<Item> items, BufferedWriter bufferedWriter, String name) throws IOException {
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

    private static void writePlurals(List<Item> items, BufferedWriter bufferedWriter, String name) throws IOException {
        bufferedWriter.write("    ");
        bufferedWriter.write("<plurals name=\"");
        bufferedWriter.write(name.substring(name.indexOf(":") + 1, name.lastIndexOf(":")));
        bufferedWriter.write("\">");
        bufferedWriter.newLine();
        for (Item item : items) {
            String itemName = item.getName();
            String pluralsQuantity = itemName.substring(itemName.lastIndexOf(":") + 1);
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


    private static void writeString(List<Item> items, BufferedWriter bufferedWriter, String name) throws IOException {
        bufferedWriter.write("    ");
        bufferedWriter.write("<string name=\"");
        bufferedWriter.write(name.substring(name.indexOf(":") + 1));
        bufferedWriter.write("\">");
        for (Item item : items) { // 这个items 列表只会有一个元素的，这里也使用个循环，和其他2个方法类似
            bufferedWriter.write("\"" + item.getStringTranslation() + "\""); //插入 到 <string></string> 标签之间的值
        }
        bufferedWriter.write("</string>");
        bufferedWriter.newLine();
    }

}