你需要一个服务器。
比如php。
这里拿nodejs的http-server演示。

~/client> http-server -p 8000

访问http://localhost:8000/index.html

index.html的body：
<body>
	<script src="wordcount.js"></script>
	<script src="index.js"></script>
</body>

wordcount.js是编译出来的
编译命令:
em++ WordCount.cpp -s WASM=1 -std=c++11 -o wordcount.html -s "EXTRA_EXPORTED_RUNTIME_METHODS=['ccall', 'cwrap', 'AsciiToString', 'getValue']"

你会得到wordcount.js和wordcount.wasm, 把他们俩放这。

index.js里
Module['onRuntimeInitialized'] 是wordcount初始化结束的回调函数。

doMapper接受字符串， 返回
{'result': 结果字符串, 'error': 错误消息, 'partitionIndex': 分区索引数组}

doReducer接受[map1.result, map2.result, ... ]
返回{'result': 结果, 'error': 错误消息}
