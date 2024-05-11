package ru.swetophor.astrowidjaspringshell.model;

import lombok.RequiredArgsConstructor;

public class MultiChart extends ChartObject {

    private final Chart[] moments;

    public MultiChart(String name, Chart... moments) {
        super(name);
        this.moments = moments;
    }

    @Override
    public Chart[] getData() {
        return moments;
    }

    @Override
    public boolean resonancePresent(Astra a, Astra b, int harmonic) {
        return false;
    }

    @Override
    public int getDimension() {
        return moments.length;
    }
}
