(function() {
    if (typeof porg === 'undefined')
        porg = {};
    var ws; // websocket object

    var taskHandle = {
        "maponly": maponlyTask,
        "mapcache": mapcacheTask,
        "mapsort": mapsortTask,
        "reduce": reduceTask
    };

    var messageHandle = {
        "newTask": getNewTask
    };

    porg.run = function(host, args) { // TODO: using args of porg
        ws = new WebSocket(host);
        ws.onopen = onOpen;
        ws.onmessage = onMessage;
        ws.onclose = onClose; 
    };    

    function onOpen() {
        registerWorker();
    }

    function onMessage(msg) {
        var request = JSON.parse(msg);
        messageHandle[request["action"]](request["data"]);
    }

    function onClose() {

    }

    function registerWorker() {
        var msg = {
            status: "success",
            message: "",
            action: "register worker"
        }
        ws.send(JSON.stringify(msg));
    }

    function getNewTask(taskInfo) { // TODO: reuse program
        // example: {
        //     "job":{
        //       "name":"jobname",
        //       "jid":13,
        //       "jtype":"MapOnly",
        //       "dir":"job_dir",
        //       "program":"program",
        //       "map_num":8,
        //       "reduce_num":1
        //     },
        //     "tType":"maponly",
        //     "tid":3,
        //     "tArg":{
        //       "input":[
        //         "input1",
        //         "input2"
        //       ],
        //       "output":"output",
        //       "cache":[]
        //     }
        //   }

        var jobInfo = taskInfo["job"];
        var taskType = taskInfo["tType"];
        var taskArg = taskInfo["tArg"];
        
        taskHandle[taskType](jobInfo, taskInfo, taskArg);
    }

    function rejectionPromise(msg) {
        return new Promise(function (resolve, reject) {
            reject(msg);
        });
    }

    function maponlyTask(jobInfo, taskInfo, taskArg) {
        var downloadProgram = ajax(jobInfo["program"], {}, "get"); // download function
        var downloadInput = ajax(taskInfo["input"][0], {}, "get"); // download input
        Promise.all([downloadProgram, downloadInput])
            .then(function(values) {                                // execute program
                return new Promise(function (resolve, reject) {
                    var program = new Function(values[0]);
                    var input = values[1];
                    try {
                        var results = program(input);   // TODO: program args
                        resolve(results);
                    } catch (ex) {
                        reject(ex);
                    }
                });
            }, rejectionPromise)
            .then(function(results) {
                return ajax(jobInfo["upload"], {output: taskArg["output"], content: results}, "post");
            }, rejectionPromise)
            .then(function(msg){
                finishTask(taskInfo, msg);
            }, function(errors) {
                failTask(taskInfo, errors);
            });
    }

    function mapcacheTask(jobInfo, taskArg) {
        
    }

    function mapsortTask(jobInfo, taskArg) {
        
    }

    function failTask(taskInfo, msg) {
        var response = {
            status: "fail",
            message: msg,
            action: "failTask",
            data: taskInfo
        };

        ws.send(JSON.stringify(response));
    }

    function finishTask(taskInfo, msg) {
        var response = {
            status: "success",
            message: "",
            action: "finishTask",
            data: taskInfo
        };

        ws.send(JSON.stringify(response));
    }

    async function ajax(url, data, type) {
        return new Promise(function (resolve, reject) {
            var setting = {
                url: url,
                data: data,
                success: function (message) {resolve(message);},
                async: true
            }
            if (type == "get")
                Ajax.get(setting)
            else
                Ajax.post(setting)    
        });
    }

    var Ajax = (function() {
        var that = this;
        that.createXHR = function() {
            if (window.XMLHttpRequest) { 
                return new XMLHttpRequest();
            } else if (window.ActiveXObject) { 
                var versions = [ 'MSXML2.XMLHttp', 'Microsoft.XMLHTTP' ];
                for (var i = 0, len = versions.length; i < len; i++) {
                    try {
                        return new ActiveXObject(version[i]);
                        break;
                    } catch (e) {
                    }
                }
            } else {
            }
        }
        that.init = function(obj) {
            var objAdapter = {
                method : 'get',
                data : {},
                success : function() {
                },
                complete : function() {
                },
                error : function(s) {
                    alert('status:' + s + 'error!');
                },
                async : true
            }
            that.url = obj.url + '?rand=' + Math.random();
            that.method = obj.method || objAdapter.method;
            that.data = that.params(obj.data) || that.params(objAdapter.data);
            that.async = obj.async || objAdapter.async;
            that.complete = obj.complete || objAdapter.complete;
            that.success = obj.success || objAdapter.success;
            that.error = obj.error || objAdapter.error;
        }
        that.ajax = function(obj) {
            that.method = obj.method || 'get';
            if (obj.method === 'post') {
                that.post(obj);
            } else {
                that.get(obj);
            }
        }
        that.post = function(obj) {
            var xhr = that.createXHR(); 
            that.init(obj);
            that.method = 'post';
            if (that.async === true) { 
                xhr.onreadystatechange = function() {
                    if (xhr.readyState == 4) { 
                        that.callback(obj, this); 
                    }
                };
            }
            xhr.open(that.method, that.url, that.async);
            xhr.setRequestHeader('Content-Type',
                    'application/x-www-form-urlencoded');
            xhr.send(that.data); 
            if (that.async === false) { 
                that.callback(obj, this); 
            }
        };
        that.get = function(obj) {
            var xhr = that.createXHR(); 
            that.init(obj);
            if (that.async === true) { 
                xhr.onreadystatechange = function() {
                    if (xhr.readyState == 4) { 
                        that.callback(obj, this); 
                    }
                };
            }
            that.url += that.url.indexOf('?') == -1 ? '?' + that.data : '&'
                    + that.data;
            xhr.open(that.method, that.url, that.async);
            xhr.send(null); 
            if (that.async === false) { 
                that.callback(obj, this); 
            }
        }
        that.callback = function(obj, xhr) {
            if (xhr.status == 200) {
                obj.success(xhr.responseText);
            } else {
            }
        }
        that.params = function(data) {
            var arr = [];
            for ( var i in data) {
                arr.push(encodeURIComponent(i) + '=' + encodeURIComponent(data[i]));
            }
            return arr.join('&');
        }
        return {
            post : that.post,
            get : that.get,
            ajax : that.ajax
        }
    })();
})();