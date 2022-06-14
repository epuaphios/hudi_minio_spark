package org.streaming

class recordData {

  private var _cas: String = _
  private var _event: String = _
  private var _key: String = _
  private var _content: String = _

  def cas = _cas

  def cas_=(cas: String) {
    _cas = cas
  }

  def event = _event

  def event_=(event: String) {
    _event = event
  }

  def key = _key

  def key_=(key: String) {
    _key = key
  }

  def content = _content

  def content_=(content: String) {
    _content = content
  }
}


//
//
//  def getKafkaMessageKey = udf((col: String) => {
//    val segments = col.split("content=", 2)
//    var key = null: String
//    if (segments.length == 2) {
//      key = StringUtils.substringBetween(segments(0), "key=", ",")
//    }
//    else if (segments.length == 1) {
//      key = StringUtils.substringBetween(segments(0), "key=", ",")}
//    else {
//      key = null
//    }
//    key
//  })
//
//  def getKafkaMessageContent = udf((col: String) => {
//    val segments = col.split("content=", 2)
//    var content = null: String
//
//    if (segments.length == 2) {
//      content = StringUtils.chop(segments(1))
//    }
//    else if (segments.length == 1) {
//      content = null
//
//    }
//    content
//  })
//
//
//}
