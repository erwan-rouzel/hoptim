from django.conf.urls import url

from . import views

app_name = 'home'
urlpatterns = [
    # ex: /home/
    url(r'^$', views.hbase_region_servers, name='hbase_region_servers$'),
    url(r'^opentsdb/split$', views.opentsdb_split, name='opentsdb_split'),
    url(r'^opentsdb/regions/(?P<encoded_name>[a-f0-9]+)$', views.opentsdb_regions_infos, name='opentsdb_regions_infos$'),
    url(r'^opentsdb/regions$', views.opentsdb_regions, name='opentsdb_regions'),
    url(r'^opentsdb/metric$', views.opentsdb_metric, name='opentsdb_metric'),
    url(r'^hbase/region_servers$', views.hbase_region_servers, name='hbase_region_servers$'),
    url(r'^hbase/tables$', views.hbase_tables, name='hbase_tables$'),
    url(r'^hbase/tables/(?P<table_name>.+)$', views.hbase_tables_infos, name='hbase_tables_infos$'),
    url(r'^hbase/regions$', views.hbase_regions, name='hbase_regions$'),
    url(r'^hbase/regions/(?P<encoded_name>[a-f0-9]+)$', views.hbase_regions_infos, name='hbase_regions_infos$')
]
