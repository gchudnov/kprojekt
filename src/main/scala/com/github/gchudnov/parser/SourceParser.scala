package gc.learnkafka.trakt.parser
import org.apache.kafka.streams.TopologyDescription.Source

/**
 * Parse the Source like
 * 
 * Source: KSTREAM-SOURCE-0000000000(topics: inputTopic) --> KSTREAM-FLATMAPVALUES-0000000001
 * 
 */ 
object SourceParser {

  private val Tag = "Source"

  def parse(line: String): Source = {
    require(line.indexOf(Tag) == 0, s"Invalid Parser for Tag: ${Tag}")

    line.stripSuffix(Tag + ":").split("-->")
  }

}
