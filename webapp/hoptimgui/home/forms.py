import floppyforms as forms
from .client import RestClient


class MetricForm(forms.Form):
    def __init__(self, *args, **kwargs):
        super(MetricForm, self).__init__(*args)
        self.fields["metric_name"].initial = kwargs.pop('init_value')

    metric_name = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control'}), label='Your metric', max_length=100)


class NumberSlider(forms.RangeInput):
    step = 1
    template_name = 'snippets/slider.html'


class SizeBalancingConfig(forms.Form):
    has_config = False
    title = "Size Balancing Configuration"
    panel_info_template = "snippets/panel_info_size_balancing.html"

    def get_json_config(self):
        json_config = dict()
        return json_config


class GenericSplitConfig(forms.Form):
    def __init__(self, *args, **kwargs):
        super(GenericSplitConfig, self).__init__(*args)
        json_tables = RestClient.get_hbase_tables()
        self.fields["tables_selection"].choices = [[table['tableName'], table['tableName']] for table in json_tables]

    has_config = True
    title = "Generic Split Configuration"
    panel_info_template = "snippets/panel_info_generic_split.html"
    number_of_splits = forms.IntegerField(widget=NumberSlider, label='Number of splits', required=False, max_value=30)

    tables_selection = forms.MultipleChoiceField(
        widget=forms.SelectMultiple(attrs={'size': '10'}),
        label='Tables selection',
        required=False
    )

    def get_json_config(self):
        json_config = dict()
        json_config['number_of_splits'] = self.cleaned_data['number_of_splits']
        json_config['tables_selection'] = self.cleaned_data['tables_selection']
        return json_config


class OptaPlannerBalancingConfig(forms.Form):
    def __init__(self, *args, **kwargs):
        super(OptaPlannerBalancingConfig, self).__init__(*args)
        json_tables = RestClient.get_hbase_tables()
        self.fields["tables_selection"].choices = [[table['tableName'], table['tableName']] for table in json_tables]

    has_config = True
    title = "Opta Planner Balancing Configuration"
    panel_info_template = "snippets/panel_info_optaplanner_balancing.html"
    timeout = forms.IntegerField(widget=NumberSlider, label='Time in seconds to explore the space of solutions', required=False, max_value=240)
    move_max = forms.IntegerField(widget=NumberSlider, label='Maximum number of moves (hard constraint)', required=False, max_value=240)
    move_weight = forms.IntegerField(widget=NumberSlider, label='Move weight (soft constraint)', required=False, max_value=100)
    size_weight = forms.IntegerField(widget=NumberSlider, label='Size weight (soft constraint)', required=False, max_value=100)
    read_weight = forms.IntegerField(widget=NumberSlider, label='Read requests weight (soft constraint)', required=False, max_value=100)
    write_weight = forms.IntegerField(widget=NumberSlider, label='Write requests weight (soft constraint)', required=False, max_value=100)

    tables_selection = forms.MultipleChoiceField(
        widget=forms.SelectMultiple(attrs={'size': '10'}),
        label='Tables selection',
        required=True
    )

    def get_json_config(self):
        if len(self.cleaned_data['tables_selection']) == 0:
            raise forms.ValidationError("You must select at least one table!")

        json_config = dict()
        json_config['timeout'] = self.cleaned_data['timeout']
        json_config['move_max'] = self.cleaned_data['move_max']
        json_config['move_weight'] = self.cleaned_data['move_weight']
        json_config['size_weight'] = self.cleaned_data['size_weight']
        json_config['read_weight'] = self.cleaned_data['read_weight']
        json_config['write_weight'] = self.cleaned_data['write_weight']
        json_config['tables_selection'] = self.cleaned_data['tables_selection']
        return json_config


class OpenTSDBGenericSplitConfig(forms.Form):
    has_config = True
    title = "OpenTSDB Generic Split Configuration"
    panel_info_template = "snippets/panel_info_opentsdb_generic_split.html"
    number_of_splits = forms.IntegerField(widget=NumberSlider, label='Number of splits', required=False, max_value=30)

    def get_json_config(self):
        json_config = dict()
        json_config['number_of_splits'] = self.cleaned_data['number_of_splits']
        return json_config


class OpenTSDBVipSplitConfig(forms.Form):
    has_config = False
    title = "OpenTSDB Vip Split Configuration"
    panel_info_template = "snippets/panel_info_opentsdb_vip_split.html"

    def get_json_config(self):
        json_config = dict()
        return json_config


class RestoreDumpConfig(forms.Form):
    has_config = False
    title = "Restore Dump Configuration"
    panel_info_template = "snippets/panel_info_restore_dump.html"

    def get_json_config(self):
        json_config = dict()
        return json_config

