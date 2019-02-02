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


public class Item {
    /**
     * // String Name, 第一列表示string名称
     */
    private String name;

    /**
     * // 第2列表示 APP Path
     */
    private String path;

    /**
     * //第3列，表示备注啊
     */
    private String stringBase;

    /**
     * 从第4列开始是values各个语言对应的字符串值
     */
    private String stringTranslation;

    public Item() {
    }

    public Item(String name, String path, String stringBase, String stringTranslation) {
        this.name = name;
        this.path = path;
        this.stringBase = stringBase;
        this.stringTranslation = stringTranslation;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStringBase() {
        return this.stringBase;
    }

    public void setStringBase(String stringBase) {
        this.stringBase = stringBase;
    }

    public String getStringTranslation() {
        return this.stringTranslation;
    }

    public void setStringTranslation(String stringTranslation) {
        this.stringTranslation = stringTranslation;
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", stringBase='" + stringBase + '\'' +
                ", stringTranslation='" + stringTranslation + '\'' +
                '}';
    }
}
