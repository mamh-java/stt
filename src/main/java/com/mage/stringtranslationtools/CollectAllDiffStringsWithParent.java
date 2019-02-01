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
    public CollectAllDiffStringsWithParent() {
    }

    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String filterFileName = "strcheck_filter.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePath = "allUntranslatedStrings.xls";
        if (EnviromentBuilder.isValidArgs(args)) {
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

                String resDir;
                Iterator var16;
                for(var16 = valuesSet.iterator(); var16.hasNext(); ++count) {
                    resDir = (String)var16.next();
                    Label contentLabel = new Label(count, 0, resDir);
                    sheet.addCell(contentLabel);
                }

                workbook.write();
                workbook.close();
                var16 = resDirPathSet.iterator();

                while(var16.hasNext()) {
                    resDir = (String)var16.next();
                    workbook = Workbook.createWorkbook(xmlFile, Workbook.getWorkbook(xmlFile));
                    sheet = workbook.getSheet(0);
                    collectAllString(filePath, resDir, valuesSet, sheet, filterMap);
                    workbook.write();
                    workbook.close();
                }
            } catch (Exception var18) {
                var18.printStackTrace();
            }

        }
    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableSheet sheet, Map<String, Boolean> filterMap) {
        List<String> keys = new ArrayList();
        Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + (String)valuesSet.get(0), keys);
        Map<String, Map<String, String>> valuesResourceMap = new HashMap();
        valuesSet = valuesSet.subList(1, valuesSet.size());
        Iterator var9 = valuesSet.iterator();

        String key;
        while(var9.hasNext()) {
            key = (String) var9.next();
            Map<String, String> valuesResourceTemp = new HashMap();
            int index = key.indexOf("-");

            while(index != -1) {
                index = key.indexOf("-", index + 1);
                if (index == -1) {
                    break;
                }

                key = key.substring(0, index);
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, (List)null));
            }

            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, (List)null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }

        List<String> tempStringNames = new ArrayList();
        String lastName = null;

        try {
            Iterator var16 = keys.iterator();

            while(true) {
                while(var16.hasNext()) {
                    key = (String)var16.next();
                    if (!key.startsWith("A:") && !key.startsWith("P:")) {
                        if (tempStringNames.size() != 0) {
                            if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                                writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
                            }

                            tempStringNames.clear();
                            var9 = null;
                        }

                        tempStringNames.add(key);
                        if (EnviromentBuilder.isArrayNotTranslated(tempStringNames, valuesResource, resDir, filterMap, valuesSet, valuesResourceMap)) {
                            writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
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
                                    writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
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
                        writeItems(tempStringNames, sheet, resDir, valuesResource, valuesSet, valuesResourceMap);
                    }

                    tempStringNames.clear();
                    var9 = null;
                }
                break;
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        }

    }

    private static void writeItems(List<String> tempStringNames, WritableSheet sheet, String resDir, Map<String, String> valuesResource, List<String> valuesSet, Map<String, Map<String, String>> valuesResourceMap) throws RowsExceededException, WriteException {
        int count = sheet.getRows();

        for(Iterator var8 = tempStringNames.iterator(); var8.hasNext(); ++count) {
            String key = (String)var8.next();
            Label labelKey = new Label(0, count, key);
            Label labelPath = new Label(1, count, resDir);
            Label labelValue = new Label(2, count, (String)valuesResource.get(key));
            sheet.addCell(labelKey);
            sheet.addCell(labelPath);
            sheet.addCell(labelValue);
            int verCount = 3;

            for(Iterator var14 = valuesSet.iterator(); var14.hasNext(); ++verCount) {
                String str = (String)var14.next();
                String temp = "";
                if (valuesResourceMap.get(str) != null && ((Map)valuesResourceMap.get(str)).get(key) != null) {
                    temp = (String)((Map)valuesResourceMap.get(str)).get(key);
                }

                Label contentLabel = new Label(verCount, count, temp);
                sheet.addCell(contentLabel);
            }
        }

    }
}

