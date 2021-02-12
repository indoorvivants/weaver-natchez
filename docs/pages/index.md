---
title: Home
scala-mdoc: true
---

**this library is considered to be so raw it's almost dangerous to use it**

A very small implementation of `Span` and `EntryPoint` to create a
trace tree in memory and dump it pretty when the test fails in weaver

## Installation

This "library" is published to Sonatype Snapshots

```scala
resolvers += Resolver.sonatypeRepo("snapshots"),
libraryDependencies += "com.indoorvivants" %% "wrenchez-core" % "@VERSION@"
```

It's available for Scala 2.12, 2.13, and 3.0.0-M3

## Why?

I'm not sure. It's pretty.

[Example from tests](https://github.com/indoorvivants/weaver-natchez/blob/master/modules/core/src/test/scala/Tests.scala):

![](assets/clickbait.png)
