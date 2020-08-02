from django.shortcuts import render
from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.parsers import JSONParser
from rest_framework.response import Response
from queries.models import Query
from queries.serializers import QuerySerializer
from django.contrib.sites.shortcuts import get_current_site
from django.shortcuts import render, redirect
from django.utils.encoding import force_bytes
from django.utils.http import urlsafe_base64_encode
from django.template.loader import render_to_string
from django.contrib.auth.models import User
from queries.serializers import UserSerializer
from rest_framework import generics
from rest_framework import permissions
from queries.permissions import IsOwnerOrReadOnly
from django.contrib.auth import authenticate, login, logout
from rest_framework.authtoken.models import Token
from rest_framework.authentication import SessionAuthentication, BasicAuthentication
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
# Create your views here.
import json


@api_view(['GET'])
def api_root(request, format=None):
    return Response({
        'users': reverse('user-list', request=request, format=format),
        'queries': reverse('query-list', request=request, format=format)
    })


@csrf_exempt
def create_user(request, format=None):
    print("Non authorised")
    if request.method == 'POST':
        if request.body is not None:
            body_unicode = request.body.decode('utf-8')
            body = json.loads(body_unicode)
            username = body['username']
            email = body['email']
            password = body['password']
            password2 = body['password2']
            if password != password2:
                return HttpResponse('Passwords do not match', status=400)

            user = User.objects.create_user(username=username, email=email, password=password)
            # token = Token.objects.create(user=user)
            # print(token.key)
            user.save()
            return_data = {"username": username, "email": email, "id": user.id}  # , "token": token.key}
            json_return_data_dump = json.dumps(return_data)
            json_return_data = json.loads(json_return_data_dump)
            return JsonResponse(return_data)

        else:
            return JsonResponse('Invalid request', status=400)
    else:
        return JsonResponse('Invalid request', status=400)


@csrf_exempt
def logout_request(request):
    # if request.user.is_authenticated:

    logout(request)

    response = {"response": "Logged out"}
    headers = request.META
    print(str(headers))

    print(request, "Logged out successfully!")

    # request.user.auth_token.delete()
    return HttpResponse(response, status=200)


# else:
#     headers = request.META
#     print(str(headers))
#     print("failed logout")
#     failResponse = {"response": "Failed to log out"}
#     return HttpResponse(headers, status=400)

@csrf_exempt
def user_login(request):
    if request.method == 'POST':
        if request.body is not None:
            body_unicode = request.body.decode('utf-8')
            body = json.loads(body_unicode)
            username = body['username']
            password = body['password']
            user = authenticate(username=username, password=password)
            is_tokened = Token.objects.filter(user=user).exists()
            print("token = " + str(is_tokened))
            if user:
                if user.is_active:
                    if is_tokened:

                        token = Token.objects.get(user=user)

                        print(token)

                        string_token = (str(token))

                        login(request, user)
                        return_data = {"token": string_token, "id": user.id}
                        return JsonResponse(return_data)

                    elif not is_tokened:
                        print("creating new token not is tokened")
                        token = Token.objects.create(user=user)
                        print(token)
                        login(request, user)
                        return_data = {"token": token.key, "id": user.id}
                        return JsonResponse(return_data)
                else:
                    response = {"response": "your account was inactive"}
                    return JsonResponse(response, status=400)
            else:
                print("Someone tried to login and failed.")
                print("They used username: {} and password: {}".format(username, password))
                response = {"response": "invalid login details given"}
                return JsonResponse(response, status=400)
        else:
            response = {"response": "Invalid"}
            return JsonResponse(response, status=400)
    else:
        response = {"response": "Invalid"}
        return JsonResponse(response, status=400)

@api_view(['GET'])
def queryTest(request):
    queryset = Query.objects.all()
    #print(queryset)
    array = []
    output_dict = {}

    for query in queryset:
        languages = query.language
        dates = query.date_created
        owners = query.owner
        inittext = query.initialText
        translated = query.translatedText

        record = {"owner":str(query.owner), "date_created":str(dates), "language":languages,"initialText":inittext,"translatedText":translated}
        array.append(record)

    output_dict["listofqueries"] = array
    return JsonResponse(output_dict)

@api_view(['GET'])
def get_languages(request):
    queryset = Query.objects.all()
    #print(queryset)
    array = []
    array2 = []
    output_dict = {}

    for abc in queryset:
        languages = abc.language
        #languages = query.language
        record = {"language":languages}
        array.append(record)
        array2.append(languages)
    record2 = {}
    array3 = []
    array4 = []
    array4 = list(set(array2))
    print(array4)
    for abe in array4:
        record2 = {abe:array2.count(abe)}
        array3.append(record2)
    print(array3)
    output_dict["listoflangs"] = array3
    return JsonResponse(output_dict)




class QueryList(generics.ListCreateAPIView):
    queryset = Query.objects.all()
    serializer_class = QuerySerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)


class QueryDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Query.objects.all()
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]
    serializer_class = QuerySerializer


class UserList(generics.ListAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer


class UserDetail(generics.RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
