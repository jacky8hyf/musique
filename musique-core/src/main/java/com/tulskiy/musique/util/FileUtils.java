/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.util;

import javax.swing.*;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Maksim Liauchuk
 */
public class FileUtils {

    private FileUtils() {
        // prevent instantiation
    }

    public static void deleteEmptyParentFolders(final File file, final boolean isConfirmationRequired) {
        File current = file.getParentFile();
        File parent;
        File[] files = current.listFiles();
        if (files != null && files.length == 0) {
            int ret = JOptionPane.showConfirmDialog(null, "Do you want delete empty folder(s) as well?", "Delete File(s)?", JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                while (current != null) {
                    parent = current.getParentFile();
                    current.delete();
                    files = parent.listFiles();
                    if (files != null && files.length == 0) {
                        current = parent;
                    }
                    else {
                        current = null;
                    }
                }
            }
        }
    }

    /**
     * replace illegal characters in a filename with "_"
     * Windows illegal characters :
     *           : \ / * ? | < >
     * Illegal characters of Mac / Linux are a subset of above.
     * //http://stackoverflow.com/questions/2357133/identifying-os-dependent-invalid-filename-characters-in-java-6-not-7 
     * @param name
     * @return
     */
     public static String sanitizeFilename(String name) {
       return name.replaceAll("[:\\\\/*?|<>]", "_");
     }

     public static File getSanitizedPath(File directory, String fileName) throws InvalidPathException {
         try {
             return Paths.get(directory.getAbsolutePath(), fileName).toFile();
         } catch (InvalidPathException ex) {
             return Paths.get(directory.getAbsolutePath(), FileUtils.sanitizeFilename(fileName)).toFile();
         }
     }

}