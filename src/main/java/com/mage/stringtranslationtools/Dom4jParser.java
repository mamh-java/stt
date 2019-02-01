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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Dom4jParser
implements Parser {
    private SAXReader reader = new SAXReader();

    public String stripElementName(String element) {
        if (element.indexOf(">") != -1) {
            element = element.substring(element.indexOf(">") + 1);
        }
        if (element.lastIndexOf("</") != -1) {
            element = element.substring(0, element.lastIndexOf("</"));
        }
        if (element.indexOf("xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\" ") != -1) {
            element = element.replace("xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\" ", "");
        }
        if (element.indexOf("xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\"") != -1) {
            element = element.replace("xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\"", "");
        }
        if ((element = element.trim()).startsWith("\"") && element.endsWith("\"") && element.indexOf("\"") != element.lastIndexOf("\"")) {
            element = element.substring(1, element.length() - 1);
        }
        return element;
    }

    @Override
    public Map<String, String> parseValidStringNames(File strFile, List<String> keys) {
        String productName;
        Attribute productAttribute;
        String elementAsXML;
        Document document;
        HashMap<String, String> xmlContentMap = new HashMap<String, String>();
        try {
            document = this.reader.read(strFile);
        }
        catch (DocumentException e) {
            try {
                FileWriter fw = new FileWriter("parseFailedXML.txt", true);
                PrintWriter pw = new PrintWriter(fw);
                pw.println(strFile.getAbsolutePath());
                pw.close();
                fw.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            e.printStackTrace();
            return xmlContentMap;
        }
        Element root = document.getRootElement();
        Element element = null;
        Element elementItem = null;
        Attribute attribute = null;
        String strName = null;
        Iterator i = root.elementIterator("string");
        while (i.hasNext()) {
            element = (Element)i.next();
            elementAsXML = this.stripElementName(element.asXML());
            attribute = element.attribute("translatable");
            if (attribute != null && !Boolean.getBoolean(attribute.getValue())) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                continue;
            }
            attribute = element.attribute("translate");
            if (attribute != null && !Boolean.getBoolean(attribute.getValue())) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                continue;
            }
            if (element.nodeCount() == 0 || element.nodeCount() == 1 && elementAsXML != null && elementAsXML.length() == 0) continue;
            productAttribute = element.attribute("product");
            productName = null;
            if (productAttribute != null && StringUtils.isNotEmpty(productAttribute.getValue())) {
                productName = productAttribute.getValue();
            }
            if ((attribute = element.attribute("name")) == null || !StringUtils.isNotEmpty(strName = attribute.getValue()) || !EnviromentBuilder.isValidString("S:" + (productName != null ? new StringBuilder(String.valueOf(productName)).append(":").toString() : "") + strName, this.stripElementName(element.asXML()))) continue;
            xmlContentMap.put("S:" + (productName != null ? new StringBuilder(String.valueOf(productName)).append(":").toString() : "") + strName, elementAsXML);
            if (keys == null) continue;
            keys.add("S:" + (productName != null ? new StringBuilder(String.valueOf(productName)).append(":").toString() : "") + strName);
        }
        i = root.elementIterator("plurals");
        while (i.hasNext()) {
            element = (Element)i.next();
            elementAsXML = this.stripElementName(element.asXML());
            attribute = element.attribute("translatable");
            if (attribute != null && !Boolean.getBoolean(attribute.getValue())) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                continue;
            }
            attribute = element.attribute("translate");
            if (attribute != null && !Boolean.getBoolean(attribute.getValue())) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                continue;
            }
            if (element.nodeCount() == 0 || element.nodeCount() == 1 && elementAsXML != null && elementAsXML.length() == 0) continue;
            productAttribute = element.attribute("product");
            productName = null;
            if (productAttribute != null && StringUtils.isNotEmpty(productAttribute.getValue())) {
                productName = productAttribute.getValue();
            }
            if ((attribute = element.attribute("name")) == null || !StringUtils.isNotEmpty(strName = attribute.getValue())) continue;
            Iterator itemIte = element.elementIterator("item");
            while (itemIte.hasNext()) {
                elementItem = (Element)itemIte.next();
                xmlContentMap.put("P:" + (productName != null ? new StringBuilder(String.valueOf(productName)).append(":").toString() : "") + strName + ":" + elementItem.attribute("quantity").getText(), this.stripElementName(elementItem.asXML()));
                if (keys == null) continue;
                keys.add("P:" + (productName != null ? new StringBuilder(String.valueOf(productName)).append(":").toString() : "") + strName + ":" + elementItem.attribute("quantity").getText());
            }
        }
        i = root.elementIterator("string-array");
        while (i.hasNext()) {
            element = (Element)i.next();
            elementAsXML = this.stripElementName(element.asXML());
            attribute = element.attribute("translatable");
            if (attribute != null && !Boolean.getBoolean(attribute.getValue())) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                continue;
            }
            attribute = element.attribute("translate");
            if (attribute != null && !Boolean.getBoolean(attribute.getValue())) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                continue;
            }
            if (element.nodeCount() == 0 || element.nodeCount() == 1 && elementAsXML != null && elementAsXML.length() == 0) continue;
            productAttribute = element.attribute("product");
            productName = null;
            if (productAttribute != null && StringUtils.isNotEmpty(productAttribute.getValue())) {
                productName = productAttribute.getValue();
            }
            if ((attribute = element.attribute("name")) == null || !StringUtils.isNotEmpty(strName = attribute.getValue())) continue;
            int itemElementIndex = 0;
            Iterator itemIte = element.elementIterator("item");
            while (itemIte.hasNext()) {
                elementItem = (Element)itemIte.next();
                xmlContentMap.put("A:" + (productName != null ? new StringBuilder(String.valueOf(productName)).append(":").toString() : "") + strName + ":" + itemElementIndex, this.stripElementName(elementItem.asXML()));
                if (keys != null) {
                    keys.add("A:" + (productName != null ? new StringBuilder(String.valueOf(productName)).append(":").toString() : "") + strName + ":" + itemElementIndex);
                }
                ++itemElementIndex;
            }
        }
        return xmlContentMap;
    }
}

