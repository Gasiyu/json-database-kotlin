# JSON Database (Kotlin)

A client-server application that implements a JSON database with support for nested JSON structures. The database allows clients to store, retrieve, and delete JSON data using a simple command-line interface.


- Client-server architecture with socket communication
- Persistent JSON storage
- Support for nested JSON structures with path-based access
- Thread-safe operations with read-write locks
- Concurrent request handling with a thread pool
- Command-line interface for client operations
- JSON-based communication protocol


- Kotlin
- kotlinx.serialization for JSON processing
- Java Socket API for network communication
- Java concurrent utilities for thread safety
- Gradle for build management



- JDK 11 or higher
- Kotlin 1.5 or higher
- Gradle 7.0 or higher


```bash
./gradlew build
```


```bash
./gradlew run --args="server"
```

The server will start on localhost (127.0.0.1) port 23456.


```bash
./gradlew run --args="client -t <command_type> -k <key> -v <value>"
```

Or using a JSON file for input:

```bash
./gradlew run --args="client -in <filename>"
```


- `-t`: Command type (get, set, delete, exit)
- `-k`: Key (can be a string or a JSON array for nested access)
- `-v`: Value (for set command)
- `-in`: Input file containing a JSON request



```bash
./gradlew run --args="client -t get -k person"
```

Get a nested value:

```bash
./gradlew run --args="client -t get -k '[\"person\", \"name\"]'"
```


```bash
./gradlew run --args="client -t set -k person -v '{\"name\":\"John\",\"age\":30}'"
```

Set a nested value:

```bash
./gradlew run --args="client -t set -k '[\"person\", \"name\"]' -v '\"John\"'"
```


```bash
./gradlew run --args="client -t delete -k person"
```

Delete a nested value:

```bash
./gradlew run --args="client -t delete -k '[\"person\", \"name\"]'"
```


```bash
./gradlew run --args="client -t exit"
```


Create a JSON file (e.g., `request.json`) with the following structure:

```json
{
  "type": "set",
  "key": ["person", "name"],
  "value": "John"
}
```

Then run:

```bash
./gradlew run --args="client -in request.json"
```



```json
{
  "type": "command_type",
  "key": "key_or_path",
  "value": "value_to_set"
}
```

- `type`: Command type (get, set, delete, exit)
- `key`: Key or path to access (string or array of strings)
- `value`: Value to set (for set command)


```json
{
  "response": "status",
  "value": "retrieved_value",
  "reason": "error_message"
}
```

- `response`: Status ("OK" or "ERROR")
- `value`: Retrieved value (for get command)
- `reason`: Error message (for failed operations)


- `client/`: Client implementation
- `server/`: Server implementation
- `database/`: Database implementation
  - `Database.kt`: Interface defining database operations
  - `FileDatabase.kt`: File-based database implementation
  - `CommandParser.kt`: Parser for client commands
  - `DatabaseService.kt`: Service for processing commands
  - `JsonModels.kt`: Data models for JSON communication
  - `DatabaseCommand.kt`: Command objects for database operations
