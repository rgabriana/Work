from django.conf.urls import patterns, include, url
# Uncomment the next two lines to enable the admin:
# from django.contrib import admin
# admin.autodiscover()

urlpatterns = patterns('management.views',

    url(r'^em_mgmt/$', 'login'),
    url(r'^em_mgmt/login/$', 'login'),
    url(r'^em_mgmt/maintenance/$', 'maintenance'),
    url(r'^em_mgmt/upgrade/$', 'upgrade'),
    url(r'^em_mgmt/authenticate/$', 'authenticate'),
    url(r'^em_mgmt/upload/upgrade/file/$', 'uploadUpgradeFile'),
    url(r'^em_mgmt/upload/backup/file/$', 'uploadBackupFile'),
    url(r'^em_mgmt/services/', include('services.urls')),
    url(r'^em_mgmt/home/', 'home'),
    url(r'^em_mgmt/backuprestore/', 'backuprestore'),
    url(r'^em_mgmt/backup/download/', 'download_file'),
    url(r'^em_mgmt/history/', 'history'),
    url(r'^em_mgmt/cloudsettings/$', 'cloudsettings'),
    url(r'^em_mgmt/cloudsettings/save/$', 'saveCloudsettings'),
    url(r'^em_mgmt/cloudsettings/upload/file/$', 'uploadCertificateFile'),
    url(r'^em_mgmt/logs/download/', 'logs_download'),

)

