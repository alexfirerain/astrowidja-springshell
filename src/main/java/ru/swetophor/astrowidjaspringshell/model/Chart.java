package ru.swetophor.astrowidjaspringshell.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Chart extends ChartObject {

    private final List<Astra> astras = new ArrayList<>();

    public Chart(String name) {
        super(name);
    }

    public Chart(String name, List<Astra> astras) {
        this(name);
        astras.forEach(this::addAstra);
    }

    /**
     * Конструктор карты на основе строки ввода,
     * содержащей имя карты и, в следующих строках,
     * описание каждой астры в подобающем формате.
     *
     * @param input входная строка.
     * @return сформированную карту.
     */
    public static Chart readFromString(String input) {
        String[] lines = input.lines().toArray(String[]::new);
        if (lines.length == 0)
            throw new IllegalArgumentException("текст не содержит строк");

        var astras =
                Arrays.stream(lines, 1, lines.length)
                        .filter(line -> !line.isBlank()
                                && !line.startsWith("//"))
                        .map(Astra::readFromString)
                        .collect(Collectors.toList());
        return new Chart(lines[0], astras);
    }


    @Override
    public Chart[] getData() {
        return new Chart[]{ this };
    }

    @Override
    public boolean resonancePresent(Astra a, Astra b, int harmonic) {
        return false;
    }

    @Override
    public int getDimension() {
        return 1;
    }

    private void addAstra(Astra astra) {
        astra.setHeaven(this);
        var name = astra.getName();
        for (int i = 0; i < astras.size(); i++) {
            if (astras.get(i).getName().equals(name)) {
                astras.set(i, astra);
                return;
            }
            astras.add(astra);
        }
    }

}
