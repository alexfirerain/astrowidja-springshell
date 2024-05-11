package ru.swetophor.astrowidjaspringshell.model;

import lombok.Getter;
import ru.swetophor.astrowidjaspringshell.provider.CelestialMechanics;

import java.util.List;

import static java.lang.Math.floor;
import static ru.swetophor.astrowidjaspringshell.model.Harmonics.findMultiplier;
import static ru.swetophor.astrowidjaspringshell.provider.Mechanics.secondFormat;

/**
 * Некоторая установленная для реальной дуги гармоническая кратность.
 * Аспект характеризуется математическим определением резонанса
 * (основное гармоническое число и множитель повторения кратности)
 * и степенью точности (добротности) резонанса, определяемой как близость
 * реального небесного расстояния к дуге точного аспекта.
 */
@Getter
public class Aspect {
    /**
     * Гармоника, в которой аспект предстаёт соединением.
     * Т.е. число, на которое делится Круг, чтобы получить
     * дугу единичного резонанса для данной гармоники.
     * -- GETTER --
     *  Сообщает резонансное число аспекта.
     *
     * @return номер гармоники.
     */
    private final int numeric;              //
    /**
     * Множитель дальности, или повторитель кратности. Т.е. то число, на которое
     * нужно умножить дугу единичного резонанса этой гармоники, чтоб получить
     * дугу чистого неединичного аспекта.
     */
    private final int multiplicity;               //

    /**
     * Разность дуги резонанса с дугой чистого аспекта, полученной как
     * (360° / гармоника) * множитель. Т.е. эффективный орбис аспекта.
     * -- GETTER --
     *  Сообщает орбис аспекта, т.е. разность координаты и экзакта.
     *
     * @return разность фактического и чистого аспекта в градусах.

     */
    private final double clearance;           //

    /**
     * Эффективный орбис аспекта, выраженный в %-ах, где 100% означает полное
     * совпадение реальной дуги с математическим аспектом (экзакт, эффективный орбис 0°),
     * а 0% означает совпадение эффективного орбиса с предельным для данной гармоники.
     * Иначе говоря, точность аспекта, выраженная в %-ах.
     */
    private final double strength;            //
    /**
     * Модель аспекта, одного из резонансов в дуге.
     * @param numeric      гармоника, т.е. кратность дуги Кругу.
     * @param clearance     эффективный орбис в карте гармоники.
     * @param fromArc   реальная дуга, в которой распознан этот резонанс.
     * @param orb   первичный орб для соединений, используемый при определении резонансов.
     */
    public Aspect(int numeric, double clearance, double fromArc, double orb) {
        this.numeric = numeric;
        this.multiplicity = findMultiplier(numeric, fromArc, orb);
        this.clearance = clearance;
        this.strength = CelestialMechanics.calculateStrength(orb, clearance);
        this.depth = (int) floor(orb / clearance);
    }
    /**
     * Выдаёт список простых множителей, в произведении дающих
     * число резонанса данного аспекта.
     * @return  список множителей гармоники, каждый из которых является простым числом.
     */
    public List<Integer> getMultipliers() {
        return Harmonics.multipliersExplicate(numeric);
    }
    /**
     * Точность аспекта через количество последующих гармоник, через кои проходит
     * -- GETTER --
     *  Сообщает глубину аспекта, т.е. в скольки следующих
     *  после данной гармониках он сохраняется.
     *
     * @return 0, если аспект отсутствует;
     *      1, если аспект присутствует только в этой гармонике;
     *      n, если соединение присутствует в гармониках кратностью до n от данной.

     */
    private final int depth;

    /**
     * Выводит характеристику, насколько точен резонанс.
     *
     * @return строковое представление ранга точности.
     */
    public String getStrengthLevel() {
        if (depth <= 1) return "- приблизительный ";
        else if (depth == 2) return "- уверенный ";
        else if (depth <= 5) return "- глубокий ";
        else if (depth <= 12) return "- точный ";
        else if (depth <= 24) return "- глубоко точный ";
        else return "- крайне точный ";
    }

    /**
     * Рейтинг силы в звёздах:
     * <p>★        < 50% - присутствует только в данной гармонике</p>
     * <p>★★       50-66% - присутствует в данной и в следующей х2</p>
     * <p>★★★     67-83% - сохраняется ещё в гармониках х3, х4 и х5</p>
     * <p>★★★★    84-92% - сохраняется до гармоник х12</p>
     * <p>★★★★★   93-96% - сохраняется до х24 гармоник</p>
     * <p>✰✰✰✰✰   > 96 % - присутствует в нескольких десятках кратных гармоник</p>
     *
     * @return звездообразный код рейтинга силы согласно соответствию.
     */
    public String strengthRating() {
        if (depth <= 1) return "★";
        if (depth == 2) return "★★";
        if (depth <= 5) return "★★★";
        if (depth <= 12) return "★★★★";
        if (depth <= 24) return "★★★★★";
        return "✰✰✰✰✰";
    }


    public boolean hasResonance(int harmonic) {
        return harmonic % numeric == 0 && harmonic / numeric <= depth;
    }

    @Override
    public String toString() {
        return "Резонанс %d (x%d) - %s как %d (%.2f%%, %s)%n".formatted(
                numeric,
                multiplicity,
                getStrengthLevel(),
                depth,
                strength,
                secondFormat(clearance, true));
    }
}
