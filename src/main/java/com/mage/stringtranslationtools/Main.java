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


import com.mage.stringtranslationtools.checkdifftag.CollectAllStringsWithParentCheckDiff;
import com.mage.stringtranslationtools.xls.XlsToSourceCodeDir;
import com.mage.stringtranslationtools.xls.XlsToSourceCodeDirDifferentDir;
import com.mage.stringtranslationtools.xls.XlsToXMLDir;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        int actionNumber = Integer.parseInt(args[0]);
        switch (actionNumber) {
            case 1:
                CollectAllStringsWithParent.doCollectAllStrings(args);
                break;
            case 2:
                CollectAllDiffStringsWithParent.doCollectAllStrings(args);
                break;
            case 3:
                CollectAllDiffStringsWithParentWithCheckDatabase.doCollectAllStrings(args);
                break;
            case 4:
                XlsToXMLDir.doCollectAllStrings(args);
                break;
            case 5:
                XlsToSourceCodeDir.doCollectAllStrings(args);
                break;
            case 6:
                CollectAllStringsWithParentCheckDiff.doCollectAllStrings(args);
                break;
            case 7:
                CreateDatabase.doCollectAllStrings(args);
                break;
            case 8:
                CheckFormatStrings.doCollectAllStrings(args);
                break;
            case 9:
                CollectAllDiffStringsWithParentWithSeperateSheet.doCollectAllStrings(args);
                break;
            case 10:
                CollectAllDiffStringsWithParentWithXMLDir.doCollectAllStrings(args);
                break;
            case 11:
                XlsToSourceCodeDirDifferentDir.doCollectAllStrings(args);
        }

    }
}