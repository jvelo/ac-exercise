AC Exercise
===========

This program analyzes temperature & humidity time-series from a grow-house to detect conditions opportune
to the development of diseases, based on a set of rules.

Choices & design
----------------

* The program is written in [Kotlin](kotlinlang.org), an easy-to-read, concise and safe general
purpose language for the JVM, that I felt was suited for such an exercise. Also, it's very fun to write in.
* I've designed the solution so that it would be simple to adapt and run "live". The main component,
the "supervisor" exposes an observable stream of hot alerts using [RxJava](https://github.com/ReactiveX/RxJava).
* The supervisor mutable state is isolated in its own immutable structure that gets copied and updated when state
 changes. This is similar in nature to the state of a React component ; it helps to reason about the program and its
 data, makes it predictable and less prone to unexpected side-effects.

Assumptions
-----------

* I've assumed sensor values are pushed to the system in natural time order
* I've assumed a sensor value is "valid" until the next one received for that sensor
* I've assumed all dates are given at Paris time

Structure
---------

Here's the project structure overview. Some files are not listed for clarity.

```
src
├── main
│   ├── kotlin
│   │   └── exercise
│   │       ├── app.kt        # Entry point of the app. CLI parsing, CSV loading, etc.
│   │       ├── model.kt      # Domain models the supervisor manipulates
│   │       ├── rules.kt      # The actual rules evaluated by the program (oïdium, botrytis)
│   │       ├── supervisor.kt # The core supervisor engines that detects diseases conditions
│   │       └── utils.kt      # Various utils
│   └── resources # CSV files
└── test
    ├── kotlin
    │   └── exercise
    │       ├── all-tests.kt  # Test suite with all tests
    │       └── test-utils.kt # Unit test helpers
    └── resources
        └── fixtures          # Test fixtures
```

Areas for improvement
---------------------

* In this version and for the sake of the exercise, the supervisor emits an alert when a "situation"
 is finished. In a real system, of course the alert must be emitted as soon as the time threshold
 for the situation is reached.
* Here the rules are declared formally by instantiating a rule object. It would be better to propose
 a rule builder or a DSL to express rules and their expressions in simpler fashion.
 For example such a DSL could allow to express rules the following way, in a type-safe manner :

        "Botrytis" {
			  when(temperature > 15)
			  when(temperature < 25)
			  when(humity > 90)
			  for(600 minutes)
        }

Building & running
------------------

You will need a JVM and [Gradle](https://gradle.org/gradle-download/) to build the program from sources.

You can build the app using :

```
gradle build
```

And to run directly the entry-point that analyses the sample lab files provided :

```
gradle run
```

Running tests
-------------

You can run unit tests using :

```
gradle cleanTest test
```

Building the docker image
-------------------------

The docker image is also built with gradle, using :

```
gradle distDocker
```

