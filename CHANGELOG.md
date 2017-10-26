# Changes in BAT-Framework

## 1.3.5 (2017-10-26) WAT2 support and bootstrap measures
- Added bootstrap statistics for micro/macro precision/recall/F1
- Added interface to WAT2 annotator
- Added Wikipedia interface through WAT2

## 1.3.4 (2017-06-15) New datasets and local Wikpedia interface.
- Added local Wikipedia interface (faster and does not depend on the online Wikipedia version).
- Fixed access to TagMe (now points to the SoBigData deployment by default). 
- Added NEEL 2016 #Microposts dataset.
- Updated GERDAQ references.
- Added utility classes to merge and divide datasets.

## 1.3.3 (2016-04-28) Fixes
- Fixed default DBPedia Spotlight URL (fixes Issue [\#6](https://github.com/marcocor/bat-framework/issues/6)).
- Added computation of variance and standard deviation on dataset-wise metrics.

## 1.3.1 (2016-02-24) Dataset factory
- Fixed dataset resource management. Dataset can now be created from other jars.

## 1.3 (2016-02-23) Dataset factory
- Datasets moved to resource folders, now accessible from other jars.
- Added dataset factory.

## 1.2 (2016-02-22) Dataset factory
- Added a few helper methods to compute metrics for single problems.
- Added three new dataset plugins: GERDAQ (dataset included), Yahoo Webscope L42 (dataset not included for copyright reasons), and ERD2014 (dataset included).

## 1.1 (2015-07-24) Fixed broken Wikipedia API
- Fix: Updated URL of Wikipedia API.

## 1.0 (2015-05-05) First stable mavenized release
- Mavenized project.
