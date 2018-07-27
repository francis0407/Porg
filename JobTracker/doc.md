# Job trakcer
## Dependency:
```
pip install websocket-client
```
## Configuration:
1. Address and port of websocket server
    ```
    ws = create_connection("ws://address:port")
    ```

2. Partition of the input file
   ```
   map_list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
   ```
3. URL of input file
   ```
   url = ""
   ```
4. Number of reducers
   ```
   n_reducer = ???
   ```

## Implementation:
Job tracker and scheduler are two different threads.
The scheduler's job is to allocate workers for map and reduce tasks.
Job tracker is responsible for receiving and parsing messages. It then tells the scheduler what to do:

1. When a new browser is connected, job tracker puts the new worker into the worker_q (which is a priority queue). The scheduler could restart if it has been blocked.
2. When a browser is dead, job tracker tells the scheduler to move that browser into the blacklist(a python *set*). So that no more jobs will be assigned to browser.
3. When a job is finished, the corresponding entity in the scheduler's ***_status *dict* is set to None. All workers all the put back to the queue.
 