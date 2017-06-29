/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) [2017] [velli20]
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package com.velli20.tachograph.filepicker;

import java.io.File;
import java.util.Comparator;


public class FileComparator implements Comparator<File> {
    public static int COMPARE_BY_LAST_MODIFIED = 0;
    public static int COMPARE_BY_NAME = 1;
    public static int COMPARE_BY_SIZE = 2;

    private final int mCompareBy;

    public FileComparator(int compareBy) {
        mCompareBy = compareBy;
    }

    @Override
    public int compare(File lhs, File rhs) {
        if (mCompareBy == COMPARE_BY_LAST_MODIFIED) {
            return Long.valueOf(lhs.lastModified()).compareTo(rhs.lastModified());
        } else if (mCompareBy == COMPARE_BY_NAME) {
            return String.valueOf(lhs.getName()).compareTo(rhs.getName());
        } else {
            return Long.valueOf(lhs.length()).compareTo(rhs.length());
        }
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }
}
