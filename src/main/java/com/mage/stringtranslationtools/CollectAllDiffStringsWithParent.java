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

public class CollectAllDiffStringsWithParent {
    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePath = "allUntranslatedStrings.xls";
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
            WritableSheet sheet = workbook.createSheet("strings", 0);
            Label label = new Label(0, 0, "String Name");
            sheet.addCell(label);
            Label pLabel = new Label(1, 0, "App Path");
            sheet.addCell(pLabel);
            int count = 2;
            for (String str : valuesSet) {
                Label contentLabel = new Label(count, 0, str);
                sheet.addCell(contentLabel);
                ++count;
            }
            workbook.write();
            workbook.close();
            for (String resDir : resDirPathSet) {
                workbook = Workbook.createWorkbook(xmlFile, Workbook.getWorkbook(xmlFile));
                sheet = workbook.getSheet(0);
                CollectAllDiffStringsWithParent.collectAllString(filePath, resDir, valuesSet, sheet, filterMap);
                workbook.write();
                workbook.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableSheet sheet, Map<String, Boolean> filterMap) {
        ArrayList<String> keys = new ArrayList<String>();
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
                            CollectAllDiffStringsWithParent.writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
                        }
                        tempStringNames.clear();
                    }
                    tempStringNames.add(key);
                    lastName = itemName;
                    continue;
                }
                if (tempStringNames.size() != 0) {
                    if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                        CollectAllDiffStringsWithParent.writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
                    }
                    tempStringNames.clear();
                    lastName = null;
                }
                tempStringNames.add(key);
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    CollectAllDiffStringsWithParent.writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
                }
                tempStringNames.clear();
                lastName = null;
            }
            if (tempStringNames.size() != 0) {
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    CollectAllDiffStringsWithParent.writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
                }
                tempStringNames.clear();
                lastName = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeItems(List<String> tempStringNames, WritableSheet sheet, String resDir, Map<String, String> valuesResource, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap) throws RowsExceededException, WriteException {
        int count = sheet.getRows();
        for (String key : tempStringNames) {
            Label labelKey = new Label(0, count, key);
            Label labelPath = new Label(1, count, resDir);
            Label labelValue = new Label(2, count, valuesResource.get(key));
            sheet.addCell(labelKey);
            sheet.addCell(labelPath);
            sheet.addCell(labelValue);
            int verCount = 3;
            for (String str : valuesSet) {
                String temp = "";
                if (valuesResourceMap.get(str) != null && valuesResourceMap.get(str).get(key) != null) {
                    temp = valuesResourceMap.get(str).get(key);
                }
                Label contentLabel = new Label(verCount, count, temp);
                sheet.addCell(contentLabel);
                ++verCount;
            }
            ++count;
        }
    }
}

