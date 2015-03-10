package com.tulskiy.musique.playlist.formatting;

public class TTPlaylistEntry {
    public final String file;
    public final int subTrack;
    public final String title;
    public final long len;

    TTPlaylistEntry(String file, int subTrack, String title, long len) {
        this.file = file;
        this.subTrack = subTrack;
        this.title = title;
        this.len = len;
    }

    @Override
    public String toString() {
        return "TTPlaylistEntry [file=" + file + ", subTrack=" + subTrack
                + ", title=" + title + ", len=" + len + "]";
    }
}