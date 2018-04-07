mud-server
==========

A MUD server written in Java which aspires to be something of a general purpose framework for such games. For example, I have adopted the MUSH/MUCK convention of allowing exit names to be arbitrary text (rather than n/s/e/w), although there may be some support for reliably using the latter in the future (nothing explicity prevents creating exits with those names). Also, there is a fairly limited internal scripting system highly reminiscent of MPI from Fuzzball MUCK (http://www.belfry.com/fuzzball/mpihelp.html). Which is to say that it allows setting, modifying, and checking properties on each object. In general, though the code sticks closer to the hardcode end (at the moment) regard game systems and mechanics.

### Status

This code is very much in an unfinished state, despite the fact that it is fairly stable and usable. As such, there may occasionally be radical shifts in the inner workings in the future. It would be wise to regularly backup the database if you use this code as, despite the above, it may have occasional stability problems and attempting to switch to a newer version may result in corrupted data and/or the server may someday utilize an alternate approach for data storage.

This code requires at least Java 8 (1.8) due to use of streams and predicates.

### Libraries

[Google-gson](https://github.com/google/gson) is used for handling JSON files in certain places.

### Usage

```
Usage: java -jar mud_server.jar [ parameters ]

  --port  <port number> Specify the port for the server to listen on, default is 4000
  --db <database>       Specifies the database file to load (databases directory)
  --debug               Enable debugging messages
  --enable-logging      Enable logging of various things
  --theme               Use the specified theme file (theme directory)
  --setup               Run first time setup
  --telnet              Indicate that you'd like the server to handle pure telnet

* generally speaking, you will only use setup once, and can run the server with just that
* parameter if you just want to generate the necessary files and don't need anything else set
* telnet connections here just means a raw connection sending one character at a time
```

See [Installation and Setup](https://github.com/jnharton/mud-server/wiki/Installation-and-Setup) for more details.

### Help

For help using the code and miscellaneous documentation, see the [Wiki](https://github.com/jnharton/mud-server/wiki) for this project.

### Updates

See [Updates](https://github.com/jnharton/mud-server/wiki/Updates) for more details.

### Copyright

Copyright (c) 2012 Jeremy Harton. See LICENSE.txt for further details.

The license given basically applies to all files in the source (MUDServer/src) directory unless otherwise specified here or in a file (license-exclusions.txt) within the package/folder in question.