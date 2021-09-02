package com.github.gchudnov.kprojekt.ids

trait NodeId {
  def tId: String
  def name: String
}

case class TopicId(name: String) extends NodeId {
  override def tId: String = s"t:$name"
}

case class SourceId(name: String) extends NodeId {
  override def tId: String = s"s:$name"
}

case class ProcessorId(name: String) extends NodeId {
  override def tId: String = s"p:$name"
}

case class SinkId(name: String) extends NodeId {
  override def tId: String = s"k:$name"
}

case class StoreId(name: String) extends NodeId {
  override def tId: String = s"r:$name"
}
