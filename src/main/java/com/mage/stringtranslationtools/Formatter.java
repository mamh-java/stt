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


import java.math.BigInteger;
import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Formattable;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.MissingFormatWidthException;
import java.util.UnknownFormatConversionException;

public class Formatter {
    public Formatter() {
    }

    public static List<String> scanFormatStrings(String format) {
        FormatSpecifierParser fsp = new FormatSpecifierParser(format);
        List<String> formatStringList = null;

        if (!format.contains("%")) {
            return formatStringList;
        }

        formatStringList = new LinkedList<>();
        int length = format.length();

        int i = 0;
        while (i < length) {
            int nextPercent = format.indexOf(37, i);
            int plainTextEnd = nextPercent == -1 ? length : nextPercent;

            i = plainTextEnd;
            if (plainTextEnd < length) {
                fsp.parseFormatToken(plainTextEnd + 1);
                formatStringList.add(format.substring(plainTextEnd, fsp.i));
                i = fsp.i;
            }
        }

        return formatStringList;

    }

    private static class FormatSpecifierParser {
        private String format;
        private int length;
        private int startIndex;
        private int i;

        FormatSpecifierParser(String format) {
            this.format = format;
            this.length = format.length();
        }

        void parseFormatToken(int offset) {
            this.startIndex = offset;
            this.i = offset;
            this.parseArgumentIndexAndFlags(new FormatToken());
        }

        String getFormatSpecifierText() {
            return this.format.substring(this.startIndex, this.i);
        }

        private int peek() {
            return this.i < this.length ? this.format.charAt(this.i) : -1;
        }

        private char advance() {
            if (this.i >= this.length) {
                throw this.unknownFormatConversionException();
            } else {
                return this.format.charAt(this.i++);
            }
        }

        private UnknownFormatConversionException unknownFormatConversionException() {
            throw new UnknownFormatConversionException(this.getFormatSpecifierText());
        }

        private void parseArgumentIndexAndFlags(FormatToken token) {
            int position = this.i;
            int ch = this.peek();
            if (Character.isDigit(ch)) {
                int number = this.nextInt();
                if (this.peek() == 36) {
                    this.advance();
                    if (number == -1) {
                        throw new MissingFormatArgumentException(this.getFormatSpecifierText());
                    }

                    token.setArgIndex(Math.max(0, number - 1));
                } else {
                    if (ch != 48) {
                        this.parseWidth(token, number);
                        return;
                    }

                    this.i = position;
                }
            } else if (ch == 60) {
                token.setArgIndex(-2);
                this.advance();
            }

            while (token.setFlag(this.peek())) {
                this.advance();
            }

            ch = this.peek();
            if (Character.isDigit(ch)) {
                this.parseWidth(token, this.nextInt());
            } else {
                if (ch == 46) {
                    this.parsePrecision(token);
                } else {
                    this.parseConversionType(token);
                }
            }
        }

        private void parseWidth(FormatToken token, int width) {
            token.setWidth(width);
            int ch = this.peek();
            if (ch == 46) {
                this.parsePrecision(token);
            } else {
                this.parseConversionType(token);
            }
        }

        private void parsePrecision(FormatToken token) {
            this.advance();
            int ch = this.peek();
            if (Character.isDigit(ch)) {
                token.setPrecision(this.nextInt());
                this.parseConversionType(token);
            } else {
                throw this.unknownFormatConversionException();
            }
        }

        private void parseConversionType(FormatToken token) {
            char conversionType = this.advance();
            token.setConversionType(conversionType);
            if (conversionType == 't' || conversionType == 'T') {
                char dateSuffix = this.advance();
                token.setDateSuffix(dateSuffix);
            }

        }

        private int nextInt() {
            long value = 0L;

            while (this.i < this.length && Character.isDigit(this.format.charAt(this.i))) {
                value = 10L * value + (long) (this.format.charAt(this.i++) - 48);
                if (value > 2147483647L) {
                    return this.failNextInt();
                }
            }

            return (int) value;
        }

        private int failNextInt() {
            while (Character.isDigit(this.peek())) {
                this.advance();
            }

            return -1;
        }
    }

    private static class FormatToken {
        static final int LAST_ARGUMENT_INDEX = -2;
        static final int UNSET = -1;
        static final int FLAGS_UNSET = 0;
        static final int DEFAULT_PRECISION = 6;
        static final int FLAG_ZERO = 16;
        private int argIndex;
        boolean flagComma;
        boolean flagMinus;
        boolean flagParenthesis;
        boolean flagPlus;
        boolean flagSharp;
        boolean flagSpace;
        boolean flagZero;
        private char conversionType;
        private char dateSuffix;
        private int precision;
        private int width;
        private StringBuilder strFlags;

        private FormatToken() {
            this.argIndex = -1;
            this.conversionType = '\uffff';
            this.precision = -1;
            this.width = -1;
        }

        boolean isDefault() {
            return !this.flagComma && !this.flagMinus &&
                    !this.flagParenthesis && !this.flagPlus && !this.flagSharp &&
                    !this.flagSpace && !this.flagZero && this.width == -1 && this.precision == -1;
        }

        boolean isPrecisionSet() {
            return this.precision != -1;
        }

        int getArgIndex() {
            return this.argIndex;
        }

        void setArgIndex(int index) {
            this.argIndex = index;
        }

        int getWidth() {
            return this.width;
        }

        void setWidth(int width) {
            this.width = width;
        }

        int getPrecision() {
            return this.precision;
        }

        void setPrecision(int precise) {
            this.precision = precise;
        }

        String getStrFlags() {
            return this.strFlags != null ? this.strFlags.toString() : "";
        }

        boolean setFlag(int ch) {
            boolean dupe = false;
            switch (ch) {
                case 32:
                    dupe = this.flagSpace;
                    this.flagSpace = true;
                    break;
                case 33:
                case 34:
                case 36:
                case 37:
                case 38:
                case 39:
                case 41:
                case 42:
                case 46:
                case 47:
                default:
                    return false;
                case 35:
                    dupe = this.flagSharp;
                    this.flagSharp = true;
                    break;
                case 40:
                    dupe = this.flagParenthesis;
                    this.flagParenthesis = true;
                    break;
                case 43:
                    dupe = this.flagPlus;
                    this.flagPlus = true;
                    break;
                case 44:
                    dupe = this.flagComma;
                    this.flagComma = true;
                    break;
                case 45:
                    dupe = this.flagMinus;
                    this.flagMinus = true;
                    break;
                case 48:
                    dupe = this.flagZero;
                    this.flagZero = true;
            }

            if (dupe) {
                throw new DuplicateFormatFlagsException(String.valueOf(ch));
            } else {
                if (this.strFlags == null) {
                    this.strFlags = new StringBuilder(7);
                }

                this.strFlags.append((char) ch);
                return true;
            }
        }

        char getConversionType() {
            return this.conversionType;
        }

        void setConversionType(char c) {
            this.conversionType = c;
        }

        char getDateSuffix() {
            return this.dateSuffix;
        }

        void setDateSuffix(char c) {
            this.dateSuffix = c;
        }

        boolean requireArgument() {
            return this.conversionType != '%' && this.conversionType != 'n';
        }

        void checkFlags(Object arg) {
            boolean allowComma = false;
            boolean allowMinus = true;
            boolean allowParenthesis = false;
            boolean allowPlus = false;
            boolean allowSharp = false;
            boolean allowSpace = false;
            boolean allowZero = false;
            boolean allowPrecision = true;
            boolean allowWidth = true;
            boolean allowArgument = true;
            switch (this.conversionType) {
                case '%':
                    allowArgument = false;
                    allowPrecision = false;
                    break;
                case 'A':
                case 'a':
                    allowZero = true;
                    allowSpace = true;
                    allowSharp = true;
                    allowPlus = true;
                case 'B':
                case 'H':
                case 'b':
                case 'h':
                    break;
                case 'C':
                case 'T':
                case 'c':
                case 't':
                    allowPrecision = false;
                    break;
                case 'E':
                case 'e':
                    allowZero = true;
                    allowSpace = true;
                    allowSharp = true;
                    allowPlus = true;
                    allowParenthesis = true;
                    break;
                case 'G':
                case 'g':
                    allowZero = true;
                    allowSpace = true;
                    allowPlus = true;
                    allowParenthesis = true;
                    allowComma = true;
                    break;
                case 'S':
                case 's':
                    if (arg instanceof Formattable) {
                        allowSharp = true;
                    }
                    break;
                case 'X':
                case 'o':
                case 'x':
                    allowZero = true;
                    allowSharp = true;
                    if (arg == null || arg instanceof BigInteger) {
                        allowSpace = true;
                        allowPlus = true;
                        allowParenthesis = true;
                    }

                    allowPrecision = false;
                    break;
                case 'd':
                    allowZero = true;
                    allowSpace = true;
                    allowPlus = true;
                    allowParenthesis = true;
                    allowComma = true;
                    allowPrecision = false;
                    break;
                case 'f':
                    allowZero = true;
                    allowSpace = true;
                    allowSharp = true;
                    allowPlus = true;
                    allowParenthesis = true;
                    allowComma = true;
                    break;
                case 'n':
                    allowMinus = false;
                    allowWidth = false;
                    allowPrecision = false;
                    allowArgument = false;
                    break;
                default:
                    throw this.unknownFormatConversionException();
            }

            String mismatch = null;
            if (!allowComma && this.flagComma) {
                mismatch = ",";
            } else if (!allowMinus && this.flagMinus) {
                mismatch = "-";
            } else if (!allowParenthesis && this.flagParenthesis) {
                mismatch = "(";
            } else if (!allowPlus && this.flagPlus) {
                mismatch = "+";
            } else if (!allowSharp && this.flagSharp) {
                mismatch = "#";
            } else if (!allowSpace && this.flagSpace) {
                mismatch = " ";
            } else if (!allowZero && this.flagZero) {
                mismatch = "0";
            }

            if (mismatch != null) {
                if (this.conversionType == 'n') {
                    throw new IllegalFormatFlagsException(mismatch);
                } else {
                    throw new FormatFlagsConversionMismatchException(mismatch, this.conversionType);
                }
            } else if ((this.flagMinus || this.flagZero) && this.width == -1) {
                throw new MissingFormatWidthException("-" + this.conversionType);
            } else if (!allowArgument && this.argIndex != -1) {
                throw new IllegalFormatFlagsException("%" + this.conversionType + " doesn't take an argument");
            } else if (!allowPrecision && this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            } else if (!allowWidth && this.width != -1) {
                throw new IllegalFormatWidthException(this.width);
            } else if (this.flagPlus && this.flagSpace) {
                throw new IllegalFormatFlagsException("the '+' and ' ' flags are incompatible");
            } else if (this.flagMinus && this.flagZero) {
                throw new IllegalFormatFlagsException("the '-' and '0' flags are incompatible");
            }
        }

        public UnknownFormatConversionException unknownFormatConversionException() {
            if (this.conversionType != 't' && this.conversionType != 'T') {
                throw new UnknownFormatConversionException(String.valueOf(this.conversionType));
            } else {
                throw new UnknownFormatConversionException(String.format("%c%c", this.conversionType, this.dateSuffix));
            }
        }
    }
}
