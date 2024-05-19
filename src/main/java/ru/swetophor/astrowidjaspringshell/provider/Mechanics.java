package ru.swetophor.astrowidjaspringshell.provider;

import ru.swetophor.astrowidjaspringshell.model.Astra;
import ru.swetophor.astrowidjaspringshell.model.AstraEntity;
import ru.swetophor.astrowidjaspringshell.model.Chart;
import ru.swetophor.astrowidjaspringshell.model.Harmonics;

import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static ru.swetophor.astrowidjaspringshell.model.AstraEntity.*;
import static ru.swetophor.astrowidjaspringshell.provider.CelestialMechanics.*;
import static ru.swetophor.astrowidjaspringshell.model.ZodiacSign.zodiumIcon;

/**
 * Инструментальный класс для решения
 * задач по пространственному расположению
 */
public class Mechanics {

    //    public static String секундФормат(double вГрадусах) {
//        double минутнаяЧасть = вГрадусах % 1;
//        if (минутнаяЧасть < 1 / (КРУГ * 60)) return String.valueOf((int) вГрадусах) + "°";
//        else if (вГрадусах  %  (1.0/60) < 1 / (КРУГ * 3600)) {
//            return String.valueOf((int) вГрадусах) + "°" +
//                    String.valueOf((int) (минутнаяЧасть * 60) + "'");
//        }
//        else {
//            return String.valueOf((int) вГрадусах) + "°" +
//                    String.valueOf((int) ((минутнаяЧасть * 60)) + "'") +
//                    String.valueOf((int) floor(((минутнаяЧасть * 60) % 1) * 60)) + "\"";
//        }
//    }

    public static String secondFormat(double inDegrees) {
        int[] coors = CelestialMechanics.degreesToCoors(inDegrees);
        return "%s°%s'%s\""
                .formatted(
                        format("%3s", coors[0]),
                        format("%2s", coors[1]),
                        format("%2s", coors[2])
                );
    }

    /**
     * Выдаёт строку вида градусы°минуты'секунды"
     *      на основе переданной дуги в десятичных градусах.
     * Если withoutExtraZeros = false,
     *      то отсутствующия секунды или секунды и минуты упускаются.
     */
    public static String secondFormat(double inDegrees, boolean withoutExtraZeros) {
        int[] coors = CelestialMechanics.degreesToCoors(inDegrees);
        StringBuilder degreeString = new StringBuilder();

        if (withoutExtraZeros &&
                (coors[0] != 0))
            degreeString.append(coors[0]).append("°");
        else
          degreeString.append(format("% 3d°", coors[0]));

        if (withoutExtraZeros &&
                (coors[1] > 0 || coors[2] > 0) &&
                (coors[1] != 0))
            degreeString.append(coors[1]).append("'");
        else
          degreeString.append(format("% 2d'", coors[1]));

        if (withoutExtraZeros &&
                coors[2] > 0)
            degreeString.append(coors[2]).append("\"");
        else
            degreeString.append(format("% 2d\"", coors[2]));

        if (degreeString.isEmpty())
            return "0°";

        return degreeString.toString();
    }

    /**
     * Аналогично функции секундФормат(),
     *      но выдаёт строку длиной точно 10 символов,
     *      выровненную влево.
     */
    public static String secondFormatForTable(double inDegrees, boolean withoutExtraZeros) {
        int[] coors = CelestialMechanics.degreesToCoors(inDegrees);
        StringBuilder formatHolder = new StringBuilder();

        if (withoutExtraZeros)
            formatHolder.append(format("%3s°", coors[0]));
        else
            formatHolder.append(format("%03d°", coors[0]));

        if (withoutExtraZeros &&
                (coors[1] > 0 || coors[2] > 0))
            formatHolder.append(format("%2s'", coors[1]));
        else
            formatHolder.append(format("%02d'", coors[1]));

        if (withoutExtraZeros &&
                coors[2] > 0)
            formatHolder.append(format("%2s\"", coors[2]));
        else
            formatHolder.append(format("%02d\"", coors[2]));

        return format("%-10s", formatHolder);

    }

    /**
     *      Выдаёт строку точно 10 знаков
     *          съ всеми избыточными нолями
     */
    public static String secondFormatForTable(double inDegrees) {
        int[] coors = CelestialMechanics.degreesToCoors(inDegrees);
        StringBuilder formatHolder = new StringBuilder();

        formatHolder.append(format("%03d°", coors[0]))
                    .append(format("%02d'", coors[1]))
                    .append(format("%02d\"", coors[2]));
        return format("%-10s", formatHolder);
    }


    /**
     *  Преобразует эклиптическую долготу в зодиакальную
     * @param position эклиптическая долгота.
     * @return  строку, представляющую зодиакальную координату (знак + секундФормат без лишних нолей).
     */
    public static String zodiacFormat(double position) {
        return "%c\t%s"
                .formatted(zodiumIcon(position),
                        secondFormat(position % 30, true));
    }


    public static void displayMultipliers(int upto) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i <= upto; i++) {
            output.append(i > 999 ?
                    "%d → ".formatted(i) :
                    "%3d → ".formatted(i));
            var multi = Harmonics.multipliersExplicate(i);
            output.append(multi.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" + ")));
            if (multi.size() > 1)
                output.append(" = ").append(Harmonics.multiSum(i));
            output.append("\n");
        }
        System.out.println(output);
    }


    public static void main(String[] args) {
        displayMultipliers(108);
//        buildHeavens(108);
        Harmonics.buildHeavensWithHarmonics(144);
    }


    /**
     * Создаёт композитную карту из двух данных карт. В композите каждая астра
     * получает среднюю координату между координатами этой астры в двух картах.
     * В композит попадают только те астры, которые есть в обеих картах.
     * Меркурий и Венера гарантированно помещаются в той же части Неба, что Солнце.
     * @param chart_a   первая карта для композита.
     * @param chart_b   вторая карта для композита.
     * @return  композитную карту, среднюю между двух данных карт.
     */
    public static Chart composite(Chart chart_a, Chart chart_b) {
        if (chart_a == null || chart_b == null)
            throw new IllegalArgumentException("карта для композита не найдена");
        Chart composite = new Chart("Средняя карта %s и %s"
                .formatted(chart_a.getName(), chart_b.getName()));

        Astra sun = null, mercury = null, venus = null;
        for (Astra astra : chart_a.getAstras()) {
            Astra counterpart = chart_b.getAstra(astra.getName());
            if (counterpart == null) continue;

            Astra compositeAstra = new Astra(astra.getName(),
                    findMedian(astra.getZodiacPosition(),
                            counterpart.getZodiacPosition()));
            composite.addAstra(compositeAstra);

            AstraEntity innerBody = AstraEntity.getEntityByName(compositeAstra.getName());
            if (innerBody != null)
                switch (innerBody) {
                    case SOL -> sun = compositeAstra;
                    case MER -> mercury = compositeAstra;
                    case VEN -> venus = compositeAstra;
                }

        }

        if (sun != null) {
            if (mercury != null &&
                    getArc(sun, mercury) > 30.0)
                composite.addAstra(mercury.advanceCoordinateBy(HALF_CIRCLE));

            if (venus != null &&
                    getArc(sun, venus) > 60.0)
                composite.addAstra(venus.advanceCoordinateBy(HALF_CIRCLE));
        }

        return composite;
    }

    /**
     * Дополняет к строке расширение файла Астровидьи, если строка
     * ещё не оканчивается на него. Если второй параметр {@code ДА},
     * расширение используется {@code .awc}, иначе {@code .awb}.
     * @param filename имя файла, которое снабжается расширением.
     * @param asAwc    использовать ли расширение {@code .awc} (иначе {@code .awb}).
     * @return  строку с добавленным, если было необходимо, расширением файла.
     */
    public static String extendFileName(String filename, boolean asAwc) {
        if (!filename.endsWith(".awb") && !filename.endsWith(".awc"))
            filename += asAwc ? ".awc" : ".awb";
        return filename;
    }
}
