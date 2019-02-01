package test.com.mage.stringtranslationtools.xls;

import com.mage.stringtranslationtools.xls.XlsToXMLDir;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * XlsToXMLDir Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Feb 1, 2019</pre>
 */
public class XlsToXMLDirTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: doCollectAllStrings(String[] args)
     */
    @Test
    public void testDoCollectAllStrings() throws Exception {
        String[] args = {"", "/home/mamh/fanyi/test.xls", "test"};
        XlsToXMLDir.doCollectAllStrings(args);
    }

    /**
     * Method: writeItemsToXML(List<Item> items, String valuesDir, File fileDirBase)
     */
    @Test
    public void testWriteItemsToXML() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: writeItemToResources(List<Item> items, BufferedWriter bufferedWriter)
     */
    @Test
    public void testWriteItemToResources() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: isItemsAllNull(List<Item> items)
     */
    @Test
    public void testIsItemsAllNull() throws Exception {
        //TODO: Test goes here...
    }


    /**
     * Method: processSeperateSheet(Workbook workbook, File xmlFileDir)
     */
    @Test
    public void testProcessSeperateSheet() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = XlsToXMLDir.getClass().getMethod("processSeperateSheet", Workbook.class, File.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
    }

    /**
     * Method: processOneSheet(Workbook workbook, File xmlFileDir)
     */
    @Test
    public void testProcessOneSheet() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = XlsToXMLDir.getClass().getMethod("processOneSheet", Workbook.class, File.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
    }

    /**
     * Method: extractXMLFromOneSheet(Sheet sheet, File xmlFileDir)
     */
    @Test
    public void testExtractXMLFromOneSheet() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = XlsToXMLDir.getClass().getMethod("extractXMLFromOneSheet", Sheet.class, File.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
    }

}
