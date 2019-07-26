package com.snowdragon.whatsnext.patterns;

import java.util.HashMap;

public class Invoker {

    private final HashMap<String, Command> mCommandMap;

    public static final Command EMPTY_COMMAND = new Command() {
        @Override
        public void execute() {

        }
    };

    public Invoker() {
        mCommandMap = new HashMap<>();
    }

    public void register(String commandName, Command command) {
        mCommandMap.put(commandName, command);
    }

    public void execute(String commandName) {
        Command command = mCommandMap.get(commandName);
        if(command == null) {
            throw new IllegalArgumentException("Command name does not exist");
        }
        command.execute();
    }
}
