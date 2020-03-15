package com.github.devmanu.automaticbooks;

import com.github.devmanu.automaticbooks.AutomaticBooks;

public class AutomaticBooksAPI {


    protected static AutomaticBooks instance;

    private AutomaticBooksAPI() {}


    public static AutomaticBooks getAPI() { ;
        return instance;
    }



}
