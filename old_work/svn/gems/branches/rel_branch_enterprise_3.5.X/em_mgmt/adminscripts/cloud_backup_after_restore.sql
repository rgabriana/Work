select update_all_sequences();

select addTriggers();

select add_triggers();

update cloud_config set val = (select extract(epoch from date_trunc('milliseconds', now())) * 1000) where name = 'successful.sync.time';

update cloud_config set val = '0' where name = 'remigration.required';
