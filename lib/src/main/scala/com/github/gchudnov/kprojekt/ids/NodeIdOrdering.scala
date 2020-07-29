package com.github.gchudnov.kprojekt.ids

import scala.math.Ordering

object NodeIdOrdering {
  implicit val nodeIdOrdering: Ordering[NodeId] = (x: NodeId, y: NodeId) => x.name.compare(y.name)

  implicit val topicIdOrdering: Ordering[TopicId]         = (x: NodeId, y: NodeId) => x.name.compare(y.name)
  implicit val sourceIdOrdering: Ordering[SourceId]       = (x: NodeId, y: NodeId) => x.name.compare(y.name)
  implicit val processorIdOrdering: Ordering[ProcessorId] = (x: NodeId, y: NodeId) => x.name.compare(y.name)
  implicit val sinkIdOrdering: Ordering[SinkId]           = (x: NodeId, y: NodeId) => x.name.compare(y.name)
  implicit val storeIdOrdering: Ordering[StoreId]         = (x: NodeId, y: NodeId) => x.name.compare(y.name)
}
