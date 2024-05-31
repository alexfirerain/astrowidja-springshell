package ru.swetophor.astrowidjaspringshell.repository;

import ru.swetophor.astrowidjaspringshell.model.ChartList;
import ru.swetophor.astrowidjaspringshell.model.ChartObject;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public interface ChartRepository {

    static String newAutosaveName() {
        return "сохранение %s.awb"
                .formatted(new SimpleDateFormat("E d MMMM .yy HH-mm")
                        .format(new Date()));
    }

    boolean albumExists(String albumName);

    ChartList getAlbumContent(String filename);

    Collection<String> albumNames();

    Collection<? extends ChartList> getAllAlbums();

    String addChartsToAlbum(ChartList table, String target);

    boolean addChartsToAlbum(String s, ChartObject... chartObject);

    void saveChartsAsAlbum(ChartList desk, String s);

    String deleteAlbum(String groupToDelete);
}
