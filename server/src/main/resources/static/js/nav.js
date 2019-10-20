$(document).ready(function () {

    fetch("api/getJobList")
    .then(res => res.json())
    .then(json => {
        var jobs = json['jobs']
        for (var i = 0; i < jobs.length; i++) {
            var jid = jobs[i]
            var ul = document.getElementById("JobDetails")
            ul.innerHTML += "<li><a href=\"job.html?jid="+ jid +"\">Job"+ jid + "</a></li>";
        }
    });

});