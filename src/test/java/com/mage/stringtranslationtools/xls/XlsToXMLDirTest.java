package test.com.mage.stringtranslationtools.xls;

import com.mage.stringtranslationtools.Item;
import com.mage.stringtranslationtools.xls.XlsToXMLDir;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

/**
 * XlsToXMLDir Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Feb 1, 2019</pre>
 */
public class XlsToXMLDirTest {

    private Class<?> clazz;
    private String[] args = {"", "/home/mamh/fanyi/test.xls", "test"};
    private File xmlFileDir;
    private File xlsFile;
    private Workbook workbook;

    @Before
    public void before() throws Exception {
        clazz = Class.forName("com.mage.stringtranslationtools.xls.XlsToXMLDir");

        xmlFileDir = new File("test");
        xlsFile = new File("/home/mamh/fanyi/test.xls");
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setEncoding("ISO-8859-15"); //关键代码，解决中文乱码
        workbook = Workbook.getWorkbook(xlsFile, workbookSettings);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: doCollectAllStrings(String[] args)
     */
    @Test
    public void testDoCollectAllStrings() throws Exception {
        XlsToXMLDir.doCollectAllStrings(args);
    }

    /**
     * Method: isItemsAllNull(List<Item> items)
     */
    @Test
    public void testIsItemsAllNull() throws Exception {
        List<Item> list1 = new ArrayList<>();
        boolean b1 = XlsToXMLDir.isItemsAllNull(list1);//空列表返回是true
        Assert.assertTrue(b1);

        List<Item> list2 = new ArrayList<>(10);
        list2.add(null);
        boolean b2 = XlsToXMLDir.isItemsAllNull(list2);//list 中元素是 null, 返回是true
        Assert.assertTrue(b2);

        List<Item> list3 = new ArrayList<>();
        list3.add(new Item());
        boolean b3 = XlsToXMLDir.isItemsAllNull(list3);//list 中元素是new Item(),里面的成员变量都是null,返回是true
        Assert.assertTrue(b3);

        List<Item> list4 = new ArrayList<>();
        Item item4 = new Item();
        item4.setName("44");
        list4.add(item4);
        boolean b4 = XlsToXMLDir.isItemsAllNull(list4);//list 中元素是new Item(),里面的stringTranslation成员变量都是null,返回是true
        Assert.assertTrue(b4);

        List<Item> list5 = new ArrayList<>();
        Item item5 = new Item();
        item5.setName("55");
        item5.setPath("55");
        list5.add(item5);
        boolean b5 = XlsToXMLDir.isItemsAllNull(list5);//list 中元素是new Item(),里面的stringTranslation成员变量都是null,返回是true
        Assert.assertTrue(b5);

        List<Item> list6 = new ArrayList<>();
        Item item6 = new Item();
        item6.setName("66666");
        item6.setPath("66666");
        item6.setStringBase("66666");
        list6.add(item6);
        boolean b6 = XlsToXMLDir.isItemsAllNull(list6);//list 中元素是new Item(),里面的stringTranslation成员变量都是null,返回是true
        Assert.assertTrue(b6);

        List<Item> list7 = new ArrayList<>();
        list7.add(new Item());
        list7.add(new Item("7", "7", "7", "7"));
        list7.add(new Item());
        list7.add(new Item());
        list7.add(new Item());
        boolean b7 = XlsToXMLDir.isItemsAllNull(list7);
        Assert.assertFalse(b7);

        List<Item> list8 = new ArrayList<>();
        list8.add(new Item("8", "8", "8", "8"));
        boolean b8 = XlsToXMLDir.isItemsAllNull(list8);
        Assert.assertFalse(b8);
    }

    /**
     * Method: processSeperateSheet(Workbook workbook, File xmlFileDir)
     */
    @Test
    public void testProcessSeperateSheet() throws Exception {
        try {
            Method method = clazz.getDeclaredMethod("processSeperateSheet", Workbook.class, File.class);
            method.setAccessible(true);
            method.invoke(clazz, workbook, xmlFileDir);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method: processOneSheet(Workbook workbook, File xmlFileDir)
     */
    @Test
    public void testProcessOneSheet() throws Exception {
        try {
            Method method = clazz.getDeclaredMethod("processOneSheet", Workbook.class, File.class);
            method.setAccessible(true);
            method.invoke(clazz, workbook, xmlFileDir);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method: extractXMLFromOneSheet(Sheet sheet, File xmlFileDir)
     */
    @Test
    public void testExtractXMLFromOneSheet() throws Exception {
        Sheet sheet = workbook.getSheet("strings");

        try {
            Method method = clazz.getDeclaredMethod("extractXMLFromOneSheet", Sheet.class, File.class);
            method.setAccessible(true);
            method.invoke(clazz, sheet, xmlFileDir);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method: writeItemsToXML(List<Item> items, String valuesDir, File fileDirBase)
     */
    @Test
    public void testWriteItemsToXML() throws Exception {
        String valudesDir = "values";//语言目录名
        List<Item> list = new ArrayList<>();
        //这个list的第一个元素的 属性path=valuesDir 值决定了 存放目录的值
        list.add(new Item("S:productName:exlight_settings_1", "/OTA", "", "Lighting effect setting"));

        //list.add(new Item("S:productName:", "/ES", "", "Lighting effect setting"));//这个会报java.lang.IndexOutOfBoundsException : Invalid array range: 1 to 1 错误的，按照冒号分割出来的数组只有一个长度的。
        list.add(new Item("S::exlight_settings_1", "/OTA", "", "Lighting effect setting"));
        list.add(new Item("S:exlight_settings_1", "/OTA", "", "Lighting effect setting"));

        list.add(new Item("S:exlight_settingsOTA_1", "/OTA", "", "Lighting effect setting"));
        list.add(new Item("S:exlight_settingsOTA_2", "/OTA", "", "Lighting effect setting"));
        list.add(new Item("S:exlight_settingsOTA_3", "/OTA", "", "Lighting effect setting"));

        list.add(new Item("A:exlight_settings_array_OTA_3:0", "/OTA", "", "Lighting effect setting"));
        list.add(new Item("A:exlight_settings_array_OTA_3:1", "/OTA", "", "Lighting effect setting"));
        list.add(new Item("A:exlight_settings_array_OTA_3:2", "/OTA", "", "Lighting effect setting"));
        list.add(new Item("P:exlight_settings_plurals_OTA_3:plurals0", "/OTA", "", "plurals Lighting effect setting"));
        list.add(new Item("P:exlight_settings_plurals_OTA_3:plurals1", "/OTA", "", "plurals Lighting effect setting"));
        list.add(new Item("P:exlight_settings_plurals_OTA_3:plurals2", "/OTA", "", "plurals Lighting effect setting"));

        try {
            Method method = clazz.getDeclaredMethod("writeItemsToXML", List.class, String.class, File.class);
            method.setAccessible(true);
            method.invoke(clazz, list, valudesDir, xmlFileDir);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method: writeItemToResources(List<Item> items, BufferedWriter bufferedWriter)
     */
    @Test
    public void testWriteItemToResources() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = XlsToXMLDir.getClass().getMethod("writeItemToResources", List<Item>.class, BufferedWriter.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
    }

    /**
     * Method: writeArray(List<Item> items, BufferedWriter bufferedWriter, Item itemFirst)
     */
    @Test
    public void testWriteArray() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = XlsToXMLDir.getClass().getMethod("writeArray", List<Item>.class, BufferedWriter.class, Item.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
    }

    /**
     * Method: writePlurals(List<Item> items, BufferedWriter bufferedWriter, Item itemFirst)
     */
    @Test
    public void testWritePlurals() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = XlsToXMLDir.getClass().getMethod("writePlurals", List<Item>.class, BufferedWriter.class, Item.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
    }

    /**
     * Method: writeString(List<Item> items, BufferedWriter bufferedWriter, Item itemFirst)
     */
    @Test
    public void testWriteString() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = XlsToXMLDir.getClass().getMethod("writeString", List<Item>.class, BufferedWriter.class, Item.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
    }

}
