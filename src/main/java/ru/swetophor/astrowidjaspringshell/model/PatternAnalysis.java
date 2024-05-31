package ru.swetophor.astrowidjaspringshell.model;

import jakarta.validation.constraints.NotNull;
import ru.swetophor.astrowidjaspringshell.config.Settings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static ru.swetophor.astrowidjaspringshell.utils.Decorator.singularFrame;

/**
 * Удобная обёртка для представления результатов гармонического анализа карты.
 */
public class PatternAnalysis implements Iterable<Map.Entry<Integer, List<Pattern>>> {
    private final SortedMap<Integer, List<Pattern>> listMap = new TreeMap<>();


    /**
     * Добавляет к Анализу паттерн, он добавляется к списку,
     * сопоставленному номеру гармоники как ключу.
     * Если паттернов по этой гармоники ещё не было,
     * инициализируется новое сопоставление.
     * @param pattern добавляемый паттерн астральных резонансов.
     */
    public void addPattern(Pattern pattern) {
        int harmonic = pattern.getHarmonic();
        listMap.putIfAbsent(harmonic, new ArrayList<>());
        listMap.get(harmonic).add(pattern);
    }

    /**
     * Возвращает список паттернов для указанной гармоники.
     * Если по данной гармонике не фиксировано ни одного паттерна, возвращается пустой список.
     *
     * @param harmonic номер гармоники, для которой нужно получить паттерны.
     * @return список паттернов, связанных с данной гармоникой, или пустой список, если таковых нет.
     */
    public List<Pattern> getPatternsFor(int harmonic) {
        List<Pattern> patterns = listMap.get(harmonic);
        return patterns == null ? new ArrayList<>() : patterns;
    }

    public int size() {
        return listMap.size();
    }

    /**
     * Сообщает среднюю силу паттернов для указанной гармоники,
     * рассчитанную как среднее арифметическое средней силы каждого паттерна
     * с указанным резонансным числом для данного узор-разбора.
     * @param harmonic по какой гармонике смотрим среднюю силу.
     * @return  среднее значение для силы паттернов по этому резонансному
     * числу, или 0, если таковых паттернов этой гармоники
     * в данном узор-разборе не обнаружено.
     */
    public Double getAverageStrengthForHarmonic(int harmonic) {
        List<Pattern> patterns = listMap.get(harmonic);
        return patterns == null || patterns.isEmpty() ?
                0.0 :
                patterns.stream()
                        .mapToDouble(Pattern::getAverageStrength)
                        .sum() / patterns.size();
    }

    /**
     * Сообщает, сколько всего астр связано в паттерны по указанной гармонике
     * в данном узор-разборе.
     * @param harmonic по какому резонансному числу хотим узнать.
     * @return  количество астр во всех паттернах в данному анализе паттернов
     * по указанному резонансному числу.
     */
    public int getAstrasQuantityFor(int harmonic) {
        List<Pattern> patterns = listMap.get(harmonic);
        return patterns == null || patterns.isEmpty() ?
                0 :
                patterns.stream()
                        .mapToInt(Pattern::size)
                        .sum();
    }

    /**
     * Выдаёт многостроку, составленную из суммы описаний паттернов,
     * как те предоставляются {@link Pattern#getConnectivityReport()}.
     * В начале выводится заголовок в рамке, сообщающий, по какой гармонике
     * это паттерны, сколько в них в общей сложности астр, и какова
     * средняя сила этих паттернов.
     * Если ни одного паттерна по указанной гармонике в данном анализе
     * не найдено, выдаёт рамку с сообщением об этом.
     * @param harmonic по какой гармонике запрашиваем статистику.
     * @return  готовое для текстового вывода представление паттернов
     *  данного разбора узоров для указанного резонансного числа.
     */
    public String getDetailedPatternRepresentation(int harmonic) {
        List<Pattern> patterns = listMap.get(harmonic);
        if (patterns == null || patterns.isEmpty())
            return singularFrame("Ни одного паттерна на резонансном числе " + harmonic);
        else
            return patterns.stream()
                .map(pattern -> "\t\t%s_______\n"
                        .formatted(pattern.getConnectivityReport()))
                .collect(joining(
                        "_______\n",
                        singularFrame(
                                    """
                                        Паттерны по числу %d%n
                                            <всего планет %d, средняя сила %.0f%%>
                                    """.formatted(harmonic,
                                                getAstrasQuantityFor(harmonic),
                                                getAverageStrengthForHarmonic(harmonic))
                        ),
                        "_______\n\n"));
    }

    /**
     * Выдаёт многостроку, рассказывающую обо всех паттернах этого сбора узоров.
     * Состоит из последовательно соединённых описаний паттернов по каждой
     * гармонике от 1 до {@link Settings#getEdgeHarmonic}, как то предоставляется
     * {@link #getDetailedPatternRepresentation}
     * @return описание паттернов по всем анализируемым гармоникам этой
     * карты или сочетания карт.
     */
    public String getFullAnalysisRepresentation() {
        return IntStream.rangeClosed(1, Settings.getEdgeHarmonic())
                .mapToObj(this::getDetailedPatternRepresentation)
                .collect(joining());
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @SuppressWarnings("NullableProblems")
    @Override
    @NotNull
    public Iterator<Map.Entry<Integer, List<Pattern>>> iterator() {
        return listMap.entrySet().iterator();
    }


    public String getShortAnalysisRepresentation() {
        return IntStream.rangeClosed(1, Settings.getEdgeHarmonic())
                .mapToObj(this::getPatternRepresentation)
                .collect(joining());
    }

    private String getPatternRepresentation(int harmonic) {
        StringBuilder output = new StringBuilder();
        listMap
                .forEach((key, list) -> {
                    output.append("%d: ".formatted(key));
                    if (list.isEmpty())
                        output.append("-\n");
                    else {
                        output.append(list.stream()
                                        .map(Pattern::getString)
                                        .collect(Collectors.joining(" | ")))
                                .append("\n");
                    }
                });
        return output.toString();

    }
}
