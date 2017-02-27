from django.db import models

# Create your models here.
class UpgradeFile(models.Model):
    filename = models.CharField(max_length=30)
    filepath = models.CharField(max_length=200)
    creationDate = models.CharField(max_length=30)
    version = models.CharField(max_length=20)
    filesize = models.CharField(max_length=20)

class BackupFile(models.Model):
    filename = models.CharField(max_length=30)
    filepath = models.CharField(max_length=200)
    creationDate = models.CharField(max_length=30)
    filesize = models.CharField(max_length=20)
