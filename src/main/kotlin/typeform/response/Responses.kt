/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

package de.dkjs.survey.typeform.response

import kotlinx.serialization.SerialName

class Responses(
  @SerialName("total_items")
  var totalItems: String,
  @SerialName("page_count")
  var pageCount: String
) {



  //lateinit var items: Array<Item>

//  data class Item {
//
//  }
}
