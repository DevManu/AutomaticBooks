### AutomaticBooks


AutomaticBooks is a plugin that allows you to open books in a very easy way.


This plugin has an API that you can use in your plugin. 


## How to use the API

* Add AutomaticBooks.jar to your project.
* Set "**softdepend: [AutomaticBooks]**" in plugin.yml;

### Open a book

```java

//Get main class
AutomaticBooks automaticBooks = AutomaticBooksAPI.getAPI(); 

//Create a list which contains all pages of your book.
List<String> pages = new ArrayList<String>();
pages.add("&9This is the first line of the first page of the book \n"
+ "&8This is the second line of the first page of the book");
pages.add("&cThis is the first line of the second page of the book \n"
+ "&8This is the second line of the second page of the book");


//Get the player
Player player = Bukkit.getPlayer("_Ma_nu_");


//Open the book
automaticBooks.getBookOpener().openBook(player, pages);


```

