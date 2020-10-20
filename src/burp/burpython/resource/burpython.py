import sys
import base64
import re
import json
import requests

def isVersion3():
    '''judge if python major version is 3'''
    if sys.version_info.major == 3:
        return True
    return False
def b64Encode(data):
    '''For base64 encode.The param data must be str.'''
    if isVersion3():
        return base64.b64encode(data.encode()).decode()
    else:
        return base64.b64encode(data)
def b64Decode(data):
    '''For base64 decode.The param data must be str.'''
    if isVersion3():
        return base64.b64decode(data.encode()).decode()
    else:
        return base64.b64decode(data)

def println(string):
    '''print the value of param "string in extender's stdin"'''
    sys.stdout.write("debug "+b64Encode(str(string))+'\n')

def raw_response(response):
    '''parse response from requests package to text'''
    line = b"{} {} {}\r\n".format("HTTP/1.1", response.status_code, response.reason)
    headers = b''
    for k, v in response.headers.items():
        if k == "Set-Cookie":
            for v in response.raw.headers.getlist("Set-Cookie"):
                headers += b"{}: {}\r\n".format("Set-Cookie", v)
        else:
            headers += b"{}: {}\r\n".format(k, v)
    body = response.content
    return "{}{}\r\n{}".format(line, headers, body)

class HTTPResponse(object):
    '''For parsing HTTP/HTTPS response'''
    def __init__(self, response_text=None):
        self.response_raw = response_text
        self.response_line = {"protocol":"HTTP", "major":1, "minor":1, "code": 200, "message": "OK"}
        self.headers = {"Content-Type":"text/plain"}
        self.body_raw = "burpython is ok!"
        if self.response_raw is None:
            self.build()
        else:
            self.parse()

    def parse(self):
        re_response_line = re.match(r"(HTTP|HTTPS)/(\d+)\.(\d+) (\d+) ([\S ]*)\r\n", self.response_raw)
        self.response_line.update({
            "protocol":re_response_line.group(1), 
            "major":re_response_line.group(2), 
            "minor":re_response_line.group(3), 
            "code": re_response_line.group(4), 
            "message": re_response_line.group(5)
        })
        re_headers = re.findall(r"([^:\s]+?): (.*?)\r\n", self.response_raw)
        self.headers = {}
        for header in re_headers:
            if header[0] == "Set-Cookie":
                if self.headers.get("Set-Cookie") is None:
                    self.headers["Set-Cookie"] = [header[1]]
                else:
                    self.headers["Set-Cookie"].append(header[1])
            else:
                self.headers[header[0]] = header[1]
        self.body_raw = re.search(r"\r\n\r\n(.*)", self.response_raw, re.S).group(1)
    
    def build(self):
        self.response_raw = ''
        self.response_raw += "{}/{}.{} {} {}\r\n".format(
            self.response_line.get("protocol"),
            self.response_line.get("major"),
            self.response_line.get("minor"),
            self.response_line.get("code"),
            self.response_line.get("message")
        )
        for k, v in self.headers.items():
            if k != "Set-Cookie":
                self.response_raw += "{}: {}\r\n".format(k, v)
            else:
                for vv in v:
                    self.response_raw += "{}: {}\r\n".format(k, vv)
        self.response_raw += "\r\n" + self.body_raw
        return self.response_raw

        

class HTTPRequest(object):
    '''For parsing HTTP/HTTPS request.'''
    def __init__(self, request_text=None):
        self.request_raw = request_text
        self.request_line = {"method":"GET", "path":"/", "protocol":"HTTP", "major":1, "minor":1}
        self.headers = {"Host":"127.0.0.1"}
        self.cookies = {}
        self.body_raw = ""
        self.body_json = {}
        self.is_json = False # body type
        self.request_param = {}
        if self.request_raw is None:
            self.build()
        else:
            self.parse()

    def get_url(self, https=False):
        protocol = 'https' if https else 'http'
        return "{}://{}{}".format(protocol, self.headers.get("Host"), self.request_line.get('path'))

    def set_path(self, path):
        self.__request_line.update({"path":path})
        self.__build_param(path)

    def __build_param(self, path):
        params = re.search(r"/\S*?\?(.*)", path)
        self.request_param = {}
        if params is not None:
            re_request_param = re.findall(r"([\w_\-\[\]]+)=([^&]*)&?", params.group(1))
            if re_request_param is not None:
                self.request_param = {}
                for p in re_request_param:
                    self.request_param[p[0]] = p[1]
    

    def build(self):
        '''You can modify the request like this:
                self.cookie['JSESSIONID'] = '12345'
            After that,you can call this function to build a new request which has '12345' cookies'''
        self.request_raw = ''
        path = self.request_line.get("path")
        path = path[:len(path) if path.find('?') == -1 else path.find('?')+1]
        for k, v in self.request_param.items():
            path += "{}={}&".format(k, v)
        path = path.rstrip('&')
        self.request_line.update({"path":path})
        self.request_raw += "{} {} {}/{}.{}\r\n".format(self.request_line.get("method"),
                                                self.request_line.get("path"),
                                                self.request_line.get("protocol"),
                                                self.request_line.get("major"),
                                                self.request_line.get("minor"))
        headers = ''
        for h in self.headers.keys():
            if h == "Cookie":
                cookie = ''
                for c in self.cookies.keys():
                    cookie += "{}={}; ".format(c, self.cookies.get(c))
                headers += "Cookie: "+cookie.rstrip("; ")+"\r\n"
            else:
                headers += "{}: {}\r\n".format(h, self.headers.get(h))
        if "Cookie" not in self.headers.keys() and len(self.cookies) > 0:
            cookie = ''
            for c in self.cookies.keys():
                cookie += "{}={}; ".format(c, self.cookies.get(c))
            headers += "Cookie: "+cookie.rstrip("; ")+"\r\n"
        self.request_raw += headers
        self.request_raw += "\r\n"
        self.body_raw = ''
        rawbody = self.body_json.pop("&raw")
        if self.is_json:
            self.body_raw = json.dumps(self.body_json)
        elif len(self.body_json)>0:
            for k in self.body_json:
                self.body_raw += "{}={}&".format(k,self.body_json.get(k))
            self.body_raw = self.body_raw.rstrip("&")
        else:
            self.body_raw = rawbody
        self.request_raw += self.body_raw
        return self.request_raw

    def parse(self):
        '''Parsing request'''
        re_request_line = re.match(r"(GET|POST|HEAD|OPTIONS|MOVE|PUT|TRACE|DELETE) (/\S*) (HTTP|HTTPS)/(\d+)\.(\d+)\r\n",self.request_raw)
        re_headers = re.findall(r"([^:\s]+?): (.*?)\r\n", self.request_raw)
        if len(re_headers) == 0:
            re_headers = None
        self.body_raw = re.search(r"\r\n\r\n(.*)", self.request_raw, re.S).group(1)

        if None in (re_request_line, re_headers, self.body_raw):
            return False
        self.request_line.update({"method":re_request_line.group(1),
            "path":re_request_line.group(2),
            "protocol":re_request_line.group(3),
            "major":re_request_line.group(4),
            "minor":re_request_line.group(5)
        })

        self.__build_param(self.request_line.get("path"))

        self.headers = {}
        for h in re_headers:
            self.headers[h[0]] = h[1]

        # parse cookies
        if self.headers.get("Cookie") is None:
            self.cookies = {}
        else:
            for c in self.headers.get("Cookie").split("; "):
                p = c.split("=")
                self.cookies[p[0]] = p[1]
        
        try:
            self.body_json = json.loads(self.body_raw)
            self.is_json = True
        except Exception:
            re_body_json = re.findall(r"([\w_\-\[\]]+)=([^&]*)&?", self.body_raw)
            if len(re_body_json) == 0 and len(self.body_raw) != 0:
                pass
            else:
                self.body_json = {}
                for p in re_body_json:
                    self.body_json[p[0]] = p[1]
        finally:
            self.body_json["&raw"] = self.body_raw
        return True

class Action(object):
    '''This class is base of exchaging data between scripts with extender'''
    ACTION_MAP = {
        "close":"Close",
        "update_request":"UpdateRequest",
        "selection_text":"SelectionText",
        "proxy_handler":"Proxy",
        "util":"Util"
    }

    def __init__(self, action, **kw):
        self.__action = action
        self._keywords = kw

    def _send_once(self):
        '''Send once data endswith \n like writeline'''
        if self.__action not in Action.ACTION_MAP.values():
            return False
        params = ''
        for k in self._keywords.keys():
            params += '%s:%s&'%(k, b64Encode(self._keywords[k]))
        params = params.rstrip('&')
        sys.stdout.write(b64Encode(self.__action+" "+params)+"\n")
        sys.stdout.flush()
        return True

    def update(self, **kw):
        '''Update self._keywords from keyword.'''
        for k in kw.keys():
            kw.update({k:str(kw.get(k))})
        self._keywords.update(kw)
    def set(self, **kw):
        '''Set the self._keywords'''
        for k in kw.keys():
            kw.update({k:str(kw.get(k))})
        self._keywords = kw

    def _recv_once(self):
        '''Recv once data endswith \n like readline'''
        result = {}
        try:
            text = sys.stdin.readline().rstrip("\n")
            l = text.split("&")
            for p in l:
                pl = p.split(":")
                result[pl[0]] = b64Decode(pl[1])
        except Exception:
            pass
        return result

    def call_action(self, action_name, **kw):
        '''call a function from burpython quickly'''
        self._keywords[action_name] = "1"
        self.update(**kw)
        self._send_once()
        return self._recv_once()
    
class Util(Action):
    '''Provide some input/output for script.'''
    def __init__(self,**kw):
        super(Util,self).__init__(Action.ACTION_MAP["util"],**kw)
    def get_from_ui_input(self, tips):
        return self.call_action("get_from_ui_input", tips=tips).get("get_from_ui_input")
    def set_to_mouse_pointer(self, result):
        return self.call_action("set_to_mouse_pointer", result=result)
    def set_to_ui_textarea(self, result, tips):
        return self.call_action("set_to_ui_textarea", tips=tips, result=result)

util = Util()

class Close(Action):
    '''Close the connection.'''
    def __init__(self,**kw):
        super(Close,self).__init__(Action.ACTION_MAP["close"],**kw)
    def close(self):
        '''Close the connection.'''
        self._keywords.clear()
        self._keywords.update({"msg":"closed"})
        self._send_once()

class UpdateRequest(Action):
    '''Update request in message editor'''
    def __init__(self,**kw):
        super(UpdateRequest,self).__init__(Action.ACTION_MAP["update_request"],**kw)
    def send_result(self):
        '''set the latest result'''
        return self._send_once()
    def update_header(self,header_name, header_value):
        '''Update the request'header in message editor.
            param header_name indicates header name like "Cookie".
            param header_value indicates value of header like "JSESSIONID=12345"'''
        self.set(update_header="1", header_name=header_name, header_value=header_value)
        return self._send_once()
    def get_request(self):
        '''Get current request in message editor.'''
        self.set(get_request="1")
        self._send_once()
        return self._recv_once().get("request")
    def get_all_request(self):
        '''Get all request from proxy history list.'''
        self.set(get_all_request="1")
        self._send_once()
        data = self._recv_once()
        return data
    def get_last_request(self):
        '''Get lasted request from proxy history list.'''
        self.set(get_last_request="1")
        self._send_once()
        data = self._recv_once()
        return data.get("last")
    def set_request(self, request):
        '''Set request to current message editor.But it doesn't work if message editor is read-only.
            Param request is str.'''
        self.set(set_request=1, request=request)
        return self._send_once()
    def get_response(self):
        '''Get current response '''
        self.set(get_response=1)
        self._send_once()
        data = self._recv_once()
        return None if data.get("response") == "burpython_null" else data.get("response")

class SelectionText(Action):
    '''Handle selection text.'''
    def __init__(self,**kw):
        super(SelectionText,self).__init__(Action.ACTION_MAP["selection_text"],**kw)
    def get_select_text(self):
        '''Get selection text.'''
        self.set(get_select_text=1)
        self._send_once()
        return self._recv_once().get("get_select_text")

class ProxyHandler(Action):
    '''Handle current request/response from proxy.'''

    # copy from burp
    ACTION_FOLLOW_RULES = 0
    ACTION_DO_INTERCEPT = 1
    ACTION_DONT_INTERCEPT = 2
    ACTION_DROP = 3
    ACTION_FOLLOW_RULES_AND_REHOOK = 0x10
    ACTION_DO_INTERCEPT_AND_REHOOK = 0x11
    ACTION_DONT_INTERCEPT_AND_REHOOK = 0x12

    def __init__(self,**kw):
        super(ProxyHandler,self).__init__(Action.ACTION_MAP["proxy_handler"],**kw)
    def get_request_response(self):
        '''Get current request or response from proxy.'''
        self.set(get_request_response=1)
        self._send_once()
        data = self._recv_once()
        request = data.get("request")
        response = data.get("response")
        return (None if request == "burpython_null" else request), (None if response == "burpython_null" else response)
    def set_action(self, action):
        '''set proxy action,the func must be called at last.'''
        self.set(set_action=1, action=action)
        self._send_once()
        return self._recv_once()
    def get_client_ip(self):
        '''Get the client ip.'''
        self.set(get_ip=1)
        self._send_once()
        return self._recv_once().get("get_ip")
    def get_proxy_listener_addr(self):
        '''Get proxy listener addr, like 127.0.0.1:8080'''
        self.set(get_proxy_listener_addr=1)
        self._send_once()
        return self._recv_once().get("get_proxy_listener_addr")
    def is_request(self):
        '''Whether the message is request or response'''
        self.set(get_is_request=1)
        self._send_once()
        return True if self._recv_once().get("get_is_request") == "1" else False
    def set_result(self, r):
        '''set request or response'''
        self.set(set_result=r)
        self._send_once()
        return self._recv_once()