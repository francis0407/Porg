var ws = new WebSocket('ws://localhost:7272');

ws.onopen = function(evt) {
	response = {};
	response.action = 'connect';
	response.speed = 0;
};

ws.onmessage = function(evt) {
	data = eval('('+evt.data+')');
	response = {};
	switch (data.type) {
	case 'ping':
		response.action = 'pong';
		response.data = {};
		break;
	default:
		break;
	}
	console.log(JSON.stringify(response));
	ws.send(JSON.stringify(response));
};

ws.onclose = function(evt) {
	// on connection closing
};

function doMapper(input) {
	var enc = new TextEncoder();
	var inputBuffer = enc.encode(input);
	var ret = Module.ccall('__doMapperWrapper__', 'number', ['array'], [inputBuffer]);
	var resultAddr = Module.ccall('__getMapperResultRef', 'number', null, null);
	var errAddr = Module.ccall('__getMapperErrorMessageRef', 'number', null, null);
	var partAddr = Module.ccall('__getMapperPartitionRef', 'number', null, null);
	var partLen = Module.ccall('__getPartitionIndexLength', 'number', null, null);
	var result = Module.AsciiToString(resultAddr);
	var errorMsg = Module.AsciiToString(errAddr);
	var partitionIndex = [];
	for (var i = 0; i < partLen; ++i) {
		partitionIndex.push(Module.getValue(partAddr, 'i32'));
		partAddr += 4;	// sizeof(i32)
	}
	return {
		'result': result,
		'error': errorMsg,
		'partitionIndex': partitionIndex
	};
}

function doReducer(input) {
	var enc = new TextEncoder();
	for (var i = 0; i < input.length; ++i) {
		var inputBuffer = enc.encode(input[i]);
		Module.ccall('__addStringToInputList', 'number', ['array'], [inputBuffer]);
	}
	var ret = Module.ccall('__doReducerWrapper__', 'number', null, null);
	var resultAddr = Module.ccall('__getReducerResultRef', 'number', null, null);
	var errAddr = Module.ccall('__getReducerErrorRef', 'number', null, null);
	var result = Module.AsciiToString(resultAddr);
	var errorMsg = Module.AsciiToString(errAddr);
	return {
		'result': result,
		'error': errorMsg
	};
}

Module['onRuntimeInitialized'] = () => {
	map1 = doMapper("Hello world\nHello again\nBye bye.\n");
	map2 = doMapper("This is a sentence.\nHere comes the second.\nAnd the third!\n");
	reduce = doReducer([map1.result, map2.result]);
}