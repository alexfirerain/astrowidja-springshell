package ru.swetophor.astrowidjaspringshell.model;

import java.util.*;

public class AstroMatrix {

    private final Chart[] heavens;

    private final Astra[] allAstras;

    private Map<Chart, Map<Astra, Integer>> index;

    private Resonance[][] matrix;

    public AstroMatrix(Chart... charts) {
        heavens = charts;
        allAstras = Arrays.stream(charts)
                .flatMap(c -> c.getAstras().stream())
                .toArray(Astra[]::new);
        buildIndex();
        buildMatrix();
    }

    private void buildIndex() {
        Map<Chart, Map<Astra, Integer>> builtIndex = new HashMap<>();
        int counter = 0;
        for (Chart chart : heavens) {
            builtIndex.put(chart, new HashMap<>());
            var astraMap = builtIndex.get(chart);
            for (Astra astra : chart.getAstras())
                astraMap.put(astra, counter++);
        }
        index = builtIndex;
    }

    private int astraIndex(Astra astra) {
        return index.get(astra.getHeaven()).get(astra);
    }

    private void buildMatrix() {
        Resonance[][] builtMatrix = new Resonance[allAstras.length][allAstras.length];
        for (int i = 0; i < allAstras.length - 1; i++)
            for (int j = i + 1; j < allAstras.length; j++)
                matrix[i][j] = new Resonance(allAstras[i], allAstras[j]);
        matrix = builtMatrix;
    }

    public Resonance getResonanceFor(Astra a, Astra b) {
        if (a == b) throw new IllegalArgumentException("Астра не делает резонанса сама с собой");
        int iA = astraIndex(a), iB = astraIndex(b);
        return iA < iB ?
                matrix[iA][iB] :
                matrix[iB][iA];
    }

    public boolean inResonance(Astra a, Astra b, int harmonic) {
        return getResonanceFor(a,b).hasGivenHarmonic(harmonic);
    }
}
