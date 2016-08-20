AC Exercise
===========

This program analyzes temperature & humidity grow-house time-series to detect conditions opportune
to the development of diseases, based on a set of rules.

Choices & design
----------------

* The program is written in [Kotlin](kotlinlang.org), an easy-to-read, concise and safe general
purpose language for the JVM, that I felt was suited for such an exercise. Also, it's very fun to write in.
* I've designed the solution so that it would be simple to adapt and run "live". The main component,
the "supervisor" exposes an observable stream of alerts using [RxJava](https://github.com/ReactiveX/RxJava).

Assumptions
-----------

* I've assumed sensor values are pushed to the system in natural time order
* I've assumed a sensor value is "valid" until the next one received for that sensor
* I've assumed all dates are given at Paris time

Areas for improvement
---------------------

* In this version and for the sake of the exercise, the supervisor emits an alert when a "situation"
 is finished. In a real system, of course the alert must be emitted as soon as the time threshold
 for the situation is reached.

Building & running
------------------

You will need a JVM and [Gradle](https://gradle.org/gradle-download/) to build the program from sources.

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

