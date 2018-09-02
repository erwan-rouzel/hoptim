import requests
import json
import urllib
from django.conf import settings

api_url = settings.REST_API_URL


def api_get(path):
    print(api_url + path)
    return json.loads(requests.get(api_url + path).json())


def api_post(path, arg='', json_arg=''):
    headers = {'Content-Type': 'application/json'}
    if arg == '':
        return json.loads(requests.post(api_url + path, json=json_arg, headers=headers).json())
    else:
        return json.loads(requests.post(api_url + path + "/" + arg, json=json_arg, headers=headers).json())


class RestClient:
    @staticmethod
    def get_tables_list():
        return api_get('tables/list')

    @staticmethod
    def get_metric(metric_name):
        return api_get('metric/' + metric_name)

    @staticmethod
    def get_metric_region_servers(metric_name):
        return api_get('metric/' + metric_name + '/region_servers')

    @staticmethod
    def get_regions_smart_balancing_plan(algorithm_class_name, json_config):
        return api_get('region_servers/smart_balancing/plan/' + algorithm_class_name + '/' + urllib.parse.quote_plus(json_config))

    @staticmethod
    def get_regions_smart_balancing_execute(algorithm_class_name, json_config):
        return api_get('region_servers/smart_balancing/execute/' + algorithm_class_name + '/' + urllib.parse.quote_plus(json_config))

    @staticmethod
    def get_regions_smart_balancing_execute_delayed(algorithm_class_name, json_config):
        return api_get('region_servers/smart_balancing/execute_delayed/' + algorithm_class_name + '/' + urllib.parse.quote_plus(json_config))

    @staticmethod
    def get_regions_smart_balancing_execute_delayed_repeat(algorithm_class_name, json_config):
        return api_get('region_servers/smart_balancing/execute_delayed_repeat/' + algorithm_class_name + '/' + urllib.parse.quote_plus(json_config))

    @staticmethod
    def get_hbase_region_servers():
        return api_get('region_servers')

    @staticmethod
    def get_hbase_tables():
        return api_get('tables')

    @staticmethod
    def get_hbase_tables_infos(table_name):
        return api_get('tables/infos/' + table_name)

    @staticmethod
    def get_hbase_regions():
        return api_get('regions')

    @staticmethod
    def get_hbase_regions_infos(encoded_name):
        return api_get('regions/infos/' + encoded_name)

    @staticmethod
    def get_split_region(encoded_name):
        return api_get('region/split/' + encoded_name)

    @staticmethod
    def get_tsdb_stats():
        return api_get('stats')

    @staticmethod
    def get_tsdb_regions():
        return api_get('regions/tsdb/views')

    @staticmethod
    def get_tsdb_regions_infos(encoded_name):
        return api_get('regions/tsdb/infos/' + encoded_name)

    @staticmethod
    def get_tsdb_presplit():
        return api_get('regions/tsdb/presplit')

    @staticmethod
    def get_tsdb_presplit_file():
        return api_get('regions/tsdb/presplit/file')

    @staticmethod
    def get_active_tasks():
        return api_get('tasks/active')

    @staticmethod
    def get_dump_update():
        return api_get('dump/update')

    @staticmethod
    def get_dump_timestamp():
        return api_get('dump/timestamp')

    @staticmethod
    def get_info():
        return api_get('info')
