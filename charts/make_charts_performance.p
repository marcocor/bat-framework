#set format xy "$%g$"
set terminal png
set key right bottom
set xlabel "Threshold"
set ylabel "Micro-Precision"
set autoscale
set xrange [0:1.0]
set yrange [0:1.0]
set title "Precision DATASET - METRICS"
set output "charts/METRICS-precision-DATASET.png"
plot "charts/METRICS_precision_threshold_aidapriorityonly_DATASET.dat" with lines t "AIDA - PriorityOnly","charts/METRICS_precision_threshold_aidalocal_DATASET.dat" with lines t "AIDA - Local","charts/METRICS_precision_threshold_aidacocktailparty_DATASET.dat" with lines t "AIDA - CocktailParty", "charts/METRICS_precision_threshold_wikipediaminer_DATASET.dat" with lines t "Wikipedia Miner", "charts/METRICS_precision_threshold_illinoiswikifier_DATASET.dat" with lines t "Illinois Wikifier", "charts/METRICS_precision_threshold_tagme2_DATASET.dat" with lines t "TagMe 2", "charts/METRICS_precision_threshold_dbpediaspotlight_DATASET.dat" with lines t "DBPedia Spotlight"

set key default
set ylabel "Micro-Recall"
set title "Recall DATASET - METRICS"
set output "charts/METRICS-recall-DATASET.png"
plot "charts/METRICS_recall_threshold_aidapriorityonly_DATASET.dat" with lines t "AIDA - PriorityOnly","charts/METRICS_recall_threshold_aidalocal_DATASET.dat" with lines t "AIDA - Local","charts/METRICS_recall_threshold_aidacocktailparty_DATASET.dat" with lines t "AIDA - CocktailParty", "charts/METRICS_recall_threshold_wikipediaminer_DATASET.dat" with lines t "Wikipedia Miner", "charts/METRICS_recall_threshold_illinoiswikifier_DATASET.dat" with lines t "Illinois Wikifier", "charts/METRICS_recall_threshold_tagme2_DATASET.dat" with lines t "TagMe 2", "charts/METRICS_recall_threshold_dbpediaspotlight_DATASET.dat" with lines t "DBPedia Spotlight"


set ylabel "Micro-F1"
set title "F1 DATASET - METRICS"
set output "charts/METRICS-f1-DATASET.png"
plot "charts/METRICS_f1_threshold_aidapriorityonly_DATASET.dat" with lines t "AIDA - PriorityOnly","charts/METRICS_f1_threshold_aidalocal_DATASET.dat" with lines t "AIDA - Local","charts/METRICS_f1_threshold_aidacocktailparty_DATASET.dat" with lines t "AIDA - CocktailParty", "charts/METRICS_f1_threshold_wikipediaminer_DATASET.dat" with lines t "Wikipedia Miner", "charts/METRICS_f1_threshold_illinoiswikifier_DATASET.dat" with lines t "Illinois Wikifier",  "charts/METRICS_f1_threshold_tagme2_DATASET.dat" with lines t "TagMe 2",  "charts/METRICS_f1_threshold_dbpediaspotlight_DATASET.dat" with lines t "DBPedia Spotlight"

