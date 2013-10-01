libmao
======

numerical computation, modeling, and machine learning library with
math, statistics, randomization, and other useful utilities

## The highlights

- [Voting rule computation]
  (src/main/java/net/andrewmao/socialchoice/rules): Computation and
  error metrics for many common voting rules as well as those derived
  from more complicated noise models (see below). These were used in
  the AAAI 2013 paper *Better Human Computation Through Principled
  Voting*.

- [Ranking model estimation]
  (src/main/java/net/andrewmao/models/discretechoice): Efficient
  estimation techniques for models on rankings of items, known
  classically as *discrete choice models* and some more recently as
  *random utility models*. The implementation covers many common
  models such as Mallows, Bradley-Terry, Plackett-Luce, and
  Thurstone-Mosteller. In addition, there is a fast parallelized
  implementation of the Monte Carlo EM (MCEM) algorithm from the NIPS
  2012 paper *Random Utility Theory for Social Choice*.

- [HMM inference for repeated games]
  (src/main/java/net/andrewmao/models/games): Hidden Markov Model
  based inference for players' strategies in repeated games. We
  developed this as a technique to analyze experimental data.

## Other useful pieces

- [Random (weighted) subset and stream
  selection](src/main/java/net/andrewmao/math/RandomSelection.java): A
  class for shuffling and selecting random items from a Collection
  efficiently.

- [Truncated Normal and Multivariate Normal
  Distributions](src/main/java/net/andrewmao/probability): Apart from
  a few existing subroutines for normal distributions, two things you
  won't find elsewhere for Java are implementations of truncated
  Normal distributions and a thread-safe library for computing the CDF
  and expected first and second moments of a multivariate normal
  distribution. [Alan
  Genz](http://www.math.wsu.edu/faculty/genz/homepage) deserves credit
  for the actual subroutine, but this code makes it callable from Java
  and thread-safe.

## Interesting tidbits

- [LibraryReplicator]
  (src/main/java/net/andrewmao/misc/LibraryReplicator.java) was a
  class designed to make a non-reentrant native library reentrant. It
  does this by making *n* copies of the library, where *n* is the
  desired level of concurrency, and creating a proxied interface that
  shares the copies of the native library using a thread-safe
  queue. See http://stackoverflow.com/q/14553996/586086 for more
  information on why this was needed.
