from django.urls import path
from queries import views
from rest_framework.urlpatterns import format_suffix_patterns
urlpatterns = [
    path('queries/', views.QueryList.as_view()),
    path('queryTest/', views.queryTest),
    path('languages/', views.get_languages),
    path('queries/<int:pk>/', views.QueryDetail.as_view()),
    path('users/', views.UserList.as_view()),
    path('users/<int:pk>/', views.UserDetail.as_view()),
    path('register/', views.create_user, name='create_user'),
    path('logout/', views.logout_request, name='logout_request'),
    path('login/', views.user_login, name='user_login')
]

urlpatterns = format_suffix_patterns(urlpatterns)
