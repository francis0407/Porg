package edu.porg.webserver

import java.util.Date
import akka.http.scaladsl.server.{HttpApp, Route}
import edu.porg.history.{JobHistory, MapOnlyJobHistory, WorkerHistory}
import edu.porg.scheduler.{Job, WorkerManager}

class WebServer extends HttpApp {

  override protected def routes: Route = {
    get {
      concat(
        path("") {
          getFromResource("static/index.html")
        },
        pathPrefix("api") {
          concat(
            path("getOverviewData") {
              // 1. number of connected workers
              // 2. number of ideal workers
              // 3. number of submitted jobs
              // 4. number of finished jobs
              // 5. name of current job
              // 6. number of current job's tasks
              // 7. number of running tasks
              // 8. number of finished tasks
              val currentJobInfo = JobHistory.getCurrentJobInfo
              val result =
                s"""
                   |{
                   |    "connected_workers" : ${WorkerManager.getConnectedWorkersNumber},
                   |    "ideal_workers"     : ${WorkerManager.getIdealWorkersNumber},
                   |    "submitted_jobs"    : ${Job.getCurrentID},
                   |    "finished_jobs"     : ${JobHistory.getFinishedJobsNumber},
                   |    "current_job_name"  : "${currentJobInfo._1}",
                   |    "current_tasks"     : ${currentJobInfo._2},
                   |    "running_tasks"     : ${currentJobInfo._3},
                   |    "finished_tasks"    : ${currentJobInfo._4}
                   |}
                 """.stripMargin
              complete(result)
            },
            path("getWorkerNumberHistory") {
              val history = WorkerHistory.getWorkerNumberHistory
              val labels = history.map(x => {
                  '"' + s"${x._1.getHours}:${x._1.getMinutes}" + '"'
              }).mkString(",")
              val data = history.map(_._2).mkString(",")
              val result =
                s"""
                   |{
                   |  "labels": [$labels],
                   |  "data"  : [$data]
                   |}
                 """.stripMargin
              complete(result)
            },
            path("getAverageTaskFinishTime" / IntNumber) { jid =>
              val x = jid
              val finishTime = Seq(10, 20, 10, 50, 30, 40, 40, 20, 70, 90, 100, 100, 100, 100, 100, 100)//JobHistory.getTaskFinishTime(jid)

              // TODO: move this to the front end.
              val sortedTime =
                if (finishTime.nonEmpty)
                  finishTime.sorted
                else
                  Seq(0)

              /*********************** Slot Number ********************/
              val slotNumber = 12
              /*********************** Slot Number ********************/

              val avg = sortedTime.sum / sortedTime.size
              val duration:Double = (sortedTime.last - sortedTime.head).toDouble / slotNumber
              var upperBound: Double = sortedTime.head.toDouble + duration
              var data: Seq[Int] = Seq()
              var label: Seq[String] = Seq()
              var slot = 0
              for (t <- sortedTime) {
                while (t.toDouble > upperBound) {
                  data = data :+ slot
                  label = label :+ ((upperBound + upperBound - duration) / 2).toInt.toString
                  slot = 0
                  upperBound += duration
                }
                slot += 1
              }
              if (label.size != slotNumber) {
                data = data :+ slot
                label = label :+ ((upperBound + upperBound - duration) / 2).toInt.toString
              }
              label = label.map('"' + _ + "ms" + '"')
              val result =
                s"""
                  |{
                  |    "avg"   :$avg,
                  |    "labels":[${label.mkString(",")}],
                  |    "data"  :[${data.mkString(",")}]
                  |}
                """.stripMargin
              complete(result)
            },
            path("getJobList") {
              val jobList = JobHistory.getJobList.map('"' + _.toString + '"').mkString(",")
              val result =
                s"""
                  |{
                  |    "jobs"  : [${jobList}]
                  |}
                """.stripMargin
              complete(result)
            },
            path("getJobInfo"/IntNumber) {jid =>
              val jobInfo = JobHistory.getJobInfo(jid)
              val result = jobInfo match {
                case moj: MapOnlyJobHistory =>
                  val avg_finish_time : Double =
                    moj.finish_tasks.map(t => t.finishTime - t.startTime).sum.toDouble / moj.finish_tasks.size
                  val taskInfos = moj.finish_tasks.map(t => {
                    s"""
                      |{
                      |  "task_id"    : "${t.taskID.toString}",
                      |  "unique_id"  : "${t.uniqueID}",
                      |  "start_time" : "${new Date(t.startTime).toLocaleString}",
                      |  "finish_time": "${new Date(t.finishTime).toLocaleString}",
                      |  "use_time"   : "${t.finishTime - t.startTime} ms",
                      |  "input_path" : "${t.input}",
                      |  "output_path": "${t.output}",
                      |}
                    """.stripMargin
                  }).mkString(",")
                  s"""
                    |{
                    |    "job_name"       : "${moj.name}",
                    |    "job_id"         : "$jid",
                    |    "job_type"       : "MapOnlyJob",
                    |    "job_dir"        : "${moj.dir}",
                    |    "program_path"   : "${moj.program}",
                    |    "start_time"     : "${moj.startTime.toLocaleString}",
                    |    "finish_time"    : "${moj.finishTime.toLocaleString}",
                    |    "task_number"    : "${moj.inputs.size}",
                    |    "avg_finish_time": "$avg_finish_time ms",
                    |    "tasks_info"     : [$taskInfos]
                    |}
                  """.stripMargin
                case _ =>
                  s"""
                    |{
                    |    "job_name"      : "No Such Job"
                    |}
                  """.stripMargin
              }
              complete(result)
            }
          )
        },
        pathPrefix("") {
          getFromResourceDirectory("static")
        }
      )
    }
  }

}
