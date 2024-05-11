package ru.swetophor.astrowidjaspringshell.model;

import lombok.RequiredArgsConstructor;

/**
 * Какой может быть карта с точки зрения
 * отражённых в ней данных
 */
@RequiredArgsConstructor
public enum ChartType {
    COSMOGRAM("Космограмма"),
    SYNASTRY("Синастрия"),
    TRANSIT("Транзит");

    public final String presentation;

    @Override
    public String toString() {
        return presentation;
    }
}
