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
        "task": getNewTask
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
        console.log(msg);
        var request = JSON.parse(msg.data);
        console.log(request);
        messageHandle[request["action"]](request["data"]);
    }

    function onClose() {
        console.log("websocket closing...");
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
        var downloadProgram = ajaxGet(jobInfo["program"]); // download function
        var downloadInput = ajaxGet(taskArg["input"][0]); // download input
        Promise.all([downloadProgram, downloadInput])
            .then(function(values) {                                // execute program
                return new Promise(function (resolve, reject) {
                    console.log(values[0]);
                    console.log(values[1]);
                    var input = values[1];
                    try {
                        var program = eval(values[0]);
                        // var results = program(input);   // TODO: program args
                        // console.log(results);
                        // resolve(results);
                        return program(input);
                    } catch (ex) {
                        // reject(ex);
                        return new Promise((rs, rj) => rj(ex));
                    }
                });
            }, rejectionPromise)
            .then(function(results) {
                return ajaxPost(jobInfo["host"], {output: taskArg["output"], content: results});
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

    function reduceTask(jobInfo, taskArg) {

    }

    function failTask(taskInfo, msg) {
        var response = {
            status: "fail",
            message: msg,
            action: "fail task",
            data: taskInfo
        };

        ws.send(JSON.stringify(response));
    }

    function finishTask(taskInfo, msg) {
        if (JSON.parse(msg)['status'] != "success") {
            failTask(taskInfo, msg);
            return;
        }
        var response = {
            status: "success",
            message: "",
            action: "finish task",
            data: taskInfo
        };

        ws.send(JSON.stringify(response));
    }
    
    async function ajaxGet(url) {
        // Default options are marked with *
                  return fetch(url, {
                      cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                      credentials: 'same-origin', // include, same-origin, *omit
                      headers: {
                        'user-agent': 'Mozilla/4.0 MDN Example',
                      },
                      method: "Get", // *GET, POST, PUT, DELETE, etc.
                      mode: 'cors', // no-cors, cors, *same-origin
                      redirect: 'follow', // manual, *follow, error
                      referrer: 'no-referrer', // *client, no-referrer
                  })
                  .then(response => response.text())
                  .catch(rejectionPromise);
    }

    async function ajaxPost(url, data) {
        return fetch(url, {
            body: JSON.stringify(data),
            cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
            credentials: 'same-origin', // include, same-origin, *omit
            headers: {
            'user-agent': 'Mozilla/4.0 MDN Example',
            'content-type': 'application/json'
            },
            method: "POST", // *GET, POST, PUT, DELETE, etc.
            mode: 'cors', // no-cors, cors, *same-origin
            redirect: 'follow', // manual, *follow, error
            referrer: 'no-referrer', // *client, no-referrer
        })
        .then(response => response.text())
        .catch(rejectionPromise);
    }
})();