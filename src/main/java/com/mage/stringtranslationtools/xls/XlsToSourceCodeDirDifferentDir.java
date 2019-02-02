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
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class XlsToSourceCodeDirDifferentDir {
    private static final boolean ISSORTED = false;

    public XlsToSourceCodeDirDifferentDir() {
    }

    public static void doCollectAllStrings(String[] args) {
        if (EnviromentBuilder.isValidArgsFour(args)) {
            return;
        }
        File xlsFile = new File(args[1]);
        File xmlFileDir = new File(args[2]);
        File xmlFileOutDir = new File(args[3]);

        try {
            Workbook workbook = Workbook.getWorkbook(xlsFile);
            boolean isOneSheet = false;
            String[] sheetNames = workbook.getSheetNames();
            for (String sheetName : sheetNames) {
                if (sheetName.startsWith("strings")) {
                    isOneSheet = true;
                }
            }

            if (isOneSheet) {
                processOneSheet(xlsFile, xmlFileDir, xmlFileOutDir);
            } else {
                processSeperateSheet(xlsFile, xmlFileDir, xmlFileOutDir);
            }
        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }


    }

    private static void processSeperateSheet(File xlsFile, File xmlFileDir, File xmlFileOutDir) {
        try {
            Workbook workbook = Workbook.getWorkbook(xlsFile);
            String[] sheetNames = workbook.getSheetNames();
            for (String sheetName : sheetNames) {
                if (sheetName.startsWith("values-")) {
                    extractXMLFromOneSheet(workbook.getSheet(sheetName), xmlFileDir, xmlFileOutDir);
                }
            }
        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }

    }

    private static void processOneSheet(File xlsFile, File xmlFileDir, File xmlFileOutDir) {
        try {
            Workbook workbook = Workbook.getWorkbook(xlsFile);
            extractXMLFromOneSheet(workbook.getSheet("strings"), xmlFileDir, xmlFileOutDir);
        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }

    }

    private static void extractXMLFromOneSheet(Sheet sheet, File xmlFileDir, File xmlFileOutDir) {
        String pastPath = null;
        List<Item> items = new ArrayList();
        int column = sheet.getColumns();
        List<String> valuesSet = new ArrayList();

        for (int i = 3; i < column; ++i) {
            String valuesDir = sheet.getCell(i, 0).getContents();
            if (valuesDir != null && valuesDir.length() != 0) {
                Utils.logout("COLUMN:" + sheet.getCell(i, 0).getContents());
                valuesSet.add(sheet.getCell(i, 0).getContents());
            }
        }

        int columnCount = 3;
        for (Iterator iterator = valuesSet.iterator(); iterator.hasNext(); ++columnCount) {
            String valuesDir = (String) iterator.next();

            pastPath = sheet.getCell(1, 1).getContents();

            int lineCounter = sheet.getRows();

            for (int i = 1; i < lineCounter; ++i) {
                String name = sheet.getCell(0, i).getContents();
                if (name != null && name.length() != 0) {
                    String path = sheet.getCell(1, i).getContents();
                    String stringBase = sheet.getCell(2, i).getContents();
                    String stringTranslation = sheet.getCell(columnCount, i).getContents();
                    if (path.equals(pastPath)) {
                    } else {
                        writeItemsToXML(items, valuesDir, xmlFileDir, xmlFileOutDir);
                        items = new ArrayList<>();
                    }

                    pastPath = path;
                    items.add(new Item(name, path, stringBase, stringTranslation));
                }
            }

            writeItemsToXML(items, valuesDir, xmlFileDir, xmlFileOutDir);

            items = new ArrayList<>();
        }

    }

    public static void writeItemsToXML(List<Item> items, String valuesDir, File fileDirBase, File fileDirBaseOut) {
        if (items.size() != 0) {
            String resPath = ((Item) items.get(0)).getPath();
            resPath = resPath.replace('/', File.separatorChar);
            String fileDir = resPath + File.separator + "res" + File.separator + valuesDir;
            File file = new File(fileDirBase, fileDir);
            File fileOut = new File(fileDirBaseOut, fileDir);
            Map<String, String> valuesResource = EnviromentBuilder.readStringValueFromDir(file.getAbsolutePath(), (List) null);
            if (items.size() != 0) {
                StringBuffer buffer = new StringBuffer();

                try {
                    if (!fileOut.exists()) {
                        fileOut.mkdirs();
                    }

                    buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                    buffer.append(System.getProperty("line.separator"));
                    buffer.append("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">");
                    buffer.append(System.getProperty("line.separator"));
                    List<Item> itemsTemp = null;
                    String lastName = null;

                    for (Item item : items) {
                        if (item.getName().startsWith("S:")) {
                            writeItemToResources(itemsTemp, buffer, valuesResource);
                            itemsTemp = new ArrayList<>();
                            lastName = item.getName();
                            itemsTemp.add(item);
                        } else if (item.getName().startsWith("P:")) {
                            String itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                            if (itemName.equals(lastName)) {
                                lastName = itemName;
                                itemsTemp.add(item);
                            } else {
                                writeItemToResources(itemsTemp, buffer, valuesResource);
                                itemsTemp = new ArrayList<>();
                                lastName = itemName;
                                itemsTemp.add(item);
                            }
                        } else if (item.getName().startsWith("A:")) {
                            String itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                            if (itemName.equals(lastName)) {
                                lastName = itemName;
                                itemsTemp.add(item);
                            } else {
                                writeItemToResources(itemsTemp, buffer, valuesResource);
                                itemsTemp = new ArrayList<>();
                                lastName = itemName;
                                itemsTemp.add(item);
                            }

                        }
                    }//end for (Item item : items)

                    writeItemToResources(itemsTemp, buffer, valuesResource);

                    buffer.append("</resources>");
                    buffer.append(System.getProperty("line.separator"));

                    BufferedWriter fw = new BufferedWriter(new FileWriter(new File(fileOut, "strings.xml")));
                    fw.write(buffer.toString());
                    fw.flush();
                    fw.close();
                } catch (IOException var15) {
                    var15.printStackTrace();
                }

            }
        }
    }

    public static void writeItemToResources(List<Item> items, StringBuffer buffer, Map<String, String> valuesResource) throws IOException {
        if (items != null && items.size() != 0) {
            return;
        }

        if (XlsToXMLDir.isItemsAllNull(items)) {
            return;
        }

        boolean isTranslated = true;
        for (Item item : items) {
            String name = item.getName();
            if (valuesResource.get(name) == null || ((String) valuesResource.get(name)).length() == 0) {
                isTranslated = false;
                break;
            }
        }

        if (isTranslated) {
            return;
        }

        String name = items.get(0).getName(); //一般这个不会是null
        if (name.startsWith("S:")) {
            writeString(items, buffer, name);
        } else if (name.startsWith("P:")) {
            writePlurlas(items, buffer, name);
        } else if (name.startsWith("A:")) {
            writeArray(items, buffer, name);
        }


    }

    private static void writeArray(List<Item> items, StringBuffer buffer, String name) {
        String stringName = name.substring(name.indexOf(":") + 1, name.lastIndexOf(":"));

        String productName = null;
        if (stringName.contains(":")) {
            String[] strs = stringName.split(":");
            productName = strs[0];
            stringName = strs[1];
        }

        buffer.append("    ");
        buffer.append("<string-array name=\"");
        buffer.append(stringName);
        if (productName != null) {
            buffer.append("\" product=\"");
            buffer.append(productName);
        }

        buffer.append("\">");
        buffer.append(System.getProperty("line.separator"));

        for (Item item : items) {
            buffer.append("        ");
            buffer.append("<item>");
            buffer.append("\"").append(item.getStringTranslation()).append("\"");
            buffer.append("</item>");
            buffer.append(System.getProperty("line.separator"));
        }

        buffer.append("    ");
        buffer.append("</string-array>");
        buffer.append(System.getProperty("line.separator"));
    }

    private static void writePlurlas(List<Item> items, StringBuffer buffer, String name) {
        String stringName = name.substring(name.indexOf(":") + 1, name.lastIndexOf(":"));

        String productName = null;
        if (stringName.contains(":")) {
            String[] strs = stringName.split(":");
            productName = strs[0];
            stringName = strs[1];
        }

        buffer.append("    ");
        buffer.append("<plurals name=\"");
        buffer.append(stringName);
        if (productName != null) {
            buffer.append("\" product=\"");
            buffer.append(productName);
        }

        buffer.append("\">");
        buffer.append(System.getProperty("line.separator"));

        for (Item item : items) {
            String itemName = item.getName();
            String pluralsQuantity = itemName.substring(itemName.lastIndexOf(":") + 1);
            buffer.append("        ");
            buffer.append("<item quantity=\"");
            buffer.append(pluralsQuantity);
            buffer.append("\">");
            buffer.append("\"").append(item.getStringTranslation()).append("\"");
            buffer.append("</item>");
            buffer.append(System.getProperty("line.separator"));
        }

        buffer.append("    ");
        buffer.append("</plurals>");
        buffer.append(System.getProperty("line.separator"));
    }

    private static void writeString(List<Item> items, StringBuffer buffer, String name) {
        String stringName = name.substring(name.indexOf(":") + 1);

        String productName = null;
        if (stringName.contains(":")) {
            String[] strs = stringName.split(":");
            productName = strs[0];
            stringName = strs[1];
        }

        buffer.append("    ");
        buffer.append("<string name=\"");
        buffer.append(stringName);

        if (productName != null) {
            buffer.append("\" product=\"");
            buffer.append(productName);
        }

        buffer.append("\">");
        for (Item item : items) { // 这个items 列表只会有一个元素的，这里也使用个循环，和其他2个方法类似
            buffer.append("\"").append(item.getStringTranslation()).append("\"");
        }

        buffer.append("</string>");
        buffer.append(System.getProperty("line.separator"));
    }


}

