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
package com.mage.stringtranslationtools.database;

import com.mage.stringtranslationtools.Utils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class TranslationStringDatabase {
    private String mDatabaseDir;
    private String mAppPath;
    private List<Map<String, Map<String, String>>> mData = new ArrayList<Map<String, Map<String, String>>>();
    private static FilenameFilter mStringZIPFileFilter = new FilenameFilter(){

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(".zip");
        }
    };

    public TranslationStringDatabase(String mDatabaseDir, String mAppPath) {
        Utils.logout("mAppPath:" + mAppPath);
        this.mDatabaseDir = mDatabaseDir;
        this.mAppPath = mAppPath;
        File[] files = new File(this.mDatabaseDir).listFiles(mStringZIPFileFilter);
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                int i;
                ZipEntry ze;
                ZipFile zf = new ZipFile(file);
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                ZipInputStream zin = new ZipInputStream(in);
                BufferedInputStream resourceInputStream = null;
                while ((ze = zin.getNextEntry()) != null) {
                    Utils.logout("FILES1:" + ze.getName().substring(ze.getName().indexOf("/")).replace("/", "\\"));
                    Utils.logout("FILES2:" + new StringBuilder(String.valueOf(this.mAppPath)).append(File.separator).append("allStrings.xls").toString().replace("/", "\\"));
                    if (!ze.getName().substring(ze.getName().indexOf("/")).replace("/", "\\").endsWith((String.valueOf(this.mAppPath) + File.separator + "allStrings.xls").replace("/", "\\"))) continue;
                    Utils.logout("FILES:" + file.getAbsolutePath() + ":NAME:" + ze.getName());
                    resourceInputStream = new BufferedInputStream(zf.getInputStream(ze));
                }
                HashMap stringsMap = new HashMap();
                if (resourceInputStream == null) {
                    zin.closeEntry();
                    zin.close();
                    return;
                }
                Workbook workbook = Workbook.getWorkbook(resourceInputStream);
                Sheet sheet = workbook.getSheet("strings");
                int lineCounter = sheet.getRows();
                int column = sheet.getColumns();
                ArrayList<String> valuesList = new ArrayList<String>();
                for (i = 2; i < column; ++i) {
                    Utils.logout("COLUMN:" + sheet.getCell(i, 0).getContents());
                    valuesList.add(sheet.getCell(i, 0).getContents());
                }
                for (i = 1; i < lineCounter; ++i) {
                    String name = sheet.getCell(0, i).getContents();
                    Utils.logout("String:" + name);
                    String path = sheet.getCell(1, i).getContents();
                    Utils.logout("Path:" + path);
                    if (!this.mAppPath.replace("/", "\\").equals(path.replace("/", "\\"))) continue;
                    HashMap<String, String> valuesMap = new HashMap<String, String>();
                    for (int j = 2; j < column; ++j) {
                        Utils.logout("CONTENT:" + (String)valuesList.get(j - 2));
                        Utils.logout("CONTENT:" + sheet.getCell(j, i).getContents());
                        valuesMap.put((String)valuesList.get(j - 2), sheet.getCell(j, i).getContents());
                    }
                    stringsMap.put(name, valuesMap);
                }
                this.mData.add(stringsMap);
                zin.closeEntry();
                zin.close();
            }
            catch (BiffException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Map<String, String>> getTranslatedStringsMap(Map<String, Map<String, String>> stringsMap, String valuesBase) {
        Utils.logout("getTranslatedStringsMap:start:" + valuesBase);
        for (String key : stringsMap.keySet()) {
            Utils.logout("getTranslatedStringsMap1:" + key);
            Map<String, String> valuesMap = stringsMap.get(key);
            for (String value : valuesMap.keySet()) {
                Utils.logout("getTranslatedStringsMap2:" + value);
                if (valuesMap.get(value) != null) continue;
                Utils.logout("getTranslatedStringsMap3:" + this.getTranslatedString(key, value, valuesBase, stringsMap.get(key).get(valuesBase)));
                valuesMap.put(value, this.getTranslatedString(key, value, valuesBase, stringsMap.get(key).get(valuesBase)));
            }
        }
        Utils.logout("getTranslatedStringsMap:end");
        return stringsMap;
    }

    public String getTranslatedString(String key, String value, String valuesBase, String valueBaseString) {
        Utils.logout("getTranslatedString:start");
        Utils.logout("getTranslatedString:start:" + key);
        Utils.logout("getTranslatedString:start:" + value);
        Utils.logout("getTranslatedString:start:" + valuesBase);
        Utils.logout("getTranslatedString:start:" + valueBaseString);
        for (Map<String, Map<String, String>> stringsMap : this.mData) {
            if (stringsMap.get(key) != null && stringsMap.get(key).get(valuesBase) != null && !stringsMap.get(key).get(valuesBase).equals(valueBaseString) || stringsMap.get(key) == null || stringsMap.get(key).get(value) == null) continue;
            return stringsMap.get(key).get(value);
        }
        Utils.logout("getTranslatedString:end");
        return null;
    }

}

