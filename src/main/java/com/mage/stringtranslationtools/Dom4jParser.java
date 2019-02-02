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


import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Dom4jParser implements Parser {
    private SAXReader reader = new SAXReader();

    public Dom4jParser() {
    }

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

        element = element.trim();
        if (element.startsWith("\"") && element.endsWith("\"") && element.indexOf("\"") != element.lastIndexOf("\"")) {
            element = element.substring(1, element.length() - 1);
        }

        return element;
    }

    public Map<String, String> parseValidStringNames(File strFile, List<String> keys) {
        Map<String, String> xmlContentMap = new HashMap<>();

        Document document;
        FileWriter fw;
        PrintWriter pw;

        try {
            document = this.reader.read(strFile);
        } catch (DocumentException e) {
            try {
                fw = new FileWriter("parseFailedXML.txt", true);
                pw = new PrintWriter(fw);
                pw.println(strFile.getAbsolutePath());
                pw.close();
                fw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            e.printStackTrace();
            return xmlContentMap;
        }

        Element root = document.getRootElement();

        for (Iterator i = root.elementIterator("string"); i.hasNext(); ) {
            Element element = (Element) i.next();
            String elementAsXML = stripElementName(element.asXML());

            Attribute attribute = element.attribute("translatable");

            if ((attribute != null) && (!Boolean.getBoolean(attribute.getValue()))) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
            } else {
                attribute = element.attribute("translate");

                if ((attribute != null) && (!Boolean.getBoolean(attribute.getValue()))) {
                    Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                } else if (element.nodeCount() != 0) {
                    if ((element.nodeCount() != 1) || (elementAsXML == null) || (elementAsXML.length() != 0)) {

                        Attribute productAttribute = element.attribute("product");

                        String productName = null;

                        if ((productAttribute != null) && (StringUtils.isNotEmpty(productAttribute.getValue()))) {
                            productName = productAttribute.getValue();
                        }

                        attribute = element.attribute("name");
                        String strName;
                        if ((attribute != null) && (StringUtils.isNotEmpty(strName = attribute.getValue()))) {
                            if (EnviromentBuilder.isValidString("S:" + (productName != null ? productName + ":" : "") + strName, stripElementName(element.asXML()))) {
                                xmlContentMap.put("S:" + (productName != null ? productName + ":" : "") + strName, elementAsXML);
                                if (keys != null) {
                                    keys.add("S:" + (productName != null ? productName + ":" : "") + strName);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Iterator i = root.elementIterator("plurals"); i.hasNext(); ) {
            Element element = (Element) i.next();
            String elementAsXML = stripElementName(element.asXML());

            Attribute attribute = element.attribute("translatable");
            if ((attribute != null) && (!Boolean.getBoolean(attribute.getValue()))) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
            } else {
                attribute = element.attribute("translate");

                if ((attribute != null) && (!Boolean.getBoolean(attribute.getValue()))) {
                    Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                } else if (element.nodeCount() != 0) {
                    if ((element.nodeCount() != 1) || (elementAsXML == null) || (elementAsXML.length() != 0)) {

                        Attribute productAttribute = element.attribute("product");
                        String productName = null;
                        if ((productAttribute != null) && (StringUtils.isNotEmpty(productAttribute.getValue()))) {
                            productName = productAttribute.getValue();
                        }
                        attribute = element.attribute("name");
                        String strName;

                        if ((attribute != null) && (StringUtils.isNotEmpty(strName = attribute.getValue()))) {
                            for (Iterator itemIte = element.elementIterator("item"); itemIte.hasNext(); ) {
                                Element elementItem = (Element) itemIte.next();
                                xmlContentMap.put("P:" + (productName != null ? productName + ":" : "") + strName +
                                        ":" + elementItem.attribute("quantity").getText(), stripElementName(elementItem.asXML()));
                                if (keys != null) {
                                    keys.add("P:" + (productName != null ? productName + ":" : "") + strName + ":" + elementItem.attribute("quantity").getText());
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Iterator i = root.elementIterator("string-array"); i.hasNext(); ) {
            Element element = (Element) i.next();
            String elementAsXML = stripElementName(element.asXML());

            Attribute attribute = element.attribute("translatable");
            if ((attribute != null) && (!Boolean.getBoolean(attribute.getValue()))) {
                Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
            } else {
                attribute = element.attribute("translate");
                if ((attribute != null) && (!Boolean.getBoolean(attribute.getValue()))) {
                    Utils.logout("string name:" + element.attribute("name").getText() + " -- ignored");
                } else if (element.nodeCount() != 0) {
                    if ((element.nodeCount() != 1) || (elementAsXML == null) || (elementAsXML.length() != 0)) {
                        Attribute productAttribute = element.attribute("product");
                        String productName = null;
                        if ((productAttribute != null) && (StringUtils.isNotEmpty(productAttribute.getValue()))) {
                            productName = productAttribute.getValue();
                        }
                        attribute = element.attribute("name");
                        String strName;

                        if ((attribute != null) && (StringUtils.isNotEmpty(strName = attribute.getValue()))) {
                            int itemElementIndex = 0;
                            for (Iterator itemIte = element.elementIterator("item"); itemIte.hasNext(); ) {
                                Element elementItem = (Element) itemIte.next();
                                xmlContentMap.put("A:" + (productName != null ? productName + ":" : "") + strName + ":" + itemElementIndex, stripElementName(elementItem.asXML()));
                                if (keys != null) {
                                    keys.add("A:" + (productName != null ? productName + ":" : "") + strName + ":" + itemElementIndex);
                                }
                                itemElementIndex++;
                            }
                        }
                    }
                }
            }
        }

        return xmlContentMap;
    }
}

