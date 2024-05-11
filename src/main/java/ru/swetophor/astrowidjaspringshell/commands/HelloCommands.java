package ru.swetophor.astrowidjaspringshell.commands;


import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class HelloCommands   {

    @ShellMethod(key = "hello", value = "звёзды приветствуют")
    public String sayHallo(@ShellOption(defaultValue = "World") String arg) {
        return "Hallo " + arg + "!";
    }

    @ShellMethod(key = "goodbye", value = "звёзды прощают")
    public String sayGoodbye() {
        return "Goodbye";
    }
}
