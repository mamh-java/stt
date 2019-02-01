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
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class CollectAllDiffStringsWithParentWithSeperateSheet {
    public CollectAllDiffStringsWithParentWithSeperateSheet() {
    }

    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePathWithCheckDatabase = "allUntranslatedStrings.xls";
        if (EnviromentBuilder.isValidArgs(args)) {
            String filePath = args[1];
            Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
            Map<String, Boolean> filterMap = EnviromentBuilder.scanFilterItems(filterFileName);
            List<String> valuesSet = EnviromentBuilder.scanValuesList(valuesConfigFileName);
            File xmlFileWithCheckDatabase = new File(xmlFilePathWithCheckDatabase);

            try {
                String valueBase = (String) valuesSet.get(0);
                List<String> valuesSetTemp = valuesSet.subList(1, valuesSet.size());
                WritableWorkbook workbookWithCheckDatabase = Workbook.createWorkbook(xmlFileWithCheckDatabase);
                Iterator var15 = valuesSetTemp.iterator();

                String resDir;
                while (var15.hasNext()) {
                    resDir = (String) var15.next();
                    WritableSheet sheetWithCheckDatabase = workbookWithCheckDatabase.createSheet(resDir, 0);
                    Label labelWithCheckDatabase = new Label(0, 0, "String Name");
                    sheetWithCheckDatabase.addCell(labelWithCheckDatabase);
                    Label pLabelWithCheckDatabase = new Label(1, 0, "App Path");
                    sheetWithCheckDatabase.addCell(pLabelWithCheckDatabase);
                    Label valueBaseLabel = new Label(2, 0, valueBase);
                    sheetWithCheckDatabase.addCell(valueBaseLabel);
                    Label contentLabel = new Label(3, 0, resDir);
                    sheetWithCheckDatabase.addCell(contentLabel);
                }

                workbookWithCheckDatabase.write();
                workbookWithCheckDatabase.close();
                var15 = resDirPathSet.iterator();

                while (var15.hasNext()) {
                    resDir = (String) var15.next();
                    workbookWithCheckDatabase = Workbook.createWorkbook(xmlFileWithCheckDatabase, Workbook.getWorkbook(xmlFileWithCheckDatabase));
                    collectAllString(filePath, resDir, valuesSet, workbookWithCheckDatabase, filterMap);
                    workbookWithCheckDatabase.write();
                    workbookWithCheckDatabase.close();
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

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableWorkbook workbookWithCheckDatabase, Map<String, Boolean> filterMap) {
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
                                writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
                            }

                            tempStringNames.clear();
                            var11 = null;
                        }

                        tempStringNames.add(key);
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
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
                                    writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
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
                        writeItems(tempStringNames, resDir, valuesResource, valuesSet, valuesResourceMap, valueBase, translationStringDatabase, workbookWithCheckDatabase);
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

    private static void writeItems(List<String> tempStringNames, String resDir, Map<String, String> valuesResource, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap, String valueBase, TranslationStringDatabase translationStringDatabase, WritableWorkbook workbookWithCheckDatabase) throws RowsExceededException, WriteException {
        Iterator var9 = tempStringNames.iterator();

        label38:
        while (var9.hasNext()) {
            String key = (String) var9.next();
            Map<String, String> notTranslatedMap = EnviromentBuilder.getNotTranslatedMap(key, valuesSet, valuesResourceMap);
            notTranslatedMap.put(valueBase, (String) valuesResource.get(key));
            Utils.logout("KEY!:" + key);
            Utils.logout(notTranslatedMap.toString());
            Map<String, Map<String, String>> notTranslatedStringsMap = new HashMap();
            notTranslatedStringsMap.put(key, notTranslatedMap);
            translationStringDatabase.getTranslatedStringsMap(notTranslatedStringsMap, valueBase);
            Utils.logout("KEY2:" + key);
            Utils.logout(notTranslatedMap.toString());
            Iterator var13 = valuesSet.iterator();

            while (true) {
                while (true) {
                    if (!var13.hasNext()) {
                        continue label38;
                    }

                    String str = (String) var13.next();
                    String temp = "";
                    if (valuesResourceMap.get(str) != null && ((Map) valuesResourceMap.get(str)).get(key) != null) {
                        temp = (String) ((Map) valuesResourceMap.get(str)).get(key);
                    }

                    if (temp != null && temp.length() != 0) {
                        outputValuesSet(workbookWithCheckDatabase, str, key, resDir, (String) valuesResource.get(key), temp);
                    } else if (notTranslatedMap.get(str) != null && ((String) notTranslatedMap.get(str)).length() != 0) {
                        outputValuesSet(workbookWithCheckDatabase, str, key, resDir, (String) valuesResource.get(key), (String) notTranslatedMap.get(str));
                    } else {
                        outputValuesSet(workbookWithCheckDatabase, str, key, resDir, (String) valuesResource.get(key), temp);
                    }
                }
            }
        }

    }
}

