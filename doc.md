# 公共接口格式

每个关键词在每条消息中均需要保留（为了保持一致，无论是否有含义）。
{
    “status”:1/0,
    "message":"",
    "action":"",
    "data":
    {
    }
}
`status`表示操作成功（1）或者失败（0），用于作为回复的消息，默认为1。
`message`用来保存操作失败时的错误信息或者其他保留信息，请注意此信息不需要解析，**若出现非空的message，各端（浏览器、服务器、调度器）均需要把message输出到终端。**
`action`用来记录当前消息的类型，这是需要被解析的字符串。
`data`用于传输消息中的数据。

## 具体接口内容

以下具体接口，在没有错误的操作里status默认为1,message默认为空，根据具体需求赋值。

### JobTracker端

接收的消息：
1.  新用户的连接
    "action":"connect"
    "data":{"uid":用户id为64位整数，“speed”:I/O速度（单位为KB/s）32位整数}

2.  用户断开连接
    "action":"disconnect"
    "data":{"uid":同上}

3.  用户发来错误消息
    "action":"error"
    "data":{"uid":,"error":错误字符串}

4.  用户完成任务
    "action":"finish"
    "data":{"type":'m'/'r',"uid":完成任务的用户id,"tid":task_id 32位整数,"url":[任务结果的地址,对于map有多个，reduce只有一个]}

发送的消息：
1.  做map/reduce
    "action":"task"
    "data":{"type":'m'/'r',"uid":做任务的用户id,"tid":task_id,"url":[输入文件的地址,对于map有一个，reduce有多个]}
2.  JobTracker与web server建立连接(此消息只有在当前没有JobTracker时有效)
    "action":"tracker"
    "data":{}

### WebServer端

接收的消息:
1. 用户完成测速
    "action":"connect"
    "data":{"speed":I/O速度（单位为KB/s）32位整数}

2. 用户完成任务
    "action":"finish"
    "data":{"type":'m'/'r',"tid":task_id 32位整数,"url":[任务结果的地址,对于map有多个，reduce只有一个]}

3. 用户发来错误消息
    "action":"error"    
    "data":{"error":错误字符串}

4. JobTracker发来的任务
    "action":"task"
    "data":{"type":'m'/'r',"uid":做任务的用户id 64位整数,"tid":task_id,"url":[输入文件的地址,对于map有一个，reduce有多个]}

5. JobTracker建立连接(此消息只有在当前没有JobTracker时有效)
    "action":"tracker"
    "data":{}

6. 心跳检测，发送给Browser

    "action":"ping"
    "data":{}

发出的消息:
1. 给用户分发任务
    "action":"task"
    "data":{"type":'m'/'r',"tid":task_id,"url":[输入文件的地址,对于map有一个，reduce有多个]}

2. 通知JobTracker完成测速
    "action":"connect"
    "data":{"uid":用户id,"speed":I/O}

3. 通知JobTracker有用户断开
    "action":"disconnect"
    "data":{"uid":用户id}

4. 通知JobTracker用户发来错误消息
    "action":"error"    
    "data":{"uid":,"error":错误字符串}

5. 通知JobTracker用户完成任务
    "action":"finish"
    "data":{"type":'m'/'r',"uid","tid":task_id 32位整数,"url":[任务结果的地址,对于map有多个，reduce只有一个]}

### Browser
只有与Webserver走WebSocket，与FileSystem走正常的HTML，这里先不写。

接收的消息:
2.  做任务
    "action":"task"
    "data":{"type":'m'/'r',"tid":task_id,"url":[输入文件的地址,对于map有一个，reduce有多个]}

发出的消息：
1. 连接上服务器自动测速
    "action":"connect"
    "data":{"speed":I/O速度（单位为KB/s）32位整数}

2. 心跳检测，发送给Web server（接受到ping消息时）

    "action":"pong"
    "data":{}

2.  完成任务
    "action":"finish"
    "data":{"type":'m'/'r',"tid":task_id 32位整数,"url":[任务结果的地址,对于map有多个，reduce只有一个]}

3.  出错了
    "action":"error"    
    "data":{"error":错误字符串}



