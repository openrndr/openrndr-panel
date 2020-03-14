package org.openrndr.panel.test

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.*
import org.openrndr.panel.style.*

fun main() = application {
    configure {
        width = 500
        height = 500
    }

    program {
        var bgColor = ColorRGBa.BLACK
        val cm = ControlManager()
//        cm.fontManager.register("default", "file:data/Roboto-Medium.ttf")
        cm.layouter.styleSheets.addAll(defaultStyles())

        val s = styleSheet {
            width = 200.px
        }

        cm.layouter.styleSheets.add(s)

        // -- our body is just going to contain a single button
        cm.body = layout(cm) {
            dropdownButton {
                label = "background"
                item {
                    label = "pink"
                    events.picked.subscribe {
                        bgColor = ColorRGBa.PINK
                    }
                }
                item {
                    label = "black"
                    events.picked.subscribe {
                        bgColor = ColorRGBa.BLACK
                    }
                }
            }

            colorpickerButton {
                label = "Color"
                events.valueChanged.subscribe {
                    bgColor = it.color
                }
            }

            toggle {

            }

            slider {
                range = Range(0.0, 10.0)
            }

            colorpicker {

            }

            vector2DPad {

            }
        }

        extend(cm)

        extend {
            drawer.background(bgColor)
        }
    }
}