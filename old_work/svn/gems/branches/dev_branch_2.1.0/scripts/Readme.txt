Basic %Mem and %CPU usage by EMS application over time
--------------------------------------------------------------------------------
monitor.sh <PID> <minutes to monitor>: 
Monitors the %CPU and %Mem of the given process and output in the format which can be used by gnuplot to plot a chart.

monitor.gnu:
This is the chart description rules used by gnuplot to plot the chart

usage.data:
monitor.sh writes to this files every minute. This is the file that is used by monitor.gnu and gnuplot as the data file


