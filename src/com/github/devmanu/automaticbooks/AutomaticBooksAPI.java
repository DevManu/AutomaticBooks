package com.github.devmanu.automaticbooks;

public class AutomaticBooksAPI {


    protected static AutomaticBooks instance;

    private AutomaticBooksAPI() {
    }


    public static AutomaticBooks getAPI() {
        return instance;
    }


}
