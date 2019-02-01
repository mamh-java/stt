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

package com.mage.stringtranslationtools.xls;


import com.mage.stringtranslationtools.EnviromentBuilder;
import com.mage.stringtranslationtools.Item;
import com.mage.stringtranslationtools.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

public class XlsToXMLDir {
    private static final boolean ISSORTED = false;
    private static final String SHEET_NAME = "strings";

    public XlsToXMLDir() {
    }

    public static void doCollectAllStrings(String[] args) {
        if (EnviromentBuilder.isValidArgsThree(args)) {//检查args是否合法，需要是长度大于2的
            File xlsFile = new File(args[1]);
            File xmlFileDir = new File(args[2]);

            try {
                Workbook workbook = Workbook.getWorkbook(xlsFile);
                boolean isOneSheet = false;
                String[] sheetNames = workbook.getSheetNames();
                for (String sheetName : sheetNames) {
                    if (sheetName.startsWith(SHEET_NAME)) {
                        isOneSheet = true; // 如果是strings开头的sheet名称。 sheet是显示在workbook窗口中的表格。一个sheet可以由1048576行和2464列构成。
                    }
                }

                if (isOneSheet) {
                    processOneSheet(xlsFile, xmlFileDir); // 处理只有一个表格的情况
                } else {
                    processSeperateSheet(xlsFile, xmlFileDir);//处理多个表格的情况，也就是表格拆分存放的情况
                }
            } catch (BiffException | IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 处理多个表格的情况，也就是表格拆分存放的情况
     *
     * @param xlsFile    excel 文件
     * @param xmlFileDir 输出的strings.xml 目录
     */
    private static void processSeperateSheet(File xlsFile, File xmlFileDir) {
        try {
            Workbook workbook = Workbook.getWorkbook(xlsFile);
            String[] sheetNames = workbook.getSheetNames();
            for (String sheetName : sheetNames) {
                if (sheetName.startsWith("values-")) {
                    extractXMLFromOneSheet(workbook.getSheet(sheetName), xmlFileDir);
                }
            }
        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理只有一个表格的情况
     *
     * @param xlsFile    excel 文件
     * @param xmlFileDir 输出的strings.xml 目录
     */
    private static void processOneSheet(File xlsFile, File xmlFileDir) {
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding("ISO-8859-15"); //关键代码，解决中文乱码
            Workbook workbook = Workbook.getWorkbook(xlsFile, workbookSettings);
            extractXMLFromOneSheet(workbook.getSheet("strings"), xmlFileDir);
        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }

    }

    private static void extractXMLFromOneSheet(Sheet sheet, File xmlFileDir) {
        String pastPath = null;
        List<Item> items = new ArrayList<>();
        int column = sheet.getColumns();
        List<String> valuesSet = new ArrayList<>();

        int columnCount;
        String valuesDir;
        for (columnCount = 2; columnCount < column; ++columnCount) {
            valuesDir = sheet.getCell(columnCount, 0).getContents();
            if (valuesDir != null && valuesDir.length() != 0) {
                Utils.logout("COLUMN:" + sheet.getCell(columnCount, 0).getContents());
                valuesSet.add(sheet.getCell(columnCount, 0).getContents());
            }
        }

        valuesSet = valuesSet.subList(1, valuesSet.size());
        columnCount = 3;

        for (Iterator var8 = valuesSet.iterator(); var8.hasNext(); ++columnCount) {
            valuesDir = (String) var8.next();
            pastPath = sheet.getCell(1, 1).getContents();
            int lineCounter = sheet.getRows();

            for (int i = 1; i < lineCounter; ++i) {
                String name = sheet.getCell(0, i).getContents();
                if (name != null && name.length() != 0) {
                    String path = sheet.getCell(1, i).getContents();
                    String stringBase = sheet.getCell(2, i).getContents();
                    String stringTranslation = sheet.getCell(columnCount, i).getContents();

                    Utils.logout("string: " + name + ", path: " + path + ", stringBase:" + stringBase + ", stringTranslation:" + stringTranslation + ", pastPath:" + pastPath);

                    if (path.equals(pastPath)) {
                        Utils.logout("XXXX");
                    } else {
                        Utils.logout("YYYY");
                        writeItemsToXML(items, valuesDir, xmlFileDir);
                        items = new ArrayList<>();
                    }

                    pastPath = path;
                    items.add(new Item(name, path, stringBase, stringTranslation));
                }
            }

            writeItemsToXML(items, valuesDir, xmlFileDir);
            pastPath = null;
            items = new ArrayList<>();
        }

    }

    public static void writeItemsToXML(List<Item> items, String valuesDir, File fileDirBase) {
        if (items.size() != 0) {
            try {
                String resPath = ((Item) items.get(0)).getPath();
                resPath = resPath.replace('/', File.separatorChar);
                String fileDir = resPath + File.separator + "res" + File.separator + valuesDir;
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
                        itemsTemp = new ArrayList<>();
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
                                itemsTemp = new ArrayList<>();
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
                                itemsTemp = new ArrayList<>();
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
        if (items != null && items.size() != 0) {
            if (!isItemsAllNull(items)) {
                Item itemFirst = (Item) items.get(0);
                String stringName;
                String productName;
                String[] strs;
                if (itemFirst.getName().startsWith("S:")) {
                    stringName = itemFirst.getName().substring(itemFirst.getName().indexOf(":") + 1, itemFirst.getName().length());
                    productName = null;
                    if (stringName.indexOf(":") >= 0) {
                        strs = stringName.split(":");
                        productName = strs[0];
                        stringName = strs[1];
                    }

                    bufferedWriter.write("    ");
                    bufferedWriter.write("<string name=\"");
                    bufferedWriter.write(stringName);
                    if (productName != null) {
                        bufferedWriter.write("\" product=\"");
                        bufferedWriter.write(productName);
                    }

                    bufferedWriter.write("\">");
                    bufferedWriter.write("\"" + itemFirst.getStringTranslation() + "\"");
                    bufferedWriter.write("</string>");
                    bufferedWriter.newLine();
                }

                Iterator var6;
                Item item;
                if (itemFirst.getName().startsWith("P:")) {
                    stringName = itemFirst.getName().substring(itemFirst.getName().indexOf(":") + 1, itemFirst.getName().lastIndexOf(":"));
                    productName = null;
                    if (stringName.indexOf(":") >= 0) {
                        strs = stringName.split(":");
                        productName = strs[0];
                        stringName = strs[1];
                    }

                    bufferedWriter.write("    ");
                    bufferedWriter.write("<plurals name=\"");
                    bufferedWriter.write(stringName);
                    if (productName != null) {
                        bufferedWriter.write("\" product=\"");
                        bufferedWriter.write(productName);
                    }

                    bufferedWriter.write("\">");
                    bufferedWriter.newLine();
                    var6 = items.iterator();

                    while (var6.hasNext()) {
                        item = (Item) var6.next();
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
                    stringName = itemFirst.getName().substring(itemFirst.getName().indexOf(":") + 1, itemFirst.getName().lastIndexOf(":"));
                    productName = null;
                    if (stringName.indexOf(":") >= 0) {
                        strs = stringName.split(":");
                        productName = strs[0];
                        stringName = strs[1];
                    }

                    bufferedWriter.write("    ");
                    bufferedWriter.write("<string-array name=\"");
                    bufferedWriter.write(stringName);
                    if (productName != null) {
                        bufferedWriter.write("\" product=\"");
                        bufferedWriter.write(productName);
                    }

                    bufferedWriter.write("\">");
                    bufferedWriter.newLine();
                    var6 = items.iterator();

                    while (var6.hasNext()) {
                        item = (Item) var6.next();
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

    public static boolean isItemsAllNull(List<Item> items) {
        Iterator var2 = items.iterator();

        Item item;
        do {
            if (!var2.hasNext()) {
                return true;
            }

            item = (Item) var2.next();
        } while (item.getStringTranslation() == null || item.getStringTranslation().length() <= 0);

        return false;
    }
}

