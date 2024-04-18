package com.uta.gasmaster

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class HomeScreen : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        val gasTypes = arrayOf("Regular", "Mid-grade", "Premium", "Diesel")
        val spinner = findViewById<Spinner>(R.id.gasTypeSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gasTypes)
        spinner.adapter = adapter

        val submitButton = findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener{
            val selectedGasType = spinner.selectedItem.toString()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("GAS_TYPE", selectedGasType)
            startActivity(intent)
        }
    }
}
