mud-server
==========

A MUD server written in Java.

## Status

This code is very much in an unfinished state, there may be radical shifts in the inner workings
in the future. Try not to make too many assumptions about stability and backup the database if you0
play with this as future code may corrupt the data and/or utilize an alternate approach for data storage.

This code requires at least Java 7 (1.7)

## Usage

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

See 'Installation and Setup' page in wiki for more details.

## Help

For help using the code and miscellaneous documentation, see the [Wiki](https://github.com/jnharton/mud-server/wiki) for this project.

## Updates

see the [Wiki](https://github.com/jnharton/mud-server/wiki)

## Copyright
Copyright (c) 2012 Jeremy Harton. See LICENSE.txt for further details.

The license given basically applies to all files in the source (MUDServer/src) directory.
