#!/bin/sh
awk -F"," -v zone="ZoneName" -v startDay="YYYY-MM-DD" -v endDay="YYYY-MM-DD" -v startTime="HH:MM:00" -v endTime="HH:MM:00" -f parsefiles.awk Zone.csv
