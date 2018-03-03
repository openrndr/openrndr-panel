package org.openrndr.panel.style

import org.openrndr.color.ColorRGBa


fun defaultStyles(
        controlBackground:ColorRGBa = ColorRGBa(0.5, 0.5, 0.5),
        controlHoverBackground:ColorRGBa = controlBackground.shade(1.5)

                 ) = listOf(

        styleSheet {
            selector = select(type = "item") {}
            display = Display.NONE
        },

        styleSheet {
            selector = select(type = "dropdown-button") {}
            width = 100.px
            height = 30.px
            background = Color.RGBa(controlBackground)
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
        },

        styleSheet {
            selector = select(type = "colorpicker-button") {}
            width = 100.px
            height = 30.px
            background = Color.RGBa(controlBackground)
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
        },
        styleSheet {
            selector = select(type = "envelope-button") {}
            width = 100.px
            height = 40.px
            background = Color.RGBa(controlBackground)
            marginLeft = 5.px
            marginRight = 5.px
            marginTop = 5.px
            marginBottom = 5.px
        },

        styleSheet {
            selector = select(type = "body") {}
            fontSize = 12.px
            fontFamily = "default"
        },
        styleSheet {
            selector = select(type = "slider") {}
            height = 15.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 15.px
            marginLeft = 5.px
            marginRight = 5.px
        },
        styleSheet {
            selector = select(type = "envelope-editor") {}
            height = 60.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 15.px
            marginLeft = 5.px
            marginRight = 5.px
        },

        styleSheet {
            selector = select(type = "colorpicker") {}
            height = 80.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 15.px
            marginLeft = 5.px
            marginRight = 5.px
        },


        styleSheet {
            selector = select(class_ = "overlay") {}
            zIndex = ZIndex.Value(1)


        },
        styleSheet {
            selector = select(type = "toggle") {}
            height = 15.px
            width = 100.percent
            marginTop = 5.px
            marginBottom = 5.px
            marginLeft = 5.px
            marginRight = 5.px
        },


        styleSheet {
            selector = select(type = "h1") {}
            fontSize = 24.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },

        styleSheet {
            selector = select(type = "h2") {}
            fontSize = 20.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },

        styleSheet {
            selector = select(type = "h3") {}
            fontSize = 16.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },

        styleSheet {
            selector = select(type = "p") {}
            fontSize = 12.px
            width = 100.percent
            height = LinearDimension.Auto
            display = Display.BLOCK
        },
        styleSheet {
            selector = select(type = "dropdown-button", pseudoClass = "hover") {}
            display = Display.BLOCK
            background = Color.RGBa(controlHoverBackground)
        },
        styleSheet {
            selector = select(type = "button", pseudoClass = "hover") {}
            display = Display.BLOCK
            background = Color.RGBa(controlHoverBackground)
        },

        styleSheet {
            selector = select(type = "button") {}
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