def get_revision():
    import os
    from django.conf import settings	

    try:
        filepath = os.path.join(os.path.dirname(__file__), "svninfo.txt")
        f = open(filepath , "r")          
        svnRevisionString = f.read()
        f.close()
        strings = svnRevisionString.split( )
        completeVersionString = settings.VERSION_STR + '.' +strings[1]
        return completeVersionString
    except:
        return 'Development'

REVISION = get_revision()