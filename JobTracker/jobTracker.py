from websocket import create_connection
import time
import json
import queue
from scheduler import scheduler
from scheduler import msg_generator
global worker_q
global Scheduler



def connect(msg):
    '''
    Add new user to the ready queue,
    then call the scheduler to assign a job
    to the new user.
    Data field format:
    "data":
    {
        "uid":      int64
        "speed":    int32
    }
    '''
    data = json.loads(msg['data'])
    uid = data['uid']
    speed = data['speed']
    worker_q.put((-speed, uid))

def disconnect(msg):
    '''
    Remove the user from the queue(if exists).
    Data field format:
    "data":
    {
        "uid":      int32
    }
    '''
    data = json.loads(msg['data'])
    uid = data['uid']
    Scheduler.removeWorker(uid)
    print("disconnect")

def error(msg):

    print("error")

def finish(msg):
    '''
    Mark a job as finished.
    '''
    data = json.loads(msg['data'])
    tid = data['tid']
    url = data['url']
    t = data['type']
    Scheduler.jobFinished(tid, url, t)


def callback(msg):
    action = {
        "connect": connect,
        "disconnect": disconnect,
        "error": error,
        "finish": finish
    }
    func = action.get(msg['action'])
    return func(msg)

if __name__ == "__main__":

    ws = create_connection("ws://localhost:8080/echo")
    worker_q = queue.PriorityQueue()
    
    map_list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    url = ""
    n_reducer = 30
    Scheduler = scheduler(worker_q, ws, map_list,n_reducer=n_reducer ,url = url)
    Scheduler.start()

    ws.send(msg_generator(1, "", "tracker", ""))

    while(True):
        result =  ws.recv()
        msg = json.loads(result)
        print("Remote: {}".format(msg['message']))
        callback(msg)
    
    

    ws.close()
