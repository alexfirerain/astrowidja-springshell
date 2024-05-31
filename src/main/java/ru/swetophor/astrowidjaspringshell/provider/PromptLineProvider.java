package ru.swetophor.astrowidjaspringshell.provider;

import lombok.RequiredArgsConstructor;
import org.jline.utils.AttributedString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class PromptLineProvider implements PromptProvider {
    private final String programName = "АстроВидья";
    private ProgramState programState;

    @Override
    public AttributedString getPrompt() {
        return AttributedString.fromAnsi(programName + switch(programState) {
            case BASES -> ": базы";
            case CHART -> ": карта";
            default -> "";
        });
    }
}
