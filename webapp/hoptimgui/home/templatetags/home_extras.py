import urllib

from .lib.jsonconv import json2html
from django import template
from django.core.serializers import serialize
from django.db.models.query import QuerySet
from django.utils.safestring import mark_safe
from django.conf import settings
from ..client import RestClient
import json

register = template.Library()


@register.filter(is_safe=True)
def json2table(value, arg):
    if value == '':
        return ''

    else:
        return json2html.convert(json=value, table_attributes=arg)


@register.filter(is_safe=True)
def jsonify(value):
    if isinstance(value, QuerySet):
        return mark_safe(serialize('json', value))
    return mark_safe(json.dumps(value))


@register.simple_tag
@register.filter(is_safe=True)
def get_settings(arg):
    return getattr(settings, arg)


@register.simple_tag
@register.filter(is_safe=True)
def url_quote_plus(arg):
    return urllib.parse.quote_plus(str(arg))


@register.simple_tag
@register.filter(is_safe=True)
def get_version():
    return RestClient.get_info()['version']
