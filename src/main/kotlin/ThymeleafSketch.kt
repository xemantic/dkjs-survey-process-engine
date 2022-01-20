/*
 * Copyright (c) 2022 Abe Pazos / Xemantic
 */

package de.dkjs.survey

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ThymeleafSketch {
  data class TestObject(var name: String, var birthYear: Int)

  @GetMapping("/sketch")
  fun index(model: Model): String {
    model.addAttribute("time", System.currentTimeMillis().toString())
    model.addAttribute(
      "people", listOf(
        TestObject("A", 1),
        TestObject("B", 2),
        TestObject("C", 3)
      )
    )
    return "sketch"
  }
}