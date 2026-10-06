package com.freemaf.agent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
public final class HistoryCleaner {
    private HistoryCleaner() {}
    public static void trim(Path dir, int keepLatest) {
        if (dir == null || !Files.isDirectory(dir)) return;
        try (Stream<Path> s = Files.list(dir)) {
            List<Path> all = s.filter(Files::isRegularFile)
                    .sorted(Comparator.comparingLong((Path p) -> p.toFile().lastModified()).reversed())
                    .toList();
            for (int i = keepLatest; i < all.size(); i++) {
                try { Files.deleteIfExists(all.get(i)); }
                catch (IOException ignored) {}
            }
        } catch (IOException e) {
            AppLogger.warn("HistoryCleaner failed: " + e.getMessage());
        }
    }
}
