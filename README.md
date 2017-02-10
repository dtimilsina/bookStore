# Simple Bookstore

## Compiling

A makefile is provided. Run `make` to compile the Java files (namely, the client and the server).

## Running

All commands are as specified in the assignment document.

### The Server

java Server <port> <database filename>

ex: java Server 8888 test.db

### The Client (Java)

java Client <server address> <port>

ex: java Client localhost 8888

### The Client (Python)

python Client.py <server address> <port>

ex: python Client.py localhost 8888
