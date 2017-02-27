#!/bin/bash

psql -U postgres ems -c "delete from dr_target where dr_type = 'OADR'";
psql -U postgres adr -c 'delete from dr_event_signal_interval';
psql -U postgres adr -c 'delete from dr_event_signal';
psql -U postgres adr -c 'delete from dr_event';
