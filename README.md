mud-server
==========

A MUD server written in Java.

## Status

This code is very much in an unfinished state, despite the fact that it is fairly stable and usable. As such, there may be radical shifts (unannounced?) in the inner workings in the future. I will attempt to make any such shifts known in the internal wiki on here. It would be wise to regularly backup the database if you use this code as, despite the above, it t may have occasional stability problems and attempting to switch to a newer version may result in corrupted data and/or the server may utilize an alternate approach for data storage.

*For major changes see [Updates](https://github.com/jnharton/mud-server/wiki/Updates) page in wiki.*

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
