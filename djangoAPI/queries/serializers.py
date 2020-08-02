from rest_framework import serializers
from queries.models import Query
from django.contrib.auth.models import User

class RegistrationSerializer:
    class Meta:
        fields = ['username', 'email', 'token']

class QuerySerializer(serializers.ModelSerializer):
    owner = serializers.ReadOnlyField(source='owner.username')
    class Meta:
        model = Query
        fields = ['id', 'owner', 'initialText', 'language', 'translatedText', 'date_created']


class UserSerializer(serializers.ModelSerializer):
    queries = QuerySerializer(many=True, read_only=True)
    class Meta:
        model = User
        fields = ['id', 'username', 'queries']
