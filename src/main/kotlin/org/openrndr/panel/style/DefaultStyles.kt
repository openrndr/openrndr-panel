package org.openrndr.panel.style

import org.openrndr.color.ColorRGBa


fun defaultStyles(
        controlBackground:ColorRGBa = ColorRGBa(0.5, 0.5, 0.5),
        controlHoverBackground:ColorRGBa = controlBackground.shade(1.5),
        controlTextColor:Color = Color.RGBa(ColorRGBa.WHITE.shade(0.8))
                 ) = listOf(

        styleSheet {
            selector = selector { type("item") }
            display = Display.NONE
        },


        styleSheet {
            selector = selector { type( "textfield") }
            width = 100.percent
            height = 64.px

        },
        styleSheet {
            selector = selector { type("dropdown-button") }
            width = LinearDimension.Auto
            height = 32.px
            background = Color.RGBa(controlBackground)
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
            fontSize = 16.px
        },

        styleSheet {
            selector = selector { type("colorpicker-button") }
            width = 100.px
            height = 32.px
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
            fontSize = 18.px
            fontFamily = "default"
        },
        styleSheet {
            selector = selector { type("slider") }
            height = 32.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 5.px
            marginLeft = 5.px
            marginRight = 5.px
            fontSize = 16.px
            color = controlTextColor
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
            height = 32.px
            width = LinearDimension.Auto
            marginTop = 5.px
            marginBottom = 5.px
            marginLeft = 5.px
            marginRight = 5.px
            fontSize = 16.px
            color = controlTextColor
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
            fontSize = 16.px
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
            selector = selector { type("dropdown-button") } withDescendant { type("button") }
            width = 100.percent
            height = 24.px
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
            height = 32.px
            paddingLeft = 10.px
            paddingRight = 10.px
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
            fontSize = 16.px
        }
)