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

    public CollectAllDiffStringsWithParentWithXMLDir() {
    }

    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePath = "translated.xls";
        if (EnviromentBuilder.isValidArgs(args)) {
            String filePath = args[1];
            Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
            Map<String, Boolean> filterMap = EnviromentBuilder.scanFilterItems(filterFileName);
            List<String> valuesSet = EnviromentBuilder.scanValuesList(valuesConfigFileName);
            File xmlFile = new File(xmlFilePath);

            try {
                WritableWorkbook workbook = Workbook.createWorkbook(xmlFile);
                String valueBase = (String) valuesSet.get(0);
                List<String> valuesSetTemp = valuesSet.subList(1, valuesSet.size());
                Iterator var15 = valuesSetTemp.iterator();

                String resDir;
                while (var15.hasNext()) {
                    resDir = (String) var15.next();
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
                var15 = resDirPathSet.iterator();

                while (var15.hasNext()) {
                    resDir = (String) var15.next();
                    workbook = Workbook.createWorkbook(xmlFile, Workbook.getWorkbook(xmlFile));
                    collectAllString(filePath, resDir, valuesSet, workbook, filterMap);
                    workbook.write();
                    workbook.close();
                }
            } catch (Exception var20) {
                var20.printStackTrace();
            }

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
        TranslationStringDatabase translationStringDatabase = new TranslationStringDatabase(".." + File.separator + "database", resDir);
        List<String> keys = new ArrayList();
        String valueBase = (String) valuesSet.get(0);
        Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + (String) valuesSet.get(0), keys);
        Map<String, Map<String, String>> valuesResourceMap = new HashMap();
        valuesSet = valuesSet.subList(1, valuesSet.size());
        Iterator var11 = valuesSet.iterator();

        while (var11.hasNext()) {
            String key = (String) var11.next();
            Map<String, String> valuesResourceTemp = new HashMap();
            int index = key.indexOf("-");

            while (index != -1) {
                index = key.indexOf("-", index + 1);
                if (index == -1) {
                    break;
                }

                String temp = key.substring(0, index);
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + temp, (List) null));
            }

            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, (List) null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }

        List<String> tempStringNames = new ArrayList();
        String lastName = null;

        try {
            Map<String, ArrayList<Item>> itemsMap = new HashMap();
            Iterator var21 = valuesSet.iterator();

            String key;
            while (var21.hasNext()) {
                key = (String) var21.next();
                ArrayList<Item> itemsList = new ArrayList();
                itemsMap.put(key, itemsList);
            }

            var21 = keys.iterator();

            while (true) {
                while (var21.hasNext()) {
                    key = (String) var21.next();
                    if (!key.startsWith("A:") && !key.startsWith("P:")) {
                        if (tempStringNames.size() != 0) {
                            if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                                writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                            }

                            tempStringNames.clear();
                            var11 = null;
                        }

                        tempStringNames.add(key);
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                        }

                        tempStringNames.clear();
                        lastName = null;
                    } else {
                        String itemName = key.substring(0, key.lastIndexOf(":"));
                        if (itemName.equals(lastName)) {
                            tempStringNames.add(key);
                        } else {
                            if (tempStringNames.size() != 0) {
                                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                                    writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                                }

                                tempStringNames.clear();
                            }

                            tempStringNames.add(key);
                            lastName = itemName;
                        }
                    }
                }

                if (tempStringNames.size() != 0) {
                    if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                        writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbook, itemsMap);
                    }

                    tempStringNames.clear();
                    var11 = null;
                }

                var21 = valuesSet.iterator();

                while (var21.hasNext()) {
                    key = (String) var21.next();
                    writeItemsToXML((List) itemsMap.get(key), key, xmlFileDir);
                }
                break;
            }
        } catch (Exception var16) {
            var16.printStackTrace();
        }

    }

    private static void writeItems(List<String> tempStringNames, String resDir, Map<String, String> valuesResource, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap, String valueBase, TranslationStringDatabase translationStringDatabase, WritableWorkbook workbook, Map<String, ArrayList<Item>> itemsMap) throws RowsExceededException, WriteException {
        Iterator var10 = tempStringNames.iterator();

        label38:
        while (var10.hasNext()) {
            String key = (String) var10.next();
            Map<String, String> notTranslatedMap = EnviromentBuilder.getNotTranslatedMap(key, valuesSet, valuesResourceMap);
            notTranslatedMap.put(valueBase, (String) valuesResource.get(key));
            Utils.logout("KEY!:" + key);
            Utils.logout(notTranslatedMap.toString());
            Map<String, Map<String, String>> notTranslatedStringsMap = new HashMap();
            notTranslatedStringsMap.put(key, notTranslatedMap);
            translationStringDatabase.getTranslatedStringsMap(notTranslatedStringsMap, valueBase);
            Utils.logout("KEY2:" + key);
            Utils.logout(notTranslatedMap.toString());
            Iterator var14 = valuesSet.iterator();

            while (true) {
                while (true) {
                    if (!var14.hasNext()) {
                        continue label38;
                    }

                    String str = (String) var14.next();
                    String temp = "";
                    if (valuesResourceMap.get(str) != null && ((Map) valuesResourceMap.get(str)).get(key) != null) {
                        temp = (String) ((Map) valuesResourceMap.get(str)).get(key);
                    }

                    if (temp != null && temp.length() != 0) {
                        ((ArrayList) itemsMap.get(str)).add(new Item(key, resDir, (String) valuesResource.get(key), (String) valuesResource.get(key)));
                    } else if (notTranslatedMap.get(str) != null && ((String) notTranslatedMap.get(str)).length() != 0) {
                        outputValuesSet(workbook, str, key, resDir, (String) valuesResource.get(key), (String) notTranslatedMap.get(str));
                    } else {
                        ((ArrayList) itemsMap.get(str)).add(new Item(key, resDir, (String) valuesResource.get(key), (String) valuesResource.get(key)));
                    }
                }
            }
        }

    }

    public static void writeItemsToXML(List<Item> items, String valuesDir, File fileDirBase) {
        if (items.size() != 0) {
            try {
                String resPath = ((Item) items.get(0)).getPath();
                resPath = resPath.replace('/', File.separatorChar);
                String fileDir = valuesDir + File.separator + resPath + File.separator + "res" + File.separator + valuesDir;
                File file = new File(fileDirBase, fileDir);
                if (!file.exists()) {
                    file.mkdirs();
                }

                BufferedWriter fw = new BufferedWriter(new FileWriter(new File(file, "strings.xml")));
                fw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                fw.newLine();
                fw.write("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">");
                fw.newLine();
                List<Item> itemsTemp = null;
                String lastName = null;
                Iterator var10 = items.iterator();

                while (var10.hasNext()) {
                    Item item = (Item) var10.next();
                    if (item.getName().startsWith("S:")) {
                        writeItemToResources(itemsTemp, fw);
                        itemsTemp = new ArrayList();
                        lastName = item.getName();
                        itemsTemp.add(item);
                    } else {
                        String itemName;
                        if (item.getName().startsWith("P:")) {
                            itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                            if (itemName.equals(lastName)) {
                                lastName = itemName;
                                itemsTemp.add(item);
                            } else {
                                writeItemToResources(itemsTemp, fw);
                                itemsTemp = new ArrayList();
                                lastName = itemName;
                                itemsTemp.add(item);
                            }
                        } else if (item.getName().startsWith("A:")) {
                            itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                            if (itemName.equals(lastName)) {
                                lastName = itemName;
                                itemsTemp.add(item);
                            } else {
                                writeItemToResources(itemsTemp, fw);
                                itemsTemp = new ArrayList();
                                lastName = itemName;
                                itemsTemp.add(item);
                            }
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
        if (items != null) {
            Item itemFirst = (Item) items.get(0);
            if (itemFirst.getName().startsWith("S:")) {
                bufferedWriter.write("    ");
                bufferedWriter.write("<string name=\"");
                bufferedWriter.write(itemFirst.getName().substring(itemFirst.getName().indexOf(":") + 1, itemFirst.getName().length()));
                bufferedWriter.write("\">");
                bufferedWriter.write("\"" + itemFirst.getStringTranslation() + "\"");
                bufferedWriter.write("</string>");
                bufferedWriter.newLine();
            }

            String name;
            if (itemFirst.getName().startsWith("P:")) {
                name = itemFirst.getName();
                bufferedWriter.write("    ");
                bufferedWriter.write("<plurals name=\"");
                bufferedWriter.write(name.substring(name.indexOf(":") + 1, name.lastIndexOf(":")));
                bufferedWriter.write("\">");
                bufferedWriter.newLine();
                Iterator var5 = items.iterator();

                while (var5.hasNext()) {
                    Item item = (Item) var5.next();
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
                Iterator var10 = items.iterator();

                while (var10.hasNext()) {
                    Item item = (Item) var10.next();
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
}