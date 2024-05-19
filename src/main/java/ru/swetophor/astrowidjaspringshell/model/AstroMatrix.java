package ru.swetophor.astrowidjaspringshell.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AstroMatrix {
    /**
     * Карты, на основе которых рассчитана Матрица,
     * в том порядке, как переданы аргументы в конструктор.
     */
    private final Chart[] heavens;
    /**
     * Объединённый массив астр из всех карт. Соответствующие картам блоки
     * следуют в порядке, как в {@link #heavens}, в каждом блоке астры
     * следуют в порядке, возвращённом {@link Chart#getAstras() getAstras()}.
      */
    private final Astra[] allAstras;
    /**
     * Мапа в ОЗУ для быстрого получения номера астры в массиве {@link #allAstras}.
     * Реально ли это быстрее, чем {@code indexOf()}, не факт, но всё же.
     */
    private final Map<Chart, Map<Astra, Integer>> index;
    /**
     * Треугольный двумерный массив, отражающий все возможные парные отношения
     * между всеми астрами Матрицы. Если общее количество астр N,
     * то длина первого ряда равна N - 1, каждого последующего — на один меньше.
     */
    private final Resonance[][] matrix;

    /**
     * Создание матрицы резонансов для некоторого количества
     * астрологических карт.
     * @param charts карты, предоставляющие наборы астр для анализа.
     */
    public AstroMatrix(Chart... charts) {
        // фиксация массива карт
        heavens = charts;

        // создание общего массива астр
        allAstras = Arrays.stream(heavens)
                .flatMap(c -> c.getAstras().stream())
                .toArray(Astra[]::new);

        // построение индекса астр
        index = new HashMap<>();
        int counter = 0;
        for (Chart chart : heavens) {
            index.put(chart, new HashMap<>());
            var astraMap = index.get(chart);
            for (Astra astra : chart.getAstras())
                astraMap.put(astra, counter++);
        }

        // построение матрицы резонансов
        matrix = new Resonance[allAstras.length][allAstras.length];
        for (int i = 0; i < allAstras.length - 1; i++)
            for (int j = i + 1; j < allAstras.length; j++)
                matrix[i][j] = new Resonance(allAstras[i], allAstras[j]);
    }

    /**
     * Внутренний метод определения номера астры в {@link #allAstras}
     * с помощью {@link #index}
     * @param astra ссылка на астру.
     * @return  номер указанной астры в объединённом массиве. Если указанной астры
     * нет ни в одной карте, то -1.
     */
    private int astraIndex(Astra astra) {
        Integer i;
        try {
            i = index.get(astra.getHeaven()).get(astra);
        } catch (NullPointerException e) {
            return -1;
        }
        return i != null ? i : -1;
    }

    /**
     *  Выдаёт рассчитанный для пары астр резонанс.
     * @param a первая астра резонанса.
     * @param b вторая астра резонанса.
     * @return  объект резонанса, рассчитанный в Матрице для двух указанных астр.
     */
    public Resonance getResonanceFor(Astra a, Astra b) {
        if (a == b) throw new IllegalArgumentException("Астра не делает резонанса сама с собой");
        int iA = astraIndex(a), iB = astraIndex(b);
        if (iA == -1 || iB == -1) throw new IllegalArgumentException("Астра %s не найдена"
                .formatted(iA == -1 ?
                                iB == -1 ?
                                "%s и %s".formatted(a.getSymbolWithOwner(), b.getSymbolWithOwner()) :
                                "%s".formatted(a.getSymbolWithOwner()) :
                            "%s".formatted(b.getSymbolWithOwner())));
        return iA < iB ?
                matrix[iA][iB] :
                matrix[iB][iA];
    }

    /**
     * Сообщает, что между этими астрами присутствует
     * номинальный резонанс по указанной гармонике, как это сообщается
     * {@link Resonance#hasGivenHarmonic(int) hasGivenHarmonic()}.
     * @param a первая проверяемая астра.
     * @param b вторая проверяемая астра.
     * @param harmonic  гармоника, явная связь по которой проверяется.
     * @return  {@code true}, если между указанными астрами существует
     * яврный резонанс по указанной гармонике, в противном случае {@code false}.
     */
    public boolean inResonance(Astra a, Astra b, int harmonic) {
        return getResonanceFor(a,b).hasGivenHarmonic(harmonic);
    }

    /**
     * Выдаёт список {@link Resonance Резонансов}, которые указанная астра
     * делает со всеми остальными астрами в Матрице (из одной или разных карт).
     * @param a астра, резонансы которой интересуют.
     * @return  список резонансов указанной астры с каждой из остальных астр Матрицы.
     */
    public List<Resonance> resonancesFor(Astra a) {
        List<Resonance> list = new ArrayList<>();
        int index = astraIndex(a);
        int i = 0, j = index;
        boolean turning = false;
        while (i + j < allAstras.length + index - 1) {
            if (turning) j++;
            if (i == j) {
                turning = true;
                continue;
            }
            list.add(matrix[i][j]);
            if (!turning) i++;
        }
        return list;
    }

    /**
     * Выплёскивает поток существующих в АстроМатрице резонансов,
     * проход матрицы осуществляем "косынкой": [0][1]→[0][2]→[0][3]→[1][2]→[1][3]→[2][3].
     * Т.е. по резонансу для всех возможных пар между астрами анализируемой карты или карт.
     * @return  поток объектов-резонансов, начиная с первой планеты первой карты.
     */
    public Stream<Resonance> stream() {
        return IntStream.range(0, allAstras.length - 1)
                .mapToObj(i -> Arrays.asList(matrix[i])
                        .subList(i + 1, allAstras.length))
                .flatMap(Collection::stream);
    }

    public List<Resonance> getAllResonances() {
        return stream().collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Resonance> getResonancesFor(Chart chart) {
        return stream()
                .filter(Resonance::isNonSynastric)
                .filter(r -> r.getHeavens().contains(chart))
                .toList();
    }

    public List<Resonance> getResonancesFor(Chart chart1, Chart chart2) {
        return stream()
                .filter(Resonance::isSynastric)
                .filter(r -> r.getHeavens().containsAll(Set.of(chart1, chart2)))
                .toList();
    }
//    private static void testIJ(int index, int quantity) {
//        int i = 0, j = index;
//        boolean turning = false;
//        while (i + j < quantity + index - 1) {
//            if (turning) j++;
//            if (i == j) {
//                turning = true;
//                continue;
//            }
//            System.out.printf("%d/%d ", i, j);
//            if (!turning) i++;
//        }
//        System.out.println();
//    }
}
