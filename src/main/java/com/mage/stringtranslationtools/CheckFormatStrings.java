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

public class CheckFormatStrings {
    public CheckFormatStrings() {
    }

    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        String xmlFilePath = "allFormattedStrings.xls";

        if (EnviromentBuilder.isValidArgsTwo(args)) {
            return;
        }

        String filePath = args[1];

        Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
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
            for (Iterator iterator = valuesSet.iterator(); iterator.hasNext(); ++count) {
                String resDir = (String) iterator.next();
                Label contentLabel = new Label(count, 0, resDir);
                sheet.addCell(contentLabel);
            }

            workbook.write();
            workbook.close();

            for (String resDir : resDirPathSet) {
                workbook = Workbook.createWorkbook(xmlFile, Workbook.getWorkbook(xmlFile));
                sheet = workbook.getSheet(0);
                collectAllString(filePath, resDir, valuesSet, sheet);
                workbook.write();
                workbook.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableSheet sheet) {
        List<String> keys = new ArrayList<>();
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
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + temp, (List) null));
            }

            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(filePath + resDir + File.separator + "res" + File.separator + key, (List) null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }

        try {
            int count = sheet.getRows();
            for (String key : keys) {
                try {
                    String baseString = valuesResource.get(key);
                    boolean isError = false;

                    int verCount = 3;
                    for (String str : valuesSet) {
                        String temp = "";
                        if (valuesResourceMap.get(str) != null && ((Map) valuesResourceMap.get(str)).get(key) != null) {
                            temp = (String) ((Map) valuesResourceMap.get(str)).get(key);
                        }

                        if (temp != null && temp.length() != 0 && checkError(baseString, temp)) {
                            Label contentLabel = new Label(verCount, count, temp);
                            sheet.addCell(contentLabel);
                            isError = true;
                        }
                        verCount++;
                    }

                    if (isError) {
                        Label labelKey = new Label(0, count, key);
                        Label labelPath = new Label(1, count, resDir);
                        Label labelValue = new Label(2, count, valuesResource.get(key));
                        sheet.addCell(labelKey);
                        sheet.addCell(labelPath);
                        sheet.addCell(labelValue);
                        count++;
                    }
                } catch (Exception e) {
                    Label labelPath = new Label(1, count, resDir);
                    Label labelKey = new Label(2, count, valuesResource.get(key));
                    sheet.addCell(labelKey);
                    sheet.addCell(labelPath);
                    sheet.addCell(labelKey);

                    int verCount = 3;

                    for (String str : valuesSet) {
                        String temp = "";
                        if ((valuesResourceMap.get(str) != null) && (((Map) valuesResourceMap.get(str)).get(key) != null)) {
                            temp = (String) ((Map) valuesResourceMap.get(str)).get(key);
                        }
                        Label contentLabel = new Label(verCount, count, temp);
                        sheet.addCell(contentLabel);
                        verCount++;
                    }

                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean checkError(String baseString, String translatedString) {
        List<String> formatStringList = Formatter.scanFormatStrings(baseString);
        if (formatStringList != null && formatStringList.size() != 0 && checkFormatedStringError(translatedString, formatStringList)) {
            return true;
        } else {
            return checkXliffError(baseString, translatedString);
        }
    }

    public static boolean checkXliffError(String baseString, String translatedString) {
        int xliffCount = 0;
        int index = baseString.indexOf("<xliff:g");
        while (index >= 0) {
            xliffCount++;
            index = baseString.indexOf("<xliff:g", baseString.indexOf("</xliff:g>", index) + "</xliff:g>".length());
        }
        int xliffCountLogic = 0;
        int indexLogic = translatedString.indexOf("<xliff:g");
        while (indexLogic >= 0) {
            xliffCountLogic++;
            indexLogic = translatedString.indexOf("<xliff:g", translatedString.indexOf("</xliff:g>", indexLogic) + "</xliff:g>".length());
        }
        if (xliffCount != xliffCountLogic) {
            return true;
        }
        return false;
    }

    public static boolean checkFormatedStringError(String translatedString, List<String> formatStringList) {
        for (String formatString : formatStringList) {
            int index = translatedString.indexOf(formatString);
            if (index < 0) {
                return true;
            }
            translatedString = translatedString.substring(0, index) + translatedString.substring(index + formatString.length());
        }
        return false;
    }
}

