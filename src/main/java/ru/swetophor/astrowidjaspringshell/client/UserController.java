package ru.swetophor.astrowidjaspringshell.client;

import ru.swetophor.astrowidjaspringshell.mainframe.Main;

public interface UserController {

    void welcome();

    void mainCycle(Main main);

    boolean confirmationAnswer(String prompt);
}
