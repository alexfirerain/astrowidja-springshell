package ru.swetophor.astrowidjaspringshell.model;

import lombok.Getter;
import ru.swetophor.astrowidjaspringshell.config.Settings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.swetophor.astrowidjaspringshell.config.Settings.*;
import static ru.swetophor.astrowidjaspringshell.provider.CelestialMechanics.calculateStrength;
import static ru.swetophor.astrowidjaspringshell.provider.CelestialMechanics.getArcForHarmonic;
import static ru.swetophor.astrowidjaspringshell.config.Settings.getPrimalOrb;

/**
 * Олицетворяет группу связанных каким-то резонансом точек
 * (астр из одной или нескольких карт).
 * Точки считаются связанными по данной гармонике, если между ними присутствует
 * соответствующий резонанс; для принадлежности паттерну точка должна иметь
 * резонанс по крайней мере с одной из входящих в него точек.
 */
@Getter
public class Pattern {
    /**
     * По какому гармоническому числу выделен паттерн.
     */
    private final int harmonic;
    /**
     * {@link AstroMatrix Матрица резонансов}, на базе которой выделяются аспекты и паттерны.
     */
    private final AstroMatrix analysis;
    /**
     * Сопоставление астр и суммы их зазоров с каждой другой астрой паттерна.
     */
    private final Map<Astra, Double> elementsClearances = new HashMap<>();
    /**
     * Набор карт (одна или несколько), к которым (или которой) принадлежат астры паттерна.
     */
    private final List<Chart> heavens = new ArrayList<>();
    /**
     * Сумма эффективных орбисов астр в паттерне.
     */
    private double totalClearance = 0.0;

    /**
     * Задаёт новый паттерн резонансов по указанной гармонике,
     * рассчитываемый на базе указанной матрицы.
     * @param harmonic      указанная гармоника.
     * @param host  указанная {@link AstroMatrix АстроМатрица}.
     */
    public Pattern(int harmonic, AstroMatrix host) {
        this.harmonic = harmonic;
        this.analysis = host;
    }

    /**
     * Задаёт новый паттерн резонансов по указанной гармонике,
     * рассчитываемый на базе указанной матрицы. В него сразу
     * добавляются астры из предоставленного списка.
     * @param harmonic указанная гармоника.
     * @param astras    предложенный список астр.
     * @param host      указанная {@link AstroMatrix АстроМатрица}.
     */
    public Pattern(int harmonic, List<Astra> astras, AstroMatrix host) {
        this(harmonic, host);
        astras.forEach(this::addAstra);
    }

    /**
     * Добавляет астру к паттерну, обновляя сумматоры орбисов:
     * общий для паттерна и для каждого элемента, включая добавляемый.
     * Если астра с таким именем из той же карты уже есть в паттерне,
     * она игнорируется и повторно не добавляется.
     *
     * @param astra добавляемая к паттерну астра.
     */
    public void addAstra(Astra astra) {
        for (Astra a : elementsClearances.keySet())
            if (a.isTheSame(astra)) return;
        if (!heavens.contains(astra.getHeaven()))
            heavens.add(astra.getHeaven());
        double clearanceSum = 0.0;
        for (Astra a : elementsClearances.keySet()) {
            double clearance = getArcForHarmonic(astra, a, harmonic);
            clearanceSum += clearance;
            elementsClearances.forEach((key, value) ->
                    elementsClearances.put(key, value + clearance));
            totalClearance += clearance;
        }
        elementsClearances.put(astra, clearanceSum);
    }

    /**
     * Выдаёт список астр, входящих в паттерн.
     * Список сортирован по убыванию средней силы номинального аспекта астры
     * к другим астрам паттерна, соответствующей возрастанию суммы зазоров
     * этой астры с остальными астрами паттерна.
     * @return список астр в паттерне, сортированный по возрастанию силы связанности.
     */
    public List<Astra> getAstrasByConnectivity() {
        return elementsClearances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Выдаёт многострочное представление паттерна.
     * Первая строка докладывает среднюю силу связанности паттерна и кол-во астр.
     * Каждая следующая строка содержит символ астры и (в скобках) среднюю
     * силу её связанности с другими элементами паттерна.
     * Если паттерн включает астры из более чем одной карты, вслед
     * за именем и координатой астры отображается также краткое имя владельца.
     * Если паттерн содержит только одну астру (такой паттерн не считается
     * валидным, и такой случай не должен возникать), представление имеет вид
     * {@code "{символ} (-)"}.
     *
     * @return многостроку с представлением паттерна.
     */
    public String getConnectivityReport() {
        return size() == 1 ?
                "%s (-)%n"
                        .formatted(getString()) :
                "\t%.0f%% (%d):%n"
                        .formatted(getAverageStrength(),
                                size())
                        +
                        getAstrasByConnectivity().stream()
                                .map(astra -> "\t\t%s%s (%.0f%%)%n"
                                        .formatted(
                                                astra.getSymbolWithDegree(),
                                                getDimension() > 1 ?
                                                        "<%s>".formatted(astra.getHeaven().getShortenedName(3)) :
                                                        "",
                                                calculateStrength(
                                                        defineOrb(),
                                                        elementsClearances.get(astra) / (size() - 1)
                                                )
                                        )
                                )
                                .collect(Collectors.joining());
    }
    // TODO: сделать (скорее всего в МногоКарте или в Астро Матрице)
    //  умное сокращение названий карт

    /**
     * @return количество астр в паттерне.
     */
    public int size() {
        return elementsClearances.size();
    }

    /**
     * Сообщает размерность, т.е. скольки картам принадлежат
     * входящие в паттерн астры.
     * @return  количество карт, к которым относятся элементы паттерна.
     */
    public int getDimension() {
        return heavens.size();
    }

    /**
     * Сообщает, не пуст ли паттерн.
     * @return  {@code true}, если в паттерне нет ни одной астры. И наоборот.
     */
    public boolean isEmpty() {
        return elementsClearances.isEmpty();
    }

    /**
     * Добавляет к паттерну все астры из другого паттерна.
     * (Сомнительно, нужен ли такой функционал).
     * @param pattern другой паттерн, астры из которого добавляются.
     */
    public void addAllAstras(Pattern pattern) {
        pattern.getElementsClearances().keySet().forEach(this::addAstra);
    }


    public double getAverageStrength() {
        return size() >= 2 ?
                calculateStrength(defineOrb(), totalClearance / size()) :
                0.0;
    }

    /**
     * Инструментальный метод, определяющий орб, используемый для
     * суждения о силе аспектов-резонансов.
     * @return
     */
    private double defineOrb() {
        return getDimension() > 1 && isHalfOrbsForDoubles() ?
                getPrimalOrb() / 2 :
                getPrimalOrb();
    }

    /**
     * Текстовая репрезентация паттерна.
     * @return  строку, состоящую из символов входящих в паттерн астр,
     * упорядоченных по убыванию средней связанности.
     */
    public String getString() {
        return getAstrasByConnectivity().stream()
                .map(Astra::getSymbol)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    /**
     * Предикат, удостоверяющий, что в группе астр наличествует
     * номинальный аспект в явном виде для хотя бы одной пары.
     *
     * @return {@code false}, если паттерн пуст или содержит только
     * одну астру, или если ни в одной из пар элементов нет номинального резонанса.
     * {@code true}, если хотя бы в одной паре номинальный резонанс
     * наличествует.
     */
    public boolean isValid() {

        List<Astra> astras = elementsClearances.keySet().stream().toList();

        return IntStream.range(0, astras.size() - 1)
                .anyMatch(i -> IntStream.range(i + 1, astras.size())
                        .anyMatch(j ->
                                analysis.inResonance(astras.get(i), astras.get(j), harmonic)));
    }

    // TODO: сделать считалку паттернов для синастрии
}
