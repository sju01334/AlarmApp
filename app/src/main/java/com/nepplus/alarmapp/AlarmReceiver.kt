package com.nepplus.alarmapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        notifyNotification(context)

    }

    private fun createNotificationChannel(context: Context) {

    }

    private fun notifyNotification(context: Context) {

    }

}