package com.tulskiy.musique.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import net.pempek.unicode.UnicodeBOMInputStream;

import org.mozilla.universalchardet.UniversalDetector;

// https://code.google.com/p/juniversalchardet/
public class EncodingDetector {
    private EncodingDetector(){} // prevents instantiation
    private static Logger log = Logger.getLogger(EncodingDetector.class.getName());
    private static UniversalDetector detector = new UniversalDetector(null);
    private static final int TEST_SIZE = 4096;
    private static Charset defaultTextCharset = Charset.forName("UTF-8");
    /**
     * Detect encoding.
     * @param file
     * @param fallback
     * @return
     * @throws IOException
     */
    public static final Charset detect(File file, Charset fallback) throws IOException {
    	FileInputStream fis = null;
        try {
        	fis = new FileInputStream(file);
            Charset ret = detect0(fis);
            if (ret == null) {
                log.warning(String.format("encoding unknown for file %s, using default %s", file, fallback.name()));
                return fallback;
            } else {
                log.finest(String.format("encoding = %s for file %s", ret, file));
                return ret;
            }
        } finally {
        	if(fis != null) {
        		try {
        			fis.close();
        		} catch (IOException ex){}
        	}
        }
    }
    
    public static final Charset detect(InputStream is, Charset fallback) throws IOException {
        Charset ret = detect0(is);
        if(ret == null) {
            log.warning(String.format("encoding unknown, using default %s", fallback.name()));
            return fallback;
        } else {
            log.finest(String.format("encoding = %s", ret));
            return ret;
        }
    }
    
    public static final Charset detect(Reader reader, Charset fallback) throws IOException {
        return detect(new ReaderInputStream(reader), fallback);
    }
    
    /**
     * 
     * @param is
     * @return null if cannot detect
     * @throws IOException
     */
    private static final Charset detect0(InputStream is) throws IOException {
        try {
            if (is.markSupported()) {
                is.mark(TEST_SIZE);
            }
            byte[] buf = new byte[TEST_SIZE];
            String encoding;
            detector.reset();
            int nread;
            while ((nread = is.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
            if (encoding != null) {
                return Charset.forName(encoding);
            } else {
                return null;
            }
        } finally {
            try {
                detector.reset();
            } catch (Exception ignored) {
            }
            if (is.markSupported()) {
                try {
                    is.reset();
                } catch (Exception ignored) { }
            }
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
        UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(new FileInputStream(file));
        ubis.skipBOM();
        // http://stackoverflow.com/questions/14918188/reading-text-file-with-utf-8-encoding-using-java
        return new InputStreamReader(ubis, detect(file, defaultCharset));
    }

    /**
     * @see #getInputStreamReader(File, Charset)
     * @param file
     * @return
     * @throws IOException
     */
    public static final InputStreamReader getInputStreamReader(File file) throws IOException {
        return getInputStreamReader(file, defaultTextCharset); 
    }

    public static Charset getDefaultTextCharset() {
        return defaultTextCharset;
    }
    public static void setDefaultTextCharset(Charset charset) {
        defaultTextCharset = charset;
    }
    
    public static void main(String... args) throws IOException {
        String testString = "哎呀搞什么鬼为什么总是识别不了";
        StringBuilder sb = new StringBuilder(testString);

        for(int i = 0; i < 10000; i++) sb.append(testString);
        byte[] bytes = sb.toString().getBytes("GBK");
        Charset guessed = detect(new ByteArrayInputStream(bytes), Charset.forName("UTF-8"));
        System.out.println(guessed);
        System.out.println(new String(testString.getBytes("GBK"), guessed));
    }
}