from django.conf import settings
from django.http import HttpResponseRedirect
from django.http import HttpResponse

class LoginRequiredMiddleware(object):
    def process_request(self, request):
        if (not (request.META['REMOTE_ADDR'] == "127.0.0.1" or request.META['REMOTE_ADDR'] ==  "localhost" ) and not request.session.get('isAuthenticated', False)) and (request.path.find("login") == -1) and (request.path.find("maintenance") == -1) and (request.path.find("authenticate") == -1) and (request.path.find("/ems") == -1):
            if request.POST:
                return HttpResponseRedirect("/em_mgmt/login")
            else:
                return HttpResponseRedirect("/em_mgmt/login?forward=" + request.path)
            
