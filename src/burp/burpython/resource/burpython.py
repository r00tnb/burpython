import sys
import base64
import re
import json

def isVersion3():
    if sys.version_info.major == 3:
        return True
    return False
def b64Encode(data):
    if isVersion3():
        return base64.b64encode(data.encode()).decode()
    else:
        return base64.b64encode(data)
def b64Decode(data):
    if isVersion3():
        return base64.b64decode(data.encode()).decode()
    else:
        return base64.b64decode(data)

def println(string):
    sys.stdout.write("debug "+b64Encode(str(string))+'\n')
 
class HTTPRequest(object):
    def __init__(self, request_text):
        self.request_raw = request_text
        self.request_line = {"method":"GET", "path":"/", "protocol":"HTTP", "major":1, "minor":1}
        self.headers = {}
        self.cookies = {}
        self.body_raw = ""
        self.is_json = False
        self.body_json = {}
        self.request_param = {}
        self.parse()

    def build(self):
        self.request_raw = ''
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
        self.request_raw += headers
        self.request_raw += "\r\n"
        self.body_raw = ''
        if self.is_json and self.body_json is not None:
            self.body_raw = json.dumps(self.body_json)
        elif self.body_json is not None and not self.is_json:
            for k in self.body_json:
                self.body_raw += "{}={}&".format(k,self.body_json.get(k))
            self.body_raw = self.body_raw.rstrip("&")
        self.request_raw += self.body_raw
        return self.request_raw

    def parse(self):
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

        params = re.search(r"/\S*?\?(.*)", self.request_line.get("path"))
        if params is None:
            self.request_param = None
        else:
            re_request_param = re.findall(r"([\w_\-\[\]]+)=([^&]*)&?", params.group(1))
            if re_request_param is None:
                self.request_param = None
            else:
                self.request_param = {}
                for p in re_request_param:
                    self.request_param[p[0]] = p[1]

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
        except Exception:
            re_body_json = re.findall(r"([\w_\-\[\]]+)=([^&]*)&?", self.body_raw)
            if len(re_body_json) == 0 and len(self.body_raw) != 0:
                self.body_json = None
            else:
                self.body_json = {}
                for p in re_body_json:
                    self.body_json[p[0]] = p[1]
        return True


class Action(object):
    ACTION_MAP = {
        "generate_text":"GenerateText",
        "close":"Close",
        "update_request":"UpdateRequest"
    }

    def __init__(self, action, **kw):
        self.__action = action
        self._keywords = kw

    def _send_once(self):
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
        self._keywords.update(kw)
    def set(self, **kw):
        for k in kw.keys():
            kw.update({k:str(kw.get(k))})
        self._keywords = kw

    def _recv_once(self):
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
    

class Close(Action):
    def __init__(self,**kw):
        super(Close,self).__init__(Action.ACTION_MAP["close"],**kw)
    def close(self):
        self._keywords.clear()
        self._keywords.update({"msg":"closed"})
        self._send_once()

class GenerateText(Action):
    def __init__(self,**kw):
        super(GenerateText,self).__init__(Action.ACTION_MAP["generate_text"],**kw)
    def send_result(self,result):
        self.set(result=result)
        return self._send_once()
    def recv_from_ui(self, tips):# return str of ui
        self.set(ui="1", tips=tips)
        self._send_once()
        return self._recv_once().get("ui")

class UpdateRequest(Action):
    def __init__(self,**kw):
        super(UpdateRequest,self).__init__(Action.ACTION_MAP["update_request"],**kw)
    def send_result(self):
        return self._send_once()
    def update_header(self,header_name, header_value):
        self.set(update_header="1", header_name=header_name, header_value=header_value)
        return self._send_once()
    def get_request(self):
        self.set(get_request="1")
        self._send_once()
        return self._recv_once().get("request")
    def get_all_request(self):
        self.set(get_all_request="1")
        self._send_once()
        data = self._recv_once()
        return data
    def get_last_request(self):
        self.set(get_last_request="1")
        self._send_once()
        data = self._recv_once()
        return data.get("last")
    def set_request(self, request):
        self.set(set_request=1, request=request)
        return self._send_once()