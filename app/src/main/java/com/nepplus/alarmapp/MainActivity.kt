package com.nepplus.alarmapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            initOnOffButtion()
            initChangeAlarmTimeButton()

            val model = fetchDataFromSharedPreferences()
            renderView(model)

        }


        private fun initOnOffButtion(){
            val onOffButton = findViewById<Button>(R.id.onOffButton)
            onOffButton.setOnClickListener {
                val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener

                //모델의 on / off 만 변경해서 newModel 로 변경
                val newModel = saveAlarmModel(hour = model.hour, minute = model.minute, !model.onOff)
                //화면에 보여주기
                renderView(newModel)

                if(newModel.onOff){
                    //켜진경우 -> 알람등록
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, newModel.hour)
                        set(Calendar.MINUTE, newModel.minute)

                        if(before(Calendar.getInstance())){
                            add(Calendar.DATE, 1)
                        }
                    }

                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(this,
                        ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    //알람매니저로 알람 맞추기
                    // setInexactRepeating 의 경우 정확한 시간에 울리지는 않는다.
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }else{
                    // 꺼진경우 -> 알람삭제
                    cancelAlarm()
                }

            }
        }

        private  fun initChangeAlarmTimeButton(){
            val changeAlarmButton = findViewById<Button>(R.id.changeAlarmTimeButton)
            changeAlarmButton.setOnClickListener {
                val calendar = Calendar.getInstance()
                //TimePickerDialog 띄워줘서 시간설정
                TimePickerDialog(this, { picker, hour, minute ->

                    val model = saveAlarmModel(hour, minute, false)
                    renderView(model)
                    //기존 알람 삭제
                    cancelAlarm()

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
                    .show()


            }
        }

        private  fun saveAlarmModel(hour : Int, minute : Int, onOff : Boolean) : AlarmDisplayModel{
            Log.d("알람모델", "$hour:$minute")
            val model = AlarmDisplayModel(
                hour = hour,
                minute = minute,
                onOff = onOff
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

            val timeDBValue = sharedPreferences.getString(ALARM_KEY, "9:30") ?: "9:30"
            val onOffDBValue = sharedPreferences.getBoolean(ONOFF_KEY, false)
            val alarmData = timeDBValue.split(":")
            val alarmModel = AlarmDisplayModel(
                hour = alarmData[0].toInt(),
                minute = alarmData[1].toInt(),
                onOff = onOffDBValue
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

        private fun cancelAlarm(){
            val pendingIntent = PendingIntent.getBroadcast(this,
                ALARM_REQUEST_CODE,
                Intent(this, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE
            )
            pendingIntent?.cancel()

        }

        companion object {
            private const val SHARED_PREFERENCES_NAME = "time"
            private const val ALARM_KEY = "alarm"
            private const val ONOFF_KEY = "onOff"
            private const val ALARM_REQUEST_CODE = 1000
        }

    }