package com.jawaadianinc.valorant_stats.valo.crosshair

class CrosshairClass(
    var colour: Int,
    var outlinesEnabled: Boolean,
    var outlineWidth: Int,
    var outlineAlpha: Float,
    var centerDotEnabled: Boolean,
    var centerDotWidth: Int,
    var centerDotAlpha: Float,
    var innerLinesEnabled: Boolean,
    var innerLinesWidth: Int,
    var innerLinesLength: Int,
    var innerLinesOffset: Int,
    var innerLinesAlpha: Float,
    var outerLinesEnabled: Boolean,
    var outerLinesWidth: Int,
    var outerLinesLength: Int,
    var outerLinesOffset: Int,
    var outerLinesAlpha: Float
) {

    var code = ""

    constructor() : this(
        0,
        true,
        0,
        0f,
        false,
        0,
        0f,
        true,
        0,
        0,
        0,
        0f,
        true,
        0,
        0,
        0,
        0f
    )

    fun generateCrosshairSettingsFromCode(crosshairCode: String) {
        code = crosshairCode
        // split the code into an array of strings with delimiter ";"
        val codeArray = crosshairCode.split(";")
        // loop through the array and set the values
        for (i in codeArray.indices) {
            when (codeArray[i]) {
                "c" -> colour = codeArray[i + 1].toInt()
                // if h is found, set the outlinesEnabled to false
                "h" -> outlinesEnabled = false
                "t" -> outlineWidth = codeArray[i + 1].toInt()
                "o" -> outlineAlpha = codeArray[i + 1].toFloat()
                // if d is found, set the centerDotEnabled to true
                "d" -> centerDotEnabled = true
                "z" -> centerDotWidth = codeArray[i + 1].toInt()
                "a" -> centerDotAlpha = codeArray[i + 1].toFloat()
                // if 0b is found, set the innerLinesEnabled to true
                "0b" -> innerLinesEnabled = false
                "0t" -> innerLinesWidth = codeArray[i + 1].toInt()
                "0l" -> innerLinesLength = codeArray[i + 1].toInt()
                "0o" -> innerLinesOffset = codeArray[i + 1].toInt()
                "0a" -> innerLinesAlpha = codeArray[i + 1].toFloat()
                // if 1b is found, set the outerLinesEnabled to true
                "1b" -> outerLinesEnabled = false
                "1t" -> outerLinesWidth = codeArray[i + 1].toInt()
                "1l" -> outerLinesLength = codeArray[i + 1].toInt()
                "1o" -> outerLinesOffset = codeArray[i + 1].toInt()
                "1a" -> outerLinesAlpha = codeArray[i + 1].toFloat()
            }
        }
    }

    // returns the code for the crosshair
    override fun toString(): String {
        var code = ""
        code += "Colour : $colour\n"
        code += "Outlines Enabled : $outlinesEnabled\n"
        code += "Outline Width : $outlineWidth\n"
        code += "Outline Alpha : $outlineAlpha\n"
        code += "Center Dot Enabled : $centerDotEnabled\n"
        code += "Center Dot Width : $centerDotWidth\n"
        code += "Center Dot Alpha : $centerDotAlpha\n"
        code += "Inner Lines Enabled : $innerLinesEnabled\n"
        code += "Inner Lines Width : $innerLinesWidth\n"
        code += "Inner Lines Length : $innerLinesLength\n"
        code += "Inner Lines Offset : $innerLinesOffset\n"
        code += "Inner Lines Alpha : $innerLinesAlpha\n"
        code += "Outer Lines Enabled : $outerLinesEnabled\n"
        code += "Outer Lines Width : $outerLinesWidth\n"
        code += "Outer Lines Length : $outerLinesLength\n"
        code += "Outer Lines Offset : $outerLinesOffset\n"
        code += "Outer Lines Alpha : $outerLinesAlpha\n"
        return code
    }

    // update the colour of the crosshair and return the whole code
    fun updateCrosshair(newColour: Int, outLinesEnabled: Boolean, outlineAlpha: Float): String {
        this.colour = newColour
        this.outlinesEnabled = outLinesEnabled
        this.outlineAlpha = outlineAlpha
        // split the code into an array of strings with delimiter ";"
        val codeArray = code.split(";").toMutableList()
        // loop through the array and set the values for all values
        for (i in codeArray.indices) {
            when (codeArray[i]) {
                "c" -> codeArray[i + 1] = colour.toString()
                "t" -> codeArray[i + 1] = outlineWidth.toString()
                "o" -> codeArray[i + 1] = this.outlineAlpha.toString()
                "z" -> codeArray[i + 1] = centerDotWidth.toString()
                "a" -> codeArray[i + 1] = centerDotAlpha.toString()
                "0t" -> codeArray[i + 1] = innerLinesWidth.toString()
                "0l" -> codeArray[i + 1] = innerLinesLength.toString()
                "0o" -> codeArray[i + 1] = innerLinesOffset.toString()
                "0a" -> codeArray[i + 1] = innerLinesAlpha.toString()
                "1t" -> codeArray[i + 1] = outerLinesWidth.toString()
                "1l" -> codeArray[i + 1] = outerLinesLength.toString()
                "1o" -> codeArray[i + 1] = outerLinesOffset.toString()
                "1a" -> codeArray[i + 1] = outerLinesAlpha.toString()
            }
        }

        if (!outLinesEnabled) codeArray.add("h;0")

        // join the array back together with delimiter ";"
        code = codeArray.joinToString(";")
        return code
    }

}
