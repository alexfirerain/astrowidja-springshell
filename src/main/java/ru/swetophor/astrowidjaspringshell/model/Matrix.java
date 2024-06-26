package ru.swetophor.astrowidjaspringshell.model;

import lombok.Getter;
import lombok.Setter;
import ru.swetophor.astrowidjaspringshell.config.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.swetophor.astrowidjaspringshell.utils.CelestialMechanics.CIRCLE;
import static ru.swetophor.astrowidjaspringshell.model.MatrixType.COSMOGRAM;
import static ru.swetophor.astrowidjaspringshell.model.MatrixType.SYNASTRY;

/**
 * Двумерная таблица, получающая два массива астр
 * и вычисляющая структуру резонансов
 * для каждой пары
 */
@Setter
@Getter
@Deprecated
public class Matrix {
    /**
     * Множество астр, от которых вычисляются резонансы.
     */
    protected Astra[] datum1;
    /**
     * Второе множество астр, к которым вычисляются резонансы от астр первого множества.
     */
    protected Astra[] datum2;
    /**
     * Двумерный массив резонансов между астрами.
     */
    protected ResonanceBatch[][] resonanceBatches;
    /**
     * Крайняя гармоника, до которой рассматриваются резонансы.
     */
    protected int edgeHarmonic;
    /**
     * Какова кратность круга для орбиса соединения.
     */
    protected int orbsDivider;
    /**
     * Тип матрицы.
     */
    protected MatrixType type;

    public boolean resonancePresent(Astra a, Astra b, int harmonic) {
        ResonanceBatch resonanceBatch = findResonance(a, b);
        if (resonanceBatch == null)
            throw new IllegalArgumentException("резонанс не найден");
        return resonanceBatch.hasGivenHarmonic(harmonic);
    }


    /**
     * Выдаёт строку с перечислением всех резонансов между астрами Матрицы.
     * Если тип "Космограмма", выдаются резонансы каждой точки с каждой следующей по списку.
     * Если тип "Синастрия", выдаются резонансы каждой точки с каждой точкой второй карты.
     * @return строковое представление всех резонансов между уникальными точками.
     */
    public String resultsOutput() {
        StringBuilder sb = new StringBuilder();

        switch (type) {
            case SYNASTRY -> {                             // таблица всех астр одной на все астры другой
                for (int x = 0; x < datum1.length; x++)
                    for (int y = 0; y < datum2.length; y++)
                        sb.append(resonanceBatches[x][y].resonancesOutput()).append("\n");
            }
            case COSMOGRAM -> {                            // полутаблица астр карты между собой
                for (int i = 0; i < datum1.length; i++)
                    for (int j = i + 1; j < datum2.length; j++)
                        sb.append(resonanceBatches[i][j].resonancesOutput()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Вычисление матрицы для двух массивов (конструктор)
     *  с заданием крайней гармоники и делителя орбиса
     * @param astras_1    первый массив астр
     * @param astras_2    второй массив астр
     * @param edgeHarmonic  до какой максимальной гармоники проводится поиск
     * @param orbsDivider   делитель для определения начального орбиса.
     */
    public Matrix(List<Astra> astras_1, List<Astra> astras_2, int edgeHarmonic, int orbsDivider) {
        datum1 = astras_1.toArray(Astra[]::new);
        datum2 = astras_2.toArray(Astra[]::new);
        type = Arrays.equals(datum1, datum2) ? COSMOGRAM : SYNASTRY;

        this.orbsDivider = type == SYNASTRY &&
                Settings.isHalfOrbsForDoubles() ?
                orbsDivider * 2 :
                orbsDivider;

        this.edgeHarmonic = edgeHarmonic;

        resonanceBatches = new ResonanceBatch[astras_1.size()][astras_2.size()];
        for (int i = 0; i < datum1.length; i++)
            for (int j = 0; j < datum2.length; j++)
                resonanceBatches[i][j] = new ResonanceBatch(datum1[i],
                        datum2[j],
                        CIRCLE / this.orbsDivider,
                        this.edgeHarmonic);
    }

    /**
     * Вычисление матрицы из двух массивов б/доп-параметров
     * Крайняя гармоника и делитель первообраза берутся из Настроек
     * @param astras_1  первый массив астр
     * @param astras_2   второй массив астр
     */
    public Matrix(List<Astra> astras_1, List<Astra> astras_2) {
        this(astras_1, astras_2, Settings.getEdgeHarmonic(), Settings.getOrbDivisor());
    }

    /**
     * Из одного массива (сам на себя) б/доп-параметров.
     * Крайняя гармоника и делитель орбиса берутся из Настроек.
     * @param astras     массив астр.
     */
    public Matrix(List<Astra> astras) {
        this(astras, astras);
    }

    /**
     * Выдаёт полный список уникальных резонансов матрицы:
     * для космограммы резонанс каждой астры со всеми последующими,
     * для синастрии резонанс каждой астры первой карты с каждой астрой второй.
     * @return список всех резонансов в том же порядке, как при выдаче описания.
     */
    public List<ResonanceBatch> getAllResonances() {
        List<ResonanceBatch> content = new ArrayList<>();
        switch (type) {
            case SYNASTRY ->
                // таблица всех астр одной на все астры другой
                content = IntStream.range(0, datum1.length)
                            .mapToObj(x -> Arrays.asList(resonanceBatches[x])
                                .subList(0, datum2.length))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
            case COSMOGRAM -> {
                // полутаблица астр карты между собой
                for (int i = 0; i < datum1.length; i++)
                    content.addAll(Arrays.asList(
                            resonanceBatches[i]).subList(i + 1, datum2.length));
            }
        }
        return content;
    }

    /**
     * Список всех резонансов для астры с указанным номером в списке астр.
     * Для космограммы выдаёт взаимодействие определённой астры со всеми
     * остальными астрами карты.
     * Для синастрии выдаёт взаимодействие определённой астры первой карты
     * со всеми астрами второй.
     * @param astraOrdinal номер астры в списке астр карты.
     * @return список резонансов, которые имеет астра с указанным номером,
     * {@code null}, если индекс за пределами массива астр первой карты.
     */
    public List<ResonanceBatch> getResonancesFor(int astraOrdinal) {
        if (astraOrdinal < 0 || astraOrdinal >= datum1.length)
            return null;
        return Arrays.stream(resonanceBatches[astraOrdinal])
                .collect(Collectors.toList());
    }

    /**
     * Список всех резонансов для астры с указанным номером в списке астр
     * из второй карты.
     * Для космограммы выдаёт то же, что {@link #getResonancesFor(int)}.
     * Для синастрии выдаёт взаимодействие определённой астры второй карты
     * со всеми астрами первой.
     * @param astraOrdinal номер астры в списке астр второй карты.
     * @return список резонансов, которые имеет астра с указанным номером
     * из второй карты.
     */
    public List<ResonanceBatch> getResonancesOfBackChartFor(int astraOrdinal) {
        if (astraOrdinal < 0 || astraOrdinal >= datum2.length)
            return null;
        return Arrays.stream(resonanceBatches)
                .map(row -> row[astraOrdinal])
                .collect(Collectors.toList());
    }

    /**
     * Список всех резонансов для астры с указанным именем.
     * Для космограммы выдаёт взаимодействие определённой астры со всеми остальными астрами карты.
     * Для синастрии выдаёт взаимодействие определённой астры первой карты со всеми астрами второй.
     * @param astraName имя астры (в случае синастрии имеется в виду первая карта).
     * @return список резонансов, которые имеет астра с указанным именем.
     */
    public List<ResonanceBatch> getResonancesFor(String astraName) {
        for (int i = 0; i < datum1.length; i++)
            if (datum1[i].getName().equals(astraName))
                return getResonancesFor(i);
        return null;
    }
    /**
     * Список всех резонансов для указанной астры.
     * Для космограммы выдаёт взаимодействие астры со всеми остальными астрами карты.
     * Для синастрии выдаёт взаимодействие астры указанной карты со всеми астрами другой.
     * @param astra астра (из любой карты).
     * @return список резонансов, которые имеет указанная астра.
     */
    public List<ResonanceBatch> getResonancesFor(Astra astra) {
//        if (datum1.length == 0)
//            throw new IllegalStateException("Матрица пуста.");

        var isIn1stArrays = true;

        for (Astra a : datum1) {
            if (a.equals(astra)) {
                break;
            }
        }

        if (astra.getHeaven() == datum1[0].getHeaven()) {
            for (int i = 0; i < datum1.length; i++)
                if (datum1[i].equals(astra))
                    return getResonancesFor(i);
        } else {
//            if (datum2.length == 0) throw new IllegalStateException("Матрица пуста.");
            if (datum2[0].getHeaven() != astra.getHeaven()) return null;

            for (int i = 0; i < datum2.length; i++)
                if (datum2[i].equals(astra))
                    return getResonancesOfBackChartFor(i);
        }
        return null;
    }

    /**
     * Список всех резонансов для астры с указанным именем из второй карты.
     * Для космограммы выдаёт то же, что {@link #getResonancesFor(String)}.
     * Для синастрии выдаёт взаимодействие определённой астры второй карты
     * со всеми астрами первой.
     * @param astraName имя астры из второй карты.
     * @return список резонансов, которые имеет указанная астра из второй карты.
     */
    public List<ResonanceBatch> getResonancesOfBackChartFor(String astraName) {
        for (int i = 0; i < datum2.length; i++)
            if (datum2[i].getName().equals(astraName))
                return getResonancesOfBackChartFor(i);
        return null;
    }

    /**
     * Выдаёт список астр, находящихся в резонансе с данной по указанной гармонике.
     *
     * @param astra    астра, резонансы с которой ищутся.
     * @param harmonic число, по которому определяется резонанс.
     * @return список астр, находящих в резонансе с данной по указанной гармонике.
     * Для космограммы просматриваются все остальные астры карты,
     * для синастрии все астры второй карты.
     */
    public List<Astra> getConnectedAstras(Astra astra, int harmonic) {
        return getResonancesFor(astra)
                .stream().filter(r -> r.hasHarmonicPattern(harmonic))
                .map(resonanceBatch -> resonanceBatch.getCounterpart(astra))
                .collect(Collectors.toList());
    }

    /**
     * Предикат, удостоверяющий, что между двумя указанными астрами
     * существует явный резонанс по указанному числу.
     *
     * @param a        первая астра (для синастрии астра из первой карты).
     * @param b        вторая астра (для синастрии астра со второй карты).
     * @param harmonic число, резонанс по которому интересует.
     * @return {@code true}, если обе астры имеются в матрице, и
     * между ними имеется явное взаимодействие по указанной гармонике.
     */
    public boolean astrasInResonance(Astra a, Astra b, int harmonic) {
        ResonanceBatch crossing = findResonance(a, b);
        if (crossing == null)
            return false;
        return crossing.hasGivenHarmonic(harmonic);
    }

    /**
     * Вытаскивает из матрицы конкретный объект резонанса
     * между двумя указанными астрами.
     * Если это синастрия, аргументы могут следовать в любом порядке.
     *
     * @param a первая астра.
     * @param b вторая астра.
     * @return резонанс из матрицы на пересечении первой астры со второй;
     * если хотя бы одна из астр пуста или не содержится в матрице,
     * или в обоих параметрах указана идентичная астра, то {@code null}.
     */
    public ResonanceBatch findResonance(Astra a, Astra b) {
        if (a == null || b == null || a.equals(b))
            return null;

        int x = -1, y = -1;

        for (int i = 0; i < datum1.length; i++)
            if (datum1[i].equals(a))
                x = i;

        boolean reversed = false;

        if (!Astra.ofSameHeaven(a, b) && x == -1)
            for (int i = 0; i < datum2.length; i++)
                if (datum2[i].equals(b)) {
                    x = i;
                    reversed = true;
                }

        if (x == -1) return null;

        var secondMassive = reversed ? datum1 : datum2;
        for (int i = 0; i < secondMassive.length; i++)
            if (secondMassive[i].equals(b))
                y = i;

        if (y == -1) return null;

        return reversed ?
                resonanceBatches[y][x] :
                resonanceBatches[x][y];
    }

}
