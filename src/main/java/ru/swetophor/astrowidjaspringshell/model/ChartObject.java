package ru.swetophor.astrowidjaspringshell.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ChartObject {
//    protected ChartType type;
    protected String name;
    protected AstroMatrix matrix;

    public ChartObject(String name) {
        this.name = name;
    }

    public abstract Chart[] getData();
    public abstract boolean resonancePresent(Astra a, Astra b, int harmonic);
    public abstract int getDimension();

    /**
     * Выдаёт имя каты. Если оно длиннее указанного предела,
     * выдаёт его первые буквы и символ "…" в конце, так чтобы
     * общая длина строки была равна указанному пределу.
     *
     * @param limit максимальная длина возвращаемой строки.
     * @return имя карты, сокращённое до указанной длины.
     */
    public String getShortenedName(int limit) {
        return name.length() <= limit ?
                name :
                name.substring(0, limit - 1) + "…";
    }
}
