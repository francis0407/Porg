package org.porg.Messages

import org.porg.Tasks.TaskInfo

abstract class ActionType {
  def actionName: String

  override def toString: String = actionName
}

object NewTask extends ActionType {
  override def actionName: String = "task"
}

abstract class StatusType {
  def statusName: String

  override def toString: String = statusName
}

object Success extends StatusType {
  override def statusName: String = "succes"
}

//``` json
//{
//  "status": string,
//  "message": string,
//  "action": string,
//  "data": ...
//}
//```
case class BasicMessage(status: String, message: String, action: String, data: Any) {
  def toJson(): String = {
    data match {
      case t: TaskInfo =>
        s"""
          |{
          |  "status": "$status",
          |  "message": "$message",
          |  "action": "$action",
          |  "data": ${t.toJson()}
          |}
        """.stripMargin
    }
  }
}

object BasicMessage {
  def apply(status: StatusType, message: String, action: ActionType, data: Any): BasicMessage =
    BasicMessage(status.toString, message, action.toString, data)

  def apply(data: TaskInfo) = BasicMessage(Success, "", NewTask, data)
}

