set title "EMS Performance"
set xlabel "Time"
set ylabel "Percent"
set term gif
set output "usage.gif"
set data style lp
plot "usage.data" using 1:2 t "CPU", "usage.data" using 1:3 t "Memory"
