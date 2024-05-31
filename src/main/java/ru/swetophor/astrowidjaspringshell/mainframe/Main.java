package ru.swetophor.astrowidjaspringshell.mainframe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.swetophor.astrowidjaspringshell.client.UserController;
import ru.swetophor.astrowidjaspringshell.model.*;
import ru.swetophor.astrowidjaspringshell.service.HarmonicService;
import ru.swetophor.astrowidjaspringshell.service.LibraryService;

import static ru.swetophor.astrowidjaspringshell.config.Settings.*;
import static ru.swetophor.astrowidjaspringshell.utils.Decorator.*;

@Component
@Getter
@RequiredArgsConstructor
public class Main {
    public final ChartList DESK = new ChartList("Стол Астровидьи");

    private final UserController userController;
    private final LibraryService libraryService;
    private final HarmonicService harmonicService;

    public static final AstroSet DEFAULT_ASTRO_SET = new AstroSet(AstraEntity.values());

    //
//    public Main(LibraryService libraryService, HarmonicService harmonicService, UserController userController) {
//        this.libraryService = libraryService;
//        this.harmonicService = harmonicService;
//        this.userController = userController;
//    }


    @EventListener(ApplicationReadyEvent.class)
    public void runAstrowidja() {
        userController.welcome();

        if (isAutoloadEnabled())
            print(libraryService.loadAlbum(getAutoloadFile(), DESK));

        userController.mainCycle(this);

        if (isAutosave())
            print(libraryService.autosave(DESK));
    }




}
