package com.github.gchudnov.kprojekt.naming

/**
 * Represents a node name, like: KSTREAM-MAPVALUES-0000000002 or KSTREAM-SELECT-KEY-0000000002
 * in a parsed form
 */
final case class NodeName(id: Option[Int], alias: String, originalName: String)
