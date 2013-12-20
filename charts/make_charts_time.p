set terminal png

set key off
set xlabel "F1"
set ylabel "Runtime (msec)"
set autoscale
set pointsize 2
set logscale y
set xrange [0:1.0]
set yrange [100:500000]
set title "Runtime - Best F1"
set output "charts/runtime-f1.png"
plot 'charts/runtime_f1.dat' with labels point pt 7 rotate by 53 left offset 0.4
