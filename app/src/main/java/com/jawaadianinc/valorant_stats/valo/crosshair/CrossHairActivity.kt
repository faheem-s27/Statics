package com.jawaadianinc.valorant_stats.valo.crosshair

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class CrossHairActivity : AppCompatActivity() {

    private val crossHair = CrosshairClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cross_hair)

        val paste: Button = findViewById<Button>(R.id.pasteClipboard)
        val copy: Button = findViewById(R.id.copyCliboard)
        val generate: Button = findViewById(R.id.generate)
        val crosshairCode: TextView = findViewById(R.id.crosshairCode)
        val crosshairImage: ImageView = findViewById(R.id.crosshairImage)

        // ----- Crosshair stuff ----------
        val primaryColourSpinner: Spinner = findViewById(R.id.crosshairColourPrimarySpinner)
        val outLinesEnabled: SwitchMaterial = findViewById(R.id.outLinesEnabled)
        val outlinesOpacity: SeekBar = findViewById(R.id.outLinesOpacity)

        generateCrosshair(crosshairCode.text.toString())
        populateSpinners()

        primaryColourSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // get selected item
                updateCrosshairCode()
            }
        }

        // listen for changes to the outLinesEnabled switch
        outLinesEnabled.setOnCheckedChangeListener { _, isChecked ->
            updateCrosshairCode()
        }

        // make the outlines seekbar have a range from 0 to 1 but it has 3 decimal places
        outlinesOpacity.max = 100
        outlinesOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateCrosshairCode()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // do nothing
            }
        })

        paste.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (verifyContents(text)) {
                crosshairCode.text = text
                processPastedCode(text!!)
                generateCrosshair(text)
            }
        }

        copy.setOnClickListener {
            if (verifyContents(crosshairCode.text as String?)) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", crosshairCode.text)
                clipboard.setPrimaryClip(clip)
                msg("Copied to clipboard")
            }
        }

        generate.setOnClickListener {
            if (verifyContents(crosshairCode.text as String?)) {
                generateCrosshair(crosshairCode.text as String)
            }
        }
    }

    private fun updateCrosshairCode() {
        // get the code from the text field
        val crosshairCode: TextView = findViewById(R.id.crosshairCode)

        // get the current selection of the primary colour spinner
        val primaryColourSpinner: Spinner = findViewById(R.id.crosshairColourPrimarySpinner)
        val primaryColourIndex = primaryColourSpinner.selectedItemPosition
        // get the outlines enabled switch
        val outLinesEnabled: SwitchMaterial = findViewById(R.id.outLinesEnabled)

        // get the opacity of the outlines
        val outlinesOpacity: SeekBar = findViewById(R.id.outLinesOpacity)
        val outlineAlpha = outlinesOpacity.progress / 100.0f

        val code =
            crossHair.updateCrosshair(primaryColourIndex, outLinesEnabled.isChecked, outlineAlpha)
        crosshairCode.text = code
    }

    private fun processPastedCode(crossHairID: String) {
        val primaryColourSpinner: Spinner = findViewById(R.id.crosshairColourPrimarySpinner)
        val outLinesEnabled: SwitchMaterial = findViewById(R.id.outLinesEnabled)
        val outLinesOpacity: SeekBar = findViewById(R.id.outLinesOpacity)

        crossHair.generateCrosshairSettingsFromCode(crossHairID)
        // show alert dialog
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Crosshair Settings")
        dialog.setMessage(crossHair.toString())
        dialog.setPositiveButton("OK") { dialog, which ->
            // do nothing
        }
        dialog.show()
        val outline = crossHair.outlinesEnabled
        // if the pasted code has the outLines enabled, set the switch to true
        outLinesEnabled.isChecked = outline
        // set the primary colour spinner to the pasted colour
        primaryColourSpinner.setSelection(crossHair.colour)
        // set the opacity of the outlines to the pasted opacity
        outLinesOpacity.progress = (crossHair.outlineAlpha).toInt()

    }

    private fun generateCrosshair(crossHairID: String) {
        val crosshairCode: TextView = findViewById(R.id.crosshairCode)
        val crosshairImage: ImageView = findViewById(R.id.crosshairImage)
        val url = "https://api.henrikdev.xyz/valorant/v1/crosshair/generate?id=$crossHairID"
        // when picasso is done loading the image, it will call the onSuccess function
        Picasso.get().load(url).into(crosshairImage, object : com.squareup.picasso.Callback {
            override fun onSuccess() {
                hideGenerateButton()
            }

            override fun onError(e: Exception?) {
                msg("Error loading image")
            }
        })
        //msg("Generated crosshair!")
        crosshairCode.text = crossHairID

    }

    private fun updatePrimaryCrosshairColour(colour: Int) {
        val crosshairCode: String = findViewById<TextView>(R.id.crosshairCode).text.toString()
        // find index of the letter "P" in the string
        var index = crosshairCode.indexOf("P")
        index = index.plus(4) // add 4 to the index to get the start of the colour code
        // update index with the colour
        findViewById<TextView>(R.id.crosshairCode).text =
            crosshairCode.substring(0, index) + colour.toString() + crosshairCode.substring(
                index + 1
            )
        generateCrosshair(findViewById<TextView>(R.id.crosshairCode).text.toString())
        //showGenerateButton()
    }

    private fun verifyContents(clipboard: String?): Boolean {
        // -----------------EXAMPLES----------------------
        // 0;p;0;s;1;P;c;1;h;0;0t;4;0l;1;0o;2;0a;1;0f;0;1b;0;A;c;1;o;1;d;1;0b;0;1b;0;S;c;1;s;1.078;o;1 <-- just a green dot
        // 0;P;c;1;o;1;f;0;0t;1;0l;2;0o;2;0a;1;0f;0;1b;0 <-- no center dot, green with inner lines
        // 0;s;1;P;c;4;t;2;o;1;0t;10;0o;0;0a;1;0f;0;1t;3;1l;3;1o;0;1a;0;1m;0;1f;0 <-- danny's smiley :)
        // 0;P;c;0;o;1;f;0;0t;1;0l;2;0o;2;0a;1;0f;0;1b;0 <-- generic
        // -----------------END---------------------------

        // empty clipboard
        if (clipboard == null) {
            msg("Clipboard is empty!")
            return false
        }

        // if clipboard doesn't have semi colon
        if (!clipboard.contains(";")) {
            msg("Invalid Code!")
            return false

        }

        // if clipboard doesn't have a zero
        if (!clipboard.contains("0")) {
            msg("Invalid Code!")
            return false
        }

        // if first character is not a digit
        if (clipboard[0].digitToIntOrNull() == null) {
            msg("Invalid Code!")
            return false
        }

        // if last character is not a digit
        if (clipboard[clipboard.length - 1].digitToIntOrNull() == null) {
            msg("Invalid Code!")
            return false
        }

        return true
    }

    private fun msg(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun populateSpinners() {
        // ---------- Primary Colour Spinner ----------
        val primaryColourSpinner: Spinner = findViewById(R.id.crosshairColourPrimarySpinner)
        // create array of colours
        val primaryColours = arrayOf(
            "White",
            "Green",
            "Yellow Green",
            "Green Yellow",
            "Yellow",
            "Cyan",
            "Pink",
            "Red"
        )
        // create an adapter for the spinner
        val primaryAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, primaryColours)
        // set the adapter
        primaryColourSpinner.adapter = primaryAdapter
    }

    private fun showGenerateButton() {
        val generate: Button = findViewById(R.id.generate)
        // check if the button is already at full opacity
        if (generate.alpha == 1f) {
            return
        }
        // animate the button by setting opacity to 0 and fading in with moving animation going upwards from the bottom of the screen
        generate.animate().alpha(0f).translationY(100f).setDuration(0)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    generate.animate().alpha(1f).translationY(0f).setDuration(500)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                generate.animate().alpha(1f).translationY(0f).setDuration(0)
                                    .setListener(null)
                            }
                        })
                }
            })
    }

    private fun hideGenerateButton() {
        val generate: Button = findViewById(R.id.generate)
        // check if the button is already at zero opacity
        if (generate.alpha == 0f) {
            return
        }
        // hide the button by fading out opacity and sliding it down to the bottom of the screen
        generate.animate().alpha(1f).translationY(0f).setDuration(0)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    generate.animate().alpha(0f).translationY(0f).setDuration(500)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                generate.animate().alpha(0f).translationY(0f).setDuration(0)
                                    .setListener(null)
                            }
                        })
                }
            })
    }

}
