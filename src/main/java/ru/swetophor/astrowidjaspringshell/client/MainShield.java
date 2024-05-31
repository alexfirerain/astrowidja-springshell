package ru.swetophor.astrowidjaspringshell.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.swetophor.astrowidjaspringshell.model.ChartObject;
import ru.swetophor.astrowidjaspringshell.provider.ProgramState;

import static ru.swetophor.astrowidjaspringshell.provider.ProgramState.MAIN;

//@Component
@Getter
@Setter
public class MainShield {
    ProgramState state = MAIN;
    ChartObject activeChart = null;

}
