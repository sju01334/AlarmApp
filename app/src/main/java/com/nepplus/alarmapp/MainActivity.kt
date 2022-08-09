package com.nepplus.alarmapp

import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initOnOffButtion()
        initChangeAlarmTimeButton()

        //데이터 가져오기

        val model = fetchDataFromSharedPreferences()
        renderView(model)

        //뷰에 데이터 그려주기



    }




    private fun initOnOffButtion(){
        val onOffButton = findViewById<Button>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            //데이터 확인

            // 온오프에 따라 작업을 처리
            // 오프 -> 알람제거
            // 온 - 알람 등록

            //데이터를 저장

        }
    }

    private  fun initChangeAlarmTimeButton(){
        val changeAlarmButton = findViewById<Button>(R.id.changeAlarmTimeBUtton)
        changeAlarmButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            //TimePickerDialog 띄워줘서 시간설정
            TimePickerDialog(this, { picker, hour, minute ->

                val model = saveAlarmModel(hour, minute, false)
                renderView(model)
                //기존 알람 삭제
                val pendingIntent = PendingIntent.getBroadcast(this,
                    ALARM_REQUEST_CODE,
                    Intent(this, AlarmReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE
                )
                pendingIntent?.cancel()

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
                .show()


        }
    }

    private  fun saveAlarmModel(hour : Int, minute : Int, onOff : Boolean) : AlarmDisplayModel{
        val model = AlarmDisplayModel(
            hour = hour,
            minute = minute,
            onOff = false
        )

        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()){
            putString(ALARM_KEY, model.makeDataForDB())
            putBoolean(ONOFF_KEY, model.onOff)
            commit()
        }

        return model
    }

    private fun fetchDataFromSharedPreferences(): AlarmDisplayModel {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        val timeDBValue = sharedPreferences.getString(ALARM_KEY, "9:30") ?: "9:40"
        val onOffDBValue = sharedPreferences.getBoolean(ONOFF_KEY, false)
        val alarmData = timeDBValue.split(":")
        val alarmModel = AlarmDisplayModel(
            alarmData[0].toInt(),
            alarmData[1].toInt(),
            onOffDBValue
        )

        //예외처리

        val pendingIntent = PendingIntent.getBroadcast(this,
            ALARM_REQUEST_CODE,
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE
        )

        if ((pendingIntent == null) and alarmModel.onOff){
            //알람은 꺼져있는데 데이터는 켜져있는 경우
            alarmModel.onOff = false

        }else if ((pendingIntent != null) and alarmModel.onOff.not()){
            // 알람은 켜져있는데 , 데이터는 껴져있는 경우
            //알람을 취소
            pendingIntent.cancel()
        }

        return alarmModel

    }

    private fun renderView(model: AlarmDisplayModel) {
        findViewById<TextView>(R.id.ampmTextView).apply {
            text = model.ampmText
        }
        findViewById<TextView>(R.id.timeTextView).apply {
            text = model.timeText
        }
        findViewById<Button>(R.id.onOffButton).apply {
            text = model.onOffText
            tag = model
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "time"
        private const val ALARM_KEY = "alarm"
        private const val ONOFF_KEY = "onOff"
        private const val ALARM_REQUEST_CODE = 1000
    }

}