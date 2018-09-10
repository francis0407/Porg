var ws = new WebSocket('ws://localhost:7272');

ws.onopen = function(evt) {
	
};

ws.onmessage = function(evt) { 
	var data = eval('('+evt.data+')');
	var response = {
        status : 1,
        message : ""
    };
    console.log("Get Message ",data);
	switch (data.action) {
	case 'ping':
		response.action = 'pong';
        response.data = {};
        console.log(JSON.stringify(response));
        ws.send(JSON.stringify(response));
        break;
    case 'task':
        if(data.data.type == 'm')
            mapTask(data.data);
        else if(data.data.type == 'r')
            reduceTask(data.data);
        break;
	default:
		break;
	}

};

ws.onclose = function(evt) {
	// on connection closing
};

function sendSpeed(speed){
    var response = {
        status : 1,
        message : ""
    };
	response.action = 'connect';
    response.data = {
        speed:speed
    };
    ws.send(JSON.stringify(response));
    return;
}
function sendError(error) {
    // send error back to server
    var response = {
        status : 1,
        message : ""
    };
    response.action = 'error';
    response.data = {
        error : error
    };
    ws.send(JSON.stringify(response));
    return;
}

function mapTask(msg){
    // get map data and do map

    $.ajax({
        async:false,
        url:"readFile.php",
        type:"post",
        data:{
            "url":msg.url[0],
            "slice":msg.slice
        },
        success:function(ans){
            // console.log(ans);
            ans = eval('('+ans+')');
            if(ans.status == 0){
                console.log(ans.message);
                sendError(ans.message);
                return;
            }
            else if(ans.status == 1){
                console.log("Read file.");
                if(ans.data.length != ans.len)
                    console.log("missing data :"+ans.data);
                // Do Mapper
                var result = doMapper(ans.data);
                if(result.error.length != 0){
                    //do Mapper error
                    console.log("Map Error:"+result.error);
                    sendError(result.error);
                    return;
                }
                result.partitionIndex.push(result.result.length);
                // save map result
                $.ajax({
                    async:false,
                    url:"writeFile.php",
                    type:"post",
                    data:{
                        "type":'m',
                        "tid":msg.slice,
                        "data":result.result,
                        "job_dir":msg.job_dir,
                        "index":JSON.stringify(result.partitionIndex)
                    },
                    success:function(ans){
                        ans = eval('('+ans+')');
                        switch(ans.status){
                        case 0:
                            // error
                            console.log(ans.message);
                            sendError(ans.message);
                            return;
                        case -1:
                            // exist
                            console.log(ans.message);
                        case 1:
                            console.log("Save map result.");
                            // ws.send(JSON.stringify({
                            // console.log(JSON.stringify({  
                            var response = JSON.stringify({
                            "status":1,
                                "message":"",
                                "action":"finish",
                                "data":{
                                    "type":"m",
                                    "tid":msg.tid,
                                    "url":ans.url
                                }
                            });
                            ws.send(response);
                            console.log(response);
                            return;
                        default:
                            return;
                        }
                    },
                    error:function() {
                        console.log("Can't connect to writeFile.php");
                        sendError("Can't connect to writeFile.php");
                        return;
                    }
                })
                return;
            }
        },
        error:function(ans){
            console.log("Can't connect to readFile.php.");
            sendError("Can't connect to readFile.php.");
            return;
        }
    });
}

function reduceTask(msg){
    //get reduce input and do reduce
    console.log("Begin Reduce");
    var input = Array();
    var count = 0;
    for(var i=0;i<msg.url.length;i++)
        $.ajax({
            async:false,
            url:"readFile.php",
            type:"post",
            data:{
                "url":msg.url[i],
                "slice":msg.slice
            },
            success:function(ans){
                // console.log(ans);
                ans = eval('('+ans+')');
                if(ans.status == 0){
                    console.log(ans.message);
                    sendError(ans.message);
                    return;
                }
                if(ans.status == 1){
                    if(ans.data.length != 0)
                        input.push(ans.data);
                    count++;
                    console.log("Get reduce input ",count);
                    
                    return;
                }
            },
            error:function(){
                console.log("Can't connect to readFile.php.");
                sendError("Can't connect to readFile.php.");
                return;
            }
         });
    
    // Do reduce
    var result = doReducer(input);
    if(result.error.length != 0){
        //do reducer error
        console.log("Reduce Error:"+result.error);
        sendError(result.error);
        return;
    }
    $.ajax({
        url:"writeFile.php",
        type:"post",
        data:{
            "type":"r",
            "tid":msg.slice,
            "data":result.result,
            "job_dir":msg.job_dir
        },
        success:function(ans){
            ans = eval('('+ans+')');
            switch(ans.status){
            case 0:
                // error
                console.log(ans.message);
                sendError(ans.message);
                return;
            case -1:
                // exist
                console.log(ans.message);
            case 1:
                console.log("Save reduce result.");
                // ws.send(JSON.stringify({
                // console.log(JSON.stringify({  
                var response = JSON.stringify({
                    "status":1,
                    "message":"",
                    "action":"finish",
                    "data":{
                        "type":"r",
                        "tid":msg.tid,
                        "slice":msg.slice,
                        "url":ans.url
                    }
                });
                ws.send(response);
                console.log("send to server: ",response);
                return;
            default:
                return;
            }
        },
        error:function() {
            console.log("Can't connect to writeFile.php");
            sendError("Can't connect to writeFile.php");
            return;
        }
    });
}

