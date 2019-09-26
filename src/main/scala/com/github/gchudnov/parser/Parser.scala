package gc.learnkafka.trakt

import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.TopologyDescription.Source

/**
 * Parses the text description of the Topology
 * 
 * Example:
 * 
 * Sub-topologies:
 *   Sub-topology: 0
 *     Processor: KSTREAM-FILTER-0000000005(stores: []) --> KSTREAM-SINK-0000000004 <-- KSTREAM-KEY-SELECT-0000000002
 *     Processor: KSTREAM-KEY-SELECT-0000000002(stores: []) --> KSTREAM-FILTER-0000000005 <-- KSTREAM-FLATMAPVALUES-0000000001
 *     Processor: KSTREAM-FLATMAPVALUES-0000000001(stores: []) --> KSTREAM-KEY-SELECT-0000000002 <-- KSTREAM-SOURCE-0000000000
 *     Source: KSTREAM-SOURCE-0000000000(topics: inputTopic) --> KSTREAM-FLATMAPVALUES-0000000001
 *     Sink: KSTREAM-SINK-0000000004(topic: Counts-repartition) <-- KSTREAM-FILTER-0000000005
 *   Sub-topology: 1
 *     Source: KSTREAM-SOURCE-0000000006(topics: Counts-repartition) --> KSTREAM-AGGREGATE-0000000003
 *     Processor: KTABLE-TOSTREAM-0000000007(stores: []) --> KSTREAM-SINK-0000000008 <-- KSTREAM-AGGREGATE-0000000003
 *     Sink: KSTREAM-SINK-0000000008(topic: outputTopic) <-- KTABLE-TOSTREAM-0000000007
 *     Processor: KSTREAM-AGGREGATE-0000000003(stores: [Counts]) --> KTABLE-TOSTREAM-0000000007 <-- KSTREAM-SOURCE-0000000006
 * Global Stores:
 *   none
 * 
 */
object Parser {

  def parse(input: List[String]): TopologyDescription = ???

}

trait LineParser {
  def parseNext(line: String): Unit
}

class NoneParser() {
  
}

class TopologyParser() {


}

class GlobalStoreParser() {

}

class ProcessorParser() {

}


class SinkParser() {

}
