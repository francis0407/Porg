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
        return sleep(5000).then(()=>{
            return new Promise((rs, rj) => {
                rs(output);
            });
        });
    }
})();