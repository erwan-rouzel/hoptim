import mimetypes
from collections import OrderedDict
import json
import requests
from django.http import HttpResponse
from django.shortcuts import render
from .client import RestClient
from .forms import *
import datetime
import time

def index(request):
    return render(request, 'home/index.html')


class OptimizationAlgorithms(object):
    def __init__(self, request):
        self.algorithms_definitions = [
            {
                'full_name': 'com.dassault_systemes.infra.hoptim.smartbalancing.SizeBalancingAlgorithm',
                'action_id': 'optimize_size',
                'config_form': SizeBalancingConfig(request.POST),
                'is_repeatable': True,
                'is_split_algorithm': False,
                'display_graph': True
            },
            {
                'action_id': 'optimize_opentsdb_vip_split',
                'full_name': 'com.dassault_systemes.infra.hoptim.smartbalancing.OpenTSDBVipSplitAlgorithm',
                'config_form': OpenTSDBVipSplitConfig(request.POST),
                'is_repeatable': False,
                'is_split_algorithm': True,
                'display_graph': False
            },
            {
                'action_id': 'optimize_opentsdb_generic_split',
                'full_name': 'com.dassault_systemes.infra.hoptim.smartbalancing.OpenTSDBGenericSplitAlgorithm',
                'config_form': OpenTSDBGenericSplitConfig(request.POST),
                'is_repeatable': False,
                'is_split_algorithm': True,
                'display_graph': False
            },
            {
                'action_id': 'optimize_generic_split',
                'full_name': 'com.dassault_systemes.infra.hoptim.smartbalancing.GenericSplitAlgorithm',
                'config_form': GenericSplitConfig(request.POST),
                'is_repeatable': False,
                'is_split_algorithm': True,
                'display_graph': False
            },
            {
                'action_id': 'optimize_optaplanner',
                'full_name': 'com.dassault_systemes.infra.hoptim.smartbalancing.OptaPlannerBalancingAlgorithm',
                'config_form': OptaPlannerBalancingConfig(request.POST),
                'is_repeatable': True,
                'is_split_algorithm': False,
                'display_graph': True
            },
            {
                'action_id': 'optimize_restore_dump',
                'full_name': 'com.dassault_systemes.infra.hoptim.smartbalancing.RestoreDumpAlgorithm',
                'config_form': RestoreDumpConfig(request.POST),
                'is_repeatable': False,
                'is_split_algorithm': False,
                'display_graph': True
            }
        ]

    def get_by_id(self, id):
        for definition in self.algorithms_definitions:
            if definition['action_id'] == id:
                return definition


def hbase_region_servers(request):
    optimization_algorithms = OptimizationAlgorithms(request)
    json_tasks = RestClient.get_active_tasks()
    action_buttons = dict()
    json_status = dict()

    action_buttons = OrderedDict([
        ("optimize_opentsdb_vip_split", "OpenTSDB VIP Split"),
        ("optimize_opentsdb_generic_split", "OpenTSDB Generic Split"),
        ("optimize_generic_split", "Generic Split"),
        ("optimize_optaplanner", "Opta Planner Balancing"),
        ("optimize_size", "Size Balancing"),
        ("optimize_restore_dump", "Restore Dump")
    ])

    if request.method == 'POST' and not request.POST.get("cancel") and not request.POST.get("update_dump"):
        chosen_algorithm = {}

        if request.POST.get("optimize_size"):
            chosen_algorithm = optimization_algorithms.get_by_id("optimize_size")
        elif request.POST.get("optimize_opentsdb_vip_split"):
            chosen_algorithm = optimization_algorithms.get_by_id("optimize_opentsdb_vip_split")
        elif request.POST.get("optimize_opentsdb_generic_split"):
            chosen_algorithm = optimization_algorithms.get_by_id("optimize_opentsdb_generic_split")
        elif request.POST.get("optimize_generic_split"):
            chosen_algorithm = optimization_algorithms.get_by_id("optimize_generic_split")
        elif request.POST.get("optimize_optaplanner"):
            chosen_algorithm = optimization_algorithms.get_by_id("optimize_optaplanner")
        elif request.POST.get("optimize_restore_dump"):
            chosen_algorithm = optimization_algorithms.get_by_id("optimize_restore_dump")
        elif request.POST.get("preview"):
            action_buttons = OrderedDict([
                ("cancel", "Cancel"),
                ("execute", "Execute immediately"),
                ("execute_delayed", "Execute over SCHEDULER_DELAY settings")
            ])

            action_id = request.POST.get("option")
            chosen_algorithm = optimization_algorithms.get_by_id(action_id)

            if chosen_algorithm['is_repeatable']:
                action_buttons.update({"execute_delayed_repeat": "Execute over SCHEDULER_DELAY settings + Repeat"})
                action_buttons.move_to_end("execute_delayed_repeat", last=True)

            if chosen_algorithm['config_form'].is_valid():
                json_config = chosen_algorithm['config_form'].get_json_config()
                json_result = RestClient.get_regions_smart_balancing_plan(chosen_algorithm['full_name'], json.dumps(json_config))

                return render(request,
                              'home/hbase_region_servers_optimize_preview.html',
                              {'action_buttons': action_buttons,
                               'json': json.dumps(json_result),
                               'json_current': json_result['currentRegionBalancing'],
                               'json_new': json_result['newRegionBalancing'],
                               'score_gain': json_result['scoreGainPercentage'],
                               'action_id': action_id,
                               'display_graph': chosen_algorithm['display_graph'],
                               'full_name': chosen_algorithm['full_name'],
                               'is_repeatable': chosen_algorithm['is_repeatable'],
                               'json_config': json_config})

        if chosen_algorithm:
            json_split = ''
            if chosen_algorithm['is_split_algorithm']:
                json_split = RestClient.get_tsdb_presplit()

            action_buttons = OrderedDict([("cancel", "Cancel")])
            return render(request,
                          'home/hbase_region_servers_optimize_config.html',
                          {'json': json_split, 'action_buttons': action_buttons, 'action_id': chosen_algorithm['action_id'], 'form': chosen_algorithm['config_form']})

        if request.POST.get("execute"):
            algorithm = optimization_algorithms.get_by_id(request.POST.get("option"))
            json_config = request.POST.get("config")
            json_status = RestClient.get_regions_smart_balancing_execute(algorithm['full_name'], json_config)
            time.sleep(10)
        elif request.POST.get("execute_delayed"):
            algorithm = optimization_algorithms.get_by_id(request.POST.get("option"))
            json_config = request.POST.get("config")
            json_status = RestClient.get_regions_smart_balancing_execute_delayed(algorithm['full_name'], json_config)
        elif request.POST.get("execute_delayed_repeat"):
            algorithm = optimization_algorithms.get_by_id(request.POST.get("option"))
            json_config = request.POST.get("config")
            json_status = RestClient.get_regions_smart_balancing_execute_delayed_repeat(algorithm['full_name'], json_config)

    if request.POST.get("update_dump"):
        RestClient.get_dump_update()

    json_result = RestClient.get_hbase_region_servers()
    if request.GET.get('chart_height', None):
        request.session['chart_height'] = request.GET.get('chart_height', None)
    elif not request.session.get('chart_height'):
        request.session['chart_height'] = 70

    timestamp_seconds = int(RestClient.get_dump_timestamp())/1000

    if timestamp_seconds > 0:
        value = datetime.datetime.fromtimestamp(timestamp_seconds)
        latest_update = value.strftime('%Y-%m-%d %H:%M:%S')
    else:
        latest_update = ''

    return render(request,
                  'home/hbase_region_servers.html',
                  {'json': json_result, 'latest_update': latest_update, 'chart_height': request.session['chart_height'], 'json_tasks': json_tasks, 'json_status': json_status, 'action_buttons': action_buttons})


def hbase_tables(request):
    json_tasks = RestClient.get_active_tasks()
    json_result = RestClient.get_hbase_tables()
    return render(request, 'home/hbase_tables.html', {'json': json_result, 'json_tasks': json_tasks})


def hbase_regions(request):
    json_tasks = RestClient.get_active_tasks()
    json_result = RestClient.get_hbase_regions()
    return render(request, 'home/hbase_regions.html', {'json': json_result, 'json_tasks': json_tasks})


def hbase_regions_infos(request, encoded_name):
    json_tasks = RestClient.get_active_tasks()

    # if this is a POST request we need to process the form data
    if request.method == 'POST':
        # Call Split region:
        RestClient.get_split_region(encoded_name)

    json_result = RestClient.get_hbase_regions_infos(encoded_name)
    action_buttons = OrderedDict([
        ("split_on_region", "Split this region")
    ])
    action_url1 = encoded_name
    return render(request, 'home/hbase_regions_infos.html', {'json': json_result, 'encoded_name': encoded_name, 'json_tasks': json_tasks, 'action_buttons': action_buttons, 'action_url': action_url1,})


def hbase_tables_infos(request, table_name):
    json_tasks = RestClient.get_active_tasks()
    json_result = RestClient.get_hbase_tables_infos(table_name)

    if request.GET.get('chart_height', None):
        request.session['chart_height'] = request.GET.get('chart_height', None)
    elif not request.session.get('chart_height'):
        request.session['chart_height'] = 70

    return render(request, 'home/hbase_tables_infos.html', {'json': json_result, 'table_name': table_name, 'chart_height': request.session['chart_height']})


def opentsdb_metric(request):
    json_tasks = RestClient.get_active_tasks()

    # if this is a POST request we need to process the form data
    if request.method == 'POST':
        # create a form instance and populate it with data from the request:
        form = MetricForm(request.POST, init_value="")
        # check whether it's valid:
        if form.is_valid():
            metric_name = form.cleaned_data['metric_name']
            form = MetricForm(init_value=metric_name)

            if request.POST.get("get_tags"):
                json_result = RestClient.get_metric(metric_name)
            elif request.POST.get("get_region_servers"):
                json_result = RestClient.get_metric_region_servers(metric_name)

            return render(request, 'home/opentsdb_metric.html',
                          {'form': form, 'json': json_result, 'json_tasks': json_tasks})

    # if a GET (or any other method) we'll create a blank form
    else:
        form = MetricForm(init_value="")
        return render(request, 'home/opentsdb_metric.html', {'form': form, 'json': '', 'json_tasks': json_tasks})


def opentsdb_regions(request):
    json_tasks = RestClient.get_active_tasks()
    json_result = RestClient.get_tsdb_regions()
    return render(request, 'home/opentsdb_regions.html', {'json': json_result, 'json_tasks': json_tasks})


def opentsdb_split(request):
    json_tasks = RestClient.get_active_tasks()
    json_result = RestClient.get_tsdb_presplit()

    action_buttons = OrderedDict([
        ("download_presplit_file", "Download Presplit File")
    ])

    if request.method == 'POST':
        if request.POST.get("download_presplit_file"):
            presplit_file = RestClient.get_tsdb_presplit_file()
            response = HttpResponse(presplit_file['content'])
            response['Content-Type'] = 'application/json'
            response['Content-Length'] = len(presplit_file['content'])
            response['Content-Encoding'] = 'Charset: utf-8'
            filename_header = 'filename=presplit_file.txt'
            response['Content-Disposition'] = 'attachment; ' + filename_header
            return response
    else:
        return render(request, 'home/opentsdb_split.html',
                      {'json': json_result, 'json_tasks': json_tasks, 'action_buttons': action_buttons})


def opentsdb_regions_infos(request, encoded_name):
    json_tasks = RestClient.get_active_tasks()
    json_result = RestClient.get_tsdb_regions_infos(encoded_name)
    return render(request, 'home/opentsdb_regions_infos.html', {'json': json_result, 'encoded_name': encoded_name, 'json_tasks': json_tasks})
