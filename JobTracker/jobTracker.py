from websocket import create_connection
import time
import json
import queue
from scheduler import scheduler
from scheduler import msg_generator
from job import job
import globalState
global Scheduler

def newJob(msg):
    data = json.loads(msg['data'])
    new_job = job(data['name'], data['cid'],
                data['job_dir'], data['program'],
                data['input'], int(data['map_num']), int(data['reduce_num']))
    globalState.job_q.put(new_job)

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
    data = json.loads(msg['Data'])
    uid = data['Uid']
    speed = data['Speed']
    globalState.worker_q.put((-speed, uid))

def disconnect(msg):
    '''
    Remove the user from the queue(if exists).
    Data field format:
    "data":
    {
        "uid":      int32
    }
    '''
    # data = json.loads(msg['data'])
    data = msg['Data']
    uid = data['Uid']
    Scheduler.removeWorker(uid)
    print("disconnect")

def error(msg):

    print("error")

def finish(msg):
    '''
    Mark a job as finished.
    '''
    data = json.loads(msg['Data'])
    #data = msg['Data']
    tid = data['Tid']
    url = data['Url']
    t = data['Type']
    jobID = data['Name']
    Scheduler.jobFinished(tid, url, t, jobID)

def ping(msg):
    ws.send(msg_generator(1,"","pong",""))
    

def callback(msg):
    action = {
        "connect": connect,
        "disconnect": disconnect,
        "error": error,
        "finish": finish,
        "ping":ping,
        "job":newJob
    }
    func = action.get(msg['Action'])
    return func(msg)

if __name__ == "__main__":

    ws = create_connection("ws://localhost:7272")
    ws.send(msg_generator(1, "", "tracker", ""))
    Scheduler = scheduler(ws)
    Scheduler.start()


    while(True):
        result =  ws.recv()
        msg = json.loads(result)
        print("Remote: {}".format(msg))
        callback(msg)
    
    

    ws.close()
