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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class CollectAllDiffStringsWithParentWithCheckDatabase {
    public CollectAllDiffStringsWithParentWithCheckDatabase() {
    }

    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePathWithCheckDatabase = "allUntranslatedStrings.xls";

        if (EnviromentBuilder.isValidArgsTwo(args)) {
            return;
        }
        String filePath = args[1];

        Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
        Map<String, Boolean> filterMap = EnviromentBuilder.scanFilterItems(filterFileName);
        List<String> valuesSet = EnviromentBuilder.scanValuesList(valuesConfigFileName);
        File xmlFileWithCheckDatabase = new File(xmlFilePathWithCheckDatabase);

        try {
            WritableWorkbook workbook = Workbook.createWorkbook(xmlFileWithCheckDatabase);
            WritableSheet sheetWithCheckDatabase = workbook.createSheet("strings", 0);

            Label labelWithCheckDatabase = new Label(0, 0, "String Name");
            sheetWithCheckDatabase.addCell(labelWithCheckDatabase);

            Label pLabelWithCheckDatabase = new Label(1, 0, "App Path");
            sheetWithCheckDatabase.addCell(pLabelWithCheckDatabase);

            int countWithCheckDatabase = 2;
            for (String str : valuesSet) {
                Label contentLabelWithCheckDatabase = new Label(countWithCheckDatabase, 0, str);
                sheetWithCheckDatabase.addCell(contentLabelWithCheckDatabase);
                countWithCheckDatabase++;
            }
            workbook.write();
            workbook.close();
            for (String resDir : resDirPathSet) {
                workbook = Workbook.createWorkbook(xmlFileWithCheckDatabase, Workbook.getWorkbook(xmlFileWithCheckDatabase));
                sheetWithCheckDatabase = workbook.getSheet(0);
                collectAllString(filePath, resDir, valuesSet, sheetWithCheckDatabase, filterMap);
                workbook.write();
                workbook.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableSheet sheet, Map<String, Boolean> filterMap) {
        TranslationStringDatabase database = new TranslationStringDatabase(".." + File.separator + "database", resDir);
        List<String> keys = new ArrayList<>();
        String valueBase = valuesSet.get(0);
        Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + valuesSet.get(0), keys);

        Map<String, Map<String, String>> valuesResourceMap = new HashMap<>();

        valuesSet = valuesSet.subList(1, valuesSet.size());

        for (String key : valuesSet) {
            Map<String, String> valuesResourceTemp = new HashMap<>();

            int index = key.indexOf("-");
            while (index != -1) {
                index = key.indexOf("-", index + 1);
                if (index == -1) {
                    break;
                }
                String temp = key.substring(0, index);
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + temp, null));
            }

            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }

        List<String> tempStringNames = new ArrayList<>();
        String lastName = null;
        try {
            for (String key : keys) {
                if ((key.startsWith("A:")) || (key.startsWith("P:"))) {
                    String itemName = key.substring(0, key.lastIndexOf(":"));
                    if (itemName.equals(lastName)) {
                        tempStringNames.add(key);
                    } else {
                        if (tempStringNames.size() != 0) {
                            if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                                writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, sheet);
                            }
                            tempStringNames.clear();
                        }
                        tempStringNames.add(key);
                        lastName = itemName;
                    }
                } else {
                    if (tempStringNames.size() != 0) {
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, sheet);
                        }
                        tempStringNames.clear();
                        lastName = null;
                    }
                    tempStringNames.add(key);
                    if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                        writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, sheet);
                    }
                    tempStringNames.clear();
                    lastName = null;
                }
            }
            if (tempStringNames.size() != 0) {
                if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                    writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, database, sheet);
                }
                tempStringNames.clear();
                lastName = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void writeItems(List<String> tempStringNames, String resDir,
                                   Map<String, String> valuesResource, List<String> valuesSet,
                                   Map<String, Map<String, String>> valuesResourceMap, String valueBase,
                                   TranslationStringDatabase database, WritableSheet sheet)
            throws WriteException {
        int count = sheet.getRows();
        for (String key : tempStringNames) {
            Map<String, String> notTranslatedMap = EnviromentBuilder.getNotTranslatedMap(key, valuesSet, valuesResourceMap);
            notTranslatedMap.put(valueBase, valuesResource.get(key));
            Utils.logout("KEY!:" + key);
            Utils.logout(notTranslatedMap.toString());
            Map<String, Map<String, String>> notTranslatedStringsMap = new HashMap<>();
            notTranslatedStringsMap.put(key, notTranslatedMap);

            database.getTranslatedStringsMap(notTranslatedStringsMap, valueBase);

            Utils.logout("KEY2:" + key);
            Utils.logout(notTranslatedMap.toString());
            Label labelKeyWithCheckDatabase = new Label(0, count, key);
            Label labelPathWithCheckDatabase = new Label(1, count, resDir);
            Label labelValueWithCheckDatabase = new Label(2, count, valuesResource.get(key));
            sheet.addCell(labelKeyWithCheckDatabase);
            sheet.addCell(labelPathWithCheckDatabase);
            sheet.addCell(labelValueWithCheckDatabase);

            int verCount = 3;
            for (String str : valuesSet) {
                String temp = "";
                if ((valuesResourceMap.get(str) != null) && (((Map) valuesResourceMap.get(str)).get(key) != null)) {
                    temp = (String) ((Map) valuesResourceMap.get(str)).get(key);
                }
                if (((temp == null) || (temp.length() == 0)) && (notTranslatedMap.get(str) != null) && ((notTranslatedMap.get(str)).length() != 0)) {
                    WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.RED);
                    WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                    Label contentLabel = new Label(verCount, count, notTranslatedMap.get(str), wcfFC);
                    sheet.addCell(contentLabel);
                } else {
                    Label contentLabelWithCheckDatabase = new Label(verCount, count, temp);
                    sheet.addCell(contentLabelWithCheckDatabase);
                }
                verCount++;
            }
            count++;
        }
    }
}
