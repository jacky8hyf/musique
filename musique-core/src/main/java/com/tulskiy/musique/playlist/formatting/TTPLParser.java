package com.tulskiy.musique.playlist.formatting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

public class TTPLParser {

    static Logger logger = Logger.getLogger(TTPLParser.class.getCanonicalName());

    private static final List<TTPlaylistEntry> parseTTPL(File fXmlFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        // optional, but recommended
        // read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        NodeList items = doc.getElementsByTagName("items").item(0).getChildNodes();
        int len = items.getLength();
        List<TTPlaylistEntry> entries = new ArrayList<TTPlaylistEntry>(len);
        for(int i = 0; i < len; i++) {
            Node item = items.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {

                Element ele = (Element) item;

                String file = ele.getAttribute("file");
                // file is a must-have attribute.
                if(file == null || file.isEmpty()) continue;
                String title = ele.getAttribute("title");
                long lenSec = 0;
                try {
                    lenSec = Long.parseLong(ele.getAttribute("len"));
                } catch (NumberFormatException ex) { }
                // subtrack is 1-indexed, so I can use 0 to denote non-exist
                int subTrack = 0;
                try {
                    subTrack = Integer.parseInt(ele.getAttribute("subtk"));
                } catch (NumberFormatException ex) { }
                entries.add(new TTPlaylistEntry(file, subTrack, title, lenSec));
            }
        }
        return entries;
    }

    // compatible with TTPlayer 5.9.4
    public static final Playlist loadTTPL(File ttplFile, Map<String, Object> progress) {
        List<TTPlaylistEntry> entries;
        try {
            entries = parseTTPL(ttplFile);
        } catch (Exception e) {
            logger.warning("error parsing ttpl " + ttplFile.getName() + ": "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            // then return empty playlist.
            return new Playlist();
        }
        Playlist ret = new Playlist();
        Map<String, Playlist> cues = new HashMap<>();

        for(TTPlaylistEntry entry : entries) {
            String absFile = new File(entry.file).getAbsolutePath();
            if(Util.getFileExt(absFile).equals("cue")) {
                if(entry.subTrack <= 0) {
                    // not allowed
                    logger.warning("ttpl skipping " + absFile + ": expected positive subtk attribute");
                    continue;
                }
                // load the subtrack only
                Playlist cuePlaylist = cues.get(absFile);
                if(cuePlaylist == null) {
                    cuePlaylist = new Playlist();
                    cuePlaylist.insertItem(absFile, -1, false, progress);
                    cues.put(absFile, cuePlaylist);
                }
                // subtrack is 1-indexed.
                Track tk = null;
                try {
                    tk = cuePlaylist.get(entry.subTrack - 1); // will just return null if no such subtrack
                } catch (IndexOutOfBoundsException ex) {
                    // handled below.
                }
                if(tk == null) {
                    logger.warning("ttpl skipping " + absFile + ":" + entry.subTrack + ": subtrack number too large");
                    continue;
                }
                ret.add(tk);
            } else {
                ret.insertItem(absFile, -1, false, progress);
            }
        }

        return ret;
    }

}
