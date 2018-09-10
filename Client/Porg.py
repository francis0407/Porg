from websocket import create_connection
import requests
import json
global ws
WSIP = "ws://localhost:7272"
FSIP = "http://localhost:80//"
class FileConf():
    def __init__(self,url,name='unnamed'):
        self.__url = url
        self.name = name
        self.__persistent = 0

    def persist(self,value = 1):
        self.__persistent = value

    def isSet(self):
        if self.__url == "":
            return 0
        else:
            return 1
    
    def deleteFile(self):
        '''
        delete file in fs
        '''
    def __del__(self):
        if self.__persistent == 0:
            self.deleteFile()

class Job():
    def __init__(self,job_name,prog_file,reduce_num,map_size =1024*1024,input_file = FileConf("")):
        self.job_name = job_name
        self.prog_file = prog_file
        self.input_file = input_file
        self.map_num = 0
        self.reduce_num = reduce_num
        self.map_size = map_size #Byte
        self.output_file = FileConf("")
        self.__runable = 1
   
    def submitAndWait(self):
        if not self.prog_file.isSet:
            raise Exception("program file is not set")
        if not self.input_file.isSet:
            raise Exception("input file is not set")
        if not self.__runable:
            raise Exception("Job %s not runable"%(self.job_name))
        # HttpRequest to FS 
        # create job directory
        result = requests.post(url="makeDir.php",data={
            # """url":self.input_file,
            # "map_num":self.map_num,
            # "map_size":self.map_size""
            "job_name":self.job_name
        })
        result_obj = json.loads(result)
        if not result_obj['status']:
            raise Exception(result_obj['message'])
        self.job_dir = result_obj['job_dir']

        #calculate index of input files
        result = requests.post(url="makeIndex.php",data={
            "url":self.input_file,
            "map_size":self.map_size
        })
        result_obj = json.loads(result)
        if not result_obj['status']:
            raise Exception(result_obj['message'])
        self.map_num = result_obj['map_num']
        
        # send message to WebServer
        global ws
        ws.send({
            "status":1,
            "message":"",
            "action":"job",
            "data":{
                "name":self.job_name,
                "job_dir":self.job_dir,
                "program":self.prog_file,
                "input":self.input_file,
                "map_num":self.map_num,
                "reduce_num":self.reduce_num
            }
        })

        # wait for result
        msg = ws.recieve(action="jobfinish")
        result = json.loads(msg)
        self.output_file = FileConf(self.job_dir)
        self.output_file.persist()
        self.__runable = 0

    def getResult(self):
        # HttpRequest to FS
        return 0

class WSConnector():
    def __init__(self,url=""):
        self.__url = url
        self.__msg_queue = []
    def connect(self,url=""):
        if url != "":
            self.__url = url
        self.__ws = create_connection(self.__url)
    def send(self,msg):
        if not hasattr(self,"__ws"):
            raise Exception("WebSocket has not connected.")
        if type(msg) == str:
            self.__ws.send(msg)
        else:
            self.__ws.send(json.dumps(msg))
    def recieve(self,action=""):
        if not hasattr(self,"__ws"):
            raise Exception("WebSocket has not connected.")
        if action == "":
            result = self.__ws.recv()
        else:
            for i in range(len(self.__msg_queue)):
                if json.loads(self.__msg_queue[i])['action'] == action:
                    result = self.__msg_queue.pop(i)
                    return result
            while(True):
                msg = self.__ws.recv()
                print(msg)
                msg_dict =json.loads(msg)
                if  msg_dict['action'] == action:
                    return msg
                elif msg_dict['action'] == "ping":
                    self.__ws.send(json.dumps({"action":"pong"}))
                else:
                    self.__msg_queue.append(msg)
        return result
    
def connectServer(url):
    global ws
    ws = WSConnector("ws://localhost:7272")
    ws.connect(url)
    ws.send({
        "status":1,
        "action":"client",
        "message":""
    })
