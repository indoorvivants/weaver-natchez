---
title: Home
mdoc: true
---

**this library is considered to be so raw it's almost dangerous to use it**

A very small and incomplete implementation of `Span` and `EntryPoint` to create a
trace tree in memory and dump it pretty when the test fails in weaver

## Installation

This "library" is published to Sonatype Snapshots, for all latest Scala 2.12, 2.13 and 3.0.0-M3.

It's also cross built for Cats effect 2 and 3 - CE3 artifacts are 1 minor version higher.

**Cats Effect 2**

```scala
resolvers += Resolver.sonatypeRepo("snapshots"),
libraryDependencies += "com.indoorvivants" %% "wrenchez-core" % "@CE2_VERSION@"
```
**Cats Effect 3**

```scala
resolvers += Resolver.sonatypeRepo("snapshots"),
libraryDependencies += "com.indoorvivants" %% "wrenchez-core" % "@CE3_VERSION@"
```

## Why?

I'm not sure. It's pretty.

[Example from tests](https://github.com/indoorvivants/weaver-natchez/blob/master/modules/core/src/test/scala/Tests.scala):

![](assets/clickbait.png)
