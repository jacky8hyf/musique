package com.tulskiy.musique.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import net.pempek.unicode.UnicodeBOMInputStream;

import org.mozilla.universalchardet.UniversalDetector;

// https://code.google.com/p/juniversalchardet/
public class EncodingDetector {
    private static Logger log = Logger.getLogger(EncodingDetector.class.getCanonicalName());
    /**
     * Detect encoding.
     * @param file
     * @param fallback
     * @return
     * @throws IOException
     */
    public static final Charset detect(File file, Charset fallback) throws IOException {
        byte[] buf = new byte[4096];
        String encoding;
        UniversalDetector detector = new UniversalDetector(null);
        try (FileInputStream fis = new FileInputStream(file)){
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
              detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
            if (encoding != null) {
                log.fine(String.format("encoding = %s for file %s", encoding, file));
                return Charset.forName(encoding);
            } else {
                log.warning(String.format("encoding unknown for file %s, using default %s", file, fallback.name()));
                return fallback;
            }
        } finally {
            detector.reset();
        }
    }

    /**
     * Detect encoding and skip BOM if necessary.
     * @param file
     * @param defaultCharset
     * @return
     * @throws IOException
     */
    public static final InputStreamReader getInputStreamReader(File file, Charset defaultCharset) throws IOException {
        // https://github.com/gpakosz/UnicodeBOMInputStream
        UnicodeBOMInputStream fis = new UnicodeBOMInputStream(new FileInputStream(file));
        fis.skipBOM();
        // http://stackoverflow.com/questions/14918188/reading-text-file-with-utf-8-encoding-using-java
        return new InputStreamReader(fis, detect(file, defaultCharset));
    }
}