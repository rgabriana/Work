from django import template
from django.template.base import Node
from management import util
from management.templatetags import REVISION
from datetime import datetime

register = template.Library()

@register.simple_tag
def revision():
    return REVISION

@register.simple_tag    
def currentdatetime():
    now = datetime.now()
    return now.strftime("%Y-%m-%d %H:%M")    

@register.tag(name="translate")
def translate(parser, token):
    try:
        tokens = token.split_contents()
        name = tokens[1].strip('"').strip("'")
        args = dict(pairwise(tokens))
    except ValueError:
        raise template.TemplateSyntaxError("%r tag requires a single argument" % token.contents.split()[0])
    return TranslateNode(args, name)

class TranslateNode(Node):
    def __init__(self, args, name):
        self.args = args
        self.name = name

    def render(self, context):
        return util.translate(self.name, self.args)


def pairwise(iterable):
    itnext = iter(iterable).next
    itnext()
    itnext()
    while 1:
        yield itnext().strip('"').strip("'"), itnext().strip('"').strip("'")

