# Porg Server Design Doc

主要功能：
1. 注册 Worker、提交 Job
2. 调度作业
3. 提供一个 DashBoard 监控
   
模块划分：
1. WebSocketServer
2. Scheduler
3. WebServer
   
## WebSocketServer 功能
1. 接受通过 WebSocket Client 发来的 Job Registration
2. 接受通过 WebSocket Client 发来的 Worker Registration
3. 向 WebSocket Client 反馈信息

## Scheduler 功能
1. 记录 Job
2. 记录 Worker
3. 将 Job 划分为 Task，加入到调度池
4. 调度 Task
5. 向 WebServer 提供实时信息

## WebServer 功能
1. 提供一个 Web Dashboard，其中包含内容：
   * Worker 数量（Total，Waiting，Working)，提供一个 LineChart
   * Job 数量 LineChart
   * Task 数量，提供一个 LineChart
2. 支持每个 Job 和 Worker 的历史记录，可以进入查看每个部分的内容
   * Job 要看到剩余多少 Task，完成多少 Task，开始时间，
   * Worker 要看到完成了哪些 Task，正在进行哪些 Task

## Job 的分类
1. MapOnlyJob (MapOnlyTask)
2. MapCacheJob (MapCacheTask)
3. MapReduceJob (MapShuffleTask, MapReduceTask)
   
## 

Heap
Map(date -> 248, lineorder -> 169499, supplier -> 750, part -> 1517, customer -> 1098)
Map(date -> 196, lineorder -> 166547, supplier -> 709, part -> 1479, customer -> 1049)
Map(date -> 224, lineorder -> 170201, supplier -> 687, part -> 1592, customer -> 921)

OffHeap
Map(date -> 193, lineorder -> 164171, supplier -> 675, part -> 1531, customer -> 1060)
Map(date -> 229, lineorder -> 165623, supplier -> 743, part -> 1583, customer -> 1050)
Map(date -> 228, lineorder -> 165352, supplier -> 698, part -> 1654, customer -> 1065)

int findMax(int* a, int n) {
    if (n == 1) {
        return a[0];
    } else {
        return max(a[0], findMax(a+1, n-1));
    }
}

heap
Map(date -> 0, lineorder -> 2, supplier -> 0, part -> 0, customer -> 0)
Map(date -> 1, lineorder -> 48509, supplier -> 4, part -> 183, customer -> 89)

offheap
Map(date -> 0, lineorder -> 2, supplier -> 0, part -> 0, customer -> 0)
Map(date -> 1, lineorder -> 42559, supplier -> 4, part -> 164, customer -> 88)

Map(date -> 0, lineorder -> 2, supplier -> 0, part -> 0, customer -> 0)
Map(date -> 2, lineorder -> 46486, supplier -> 6, part -> 201, customer -> 119)
Map(date -> 181, lineorder -> 17754, supplier -> 192, part -> 461, customer -> 354)
