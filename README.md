bat-framework
=============

A framework to compare entity annotation systems.

### Introduction
As a contribute to the scientific community working on the field of entity annotation, we developed a framework to compare text annotators: systems that, given a text document, aim at finding the entities the text is about, identified as Wikipedia pages. The BAT-Framework comes along with a formal framework that defines a set of problems, the way systems can be compared to each other, and a set of measures that – extending classic IR measures – fairly and fully compares entity annotators features. The formal framework, whose understanding is required to use the benchmark framework, is presented in [this paper](http://research.google.com/pubs/pub40749.html) published at WWW’13.

### Main features
* Compare in a fair and complete way any Entity-Annotation system.
* Provides an implementation for all defined measures and match relations.
* Easily extensible adding new systems, new datasets, new similarity measures.
* Performs extensive testing on any Entity Annotator and any dataset.
* Performs runtime testing.
* Generates gnuplot charts and Latex tables summarizing test results.
* Completely open source, distributed under the GPLv3 license.

### How to include
The BAT-framework is mavenized. You can include it with:
```
<dependency>
    <groupId>it.unipi.di.acube</groupId>
    <artifactId>bat-framework</artifactId>
    <version>1.3.3</version>
</dependency>
```

### How to use
You can either use BAT-framework directly (see [this](http://acube.di.unipi.it/wp-content/uploads/2013/01/BAT-Framework-0.1-reference.pdf) guide) or by using [GERBIL](https://github.com/AKSW/gerbil), which provides an easy web interface and is built on top of the BAT-Framework.

### Changelog
To keep track of the changes in recent versions of the BAT-Framework, see the [CHANGELOG](CHANGELOG.md).

### Contributing
You can contribute by either opening an [issue](https://github.com/marcocor/bat-framework/issues) or by forking the project, implement the feature in a separate branch, and creating a pull request.
