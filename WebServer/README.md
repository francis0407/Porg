# Usage

## Start

### debug mode

`php start.php start`

### daemon mode

`php start.php start -d`

## Stop

`php start.php stop`

# Structure

## Vendor

- `\workerman\gateway-worker\src\Gateway.php`
  - `Gateway`进程是暴露给客户端的让其连接的进程。
  - `Gateway`进程负责维持客户端连接，并转发客户端的数据给`BusinessWorker`进程处理
- `\workerman\gateway-worker\src\Register.php`
  - `Gateway`进程和`BusinessWorker`进程启动后分别向`Register`进程注册自己的通讯地址，`Gateway`进程和`BusinessWorker`通过`Register`进程得到通讯地址后，就可以建立起连接并通讯了。
  - `Register`服务是`GatewayWorker`内部通讯用的，用户不需要关心。
- `\workerman\gateway-worker\src\BusinessWorker.php`
  - `BusinessWorker`进程负责处理实际的业务逻辑（默认调用`Events.php`处理业务），并将结果推送给对应的客户端。

## Application/Prog

- Gateway进程是暴露给客户端的让其连接的进程。

### 心跳检测

- 如果客户端与服务端定时有心跳数据传输，则会比较及时的发现连接断开，触发`onClose`事件回调。
- 服务端向客户端发送心跳检测，客户端接收到心跳数据后，可以忽略不做任何处理，也可以回应心跳检测（向服务端发送一段任意数据）。
- 客户端发送`pong`，服务端发送`ping`