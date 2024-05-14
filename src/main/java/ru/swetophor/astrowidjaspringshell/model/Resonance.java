package ru.swetophor.astrowidjaspringshell.model;

import lombok.Getter;
import lombok.Setter;
import ru.swetophor.astrowidjaspringshell.config.Settings;
import ru.swetophor.astrowidjaspringshell.provider.CelestialMechanics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.swetophor.astrowidjaspringshell.provider.CelestialMechanics.getArcForHarmonic;
import static ru.swetophor.astrowidjaspringshell.model.Harmonics.findMultiplier;
import static ru.swetophor.astrowidjaspringshell.provider.Mechanics.secondFormat;
import static ru.swetophor.astrowidjaspringshell.provider.Interpreter.ResonanceDescription;

/**
 * Гармонический анализ взаимодействия некоторых двух астр.
 * Содержит ссылки на две точки (астры) и {@link #aspects список} рассчитанных
 * для этой {@link #arc дуги} {@link Aspect Аспектов}.
 */
@Setter
@Getter
public class Resonance {
    /**
     * Первая астра.
     */
    private Astra astra_1;
    /**
     * Вторая астра.
     */
    private Astra astra_2;
    /**
     * Угловое расстояние меж точками на небе, т.е. дуга, резонансы которой считаются.
     */
    private double arc;
    /**
     * Максимальный орбис для соединения, т.е. базовый допуск, с которым при расчётах считается,
     * что резонанс возник. Для каждой гармоники базовый орбис пропорционально уменьшается.
     */
    private double orb;
    /**
     * Наибольший проверяемый целочисленный резонанс.
     */
    private int ultimateHarmonic;
    /**
     * Найденные в пределах орбиса аспекты по росту гармоники.
     */
    private List<Aspect> aspects = new ArrayList<>();

    /**
     * Получение массива аспектов для дуги между двумя астрами (конструктор)
     *
     * @param a                первая астра резонанса.
     * @param b                вторая астра резонанса.
     * @param primalOrb              первичный орбис резонанса для соединений
     *                              (должен предоставляться {@link Settings#getPrimalOrb()}),
     *                              сокращение для синастрий осуществляется внутри метода
     *                               на основании глобальной настройки {@link Settings#isHalfOrbsForDoubles()}.
     * @param ultimateHarmonic до какой гармоники продолжать анализ.
     */
    Resonance(Astra a, Astra b, double primalOrb, int ultimateHarmonic) {
        if (a == b)
            throw new IllegalArgumentException("Одна и та же астра не делает резонанса сама с собой");
        astra_1 = a;
        astra_2 = b;
        arc = CelestialMechanics.getArc(a, b);
        // возможно, для более чем двойных карт брать ещё пропорционально меньше? наверное всё же нет
        this.orb =
                a.getHeaven() != b.getHeaven() && Settings.isHalfOrbsForDoubles() ?
                primalOrb / 2 :
                primalOrb;
        this.ultimateHarmonic = ultimateHarmonic;

        IntStream.rangeClosed(1, ultimateHarmonic).forEach(h -> {
            double arcInHarmonic = getArcForHarmonic(a, b, h);
            if (arcInHarmonic < orb && isNewSimple(h))
                aspects.add(new Aspect(h, arcInHarmonic, arc, orb));
        });
    }

    /**
     * Конструктор резонанса, использующий глобальные значения
     * первичного орба и количества анализируемых гармоник.
     * @param a первая астра резонанса.
     * @param b вторая астра резонанса.
     */
    Resonance(Astra a, Astra b) {
        this(a, b, Settings.getPrimalOrb(), Settings.getEdgeHarmonic());
    }

    /**
     * Вспомогательный метод отсечения кратных гармоник при заполнении списка аспектов.
     *
     * @param aNewNumber число, которое проверяется на кратность уже найденным аспектам.
     * @return {@code истинно}, если проверяемое число не кратно никакому из {@link #aspects уже найденных} (кроме 1),
     * а также не является точным соединением, проходящим до данной гармоники. Следовательно,
     * эту гармонику надо брать в {@link #aspects набор}. Если же {@code ложно}, брать её в набор не нужно.
     */
    private boolean isNewSimple(int aNewNumber) {
        boolean isConjunction = false;

        for (Aspect next : aspects) {
            int aPreviousHarmonic = next.getNumeric();

            if (aPreviousHarmonic == 1)
                isConjunction = true;

            if (aNewNumber % aPreviousHarmonic != 0)
                continue;

            if (isConjunction &&
                    arc > orb / aNewNumber &&
                    findMultiplier(aNewNumber, arc, orb) == 1)
                continue;

            return false;
        }
        return true;
    }

    private String resoundsInfo() {
        StringBuilder sb = new StringBuilder();

        if (aspects.isEmpty()) {
            sb.append("Ни одного резонанса до %d при орбисе %s%n".formatted(ultimateHarmonic, orb));
        }
        for (Aspect aspect : getAspectsByStrength()) {
            sb.append(ResonanceDescription(aspect.getNumeric(), aspect.getMultiplicity()));
            sb.append(aspect);
        }
        return sb.toString();
    }

    public String resonancesOutput() {
        return getTitle() +
                resoundsReport();
    }

    private List<Aspect> getAspectsByStrength() {
        return aspects.stream()
                .sorted(Comparator.comparing(Aspect::getStrength).reversed())
                .collect(Collectors.toList());
    }


    public String resoundsReport() {
        StringBuilder sb = new StringBuilder();

        if (aspects.isEmpty())
            sb.append("Ни одного резонанса до %d при орбисе %s%n".formatted(ultimateHarmonic, orb));
        getAspectsByStrength().forEach(aspect -> {
            sb.append(ResonanceDescription(aspect.getNumeric(), aspect.getMultiplicity()));
            sb.append("Резонанс %d/%d %s (%.0f%%) --- %.2f %n".formatted(
                    aspect.getMultiplicity(),
                    aspect.getNumeric(),
                    aspect.strengthRating(),
                    aspect.getStrength(),
                    aspect.getStrength() / Math.pow(Math.log(aspect.getNumeric() + 1.0), 0.5)));
        });
        return sb.toString();
    }

    /**
     * Сообщает, что запрашиваемое число присутствует
     * среди множителей хотя бы одного из аспектов в резонансе.
     * @param harmonic проверяемое на кратность гармоническое число.
     * @return  {@code true}, если резонансное число хотя бы одного
     * аспекта является кратным запрашиваемому числу, {@code false}в ином случае.
     */
    public boolean hasResonanceElement(int harmonic) {
        return aspects.stream()
                .anyMatch(a -> a.hasMultiplier(harmonic));
    }

    public boolean hasGivenHarmonic(int harmonic) {
        return aspects.stream()
                .anyMatch(a -> a.getNumeric() == harmonic);
    }

    public boolean hasHarmonicPattern(int harmonic) {
        return aspects.stream()
                .anyMatch(a -> a.hasResonance(harmonic));
    }


    /**
     * Возвращает ту астру резонанса, которая не равна указанной.
     *
     * @param taken указанная астра резонанса.
     * @return {@code null}, если указана астра, которой нет в резонансе,
     * если же указана одна из астр в наличии, возвращается вторая.
     */
    public Astra getCounterpart(Astra taken) {
        if (astra_1.equals(taken))
            return astra_2;
        if (astra_2.equals(taken))
            return astra_1;
        return null;
    }

    /**
     * <p>Выдаёт строку, описывающую резонанс в виде:</p>
     * <p>для аспекта внутри одной карты:</p>
     * "* Дуга между {символ} {координата} и {символ} {координата} ({карта})"
     * <p>для аспекта между разными картами:</p>
     * "* Дуга между {символ} {координата} ({карта}) и {символ} {координата} ({карта})"
     * @return
     */
    public String getTitle() {
        return astra_1.getHeaven() == astra_2.getHeaven() ?
                "%n* Дуга между %c %s и %c %s (%s) = %s%n".formatted(
                        astra_1.getSymbol(), astra_1.getZodiacDegree(),
                        astra_2.getSymbol(), astra_2.getZodiacDegree(),
                        astra_1.getHeaven().getName(),
                        secondFormat(arc, true)) :
                "%n* Дуга между %c %s (%s) и %c %s (%s) = %s%n".formatted(
                        astra_1.getSymbol(), astra_1.getZodiacDegree(),
                        astra_1.getHeaven().getName(),
                        astra_2.getSymbol(), astra_2.getZodiacDegree(),
                        astra_2.getHeaven().getName(),
                        secondFormat(arc, true));
    }

}
