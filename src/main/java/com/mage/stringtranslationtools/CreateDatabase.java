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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class CreateDatabase {
    public static void doCollectAllStrings(String[] args) {
        String configFileName = "all_xml.txt";
        String valuesConfigFileName = "strcheck_config.txt";
        if (!EnviromentBuilder.isValidArgs(args)) {
            return;
        }
        String filePath = args[1];
        Set<String> resDirPathSet = EnviromentBuilder.scanResDirPathList(configFileName);
        List<String> valuesSet = EnviromentBuilder.scanValuesList(valuesConfigFileName);
        File filePathFile = new File(filePath);
        try {
            for (String resDir : resDirPathSet) {
                String baseDir = String.valueOf(filePathFile.getName()) + File.separator + resDir;
                File baseDirFile = new File(baseDir);
                if (!baseDirFile.exists()) {
                    baseDirFile.mkdirs();
                }
                String xmlFilePath = "allStrings.xls";
                File xmlFile = new File(baseDirFile, xmlFilePath);
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
                workbook = Workbook.createWorkbook(xmlFile, Workbook.getWorkbook(xmlFile));
                sheet = workbook.getSheet(0);
                CreateDatabase.collectAllString(filePath, resDir, valuesSet, sheet);
                workbook.write();
                workbook.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String cmd = "zip -r " + filePathFile.getName() + ".zip " + filePathFile.getName();
        String rmcmd = "rm -rf " + filePathFile.getName();
        Runtime run = Runtime.getRuntime();
        try {
            String lineStr;
            Utils.logout("CMD:" + cmd);
            Process p = run.exec(cmd);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
            while ((lineStr = inBr.readLine()) != null) {
                Utils.logout(lineStr);
            }
            if (p.waitFor() != 0 && p.exitValue() == 1) {
                Utils.logout("\ufffd\ufffd\ufffd\ufffd\u05b4\ufffd\ufffd\u02a7\ufffd\ufffd!");
            }
            inBr.close();
            in.close();
            Utils.logout("CMD:" + rmcmd);
            p = run.exec(rmcmd);
            in = new BufferedInputStream(p.getInputStream());
            inBr = new BufferedReader(new InputStreamReader(in));
            while ((lineStr = inBr.readLine()) != null) {
                Utils.logout(lineStr);
            }
            if (p.waitFor() != 0 && p.exitValue() == 1) {
                Utils.logout("\ufffd\ufffd\ufffd\ufffd\u05b4\ufffd\ufffd\u02a7\ufffd\ufffd!");
            }
            inBr.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void collectAllString(String filePath, String resDir, List<String> valuesSet, WritableSheet sheet) {
        ArrayList<String> keys = new ArrayList<String>();
        Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(String.valueOf(filePath) + resDir + File.separator + "res" + File.separator + valuesSet.get(0), keys);
        HashMap valuesResourceMap = new HashMap();
        valuesSet = valuesSet.subList(1, valuesSet.size());
        for (String key : valuesSet) {
            Object temp = key;
            HashMap<String, String> valuesResourceTemp = new HashMap<String, String>();
            int index = ((String)temp).indexOf("-");
            while (index != -1) {
                if ((index = key.indexOf("-", index + 1)) == -1) break;
                temp = key.substring(0, index);
                valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(String.valueOf(filePath) + resDir + File.separator + "res" + File.separator + (String)temp, null));
            }
            valuesResourceTemp.putAll(EnviromentBuilder.readStringValueFromDir(String.valueOf(filePath) + resDir + File.separator + "res" + File.separator + key, null));
            valuesResourceMap.put(key, valuesResourceTemp);
        }
        try {
            int count = sheet.getRows();
            for (String key : keys) {
                Label labelKey = new Label(0, count, key);
                Label labelPath = new Label(1, count, resDir);
                Label labelValue = new Label(2, count, valuesResource.get(key));
                sheet.addCell(labelKey);
                sheet.addCell(labelPath);
                sheet.addCell(labelValue);
                int verCount = 3;
                for (String str : valuesSet) {
                    String temp = "";
                    if (valuesResourceMap.get(str) != null && ((Map)valuesResourceMap.get(str)).get(key) != null) {
                        temp = (String)((Map)valuesResourceMap.get(str)).get(key);
                    }
                    Label contentLabel = new Label(verCount, count, temp);
                    sheet.addCell(contentLabel);
                    ++verCount;
                }
                ++count;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

