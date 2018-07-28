import queue as Q
import threading
from asyncio import Semaphore
import json
import time

def msg_generator(status, message, action, data):
    msg = {}
    msg['status'] = status
    msg['message'] = message
    msg['action'] = action
    msg['data'] = data
    return json.dumps(msg)

class scheduler(threading.Thread):

    
    def __init__(self, worker_q, ws, map_jobs, n_reducer = 30, url = ""):
        '''
        worker_q: we put availabe workers into this priority queue.
        ws: a websocket connection object.
        map_jobs: a list storing all map tasks
        (more precisely: which part of the input file)
        n_reducer: Number of reducer.
        '''
        super(scheduler, self).__init__()
        self.url_list = []
        self.url = url
        self.mutex = Semaphore()
        self.map_jobs = map_jobs
        self.n_reducer = n_reducer
        self.stoprequest = threading.Event()
        self.worker_q = worker_q
        self.ws = ws
        self.dead_worker = set()
        self.mapCount = 0
        self.reduceCount = n_reducer
        # a dict used to track status of all map/reduce jobs,
        # entities in map_jobs are keys,
        # a list of worker(if not finished) or None object(if finished)
        # is the corresponding value
        self.map_status = {}
        self.reduce_status = {}
        self.tid_map = {}
        for job in map_jobs:
            self.map_status[job] = []
        for i in range(n_reducer):
            self.reduce_status[i] = []

    def removeWorker(self, uid):
        self.dead_worker.add(uid)

    def jobFinished(self, tid, url, type):
        '''
        Nth slice of the map job is finished,
        mark them as done in map_status
        '''
        if type == "m":
            if self.mapCount == 0:
                return
            self.mutex.acquire()
            n = self.tid_map[tid]
            if self.map_status[n] == None:
                self.mutex.release()
                return
            for worker in self.map_status[n]:
                self.worker_q.put(worker)
            self.url_list.append(url)
            self.map_status[n] = None
            self.mapCount -= 1
            self.map_jobs.remove(n)
            self.mutex.release()
            return
        if type == "r":
            self.mutex.acquire()
            n = self.tid_map[tid]
            if self.reduce_status[n] == None:
                self.mutex.release()
                return
            for worker in self.reduce_status[n]:
                self.worker_q.put(worker)
            self.reduce_status[n] = None
            self.reduceCount -= 1
            self.mutex.release()
            return

    def schedule_reduce(self):
        print("start reduce")
        counter = 0
        while(True):
            new_worker = self.worker_q.get(True)
            if new_worker[1] in self.dead_worker:
                self.dead_worker.remove(new_worker[1])
                continue
            self.mutex.acquire()
            if self.reduceCount == 0:
                return
            counter %= self.n_reducer
            while(self.reduce_status[counter] == None):
                counter += 1
            self.reduce_status[counter].append(new_worker)
            self.mutex.release()
            tid = (int(time.time() * 1000) + 
                    new_worker[0])
            self.tid_map[tid] = counter
            data = {
                'type':'r',
                'uid': new_worker[1],
                'tid' : tid,
                'slice': counter,
                'url': self.url_list
            }
            self.ws.send(msg_generator(1, "", "task", data))
            counter += 1

    def schedule_map(self):
        '''
        Map tasks are scheduled in this function,
        it will return iff all map tasks are finished.
        '''
        self.mapCount = len(self.map_jobs)
        counter = 0
        while(True):
            new_worker = self.worker_q.get(True)
            if new_worker[1] in self.dead_worker:
                self.dead_worker.remove(new_worker[1])
                continue
            self.mutex.acquire()
            if self.mapCount == 0:
                self.worker_q.put(new_worker)
                return
            counter %= self.mapCount
            job = self.map_jobs[counter]
            counter += 1
            self.map_status[job].append(new_worker) 
            self.mutex.release()
            tid = (int(time.time() * 1000) + 
                    new_worker[0])
            #tid = counter
            self.tid_map[tid] = job
            data = {
                'type':'m',
                'uid' :new_worker[1],
                'tid' :tid,
                'slice': job,
                'url' :[self.url]
            }
            self.ws.send(msg_generator(1, "", "task", data))


    def run(self):
        while not self.stoprequest.isSet():
            self.schedule_map()
            print("map done!")
            self.schedule_reduce()    
            print("reduce done!")     



