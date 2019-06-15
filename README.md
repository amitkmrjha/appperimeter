This sample application is now maintained at https://github.com/amitkmrjha/appperimeter.

# App Perimeter (Scala)

## Getting started
To get started make sure you have sbt and git installed on your system.

### Running: Prerequisites

- You will need to cassandra installed and running:
- create and configure the key space in cassandra as per hostip-impl\src\main\resources\gen-schema.cql

### Running

On another terminal, clone this repo and run the sample app using the command `sbt runAll`:

```
git clone https://github.com/amitkmrjha/appperimeter.git
cd appperimeter
sbt clean
sbt compile

sbt stage
```


