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

import java.io.File;
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

public class CollectAllDiffStringsWithParentWithSeperateSheet {
    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePathWithCheckDatabase = "allUntranslatedStrings.xls";
        if (!EnviromentBuilder.isValidArgs(args)) {
            return;
        }
        String filePath = args[1];
        Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
        Map<String, Boolean> filterMap = EnviromentBuilder.scanFilterItems(filterFileName);
        List<String> valuesSet = EnviromentBuilder.scanValuesList(valuesConfigFileName);
        File xmlFileWithCheckDatabase = new File(xmlFilePathWithCheckDatabase);
        try {
            String valueBase = valuesSet.get(0);
            List<String> valuesSetTemp = valuesSet.subList(1, valuesSet.size());
            WritableWorkbook workbookWithCheckDatabase = Workbook.createWorkbook(xmlFileWithCheckDatabase);
            for (String str : valuesSetTemp) {
                WritableSheet sheetWithCheckDatabase = workbookWithCheckDatabase.createSheet(str, 0);
                Label labelWithCheckDatabase = new Label(0, 0, "String Name");
                sheetWithCheckDatabase.addCell(labelWithCheckDatabase);
                Label pLabelWithCheckDatabase = new Label(1, 0, "App Path");
                sheetWithCheckDatabase.addCell(pLabelWithCheckDatabase);
                Label valueBaseLabel = new Label(2, 0, valueBase);
                sheetWithCheckDatabase.addCell(valueBaseLabel);
                Label contentLabel = new Label(3, 0, str);
                sheetWithCheckDatabase.addCell(contentLabel);
            }
            workbookWithCheckDatabase.write();
            workbookWithCheckDatabase.close();
            for (String resDir : resDirPathSet) {
                workbookWithCheckDatabase = Workbook.createWorkbook(xmlFileWithCheckDatabase, Workbook.getWorkbook(xmlFileWithCheckDatabase));
                CollectAllDiffStringsWithParentWithSeperateSheet.collectAllString(filePath, resDir, valuesSet, workbookWithCheckDatabase, filterMap);
                workbookWithCheckDatabase.write();
                workbookWithCheckDatabase.close();
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

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableWorkbook workbookWithCheckDatabase, Map<String, Boolean> filterMap) {
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
            HashMap valuesResourceTemp = new HashMap();
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
            for (String key : keys) {
                if (key.startsWith("A:") || key.startsWith("P:")) {
                    String itemName = key.substring(0, key.lastIndexOf(":"));
                    if (itemName.equals(lastName)) {
                        tempStringNames.add(key);
                        continue;
                    }
                    if (tempStringNames.size() != 0) {
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            CollectAllDiffStringsWithParentWithSeperateSheet.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
                        }
                        tempStringNames.clear();
                    }
                    tempStringNames.add(key);
                    lastName = itemName;
                    continue;
                }
                if (tempStringNames.size() != 0) {
                    if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                        CollectAllDiffStringsWithParentWithSeperateSheet.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
                    }
                    tempStringNames.clear();
                    lastName = null;
                }
                tempStringNames.add(key);
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    CollectAllDiffStringsWithParentWithSeperateSheet.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
                }
                tempStringNames.clear();
                lastName = null;
            }
            if (tempStringNames.size() != 0) {
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    CollectAllDiffStringsWithParentWithSeperateSheet.writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
                }
                tempStringNames.clear();
                lastName = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeItems(List<String> tempStringNames, String resDir, Map<String, String> valuesResource, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap, String valueBase, TranslationStringDatabase translationStringDatabase, WritableWorkbook workbookWithCheckDatabase) throws RowsExceededException, WriteException {
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
                        CollectAllDiffStringsWithParentWithSeperateSheet.outputValuesSet(workbookWithCheckDatabase, str, key, resDir, valuesResource.get(key), notTranslatedMap.get(str));
                        continue;
                    }
                    CollectAllDiffStringsWithParentWithSeperateSheet.outputValuesSet(workbookWithCheckDatabase, str, key, resDir, valuesResource.get(key), temp);
                    continue;
                }
                CollectAllDiffStringsWithParentWithSeperateSheet.outputValuesSet(workbookWithCheckDatabase, str, key, resDir, valuesResource.get(key), temp);
            }
        }
    }
}

