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

import org.apache.commons.collections.CollectionUtils;

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

/**
 * String Name	            App Path        备注      values                          values-zh-rCN
 * S:exlight_settings      /ES                     Lighting effect setting         灯效设置
 * S:exlight_logo_scene    /ES                     Logo scenario                   LOGO场景
 * S:aaa                   /OTA                    Lighting effect setting         灯效设置
 * S:bbb                   /OTA                    Logo scenario                   LOGO场景
 * S:aaa                   /push                   Lighting effect setting         灯效设置
 * S:bbb                   /push                   Logo scenario                   LOGO场景
 */
public class XlsToXMLDir {
    private static final boolean ISSORTED = false;
    private static final String STRINGS_SHEET_NAME = "strings";
    private static final String VALUES_SHEET_NAME = "values";

    private static final String STRING_PREFIX = "S:";
    private static final String ARRAY_PREFIX = "A:";
    private static final String PLURALS_PREFIX = "P:";


    /**
     * xml文件的 声明行的内容
     */
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    private static final String XML_FILE_NAME = "strings.xml";
    /**
     * strings.xml 中的主标签<resources> </resources>,
     */
    private static final String XML_RESOURCES_BEGIN = "<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">";
    private static final String XML_RESOURCES_END = "</resources>";

    public XlsToXMLDir() {
    }

    public static void doCollectAllStrings(String[] args) {
        if (EnviromentBuilder.isValidArgsThree(args)) {//检查args是否合法，需要是长度大于2的
            return;
        }
        File xlsFile = new File(args[1]);
        File xmlFileDir = new File(args[2]);

        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding("ISO-8859-15"); //关键代码，解决中文乱码
            Workbook workbook = Workbook.getWorkbook(xlsFile, workbookSettings);
            boolean isOneSheet = false;
            String[] sheetNames = workbook.getSheetNames();
            for (String sheetName : sheetNames) {
                if (sheetName.startsWith(STRINGS_SHEET_NAME)) {
                    isOneSheet = true; // 如果是strings开头的sheet名称。
                    // sheet是显示在workbook窗口中的表格。一个sheet可以由1048576行和2464列构成。
                }
            }

            if (isOneSheet) {
                processOneSheet(workbook, xmlFileDir); // 处理只有一个表格的情况
            } else {
                processSeperateSheet(workbook, xmlFileDir);//处理多个表格的情况，也就是表格拆分存放的情况
            }
        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 处理多个表格的情况，也就是表格拆分存放的情况
     *
     * @param workbook   excel workbook
     * @param xmlFileDir 输出的strings.xml 目录
     */
    private static void processSeperateSheet(Workbook workbook, File xmlFileDir) {
        String[] sheetNames = workbook.getSheetNames();
        for (String sheetName : sheetNames) {
            if (sheetName.startsWith(VALUES_SHEET_NAME)) {
                extractXMLFromOneSheet(workbook.getSheet(sheetName), xmlFileDir);
            }
        }
    }

    /**
     * 处理只有一个表格的情况
     *
     * @param workbook   excel 文件
     * @param xmlFileDir 输出的strings.xml 目录
     */
    private static void processOneSheet(Workbook workbook, File xmlFileDir) {
        extractXMLFromOneSheet(workbook.getSheet(STRINGS_SHEET_NAME), xmlFileDir);
    }

    private static void extractXMLFromOneSheet(Sheet sheet, File xmlFileDir) {
        List<Item> items = new ArrayList<>();

        int column = sheet.getColumns();//工作簿列数

        List<String> valuesSet = new ArrayList<>();//存放语言种类

        int columnCount;
        for (columnCount = 3; columnCount < column; ++columnCount) {
            String valuesDir = sheet.getCell(columnCount, 0).getContents();
            if (valuesDir != null && valuesDir.length() != 0) {
                valuesSet.add(sheet.getCell(columnCount, 0).getContents());//第1行，从第4列开始
            }
        }

        columnCount = 3;
        for (Iterator iterator = valuesSet.iterator(); iterator.hasNext(); ++columnCount) {
            String valuesDir = (String) iterator.next();

            String pastPath = sheet.getCell(1, 1).getContents();// 先获取第2行2列的那个 app path的值

            int lineCounter = sheet.getRows();//获取行数

            for (int i = 1; i < lineCounter; ++i) { //遍历sheet中的每一行
                String name = sheet.getCell(0, i).getContents();//getCell(int column, int row); 获取1列， i 行，i从第一行开始，表示的是 String Name

                if (name != null && name.length() != 0) {
                    String path = sheet.getCell(1, i).getContents();// 第2列，i行单元格的内容，第2列表示 APP Path

                    String stringBase = sheet.getCell(2, i).getContents();//第3列，表示备注啊

                    String stringTranslation = sheet.getCell(columnCount, i).getContents();//从第4列开始是values各个语言对应的字符串值


                    if (path.equals(pastPath)) {// 如果当前path等于之前的那个path，也就是当前行app path等于上一行的，说明还是在一个app里面的，
                    } else {// 到了这里不相等了，说明换另外一个app了
                        writeItemsToXML(items, valuesDir, xmlFileDir);//不相等了说明一个app的一个语言的解析完毕，可以写入xml文件了
                        items = new ArrayList<>();
                    }

                    pastPath = path;
                    items.add(new Item(name, path, stringBase, stringTranslation));
                }
            }
            writeItemsToXML(items, valuesDir, xmlFileDir);
            items = new ArrayList<>();
        }

    }

    private static void writeItemsToXML(List<Item> items, String valuesDir, File fileDirBase) {
        if (CollectionUtils.isEmpty(items)) {// 如果 集合list是空就直接返回
            return;
        }

        try {
            String resPath = items.get(0).getPath();

            resPath = resPath.replace('/', File.separatorChar);

            // 拼接出来一个类似  \ExLightService\res\values 这样的路径
            String fileDir = resPath + File.separator + "res" + File.separator + valuesDir;
            File file = new File(fileDirBase, fileDir);

            if (!file.exists()) {
                boolean ret = file.mkdirs();
            }

            BufferedWriter fw = new BufferedWriter(new FileWriter(new File(file, XML_FILE_NAME)));

            fw.write(XML_DECLARATION);
            fw.newLine();
            fw.write(XML_RESOURCES_BEGIN);
            fw.newLine();

            List<Item> itemsTemp = null;
            String lastName = null;

            for (Item item : items) {
                if (item.getName().startsWith(STRING_PREFIX)) {// S: 开头的是 表示字符串的 要存放到 <string name=""></string> 标签中的
                    //第一次这个itemsTemp是null,每次都存放一个item，存放好下次循环过来就会调用 writeItemToResources()方法写入文件
                    writeItemToResources(itemsTemp, fw);

                    itemsTemp = new ArrayList<>();
                    lastName = item.getName();
                    itemsTemp.add(item);
                } else if (item.getName().startsWith(PLURALS_PREFIX)) {
                    String itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
                    if (itemName.equals(lastName)) { // 也是在itemName 变化的情况下写入文件
                        lastName = itemName;
                        itemsTemp.add(item); // itemsTemp 对于 array会存放多个item的
                    } else {
                        writeItemToResources(itemsTemp, fw);
                        itemsTemp = new ArrayList<>();
                        lastName = itemName;
                        itemsTemp.add(item);
                    }
                } else if (item.getName().startsWith(ARRAY_PREFIX)) {
                    String itemName = item.getName().substring(0, item.getName().lastIndexOf(":"));
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

            }//end while()

            writeItemToResources(itemsTemp, fw);
            fw.write(XML_RESOURCES_END);
            fw.newLine();
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void writeItemToResources(List<Item> items, BufferedWriter bufferedWriter) throws IOException {
        if (CollectionUtils.isEmpty(items)) {// 如果 集合list是空就直接返回
            return;
        }
        if (isItemsAllNull(items)) {//items 集合里面的元素都是null
            return;
        }

        String name = items.get(0).getName(); //一般这个不会是null
        if (name.startsWith(STRING_PREFIX)) {
            writeString(items, bufferedWriter, name);
        } else if (name.startsWith(PLURALS_PREFIX)) {
            writePlurals(items, bufferedWriter, name);
        } else if (name.startsWith(ARRAY_PREFIX)) {
            writeArray(items, bufferedWriter, name);
        }
    }

    private static void writeArray(List<Item> items, BufferedWriter bufferedWriter, String name) throws IOException {
        String stringName = name.substring(name.indexOf(":") + 1, name.lastIndexOf(":"));

        String productName = null;
        if (stringName.contains(":")) {
            String[] strs = stringName.split(":");
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

        for (Item item : items) {
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

    private static void writePlurals(List<Item> items, BufferedWriter bufferedWriter, String name) throws IOException {
        String stringName = name.substring(name.indexOf(":") + 1, name.lastIndexOf(":"));

        String productName = null;
        if (stringName.contains(":")) {
            String[] strs = stringName.split(":");
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

        for (Item item : items) {
            String itemName = item.getName();
            String pluralsQuantity = itemName.substring(itemName.lastIndexOf(":") + 1);
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

    private static void writeString(List<Item> items, BufferedWriter bufferedWriter, String name) throws IOException {
        String stringName = name.substring(name.indexOf(":") + 1);

        String productName = null;
        if (stringName.contains(":")) {
            String[] strs = stringName.split(":");
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

        for (Item item : items) { // 这个items 列表只会有一个元素的，这里也使用个循环，和其他2个方法类似
            bufferedWriter.write("\"" + item.getStringTranslation() + "\""); //插入 到 <string></string> 标签之间的值
        }

        bufferedWriter.write("</string>");
        bufferedWriter.newLine();
    }

    /**
     * 判断Item 集合list中是否都是null,只要集合中有一个item不是null,
     * 并且其中的属性getStringTranslation()也不是null，空字符串
     *
     * @param items
     * @return
     */
    public static boolean isItemsAllNull(List<Item> items) {
        for (Item item : items) {
            if ((item != null) && (item.getStringTranslation() != null) && (item.getStringTranslation().length() > 0)) {
                return false;
            }
        }
        return true;
    }
}

