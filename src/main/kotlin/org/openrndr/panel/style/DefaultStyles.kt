package org.openrndr.panel.style

import org.openrndr.color.ColorRGBa


fun defaultStyles(
        controlBackground:ColorRGBa = ColorRGBa(0.5, 0.5, 0.5),
        controlHoverBackground:ColorRGBa = controlBackground.shade(1.5)

                 ) = listOf(

        styleSheet {
            selector = selector { type("item") }
            display = Display.NONE
        },

        styleSheet {
            selector = selector { type("dropdown-button") }
            width = 100.px
            height = 30.px
            background = Color.RGBa(controlBackground)
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
        },

        styleSheet {
            selector = selector { type("colorpicker-button") }
            width = 100.px
            height = 30.px
            background = Color.RGBa(controlBackground)
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
        },
        styleSheet {
            selector = selector { type("envelope-button") }
            width = 100.px
            height = 40.px
            background = Color.RGBa(controlBackground)
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
        },

        styleSheet {
            selector = selector { type("body") }
            fontSize = 12.px
            fontFamily = "default"
        },
        styleSheet {
            selector = selector { type("slider") }
            height = 15.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 15.px
            marginLeft = 5.px
            marginRight = 5.px
        },
        styleSheet {
            selector = selector { type("envelope-editor") }
            height = 60.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 15.px
            marginLeft = 5.px
            marginRight = 5.px
        },

        styleSheet {
            selector = selector { type("colorpicker") }
            height = 80.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 15.px
            marginLeft = 5.px
            marginRight = 5.px
        },


        styleSheet {
            selector = selector { `class`("overlay") }
            zIndex = ZIndex.Value(1)


        },
        styleSheet {
            selector = selector { type("toggle") }
            height = 15.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 5.px
            marginLeft = 5.px
            marginRight = 5.px
        },


        styleSheet {
            selector = selector { type("h1") }
            fontSize = 24.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },

        styleSheet {
            selector = selector { type("h2") }
            fontSize = 20.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },

        styleSheet {
            selector = selector { type("h3") }
            fontSize = 16.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },

        styleSheet {
            selector = selector { type("p") }
            fontSize = 12.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },
        styleSheet {
            selector = selector { type("dropdown-button", pseudo = "hover") }
            display = Display.BLOCK
            background = Color.RGBa(controlHoverBackground)
        },
        styleSheet {
            selector = selector { type("button", pseudo = "hover") }
            display = Display.BLOCK
            background = Color.RGBa(controlHoverBackground)
        },

        styleSheet {
            selector = selector { type("button") }
            display = Display.BLOCK
            background = Color.RGBa(controlBackground)
            width = 80.px
            height = 24.px
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
        }
)