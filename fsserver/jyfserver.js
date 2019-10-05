/* express的服务器 */

//1. 导入express
var express = require('express')
var bodyParser = require('body-parser');//解析,用req.body获取post参数
var fs = require("fs")

//2. 创建express服务器
var server = express();
server.use(bodyParser.json({limit:'1000000kb'})); 
server.use(bodyParser.urlencoded({limit:'1000000kb',extended:true}));

var conf = {
    root : "/home/francis/Documents/jyf/"
}

var input = fs.readFileSync(conf.root + "input/keys3.txt");
var keys = eval(input.toString());
// console.log(keys);
server.all('*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "X-Requested-With");
    res.header("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS");
    res.header("X-Powered-By",' 3.2.1')
    res.header("Content-Type", "application/json;charset=utf-8");
    res.setHeader("Access-Control-Allow-Headers", "content-type, user-agent");
    next();
});



//3. 访问服务器(get或者post)
//参数一: 请求根路径
//3.1 get请求
server.get('/download', function (request, res) {
    // console.log(request)
    var url = request.query.url;
    var filePath = conf.root + url;
    var file = fs.readFileSync(filePath);
    res.setHeader('Content-Length', file.length);
    res.write(file, 'binary');
    res.end();
    // console.log(url);
});

server.get('/input', function (request, res) {
    // console.log(request)
    var part = request.query.part;
    var key = keys[parseInt(part.toString()) + 499];
    res.setHeader('Content-Length', key.length);
    res.write(key, 'binary');
    res.end();
    console.log(key);
});

//3.2 post请求
server.post('/upload', function (request, response) {
    // console.log(request.body);
    var target = request.body.output;
    var content = request.body.content;
    fs.writeFile(conf.root + target, content, { 'flag': 'w' }, (err) => {console.log("save" + target.toString())});
    response.send('{"status":"success"}');
});

//4. 绑定端口
server.listen(4051)
console.log('启动4051')
