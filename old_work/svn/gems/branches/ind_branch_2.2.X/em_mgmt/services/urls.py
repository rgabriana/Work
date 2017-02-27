from django.conf.urls import patterns, include, url
from piston.resource import Resource
from services.handlers import UploadSizeHandler
from services.handlers import UpgradeHandler
from services.handlers import UpgradeLogsHandler
from services.handlers import UpgradeDeleteHandler
from services.handlers import BackupHandler
from services.handlers import RestoreHandler
from services.handlers import BackupRestoreLogsHandler
from services.handlers import BackupDeleteHandler
from services.handlers import ReleaseLockHandler

upload_size_handler = Resource(UploadSizeHandler)

upgrade_delete_handler = Resource(UpgradeDeleteHandler)
upgrade_handler = Resource(UpgradeHandler)
upgrade_log_handler = Resource(UpgradeLogsHandler)

backup_handler = Resource(BackupHandler)
backup_restore_log_handler = Resource(BackupRestoreLogsHandler)

backup_delete_handler = Resource(BackupDeleteHandler)
restore_handler = Resource(RestoreHandler)

release_lock_handler = Resource(ReleaseLockHandler)


urlpatterns = patterns('',

    url(r'^delete/upgrade/file/$', upgrade_delete_handler),
    url(r'^upload/size/$', upload_size_handler),
    url(r'^upgrade/$', upgrade_handler),
    url(r'^upgrade/logs/$', upgrade_log_handler),
    url(r'^backup/$', backup_handler),
    url(r'^backuprestore/logs/$', backup_restore_log_handler),
    url(r'^delete/backup/file/$', backup_delete_handler),
    url(r'^restore/$', restore_handler),
    url(r'^release/lock/$', release_lock_handler),
)
