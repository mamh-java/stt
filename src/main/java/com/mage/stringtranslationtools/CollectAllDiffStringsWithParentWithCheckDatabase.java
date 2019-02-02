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
import java.util.Iterator;
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
import jxl.write.biff.RowsExceededException;

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
            WritableWorkbook workbookWithCheckDatabase = Workbook.createWorkbook(xmlFileWithCheckDatabase);
            WritableSheet sheetWithCheckDatabase = workbookWithCheckDatabase.createSheet("strings", 0);
            Label labelWithCheckDatabase = new Label(0, 0, "String Name");
            sheetWithCheckDatabase.addCell(labelWithCheckDatabase);
            Label pLabelWithCheckDatabase = new Label(1, 0, "App Path");
            sheetWithCheckDatabase.addCell(pLabelWithCheckDatabase);
            int countWithCheckDatabase = 2;

            String resDir;
            Iterator var16;
            for (var16 = valuesSet.iterator(); var16.hasNext(); ++countWithCheckDatabase) {
                resDir = (String) var16.next();
                Label contentLabelWithCheckDatabase = new Label(countWithCheckDatabase, 0, resDir);
                sheetWithCheckDatabase.addCell(contentLabelWithCheckDatabase);
            }

            workbookWithCheckDatabase.write();
            workbookWithCheckDatabase.close();
            var16 = resDirPathSet.iterator();

            while (var16.hasNext()) {
                resDir = (String) var16.next();
                workbookWithCheckDatabase = Workbook.createWorkbook(xmlFileWithCheckDatabase, Workbook.getWorkbook(xmlFileWithCheckDatabase));
                sheetWithCheckDatabase = workbookWithCheckDatabase.getSheet(0);
                collectAllString(filePath, resDir, valuesSet, sheetWithCheckDatabase, filterMap);
                workbookWithCheckDatabase.write();
                workbookWithCheckDatabase.close();
            }
        } catch (Exception var18) {
            var18.printStackTrace();
        }

    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableSheet sheetWithCheckDatabase, Map<String, Boolean> filterMap) {
        TranslationStringDatabase translationStringDatabase = new TranslationStringDatabase(".." + File.separator + "database", resDir);
        List<String> keys = new ArrayList();
        String valueBase = (String) valuesSet.get(0);
        Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + (String) valuesSet.get(0), keys);
        Map<String, Map<String, String>> valuesResourceMap = new HashMap();
        valuesSet = valuesSet.subList(1, valuesSet.size());
        Iterator var11 = valuesSet.iterator();

        String key;
        while (var11.hasNext()) {
            key = (String) var11.next();
            Map<String, String> valuesResourceTemp = new HashMap();
            int index = key.indexOf("-");

            while (index != -1) {
                index = key.indexOf("-", index + 1);
                if (index == -1) {
                    break;
                }

                key = key.substring(0, index);
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, (List) null));
            }

            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, (List) null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }

        List<String> tempStringNames = new ArrayList();
        String lastName = null;

        try {
            Iterator var18 = keys.iterator();

            while (true) {
                while (var18.hasNext()) {
                    key = (String) var18.next();
                    if (!key.startsWith("A:") && !key.startsWith("P:")) {
                        if (tempStringNames.size() != 0) {
                            if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                                writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, sheetWithCheckDatabase);
                            }

                            tempStringNames.clear();
                            var11 = null;
                        }

                        tempStringNames.add(key);
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, sheetWithCheckDatabase);
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
                                    writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, sheetWithCheckDatabase);
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
                        writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, sheetWithCheckDatabase);
                    }

                    tempStringNames.clear();
                    var11 = null;
                }
                break;
            }
        } catch (Exception var15) {
            var15.printStackTrace();
        }

    }

    private static void writeItems(List<String> tempStringNames, String resDir, Map<String, String> valuesResource, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap, String valueBase, TranslationStringDatabase translationStringDatabase, WritableSheet sheetWithCheckDatabase) throws RowsExceededException, WriteException {
        int count = sheetWithCheckDatabase.getRows();

        for (Iterator var10 = tempStringNames.iterator(); var10.hasNext(); ++count) {
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
            Label labelKeyWithCheckDatabase = new Label(0, count, key);
            Label labelPathWithCheckDatabase = new Label(1, count, resDir);
            Label labelValueWithCheckDatabase = new Label(2, count, (String) valuesResource.get(key));
            sheetWithCheckDatabase.addCell(labelKeyWithCheckDatabase);
            sheetWithCheckDatabase.addCell(labelPathWithCheckDatabase);
            sheetWithCheckDatabase.addCell(labelValueWithCheckDatabase);
            int verCount = 3;

            for (Iterator var18 = valuesSet.iterator(); var18.hasNext(); ++verCount) {
                String str = (String) var18.next();
                String temp = "";
                if (valuesResourceMap.get(str) != null && ((Map) valuesResourceMap.get(str)).get(key) != null) {
                    temp = (String) ((Map) valuesResourceMap.get(str)).get(key);
                }

                if ((temp == null || temp.length() == 0) && notTranslatedMap.get(str) != null && ((String) notTranslatedMap.get(str)).length() != 0) {
                    WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.RED);
                    WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                    Label contentLabel = new Label(verCount, count, (String) notTranslatedMap.get(str), wcfFC);
                    sheetWithCheckDatabase.addCell(contentLabel);
                } else {
                    Label contentLabelWithCheckDatabase = new Label(verCount, count, temp);
                    sheetWithCheckDatabase.addCell(contentLabelWithCheckDatabase);
                }
            }
        }

    }
}
