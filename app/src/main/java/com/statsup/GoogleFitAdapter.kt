package com.statsup

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.gms.auth.api.signin.GoogleSignIn.getAccountForExtension
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.FitnessOptions.ACCESS_WRITE
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType.TYPE_WEIGHT
import com.google.android.gms.fitness.data.Field.FIELD_WEIGHT
import java.util.concurrent.TimeUnit.MILLISECONDS

class GoogleFitAdapter(private val applicationContext: Context) {

    fun exportWeight() {
        val fitnessOptions = FitnessOptions.builder().addDataType(TYPE_WEIGHT, ACCESS_WRITE).build()

        val dataSource = DataSource.Builder()
            .setAppPackageName(applicationContext)
            .setDataType(TYPE_WEIGHT)
            .setStreamName("weight")
            .setType(DataSource.TYPE_RAW)
            .build()

        val dataPoints = WeightRepository.all().takeLast(1000).map {
            DataPoint.builder(dataSource)
                .setField(FIELD_WEIGHT, it.kilograms.toFloat())
                .setTimestamp(it.dateInMillis, MILLISECONDS)
                .build()
        }

        val account = getAccountForExtension(applicationContext, fitnessOptions)
        Fitness.getHistoryClient(applicationContext, account)
            .insertData(DataSet.builder(dataSource).addAll(dataPoints).build())
            .addOnSuccessListener { toast(R.string.weights_fit_export_ok) }
            .addOnFailureListener { toast(R.string.weights_fit_export_ko) }
    }

    private fun toast(textId: Int) {
        Toast.makeText(applicationContext, textId, LENGTH_SHORT).show()
    }
}
