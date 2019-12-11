(() => {
    return function(input) {
        function sleep(ms) {
            return new Promise(resolve => 
                setTimeout(resolve, ms)
            )
        }
        var output = []
        for (var i = 0; i < 5; i++)
            output.push(input);
        var sleepTime = Math.floor(800 + Math.random() * 5000);
        return sleep(sleepTime).then(()=>{
            return new Promise((rs, rj) => {
                rs(output);
            });
        });
    }
})();