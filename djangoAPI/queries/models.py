from django.db import models
from pygments.lexers import get_all_lexers
from pygments.styles import get_all_styles
from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver
# Create your models here.


class Query(models.Model):
    date_created = models.DateTimeField(auto_now_add=True)
    owner = models.ForeignKey('auth.User', related_name='queries', on_delete=models.CASCADE, default="")
    initialText = models.TextField()
    language = models.TextField()
    translatedText = models.TextField()

    def save(self, *args, **kwargs):
        super(Query, self).save(*args, **kwargs)

    class Meta:
        ordering = ['date_created']
