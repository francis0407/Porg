import queue
worker_q = queue.PriorityQueue()
job_q = queue.Queue()
dead_worker = set()
jobFinished = {}