/*
 * Copyright (c) David Teresi 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.fauli1221.hhhhh

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.preference.PreferenceManager

@SuppressLint("ClickableViewAccessibility")
class AaaaaKeyboardView(
    context: Context,
    keyboardListener: AaaaaKeyboardListener,
) : ConstraintLayout(context), View.OnClickListener, View.OnLongClickListener,
    View.OnTouchListener {
    interface AaaaaKeyboardListener {
        var isUppercase: Boolean
        fun onH()
        fun onLongH()
        fun onReleaseH()
        fun onBackspace()
        fun onSpace()
        fun onReturn()
        fun onLongBackspace()
        fun onReleaseBackspace()
    }

    private val btnH: Button
    private val btnBackspace: ImageButton
    private val btnSpace: Button
    private val btnReturn: ImageButton
    private val btnUppercase: ImageButton

    private val keyboardListener: AaaaaKeyboardListener
    private val preferences: SharedPreferences
    private val themeWrapper: ContextThemeWrapper

    init {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(
            context
        )

        // I have to set the theme manually here, because when I call
        // setDefaultNightMode it doesn't apply to the keyboard for some
        // reason. AppThemeLight and AppThemeDark are functionally identical
        // to the light and dark modes of AppTheme.
        // https://stackoverflow.com/a/67340930/5253369
        val themeSetting = getStringPref(R.string.themeSettingKey)
        val themeId = if (themeSetting == "MODE_NIGHT_NO") {
            R.style.AppThemeLight
        } else if (themeSetting == "MODE_NIGHT_YES") {
            R.style.AppThemeDark
        } else {  // MODE_NIGHT_FOLLOW_SYSTEM
            R.style.AppTheme
        }
        themeWrapper = ContextThemeWrapper(context, themeId)
        // For some reason Kotlin calls these parameters p0, p1 and p2, so I
        // have to comment out the *actual* parameter names.
        // Have I mentioned how much I absolutely detest this language
        LayoutInflater.from(themeWrapper).inflate(
            /*resource=*/R.layout.hhhhh_keyboard_view,
            /*root=*/this,
            /*attachToRoot=*/true,
        )

        this.btnH = findViewById<Button>(R.id.btnH)
        this.btnBackspace = findViewById<ImageButton>(R.id.btnBackspace)
        this.btnSpace = findViewById<Button>(R.id.btnSpace)
        this.btnReturn = findViewById<ImageButton>(R.id.btnReturn)
        this.btnUppercase = findViewById<ImageButton>(R.id.btnUppercase)

        this.btnH.setOnLongClickListener(this)
        this.btnH.setOnTouchListener(this)
        this.btnH.setOnClickListener(this)
        this.btnBackspace.setOnClickListener(this)
        this.btnBackspace.setOnTouchListener(this)
        this.btnBackspace.setOnLongClickListener(this)
        this.btnReturn.setOnClickListener(this)

        this.keyboardListener = keyboardListener

        // Hide extra buttons if in minimalist mide
        val minimalistModeSetting = getBooleanPref(R.string.minimalistModeKey)
        if (minimalistModeSetting) {
            this.btnSpace.visibility = View.GONE
            this.btnUppercase.visibility = View.GONE
        }
        else {
            this.btnSpace.setOnClickListener(this)
            this.btnUppercase.setOnClickListener(this)
        }
    }

    private fun getBooleanPref(key: Int): Boolean {
        return this.preferences.getBoolean(context.getString(key), true)
    }

    private fun getStringPref(key: Int): String {
        return this.preferences.getString(context.getString(key), "")!!
    }

    override fun onClick(v: View) {
        val id = v.getId()
        if (this.getBooleanPref(R.string.hapticFeedbackKey))
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        when (id) {
            R.id.btnH -> keyboardListener.onH()
            R.id.btnBackspace -> keyboardListener.onBackspace()
            R.id.btnSpace -> keyboardListener.onSpace()
            R.id.btnReturn -> keyboardListener.onReturn()
            R.id.btnUppercase -> {
                keyboardListener.isUppercase = !keyboardListener.isUppercase
                if (keyboardListener.isUppercase) onUppercase() else onLowercase()
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        val id = v.getId()
        return when (id) {
            R.id.btnH -> {
                this.keyboardListener.onLongH()
                true
            }
            R.id.btnBackspace -> {
                this.keyboardListener.onLongBackspace()
                true
            }
            else -> false
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val id = v?.id
        val action = event?.action

        when (id) {
            R.id.btnH -> {
                if (action == MotionEvent.ACTION_UP) keyboardListener.onReleaseH()
            }
            R.id.btnBackspace -> {
                if (action == MotionEvent.ACTION_UP) keyboardListener.onReleaseBackspace()
            }
        }

        return false
    }

    private fun onUppercase() {
        btnH.text = "H"
        val colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary)
        btnUppercase.drawable.setTint(colorPrimary)
    }

    private fun onLowercase() {
        btnH.text = "h"
        val colorControlNormal = themeWrapper
                .theme
                .obtainStyledAttributes(intArrayOf(android.R.attr.colorControlNormal))
                .use { it.getColor(0, 0) }
        btnUppercase.drawable.setTint(colorControlNormal)
    }
}
