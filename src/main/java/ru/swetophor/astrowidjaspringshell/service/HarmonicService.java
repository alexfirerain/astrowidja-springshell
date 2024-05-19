package ru.swetophor.astrowidjaspringshell.service;

import org.springframework.stereotype.Service;
import ru.swetophor.astrowidjaspringshell.model.AstroMatrix;
import ru.swetophor.astrowidjaspringshell.model.ChartObject;

import java.util.HashMap;
import java.util.Map;

@Service
public class HarmonicService {
    private final Map<ChartObject, AstroMatrix> matrices = new HashMap<>();

    private void addMatrix(ChartObject chartObject) {
        if (matrices.containsKey(chartObject) ) {
            return;
        }
        matrices.put(chartObject, new AstroMatrix(chartObject.getData()));
    }
}
