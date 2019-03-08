import org.porg.Jobs.MapOnlyJob
import org.porg.{PorgConf, Scheduler, TestWorker}
import org.scalatest.FunSuite

class SchedulerTestSuite extends FunSuite {

  val porgConf = new PorgConf()
  val scheduler = new Scheduler(porgConf)
  val schedulerThread = new Thread(()=> scheduler.run())



  test("scheduler test") {
    schedulerThread.start()
    val testJob = MapOnlyJob("testJob", "jjj", "/output", "program", Seq("t0", "t1", "t2"))
    val worker1 = TestWorker(scheduler)
    val worker2 = TestWorker(scheduler)
    val worker3 = TestWorker(scheduler)

    scheduler.registerWorker(worker1)
    scheduler.registerWorker(worker2)
    scheduler.registerWorker(worker3)

    scheduler.registerJob(testJob)
    val testjob2 = MapOnlyJob("testJob2", "jjj2", " ", " ", Seq("ta", "tb", "tc", "td"))
    scheduler.registerJob(testjob2)

    schedulerThread.join()
  }
}