package com.rx.admin.common.constant;

import java.util.List;

public class TranscriptionConstants {

    private TranscriptionConstants() {}

    public static class Status {
        public static final int FAIL = 0;
        public static final int SUCCESS = 1;
        public static final int PENDING = 2;
    }

    public static class Video {
        public static final List<String> EXTENSIONS = List.of(".mp4", ".avi", ".mkv", ".flv", ".mov", ".webm", ".wmv", ".m4v");
        public static final String[] SPEAKER_LABELS = {"SPEAKER_00", "SPEAKER_01", "SPEAKER_02", "SPEAKER_03"};
        public static final String[] SPEAKER_NAMES = {"角色A", "角色B", "角色C", "角色D"};
        public static final String TEMP_PREFIX = "video";
    }

    public static class Audio {
        public static final List<String> EXTENSIONS = List.of(".mp3", ".wav", ".m4a", ".flac", ".aac", ".ogg", ".webm");
        public static final String TEMP_PREFIX = "audio";
    }
}
