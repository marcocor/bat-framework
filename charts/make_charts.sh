#!/bin/bash
sed s/"METRICS"/"weakannotationmatch"/g make_charts.p | sed s/"DATASET"/"aidaconll"/g | gnuplot
#sed s/"METRICS"/"weakannotationmatch"/g make_charts.p | sed s/"DATASET"/"msnbc"/g | gnuplot
#sed s/"METRICS"/"weakannotationmatch"/g make_charts.p | sed s/"DATASET"/"ace2004"/g | gnuplot
#sed s/"METRICS"/"weakannotationmatch"/g make_charts.p | sed s/"DATASET"/"aquaint"/g | gnuplot
#sed s/"METRICS"/"weakannotationmatch"/g make_charts.p | sed s/"DATASET"/"iitb"/g | gnuplot

sed s/"METRICS"/"strongannotationmatch"/g make_charts.p | sed s/"DATASET"/"aidaconll"/g | gnuplot
#sed s/"METRICS"/"strongannotationmatch"/g make_charts.p | sed s/"DATASET"/"msnbc"/g | gnuplot
#sed s/"METRICS"/"strongannotationmatch"/g make_charts.p | sed s/"DATASET"/"ace2004"/g | gnuplot
#sed s/"METRICS"/"strongannotationmatch"/g make_charts.p | sed s/"DATASET"/"aquaint"/g | gnuplot
#sed s/"METRICS"/"strongannotationmatch"/g make_charts.p | sed s/"DATASET"/"iitb"/g | gnuplot

cat make_charts_time.p | gnuplot
